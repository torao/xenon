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

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.*;

import org.koiroha.xml.Xml;
import org.w3c.dom.*;
import org.xml.sax.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// HTMLDocumentBuilder: HTML ドキュメントビルダー
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML ドキュメントビルダーです。
 * <p>
 * @version $Revision: 1.3 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/01 Java2 SE 5.0
 */
class HTMLDocumentBuilder extends DocumentBuilder {

	// ======================================================================
	// 機能フラグ
	// ======================================================================
	/**
	 * 機能フラグです。
	 * <p>
	 */
	private final Map<String,Boolean> feature = new HashMap<String,Boolean>();

	// ======================================================================
	// ドキュメントビルダー
	// ======================================================================
	/**
	 * ドキュメントビルダーです。
	 * <p>
	 */
	private final DocumentBuilder builder;

	// ======================================================================
	// エンティティリゾルバー
	// ======================================================================
	/**
	 * エンティティリゾルバーです。
	 * <p>
	 */
	private EntityResolver entityResolver = null;

	// ======================================================================
	// エラーハンドラ
	// ======================================================================
	/**
	 * エラーハンドラです。
	 * <p>
	 */
	private ErrorHandler errorHandler = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param builder このインスタンスが使用するドキュメントビルダー
	 * @param feature このビルダーの機能
	 */
	public HTMLDocumentBuilder(DocumentBuilder builder, Map<String,Boolean> feature) {
		this.builder = builder;
		this.feature.putAll(feature);
		return;
	}

	// ======================================================================
	// 機能フラグの参照
	// ======================================================================
	/**
	 * ファクトリの機能フラグを参照します。
	 * <p>
	 * @param name 機能名
	 * @return 機能フラグ
	 */
	public boolean getFeature(String name) {
		Boolean value = this.feature.get(name);
		if(value == null){
			return false;
		}
		return value;
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 *
	 * <p>
	 * @return DOM 実装
	 */
	@Override
	public DOMImplementation getDOMImplementation() {
		return builder.getDOMImplementation();
	}

	// ======================================================================
	// 名前空間有効性の参照
	// ======================================================================
	/**
	 * 名前空間が有効かどうかを参照します。
	 * <p>
	 * @return 名前空間が有効な場合 true
	 */
	@Override
	public boolean isNamespaceAware() {
		return builder.isNamespaceAware();
	}

	// ======================================================================
	// 検証の有効性参照
	// ======================================================================
	/**
	 * ドキュメントの検証が有効かどうかを参照します。
	 * <p>
	 * @return 有効な場合 true
	 */
	@Override
	public boolean isValidating() {
		return builder.isValidating();
	}

	// ======================================================================
	// 新規ドキュメントの構築
	// ======================================================================
	/**
	 * 新規のドキュメントを構築します。
	 * <p>
	 * @return 新規のドキュメント
	 */
	@Override
	public Document newDocument() {
		return builder.newDocument();
	}

	// ======================================================================
	// 解析の実行
	// ======================================================================
	/**
	 * 指定された入力ソースに基づいて解析を実行します。
	 * <p>
	 * @param is 入力ソース
	 * @return 解析したドキュメント
	 * @throws SAXException
	 * @throws IOException
	 */
	@Override
	public Document parse(InputSource is) throws SAXException, IOException {

		// HTML パーサファクトリの構築
		HTMLParserFactory factory = new HTMLParserFactory();
		factory.setNamespaceAware(builder.isNamespaceAware());
		factory.setValidating(builder.isValidating());
		factory.setXIncludeAware(builder.isXIncludeAware());
		factory.setSchema(builder.getSchema());
		factory.setLowerCaseName(getFeature(LooseXMLReader.FEATURE_LOWERCASE_NAME));

		// SAX パーサの参照
		SAXParser parser = factory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		if(this.errorHandler != null){
			reader.setErrorHandler(errorHandler);
		}
		if(this.entityResolver != null){
			reader.setEntityResolver(entityResolver);
		}
		assert(parser.isNamespaceAware() == builder.isNamespaceAware());
		assert(parser.isValidating() == builder.isValidating());
		assert(parser.isXIncludeAware() == builder.isXIncludeAware());

		// HTML ビルダーの構築
		HTMLBuilder builder = new HTMLBuilder(
				this.builder, entityResolver, errorHandler, feature);
		reader.setProperty(Xml.PROPERTY_LEXICAL_HANDLER, builder);
		reader.setContentHandler(builder);
		reader.setDTDHandler(builder);

		reader.parse(is);
		return builder.getDocument();
	}

	// ======================================================================
	// エンティティリゾルバーの設定
	// ======================================================================
	/**
	 * エンティティリゾルバーを設定します。
	 * <p>
	 * @param er エンティティリゾルバー
	 */
	@Override
	public void setEntityResolver(EntityResolver er) {
		this.entityResolver = er;
		return;
	}

	// ======================================================================
	// エラーハンドラの設定
	// ======================================================================
	/**
	 * エラーハンドラを設定します。
	 * <p>
	 * @param eh エラーハンドラ
	 */
	@Override
	public void setErrorHandler(ErrorHandler eh) {
		this.errorHandler = eh;
		return;
	}

}
