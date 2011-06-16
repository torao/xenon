/* **************************************************************************
 * Copyright (C) 2011 BJoRFUAN. All Rights Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * Apache License Ver. 2.0, and comes with NO WARRANTY.
 *
 *                                           takami torao <koiroha@gmail.com>
 *                                                   http://www.bjorfuan.com/
 */
package org.koiroha.xml;

import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.junit.Test;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// XmlTest: Xml クラステストケース
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Xml クラスのテストケースです。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/09 Java2 SE 5.0
 */
public class XmlTest extends TestCase{

	// ======================================================================
	// コンストラクタのテスト
	// ======================================================================
	/**
	 * ユーティリティクラスであることをテストします。
	 * <p>
	 */
	@Test
	public void testClass(){
		verifyUtilityClass(Xml.class);
		return;
	}

	// ======================================================================
	// 空白文字判定のテスト
	// ======================================================================
	/**
	 * XML 空白文字判定のテストを行います。
	 * <p>
	 */
	@Test
	public void testIsWhitespace() {
		assertTrue(Xml.isWhitespace(' '));
		assertTrue(Xml.isWhitespace('\t'));
		assertTrue(Xml.isWhitespace('\r'));
		assertTrue(Xml.isWhitespace('\n'));
		assertFalse(Xml.isWhitespace('\f'));
		assertFalse(Xml.isWhitespace('　'));
		assertFalse(Xml.isWhitespace(-1));
		return;
	}

	// ======================================================================
	// XMLエスケープのテスト
	// ======================================================================
	/**
	 * 文字列の XML エスケープをテストします。
	 * <p>
	 */
	@Test
	public void testEscape() {
		assertEquals("", Xml.escape(""));
		assertEquals("ABC", Xml.escape("ABC"));
		assertEquals(" \t\n\t\n\t\n", Xml.escape(" \t\r\n\t\n\t\r"));
		assertEquals("&amp;&lt;&gt;&quot;&apos;", Xml.escape("&<>\"\'"));
		assertEquals("&#" + 0xFFFE + ";", Xml.escape("\uFFFE"));
		return;
	}

	// ======================================================================
	// XMLエスケープ解除のテスト
	// ======================================================================
	/**
	 * 文字列の XML エスケープ解除をテストします。
	 * <p>
	 */
	@Test
	public void testUnescape() {
		assertEquals("", Xml.unescape(""));
		assertEquals("ABC", Xml.unescape("ABC"));
		assertEquals("&,<,>,\",\',A,A,A", Xml.unescape("&amp;,&lt;,&gt;,&quot;,&apos;,&#" + ((int)'A') + ";,&#x41;,&#X41;"));
		assertEquals("&&foo;&#;&#65536;&#x;", Xml.unescape("&&foo;&#;&#65536;&#x;"));
		return;
	}

	// ======================================================================
	// 文字セット参照のテスト
	// ======================================================================
	/**
	 * Content-Type から文字セットの参照をテストします。
	 * <p>
	 */
	@Test
	public void testGetCharset() {
		assertEquals(Charset.forName("utf-8"), Xml.getCharset("text/xml;charset=UTF-8"));
		assertEquals(Charset.forName("utf-8"), Xml.getCharset("application/javascript;\ncharset=\"UTF-8\"\r\n;foo=\"bar\";"));
		assertNull(Xml.getCharset("application/javascript;foo=\"bar\";"));
		assertNull(Xml.getCharset("text/xml;charset=UNKNOWN"));
		assertNull(Xml.getCharset("text/xml;charset="));
		return;
	}

}
