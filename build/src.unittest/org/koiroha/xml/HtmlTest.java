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

import static junit.framework.Assert.*;

import org.junit.Test;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// HtmlTest: HTML テストケース
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Html クラスのテストを行います。
 * <p>
 * @version $Revision: 1.1 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2010/02/24 Java SE 6
 */
public class HtmlTest extends TestCase{

	// ======================================================================
	// コンストラクタのテスト
	// ======================================================================
	/**
	 * ユーティリティクラスであることをテストします。
	 * <p>
	 */
	@Test
	public void testClass(){
		verifyUtilityClass(Html.class);
		return;
	}

	// ======================================================================
	// エンティティから文字の参照
	// ======================================================================
	/**
	 * エンティティから文字参照をテストします。
	 * <p>
	 */
	@Test
	public void testGetCharacter(){

		// 一般的な HTML エンティティ
		assertEquals('<', Html.getCharacter("&lt;"));
		assertEquals('>', Html.getCharacter("&gt;"));
		assertEquals('&', Html.getCharacter("&amp;"));
		assertEquals('\"', Html.getCharacter("&quot;"));

		// HTML エンティティ
		assertEquals('©', Html.getCharacter("&copy;"));
		assertEquals('\u00A0', Html.getCharacter("&nbsp;"));
		assertTrue(Html.getCharacter("&apos;") < 0);	// HTML では未定義

		// 数値参照
		assertEquals(0, Html.getCharacter("&#0;"));
		assertEquals(1024, Html.getCharacter("&#1024;"));
		assertEquals(0x1, Html.getCharacter("&#x1;"));
		assertEquals(0xFFFF, Html.getCharacter("&#xfFfF;"));
		assertTrue(Html.getCharacter("&#-1;") < 0);
		assertTrue(Html.getCharacter("&#65536;") < 0);
		assertTrue(Html.getCharacter("&#x10000;") < 0);
		assertTrue(Html.getCharacter("&#xABCG;") < 0);

		assertTrue(Html.getCharacter("") < 0);			// 数値参照は未対応

		try{
			Html.getCharacter(null);
			fail();
		} catch(NullPointerException ex){/* */}

		return;
	}

	// ======================================================================
	// 実体参照の参照
	// ======================================================================
	/**
	 * 文字に対する HTML 実体参照を参照します。
	 * <p>
	 */
	@Test
	public void testGetEntityReference(){

		// 一般的な HTML エンティティ
		assertEquals("&lt;", Html.getEntityReference('<'));
		assertEquals("&gt;", Html.getEntityReference('>'));
		assertEquals("&amp;", Html.getEntityReference('&'));
		assertEquals("&quot;", Html.getEntityReference('\"'));

		// HTML エンティティ
		assertEquals("&copy;", Html.getEntityReference('©'));
		assertEquals("&nbsp;", Html.getEntityReference('\u00A0'));

		assertEquals("&#x27;", Html.getEntityReference('\''));
		assertEquals("&#x0;", Html.getEntityReference('\0'));
		return;
	}

	// ======================================================================
	// 実体参照の解除
	// ======================================================================
	/**
	 * 実体参照の解除をテストします。
	 * <p>
	 */
	@Test
	public void unescape(){
		assertEquals("", Html.unescape(""));
		assertEquals("あいうえお", Html.unescape("あいうえお"));
		assertEquals("a=& b=\"", Html.unescape("a=&amp; b=&quot;"));
		assertEquals("abc&fooooo;def", Html.unescape("abc&fooooo;def"));
		return;
	}

}
