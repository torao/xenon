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

import static org.junit.Assert.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.junit.*;
import org.koiroha.xml.TestCase;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.ext.DefaultHandler2;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// SpecPatternTest: データパターンテスト
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version $Revision: 1.5 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/01 Java2 SE 5.0
 */
public class SpecPatternTest extends TestCase{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SpecPatternTest.class.getName());

	// ======================================================================
	// テスト結果の出力先
	// ======================================================================
	/**
	 * テスト結果の出力先ディレクトリです。
	 * <p>
	 */
	private static final File dir = new File("./build/doc/");

	// ======================================================================
	// XML 出力先
	// ======================================================================
	/**
	 * テスト結果の出力先です。
	 * <p>
	 */
	private static XMLStreamWriter out = null;

	// ======================================================================
	// 仕様ドキュメント
	// ======================================================================
	/**
	 * 仕様のドキュメントです。
	 * <p>
	 */
	private static Document specification = null;

	// ======================================================================
	// XML 出力先
	// ======================================================================
	/**
	 * テスト結果の出力先です。
	 * <p>
	 */
	private static int line = 0;

	/**
	 * 初期化処理
	 * @throws IOException */
	@BeforeClass
	public static void prepare() throws IOException{
		dir.mkdirs();

		// ストリームのオープン
		String xmlns = "http://www.w3.org/1999/xhtml";
		File file = new File(dir, "specification.xhtml");
		out = new XMLStreamWriter(new FileWriter(file));
		out.setDefaultNamespace(xmlns);

		//
		out.writeStartDocument(Charset.defaultCharset().name(), "1.0");
		out.writeCharacters("\n");
		out.writeDTD("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
		out.writeCharacters("\n");
		out.writeStartElement("html");
		out.writeAttribute("xmlns", xmlns);
		out.writeCharacters("\n");
		out.writeStartElement("head");
		out.writeCharacters("\n");
		out.writeStartElement("title");
		out.writeCharacters("HTML Parser Specification");
		out.writeEndElement();
		out.writeCharacters("\n");
		out.writeStartElement("style");
		out.writeAttribute("type", "text/css");
		out.writeCharacters("\n");
		out.writeCharacters("\t.ctrl { width:8px; height:8px; }\n");
		out.writeCharacters("\t.tab  { width:32px; height:8px; }\n");
		out.writeCharacters("\t.smpl { font-size:11px; vertical-align:top; background-color:#F4F4F4; }\n");
		out.writeCharacters("\t.titl { font-size:12px; font-family:sans-serif; padding-top:8px; border-bottom:solid 1px black; }\n");
		out.writeCharacters("\t.desc { font-size:10px; }\n");
		out.writeCharacters("\t.err  { font-family:sans-serif; color:red; }\n");
		out.writeEndElement();
		out.writeCharacters("\n");
		out.writeEndElement();
		out.writeCharacters("\n");
		out.writeStartElement("body");
		out.writeCharacters("\n");
		return;
	}

	/**
	 * 終了処理
	 * @throws IOException */
	@AfterClass
	public static void cleanup() throws IOException{
		if(out != null){
			out.writeEndElement();
			out.writeCharacters("\n");
			out.writeEndElement();
			out.writeEndDocument();
			out.close();
		}
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @throws Exception 初期化に失敗した場合
	 */
	@BeforeClass
	public static void init() throws Exception{

		// 仕様のドキュメントを読み込み
		File file = new File("build/spec/spec.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);
		specification = factory.newDocumentBuilder().parse(file.toURI().toString());

		return;
	}

	// ======================================================================
	// 仕様検証の実行
	// ======================================================================
	/**
	 * 仕様のテストパターンを検証します。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Test
	public void test() throws Exception{
		testSection("prologue");
		testSection("element");
		testSection("text");
		testSection("entity-reference");
		testSection("cdata");
		testSection("comment");
		testSection("pi");
		testSection("dtd");
		testSection("structure");
		return;
	}

	// ======================================================================
	// セクションのテスト
	// ======================================================================
	/**
	 * 指定された ID を持つセクションの仕様をテストします。
	 * <p>
	 * @param id テストするセクションの ID
	 * @throws Exception テストに失敗した場合
	 */
	private void testSection(String id) throws Exception{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		Element section = (Element)xpath.evaluate("/html-parse-spec/section[@id='" + id + "']", specification, XPathConstants.NODE);
		assert(section != null): id;
		String title = xpath.evaluate("title", section);
		writeTag("h2", title);
		writeStartConvertPattern();
		NodeList specs = (NodeList)xpath.evaluate("spec", section, XPathConstants.NODESET);
		for(int j=0; j<specs.getLength(); j++){
			Element spec = (Element)specs.item(j);
			evaluate(spec, xpath);
		}
		writeEndConvertPattern();
		return;
	}

	// ======================================================================
	// 仕様検証の実行
	// ======================================================================
	/**
	 * 仕様のテストパターンを検証します。
	 * <p>
	 * @param spec 検証する仕様
	 * @param xpath XPath
	 * @throws Exception テストに失敗した場合
	 */
	private void evaluate(Element spec, XPath xpath) throws Exception{
		String title = xpath.evaluate("title", spec);
		String desc = xpath.evaluate("desc", spec);
		String errcd = xpath.evaluate("error", spec);
		String sample = xpath.evaluate("sample", spec);
		String expected = xpath.evaluate("expected", spec);

		Set<String> err = new HashSet<String>();
		StringTokenizer tk = new StringTokenizer(errcd, ", \t\r\n");
		while(tk.hasMoreTokens()){
			err.add(tk.nextToken());
		}

		logger.fine("-------------------------------------------------");
		List<Warning> errmsg = new ArrayList<Warning>();
		expected = getText(parse(expected, false, null));
		String actual = getText(parse(sample, true, errmsg));

		// 変換パターンのレポート
		writeConvert(errmsg, sample, actual, title, desc);

		// 解析後に再構築したXML文字列と想定するXML文字列を比較
		logger.fine("ORIGINAL HTML : " + sample.trim());
		logger.fine("RECOGNIZED XML: " + actual.trim());
		if(! actual.equals(expected)){
			logger.fine("EXPECTED XML  : " + expected.trim());
		}
		assertEquals(expected, actual);

		// 警告が発生し得ない場合
		if(err.size() == 0){
			assertEquals("予期しない警告の発生: " + errmsg.toString(), 0, errmsg.size());

		// 警告想定がある場合は想定したメッセージかを判定
		} else {
			Set<String> notYet = new HashSet<String>(err);
			for(Warning w: errmsg){
				assertTrue("想定外の警告が発生: " + w, err.contains(w.code));
				notYet.remove(w.code);
			}
			assertEquals("予期する警告の未通知: " + notYet.toString(), 0, notYet.size());
		}
		return;
	}

	// ======================================================================
	// 文字列の解析
	// ======================================================================
	/**
	 * 指定された文字列を解析して DOM を構築します。
	 * <p>
	 * @param xml 解析する文字列
	 * @param html HTML として解析する場合 true
	 * @param msg 警告メッセージ格納用リスト
	 * @return 解析したドキュメント
	 */
	private static Document parse(String xml, boolean html, List<Warning> msg){
		final List<Warning> warning = (msg==null)? new ArrayList<Warning>(): msg;
		try{
			InputSource is = new InputSource(new StringReader(xml));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			if(html){
				HTMLDocumentBuilderFactory f = new HTMLDocumentBuilderFactory();
				f.setHtmlOptimize(true);
				f.setLowerCaseName(true);
				factory = f;
			}
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new DefaultHandler2(){
				/** @param e */
				@Override
				public void warning(SAXParseException e) throws SAXException {
					logger.warning("[" + e.getLineNumber() + ":" + e.getColumnNumber() + "] " + e.getMessage());
					Warning w = new Warning();
					String msg = e.getMessage();
					w.code = msg.substring(0, msg.indexOf(':'));
					w.msg = msg.substring(msg.indexOf(':') + 1);
					w.line = e.getLineNumber();
					w.col = e.getColumnNumber();
					warning.add(w);
					return;
				}
			});
			return builder.parse(is);
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

	// ======================================================================
	// DOM の文字列化
	// ======================================================================
	/**
	 * 指定された DOM を XML の文字列に変換します。
	 * <p>
	 * @param doc ドキュメント
	 * @return XML 文字列
	 */
	private static String getText(Document doc){
		StringWriter sw = new StringWriter();
		try{
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty("method", "xml");
			transformer.setOutputProperty("omit-xml-declaration", "yes");
			transformer.transform(new DOMSource(doc), new StreamResult(sw));
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
		return sw.toString();
	}

	/** エラーメッセージ */
	public static class Warning{
		/** エラーコード */
		public String code = null;
		/** エラーメッセージ */
		public String msg = null;
		/** 行番号 */
		public int line = 0;
		/** 桁番号 */
		public int col = 0;
		/** 文字列化 */
		@Override
		public String toString(){
			return String.format("%2d,%2d: %s:%s", line, col, code, msg);
		}
	}

	// ======================================================================
	// ブロックタグの出力
	// ======================================================================
	/**
	 * ブロック要素を出力します。
	 * <p>
	 * @param tag 要素のタグ名
	 * @param content 要素の内容
	*/
	private static void writeTag(String tag, String content){
		out.writeStartElement(tag);
		out.writeCharacters(content);
		out.writeEndElement();
		out.writeCharacters("\n");
		return;
	}

	// ======================================================================
	// セクションの開始
	// ======================================================================
	/**
	 * セクションを開始します。
	 * <p>
	*/
	private static void writeStartConvertPattern(){
		line = 0;
		out.writeStartElement("table");
		out.writeCharacters("\n");
		return;
	}

	// ======================================================================
	// セクションの開始
	// ======================================================================
	/**
	 * セクションを開始します。
	 * <p>
	*/
	private static void writeEndConvertPattern(){
		out.writeEndElement();
		out.writeCharacters("\n");
		return;
	}

	// ======================================================================
	// 変換結果の出力
	// ======================================================================
	/**
	 * 指定された変換パターンを出力します。
	 * <p>
	 * @param errmsg エラーメッセージ
	 * @param before 変換前の文字列
	 * @param after 変換後の文字列
	 * @param title この変換の説明
	 * @param desc この結果に関する説明
	*/
	private static void writeConvert(List<Warning> errmsg, String before, String after, String title, String desc){
		boolean strict = (errmsg.size() == 0);

		// タイトルの出力
		out.writeStartElement("tr");
		if(line % 2 == 1){
			out.writeAttribute("class", "odd");
		}
		out.writeStartElement("td");
		out.writeAttribute("colspan", "3");
		out.writeAttribute("class", "titl");
		if(strict){
			out.writeCharacters("●");
		} else {
			out.writeCharacters("○");
			out.writeStartElement("i");
		}
		out.writeCharacters(title);
		if(! strict){
			out.writeEndElement();
		}
		out.writeEndElement();
		out.writeEndElement();
		out.writeCharacters("\n");

		// 説明の出力
		out.writeStartElement("tr");
		if(line % 2 == 1){
			out.writeAttribute("class", "odd");
		}
		out.writeStartElement("td");
		out.writeAttribute("colspan", "3");
		out.writeAttribute("class", "desc");
		out.writeCharacters(desc);
		out.writeEndElement();
		out.writeEndElement();
		out.writeCharacters("\n");

		// 変換内容の出力
		out.writeStartElement("tr");
		if(line % 2 == 1){
			out.writeAttribute("class", "odd");
		}
		out.writeCharacters("\n\t");
		out.writeStartElement("td");
		out.writeAttribute("class", "smpl");
		write(before);
		out.writeEndElement();
		out.writeCharacters("\n\t");
		out.writeStartElement("td");
		out.writeAttribute("style", "font-size:10px;vertical-align:top;");
		out.writeCharacters("→");
		out.writeEndElement();
		out.writeCharacters("\n\t");
		out.writeStartElement("td");
		out.writeAttribute("class", "smpl");
		write(after);
		out.writeEndElement();
		out.writeCharacters("\n");
		out.writeEndElement();
		out.writeCharacters("\n");

		// 説明の出力
		if(errmsg.size() > 0){
			out.writeStartElement("tr");
			if(line % 2 == 1){
				out.writeAttribute("class", "odd");
			}
			out.writeStartElement("td");
			out.writeAttribute("colspan", "3");
			out.writeAttribute("class", "desc err");
			for(int i=0; i<errmsg.size(); i++){
				if(i != 0){
					out.writeEmptyElement("br");
				}
				out.writeCharacters(errmsg.get(i).toString());
			}
			out.writeEndElement();
			out.writeEndElement();
			out.writeCharacters("\n");
		}

		line ++;
		return;
	}

	// ======================================================================
	// サンプルXMLの出力
	// ======================================================================
	/**
	 * サンプル用のXML文字列を出力します。
	 * <p>
	 * @param text 出力する文字列
	*/
	private static void write(String text){
		for(int i=0; i<text.length(); i++){
			char ch = text.charAt(i);
			switch(ch){
			case '\r':
				out.writeEmptyElement("img");
				out.writeAttribute("src", "ctrl/0d.png");
				out.writeAttribute("alt", "\r");
				out.writeAttribute("title", "CR");
				out.writeAttribute("class", "ctrl");
				break;
			case '\n':
				out.writeEmptyElement("img");
				out.writeAttribute("src", "ctrl/0a.png");
				out.writeAttribute("alt", "");
				out.writeAttribute("title", "LF");
				out.writeAttribute("class", "ctrl");
				out.writeEmptyElement("br");
				break;
			case '\t':
				out.writeEmptyElement("img");
				out.writeAttribute("src", "ctrl/0c.png");
				out.writeAttribute("alt", "\t");
				out.writeAttribute("title", "TAB");
				out.writeAttribute("class", "ctrl tab");
				break;
			case ' ':
				out.writeEmptyElement("img");
				out.writeAttribute("src", "ctrl/20.png");
				out.writeAttribute("alt", " ");
				out.writeAttribute("title", "SPACE");
				out.writeAttribute("class", "ctrl");
				break;
			default:
				out.writeCharacters(String.valueOf(ch));
				break;
			}
		}
		out.writeEmptyElement("img");
		out.writeAttribute("src", "ctrl/eof.png");
		out.writeAttribute("alt", "");
		out.writeAttribute("title", "EOF");
		out.writeAttribute("class", "ctrl");
		return;
	}

}
