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

import java.util.*;

import javax.xml.XMLConstants;

import org.junit.Test;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// DefaultNamespaceContextTest:
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/09 Java2 SE 5.0
 */
public class DefaultNamespaceContextTest extends TestCase{

	// ======================================================================
	// コンストラクタのテスト
	// ======================================================================
	/**
	 * 構築時に例外が発生しない事を確認します。
	 * <p>
	 */
	@Test
	public void test全部() {
		DefaultNamespaceContext parent = new DefaultNamespaceContext();
		DefaultNamespaceContext child = new DefaultNamespaceContext(parent);
		DefaultNamespaceContext grandchild = new DefaultNamespaceContext(child);

		// 規定値を確認
		assertEquals(XMLConstants.NULL_NS_URI, grandchild.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));
		assertEquals(XMLConstants.XML_NS_URI, grandchild.getNamespaceURI(XMLConstants.XML_NS_PREFIX));
		assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, grandchild.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
		assertEquals(XMLConstants.DEFAULT_NS_PREFIX, parent.getPrefix(XMLConstants.NULL_NS_URI));
		assertEquals(XMLConstants.XML_NS_PREFIX, parent.getPrefix(XMLConstants.XML_NS_URI));
		assertEquals(XMLConstants.XMLNS_ATTRIBUTE, parent.getPrefix(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));

		// 未定義の接頭辞に対するデフォルトの名前空間を確認
		assertEquals(XMLConstants.NULL_NS_URI, parent.getNamespaceURI("undefined"));
		assertEquals(XMLConstants.NULL_NS_URI, child.getNamespaceURI("undefined"));
		assertEquals(XMLConstants.NULL_NS_URI, grandchild.getNamespaceURI("undefined"));

		String prefix = "px";
		String uri = "http://foo.bar.com/px";
		parent.setNamespaceURI(prefix, uri);
		assertEquals(uri, parent.getNamespaceURI(prefix));
		assertEquals(uri, child.getNamespaceURI(prefix));
		assertEquals(uri, grandchild.getNamespaceURI(prefix));
		assertEquals(prefix, parent.getPrefix(uri));
		assertEquals(prefix, child.getPrefix(uri));
		assertEquals(prefix, grandchild.getPrefix(uri));

		prefix = "cx";
		uri = "http://foo.bar.com/cx";
		child.setNamespaceURI(prefix, uri);
		assertEquals(XMLConstants.NULL_NS_URI, parent.getNamespaceURI(prefix));
		assertEquals(uri, child.getNamespaceURI(prefix));
		assertEquals(uri, grandchild.getNamespaceURI(prefix));
		assertNull(parent.getPrefix(uri));
		assertEquals(prefix, child.getPrefix(uri));
		assertEquals(prefix, grandchild.getPrefix(uri));

		prefix = "gx";
		uri = "http://foo.bar.com/gx";
		grandchild.setNamespaceURI(prefix, uri);
		assertEquals(XMLConstants.NULL_NS_URI, parent.getNamespaceURI(prefix));
		assertEquals(XMLConstants.NULL_NS_URI, child.getNamespaceURI(prefix));
		assertEquals(uri, grandchild.getNamespaceURI(prefix));
		assertNull(parent.getPrefix(uri));
		assertNull(child.getPrefix(uri));
		assertEquals(prefix, grandchild.getPrefix(uri));

		// デフォルトの名前空間は置き換えが可能であることを確認
		uri = "http://foo.bar.com/default";
		parent.setNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX, uri);
		assertEquals(uri, grandchild.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));

		// 名前空間に定義されている全ての接頭辞を列挙できることを確認
		uri = "http://foo.bar.com/duplicate";
		parent.setNamespaceURI("px", uri);
		parent.setNamespaceURI("cx", uri);
		parent.setNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX, uri);
		child.setNamespaceURI("cx", uri);
		grandchild.setNamespaceURI("gx", uri);
		List<String> px = new ArrayList<String>();
		Iterator<String> it = grandchild.getPrefixes(uri);
		while(it.hasNext()){
			px.add(it.next());
		}
		assertEquals(4, px.size());
		assertTrue(px.contains("px"));
		assertTrue(px.contains("cx"));
		assertTrue(px.contains("gx"));
		assertTrue(px.contains(XMLConstants.DEFAULT_NS_PREFIX));

		it = grandchild.getPrefixes(XMLConstants.XML_NS_URI);
		assertTrue(it.hasNext());
		assertEquals(XMLConstants.XML_NS_PREFIX, it.next());
		assertFalse(it.hasNext());

		it = grandchild.getPrefixes(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		assertTrue(it.hasNext());
		assertEquals(XMLConstants.XMLNS_ATTRIBUTE, it.next());
		assertFalse(it.hasNext());

		// 未定義の名前空間に対する接頭辞の列挙
		it = grandchild.getPrefixes("undefined");
		assertFalse(it.hasNext());

		// 接頭辞に null を指定した場合
		try{
			grandchild.setNamespaceURI(null, "");
			fail();
		} catch(IllegalArgumentException ex){/* */}

		// 名前空間に null を指定した場合
		try{
			grandchild.setNamespaceURI("", null);
			fail();
		} catch(IllegalArgumentException ex){/* */}

		// 接頭辞に null を指定した場合
		try{
			grandchild.getNamespaceURI(null);
			fail();
		} catch(IllegalArgumentException ex){/* */}

		// 名前空間に null を指定した場合
		try{
			grandchild.getPrefix(null);
			fail();
		} catch(IllegalArgumentException ex){/* */}

		// 名前空間に null を指定した場合
		try{
			grandchild.getPrefixes(null);
			fail();
		} catch(IllegalArgumentException ex){/* */}

		// 規定の接頭辞を再定義しようとした場合
		try{
			grandchild.setNamespaceURI(XMLConstants.XML_NS_PREFIX, "http://foo.bar.com/xxx");
			fail();
		} catch(IllegalArgumentException ex){/* */}

		// 規定の接頭辞を再定義しようとした場合
		try{
			grandchild.setNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE, "http://foo.bar.com/xxx");
			fail();
		} catch(IllegalArgumentException ex){/* */}

		return;
	}

}
