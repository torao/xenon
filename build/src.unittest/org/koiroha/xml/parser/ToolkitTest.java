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
import java.util.*;

import javax.xml.parsers.*;

import org.junit.Test;
import org.koiroha.xml.TestCase;
import org.w3c.dom.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// ToolkitTest:
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version $Revision: 1.4 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/09 Java2 SE 5.0
 */
public class ToolkitTest extends TestCase{

	// ======================================================================
	// クラス定義のテスト
	// ======================================================================
	/**
	 * クラス定義をテストします。
	 * <p>
	 */
	@Test
	public void testClass(){
		verifyUtilityClass(Toolkit.class);
		return;
	}

	// ======================================================================
	// 空要素判定のテスト
	// ======================================================================
	/**
	 * 空要素判定をテストします。
	 * <p>
	 */
	@Test
	public void testIsEmptyElement() {
		assertTrue(Toolkit.isEmptyElement(null, null, "br"));
		assertTrue(Toolkit.isEmptyElement(null, null, "BR"));
		assertFalse(Toolkit.isEmptyElement(null, null, "table"));
		assertFalse(Toolkit.isEmptyElement(null, null, "TaBlE"));
		return;
	}

	// ======================================================================
	// ストリームクローズのテスト
	// ======================================================================
	/**
	 * ストリームのクローズをテストします。
	 * <p>
	 */
	@Test
	public void testClose() {
		Toolkit.close(new ByteArrayInputStream(new byte[]{}));
		Toolkit.close(new Closeable(){
			public void close() throws IOException {
				throw new IOException();
			}
		});
		return;
	}

	// ======================================================================
	// メッセージのフォーマットのテスト
	// ======================================================================
	/**
	 * メッセージのフォーマットをテストします。
	 * <p>
	 */
	@Test
	public void testFormat() {

		// 定義されているメッセージを参照できることを確認
		ResourceBundle res = ResourceBundle.getBundle("org.koiroha.xml.parser.messages");
		String msgid = res.getKeys().nextElement();
		Toolkit.format(msgid, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

		// 未定義のメッセージを参照して例外が発生することを確認
		try{
			Toolkit.format("unknown");
			fail();
		} catch(MissingResourceException ex){/* */}

		return;
	}

	// ======================================================================
	// ストリーム開始判定のテスト
	// ======================================================================
	/**
	 * ストリーム開始判定をテストします。
	 * <p>
	 * @throws IOException テストに失敗した場合
	 */
	@Test
	public void testStreamStartsWith() throws IOException{
		String sample = "ABC";
		PushbackReader in = new PushbackReader(new StringReader(sample), 10);

		assertTrue(Toolkit.streamStartsWith(in, "ABC", false));
		assertFalse(Toolkit.streamStartsWith(in, "abc", false));
		assertFalse(Toolkit.streamStartsWith(in, "ABCD", false));
		assertFalse(Toolkit.streamStartsWith(in, "ABC\uFFFE", false));
		assertFalse(Toolkit.streamStartsWith(in, "AB\uFFFF", false));
		assertTrue(Toolkit.streamStartsWith(in, "ABC\uFFFF", false));
		assertFalse(Toolkit.streamStartsWith(in, "abc\uFFFF", false));
		return;
	}

	// ======================================================================
	// 文字パターン一致判定のテスト
	// ======================================================================
	/**
	 * 文字パターン一致判定をテストします。
	 * <p>
	 */
	@Test
	public void testMatches() {
		assertTrue(Toolkit.matches('A', 'A', false));
		assertFalse(Toolkit.matches('A', 'a', false));
		assertFalse(Toolkit.matches('A', 'b', false));
		assertTrue(Toolkit.matches('A', 'A', true));
		assertTrue(Toolkit.matches('A', 'a', true));
		assertFalse(Toolkit.matches('A', 'b', true));
		assertTrue(Toolkit.matches('\0', ' ', false));
		assertTrue(Toolkit.matches('\0', '\t', false));
		assertTrue(Toolkit.matches('\0', '\r', false));
		assertTrue(Toolkit.matches('\0', '\n', false));
		assertFalse(Toolkit.matches('\0', 'A', false));
		assertFalse(Toolkit.matches('\0', -1, false));
		assertFalse(Toolkit.matches('\uFFFF', 'A', false));
		assertFalse(Toolkit.matches('\uFFFF', ' ', false));
		assertTrue(Toolkit.matches('\uFFFF', -1, false));
		assertTrue(Toolkit.matches('\uFFFE', 'A', false));
		assertTrue(Toolkit.matches('\uFFFE', ' ', false));
		assertFalse(Toolkit.matches('\uFFFE', -1, false));
		return;
	}

	// ======================================================================
	// 親要素判定のテスト
	// ======================================================================
	/**
	 * 親要素判定をテストします。
	 * <p>
	 * @throws ParserConfigurationException テストに失敗した場合
	 */
	@Test
	public void testGetPreferredParent() throws ParserConfigurationException{

		// 名前空間無効でテスト
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element html = doc.createElement("html");
		Element body = doc.createElement("BODY");
		Element ul = doc.createElement("UL");
		Element big = doc.createElement("x:big");
		Element li = doc.createElement("LI");
		Element p = doc.createElement("P");
		Element p2 = doc.createElement("P");
		Element small = doc.createElement("SMALL");
		doc.appendChild(html);
		html.appendChild(body);
		html.appendChild(ul);
		html.appendChild(p);
		ul.appendChild(big);
		assertTrue(Toolkit.getPreferredParent(big, li) == ul);
		assertTrue(Toolkit.getPreferredParent(p, p2) == html);
		assertTrue(Toolkit.getPreferredParent(p, small) == p);

		doc.removeChild(html);
		doc.appendChild(small);
		assertTrue(Toolkit.getPreferredParent(small, p) == small);

		// 名前空間と接頭辞付きでテスト
		String ns = "http://www.w3.org/1999/xhtml";
		factory.setNamespaceAware(true);
		builder = factory.newDocumentBuilder();
		doc = builder.newDocument();
		html = doc.createElementNS(ns, "x:html");
		body = doc.createElementNS(ns, "x:BODY");
		ul = doc.createElementNS(ns, "x:UL");
		big = doc.createElementNS(ns, "x:big");
		li = doc.createElementNS(ns, "x:LI");
		p = doc.createElementNS(ns, "x:P");
		p2 = doc.createElementNS(ns, "x:P");
		small = doc.createElementNS(ns, "x:SMALL");
		doc.appendChild(html);
		html.appendChild(body);
		html.appendChild(ul);
		html.appendChild(p);
		ul.appendChild(big);
		assertTrue(Toolkit.getPreferredParent(big, li) == ul);
		assertTrue(Toolkit.getPreferredParent(p, p2) == html);
		assertTrue(Toolkit.getPreferredParent(p, small) == p);

		return;
	}

	// ======================================================================
	// 属性解析のテスト
	// ======================================================================
	/**
	 * 属性解析をテストします。
	 * <p>
	 */
	@Test
	public void testParseAttributesSimply() {
		Map<String,String> attr = Toolkit.parseAttributesSimply("a=b c=\"d\" e = \'f\' g=\"&quot;\" foo");
		assertEquals(5, attr.size());
		assertEquals("b", attr.get("a"));
		assertEquals("d", attr.get("c"));
		assertEquals("f", attr.get("e"));
		assertEquals("\"", attr.get("g"));
		assertEquals("foo", attr.get("foo"));
		return;
	}

}
