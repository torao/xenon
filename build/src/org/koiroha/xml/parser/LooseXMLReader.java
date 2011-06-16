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
import java.util.*;
import java.util.logging.Level;
import java.util.regex.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;

import org.koiroha.xml.Xml;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.xml.sax.ext.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LooseXMLReader: ルーズ XML パーサ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML のような well-formed でない XML を解析するための SAX パーサです。このパーサは解析
 * 対象のストリーム上でノードの検出を通知するのみであり、通常の SAX パーサのようのに XML として
 * 妥当なコールバック整合性は持ちません。
 * <p>
 * <ul>
 * <li>要素の開始-終了のコールバックペアは整合性を保証しません。これらはストリーム内で検出した
 * 都度コールバックを行います。従ってある要素の終了通知が行われないままドキュメント終了が通知される
 * 可能性があります。</li>
 * <li>妥当な XML では存在し得ない位置でのノードの通知が行われる事があります。例えばドキュメント
 * 中で最上位の要素の開始前や終了後にテキストの検出が通知される可能性があります。</li>
 * <li>不正な要素名、属性名、XML として使用できない文字が含まれていてもコールバックが行われます。</li>
 * </ul>
 * <p>
 * 解析処理が例外によって中断されなかった場合、ドキュメントの{@link
 * ContentHandler#startDocument() 開始}と{@link ContentHandler#endDocument() 終了}
 * のコールバックが保証されます。
 * <p>
 * @version $Revision: 1.5 $ $Date: 2010/07/18 15:28:36 $
 * @author torao
 * @since 2009/04/03 Java2 SE 5.0
 */
public class LooseXMLReader extends AbstractXMLReader{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LooseXMLReader.class.getName());

	// ======================================================================
	// 小文字変換機能
	// ======================================================================
	/**
	 * XML 解析時に要素名と属性名を暗黙的に小文字変換するかどうかを表す機能名です。SAX パーサ
	 * ファクトリに対して
	 * {@link SAXParserFactory#setFeature(String, boolean)} またはそのファクトリ
	 * から生じた SAX パーサの {@link XMLReader#setFeature(String, boolean)} で
	 * 使用する事が出来ます。
	 * <p>
	 * この値は定数 {@value} を示します。デフォルト値は true です。
	 * <p>
	 */
	public static final String FEATURE_LOWERCASE_NAME = "http://www.koiroha.org/sax/futures/html/lowercasename";

	// ======================================================================
	// 内容ハンドラ
	// ======================================================================
	/**
	 * 内容ハンドラです。
	 * <p>
	 */
	private ContentHandler contentHandler = null;

	// ======================================================================
	// 処理中マークアップ
	// ======================================================================
	/**
	 * 処理中のマークアップです。名前空間を保持するために使用します。
	 * <p>
	 */
	private Markup currentMarkup = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 機能フラグとプロパティが未設定状態のインスタンスを構築します。
	 * <p>
	 */
	public LooseXMLReader() {
		this(new HashMap<String,Boolean>(), new HashMap<String,Object>());
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 機能とプロパティを指定して構築を行います。
	 * <p>
	 * @param feature 機能フラグ
	 * @param property プロパティ
	 */
	public LooseXMLReader(Map<String,Boolean> feature, Map<String,Object> property) {
		super(feature, property);
		return;
	}

	// ======================================================================
	// 小文字変換の判定
	// ======================================================================
	/**
	 * この SAX パーサが検出した要素名や属性名を小文字に変換して {@link ContentHandler}
	 * へコールバックするかどうかを参照します。
	 * <p>
	 * @return 小文字に判定する場合 true
	 */
	public boolean isLowerCaseName(){
		return getFeature(FEATURE_LOWERCASE_NAME, true);
	}

	// ======================================================================
	// ドキュメント解析の開始
	// ======================================================================
	/**
	 * この SAX パーサのドキュメントの解析を開始します。
	 * <p>
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラが例外を発生した場合
	 */
	@Override
	protected void handleDocument() throws IOException, SAXException{
		logger.finest("handleDocument(); " +
			"lowercase=" + isLowerCaseName() + "," +
			"namespace=" + isNamespaceAware() + "," +
			"validation=" + isValidating());

		// 内容ハンドラの準備
		contentHandler = getContentHandler();
		if(contentHandler == null){
			contentHandler = new DefaultHandler2();
		}

		while(true){

			// 次の型を読み出し
			in.markLocation();
			Short type = in.getNextType();
			if(type == null){
				break;
			}

			switch(type){
			case Node.TEXT_NODE:
				handleText();
				break;
			case Node.CDATA_SECTION_NODE:
				handleCDATA();
				break;
			case Node.COMMENT_NODE:
				handleComment();
				break;
			case Node.PROCESSING_INSTRUCTION_NODE:
				handleProcessingInstruction();
				break;
			case Node.ELEMENT_NODE:
				handleElement();
				break;
			case Node.DOCUMENT_TYPE_NODE:
				handleDocumentType();
				break;
			default:
				assert(false): type;
				break;
			}
		}
		return;
	}

	// ======================================================================
	// 要素の処理
	// ======================================================================
	/**
	 * 要素の処理を行います。
	 * <p>
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラが例外を発生した場合
	 */
	private void handleElement() throws IOException, SAXException{
		logger.finest("handleElement()");

		// 後続の要素を読み込み
		Markup elem = readElement(currentMarkup);
		assert(elem != null);

		// 要素の終了を検出した場合
		String uri = elem.getUri();
		String localName = elem.getLocalName();
		String qName = elem.getQName();
		if(elem.isEnd()){
			contentHandler.endElement(uri, localName, qName);
		} else {
			contentHandler.startElement(uri, localName, qName, elem.getAttributes());
			if(elem.isEmpty()){
				contentHandler.endElement(uri, localName, qName);
			} else if(isNotXmlSemantics(uri, localName, qName)){
				logger.finest("recognize text or comment in element: " + elem);
				in.setTextModeEnd(qName);
			}
		}

		// スタックを操作
		if(! elem.isEnd() && ! elem.isEmpty()){
			currentMarkup = elem;
		} else if(currentMarkup != null && elem.isEnd() && currentMarkup.getQName().equals(elem.getQName())){
			currentMarkup = currentMarkup.getParent();
		}
		return;
	}

	// ======================================================================
	// テキストの読み込み
	// ======================================================================
	/**
	 * ストリームからテキストを読み込みます。
	 * <p>
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラが例外を発生した場合
	 */
	private void handleText() throws IOException, SAXException{

		// 他言語で記述された要素内を処理中の場合は先頭の '<' を無視する
		if(in.isTextMode()){
			int ch = in.read();
			if(ch == '<'){
				logger.finest("'<' detect in text mode");
				contentHandler.characters(new char[]{(char)ch}, 0, 1);
			} else {
				in.unread(ch);
			}
		}

		while(true){
			String text = read(1024, "<\uFFFE", true);	// '<' に続く EOF 以外の文字まで
			if(text == null){
				break;
			}
			char[] buffer = text.toCharArray();
			contentHandler.characters(buffer, 0, buffer.length);
		}
		return;
	}

	// ======================================================================
	// CDATA セクションの読み込み
	// ======================================================================
	/**
	 * ストリームから CDTAT セクションを読み込みます。
	 * <p>
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラが例外を発生した場合
	 */
	private void handleCDATA() throws IOException, SAXException{
		String heading = "<![CDATA[";
		String trailing = "]]>";
		in.skipSequence(heading, true, true);
		LexicalHandler lh = getLexicalHandler();
		if(lh != null){
			lh.startCDATA();
		}
		while(true){
			String text = read(1024, trailing, false);
			if(text == null){
				break;
			}
			char[] buffer = text.toCharArray();
			contentHandler.characters(buffer, 0, buffer.length);
		}
		if(lh != null){
			lh.endCDATA();
		}
		in.skipSequence(trailing, false);
		return;
	}

	// ======================================================================
	// コメントの読み込み
	// ======================================================================
	/**
	 * ストリームからコメントを読み込みます。
	 * <p>
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラが例外を発生した場合
	 */
	private void handleComment() throws IOException, SAXException{
		String heading = "<!--";
		String trailing = "-->";

		// コメントの開始を通知
		in.skipSequence(heading, true);
		if(getLexicalHandler() instanceof BuilderLexicalHandler){
			((BuilderLexicalHandler)getLexicalHandler()).startComment();
		}

		// コメントの読み出しと通知
		char[] buffer = new char[512];
		boolean warned = false;
		char pre = '\0';
		for(/* */;;){

			// コメントの読み込み
			String cmt = read(512, trailing, false);
			if(cmt == null){
				break;
			}

			// コメントのフラグメントをハンドラに通知
			for(int offset=0; offset<cmt.length(); offset+=buffer.length){
				int len = Math.min(buffer.length, cmt.length() - offset);
				cmt.getChars(offset, offset + len, buffer, 0);
				getLexicalHandler().comment(buffer, 0, len);
			}

			// コメント内の "--" をチェック
			if(! warned){
				warned = checkDoubleHyphen(pre, cmt);
				pre = cmt.charAt(cmt.length() - 1);
			}
		}

		// "--->" で終了するパターンを警告
		if(! warned && pre == '-'){
			warn("C0001");
		}

		// コメントの終了を通知
		if(getLexicalHandler() instanceof BuilderLexicalHandler){
			((BuilderLexicalHandler)getLexicalHandler()).endComment();
		}
		in.skipSequence(trailing, false);
		return;
	}

	// ======================================================================
	// コメント警告の判定
	// ======================================================================
	/**
	 * 指定されたバッファ内に "--" が存在かどうかを判定します。
	 * <p>
	 * @param pre 前の文字
	 * @param cmt コメントのフラグメント
	 * @return 警告を行った場合 true
	 * @throws SAXException ハンドラが例外を発生した場合
	 */
	private boolean checkDoubleHyphen(char pre, String cmt) throws SAXException{

		// 読み込んだバッファ内の連続するハイフンを検出
		for(int i=0; i<cmt.length(); i++){
			if(pre == '-' && cmt.charAt(i) == '-'){
				warn("C0001");
				return true;
			}
			pre = cmt.charAt(i);
		}

		return false;
	}

	// ======================================================================
	// 処理命令の読み込み
	// ======================================================================
	/**
	 * ストリームから処理命令を読み込みます。
	 * <p>
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラが例外を発生した場合
	 */
	private void handleProcessingInstruction() throws IOException, SAXException{
		String heading = "<?";
		String trailing = "?>";
		in.skipSequence(heading, true);

		// 処理命令の内部を読み出し
		String target = read(Integer.MAX_VALUE, trailing, false);
		if(target == null){
			target = "";
		}

		// 処理命令を解析
		String data = "";
		for(int i=0; i<target.length(); i++){
			if(Xml.isWhitespace(target.charAt(i))){
				int te = i;
				while(i+1<target.length() && Xml.isWhitespace(target.charAt(i+1))){
					i ++;
				}
				data = target.substring(i+1);
				target = target.substring(0, te);
				break;
			}
		}

		// XML 宣言を検出した場合
		if(target.equalsIgnoreCase("xml")){
			handleXMLDeclaration(data);

		// 通常の処理命令として通知
		} else {
			contentHandler.processingInstruction(target, data);
		}

		// 後方部分を読み飛ばし
		in.skipSequence(trailing, false);
		return;
	}

	// ======================================================================
	// XML 宣言の処理
	// ======================================================================
	/**
	 * パーサが XML 宣言を検出した時に呼び出されます。
	 * <p>
	 * @param data 処理命令のデータ部分
	 * @throws SAXException ハンドラによって中断された場合
	 */
	private void handleXMLDeclaration(String data) throws SAXException{

		// 属性値を取得
		Map<String,String> attr = Toolkit.parseAttributesSimply(data);
		String version = attr.get("version");
		String encoding = attr.get("encoding");
		// String standalone = decl.getAttributes().getValue("standalone");
		if(version == null){
			version = "1.0";
		}
		in.setXmlVersion(version);
		in.setEncoding(encoding);
		setProperty(Xml.PROPERTY_DOCUMENT_XML_VERSION, version);
		logger.finest("xml declaration detected: version=" + version +", encoding=" + encoding);

		// 未定義の XML 属性を警告
		attr.remove("version");
		attr.remove("encoding");
		attr.remove("standalone");
		for(String name: attr.keySet()){
			warn("X0001", name, Xml.escape(attr.get(name)));
		}
		return;
	}

	// ======================================================================
	// DOCTYPE の読み込み
	// ======================================================================
	/**
	 * ストリームから DOCTYPE 宣言を読み込みます。
	 * <p>
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラが例外を発生した場合
	 */
	private void handleDocumentType() throws IOException, SAXException {
		String trailing = ">";

		// 先頭部分を読み飛ばし
		in.skipSequence("<!DOCTYPE\0", true, true);

		// 内容部分を読み込み
		String decl = read(Integer.MAX_VALUE, trailing, false);
		if(decl == null){
			decl = "";
		}

		// 内容部分の解析
		String name = null;
		String publicId = null;
		String systemId = null;
		String[] format = new String[]{
			"([^\\s]+)\\s+PUBLIC\\s+[\"\']([^\"]*)[\"\']\\s*[\"\']([^\"]*)[\"\']\\s*",
			"([^\\s]+)\\s+PUBLIC\\s+[\"\']([^\"]*)[\"\']()\\s*",
			"([^\\s]+)\\s+SYSTEM\\s+()[\"\']([^\"]*)[\"\']\\s*",
			"([^\\s]+)\\s*()()",
		};
		for(int i=0; i<format.length; i++){
			Pattern pattern = Pattern.compile(format[i], Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(decl);
			if(matcher.matches()){
				name = matcher.group(1);
				publicId = matcher.group(2);
				systemId = matcher.group(3);
				break;
			}
		}
		logger.finest("dtd: " + name + ",publicId=" + publicId + ",systemId=" + systemId);

		// DTD 宣言の通知
		if(name == null){
			warn("D0001", decl);	// DTD 解析不能を警告
		} else if(getDTDHandler() != null){
			if(isLowerCaseName()){
				name = name.toLowerCase();
			}
			getDTDHandler().notationDecl(name, publicId, systemId);
		}

		// 後方部分を読み飛ばし
		in.skipSequence(trailing, false);
		return;
	}

	// ======================================================================
	// 文字の読み込み
	// ======================================================================
	/**
	 * ストリームから指定された文字シーケンスまでを読み込みます。
	 * <p>
	 * 実際の読み込み長が capacity を越える可能性があります。
	 * <p>
	 * @param sequence 終了シーケンス
	 * @param capacity 読み込み長
	 * @param resolveEntity 実体参照を解除する場合 true
	 * @return 読み込んだ文字
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラによって中断された場合
	 */
	private String read(int capacity, String sequence, boolean resolveEntity) throws IOException, SAXException{
		return read(in, capacity, sequence, resolveEntity);
	}

	// ======================================================================
	// 文字列の読み込み
	// ======================================================================
	/**
	 * 指定されたストリームから文字シーケンスまたは EOF までの文字列を読み込んで返します。
	 * これ以上読み込み可能な文字がストリームに存在しない場合は null を返します (従って長さ 0
	 * の文字列を返すことはありません)。
	 * <p>
	 * 実体参照の変換を指定した場合、返値の文字列長が capacity を越える可能性があります。
	 * <p>
	 * @param in 入力ストリーム
	 * @param capacity 読み込み長
	 * @param sequence 終了シーケンス
	 * @param resolveEntity 実体参照を解除する場合 true
	 * @return 読み込んだ文字
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラによって中断された場合
	 */
	private String read(PushbackReader in, int capacity, String sequence, boolean resolveEntity) throws IOException, SAXException{
		StringBuilder buffer = new StringBuilder();
		while((sequence == null || ! Toolkit.streamStartsWith(in, sequence, false)) && buffer.length() < capacity){

			// 次の文字を読み込み
			int ch = in.read();
			if(ch < 0){
				break;
			}

			// BOM は無視
			if(ch == 0xFEFF){
				continue;
			}

			// CR を読み込んだ場合は暗黙的に LF に変換
			if(ch == '\r'){
				buffer.append('\n');
				ch = in.read();
				if(ch < 0){
					break;
				}
				if(ch != '\n'){
					in.unread(ch);
				}
				continue;
			}

			// 実体参照を検出した場合
			if(ch == '&' && resolveEntity){
				if(in instanceof LocatorReader){
					in.unread('&');
					((LocatorReader)in).markLocation();
					in.read();
				}
				String c = readEntityReference(in);
				if(c == null){
					buffer.append('&');
				} else {
					buffer.append(c);
				}
				continue;
			}

			// それ以外はそのままバッファに設定
			buffer.append((char)ch);
		}

		// 読み込み可能な文字がなかった場合は負の値を返す
		if(buffer.length() == 0){
			return null;
		}
		return buffer.toString();
	}

	// ======================================================================
	// 要素の解析
	// ======================================================================
	/**
	 * 入力ストリームから要素部分を読み出します。ストリームの先頭は要素開始を示す &lt; に設定
	 * されている必要があります。このメソッドが終了した時、ストリームは &gt; を読み込んだ直後か
	 * EOF に設定されています。ストリームの先頭で EOF を検出した場合は null を返します。
	 * <p>
	 * @param parent 親の HTML 要素
	 * @return 解析した要素情報
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラにより処理が中断された場合
	 */
	private Markup readElement(Markup parent) throws IOException, SAXException{
		return readElement(in, isLowerCaseName(), parent);
	}

	// ======================================================================
	// 要素の解析
	// ======================================================================
	/**
	 * 指定された入力ストリームから要素部分を読み出します。ストリームの先頭は要素開始を示す &lt;
	 * に設定されている必要があります。このメソッドが終了した時、ストリームは &gt; を読み込んだ
	 * 直後か EOF に設定されています。ストリームの先頭で EOF を検出した場合は null を返します。
	 * <p>
	 * @param in 要素を読み込む入力ストリーム
	 * @param lowerCase 要素名と属性名を小文字に変換する場合 true
	 * @param parent 親の HTML 要素
	 * @return 解析した要素情報
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラによって中断された場合
	 */
	private Markup readElement(PushbackReader in, boolean lowerCase, Markup parent) throws IOException, SAXException{

		// 先行する '<' を読み飛ばし
		int leading = in.read();
		assert(leading == '<');

		// 要素の終了までを読み込み
		StringBuilder buffer = new StringBuilder();
		while(true){
			int ch = in.read();
			if(ch < 0){
				assert(buffer.length() > 0);	// ※'<' の直後に EOF が来るケースはテキストと認識されている前提
				break;
			}
			if(ch == '>'){
				break;
			}
			buffer.append((char)ch);
		}
		String body = buffer.toString();
		if(logger.isLoggable(Level.FINEST)){
			logger.finest("(" + this.in.getLineNumber() + "," + this.in.getColumnNumber() + ") read element body: <" + body + ">");
		}

		// 終了要素の判定
		boolean end = false;
		if(body.startsWith("/")){
			end = true;
			body = body.substring(1);
		}

		// 要素名の参照
		String name = null;
		for(int i=0; i<body.length(); i++){
			char ch = body.charAt(i);
			if(Xml.isWhitespace(ch)){
				name = body.substring(0, i);
				body = body.substring(i + 1).trim();
				break;
			}
		}

		// 空白を検出しなかった (全体が要素名) 場合
		boolean empty = false;
		if(name == null){
			name = body;
			body = "";
			if(name.endsWith("/")){
				name = name.substring(0, name.length() - 1);
				empty = true;
			}
		}

		// 小文字に変化
		if(lowerCase){
			name = name.toLowerCase();
		}

		// 属性の解析
		Map<String,String> attribute = new HashMap<String,String>();
		if(body.length() > 0){
			assert(! empty);
			empty = parseElementBody(body, lowerCase, attribute);
		}

		// マークアップを作成
		Markup elem = new Markup(isNamespaceAware(), parent, name, attribute, empty, end);
		if(logger.isLoggable(Level.FINEST)){
			logger.finest("parse new element: " + elem);
		}

		// 要素終了のマークアップに属性が付けられていたら警告してクリア
		if(elem.isEnd() && ! elem.isEmpty() && elem.getAttributes().getLength() > 0){
			warn("E0001", elem.getQName());
			elem.getAttributes().clear();
		}

		// 名前空間を持たない接頭辞が指定されていたら警告
		if(isNamespaceAware()){
			for(int i=0; i<elem.getAttributes().getLength(); i++){
				String qName = elem.getAttributes().getQName(i);
				int sep = qName.indexOf(':');
				if(sep >= 0){
					String prefix = qName.substring(0, sep);
					String ns = elem.getAttributes().getURI(i);
					if(ns.equals(XMLConstants.NULL_NS_URI)){
						warn("E0007", prefix);
					}
				}
			}
		}

		return elem;
	}

	// ======================================================================
	// HTML 実体参照
	// ======================================================================
	/**
	 * 指定された文字列中の実体参照を Unicode 文字に変換して返します。
	 * <p>
	 * @param text HTML 文字列
	 * @return Unicode 文字列
	 * @throws SAXException ハンドラによって中断された場合
	 * @throws IOException
	 */
	private String parseEntityReference(String text) throws SAXException, IOException{
		StringBuilder buffer = new StringBuilder(text.length());
		PushbackReader in = new PushbackReader(new StringReader(text), 64);
		while(true){
			String str = read(in, 1024, null, true);
			if(str == null){
				break;
			}
			buffer.append(str);
		}
		return buffer.toString();
	}

	// ======================================================================
	// 実体参照の読み込み
	// ======================================================================
	/**
	 * ストリームから実体参照を読み込んで該当する文字を返します。有効な実体参照を読み込んだ場合は
	 * ';' まで読み込み済みの状態になり非 null 値が返ります。実体参照が無効な場合はストリーム
	 * の位置は移動せず null が返ります。
	 * <p>
	 * @param in 入力ストリーム
	 * @return 実体参照に対応する文字
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラによって中断された場合
	 */
	private String readEntityReference(PushbackReader in) throws IOException, SAXException{
		StringBuilder buffer = new StringBuilder();
		while(buffer.length() < getMaxEntityReferenceName()){

			// 次の文字を読み込み
			int ch = in.read();
			if(ch < 0){
				break;
			}

			// 実体参照を Unicode に変換
			if(ch == ';'){
				String uc = getEntityReference(buffer.toString());
				if(uc == null){
					// 未定義の実体参照を ErrorHandler に警告
					warn("T0100", buffer);
					buffer.append((char)ch);
					in.unread(buffer.toString().toCharArray());
					return null;
				}
				return uc;
			}

			buffer.append((char)ch);
		}

		// 無効な実体参照
		warn("T0001", buffer);
		if(buffer.length() > 0){
			in.unread(buffer.toString().toCharArray());
		}
		return null;
	}

	// ======================================================================
	// 他書式要素の判定
	// ======================================================================
	/**
	 * 指定された要素が XML 以外の書式を持つ要素かどうかを判定します。これは JavaScript や
	 * CSS のような XML 以外の書式を想定しています。このメソッドが true を返した要素について
	 * は要素内がテキストとして認識されます。
	 * <p>
	 * @param uri 名前空間URI
	 * @param localName ローカル名
	 * @param qName 修飾名
	 * @return 多言語要素の場合 true
	 */
	protected boolean isNotXmlSemantics(String uri, String localName, String qName){
		return false;
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
		if(getErrorHandler() != null){
			String message = msgid + ":" + Toolkit.format("warn." + msgid, args);
			logger.finest("warn(): " + message);
			SAXParseException ex = new SAXParseException(
					message, in.getPublicId(), in.getSystemId(),
					in.getLineNumber(), in.getColumnNumber());
			getErrorHandler().warning(ex);
		}
		return;
	}

	// ======================================================================
	// 属性名パターン
	// ======================================================================
	/**
	 * 属性名のパターンです。
	 * <p>
	 */
	private static final Pattern ATTR_NAME = Pattern.compile("\\s*([^\\s=]+)\\s*");

	// ======================================================================
	// 属性値パターン
	// ======================================================================
	/**
	 * 属性値のパターンです。
	 * <p>
	 */
	private static final Pattern[] ATTR_VALUE = new Pattern[]{
		Pattern.compile("=\\s*\"([^\"]*)\"\\s*"),
		Pattern.compile("=\\s*\'([^\']*)\'\\s*"),
		Pattern.compile("=\\s*([^\\s]*)\\s*"),
	};

	// ======================================================================
	// 空要素の判定
	// ======================================================================
	/**
	 * 属性名を取得する前に空要素であるかどうかを判定する為のパターンです。
	 * <p>
	 */
	private static final Pattern EMPTY = Pattern.compile("\\s*/");

	// ======================================================================
	// 要素内の解析
	// ======================================================================
	/**
	 * 要素内の属性部分を解析します。
	 * <p>
	 * @param body 要素内文字列
	 * @param lowerCase 属性名を小文字に変換する場合 true
	 * @param attribute 解析した属性値を格納するマップ
	 * @return 空要素の場合 true
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ハンドラによって中断された場合
	 */
	private boolean parseElementBody(String body, boolean lowerCase, Map<String,String> attribute) throws IOException, SAXException{
		logger.finest("parseElementBody(" + body + ")");
		while(body.length() > 0){

			// 属性名の検出
			Matcher matcher = ATTR_NAME.matcher(body);
			if(! matcher.find()){
				if(body.trim().length() > 0){
					warn("E0009", body.trim());
				}
				break;
			}

			// 属性名の前に不正な文字が存在する場合は警告
			String head = body.substring(0, matcher.start()).trim();
			if(head.length() > 0){
				warn("E0009", head);
			}

			// 最後のスラッシュを検出した場合は空要素
			if(EMPTY.matcher(body.substring(matcher.start())).matches()){
				return true;
			}
			body = body.substring(matcher.end());

			// 属性名の取得
			String name = matcher.group(1);
			if(lowerCase){
				name = name.toLowerCase();
			}
			logger.finest("attribute name: " + name);

			// セパレータの判定
			String value = null;
			if(body.length() == 0 || body.charAt(0) != '='){
				value = name;
			} else {
				assert(body.charAt(0) == '=');

				// 属性値の取得
				for(int i=0; i<ATTR_VALUE.length; i++){
					matcher = ATTR_VALUE[i].matcher(body);
					if(matcher.lookingAt()){
						value = matcher.group(1);
						body = body.substring(matcher.end());
						break;
					}
				}
				assert(value != null): body;	// 必ずどれかと一致するはず
				value = parseEntityReference(value);
			}
			logger.finest("attribute value: " + value);
			logger.finest("remaining element body: \"" + body + "\"");

			// 既に同じ名前の属性が定義されていたら警告
			if(attribute.containsKey(name)){
				warn("E0010", name, attribute.get(name), value);
			}

			// 属性を保持
			attribute.put(name, value);
		}
		return false;
	}

}