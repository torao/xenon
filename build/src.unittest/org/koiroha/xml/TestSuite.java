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

import org.junit.runner.RunWith;
import org.junit.runners.*;
import org.junit.runners.Suite.SuiteClasses;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// TestSuite: テストスイートクラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * このパッケージのテストスイートです。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/02 Java2 SE 5.0
 */
@RunWith(Suite.class)
@SuiteClasses({
	DefaultNamespaceContextTest.class,
	XmlTest.class,
	HtmlTest.class,
})
public class TestSuite {
	/* */
}
