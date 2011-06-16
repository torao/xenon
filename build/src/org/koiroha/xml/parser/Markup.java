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

import javax.xml.XMLConstants;

import org.koiroha.xml.*;
import org.xml.sax.helpers.AttributesImpl;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Tag: XML タグ情報情報
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML の要素情報を保持するためのクラスです。
 * <p>
 * @version $Revision: 1.5 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/03/31 Java2 SE 5.0
 */
class Markup {

	// ======================================================================
	// 親要素
	// ======================================================================
	/**
	 * この要素の親です。
	 * <p>
	 */
	private final Markup parent;

	// ======================================================================
	// 名前空間 URI
	// ======================================================================
	/**
	 * この要素の名前空間 URI です。
	 * <p>
	 */
	private final String uri;

	// ======================================================================
	// ローカル名
	// ======================================================================
	/**
	 * この要素のローカル名です。
	 * <p>
	 */
	private final String localName;

	// ======================================================================
	// 完全修飾名
	// ======================================================================
	/**
	 * この要素の完全修飾名です。
	 * <p>
	 */
	private final String qName;

	// ======================================================================
	// 属性値
	// ======================================================================
	/**
	 * この要素の属性値です。
	 * <p>
	 */
	private final AttributesImpl attr = new AttributesImpl();

	// ※Xerces 2.9 (XERCESJ-1261) バグ対応
	//   Attributes2Impl#addAttributes() にはひどいバグがあり NullPointerException
	//   が発生する。Xerces 2.9 は J2SE 5.0 for Mac OS X にバンドルされている。
	// private final Attributes2Impl attr = new Attributes2Impl();

	// ======================================================================
	// 空要素
	// ======================================================================
	/**
	 * 空要素を表すフラグです。
	 * <p>
	 */
	private boolean empty;

	// ======================================================================
	// 終了要素
	// ======================================================================
	/**
	 * 要素の終了を示すフラグです。
	 * <p>
	 */
	private boolean end;

	// ======================================================================
	// 接頭辞マッピング
	// ======================================================================
	/**
	 * 接頭辞に対する名前空間 URI のマッピングです。null 値の接頭辞がデフォルトの名前空間を
	 * 表します。
	 * <p>
	 */
	private final DefaultNamespaceContext namespace;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param ns 名前空間解析が有効な場合 true
	 * @param parent 親要素
	 * @param qName 修飾名
	 * @param attrs 属性値
	 * @param empty 空要素の場合 true
	 * @param end 要素の終了を示す場合 true
	 */
	public Markup(boolean ns, Markup parent, String qName, Map<String,String> attrs, boolean empty, boolean end) {
		this.parent = parent;
		this.qName = qName;
		this.end = end;

		if(parent != null && ns){
			this.namespace = new DefaultNamespaceContext(parent.namespace);
		} else {
			this.namespace = new DefaultNamespaceContext();
		}

		// 属性値の解析 (名前空間解決の為に先に行う)
		parseAttribute(ns, attrs);

		// 名前空間 URI とローカル名を決定
		String prefix = XMLConstants.DEFAULT_NS_PREFIX;
		int sep = qName.indexOf(':');
		if(sep < 0 || ! ns){
			localName = qName;
		} else {
			prefix = qName.substring(0, sep);
			localName = qName.substring(sep + 1);
		}
		uri = getNamespaceURI(prefix);

		// ローカル名による空要素の判定
		this.empty = (empty || Toolkit.isEmptyElement(uri, localName, qName));
		return;
	}

	// ======================================================================
	// 開始イベント
	// ======================================================================
	/**
	 * この要素の名前空間 URI を参照します。
	 * <p>
	 * @return 名前空間 URI
	 */
	public String getUri() {
		return uri;
	}

	// ======================================================================
	// 修飾名の参照
	// ======================================================================
	/**
	 * 修飾名を参照します。
	 * <p>
	 * @return 修飾名
	 */
	public String getQName() {
		return qName;
	}

	// ======================================================================
	// ローカル名の参照
	// ======================================================================
	/**
	 * ローカル名を参照します。
	 * <p>
	 * @return ローカル名
	 */
	public String getLocalName() {
		return localName;
	}

	// ======================================================================
	// 属性の参照
	// ======================================================================
	/**
	 * 指定された名前の属性を参照します。
	 * <p>
	 * @return この要素の属性
	 */
	public AttributesImpl getAttributes(){
		return attr;
	}

	// ======================================================================
	// 空要素の参照
	// ======================================================================
	/**
	 * 空要素かどうかを参照します。
	 * <p>
	 * @return 空要素の場合 true
	 */
	public boolean isEmpty() {
		return empty;
	}

	// ======================================================================
	// 空要素の設定
	// ======================================================================
	/**
	 * 空要素を設定します。
	 * <p>
	 * @param empty 空要素の場合 true
	 */
	public void setEmpty(boolean empty){
		this.empty = empty;
		return;
	}

	// ======================================================================
	// 要素終了の参照
	// ======================================================================
	/**
	 * 要素の終了を参照します。
	 * <p>
	 * @return 要素の終了の場合 true
	 */
	public boolean isEnd() {
		return end;
	}

	// ======================================================================
	// 要素終了の設定
	// ======================================================================
	/**
	 * 要素終了を設定します。
	 * <p>
	 * @param end 要素の終了の場合 true
	 */
	public void setEnd(boolean end) {
		this.end = end;
		return;
	}

	// ======================================================================
	// 要素終了の判定
	// ======================================================================
	/**
	 * この要素が要素の終了を表す場合、指定された要素の終了かどうかを判定します。
	 * <p>
	 * @param elem 評価する要素
	 * @return 指定された要素の終了の場合 true
	 */
	public boolean isEndOf(Markup elem) {
		assert(isEnd()): elem;
		return getLocalName().equals(elem.getLocalName());
	}

	// ======================================================================
	// 不用な要素終了の判定
	// ======================================================================
	/**
	 * この要素の終了が不用なものかどうかを判定します。
	 * <p>
	 * @param context 現在の要素スタック
	 * @return 要素終了が不用な場合 true
	 */
	public boolean isDisusedEnd(Markup context) {
		assert(isEnd());

		Markup mover = context;
		while(mover != null){
			if(isEndOf(mover)){
				return false;
			}
			mover = context.parent;
		}
		return true;
	}

	// ======================================================================
	// 親要素の参照
	// ======================================================================
	/**
	 * 親要素を参照します。
	 * <p>
	 * @return 親要素
	 */
	public Markup getParent(){
		return parent;
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
	public String getNamespaceURI(String prefix){
		return namespace.getNamespaceURI(prefix);
	}

	// ======================================================================
	// インスタンスの文字列化
	// ======================================================================
	/**
	 * このインスタンスを文字列化します。
	 * <p>
	 * @return インスタンスの文字列
	*/
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder("<");

		// 要素名の連結
		if(isEnd()){
			buffer.append('/');
		}
		buffer.append(getQName());

		// 属性の連結
		for(int i=0; i<attr.getLength(); i++){
			buffer.append(' ');
			buffer.append(attr.getQName(i)).append("=\"");
			buffer.append(Xml.escape(attr.getValue(i))).append("\"");
		}
		if(! isEnd() && isEmpty()){
			buffer.append('/');
		}
		buffer.append('>');
		return buffer.toString();
	}

	// ======================================================================
	// 属性値の設定
	// ======================================================================
	/**
	 * 要素の属性値を設定します。
	 * <p>
	 * @param ns 名前空間の認識を行う場合 true
	 * @param attrs 属性値
	 */
	private void parseAttribute(boolean ns, Map<String,String> attrs) {

		// 先に名前空間 URI を解析
		if(ns){
			for(Map.Entry<String,String> e: attrs.entrySet()){
				String qName = e.getKey();
				String value = e.getValue();
				if(qName.equals(XMLConstants.XMLNS_ATTRIBUTE)){
					namespace.setNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX, value);
				} else if(qName.startsWith(XMLConstants.XMLNS_ATTRIBUTE + ":")){
					int sep = qName.indexOf(':');
					String localName = qName.substring(sep + 1);
					namespace.setNamespaceURI(localName, value);
				}
			}
		}

		// 属性値の解析
		for(Map.Entry<String,String> e: attrs.entrySet()){

			// 属性の情報を参照
			String qName = e.getKey();
			String value = e.getValue();
			String localName = qName;
			String uri = XMLConstants.NULL_NS_URI;

			// 名前空間が有効な場合
			if(ns){

				// 接頭辞とローカル名に分解
				int sep = qName.indexOf(':');
				if(sep >= 0){
					localName = qName.substring(sep + 1);
					String prefix = qName.substring(0, sep);
					uri = getNamespaceURI(prefix);
				}

				// 名前空間定義の設定
				if(qName.equals(XMLConstants.XMLNS_ATTRIBUTE)){
					uri = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
				}
			}

			// 属性の追加
			attr.addAttribute(uri, localName, qName, "CDATA", value);
		}
		return;
	}

}
