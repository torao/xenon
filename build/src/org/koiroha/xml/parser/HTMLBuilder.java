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

import java.lang.reflect.Proxy;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;

import org.koiroha.xml.Xml;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// HTMLBuilder: HTML DOM 作成クラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML パーサからのコールバックを受けて HTML DOM を構築するためのクラスです。
 * <p>
 * @version $Revision: 1.5 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/01 Java2 SE 5.0
 */
class HTMLBuilder extends DefaultHandler2 implements BuilderLexicalHandler {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(HTMLBuilder.class.getName());

	// ======================================================================
	// 機能フラグ
	// ======================================================================
	/**
	 * 機能フラグです。
	 * <p>
	 */
	private final Map<String,Boolean> feature = new HashMap<String,Boolean>();

	// ======================================================================
	// ドキュメントビルダー
	// ======================================================================
	/**
	 * ドキュメントビルダーです。
	 * <p>
	 */
	private final DocumentBuilder builder;

	// ======================================================================
	// エンティティリゾルバー
	// ======================================================================
	/**
	 * エンティティリゾルバーです。
	 * <p>
	 */
//	private final EntityResolver entityResolver;

	// ======================================================================
	// エラーハンドラ
	// ======================================================================
	/**
	 * エラーハンドラです。
	 * <p>
	 */
	private final ErrorHandler errorHandler;

	// ======================================================================
	// ドキュメント
	// ======================================================================
	/**
	 * 構築中のドキュメントです。
	 * <p>
	 */
	private Document doc = null;

	// ======================================================================
	// 処理対象要素
	// ======================================================================
	/**
	 * 現在処理の対象となっている要素を表します。処理コンテキストがドキュメント要素の範囲外の
	 * 場合は null となります。
	 * <p>
	 */
	private Element current = null;

	// ======================================================================
	// CDATA セクションバッファ
	// ======================================================================
	/**
	 * CDATA セクション中の文字列を格納するためのバッファです。
	 * <p>
	 */
	private StringBuilder cdata = null;

	// ======================================================================
	// 文字列バッファ
	// ======================================================================
	/**
	 * コメント用の文字列バッファです。
	 * <p>
	 */
	private StringBuilder comment = null;

	// ======================================================================
	// ロケータ
	// ======================================================================
	/**
	 * ロケータです。
	 * <p>
	 */
	private Locator2 locator = null;

	// ======================================================================
	// エピローグ
	// ======================================================================
	/**
	 * ドキュメント終了時にドキュメント要素に追加する文字列です。
	 * <p>
	 */
	private StringBuilder epilogue = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param builder ドキュメントビルダー
	 * @param entityResolver エンティティリゾルバー
	 * @param errorHandler エラーハンドラ
	 * @param feature このビルダーの機能フラグ
	 */
	public HTMLBuilder(DocumentBuilder builder,
			EntityResolver entityResolver, ErrorHandler errorHandler,
			Map<String,Boolean> feature) {
		this.builder = builder;
//		this.entityResolver = entityResolver;
		this.errorHandler = errorHandler;
		this.feature.putAll(feature);
		return;
	}

	// ======================================================================
	// 機能フラグの参照
	// ======================================================================
	/**
	 * ファクトリの機能フラグを参照します。
	 * <p>
	 * @param name 機能名
	 * @return 機能フラグ
	 */
	public boolean getFeature(String name) {
		Boolean value = this.feature.get(name);
		if(value == null){
			return false;
		}
		return value;
	}

	// ======================================================================
	// 小文字変換の参照
	// ======================================================================
	/**
	 * HTML 解析時に要素名や属性名を小文字に変換するかどうかを参照します。
	 * <p>
	 * @return 小文字に変換する場合 true
	*/
	private boolean isHtmlOptimize(){
		return getFeature(HTMLDocumentBuilderFactory.FEATURE_HTML_OPTIMIZE);
	}

	// ======================================================================
	// CDATA セクション展開の参照
	// ======================================================================
	/**
	 * CDATA セクションをテキストに展開するかどうかを参照します。
	 * <p>
	 * @return CDATA セクションをテキストとして扱う場合 true
	*/
	private boolean isCoalescing() {
		return getFeature(HTMLDocumentBuilderFactory.FEATURE_COALESCING);
	}

	// ======================================================================
	// コメント無視の参照
	// ======================================================================
	/**
	 * コメントを無視するかどうかを参照します。
	 * <p>
	 * @return コメントを無視する場合 true
	*/
	private boolean isIgnoringComments() {
		return getFeature(HTMLDocumentBuilderFactory.FEATURE_IGNORE_COMMENT);
	}

//	// ======================================================================
//	// 要素設定順序維持の参照
//	// ======================================================================
//	/**
//	 * 属性の設定順序を維持するかを判定します。
//	 * <p>
//	 * @return 順序を維持する場合 true
//	*/
//	private boolean isKeepAttributeOrder() {
//		return getFeature(HTMLDocumentBuilderFactory.FEATURE_KEEP_ATTRIBUTE_ORDER);
//	}

	// ======================================================================
	// 名前空間の有効性参照
	// ======================================================================
	/**
	 * 名前空間を認識するかどうかを参照します。
	 * <p>
	 * @return 名前空間が有効な場合 true
	*/
	private boolean isNamespaceAware() {
		return getFeature(Xml.FEATURE_NAMESPACES);
	}

	// ======================================================================
	// ドキュメントの参照
	// ======================================================================
	/**
	 * ドキュメントを参照します。
	 * <p>
	 * @return ドキュメント
	 */
	public Document getDocument() {
		return doc;
	}

	// ======================================================================
	// ロケータの設定
	// ======================================================================
	/**
	 * ロケータを世呈します。
	 * <p>
	 * @param locator ロケータ
	*/
	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = (Locator2)locator;
		return;
	}

	// ======================================================================
	// ドキュメントの開始
	// ======================================================================
	/**
	 * 新規ドキュメントを作成します。
	 * <p>
	*/
	@Override
	public void startDocument() {
		logger.finest("startDocument(); " +
				"namespace=" + isNamespaceAware() + "," +
				"xinclude=" + builder.isXIncludeAware() + "," +
				"validation=" + builder.isValidating());

		// 初期状態のドキュメントを構築
		this.doc = builder.newDocument();

		return;
	}

	// ======================================================================
	// ドキュメントの終了
	// ======================================================================
	/**
	 * ドキュメントを終了します。
	 * <p>
	*/
	@Override
	public void endDocument() {
		logger.finest("endDocument()");
		assert(cdata == null): cdata;
		assert(comment == null): comment;

		// 要素の存在しないストリームの場合はエピローグが存在しない
		if(epilogue != null && epilogue.length() > 0){
			assert(doc.getDocumentElement() != null);
			String text = epilogue.toString();
			text = trimRight(text);
			doc.getDocumentElement().appendChild(doc.createTextNode(text));
		}

		// XML バージョン情報を設定
		String version = locator.getXMLVersion();
		if(version != null){
			doc.setXmlVersion(version);
		}

		// 動的プロキシによるプロパティを設定
		HTMLInvocationHandler handler = new HTMLInvocationHandler(doc);
		handler.setInputEncoding(locator.getEncoding());
		doc = (Document)Proxy.newProxyInstance(
				HTMLBuilder.class.getClassLoader(), new Class[]{Document.class}, handler);

		return;
	}

	// ======================================================================
	// 要素の開始
	// ======================================================================
	/**
	 * 新規の要素を構築します。
	 * <p>
	 * @param uri 名前空間 URI
	 * @param localName ローカル名
	 * @param name 修飾名
	 * @param attrs 属性
	 * @throws SAXException ハンドラによって中断された場合
	*/
	@Override
	public void startElement(String uri, String localName, String name, Attributes attrs) throws SAXException{
		logger.finest("startElement(" + uri + "," + localName + "," + name + ",attrs)");

		// 新規の要素を構築
		Element elem = null;
		try{

			// 要素の構築
			if(isNamespaceAware()){
				elem = doc.createElementNS(uri, name);
			} else {
				elem = doc.createElement(name);
			}

//			// 属性の設定順序を維持する場合は動的プロキシでラップ
//			if(isKeepAttributeOrder()){
//
//				// 実装されている全てのインターフェースを参照
//				Class<?>[] interfaces = elem.getClass().getInterfaces();
//				elem = (Element)Proxy.newProxyInstance(
//					Thread.currentThread().getContextClassLoader(),
//					interfaces,
//					new AttributeOrderKeeper(elem));
//			}

		} catch(DOMException ex){
			logger.finest(ex.toString());
			if(ex.code == DOMException.INVALID_CHARACTER_ERR){
				warn("E0005", name);
			} else {
				warn("E9999", ex.getMessage());
			}
			return;
		}

		// 要素の属性を設定
		for(int i=0; i<attrs.getLength(); i++){
			String auri = attrs.getURI(i);
			String aname = attrs.getQName(i);
			String avalue = attrs.getValue(i);
			logger.finest("setAttributeNS(" + auri + "," + aname + "," + avalue + ")");
			try{
				if(isNamespaceAware()){
					elem.setAttributeNS(auri, aname, avalue);
				} else {
					elem.setAttribute(aname, avalue);
				}
			} catch(DOMException ex){
				logger.finest(ex.toString());
				if(ex.code == DOMException.INVALID_CHARACTER_ERR){
					warn("E0006", aname, Xml.escape(avalue));
				} else {
					warn("E9999", ex.getMessage());
				}
			}
		}

		// 位置情報を設定

		// html 要素の場合
		if(localName.equalsIgnoreCase("html")){
			if(doc.getDocumentElement() == null){
				// 新規ドキュメント要素として設定
				doc.appendChild(elem);
				logger.finest("html element detected");
			} else {
				// 既存のドキュメント要素と入れ替え
				Element root = doc.getDocumentElement();
				while(root.getFirstChild() != null){
					Node node = root.removeChild(root.getFirstChild());
					elem.appendChild(node);
				}
				doc.removeChild(root);
				doc.appendChild(elem);
				logger.finest("replace html element");
			}
		} else {

			// 検出した要素の適切な挿入位置を参照
			if(current != null && isHtmlOptimize()){
				Element parent = Toolkit.getPreferredParent(current, elem);
				Element mover = current;
				if(mover != parent){

					// HTML 要素認識による要素の移動を警告
					warn("E0008", mover.getTagName());

					// 省略された要素の終了を警告
					do {
						Position pos = (Position)mover.getUserData(Position.USERDATA_NAME);
						warn(pos, "E0004", mover.getTagName());
						mover = (Element)mover.getParentNode();
					} while(mover != parent);
				}
				current = parent;
			}

			// 要素の連結
			appendNode(elem);
		}

		// 空要素でなければ下位の要素に移動
		if(! Toolkit.isEmptyElement(uri, localName, name)){
			current = elem;
			logger.finest("current element pointer set to <" + name + ">");
		}

		return;
	}

	// ======================================================================
	// 要素の終了
	// ======================================================================
	/**
	 * 現在処理中の要素を終了します。
	 * <p>
	 * @param uri 名前空間 URI
	 * @param localName ローカル名
	 * @param name 修飾名
	 * @throws SAXException ハンドラによって中断された場合
	*/
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException{
		logger.finest("endElement(" + uri + "," + localName + "," + name + ")");

		// 空要素の終了通知は無視
		if(Toolkit.isEmptyElement(uri, localName, name)){
			logger.finest("ignore end of empty element");
			checkEmptyElementEnd(uri, localName, name);
			return;
		}

		// 終了対象の要素を検出
		Element target = search(name);

		// 上位に定義されていない要素の終了が通知された場合
		if(target == null){
			warn("E0002", name);
			return;
		}

		// スキップしてさかのぼる要素の終了省略を警告
		Element mover = current;
		while(mover != target){
			Position pos = (Position)mover.getUserData(Position.USERDATA_NAME);
			warn(pos, "E0004", mover.getTagName());
			mover = (Element)mover.getParentNode();
		}

		if(target.getParentNode() instanceof Element){
			current = (Element)target.getParentNode();
			logger.finest("move current elemnt to <" + current.getTagName() + ">");

		// エピローグの開始
		} else {
			current = null;
			epilogue = new StringBuilder();
			logger.finest("end document element, start epilogue");
		}
		return;
	}

	// ======================================================================
	// 要素の終了
	// ======================================================================
	/**
	 * 現在処理中の要素を終了します。
	 * <p>
	 * @param uri 名前空間 URI
	 * @param localName ローカル名
	 * @param name 修飾名
	 * @throws SAXException ハンドラによって中断された場合
	*/
	private void checkEmptyElementEnd(String uri, String localName, String name) throws SAXException{

		// 上位が非要素の場合
		if(current == null){
			warn("E0002", name);
			return;
		}

		Node last = current.getLastChild();

		// 無視可能な空白文字を読み飛ばす
		while(last instanceof Text && ((Text)last).getData().trim().length() == 0){
			last = last.getPreviousSibling();
		}

		// 直前の有効なノードが要素でない場合は警告
		if(! (last instanceof Element)){
			warn("E0002", name);
			return;
		}

		// 直前の有効なノードが空要素の開始でない場合は警告
		Element elem = (Element)last;
		if(isNamespaceAware()){
			if(! elem.getLocalName().equals(localName)){
				warn("E0002", name);
			}
		} else {
			if(! elem.getTagName().equals(name)){
				warn("E0002", name);
			}
		}
		return;
	}

	// ======================================================================
	// CDATA セクションの開始
	// ======================================================================
	/**
	 * CDATA セクションを開始します。
	 * <p>
	*/
	@Override
	public void startCDATA() {
		logger.finest("startCDATA()");
		if(! isCoalescing()){
			this.cdata = new StringBuilder();
		}
		return;
	}

	// ======================================================================
	// CDATA セクションの終了
	// ======================================================================
	/**
	 * CDATA セクションを終了します。
	 * <p>
	*/
	@Override
	public void endCDATA() {
		logger.finest("endCDATA(): <![CDATA[" + cdata + "]]>");
		CDATASection cdata = doc.createCDATASection(this.cdata.toString());
		appendNode(cdata);
		this.cdata = null;
		return;
	}

	// ======================================================================
	// テキストの通知
	// ======================================================================
	/**
	 * 指定されたテキストを内部のバッファに格納します。
	 * <p>
	 * @param ch 文字列バッファ
	 * @param start バッファの開始位置
	 * @param length 文字列の長さ
	*/
	@Override
	public void characters(char[] ch, int start, int length){
		logger.finest("characters(ch," + start + "," + length + ")");

		// CDATA セクションを評価中の場合はバッファに保持するだけで続行
		if(cdata != null){
			cdata.append(ch, start, length);
			return;
		}

		// テキストノードの連結
		Text text = doc.createTextNode(new String(ch, start, length));
		appendNode(text);

		return;
	}

	// ======================================================================
	// コメントの開始
	// ======================================================================
	/**
	 * コメントが開始する時に呼び出されます。
	 * <p>
	 * @throws SAXException 呼び出し側により処理を中断する場合
	*/
	public void startComment() throws SAXException{
		if(! isIgnoringComments()){
			comment = new StringBuilder();
		}
		return;
	}

	// ======================================================================
	// コメントの終了
	// ======================================================================
	/**
	 * コメントが終了する時に呼び出されます。
	 * <p>
	 * @throws SAXException 呼び出し側により処理を中断する場合
	*/
	public void endComment() throws SAXException{
		logger.finest("endComment(): <!--" + comment + "-->");

		if(! isIgnoringComments()){

			// ※コメント内に "--" が存在した場合はここで変換される
			Comment cmt = doc.createComment(comment.toString());

			// コメントの連結
			appendNode(cmt);

			this.comment = null;
		}
		return;
	}

	// ======================================================================
	// コメントの通知
	// ======================================================================
	/**
	 * コメントのフラグメントを内部に保持します。
	 * <p>
	 * @param ch 文字列バッファ
	 * @param start バッファ内の開始位置
	 * @param length 文字の長さ
	*/
	@Override
	public void comment(char[] ch, int start, int length) {
		if(! isCoalescing()){
			comment.append(ch, start, length);
		}
		return;
	}

	// ======================================================================
	// 処理命令の通知
	// ======================================================================
	/**
	 * 処理命令を通知します。
	 * <p>
	 * @param target ターゲット
	 * @param data データ
	 * @throws SAXException ハンドラによって中断された場合
	*/
	@Override
	public void processingInstruction(String target, String data) throws SAXException{
		logger.finest("processingInstruction(" + target + "," + data + ")");
		try{
			ProcessingInstruction pi = doc.createProcessingInstruction(target, data);
			appendNode(pi);
		} catch(DOMException ex){
			logger.fine(ex.toString());
			warn("P0001", target);
		}
		return;
	}

	// ======================================================================
	// DTD の開始
	// ======================================================================
	/**
	 * DTD の開始を通知します。
	 * <p>
	 * @param name 名前
	 * @param publicId PUBLIC ID
	 * @param systemId SYSTEM ID
	 * @throws SAXException ハンドラによって中断された場合
	*/
	@Override
	public void notationDecl(String name, String publicId, String systemId) throws SAXException{
		logger.finest("notationDecl(" + name + "," + publicId+ "," + systemId + ")");

		// DOCTYPE 要素を構築
		DOMImplementation domImpl = doc.getImplementation();
		DocumentType docType = domImpl.createDocumentType(name, publicId, systemId);

		// 既にドキュメント型が設定されている場合
		if(doc.getDoctype() != null){
			warn("D0002", name, publicId, systemId);
			return;
		}

		// ドキュメントの先頭に追加
		doc.insertBefore(docType, doc.getFirstChild());
		return;
	}

	// ======================================================================
	// ノードの追加
	// ======================================================================
	/**
	 * 現在評価中のノードに対して指定されたノードを追加します。
	 * <p>
	 * @param node 追加するノード
	*/
	private void appendNode(Node node){
		Position pos = new Position(locator.getLineNumber(), locator.getColumnNumber());
		node.setUserData(Position.USERDATA_NAME, pos, null);

		// ドキュメント要素内で評価中のコンテキストを持つ場合
		if(current != null){
			current.appendChild(node);
			return;
		}

		// ドキュメント直下への配置が許可されているノードの場合
		if(node instanceof ProcessingInstruction || node instanceof Comment || node instanceof DocumentType){
			logger.finest("append node to document directlly");
			doc.appendChild(node);
			return;
		}

		// プロローグを評価中の場合
		if(doc.getDocumentElement() == null){

			// テキストの場合は前方の空白を削除
			// ※CDATAの場合があるので instanceof は使わない
			if(node.getNodeType() == Node.TEXT_NODE){
				String text = ((Text)node).getData();
				text = trimLeft(text);
				if(text.length() == 0){
					logger.finest("fully whitespace characters in prologue is ignored");
					return;
				}
				node = doc.createTextNode(text);
				logger.finest("text in prologue: \"" + text + "\"");
			}

			// 暗黙的に新規 html 要素を作成
			Element root = doc.createElementNS(XMLConstants.NULL_NS_URI, "html");
			doc.appendChild(root);
			root.appendChild(node);
			current = root;
			logger.finest("create html element implicitly because appearance of node for element");
			assert(root.getLocalName() != null): root;
			return;
		}

		// エピローグを評価中の場合
		logger.finest("evaluating epilogue node...");
		assert(epilogue != null);
		Element root = doc.getDocumentElement();

		// テキストの場合はエピローグ用のバッファに追加するのみ
		// ※CDATAの場合があるので instanceof は使わない
		if(node.getNodeType() == Node.TEXT_NODE){
			String text = ((Text)node).getData();
			epilogue.append(text);
			return;
		}

		// 現在までのエピローグの文字列をテキストとして追加
		if(epilogue.length() > 0){
			root.appendChild(doc.createTextNode(epilogue.toString()));
			epilogue.setLength(0);
		}

		// ドキュメント要素の最後にノードを追加
		root.appendChild(node);

		return;
	}

	// ======================================================================
	// 要素の検索
	// ======================================================================
	/**
	 * 現在評価中の要素から上位にさかのぼって指定された要素を検索します。
	 * <p>
	 * @param qName 修飾名
	 * @return 該当する要素
	*/
	private Element search(String qName){
		if(current == null){
			return null;
		}
		Element mover = current;
		while(true){
			if(mover.getTagName().equals(qName)){
				return mover;
			}
			if(! (mover.getParentNode() instanceof Element)){
				break;
			}
			mover = (Element)mover.getParentNode();
		}
		return null;
	}

	// ======================================================================
	// 文字列の片側トリミング
	// ======================================================================
	/**
	 * 文字列の片側に存在する XML 空白をトリミングします。
	 * <p>
	 * @param text トリミングする文字列
	 * @return トリミングした文字列
	*/
	private static String trimLeft(String text){
		for(int i=0; i<text.length(); i++){
			if(! Xml.isWhitespace(text.charAt(i))){
				return text.substring(i);
			}
		}
		return "";
	}

	// ======================================================================
	// 文字列の片側トリミング
	// ======================================================================
	/**
	 * 文字列の片側に存在する XML 空白をトリミングします。
	 * <p>
	 * @param text トリミングする文字列
	 * @return トリミングした文字列
	*/
	private static String trimRight(String text){
		for(int i=text.length()-1; i>=0; i--){
			if(! Xml.isWhitespace(text.charAt(i))){
				return text.substring(0, i+1);
			}
		}
		return "";
	}

	// ======================================================================
	// 警告の通知
	// ======================================================================
	/**
	 * エラーハンドラに対して警告を通知します。
	 * <p>
	 * @param msgid メッセージ ID
	 * @param args メッセージフォーマット用の引数
	 * @throws SAXException ハンドラによって中断された場合
	 */
	private void warn(String msgid, Object... args) throws SAXException{
		warn(null, msgid, args);
		return;
	}

	// ======================================================================
	// 警告の通知
	// ======================================================================
	/**
	 * エラーハンドラに対して警告を通知します。
	 * <p>
	 * @param pos 位置情報
	 * @param msgid メッセージ ID
	 * @param args メッセージフォーマット用の引数
	 * @throws SAXException ハンドラによって中断された場合
	 */
	private void warn(Position pos, String msgid, Object... args) throws SAXException{
		if(errorHandler != null){
			if(pos == null){
				pos = new Position(locator.getLineNumber(), locator.getColumnNumber());
			}
			String message = msgid + ":" + Toolkit.format("warn." + msgid, args);
			SAXParseException ex = new SAXParseException(
					message, locator.getPublicId(), locator.getSystemId(),
					pos.getLineNumber(), pos.getColumnNumber());
			errorHandler.warning(ex);
		}
		return;
	}

}
