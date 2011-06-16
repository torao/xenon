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

import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.validation.Schema;

import org.koiroha.xml.Xml;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderAdapter;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// AbstractSAXParser: 抽象 SAX パーサ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * XML パーサクラスの抽象実装クラスです。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/09 Java2 SE 5.0
 */
public abstract class AbstractSAXParser extends SAXParser {

	// ======================================================================
	// パーサ機能
	// ======================================================================
	/**
	 * このパーサを生成したファクトリに設定されていた機能フラグです。
	 * <p>
	 */
	protected final Map<String, Boolean> feature = new HashMap<String, Boolean>();

	// ======================================================================
	// XInclude 有効性
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが XInclude を意識するかどうかです。
	 * <p>
	 */
	private final boolean xincludeAware;

	// ======================================================================
	// スキーマ
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが使用するスキーマです。
	 * <p>
	 */
	private final Schema schema;

	// ======================================================================
	// プロパティ
	// ======================================================================
	/**
	 * このパーサのプロパティです。
	 * <p>
	 */
	protected final Map<String, Object> property = new HashMap<String, Object>();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param feature 機能フラグ
	 * @param xinclude XInclude を有効にする場合 true
	 * @param schema 使用するスキーマ
	 */
	protected AbstractSAXParser(Map<String,Boolean> feature, boolean xinclude, Schema schema) {
		this.feature.putAll(feature);
		this.xincludeAware = xinclude;
		this.schema = schema;
		return;
	}

	// ======================================================================
	// プロパティの参照
	// ======================================================================
	/**
	 * このパーサに設定されているプロパティを参照します。
	 * <p>
	 * @param name プロパティ名
	 * @return プロパティに対する値
	 * @throws SAXNotRecognizedException プロパティ名が認識できない場合
	 * @throws SAXNotSupportedException 指定されたプロパティをサポートしていない場合
	 */
	@Override
	public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		return property.get(name);
	}

	// ======================================================================
	// プロパティの設定
	// ======================================================================
	/**
	 * このパーサのプロパティを設定します。
	 * <p>
	 * @param name プロパティ名
	 * @param value プロパティの値
	 * @throws SAXNotRecognizedException プロパティ名が認識できない場合
	 * @throws SAXNotSupportedException 指定されたプロパティをサポートしていない場合
	 */
	@Override
	public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
		property.put(name, value);
		return;
	}

	// ======================================================================
	// パーサの参照
	// ======================================================================
	/**
	 * SAX1 パーサを参照します。このメソッドは {@link #getXMLReader()} メソッドから取得
	 * した {@link XMLReader} を {@link XMLReaderAdapter} でラップしたインスタンスを
	 * 返します。
	 * <p>
	 * @return SAX1 パーサ
	 * @throws SAXException SAX パーサの構築に失敗した場合
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Parser getParser() throws SAXException{
		return new XMLReaderAdapter(getXMLReader());
	}

	// ======================================================================
	// 名前空間有効性の参照
	// ======================================================================
	/**
	 * このパーサが名前空間を認識するかどうかを参照します。
	 * <p>
	 * @return 名前空間を認識する場合 true
	 */
	@Override
	public boolean isNamespaceAware() {
		return getFeature(Xml.FEATURE_NAMESPACES, false);
	}

	// ======================================================================
	// XInclude の参照
	// ======================================================================
	/**
	 * このパーサが XInclude を認識するかどうかを参照します。
	 * <p>
	 * @return XInclude を認識する場合 true
	 */
	@Override
	public boolean isXIncludeAware(){
		return xincludeAware;
	}

	// ======================================================================
	// スキーマの参照
	// ======================================================================
	/**
	 * このパーサが使用するスキーマを参照します。
	 * <p>
	 * @return スキーマ
	*/
	@Override
	public Schema getSchema() {
		return schema;
	}

	// ======================================================================
	// DTD 検証
	// ======================================================================
	/**
	 * このパーサが DTD の検証を行うかどうかを参照します。
	 * <p>
	 * @return DTD の検証を行う場合 true
	 */
	@Override
	public boolean isValidating() {
		return getFeature(Xml.FEATURE_VALIDATION, false);
	}

	// ======================================================================
	// 機能フラグの参照
	// ======================================================================
	/**
	 * 指定された名前の機能フラグを参照します。
	 * <p>
	 * @param name 機能名
	 * @param def デフォルト値
	 * @return 機能フラグ
	 */
	private boolean getFeature(String name, boolean def){
		Boolean value = feature.get(name);
		if(value == null){
			return def;
		}
		return value;
	}

}
