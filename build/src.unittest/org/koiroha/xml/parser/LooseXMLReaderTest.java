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

import static junit.framework.Assert.*;

import java.lang.reflect.Method;
import java.util.*;

import org.junit.Test;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LooseXMLReaderTest:
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version $Revision: 1.1 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2010/02/24 Java2 SE 5.0
 */
public class LooseXMLReaderTest {

	/** テスト用ルーズ XML リーダー */
	private LooseXMLReader reader = new LooseXMLReader();

	// ======================================================================
	// 属性値解析のテスト
	// ======================================================================
	/**
	 * 属性値の解析をテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Test
	public void testParseElementBody() throws Exception{
		Method method = LooseXMLReader.class.getDeclaredMethod("parseElementBody", String.class, boolean.class, Map.class);
		method.setAccessible(true);

		// 要素内が空の場合
		Map<String,String> attr = new HashMap<String, String>();
		boolean empty = (Boolean)method.invoke(reader, "", true, attr);
		assertFalse(empty);
		assertEquals(0, attr.size());

		// 要素内が空の場合
		attr.clear();
		empty = (Boolean)method.invoke(reader, "  \n \t \n ", true, attr);
		assertFalse(empty);
		assertEquals(0, attr.size());

		// 空要素の検出
		attr.clear();
		empty = (Boolean)method.invoke(reader, "/", true, attr);
		assertTrue(empty);
		assertEquals(0, attr.size());

		// 空要素の検出 (前方に空白あり)
		attr.clear();
		empty = (Boolean)method.invoke(reader, " /", true, attr);
		assertTrue(empty);
		assertEquals(0, attr.size());

		// 空要素の検出 (属性値の後ろ)
		attr.clear();
		empty = (Boolean)method.invoke(reader, "a=\"b\" c=\"d\"/", true, attr);
		assertTrue(empty);
		assertEquals(attr.toString(), 2, attr.size());
		assertEquals("b", attr.get("a"));
		assertEquals("d", attr.get("c"));

		// 非空要素指示のスラッシュを属性名として認識する事を確認
		attr.clear();
		empty = (Boolean)method.invoke(reader, "/ ", true, attr);
		assertFalse(empty);
		assertEquals(1, attr.size());
		assertEquals("/", attr.get("/"));

		// クオート省略の属性値がスラッシュで終わっているのを空要素と認識しないことを確認
		attr.clear();
		empty = (Boolean)method.invoke(reader, "path=/foo/", true, attr);
		assertFalse(empty);
		assertEquals(1, attr.size());
		assertEquals("/foo/", attr.get("path"));

		// ダブルクオートの属性値指定1
		attr.clear();
		empty = (Boolean)method.invoke(reader, "a=\"b\"", true, attr);
		assertFalse(empty);
		assertEquals(attr.toString(), 1, attr.size());
		assertEquals("b", attr.get("a"));

		// ダブルクオートの属性値指定2
		attr.clear();
		empty = (Boolean)method.invoke(reader, " a = \"b\" ", true, attr);
		assertFalse(empty);
		assertEquals(attr.toString(), 1, attr.size());
		assertEquals("b", attr.get("a"));

		// シングルクオートの属性値指定1
		attr.clear();
		empty = (Boolean)method.invoke(reader, "a=\'b\'", true, attr);
		assertFalse(empty);
		assertEquals(attr.toString(), 1, attr.size());
		assertEquals("b", attr.get("a"));

		// シングルクオートの属性値指定2
		attr.clear();
		empty = (Boolean)method.invoke(reader, " a = \'b\' ", true, attr);
		assertFalse(empty);
		assertEquals(attr.toString(), 1, attr.size());
		assertEquals("b", attr.get("a"));

		// クオート省略の属性値指定1
		attr.clear();
		empty = (Boolean)method.invoke(reader, "a=b", true, attr);
		assertFalse(empty);
		assertEquals(attr.toString(), 1, attr.size());
		assertEquals("b", attr.get("a"));

		// クオート省略の属性値指定2
		attr.clear();
		empty = (Boolean)method.invoke(reader, " a = b ", true, attr);
		assertFalse(empty);
		assertEquals(attr.toString(), 1, attr.size());
		assertEquals("b", attr.get("a"));

		return;
	}

}
