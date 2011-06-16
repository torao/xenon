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

import org.w3c.dom.*;
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// AttributeOrderKeeper: 属性順序維持ハンドラ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 要素の属性値を設定時の順序を維持して列挙するための動的プロキシです。
 * <p>
 * @version $Revision: 1.3 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2010/01/19 Java2 SE 5.0
 */
class AttributeOrderKeeper implements InvocationHandler, Serializable {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	/** Element#getAttributes() */
	private static final Method GET_ATTRIBUTES;

	/** Element#setAttribute(String,String) */
	private static final Method SET_ATTRIBUTE;
	/** Element#setAttributeNS(String,String,String) */
	private static final Method SET_ATTRIBUTE_NS;
	/** Element#setAttributeNode(Attr) */
	private static final Method SET_ATTRIBUTE_NODE;
	/** Element#setAttributeNodeNS(Attr) */
	private static final Method SET_ATTRIBUTE_NODE_NS;
	/** Element#removeAttribute(String) */
	private static final Method REMOVE_ATTRIBUTE;
	/** Element#removeAttributeNS(String,String) */
	private static final Method REMOVE_ATTRIBUTE_NS;
	/** Element#removeAttributeNode(Attr) */
	private static final Method REMOVE_ATTRIBUTE_NODE;

	// ======================================================================
	// ノード参照メソッド
	// ======================================================================
	/**
	 * 属性マップから属性を参照するためのメソッドです。
	 * <p>
	 */
	private static final Method ITEM;

	// ======================================================================
	// スタティックイニシャライザ
	// ======================================================================
	/**
	 * 定義を構築します。
	 * <p>
	 */
	static{
		try{
			GET_ATTRIBUTES = Node.class.getDeclaredMethod("getAttributes");
			SET_ATTRIBUTE = Element.class.getDeclaredMethod("setAttribute", String.class, String.class);
			SET_ATTRIBUTE_NS = Element.class.getDeclaredMethod("setAttributeNS", String.class, String.class, String.class);
			SET_ATTRIBUTE_NODE = Element.class.getDeclaredMethod("setAttributeNode", Attr.class);
			SET_ATTRIBUTE_NODE_NS = Element.class.getDeclaredMethod("setAttributeNodeNS", Attr.class);
			REMOVE_ATTRIBUTE = Element.class.getDeclaredMethod("removeAttribute", String.class);
			REMOVE_ATTRIBUTE_NS = Element.class.getDeclaredMethod("removeAttributeNS", String.class, String.class);
			REMOVE_ATTRIBUTE_NODE = Element.class.getDeclaredMethod("removeAttributeNode", Attr.class);

			ITEM = NamedNodeMap.class.getDeclaredMethod("item", Integer.TYPE);
		} catch(Exception ex){
			throw new IllegalStateException(ex);
		}
	}

	// ======================================================================
	// 要素
	// ======================================================================
	/**
	 * ラップしている要素です。
	 * <p>
	 */
	private final Element elem;

	// ======================================================================
	// 属性
	// ======================================================================
	/**
	 * ラップしている属性です。
	 * <p>
	 */
	private final NamedNodeMap attr;

	// ======================================================================
	// 属性名リスト
	// ======================================================================
	/**
	 * 属性名の順序リストです。名前空間が指定されている場合は namespace '\0' localname
	 * という文字列が格納されます ('\0' は XML の名前に使用できないため安全)。
	 * <p>
	 */
	private final List<String> names;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 要素を指定して構築を行います。
	 * <p>
	 * @param elem 要素
	 */
	public AttributeOrderKeeper(Element elem) {
		this.elem = elem;
		this.attr = null;
		this.names = new ArrayList<String>();
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 属性を指定して構築を行います。
	 * <p>
	 * @param attr 属性
	 * @param names 属性名リスト
	 */
	public AttributeOrderKeeper(NamedNodeMap attr, List<String> names) {
		this.elem = null;
		this.attr = attr;
		this.names = names;
		return;
	}

	// ======================================================================
	// メソッドの呼び出し
	// ======================================================================
	/**
	 * メソッドを呼び出します。
	 * <p>
	 * @param proxy プロキシインスタンス
	 * @param method 呼び出すメソッド
	 * @param args 実行引数
	 * @return メソッドの実行結果
	 * @throws Throwable 例外が発生した場合
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(proxy instanceof Element){
			return invokeElement(proxy, method, args);
		}
		return invokeNamedNodeMap(proxy, method, args);
	}

	// ======================================================================
	// メソッドの呼び出し
	// ======================================================================
	/**
	 * メソッドを呼び出します。
	 * <p>
	 * @param proxy プロキシインスタンス
	 * @param method 呼び出すメソッド
	 * @param args 実行引数
	 * @return メソッドの実行結果
	 * @throws Throwable 例外が発生した場合
	 */
	private Object invokeElement(Object proxy, Method method, Object[] args) throws Throwable {

		// Element#getAttributes()
		if(method.equals(GET_ATTRIBUTES)){
			NamedNodeMap attr = elem.getAttributes();
			return Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(),
				new Class[]{NamedNodeMap.class, Serializable.class},
				new AttributeOrderKeeper(attr, names));
		}

		// Element#setAttribute(String,String)
		if(method.equals(SET_ATTRIBUTE)){
			String name = (String)args[0];
			if(! names.contains(name)){
				names.add(name);
			}
			return method.invoke(elem, args);
		}

		// Element#setAttributeNS(String,String,String)
		if(method.equals(SET_ATTRIBUTE_NS)){
			String uri = (String)args[0];
			String name = (String)args[1];
			name = uri + '\0' + name;
			if(! names.contains(name)){
				names.add(name);
			}
			return method.invoke(elem, args);
		}

		// Element#setAttributeNode(Attr)
		if(method.equals(SET_ATTRIBUTE_NODE)){
			Attr attr = (Attr)args[0];
			String name = attr.getName();
			if(! names.contains(name)){
				names.add(name);
			}
			return method.invoke(elem, args);
		}

		// Element#setAttributeNodeNS(Attr)
		if(method.equals(SET_ATTRIBUTE_NODE_NS)){
			Attr attr = (Attr)args[0];
			String uri = attr.getNamespaceURI();
			String name = attr.getLocalName();
			name = uri + '\0' + name;
			if(! names.contains(name)){
				names.add(name);
			}
			return method.invoke(elem, args);
		}

		// Element#removeAttribute(String)
		if(method.equals(REMOVE_ATTRIBUTE)){
			String name = (String)args[0];
			names.remove(name);
			return method.invoke(elem, args);
		}

		// Element#removeAttributeNS(String,String)
		if(method.equals(REMOVE_ATTRIBUTE_NS)){
			String uri = (String)args[0];
			String name = (String)args[1];
			name = uri + '\0' + name;
			names.remove(name);
			return method.invoke(elem, args);
		}

		// Element#removeAttributeNode(Attr)
		if(method.equals(REMOVE_ATTRIBUTE_NODE)){
			Attr attr = (Attr)args[0];
			String name = attr.getName();
			if(attr.getNamespaceURI() != null){
				name = attr.getNamespaceURI() + '\0' + attr.getLocalName();
			}
			names.remove(name);
			return method.invoke(elem, args);
		}

		// それ以外の場合は通常の呼び出し
		return method.invoke(elem, args);
	}

	// ======================================================================
	// メソッドの呼び出し
	// ======================================================================
	/**
	 * メソッドを呼び出します。
	 * <p>
	 * @param proxy プロキシインスタンス
	 * @param method 呼び出すメソッド
	 * @param args 実行引数
	 * @return メソッドの実行結果
	 * @throws Throwable 例外が発生した場合
	 */
	private Object invokeNamedNodeMap(Object proxy, Method method, Object[] args) throws Throwable {

		// NamedNodeMap#item(int)
		if(method.equals(ITEM)){
			int index = (Integer)args[0];
			String name = names.get(index);
			int sep = name.indexOf('\0');
			if(sep < 0){
				return attr.getNamedItem(name);
			}
			String uri = name.substring(0, sep);
			name = name.substring(sep + 1);
			return attr.getNamedItemNS(uri, name);
		}

		// それ以外の場合は通常の呼び出し
		return method.invoke(attr, args);
	}

}
