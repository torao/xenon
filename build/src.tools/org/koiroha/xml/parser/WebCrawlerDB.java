/* **************************************************************************
 * Copyright (C) 2011 BJoRFUAN. All Rights Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * Apache License Ver. 2.0, and comes with NO WARRANTY.
 *
 *                                           takami torao <koiroha@gmail.com>
 *                                                   http://www.bjorfuan.com/
 */
package org.koiroha.xml.parser;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

import org.xml.sax.SAXParseException;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// WebCrawlerDB: Web クローラーデータベース
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Web クローラーのデータベースです。
 * <p>
 * @version $Revision: 1.3 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/08 Java2 SE 5.0
 */
public class WebCrawlerDB {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(WebCrawlerDB.class.getName());

	// ======================================================================
	// データベース URL
	// ======================================================================
	/**
	 * データベースの URL です。
	 * <p>
	 */
	private final String databaseUrl;

	// ======================================================================
	// データベース接続
	// ======================================================================
	/**
	 * データベース接続です。
	 * <p>
	 */
	private Connection con = null;

	// ======================================================================
	// 件数
	// ======================================================================
	/**
	 * 現在の解析済みページ件数です。
	 * <p>
	 */
	private int parsedPageCount = 0;

	// ======================================================================
	// キュー投入件数
	// ======================================================================
	/**
	 * 現在の解析対象キュー件数です。
	 * <p>
	 */
	private int queuedPageCount = 0;

	// ======================================================================
	// エラー発生ページ件数
	// ======================================================================
	/**
	 * 現在のエラー発生ページ件数です。
	 * <p>
	 */
	private int errorPageCount = 0;

	// ======================================================================
	// キャッシュ
	// ======================================================================
	/**
	 * キャッシュです。
	 * <p>
	 */
	private final ParsedPage[] summaryCache = new ParsedPage[2 * 1024];

	// ======================================================================
	// キャッシュインデックス
	// ======================================================================
	/**
	 * キャッシュの先頭に位置する解析済みページのインデックスです。
	 * <p>
	 */
	private int summaryCacheIndex = -1;

	// ======================================================================
	// キャッシュ
	// ======================================================================
	/**
	 * キャッシュです。
	 * <p>
	 */
	private ParsedPage fullCache = null;

	// ======================================================================
	// キャッシュインデックス
	// ======================================================================
	/**
	 * キャッシュの先頭に位置する解析済みページのインデックスです。
	 * <p>
	 */
	private int fullCacheIndex = -1;

	/** 乱数シード */
	private final Random random = new Random();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param path パス
	 * @throws SQLException SQL 例外
	 */
	public WebCrawlerDB(String path) throws SQLException{
		logger.fine("WebCrawler データベース: " + path);
		try{
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		} catch(Exception ex){
			throw new IllegalStateException(ex);
		}
		databaseUrl = "jdbc:derby:" + path;
		connect();
		return;
	}

	// ======================================================================
	// 解析対象 URL の追加
	// ======================================================================
	/**
	 * 解析対象の URL を追加します。
	 * <p>
	 * @param url 解析対象の URL
	 * @throws SQLException データベース処理に失敗した場合
	 */
	public synchronized void enqueue(URL url) throws SQLException{
		String u = url.toString();
		if(u.length() > 3 * 1024){
			logger.finer("3kB を越える URL は無視されます: " + u);
			return;
		}
		try{
			url = url.toURI().normalize().toURL();
		} catch(Exception ex){/* */}

		// 指定された URL が既に解析済みなら何もしない
		if(getScalar("select count(*) from parsed_page where url=?", u) != 0){
			return;
		}

		// 指定された URL が既にキューに存在するなら何もしない
		if(getScalar("select count(*) from queue where url=?", u) != 0){
			return;
		}

		// 新規 URL をキューに追加
		update("insert into queue(url,idx) values(?,?)", u, String.valueOf(Math.abs(random.nextInt())));
		con.commit();
		queuedPageCount ++;

		return;
	}

	// ======================================================================
	// 解析対象 URL の追加
	// ======================================================================
	/**
	 * 解析対象の URL を追加します。
	 * <p>
	 * @param url 解析対象の URL
	 * @throws SQLException データベース処理に失敗した場合
	 */
	public synchronized void dequeue(URL url) throws SQLException{
		int count = update("delete from queue where url=?", url.toString());
		con.commit();
		queuedPageCount -= count;
		return;
	}

	// ======================================================================
	// 解析対象 URL の参照
	// ======================================================================
	/**
	 * 解析対象のキューに投入されている URL をランダムに参照します。このメソッドを実行しただけ
	 * では URL はキューから削除されません。
	 * <p>
	 * @return URL キューの先頭に存在する URL
	 * @throws SQLException データベース処理に失敗した場合
	 */
	public synchronized List<URL> select() throws SQLException{
		if(queuedPageCount == 0){
			return null;
		}

		List<URL> list = new ArrayList<URL>();
		Connection con = connect();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String urlStr = null;
		try{
			stmt = con.prepareStatement("select url,idx from queue where idx is not null order by idx");
			rs = stmt.executeQuery();
			for(int i=0; rs.next() && i<10; i++){
				list.add(new URL(rs.getString(1)));
			}
			rs.close();
			stmt.close();

			for(int i=0; i<list.size(); i++){
				update("update queue set idx=null where url=?", list.get(i).toString());
			}
			con.commit();
		} catch(MalformedURLException ex){
			int count = update("delete from queue where url=?", urlStr);
			con.commit();
			queuedPageCount -= count;
		} finally {
			close(rs);
			close(stmt);
		}
		return list;
	}

	// ======================================================================
	// キューのクリア
	// ======================================================================
	/**
	 * キューをクリアします。
	 * <p>
	 * @throws SQLException データベース処理に失敗した場合
	 */
	public synchronized void clearQueue() throws SQLException{
		update("delete from queue");
		con.commit();
		queuedPageCount = 0;
		return;
	}

	// ======================================================================
	// 解析済みページの削除
	// ======================================================================
	/**
	 * 指定されたインデックスの解析済みページを削除します。
	 * <p>
	 * @param i 削除するページのインデックス
	 * @return 削除したページの URL
	 * @throws SQLException データベース処理に失敗した場合
	 */
	public synchronized URL remove(int i) throws SQLException{
		ParsedPage page = getSummaryContent(i);
		URL url = page.getURL();
		update("delete from parsed_page where url=?", url.toString());
		con.commit();
		if(page.getException() == null){
			parsedPageCount --;
		} else {
			errorPageCount --;
		}

		// 現在のキャッシュに影響を与える場合
		if(i < summaryCacheIndex){
			summaryCacheIndex --;
		} else if(i < summaryCacheIndex + summaryCache.length){
			summaryCacheIndex = -1;
		}
		return url;
	}

	// ======================================================================
	// 解析済みページの追加
	// ======================================================================
	/**
	 * 指定された解析済みページを追加します。
	 * <p>
	 * @param page 解析済みのページ
	 * @return 追加したページのインデックス
	 * @throws SQLException データベース処理に失敗した場合
	 */
	public synchronized int add(ParsedPage page) throws SQLException{

		// バイナリの作成
		byte[] binary = serialize(page);

		// 解析済みページテーブルへ追加
		Connection con = connect();
		PreparedStatement stmt = null;
		try{
			logger.finer("insert into parsed_page(...)");
			stmt = con.prepareStatement("insert into parsed_page(" +
					"url,title,doctype,charset,mojibake,warn,error,serialized" +
					")  values(?,?,?,?,?,?,?,?)");
			stmt.setString(1, page.getURL().toString());
			stmt.setString(2, page.getTitle());
			stmt.setString(3, page.getDoctype());
			stmt.setString(4, (page.getCharset()==null)? null: page.getCharset().name());
			stmt.setBoolean(5, page.isMojibake());
			stmt.setInt(6, (page.getWarnings()==null)? 0: page.getWarnings().length);
			stmt.setBoolean(7, page.getException() != null);
			stmt.setBytes(8, binary);
			stmt.executeUpdate();
		} finally {
			close(stmt);
		}

		// 該当 URL を解析対象ページのキューから削除
		dequeue(page.getURL());

		con.commit();
		if(page.getException() == null){
			parsedPageCount ++;
		} else {
			errorPageCount ++;
		}

		// 追加した URL のインデックスを参照
		int index = -1;
		/*
		if(false){
			index = getIndex(page);

			// 現在のキャッシュに影響を与える場合
			if(index < summaryCacheIndex){
				summaryCacheIndex ++;
			} else if(index < summaryCacheIndex + summaryCache.length){
				int i = index - summaryCacheIndex;
				System.arraycopy(summaryCache, i, summaryCache, i+1, summaryCache.length - i - 1);
				summaryCache[i] = page.getSummary();
			}
		}
		*/
		summaryCacheIndex = -1;

		return index;
	}

	// ======================================================================
	// レコード件数の参照
	// ======================================================================
	/**
	 * 解析済みページのレコード件数を参照します。
	 * <p>
	 * @return 解析済みのページの件数
	 */
	public int getParsedPageCount(){
		return parsedPageCount;
	}

	// ======================================================================
	// 解析対象 URL 件数の参照
	// ======================================================================
	/**
	 * 解析対象 URL の件数を参照します。
	 * <p>
	 * @return 解析対象 URL の件数
	 */
	public int getQueuePageCount(){
		return queuedPageCount;
	}

	// ======================================================================
	// 解析対象 URL 件数の参照
	// ======================================================================
	/**
	 * 解析対象 URL の件数を参照します。
	 * <p>
	 * @return 解析対象 URL の件数
	 */
	public int getErrorPageCount(){
		return errorPageCount;
	}

	// ======================================================================
	// 解析済みページのサマリー参照
	// ======================================================================
	/**
	 * 解析済みページのサマリー情報を参照します。
	 * <p>
	 * @param i 解析済みページのインデックス
	 * @return 解析済みのページ
	 * @throws SQLException データベース処理に失敗した場合
	 */
	public synchronized ParsedPage getSummaryContent(int i) throws SQLException{
		fillSummaryCache(i);
		return summaryCache[i - summaryCacheIndex];
	}

	// ======================================================================
	// 解析済みページの参照
	// ======================================================================
	/**
	 * 解析済みページ情報を参照します。
	 * <p>
	 * @param i 解析済みページのインデックス
	 * @return 解析済みのページ
	 * @throws SQLException データベース処理に失敗した場合
	 */
	public synchronized ParsedPage getFullContent(int i) throws SQLException{

		if(i == fullCacheIndex){
			logger.finest("use cached");
			return fullCache;
		}

		// インデックス指定でレコードを取得
		Connection con = connect();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try{
			stmt = con.prepareStatement("select serialized" +
					" from parsed_page" +
					" where url=?");
			stmt.setString(1, getURL(i).toString());
			rs = stmt.executeQuery();
			rs.next();

			ParsedPage page = (ParsedPage)deserialize(rs.getBytes("serialized"));
			fullCacheIndex = i;
			fullCache = page;
			logger.finest("set cache: " + i);
			return page;

		} finally {
			close(rs);
			close(stmt);
		}
	}

	// ======================================================================
	// URL の参照
	// ======================================================================
	/**
	 * 指定されたインデックスの URL を参照します。
	 * <p>
	 * @param i インデックス
	 * @return インデックスの URL
	 * @throws SQLException データベース処理に失敗した場合
	 */
	private URL getURL(int i) throws SQLException{

		// キャッシュ内に存在している場合はキャッシュの内容を参照
		if(i >= summaryCacheIndex && i < summaryCacheIndex + summaryCache.length){
			if(summaryCache[i - summaryCacheIndex] != null){
				return summaryCache[i - summaryCacheIndex].getURL();
			}
		}

		// データベースから取得
		Connection con = connect();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try{
			stmt = con.prepareStatement(
				"select url from parsed_page order by error desc, url",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery();
			rs.absolute(i);
			if(! rs.next()){
				throw new IndexOutOfBoundsException(String.valueOf(i));
			}
			return new URL(rs.getString("url"));
		} catch(MalformedURLException ex){
			logger.severe("データベースに保存されている URL が不正: " + databaseUrl + "; " + ex);
			throw new IllegalStateException(ex);
		} finally {
			close(rs);
			close(stmt);
		}
	}

	// ======================================================================
	// インデックスの参照
	// ======================================================================
	/**
	 * 指定されたページのインデックスを参照します。
	 * <p>
	 * @param page ページ
	 * @return ページのインデックス
	 * @throws SQLException データベース処理に失敗した場合
	 */
//	private int getIndex(ParsedPage page) throws SQLException{
//		return getScalar(
//			"select count(*) from parsed_page where error<? or url<?",
//			String.valueOf(page.getException()!=null), page.getURL().toString());
//	}

	// ======================================================================
	// キャッシュの生成
	// ======================================================================
	/**
	 * 指定されたインデックスのページ情報がキャッシュの中央に設定されるよう読み込みを行います。
	 * <p>
	 * @param i 解析済みページのインデックス
	 * @throws SQLException データベース処理に失敗した場合
	 */
	private synchronized void fillSummaryCache(int i) throws SQLException{

		// 指定されたインデックスがキャッシュ内に存在すれば何もしない
		if(summaryCacheIndex >= 0 && summaryCacheIndex <= i && i < summaryCacheIndex + summaryCache.length){
			return;
		}

		summaryCacheIndex = Math.max(0, i - summaryCache.length / 2);
		logger.fine("サマリーをキャッシュ1: [" + i + "] " + summaryCacheIndex + "-" + (summaryCacheIndex+summaryCache.length-1));
		long start = System.currentTimeMillis();

		// 指定されたインデックスの URL を参照
		Connection con = connect();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try{
			stmt = con.prepareStatement(
				"select url,title,doctype,charset,mojibake,warn,error" +
				" from parsed_page order by error desc, url",
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery();
			logger.finest("スキップ開始: " + (System.currentTimeMillis() - start) + "ms");
//			rs.absolute(summaryCacheIndex);
			for(int j=0; j<summaryCacheIndex; j++){
				rs.next();
			}
			logger.finest("スキップ完了: " + (System.currentTimeMillis() - start) + "ms");

			// キャッシュに設定
			rs.next();
			for(int j=0; j<summaryCache.length; j++){
				summaryCache[j] = new ParsedPage(
						new URL(rs.getString("url")),
						rs.getString("title"),
						rs.getString("doctype"),
						(rs.getString("charset")==null? null: Charset.forName(rs.getString("charset"))),
						rs.getBoolean("mojibake"),
						null, null, new SAXParseException[rs.getInt("warn")]);
				if(rs.getBoolean("error")){
					summaryCache[j].setException(new Exception());
				}

				// 次のレコードへ移動
				if(! rs.next() && j+1<summaryCache.length){
					Arrays.fill(summaryCache, j+1, summaryCache.length-1, null);
					break;
				}
			}
		} catch(MalformedURLException ex){
			logger.severe("データベースに保存されている URL が不正: " + databaseUrl + "; " + ex);
		} finally {
			close(rs);
			close(stmt);
		}
		logger.finest("キャッシュ完了: " + (System.currentTimeMillis() - start) + "ms");
		return;
	}

	// ======================================================================
	// スカラー値の参照
	// ======================================================================
	/**
	 * 指定された SQL を実行指定スカラー値を参照します。
	 * <p>
	 * @param sql 実行する SQL
	 * @param args SQL のパラメータ
	 * @return 結果のスカラー値
	 * @throws SQLException データベース処理に失敗した場合
	 */
	private int getScalar(String sql, String... args) throws SQLException{
		logger.finest(sql + ": " + Arrays.toString(args));
		Connection con = connect();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try{
			stmt = con.prepareStatement(sql);
			for(int i=0; i<args.length; i++){
				stmt.setString(i+1, args[i]);
			}
			rs = stmt.executeQuery();
			rs.next();
			return rs.getInt(1);
		} finally {
			close(rs);
			close(stmt);
		}
	}

	// ======================================================================
	// 更新 SQL の実行
	// ======================================================================
	/**
	 * 更新 SQL を実行します。
	 * <p>
	 * @param sql 実行する SQL
	 * @param args SQL のパラメータ
	 * @return 結果の件数
	 * @throws SQLException データベース処理に失敗した場合
	 */
	private int update(String sql, String... args) throws SQLException{
		Connection con = connect();
		PreparedStatement stmt = null;
		try{
			long start = System.currentTimeMillis();
			logger.finer(sql + ": " + Arrays.toString(args));
			stmt = con.prepareStatement(sql);
			for(int i=0; i<args.length; i++){
				stmt.setString(i+1, args[i]);
			}
			int count = stmt.executeUpdate();
			logger.finest((System.currentTimeMillis() - start) + "ms");
			return count;
		} finally {
			close(stmt);
		}
	}

	// ======================================================================
	// 接続の終了
	// ======================================================================
	/**
	 * 接続を終了します。
	 * <p>
	 */
	public void disconnect(){
		try{
			if(con != null){
				con.close();
			}
			logger.fine("データベース接続をクローズしました");
		} catch(SQLException ex){/* */}
		try{
			DriverManager.getConnection(databaseUrl + ";shutdown=true");
		} catch(SQLException ex){
			logger.fine("接続をシャットダウンしました");
		}
		return;
	}

	// ======================================================================
	// データベースの初期化
	// ======================================================================
	/**
	 * データベースを初期化します。
	 * <p>
	 * @throws SQLException SQL 例外
	 */
	private void init() throws SQLException{

		// 解析済みページ保存用テーブルの作成
		update("create table parsed_page(" +
			"url varchar(" + (3*1024) + ") not null primary key," +
			"title varchar(1024)," +
			"doctype varchar(64)," +
			"charset varchar(64)," +
			"mojibake char(1) not null," +
			"warn integer not null," +
			"error char(1) not null," +
			"serialized blob not null)");
		update("create index parsed_page_idx01 on parsed_page(error)");

		// 解析対象 URL 保存テーブルの作成
		update("create table queue(" +
			"url varchar(" + (3*1024) + ") not null primary key," +
			"idx integer)");

		con.commit();
		logger.fine("データベースの初期化が完了しました");
		return;
	}

	// ======================================================================
	// データベースへ接続
	// ======================================================================
	/**
	 * データベースへ接続します。
	 * <p>
	 * @return データベース接続
	 * @throws SQLException SQL 例外
	 */
	private Connection connect() throws SQLException{

		// 既に接続している場合は何もしない
		if(con != null && ! con.isClosed()){
			return con;
		}

		// 接続の実行
		try{
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		} catch(Exception ex){
			throw new IllegalStateException(ex);
		}
		con = DriverManager.getConnection(databaseUrl + ";create=true");
		con.setAutoCommit(false);
		con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		logger.fine("データベースに接続しました");

		// 現在の件数を取得
		try{
			this.parsedPageCount = getScalar("select count(*) from parsed_page where error='0'");
			this.errorPageCount = getScalar("select count(*) from parsed_page where error<>'0'");
			this.queuedPageCount = getScalar("select count(*) from queue");
			logger.fine("解析済みURL件数: " + parsedPageCount);
			logger.fine("解析待ちURL件数: " + queuedPageCount);
			logger.fine("解析エラーURL件数: " + errorPageCount);
		} catch(SQLException ex){
			logger.fine("件数取得に失敗; データベースを初期化します: " + ex);
			init();
		}
		return con;
	}

	// ======================================================================
	// ステートメントのクローズ
	// ======================================================================
	/**
	 * ステートメントをクローズします。
	 * <p>
	 * @param stmt ステートメント
	 */
	private static void close(Statement stmt){
		try{
			if(stmt != null)	stmt.close();
		} catch(SQLException ex){
			ex.printStackTrace();
		}
		return;
	}

	// ======================================================================
	// 結果セットのクローズ
	// ======================================================================
	/**
	 * 結果セットをクローズします。
	 * <p>
	 * @param rs 結果セット
	 */
	private static void close(ResultSet rs){
		try{
			if(rs != null)	rs.close();
		} catch(SQLException ex){
			ex.printStackTrace();
		}
		return;
	}

	// ======================================================================
	// インスタンスのシリアライズ
	// ======================================================================
	/**
	 * インスタンスをシリアライズします。
	 * <p>
	 * @param obj シリアライズするオブジェクト
	 * @return シリアライズしたバイナリ
	 */
	private static byte[] serialize(Object obj){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(obj);
			out.flush();
			return baos.toByteArray();
		} catch(IOException ex){
			throw new IllegalStateException(ex);
		}
	}

	// ======================================================================
	// インスタンスのデシリアライズ
	// ======================================================================
	/**
	 * インスタンスをデシリアライズします。
	 * <p>
	 * @param bin 復元するバイナリ
	 * @return 復元したオブジェクト
	 */
	private static Object deserialize(byte[] bin){
		try{
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bin));
			return in.readObject();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

//	public static void main(String[] args) throws SQLException{
//		Logger logger = Logger.getLogger("org.koiroha.xml.parser");
//		for(Handler h: logger.getHandlers()){
//			logger.removeHandler(h);
//		}
//		Handler handler = new ConsoleHandler();
//		logger.addHandler(handler);
//		handler.setLevel(Level.ALL);
//		logger.setLevel(Level.ALL);
//		handler.setFormatter(new Formatter(){
//			@Override
//			public String format(LogRecord r) {
//				return String.format("[%1$tY-%1$tm-%1$td %1$tT] %2$s - %3$s%n",
//					new Date(r.getMillis()), r.getLevel(), r.getMessage());
//			}
//		});
//		WebCrawlerDB db = new WebCrawlerDB("var/derby/WEBCRAWLERDB");
//		db.update("alter table queue add column idx integer");
//		db.con.commit();
//		db.disconnect();
//		return;
//	}

}
