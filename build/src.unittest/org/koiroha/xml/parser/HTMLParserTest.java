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

import javax.xml.parsers.SAXParser;

import org.junit.Test;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// HTMLParserTest: HTML パーサテスト
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML パーサのテストケースです。
 * <p>
 * @version $Revision: 1.3 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/01 Java2 SE 5.0
 */
public class HTMLParserTest {

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public HTMLParserTest() {
		return;
	}

	// ======================================================================
	// コンストラクタテスト
	// ======================================================================
	/**
	 * コンストラクタのテストを行います。
	 * <p>
	 */
	@Test
	public void testコンストラクタ() {
		HTMLParserFactory factory = new HTMLParserFactory();
		factory.newSAXParser();
		return;
	}

	// ======================================================================
	// 名前空間設定のテスト
	// ======================================================================
	/**
	 * 名前空間の設定が有効かどうかをテストします。このテストは有効性の on/off が適切に引き継が
	 * れているかのみをテストし実際の動きまでは関知しません。
	 * <p>
	 */
	@Test
	public void test名前空間の有効性設定() {
		HTMLParserFactory factory = new HTMLParserFactory();

		// デフォルト状態で無効になっていることを確認
		SAXParser parser = factory.newSAXParser();
		assertFalse(parser.isNamespaceAware());

		// 明示的に有効にした場合に有効になっていることを確認
		factory.setNamespaceAware(true);
		parser = factory.newSAXParser();
		assertTrue(parser.isNamespaceAware());
		factory.setNamespaceAware(false);
		assertTrue(parser.isNamespaceAware());

		// 明示的に無効にした場合に無効になっていることを確認
		factory.setNamespaceAware(false);
		parser = factory.newSAXParser();
		assertFalse(parser.isNamespaceAware());
		factory.setNamespaceAware(true);
		assertFalse(parser.isNamespaceAware());

		return;
	}

	// ======================================================================
	// ドキュメント検証設定のテスト
	// ======================================================================
	/**
	 * ドキュメント検証の設定が有効かどうかをテストします。このテストは有効性の on/off が適切に
	 * 引き継がれているかのみをテストし実際の動きまでは関知しません。
	 * <p>
	 */
	@Test
	public void testドキュメント検証の有効性設定() {
		HTMLParserFactory factory = new HTMLParserFactory();

		// デフォルト状態で無効になっていることを確認
		SAXParser parser = factory.newSAXParser();
		assertFalse(parser.isValidating());

		// 明示的に有効にした場合に有効になっていることを確認
		factory.setValidating(true);
		parser = factory.newSAXParser();
		assertTrue(parser.isValidating());
		factory.setValidating(false);
		assertTrue(parser.isValidating());

		// 明示的に無効にした場合に無効になっていることを確認
		factory.setValidating(false);
		parser = factory.newSAXParser();
		assertFalse(parser.isValidating());
		factory.setValidating(true);
		assertFalse(parser.isValidating());

		return;
	}

	// ======================================================================
	// プロパティ設定のテスト
	// ======================================================================
	/**
	 * プロパティの設定が有効かどうかをテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Test
	public void testプロパティの設定() throws Exception{
//		HTMLParserFactory factory = new HTMLParserFactory();
//		SAXParser parser = factory.newSAXParser();
//
//		// デフォルト値を参照できることを確認
//		assertNull(parser.getProperty(Feature.PROPERTY_PREFIX + "lexical-handler"));
//
//		// 設定値が参照できることを確認
//		LexicalHandler lh = new DefaultHandler2();
//		parser.setProperty(Feature.PROPERTY_PREFIX + "lexical-handler", lh);
//		assertTrue(parser.getProperty(Feature.PROPERTY_PREFIX + "lexical-handler") == lh);
//
//		 未定義プロパティの参照に対して例外が発生することを確認
//		try{
//			parser.getProperty("foo");
//			fail();
//		} catch(SAXNotRecognizedException ex){/* */}

		// 未定義プロパティの設定に対して例外が発生することを確認
//		try{
//			parser.setProperty("foo", "bar");
//			fail();
//		} catch(SAXNotRecognizedException ex){/* */}

		return;
	}

	// ======================================================================
	// プロパティ設定のテスト
	// ======================================================================
	/**
	 * プロパティの設定が有効かどうかをテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Test
	public void testXMLパーサ参照() throws Exception{
		HTMLParserFactory factory = new HTMLParserFactory();

		SAXParser parser = factory.newSAXParser();
		parser.getXMLReader();
		parser.getParser();
		return;
	}

}
