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

import java.util.Map;

import javax.xml.validation.Schema;

import org.xml.sax.XMLReader;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// HTMLParser: HTML パーサ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML パーサクラスです。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/03/31 Java2 SE 5.0
 */
class HTMLParser extends AbstractSAXParser {

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * インスタンスを初期化します。
	 * <p>
	 * @param feature 機能フラグ
	 * @param xinclude XInclude を有効にする場合 true
	 * @param schema 使用するスキーマ
	 */
	public HTMLParser(Map<String,Boolean> feature, boolean xinclude, Schema schema) {
		super(feature, xinclude, schema);
		return;
	}

	// ======================================================================
	// パーサの参照
	// ======================================================================
	/**
	 * SAX2 パーサを参照します。
	 * <p>
	 * @return SAX2 パーサ
	 */
	@Override
	public XMLReader getXMLReader() {
		return new HTMLReader(feature, property);
	}

}
