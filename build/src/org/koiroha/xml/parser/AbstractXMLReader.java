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
import java.net.URL;
import java.util.*;

import org.koiroha.xml.Xml;
import org.xml.sax.*;
import org.xml.sax.ext.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// AbstractXMLReader: XML リーダー実装クラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * XML リーダーの抽象実装クラスです。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/03 Java2 SE 5.0
 */
public abstract class AbstractXMLReader implements XMLReader {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AbstractXMLReader.class.getName());

	// ======================================================================
	// 機能フラグ
	// ======================================================================
	/**
	 * 機能フラグです。
	 * <p>
	 */
	private final Map<String,Boolean> feature = new HashMap<String,Boolean>();

	// ======================================================================
	// プロパティ
	// ======================================================================
	/**
	 * プロパティです。
	 * <p>
	 */
	private final Map<String,Object> property = new HashMap<String,Object>();

	// ======================================================================
	// 内容ハンドラ
	// ======================================================================
	/**
	 * 内容ハンドラです。
	 * <p>
	 */
	private ContentHandler contentHandler = null;

	// ======================================================================
	// DTD ハンドラ
	// ======================================================================
	/**
	 * DTD ハンドラです。
	 * <p>
	 */
	private DTDHandler dtdHandler = null;

	// ======================================================================
	// エンティティリゾルバー
	// ======================================================================
	/**
	 * エンティティリゾルバーです。
	 * <p>
	 */
	private EntityResolver entityResolver = null;

	// ======================================================================
	// エンティティリゾルバー
	// ======================================================================
	/**
	 * エンティティリゾルバーです。
	 * <p>
	 */
	private ErrorHandler errorHandler = null;

	// ======================================================================
	// 実体文字参照定義
	// ======================================================================
	/**
	 * このパーサが使用する実体文字参照の定義です。
	 * <p>
	 */
	private Map<String,String> entityReference = new HashMap<String,String>();

	// ======================================================================
	// 文字実体参照の最大文字数
	// ======================================================================
	/**
	 * このこのインスタンスが実体参照 (文字参照/数値参照) として認識する最大文字数です。
	 * <p>
	 */
	private int maxEntityReferenceLength = 0;

	// ======================================================================
	// 入力ストリーム
	// ======================================================================
	/**
	 * 入力ストリームです。
	 * <p>
	 */
	protected LocatorReader in = null;

	// ======================================================================
	// 入力ストリーム
	// ======================================================================
	/**
	 * 入力ストリームです。解析処理の終了時に確実にクローズする必要がある場合に null 以外の値が
	 * 設定されます。
	 * <p>
	 */
	private InputStream mustClose = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 機能フラグとプロパティが未設定状態のインスタンスを構築します。
	 * <p>
	 */
	protected AbstractXMLReader() {
		setEntityReference("lt", "<");
		setEntityReference("gt", ">");
		setEntityReference("amp", "&");
		setEntityReference("quot", "\"");
		setEntityReference("apos", "\'");
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定された機能フラグとプロパティを引き継いだ構成で構築を行います。このインスタンスの機能
	 * またはプロパティが変更されても指定されたマップに影響は与えません。
	 * <p>
	 * @param feature 機能フラグ
	 * @param property プロパティ
	 */
	protected AbstractXMLReader(Map<String,Boolean> feature, Map<String,Object> property) {
		this();
		this.feature.putAll(feature);
		this.property.putAll(property);
		return;
	}

	// ======================================================================
	// 機能の参照
	// ======================================================================
	/**
	 * 指定された機能が有効になっているかを判定します。機能が設定されていない場合は false を
	 * 返します。
	 * <p>
	 * @param name 機能名
	 * @return 機能が有効な場合 true
	 */
	public boolean getFeature(String name){
		return getFeature(name, false);
	}

	// ======================================================================
	// 機能の参照
	// ======================================================================
	/**
	 * 指定された機能が有効になっているかを判定します。機能が設定されていない場合はデフォルト値を
	 * 返します。
	 * <p>
	 * @param name 機能名
	 * @param def デフォルト値
	 * @return 機能が有効な場合 true
	 */
	protected boolean getFeature(String name, boolean def){
		Boolean value = feature.get(name);
		if(value == null){
			return def;
		}
		return value;
	}

	// ======================================================================
	// 名前空間有効性の判定
	// ======================================================================
	/**
	 * このインスタンスが要素名または属性名の接頭辞とその名前空間 URI を認識するかどうかを判定
	 * します。
	 * <p>
	 * @return 名前空間が有効な場合 true
	 */
	public boolean isNamespaceAware(){
		return getFeature(Xml.FEATURE_NAMESPACES, false);
	}

	// ======================================================================
	// DTD 検証有効性の参照
	// ======================================================================
	/**
	 * このパーサが DTD 検証を行うかどうかを参照します。
	 * <p>
	 * @return DTD 検証を行う場合 true
	 */
	public boolean isValidating() {
		return getFeature(Xml.FEATURE_VALIDATION);
	}

	// ======================================================================
	// 機能の設定
	// ======================================================================
	/**
	 * 指定された機能を設定します。
	 * <p>
	 * @param name 機能名
	 * @param value 機能を有効にする場合 true
	 */
	public void setFeature(String name, boolean value) {
		feature.put(name, value);
		return;
	}

	// ======================================================================
	// プロパティの参照
	// ======================================================================
	/**
	 * プロパティを参照します。指定されたプロパティが設定されていない場合は null を返します。
	 * <p>
	 * @param name プロパティ名
	 * @return プロパティの値
	 */
	public Object getProperty(String name) {
		return property.get(name);
	}

	// ======================================================================
	// プロパティの設定
	// ======================================================================
	/**
	 * プロパティを設定します。
	 * <p>
	 * @param name プロパティ名
	 * @param value プロパティ値
	 */
	public void setProperty(String name, Object value) {
		logger.finest("setProperty(" + name + "," + value + ")");
		property.put(name, value);
		return;
	}

	// ======================================================================
	// 内容ハンドラの参照
	// ======================================================================
	/**
	 * 内容ハンドラを参照します。内容ハンドラが設定されていない場合は null を返します。
	 * <p>
	 * @return 内容ハンドラ
	 */
	public ContentHandler getContentHandler() {
		return contentHandler;
	}

	// ======================================================================
	// 内容ハンドラの設定
	// ======================================================================
	/**
	 * 内容ハンドラを設定します。
	 * <p>
	 * @param handler 内容ハンドラ
	 */
	public void setContentHandler(ContentHandler handler) {
		this.contentHandler = handler;
		return;
	}

	// ======================================================================
	// DTD ハンドラの参照
	// ======================================================================
	/**
	 * DTD ハンドラを参照します。DTD ハンドラが設定されていない場合は null を返します。
	 * <p>
	 * @return　DTD ハンドラ
	 */
	public DTDHandler getDTDHandler() {
		return dtdHandler;
	}

	// ======================================================================
	// DTD ハンドラの設定
	// ======================================================================
	/**
	 * DTD ハンドラを設定します。
	 * <p>
	 * @param handler DTD ハンドラ
	 */
	public void setDTDHandler(DTDHandler handler) {
		this.dtdHandler = handler;
		return;
	}

	// ======================================================================
	// エンティティリゾルバーの参照
	// ======================================================================
	/**
	 * エンティティリゾルバーを参照します。エンティティリゾルバーが設定されていない場合は null
	 * を返します。
	 * <p>
	 * @return エンティティリゾルバー
	 */
	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	// ======================================================================
	// エンティティリゾルバーの設定
	// ======================================================================
	/**
	 * エンティティリゾルバーを設定します。
	 * <p>
	 * @param resolver エンティティリゾルバー
	 */
	public void setEntityResolver(EntityResolver resolver) {
		this.entityResolver = resolver;
		return;
	}

	// ======================================================================
	// エラーハンドラの参照
	// ======================================================================
	/**
	 * エラーハンドラを参照します。エラーハンドラが設定されていない場合は null を返します。
	 * <p>
	 * @return エラーハンドラ
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	// ======================================================================
	// エラーハンドラの設定
	// ======================================================================
	/**
	 * エラーハンドラを設定します。
	 * <p>
	 * @param handler エラーハンドラ
	 */
	public void setErrorHandler(ErrorHandler handler) {
		this.errorHandler = handler;
		return;
	}

	// ======================================================================
	// 構文ハンドラの参照
	// ======================================================================
	/**
	 * プロパティから構文ハンドラを参照するためのアクセサメソッドです。構文ハンドラが設定されて
	 * いない場合は null を返します。
	 * <p>
	 * @return 構文ハンドラ
	 */
	public LexicalHandler getLexicalHandler(){
		return (LexicalHandler)getProperty(Xml.PROPERTY_LEXICAL_HANDLER);
	}

	// ======================================================================
	// 実体参照名の最大文字数の参照
	// ======================================================================
	/**
	 * 実体参照として定義されている名前の最大文字数を参照します。サブクラスはこの値を参照して、
	 * ストリーム中に出現した '&amp;' から何文字目で実体参照としての認識を放棄して良いかを
	 * 決定できます。
	 * <p>
	 * このメソッドは少なくとも数値実体参照の最大文字数 ("#65535") を返します。
	 * <p>
	 * @return 実体参照名として可能な最大文字数
	 */
	public int getMaxEntityReferenceName(){
		return Math.max(maxEntityReferenceLength, "#65535".length());
	}

	// ======================================================================
	// 実体参照の定義
	// ======================================================================
	/**
	 * このパーサが認識する実体文字参照を定義します。&amp;foo; という実体参照を定義する場合、
	 * "foo" を指定します。
	 * <p>
	 * スーパークラスはデフォルト状態で XML で定義されている "lt", "gt", "amp", "quot",
	 * "apos" が定義されています。
	 * <p>
	 * @param name 実体文字参照の名前
	 * @param value 実体参照に対する値
	 */
	public void setEntityReference(String name, String value){
		this.entityReference.put(name, value);
		this.maxEntityReferenceLength = Math.max(name.length(), maxEntityReferenceLength);
		return;
	}

	// ======================================================================
	// 実体参照定義の削除
	// ======================================================================
	/**
	 * このパーサが認識する実体文字参照定義を削除します。
	 * <p>
	 * @param name 削除する実体文字参照の名前
	 */
	public void removeEntityReference(String name){
		this.entityReference.remove(name);
		if(name.length() == this.maxEntityReferenceLength){
			int max = 0;
			for(String n: entityReference.keySet()){
				max = Math.max(n.length(), max);
			}
			this.maxEntityReferenceLength = max;
		}
		return;
	}

	// ======================================================================
	// 実体参照の文字参照
	// ======================================================================
	/**
	 * 指定された実体参照名に対する文字列を参照します。名前には "#x0a" のような数値参照を指定
	 * することも出来ます。該当する文字が定義されていなければ null を返します。
	 * <p>
	 * @param name 実体参照の名前 ("quot", "lt" など)
	 * @return 該当する文字
	 */
	public String getEntityReference(String name){

		// 実体文字参照の場合
		if(name.length() < 2 || name.charAt(0) != '#'){
			return entityReference.get(name);
		}

		// 数値実体参照の場合
		try{
			int ch = -1;
			if(Character.toLowerCase(name.charAt(1)) == 'x'){
				ch = Integer.parseInt(name.substring(2), 16);
			} else {
				ch = Integer.parseInt(name.substring(1));
			}
			if(ch < 0 || ch > 0xFFFF){
				throw new NumberFormatException();
			}
			return String.valueOf((char)ch);
		} catch(NumberFormatException ex){/* */}

		return entityReference.get(name);
	}

	// ======================================================================
	// 解析の実行
	// ======================================================================
	/**
	 * 指定された SYSTEM ID の HTML を解析します。
	 * <p>
	 * @param systemId システム ID
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラが例外を発生した場合
	 */
	public void parse(String systemId) throws IOException, SAXException {
		parse(new InputSource(systemId));
		return;
	}

	// ======================================================================
	// 解析の実行
	// ======================================================================
	/**
	 * 指定された入力ソースから読み出される XML の解析を開始します。
	 * <p>
	 * <i>is</i> にバイナリストリームが指定されエンコーディングが省略された場合、ストリームから
	 * 先読みした内容からエンコーディングを推測します。この動作を抑止するには <i>is</i> に
	 * テキストストリームを指定するか適切なエンコーディングを設定してください。
	 * <p>
	 * @param is 入力ソース
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラが例外を発生した場合
	 */
	public void parse(InputSource is) throws IOException, SAXException {
		try{

			// 入力ストリームの準備
			prepareReader(is);

			// ロケーターの設定
			ContentHandler ch = getContentHandler();
			if(ch == null){
				ch = new DefaultHandler2();
			}
			ch.setDocumentLocator(this.in);

			// 解析処理の実行
			ch.startDocument();
			handleDocument();
			ch.endDocument();

		} finally {
			Toolkit.close(mustClose);
		}
		return;
	}

	// ======================================================================
	// ドキュメント解析の開始
	// ======================================================================
	/**
	 * ドキュメントの解析を開始するためにスーパークラスによって呼び出されます。このメソッドが呼び
	 * 出された時点で入力用のストリーム <i>in</i> が準備され、ContentHandler に対する
	 * ドキュメント開始の通知が行われています。
	 * <p>
	 * サブクラスはこのメソッド内で ContentHandler へのドキュメント終了通知を行う必要はあり
	 * ません。
	 * <p>
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラによって中断された場合
	 */
	protected abstract void handleDocument() throws SAXException, IOException;

	// ======================================================================
	// 入力ストリームの準備
	// ======================================================================
	/**
	 * インスタンス変数の入力ストリーム <i>in</i> を読み出し可能な状態に設定します。
	 * <p>
	 * @param is 入力ソース
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラによって中断された場合
	 */
	private void prepareReader(InputSource is) throws IOException, SAXException {

		// テキストストリームを参照
		Reader r = is.getCharacterStream();
		if(r != null){
			this.in = new LocatorReader(r, is.getPublicId(), is.getSystemId());
			this.in.setEncoding(is.getEncoding());
			return;
		}

		// バイナリストリームを参照
		InputStream bin = is.getByteStream();
		if(bin == null){

			// SYSTEM ID からバイナリストリームをオープン
			String systemId = is.getSystemId();
			if(systemId == null){
				throw new IOException("target system-id or stream not specified in InputSource");
			}
			mustClose = new URL(systemId).openStream();
			bin = mustClose;
		}

		// エンコーディングを決定
		String encoding = is.getEncoding();
		if(encoding == null){
			encoding = "UTF-8";
		}

		// テキスト入力ストリームを決定
		this.in = new LocatorReader(
				new InputStreamReader(bin, encoding), is.getPublicId(), is.getSystemId());
		this.in.setEncoding(encoding);
		return;
	}

}
