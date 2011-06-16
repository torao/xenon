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

import java.awt.Color;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.*;

import org.xml.sax.SAXParseException;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// WebCrawlerModel: Web 巡回
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version $Revision: 1.4 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/05 Java2 SE 5.0
 */
public class WebCrawlerModel extends AbstractTableModel {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(WebCrawlerModel.class.getName());

	// ======================================================================
	// 解析済みページ
	// ======================================================================
	/**
	 * 解析済みのページです。
	 * <p>
	 */
	public final WebCrawlerDB db;

	// ======================================================================
	// 選択中解析済みページ
	// ======================================================================
	/**
	 * 現在選択されている解析済みページです。
	 * <p>
	 */
	private ParsedPage current = null;

	// ======================================================================
	// ページ内容
	// ======================================================================
	/**
	 * ページ内容です。
	 * <p>
	 */
	public final StyledDocument content = new DefaultStyledDocument();

	// ======================================================================
	// 解析済みページ内容
	// ======================================================================
	/**
	 * 解析済みのページ内容です。
	 * <p>
	 */
	public final StyledDocument parsedContent = new DefaultStyledDocument();

	// ======================================================================
	// 警告リストモデル
	// ======================================================================
	/**
	 * 警告リストモデルです。
	 * <p>
	 */
	public final WarningModel warning = new WarningModel();

	// ======================================================================
	// 保存スレッド
	// ======================================================================
	/**
	 * 解析済みページをデータベースに保存するためのスレッドです。
	 * <p>
	 */
	private final Thread saver;

	// ======================================================================
	// 保存キュー
	// ======================================================================
	/**
	 * 保存キューです。
	 * <p>
	 */
	private final BlockingQueue<QueueObj> queue = new ArrayBlockingQueue<QueueObj>(10);

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * データベースディレクトリを指定して構築を行います。
	 * <p>
	 * @param dir データベースディレクトリ
	 * @throws SQLException 接続に失敗した場合
	 */
	public WebCrawlerModel(File dir) throws SQLException{
		String path = dir.getAbsolutePath();
		this.db = new WebCrawlerDB(path);
		this.saver = new Thread(){
			@Override
			public void run(){
				asynchAddPage();
				return;
			}
		};
		saver.setPriority(Thread.MIN_PRIORITY);
		saver.start();
		return;
	}

	// ======================================================================
	// カラム数の参照
	// ======================================================================
	/**
	 * カラム数を参照します。
	 * <p>
	 * @return カラム数
	 */
	public int getColumnCount() {
		return 7;
	}

	// ======================================================================
	// カラム名の参照
	// ======================================================================
	/**
	 * カラム名を参照します。
	 * <p>
	 * @param column カラムのインデックス
	 * @return カラム名
	*/
	@Override
	public String getColumnName(int column) {
		switch(column){
		case 0:		return "URL";
		case 1:		return "タイトル";
		case 2:		return "タイプ";
		case 3:		return "文字セット";
		case 4:		return "文字化け";
		case 5:		return "警告数";
		case 6:		return "エラー";
		}
		assert(false);
		return null;
	}

	// ======================================================================
	// 行数の参照
	// ======================================================================
	/**
	 * 行数を参照します。
	 * <p>
	 * @return 行数
	 */
	public int getRowCount() {
		return db.getParsedPageCount();
	}

	// ======================================================================
	// 値の参照
	// ======================================================================
	/**
	 * 値を参照します。
	 * <p>
	 * @param row 行
	 * @param column 列
	 * @return 値
	 */
	public Object getValueAt(int row, int column) {

		// ページ情報のサマリーを取得
		ParsedPage s = null;
		try{
			s = db.getSummaryContent(row);
			if(s == null){
				return null;
			}
		} catch(SQLException ex){
			throw new IllegalStateException(ex);
		}

		// 表示用のフィールドを参照
		switch(column){
		case 0:
			return s.getURL();
		case 1:
			return s.getTitle();
		case 2:
			return s.getDoctype();
		case 3:
			return s.getCharset();
		case 4:
			return s.isMojibake();
		case 5:
			if(s.getWarnings() == null){
				return 0;
			}
			return s.getWarnings().length;
		case 6:
			return (s.getException() != null);
		}
		assert(false);
		return null;
	}

	// ======================================================================
	// 解析済みページの追加
	// ======================================================================
	/**
	 * 解析済みページを追加します。
	 * <p>
	 * @param pg 追加する解析済みページ
	 * @param next キューに投入する URL
	 * @throws InterruptedException 処理中に割り込まれた場合
	 */
	public void addPage(ParsedPage pg, List<URL> next) throws InterruptedException{
		logger.finest("queueing...");
		QueueObj obj = new QueueObj();
		obj.page = pg;
		obj.next = next;
		queue.put(obj);
	}

	// ======================================================================
	// 解析済みページの追加
	// ======================================================================
	/**
	 * キューに投入されている解析済みページをデータベースに保存します。
	 * <p>
	 */
	private void asynchAddPage(){
		try{
			while(true){

				// キューに投入されている解析済みページをデータベースに保存
				logger.finest("waiting queue...");
				QueueObj obj = queue.take();

				// 次の URL をキューに投入
				List<URL> next = obj.next;
				for(int i=0; i<next.size(); i++){
					enqueue(next.get(i));
				}

				// 海瀬木積ページをデータベースに登録
				ParsedPage page = obj.page;
				int idx = -1;
				try{
					idx = db.add(page);
				} catch(SQLException ex){
					logger.log(Level.SEVERE, "データベースへ保存に失敗", ex);
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					ex.printStackTrace(pw);
					JOptionPane.showMessageDialog(
						null, "データベースへ保存に失敗\n" + page.getURL() + "\n" + sw, "エラー", JOptionPane.ERROR_MESSAGE);
					continue;
				}

				// 挿入した結果を Swing に反映
				if(idx >= 0){
					final int i = idx;
					Runnable r = new Runnable(){
						public void run() {
							fireTableRowsInserted(i, i);
						}
					};
					if(SwingUtilities.isEventDispatchThread()){
						r.run();
					} else {
						try{
							SwingUtilities.invokeAndWait(r);
						} catch(InvocationTargetException ex){
							throw new IllegalStateException(ex);
						}
					}
				}
			}
		} catch(InterruptedException ex){/* */}
		return;
	}

	// ======================================================================
	// 解析済みページの参照
	// ======================================================================
	/**
	 * 解析済みページを参照します。
	 * <p>
	 * @param i 解析済みページのインデックス
	 * @return 解析済みページ
	 */
	public ParsedPage getPage(int i) {
		logger.finest("getPage(" + i + ")");
		try{
			return db.getFullContent(i);
		} catch(SQLException ex){
			throw new IllegalStateException(ex);
		}
	}

	// ======================================================================
	// 解析対象 URL の追加
	// ======================================================================
	/**
	 * 解析対象の URL を追加します。
	 * <p>
	 * @param url 解析対象の URL
	 */
	public void enqueue(URL url) {
		try{
			db.enqueue(url);
		} catch(SQLException ex){
			throw new IllegalStateException(ex);
		}
		return;
	}

	// ======================================================================
	// 解析対象 URL の追加
	// ======================================================================
	/**
	 * 解析対象の URL を追加します。
	 * <p>
	 * @param url 解析対象の URL
	 */
	public void dequeue(URL url) {
		try{
			db.dequeue(url);
		} catch(SQLException ex){
			throw new IllegalStateException(ex);
		}
		return;
	}

	// ======================================================================
	// 解析対象 URL の参照
	// ======================================================================
	/**
	 * 解析対象の URL を参照します。このメソッドを実行しただけでは URL はキューに投入された
	 * ままです。
	 * <p>
	 * @return URL キューの先頭に存在する URL
	 */
	public List<URL> select() {
		try{
			return db.select();
		} catch(SQLException ex){
			throw new IllegalStateException(ex);
		}
	}

	// ======================================================================
	// 解析済みページの削除
	// ======================================================================
	/**
	 * 指定されたインデックスの解析済みページを削除します。
	 * <p>
	 * @param i 削除するページのインデックス
	 * @return 削除したページの URL
	 */
	public URL remove(int i) {
		try{
			URL url = db.remove(i);
			fireTableRowsDeleted(i, i);
			return url;
		} catch(SQLException ex){
			throw new IllegalStateException(ex);
		}
	}

	// ======================================================================
	// キューのクリア
	// ======================================================================
	/**
	 * キューをクリアします。
	 * <p>
	 */
	public void clearQueue() {
		try{
			db.clearQueue();
		} catch(SQLException ex){
			throw new IllegalStateException(ex);
		}
		return;
	}

	// ======================================================================
	// 選択ページの設定
	// ======================================================================
	/**
	 * 指定されたインデックスのページを設定します。
	 * <p>
	 * @param i 選択したページ
	 * @return 選択ページに変更があった場合
	 */
	public boolean setSelectedPage(int i){
		if(i >= 0){
			ParsedPage page = getPage(i);
			if(current == page || (page != null && current != null && page.getURL().equals(current.getURL()))){
				return false;
			}
			current = page;
		}

		// HTML の内容を消去
		try {
			content.remove(0, content.getLength());
			parsedContent.remove(0, parsedContent.getLength());
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}

		// 未選択状態にする場合
		if(i < 0){
			current = null;
			warning.setWarnings(new SAXParseException[0]);
			return true;
		}

		// ページの内容を取得
		warning.setWarnings(current.getWarnings());

		try{
			content.insertString(0, current.getContent(), SimpleAttributeSet.EMPTY);
			String text = current.getParsedContent();
			if(current.getException() != null){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				current.getException().printStackTrace(pw);
				pw.flush();
				text = sw.toString();
			}
			parsedContent.insertString(0, text, SimpleAttributeSet.EMPTY);
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}

		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setUnderline(attr, true);
		StyleConstants.setForeground(attr, Color.RED);
		if(current.getWarnings() != null){
			for(SAXParseException ex: current.getWarnings()){
				logger.finest("(" + ex.getLineNumber() + "," + ex.getColumnNumber() + ") " + ex.getMessage());
				int pos = getPosition(current.getContent(), ex.getLineNumber(), ex.getColumnNumber());
				if(pos >= 0){
					int len = getLength(current.getContent(), pos);
					content.setCharacterAttributes(pos, len, attr, false);
				}
			}
		}

		return true;
	}

	// ======================================================================
	// 警告のハイライト
	// ======================================================================
	/**
	 * 警告の位置をハイライトします。
	 * <p>
	 * @param i ハイライトする警告のインデックス
	 * @return 位置と長さ
	 */
	public int[] hilightWarning(int i){
		SAXParseException ex = warning.warnings[i];
		int pos = getPosition(current.getContent(), ex.getLineNumber(), ex.getColumnNumber());
		if(pos < 0){
			return null;
		}
		int len = getLength(current.getContent(), pos);
		return new int[]{pos, len};
	}

	// ======================================================================
	// 位置の参照
	// ======================================================================
	/**
	 * 現在選択されているページ内容 (解析前) の指定された文字列位置が何行目の何桁かを返します。
	 * <p>
	 * @param begin 文字列の開始位置
	 * @param length 文字列の長さ
	 * @return 開始位置と終了位置
	 */
	public Position[] getContentPosition(int begin, int length){
		String text = current.getContent();
		if(text == null || begin == text.length()){
			return null;
		}
		assert(begin >= 0 && begin < text.length()): "bgn=" + begin + ":txt=" + text.length();
		assert(length > 0 && begin + length <= text.length()): "bgn=" + begin + ":len=" + length + ":txt=" + text.length();
		Position[] pos = new Position[2];
		int line = 1;
		int col = 1;
		for(int i=0; i<text.length(); i++){
			char ch = text.charAt(i);
			assert(ch != '\r');		// ※改行はLFに統一されている想定
			if(begin == i){
				pos[0] = new Position(line, col);
			}
			if(begin + length - 1 == i){
				pos[1] = new Position(line, col);
				break;
			}
			if(ch == '\n'){
				line ++;
				col = 1;
			} else {
				col ++;
			}
		}
		return pos;
	}

	// ======================================================================
	// 開始位置の参照
	// ======================================================================
	/**
	 * 指定された文字列の位置を参照します。
	 * <p>
	 * @param text 文字列
	 * @param l 行番号
	 * @param c 桁番号
	 * @return 文字列内の位置
	 */
	private static int getPosition(String text, int l, int c){
		int line = 1;
		int col = 1;
		int head = 0;
		for(int i=0; i<text.length(); i++){
			if(line == l && col == c){
				return i;
			}
			char ch = text.charAt(i);
			assert(ch != '\r');		// ※改行はLFに統一されている想定
			if(ch == '\n'){
				if(line == l){
					logger.warning("(" + l + "," + c + ") " + line + "行目の文字数が" + c + "文字に満たない: " + text.substring(head, i));
					return -1;
				}
				head = i + 1;
				line ++;
				col = 1;
			} else {
				col ++;
			}
		}
		logger.warning("(" + l + "," + c + ") 文字列が" + line + "行" + col + "桁まで");
		return -1;
	}

	// ======================================================================
	// 長さの参照
	// ======================================================================
	/**
	 * 長さを参照します。
	 * <p>
	 * @param text 文字列
	 * @param index 開始位置
	 * @return 文字列内の位置
	 */
	private static int getLength(String text, int index){
		char ch = text.charAt(index);
		int length = 1;
		if(ch == '<'){
			for(int i=index+1; i<text.length(); i++){
				length ++;
				if(text.charAt(i) == '>'){
					break;
				}
			}
		}
		return length;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// WarningModel: 警告モデル
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 警告モデルです。
	 * <p>
	 */
	private static class QueueObj{
		/** 解析済みページ */
		public ParsedPage page = null;
		/** URL */
		public List<URL> next = null;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// WarningModel: 警告モデル
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 警告モデルです。
	 * <p>
	 */
	private class WarningModel extends AbstractListModel{

		// ==================================================================
		// シリアルバージョン
		// ==================================================================
		/**
		 * このクラスのシリアルバージョンです。
		 * <p>
		 */
		private static final long serialVersionUID = 1L;

		// ==================================================================
		// シリアルバージョン
		// ==================================================================
		/**
		 * このクラスのシリアルバージョンです。
		 * <p>
		 */
		private SAXParseException[] warnings = new SAXParseException[0];

		// ==================================================================
		// 警告数の参照
		// ==================================================================
		/**
		 * 現在選択されているページの警告数を参照します。
		 * <p>
		 * @return 警告数
		*/
		public int getSize() {
			if(warnings == null){
				return 0;
			}
			return warnings.length;
		}

		// ======================================================================
		// 項目の参照
		// ======================================================================
		/**
		 * 指定されたインデックスの項目を参照します。
		 * <p>
		 * @param index インデックス
		 * @return 項目
		*/
		public Object getElementAt(int index) {
			SAXParseException ex = warnings[index];
			return "(" + ex.getLineNumber() + "," + ex.getColumnNumber() + ") " + ex.getMessage();
		}

		// ======================================================================
		// 警告の設定
		// ======================================================================
		/**
		 * 警告を設定します。
		 * <p>
		 * @param warnings 警告
		*/
		public void setWarnings(SAXParseException[] warnings){
			if(warnings == null){
				warnings = new SAXParseException[0];
			}
			SAXParseException[] old = this.warnings;
			this.warnings = warnings;
			if(old.length > warnings.length){
				fireIntervalRemoved(this, warnings.length, old.length-1);
			} else if(old.length < warnings.length){
				fireIntervalAdded(this, old.length, warnings.length-1);
			}
			if(warnings.length > 0){
				fireContentsChanged(this, 0, warnings.length-1);
			}
			return;
		}

	}

}
