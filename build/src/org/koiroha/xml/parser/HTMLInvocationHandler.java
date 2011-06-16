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

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

import org.w3c.dom.Document;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// HTMLInvocationHandler: HTML ドキュメントハンドラ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * ドキュメント型やエンコーディングなどを保持するための Wrapper を生成します。
 * <p>
 * @version $Revision: 1.3 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/06 Java2 SE 5.0
 */
class HTMLInvocationHandler implements InvocationHandler, Serializable {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// XML エンコーディング参照メソッド
	// ======================================================================
	/**
	 * XML エンコーディング参照メソッドです。
	 * <p>
	 */
	private static final Method GET_INPUT_ENCODING = getMethod(Document.class, "getInputEncoding");

	// ======================================================================
	// ドキュメント
	// ======================================================================
	/**
	 * ドキュメントです。
	 * <p>
	 */
	private final Document doc;

	// ======================================================================
	// プロパティ値
	// ======================================================================
	/**
	 * メソッドに対する返値のマップです。
	 * <p>
	 */
	private final Map<Method, Object> values = new HashMap<Method, Object>();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * ドキュメントを指定して構築を行います。
	 * <p>
	 * @param doc ドキュメント
	 */
	public HTMLInvocationHandler(Document doc) {
		this.doc = doc;
		return;
	}

	// ======================================================================
	// 入力エンコーディングの設定
	// ======================================================================
	/**
	 * 入力エンコーディングを設定します。
	 * <p>
	 * @param inputEncoding 入力エンコーディング
	 */
	public void setInputEncoding(String inputEncoding){
		values.put(GET_INPUT_ENCODING, inputEncoding);
		return;
	}

	// ======================================================================
	// メソッドの呼び出し
	// ======================================================================
	/**
	 * メソッドの呼び出しをハンドルします。
	 * <p>
	 * @param proxy プロキシインスタンス
	 * @param method メソッド
	 * @param args メソッドの引数
	 * @return メソッドの返値
	 * @throws Throwable 例外が発生した場合
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(values.containsKey(method)){
			return values.get(method);
		}
		return method.invoke(doc, args);
	}

	// ======================================================================
	// メソッドの参照
	// ======================================================================
	/**
	 * メソッドを参照します。
	 * <p>
	 * @param clazz メソッドを参照するクラス
	 * @param name メソッド名
	 * @param paramTypes パラメータの型
	 * @return 該当するメソッド
	 */
	private static Method getMethod(Class<Document> clazz, String name, Class<?>... paramTypes){
		try{
			return clazz.getMethod(name, paramTypes);
		} catch(NoSuchMethodException ex){
			throw new IllegalStateException(ex);
		}
	}

}
