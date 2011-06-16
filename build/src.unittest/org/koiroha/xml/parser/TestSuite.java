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

import java.util.logging.*;

import org.junit.runner.RunWith;
import org.junit.runners.*;
import org.junit.runners.Suite.SuiteClasses;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// TestSuite: テストスイートクラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * テストスイートです。
 * <p>
 * @version $Revision: 1.3 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/02 Java2 SE 5.0
 */
@RunWith(Suite.class)
@SuiteClasses({
	HTMLParserFactoryTest.class,
	HTMLParserTest.class,
	LFReaderTest.class,
	PositionTest.class,
	ToolkitTest.class,
	SpecPatternTest.class,
	LooseXMLReaderTest.class,
	HTMLDocumentBuilderFactoryTest.class,
})
public class TestSuite {
	/* */
	static {
		Logger logger = Logger.getLogger("");
		for(Handler h: logger.getHandlers()){
			logger.removeHandler(h);
		}
		Handler handler = new ConsoleHandler();
		logger.addHandler(handler);
		handler.setLevel(Level.ALL);
		logger.setLevel(Level.ALL);
	}
}
