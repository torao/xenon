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

import javax.xml.XMLConstants;
import javax.xml.parsers.*;

import org.junit.Test;
import org.koiroha.xml.TestCase;
import org.xml.sax.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// HTMLParserFactoryTest: HTML パーサファクトリテストケース
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML パーサファクトリのテストケースです。
 * <p>
 * @version $Revision: 1.5 $ $Date: 2010/07/18 15:30:18 $
 * @author torao
 * @since 2009/04/01 Java2 SE 5.0
 */
public class HTMLParserFactoryTest extends TestCase{

	// ======================================================================
	// コンストラクタテスト
	// ======================================================================
	/**
	 * コンストラクタのテストを行います。
	 * <p>
	 */
	@Test
	public void testコンストラクタ() {

		// new で生成できることを確認
		SAXParserFactory factory = new HTMLParserFactory();

		// 通常の方法で作成
		/* JSE 6 以降
		SAXParserFactory factory = SAXParserFactory.newInstance(
				HTMLParserFactory.class.getName(), HTMLParser.class.getClassLoader());
		assertEquals(HTMLParserFactory.class, factory.getClass());
		*/

		// JAXP のシステムプロパティによる作成
		String oldValue = System.getProperty("javax.xml.parsers.SAXParserFactory");
		System.setProperty("javax.xml.parsers.SAXParserFactory", HTMLParserFactory.class.getName());
		factory = SAXParserFactory.newInstance();
		if(oldValue == null){
			System.getProperties().remove("javax.xml.parsers.SAXParserFactory");
		} else {
			System.setProperty("javax.xml.parsers.SAXParserFactory", oldValue);
		}
		assertEquals(HTMLParserFactory.class, factory.getClass());

		return;
	}

	// ======================================================================
	// 機能設定/参照のテスト
	// ======================================================================
	/**
	 * ファクトリの機能設定と参照をテストします。このテストでは実際にその機能を有効にした場合の
	 * 挙動のテストまでは行いません。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Test
	public void test機能の設定と参照() throws Exception{
		SAXParserFactory factory = new HTMLParserFactory();
		/* JSE 6 以降
		SAXParserFactory factory = SAXParserFactory.newInstance(
				HTMLParserFactory.class.getName(), HTMLParser.class.getClassLoader());
		*/

		// API リファレンスに記述されている規定の機能を使用できることを確認
		assertFalse(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
		assertFalse(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));

		return;
	}

	// ======================================================================
	// HTML パーサ作成のテスト
	// ======================================================================
	/**
	 * HTML パーサ作成をテストします。
	 * <p>
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testSAXパーサ作成() throws ParserConfigurationException, SAXException  {
		SAXParserFactory factory = new HTMLParserFactory();
		/* JSE 6 以降
		SAXParserFactory factory = SAXParserFactory.newInstance(
				HTMLParserFactory.class.getName(), HTMLParser.class.getClassLoader());
		*/

		factory.newSAXParser();
		return;
	}

	// ======================================================================
	// 入力ソース推測のテスト
	// ======================================================================
	/**
	 * 入力ストリームの推測をテストします。
	 * <p>
	 * @throws Exception
	 */
	@Test
	public void 入力ソースの推測() throws Exception{

		// XML 宣言でエンコードが指定されている場合
		String sample = "<?xml encoding='UTF-8'?>あいうえお";
		guessInputSource(sample, "UTF-8", "JISAutoDetect", "UTF-8");
		guessInputSource(sample, "Windows-31J", "JISAutoDetect", "UTF-8");

		// XML 宣言が存在しエンコードが省略されている場合
		sample = "<?xml version='1.0'?>あいうえお";
		guessInputSource(sample, "UTF-8", "JISAutoDetect", "UTF-8");
		guessInputSource(sample, "Windows-31J", "JISAutoDetect", "UTF-8");

		// BOM 付きの Unicode Little (内容による判定は省略される)
		sample = "<?xml encoding='UTF-8'?>あいうえお";
		guessInputSource(sample, "UnicodeLittle", "JISAutoDetect", "UTF-16");

		// BOM 付きの Unicode Big (内容による判定は省略される)
		sample = "<?xml encoding='UTF-8'?>あいうえお";
		guessInputSource(sample, "UnicodeBig", "JISAutoDetect", "UTF-16");

		// META 要素にエンコーディングが指定されている HTML
		sample = "<html><meta http-equiv = 'content-type' content = 'text/html; charset = Windows-31J'>あいうえお</html>";
		guessInputSource(sample, "Windows-31J", "UTF-8", "Windows-31J");
		guessInputSource(sample, "UTF-8", "JISAutoDetect", "Windows-31J");

		// 内容からエンコーディングが推測できない場合 (テキスト)
		sample = "あいうえお";
		guessInputSource(sample, "Windows-31J", "JISAutoDetect", "JISAutoDetect");
		guessInputSource(sample, "UTF-8", "JISAutoDetect", "JISAutoDetect");

		// 内容からエンコーディングが推測できない場合 (HTML)
		sample = "<html>あいうえお</html>";
		guessInputSource(sample, "Windows-31J", "JISAutoDetect", "JISAutoDetect");
		guessInputSource(sample, "UTF-8", "JISAutoDetect", "JISAutoDetect");

		// 内容からエンコーディングが推測できない場合 (HTML)
		sample = "<html><meta name='foo' content='bar'>あいうえお</html>";
		guessInputSource(sample, "Windows-31J", "JISAutoDetect", "JISAutoDetect");
		guessInputSource(sample, "UTF-8", "JISAutoDetect", "JISAutoDetect");

		// 内容からエンコーディングが推測できない場合 (HTML)
		sample = "<html><meta http-equiv='foo' content='bar'>あいうえお</html>";
		guessInputSource(sample, "Windows-31J", "JISAutoDetect", "JISAutoDetect");
		guessInputSource(sample, "UTF-8", "JISAutoDetect", "JISAutoDetect");

		// 内容からエンコーディングが推測できない場合 (指定された文字セット未定義)
		sample = "<html>あいうえお</html>";
		try{
			guessInputSource(sample, "Windows-31J", "foo", "JISAutoDetect");
			fail();
		} catch(UnsupportedEncodingException ex){/* */}

		return;
	}

	// ======================================================================
	// 入力ソース推測のテスト
	// ======================================================================
	/**
	 * 入力ストリームの推測をテストします。
	 * <p>
	 * @param sample テストする文字列
	 * @param actual 実際に適用するエンコーディング
	 * @param def デフォルトとして指定するエンコーディング
	 * @param expected 予期するエンコーディング
	 * @throws Exception
	 */
	private void guessInputSource(String sample, String actual, String def, String expected) throws Exception{

		// 予想したエンコーディングが推測されていることを確認
		AbstractSAXParserFactory factory = new HTMLParserFactory();
		InputStream in = new ByteArrayInputStream(sample.getBytes(actual));
		InputSource is = factory.guessInputSource(in, def, 8 * 1024);
		assertEquals(Charset.forName(expected), Charset.forName(is.getEncoding()));

		// ストリームからサンプルと同じ内容が取得できることを確認
		StringBuilder buffer = new StringBuilder();
		Reader r = is.getCharacterStream();
		while(true){
			int ch = r.read();
			if(ch < 0)	break;
			buffer.append((char)ch);
		}
		assertEquals(new String(sample.getBytes(actual), expected), buffer.toString());
		return;
	}

}
