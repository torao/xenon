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

import java.io.Serializable;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// DefaultNamespaceContext: デフォルト名前空間コンテキスト
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 接頭辞=名前空間 URI のようなマップ型に似た名前空間コンテキストです。自身に
 * 定義されていない名前空間を親に問い合わせる階層構造を作る事が出来ます。
 * <p>
 * @version $Revision: 1.4 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/07 Java2 SE 5.0
 */
public class DefaultNamespaceContext implements NamespaceContext, Serializable {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DefaultNamespaceContext.class.getName());

	// ======================================================================
	// 親の名前空間
	// ======================================================================
	/**
	 * 親の名前空間コンテキストです。
	 * <p>
	 */
	private final NamespaceContext parent;

	// ======================================================================
	// 名前空間マップ
	// ======================================================================
	/**
	 * 接頭辞をキーとして名前空間 URI を格納するマップです。
	 * <p>
	 */
	private final Map<String,String> namespace = new HashMap<String, String>();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * デフォルトコンストラクタは親を持たないインスタンスを構築します。
	 * <p>
	 */
	public DefaultNamespaceContext() {
		this(null);
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 親となる名前空間コンテキストを指定して構築を行います。このインスタンスに定義されていない
	 * 名前空間は指定された親コンテキストに問い合わせます。
	 * <p>
	 * @param parent 親の名前空間コンテキスト
	 */
	public DefaultNamespaceContext(NamespaceContext parent) {
		this.parent = parent;
		return;
	}

	// ======================================================================
	// 名前空間 URI の設定
	// ======================================================================
	/**
	 * 指定された接頭辞に対する名前空間 URI を参照します。
	 * <p>
	 * @param prefix 接頭辞
	 * @param namespaceURI 名前空間 URI
	 * @throws IllegalArgumentException 接頭辞または名前空間 URI が不正な場合
	 */
	public void setNamespaceURI(String prefix, String namespaceURI) throws IllegalArgumentException{
		logger.finest("setNamespaceURI(" + prefix + "," + namespaceURI + ")");

		// パラメータのどちらが null であれば例外
		if(prefix == null || namespaceURI == null){
			throw new IllegalArgumentException("prefix or namespace-uri is null");
		}

		// 規定の接頭辞を再定義しようとした場合は例外
		if((prefix.equals(XMLConstants.XML_NS_PREFIX) && ! namespaceURI.equals(XMLConstants.XML_NS_URI))
		|| (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) && ! namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))){
			throw new IllegalArgumentException("specified prefix is reserved: " + prefix);
		}

		//指定された名前空間を設定
		synchronized(namespace){
			namespace.put(prefix, namespaceURI);
		}

		return;
	}

	// ======================================================================
	// 名前空間 URI の参照
	// ======================================================================
	/**
	 * 指定された接頭辞に対する名前空間 URI を参照します。
	 * <p>
	 * @param prefix 接頭辞
	 * @return 名前空間 URI
	 */
	public String getNamespaceURI(String prefix) {

		// API リファレンスの仕様
		if(prefix == null){
			throw new IllegalArgumentException();
		}
		if(prefix.equals(XMLConstants.XML_NS_PREFIX)){
			return XMLConstants.XML_NS_URI;
		}
		if(prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)){
			return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		}

		// このインスタンスから指定された接頭辞の名前空間を参照
		synchronized(namespace){
			if(namespace.containsKey(prefix)){
				return namespace.get(prefix);
			}
		}

		// 親のコンテキストから検索
		if(parent != null){
			return parent.getNamespaceURI(prefix);
		}

		// デフォルトの名前空間を返す
		return XMLConstants.NULL_NS_URI;
	}

	// ======================================================================
	// 接頭辞の参照
	// ======================================================================
	/**
	 * 指定された名前空間 URI に対する接頭辞を参照します。このコンテキストに指定された名前空間が
	 * 結びつけられていない場合は null を返します。
	 * <p>
	 * @param namespaceURI 名前空間 URI
	 * @return 名前空間 URI にマップされた接頭辞の列挙
	 */
	public String getPrefix(String namespaceURI) {

		// API リファレンスの仕様
		if(namespaceURI == null){
			throw new IllegalArgumentException();
		}
		if(namespaceURI.equals(XMLConstants.XML_NS_URI)){
			return XMLConstants.XML_NS_PREFIX;
		}
		if(namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)){
			return XMLConstants.XMLNS_ATTRIBUTE;
		}

		// このインスタンスから接頭辞を参照
		synchronized(namespace){
			for(Map.Entry<String,String> e: namespace.entrySet()){
				if(namespaceURI.equals(e.getValue())){
					return e.getKey();
				}
			}
		}

		// 親のコンテキストから検索
		if(parent != null){
			return parent.getPrefix(namespaceURI);
		}

		// 再定義されていないデフォルトの名前空間が指定された場合
		if(namespaceURI.equals(XMLConstants.NULL_NS_URI)){
			return XMLConstants.DEFAULT_NS_PREFIX;
		}

		// 未定義の名前空間が指定された場合
		return null;
	}

	// ======================================================================
	// 接頭辞の参照
	// ======================================================================
	/**
	 * 指定された名前空間 URI に対する接頭辞を参照します。
	 * <p>
	 * @param namespaceURI 名前空間 URI
	 * @return 名前空間 URI にマップされた接頭辞の列挙
	 */
	public Iterator<String> getPrefixes(String namespaceURI) {

		// API リファレンスの仕様
		if(namespaceURI == null){
			throw new IllegalArgumentException();
		}
		if(namespaceURI.equals(XMLConstants.XML_NS_URI)){
			Collection<String> col = new ArrayList<String>();
			col.add(XMLConstants.XML_NS_PREFIX);
			return col.iterator();
		}
		if(namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)){
			Collection<String> col = new ArrayList<String>();
			col.add(XMLConstants.XMLNS_ATTRIBUTE);
			return col.iterator();
		}

		// このインスタンスが保持している内容から検索
		Set<String> prefix = new HashSet<String>();
		synchronized(namespace){
			for(Map.Entry<String,String> e: namespace.entrySet()){
				if(namespaceURI.equals(e.getValue())){
					prefix.add(e.getKey());
				}
			}
		}

		// 親のコンテキストから検索
		if(parent != null){
			@SuppressWarnings("unchecked")
			Iterator<String> it = parent.getPrefixes(namespaceURI);
			while(it.hasNext()){
				prefix.add(it.next());
			}
		}

		return prefix.iterator();
	}

}
