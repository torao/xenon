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
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.koiroha.xml.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.ext.DefaultHandler2;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// ParsedPage: 解析済みページクラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 解析済みのページを著すクラスです。
 * <p>
 * @version $Revision: 1.5 $ $Date: 2010/07/18 15:30:18 $
 * @author torao
 * @since 2009/04/08 Java2 SE 5.0
 */
public final class ParsedPage implements Serializable, Cloneable{

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
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ParsedPage.class.getName());

	// ======================================================================
	// URL
	// ======================================================================
	/**
	 * URL です。
	 * <p>
	 */
	private final URL url;

	// ======================================================================
	// タイトル
	// ======================================================================
	/**
	 * ページのタイトルです。
	 * <p>
	 */
	private String title = null;

	// ======================================================================
	// ドキュメントタイプ
	// ======================================================================
	/**
	 * ドキュメントタイプです。
	 * <p>
	 */
	private String doctype = null;

	// ======================================================================
	// 文字セット
	// ======================================================================
	/**
	 * 文字セットです。
	 * <p>
	 */
	private String charset = null;

	// ======================================================================
	// 文字化けフラグ
	// ======================================================================
	/**
	 * 解析前のページに文字化けが発生しているかどうかを表すフラグです。
	 * <p>
	 */
	private boolean mojibake = false;

	// ======================================================================
	// ページ内容
	// ======================================================================
	/**
	 * 解析前のページの内容です。
	 * <p>
	 */
	private String content = null;

	// ======================================================================
	// 解析後のページ内容
	// ======================================================================
	/**
	 * HTML パーサで解析し再構成したページの内容です。
	 * <p>
	 */
	private String parsedContent = null;

	// ======================================================================
	// 警告
	// ======================================================================
	/**
	 * ページの解析で発生した警告です。
	 * <p>
	 */
	private SAXParseException[] warn = null;

	// ======================================================================
	// 例外
	// ======================================================================
	/**
	 * 解析処理中に発生した例外です。
	 * <p>
	 */
	private Throwable ex = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * URL を指定して構築を行います。
	 * <p>
	 * @param url URL
	 */
	public ParsedPage(URL url) {
		this.url = url;
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * URL を指定して構築を行います。
	 * <p>
	 * @param url URL
	 * @param title
	 * @param doctype
	 * @param charset
	 * @param mojibake
	 * @param content
	 * @param parsedContent
	 * @param warn
	 */
	public ParsedPage(URL url, String title, String doctype, Charset charset, boolean mojibake, String content, String parsedContent, SAXParseException[] warn) {
		this.url = url;
		this.title = title;
		this.doctype = doctype;
		this.charset = (charset!=null)? charset.name(): null;
		this.mojibake = mojibake;
		this.content = content;
		this.parsedContent = parsedContent;
		this.warn = warn;
		return;
	}

	// ======================================================================
	// URL の参照
	// ======================================================================
	/**
	 * ページの URL を参照します。
	 * <p>
	 * @return ページの URL
	 */
	public URL getURL() {
		return url;
	}

	// ======================================================================
	// タイトルの参照
	// ======================================================================
	/**
	 * タイトルを参照します。
	 * <p>
	 * @return タイトル
	 */
	public String getTitle() {
		return title;
	}

	// ======================================================================
	// ドキュメント型の参照
	// ======================================================================
	/**
	 * ドキュメント型を参照します。
	 * <p>
	 * @return ドキュメント型
	 */
	public String getDoctype() {
		return doctype;
	}

	// ======================================================================
	// 文字セットの参照
	// ======================================================================
	/**
	 * 文字セットを参照します。
	 * <p>
	 * @return 文字セット
	 */
	public Charset getCharset() {
		if(charset == null){
			return null;
		}
		return Charset.forName(charset);
	}

	// ======================================================================
	// 文字化けの参照
	// ======================================================================
	/**
	 * 文字化けを参照します。
	 * <p>
	 * @return 文字化けしている場合 true
	 */
	public boolean isMojibake() {
		return mojibake;
	}

	// ======================================================================
	// ページ内容の参照
	// ======================================================================
	/**
	 * ページ内容を参照します。
	 * <p>
	 * @return ページ内容
	 */
	public String getContent() {
		return content;
	}

	// ======================================================================
	// 解析済みページ内容の参照
	// ======================================================================
	/**
	 * HTML パーサで解析し再構成したページの内容を参照します。
	 * <p>
	 * @return 再構成したページ内容
	 */
	public String getParsedContent() {
		return parsedContent;
	}

	// ======================================================================
	// 警告の参照
	// ======================================================================
	/**
	 * 警告を参照します。
	 * <p>
	 * @return 警告
	 */
	public SAXParseException[] getWarnings(){
		return warn;
	}

	// ======================================================================
	// 例外の参照
	// ======================================================================
	/**
	 * このページの解析中に発生した例外を参照します。
	 * <p>
	 * @return 解析処理で発生した例外
	*/
	public Throwable getException(){
		return ex;
	}

	// ======================================================================
	// 例外の設定
	// ======================================================================
	/**
	 * ページ解析で発生した例外を設定します。
	 * <p>
	 * @param ex 解析処理で発生した例外
	*/
	public void setException(Throwable ex){
		this.ex = ex;
		return;
	}

	// ======================================================================
	// サマリーの構築
	// ======================================================================
	/**
	 * サマリーを構築します。
	 * <p>
	 * @return サマリー
	*/
	public ParsedPage getSummary(){
		try{
			ParsedPage page = (ParsedPage)clone();
			page.content = null;
			page.parsedContent = null;
			page.warn = new SAXParseException[warn==null? 0: warn.length];
			return page;
		} catch(CloneNotSupportedException ex){
			assert(false);
			throw new IllegalStateException(ex);
		}
	}

	// ======================================================================
	// 処理の実行
	// ======================================================================
	/**
	 * 処理を実行します。
	 * <p>
	 * @return このページに含まれているリンク
	 * @throws IgnoreableException この URL が HTML でない場合
	 * @throws IOException HTML の読み込みに失敗した場合
	 * @throws InterruptedException 処理が中断された場合
	 */
	public List<URL> parse() throws IgnoreableException, IOException, InterruptedException{

		// URL による接続の準備
		URLConnection con = null;
		try{
			con = url.openConnection();
			con.setAllowUserInteraction(false);
			con.setConnectTimeout(10 * 1000);
			con.setDefaultUseCaches(false);
			con.setDoInput(true);
			con.setDoOutput(false);
			con.setReadTimeout(10 * 1000);
			con.setUseCaches(false);
			con.setRequestProperty("User-Agent", "WebCrawler/1.0 (Testing HTML Parser Testing... :-)");
			con.connect();
		} catch(IOException ex){
			throw new IgnoreableException(ex.toString());
		}
		checkInterrupted();

		// Content-Type を参照して HTML かどうかを確認
		String contentType = con.getHeaderField("content-type");
		if(contentType == null || ! contentType.matches("(?i)text/html\\s*;?.*")){
			logger.finest("非 HTML 内容: " + contentType);
			throw new IgnoreableException(contentType);
		}

		// Content-Type による文字セットの参照
		Charset cs = Xml.getCharset(contentType);
		if(cs != null){
			charset = cs.name();
		} else {
			charset = "JISAutoDetect";
		}

		// 内容をバイナリとして読み込み
		logger.finest("reading content...: " + contentType);
		final int maxSize = 1024 * 1024;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			InputStream in = con.getInputStream();
			byte[] buf = new byte[1024];
			while(buffer.size() < maxSize){
				int len = in.read(buf);
				if(len < 0){
					break;
				}
				buffer.write(buf, 0, len);
			}
			in.close();
		} catch(IOException ex){
			throw new IgnoreableException(ex.toString());
		}
		checkInterrupted();

		// 入力ストリームの準備
		byte[] binary = buffer.toByteArray();
		InputStream in = new ByteArrayInputStream(binary);
		InputSource is = new HTMLParserFactory().guessInputSource(in, charset, 8 * 1024);
		is.setSystemId(url.toString());
		charset = is.getEncoding();
		this.content = toString(binary, charset);

		// ドキュメント内容の解析
		Document doc = null;
		try{
			HTMLDocumentBuilderFactory factory = new HTMLDocumentBuilderFactory();
			factory.setNamespaceAware(true);
			factory.setHtmlOptimize(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			final List<SAXParseException> warning = new ArrayList<SAXParseException>();
			builder.setErrorHandler(new DefaultHandler2(){
				/** @param e
				 * @throws SAXException
				*/
				@Override
				public void warning(SAXParseException e) throws SAXException {
					warning.add(e);
					return;
				}
			});
			doc = builder.parse(is);

			// 解析中に発生した警告を取得
			warn = warning.toArray(new SAXParseException[warning.size()]);

			// 解析に使用した文字セットを取得
			if(charset == null){
				charset = doc.getInputEncoding();
				if(charset != null && Charset.isSupported(charset)){
					this.content = toString(binary, charset);
				} else {
					charset = "JISAutoDetect";
				}
			}

			// 文字化けを判定
			mojibake = (content.indexOf('\uFFFD') >= 0);

		} catch(ParserConfigurationException ex){
			throw new IllegalStateException(ex);
		} catch(SAXException ex){
			assert(false): ex;
			throw new IllegalStateException(ex);
		} finally {
			if(doc == null){
				logger.severe(new String(buffer.toByteArray(), charset));
				logger.severe("URL: "+ url);
			}
		}
		assert(doc != null);
		checkInterrupted();

		// HTML ドキュメント内容の解析
		List<URL> link = new ArrayList<URL>();
		try{
			DefaultNamespaceContext nc = new DefaultNamespaceContext();
			nc.setNamespaceURI("x", "http://www.w3.org/1999/xhtml");
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			xpath.setNamespaceContext(nc);
			NodeList nl = (NodeList)xpath.evaluate("//a[boolean(@href)]|//x:a[boolean(@href)]", doc, XPathConstants.NODESET);
			for(int i=0; i<nl.getLength(); i++){
				Element a = (Element)nl.item(i);
				String uri = a.getAttribute("href");
				if(uri.indexOf('#') >= 0){
					uri = uri.substring(0, uri.indexOf('#'));
				}
				try{
					link.add(new URL(url, uri));
				} catch(MalformedURLException ex){
					logger.finest("不正な URL を無視しました: " + uri);
				}
			}

			// タイトルの参照
			title = xpath.evaluate("/html/head/title|/x:html/x:head/x:title", doc);

			// ドキュメント型の参照
			doctype = getDocumentType(doc);

		} catch(XPathException ex){
			throw new IllegalStateException(ex);
		}

		// 解析済み HTML を文字列に変換
		// ※未定義の接頭辞が使用されていると Transformer が失敗するため独自実装
		StringWriter sw = new StringWriter();
		XMLStreamWriter out = new XMLStreamWriter(sw);
		write(doc, out);
		out.flush();
		this.parsedContent = sw.toString();

		checkInterrupted();

		return link;
	}

	// ======================================================================
	// 割り込みの確認
	// ======================================================================
	/**
	 * スレッドが割り込まれていないかを確認します。
	 * <p>
	 * @throws InterruptedException スレッドが割り込まれている場合
	 */
	private static void checkInterrupted() throws InterruptedException{
		if(Thread.interrupted()){
			throw new InterruptedException();
		}
		return;
	}

	// ======================================================================
	// 文字列化
	// ======================================================================
	/**
	 * 指定されたバイナリを文字列化します。全ての改行は LF に統一されています。
	 * <p>
	 * @param binary バイナリ
	 * @param charset 文字セット
	 * @return バイナリの文字列
	 * @throws IOException 変換に失敗した場合
	 */
	private static String toString(byte[] binary, String charset) throws IOException{
		Reader in = new InputStreamReader(new ByteArrayInputStream(binary), charset);
		in = new LFReader(in);
		StringBuilder b = new StringBuilder();
		char[] buf = new char[1024];
		while(true){
			int len = in.read(buf);
			if(len < 0)	break;
			b.append(buf, 0, len);
		}
		return b.toString();
	}

	// ======================================================================
	// ドキュメント型の参照
	// ======================================================================
	/**
	 * 指定された HTML ドキュメントに対するドキュメント型を参照します。
	 * <p>
	 * @param doc HTML ドキュメント
	 * @return ドキュメント型
	 */
	private String getDocumentType(Document doc){

		// ドキュメント型の参照
		DocumentType doctype = doc.getDoctype();
		if(doctype == null){
			return null;
		}

		String type = null;
		if("-//W3C//DTD HTML 4.01//EN".equals(doctype.getPublicId()) || "http://www.w3.org/TR/html4/strict.dtd".equals(doctype.getSystemId())){
			type = "HTML 4.01 Strict";
		} else if("-//W3C//DTD HTML 4.0 Frameset//EN".equals(doctype.getPublicId()) || "http://www.w3.org/TR/REC-html40/frameset.dtd".equals(doctype.getSystemId())){
			type = "HTML 4.0 Frameset";
		} else if("-//W3C//DTD HTML 4.0 Transitional//EN".equals(doctype.getPublicId()) || "http://www.w3.org/TR/REC-html40/transitional.dtd".equals(doctype.getSystemId())){
			type = "HTML 4.0 Transitional";
		} else if("-//W3C//DTD HTML 4.01 Transitional//EN".equals(doctype.getPublicId()) || "http://www.w3.org/TR/html4/loose.dtd".equals(doctype.getSystemId())){
			type = "HTML 4.01 Transitional";
		} else if("-//W3C//DTD HTML 4.01 Frameset//EN".equals(doctype.getPublicId()) || "http://www.w3.org/TR/html4/frameset.dtd".equals(doctype.getSystemId())){
			type = "HTML 4.01 Frameset";
		} else if("-//W3C//DTD XHTML 1.0 Strict//EN".equals(doctype.getPublicId()) || "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd".equals(doctype.getSystemId())){
			type = "XHTML 1.0 Strict";
		} else if("-//W3C//DTD XHTML 1.0 Transitional//EN".equals(doctype.getPublicId()) || "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd".equals(doctype.getSystemId())){
			type = "XHTML 1.0 Transitional";
		} else if("-//W3C//DTD XHTML 1.0 Frameset//EN".equals(doctype.getPublicId()) || "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd".equals(doctype.getSystemId())){
			type = "XHTML 1.0 Frameset";
		} else if("-//W3C//DTD XHTML 1.1//EN".equals(doctype.getPublicId()) || "http://www.w3.org/TR/xhtml1/DTD/xhtml11.dtd".equals(doctype.getSystemId())){
			type = "XHTML 1.1 Strict";
		} else if("-//IETF//DTD HTML//EN".equals(doctype.getPublicId()) || "-//IETF//DTD HTML 2.0//EN".equals(doctype.getPublicId())){
			type = "HTML 2.0";
		} else if("-//IETF//DTD HTML 3.0//EN".equals(doctype.getPublicId())){
			type = "HTML 3.0";
		} else if("-//W3C//DTD HTML 3.2//EN".equals(doctype.getPublicId()) || "-//W3C//DTD HTML 3.2 Final//EN".equals(doctype.getPublicId())){
			type = "HTML 3.2";
		} else if(doctype.getPublicId().length() == 0 && doctype.getSystemId().length() == 0){
			/* */
		} else {
			logger.warning("認識できないドキュメント型宣言: " + doctype.getPublicId() + ", " + doctype.getSystemId());
		}

		return type;
	}

	// ======================================================================
	// ノードの出力
	// ======================================================================
	/**
	 * 指定されたノードを出力します。
	 * <p>
	 * @param node 出力するノード
	 * @param out 出力先のストリーム
	 */
	private static void write(Node node, XMLStreamWriter out){
		switch(node.getNodeType()){
		case Node.COMMENT_NODE:
			out.writeComment(((Comment)node).getData());
			break;
		case Node.TEXT_NODE:
			out.writeCharacters(((Text)node).getData());
			break;
		case Node.CDATA_SECTION_NODE:
			out.writeCData(((CDATASection)node).getData());
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			ProcessingInstruction pi = (ProcessingInstruction)node;
			out.writeProcessingInstruction(pi.getTarget(), pi.getData());
			break;
		case Node.DOCUMENT_TYPE_NODE:
			DocumentType dt = (DocumentType)node;
			if(dt.getPublicId() != null && dt.getPublicId().length() > 0 && dt.getSystemId() != null && dt.getSystemId().length() > 0){
				out.writeDTD("<!DOCTYPE " + dt.getName() + " \"" + dt.getPublicId() + "\" \"" + dt.getSystemId() + "\">");
			} else if(dt.getPublicId() != null && dt.getPublicId().length() > 0){
				out.writeDTD("<!DOCTYPE " + dt.getName() + " \"" + dt.getPublicId() + "\">");
			} else if(dt.getSystemId() != null && dt.getSystemId().length() > 0){
				out.writeDTD("<!DOCTYPE " + dt.getName() + " \"" + dt.getSystemId() + "\">");
			} else {
				out.writeDTD("<!DOCTYPE " + dt.getName() + ">");
			}
			out.writeCharacters("\n");
			break;
		case Node.ELEMENT_NODE:

			// 要素名の出力
			Element elem = (Element)node;
			boolean empty = (elem.getChildNodes().getLength() == 0);
			if(empty){
				out.writeEmptyElement(elem.getTagName());
			} else {
				out.writeStartElement(elem.getTagName());
			}

			// 属性の出力
			NamedNodeMap attr = elem.getAttributes();
			for(int i=0; i<attr.getLength(); i++){
				Attr a = (Attr)attr.item(i);
				out.writeAttribute(a.getName(), a.getValue());
			}

			// 全ての子ノードを出力
			NodeList nl = elem.getChildNodes();
			for(int i=0; i<nl.getLength(); i++){
				write(nl.item(i), out);
			}

			if(! empty){
				out.writeEndElement();
			}
			break;
		case Node.DOCUMENT_NODE:
			// 全ての子ノードを出力
			nl = node.getChildNodes();
			for(int i=0; i<nl.getLength(); i++){
				write(nl.item(i), out);
			}
			break;
		default:
			assert(false): node.getNodeType() + ":" + node;
			break;
		}
		return;
	}

}
