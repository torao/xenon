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

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.koiroha.xml.Xml;
import org.w3c.dom.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Toolkit: ツールキット
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * ユーティリティクラスです。
 * <p>
 * @version $Revision: 1.3 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/03/31 Java2 SE 5.0
 */
final class Toolkit {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Toolkit.class.getName());

	// ======================================================================
	// 空要素
	// ======================================================================
	/**
	 * 空要素を強制する要素名です。
	 * <p>
	 */
	private static final Set<String> EMPTY_ELEMENT = new HashSet<String>();

	// ======================================================================
	// 固定親要素
	// ======================================================================
	/**
	 * 親要素が特定できる要素の定義です。要素名をキーとして直接の親となりうる要素の名前の配列を
	 * 持ちます。
	 * <p>
	 */
	private static final Map<String,Set<String>> PARENT_RELATION = new HashMap<String,Set<String>>();

	// ======================================================================
	// テキスト装飾要素
	// ======================================================================
	/**
	 * HTML でテキスト装飾として使用される要素です。
	 * <p>
	 */
	private static final Set<String> TEXT_DECORATION_ELEMENT = new HashSet<String>();

	// ======================================================================
	// 属性解析パターン
	// ======================================================================
	/**
	 * XML 要素の属性部分を解析するためのパターンです。
	 * <p>
	 */
	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("([^\\s=]+)\\s*(=?)\\s*(\"[^\"]*\"|'[^']*'|[^\\s]*)");

	// ======================================================================
	// スタティックイニシャライザ
	// ======================================================================
	/**
	 * 要素を定義します。
	 * <p>
	 */
	static{

		// HTML タグの属性定義 XML を読み込み
		String resourceName = "/org/koiroha/xml/parser/html.xml";
		URL url = Toolkit.class.getResource(resourceName);
		Document doc = null;
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(url.toString());
		} catch(Exception ex){
			logger.log(Level.SEVERE, "fail to read resource: " + resourceName, ex);
			throw new IllegalStateException(ex);
		}

		XPath xpath = XPathFactory.newInstance().newXPath();
		try{
			NodeList nl = (NodeList)xpath.evaluate("/html-tag/tag", doc, XPathConstants.NODESET);
			for(int i=0; i<nl.getLength(); i++){
				Element tag = (Element)nl.item(i);
				String name = tag.getAttribute("name");

				// 親要素の指定
				String[] parent = tag.getAttribute("parent").split("\\s+");
				if(parent.length > 0){
					PARENT_RELATION.put(name, new HashSet<String>(Arrays.asList(parent)));
				}

				// スタイルの指定
				String style = tag.getAttribute("style");
				if(style.equals("empty")){
					EMPTY_ELEMENT.add(name);
				} else if(style.equals("decoration")){
					TEXT_DECORATION_ELEMENT.add(name);
				} else if(style.length() != 0){
					assert(false): "unexpected style " + name + ": " + style;
				}
			}
		} catch(Exception ex){
			throw new IllegalStateException(ex);
		}

	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタはクラス内に隠蔽されています。
	 * <p>
	 */
	private Toolkit() {
		return;
	}

	// ======================================================================
	// 空要素の判定
	// ======================================================================
	/**
	 * 指定された要素が空要素かどうかを評価します。localName に null を指定した場合は名前
	 * 空間を認識していないと見なされ name が評価に使用されます。
	 * <p>
	 * @param uri 名前空間 URI
	 * @param localName 判定するローカル名
	 * @param name 修飾名
	 * @return 空要素のにする場合 true
	 */
	public static boolean isEmptyElement(String uri, String localName, String name){
		if(localName == null){
			return EMPTY_ELEMENT.contains(name.toLowerCase());
		}
		return EMPTY_ELEMENT.contains(localName.toLowerCase());
	}

	// ======================================================================
	// ストリームのクローズ
	// ======================================================================
	/**
	 * 指定されたストリームをクローズします。
	 * <p>
	 * @param stream クローズするストリーム
	 */
	public static void close(Closeable stream){
		if(stream != null){
			try{
				stream.close();
			} catch(IOException ex){
				logger.log(Level.WARNING, "fail to close stream", ex);
			}
		}
		return;
	}

	// ======================================================================
	// メッセージリソースのフォーマット
	// ======================================================================
	/**
	 * 指定されたメッセージリソースを参照し引数でフォーマットして返します。メッセージが定義されて
	 * いない場合は例外が発生します。
	 * <p>
	 * @param id メッセージ ID
	 * @param args メッセージのフォーマット引数
	 * @return フォーマットしたメッセージ
	 * @throws MissingResourceException リソースが定義されていない場合
	 */
	public static String format(String id, Object... args) throws MissingResourceException{
		ResourceBundle res = ResourceBundle.getBundle("org.koiroha.xml.parser.messages");
		String msg = res.getString(id);
		return String.format(msg, args);
	}

	// ======================================================================
	// ストリームの判定
	// ======================================================================
	/**
	 * ストリームが指定された文字シーケンスで始まっているかどうかを判定します。
	 * <p>
	 * @param in 判定するストリーム
	 * @param sequence 判定するシーケンス
	 * @param ignoreCase 大文字小文字を無視する場合 true
	 * @return 一致する場合 true
	 * @throws IOException 読み込みに失敗した場合
	 */
	public static boolean streamStartsWith(PushbackReader in, String sequence, boolean ignoreCase) throws IOException{
		assert(sequence.indexOf('\uFFFF') < 0 || sequence.indexOf('\uFFFF') == sequence.length()-1): sequence;
		assert(sequence.indexOf('\uFFFE') < 0 || sequence.indexOf('\uFFFE') == sequence.length()-1): sequence;
		char[] preread = new char[sequence.length()];
		for(int i=0; i<sequence.length(); i++){
			int ch = in.read();

			// 文字が一致しなければ終了
			if(! matches(sequence.charAt(i), ch, ignoreCase)){
				if(ch >= 0){
					in.unread(ch);
				}
				in.unread(preread, 0, i);
				return false;
			}

			// 読み込んだ文字を保持
			preread[i] = (char)ch;
		}

		// 読み込んだ文字をストリームへ戻す
		if(sequence.charAt(sequence.length()-1) != '\uFFFF'){
			in.unread(preread);
		} else {
			in.unread(preread, 0, preread.length-1);
		}
		return true;
	}

	// ======================================================================
	// 文字パターン一致判定
	// ======================================================================
	/**
	 * 文字のパターン一致判定を行います。特殊な意味を持つ文字は以下の通りです。
	 * <p>
	 * <ul>
	 * <li> '\0' - XML で定義されている{@link Xml#isWhitespace(int) 空白文字}と一致</li>
	 * <li> '\uFFFE' - EOF 以外の任意の文字に一致</li>
	 * <li> '\uFFFF' - EOF に一致</li>
	 * </ul>
	 * <p>
	 * @param pattern 文字パターン
	 * @param ch 文字
	 * @param ignoreCase 大文字小文字を無視する場合 true
	 * @return 一致する場合 true
	 */
	public static boolean matches(char pattern, int ch, boolean ignoreCase){
		if(pattern == '\0'){
			return Xml.isWhitespace(ch);
		}
		if(pattern == '\uFFFF'){
			return (ch < 0);
		}
		if(pattern == '\uFFFE'){
			return (ch >= 0);
		}
		if(ignoreCase){
			return Character.toLowerCase(pattern) == Character.toLowerCase((char)ch);
		}
		return (pattern == ch);
	}

	// ======================================================================
	// 適切な親要素の参照
	// ======================================================================
	/**
	 * 指定された HTML 要素を連結するのに適切な要素を参照します。
	 * <p>
	 * @param current 連結しようとしている要素
	 * @param elem 連結しようとしている要素
	 * @return elem の連結に適切な親要素
	 */
	public static Element getPreferredParent(Element current, Element elem){
		assert(current != null);

		// 指定された要素の親が決定可能な場合は親をたどって適切な位置を調査
		String elemName = getName(elem).toLowerCase();
		Set<String> parentNames = PARENT_RELATION.get(elemName);
		if(parentNames != null){
			Element mover = current;
			Node parent = mover.getParentNode();
			while(true){

				// 定義に含まれている親要素を検出した場合はそれを返す
				if(parentNames.contains(getName(mover).toLowerCase())){
					return mover;
				}

				// 要素としての親が存在しなくなったら終了
				if(! (parent instanceof Element)){
					break;
				}

				// 上位の要素へ移動
				mover = (Element)parent;
				parent = parent.getParentNode();
			}
		}

		// <p> 要素は歴史的に単体出現可能で親も不定なため、親をたどって非テキスト装飾タグに連結
		if(elemName.equals("p")){
			Element mover = current;
			while(true){

				// 非テキスト修飾要素で <p> 以外の要素を検出したらそれを返す
				String name = getName(mover).toLowerCase();
				if(! TEXT_DECORATION_ELEMENT.contains(name) && ! name.equals(elemName)){
					return mover;
				}

				// 要素としての親が存在しなくなったら終了
				if(! (mover.getParentNode() instanceof Element)){
					break;
				}

				// 上位の要素へ移動
				mover = (Element)mover.getParentNode();
			}
		}

		// 不明な場合は現在の位置を返す
		return current;
	}

	// ======================================================================
	// 要素名の参照
	// ======================================================================
	/**
	 * 指定された要素の名前を参照します。要素が DOM Level 3 をサポートしている場合はその
	 * ローカル名が返されます。サポートしていない場合はタグ名を返します。
	 * <p>
	 * @param elem 名前を参照する要素
	 * @return 要素の名前
	 */
	public static String getName(Element elem){
		String name = elem.getLocalName();
		if(name == null){
			name = elem.getTagName();
		}
		return name;
	}

	// ======================================================================
	// 属性値の解析
	// ======================================================================
	/**
	 * 指定された文字列を要素内の属性定義部分とみなして属性を解析します。このメソッドは先読み
	 * バッファから文字エンコーディングを決定するためのものであり、属性解析に対する通常の警告通知を
	 * 行いません。属性名は全て小文字に変換されています。
	 * <p>
	 * @param body 要素の属性部分
	 * @return 解析した属性のマップ
	 */
	public static Map<String,String> parseAttributesSimply(CharSequence body){
		Map<String,String> attr = new HashMap<String, String>();
		Matcher matcher = PATTERN_ATTRIBUTE.matcher(body);
		while(matcher.find()){
			String name = matcher.group(1).toLowerCase();
			String equal = matcher.group(2);
			String value = matcher.group(3);
			if(equal.length() == 0){
				value = name;
			} else {
				if(value.length() >= 2 && value.charAt(0) == value.charAt(value.length()-1) && (value.charAt(0) == '\"' || value.charAt(0) == '\'')){
					value = value.substring(1, value.length() - 1);
				}
				value = Xml.unescape(value);
			}
			attr.put(name, value);
		}
		return attr;
	}

}
