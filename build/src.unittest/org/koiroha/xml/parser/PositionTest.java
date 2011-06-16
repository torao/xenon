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

import org.junit.Test;
import org.koiroha.xml.TestCase;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// PositionTest: Position クラステスト
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Position クラスをテストします。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/09 Java2 SE 5.0
 */
public class PositionTest extends TestCase{

	// ======================================================================
	// 位置クラスのテスト
	// ======================================================================
	/**
	 * 位置クラスをテストします。
	 * <p>
	 */
	@Test
	public void test(){
		Position p = new Position(1, 1);
		Position p0 = new Position(1, 1);
		Position p1 = new Position(1, 2);
		Position p2 = new Position(2, 1);
		Position p3 = new Position(2, 2);

		assertTrue(p.hashCode() == p0.hashCode());
		assertTrue(p.equals(p0));
		assertTrue(p0.equals(p));
		assertFalse(p.equals(null));
		assertFalse(p.equals("boo"));

		assertTrue(p0.compareTo(p) == 0);
		assertTrue(p.compareTo(p0) == 0);
		assertTrue(p0.compareTo(p1) < 0);
		assertTrue(p1.compareTo(p0) > 0);
		assertTrue(p1.compareTo(p2) < 0);
		assertTrue(p2.compareTo(p1) > 0);
		assertTrue(p2.compareTo(p3) < 0);
		assertTrue(p3.compareTo(p2) > 0);

		assertEquals("(1,1)", p.toString());
		return;
	}

}
