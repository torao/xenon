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

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.*;

import org.koiroha.xml.parser.HTMLDocumentBuilderFactory;
import org.xml.sax.InputSource;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Html: HTML ユーティリティクラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML を扱うためのユーティリティクラスです。
 * <p>
 * @version $Revision: 1.3 $ $Date: 2010/07/18 15:26:26 $
 * @author torao
 * @since 2010/01/19 Java2 SE 5.0
 */
public final class Html {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final Logger logger = Logger.getLogger(Html.class.getName());

	// ======================================================================
	// エンティティ → 文字マップ
	// ======================================================================
	/**
	 * エンティティを文字に変換するためのマップです。
	 * <p>
	 */
	private static final Map<String,Character> ENTITY2CHAR = new HashMap<String, Character>();

	// ======================================================================
	// 文字 → エンティティマップ
	// ======================================================================
	/**
	 * 文字をエンティティに変換するためのマップです。
	 * <p>
	 */
	private static final Map<Character,String> CHAR2ENTITY = new HashMap<Character, String>();

	// ======================================================================
	// 数値参照パターン
	// ======================================================================
	/**
	 * 数値参照のパターンです。
	 * <p>
	 */
	private static final Pattern NUM10_REF = Pattern.compile("&#([\\d]{1,5});");

	// ======================================================================
	// 数値参照パターン
	// ======================================================================
	/**
	 * 数値参照のパターンです。
	 * <p>
	 */
	private static final Pattern NUM16_REF = Pattern.compile("&#[xX]([\\da-fA-F]{1,4});");

	// ======================================================================
	// スタティックイニシャライザ
	// ======================================================================
	/**
	 * リソースの読み込みを行います。
	 * <p>
	 */
	static{

		// エンティティ定義の読み込み
		ResourceBundle res = ResourceBundle.getBundle("org.koiroha.xml.htmlentity");
		Enumeration<String> en = res.getKeys();
		while(en.hasMoreElements()){
			String key = en.nextElement();
			String value = res.getString(key);
			String entity = "&" + key + ";";
			Character ch = Character.valueOf((char)Integer.parseInt(value));
			ENTITY2CHAR.put(entity, ch);
			CHAR2ENTITY.put(ch, entity);
		}
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタはクラス内に隠蔽されています。
	 * <p>
	 */
	private Html() {
		return;
	}

	// ======================================================================
	// 文字の参照
	// ======================================================================
	/**
	 * 指定された HTML 実体参照に対応する文字を返します。未定義の実体参照が指定された場合は負の
	 * 値を返します。パラメータ entity には "&amp;nbsp;" のように '&amp;' と ';' を
	 * 含める必要があります。
	 * <p>
	 * &amp;apos; は HTML では定義されていませんので負の値を返します。
	 * <p>
	 * @param entity 実体参照の文字列 ("&amp;nbsp;", "&amp;#1024;" など)
	 * @return 実体参照に対応する文字
	 * @throws NullPointerException entity に null を指定した場合
	 */
	public static int getCharacter(String entity) throws NullPointerException{

		// パラメータを確認
		if(entity == null){
			throw new NullPointerException();
		}

		// パラメータの確認
		if(entity.length() < 2 || entity.charAt(0) != '&' || entity.charAt(entity.length()-1) != ';'){
			logger.fine("invalid entity reference: " + entity + "; entity must start with '&' and end with ';'.");
			return -1;
		}

		// 実体参照から文字を取得
		Character ch = ENTITY2CHAR.get(entity);
		if(ch != null){
			return ch.charValue();
		}

		// 数値参照を解除 (10進数)
		Matcher m = NUM10_REF.matcher(entity);
		if(m.matches()){
			int num = Integer.parseInt(m.group(1));
			if(num >= Character.MIN_VALUE && num <= Character.MAX_VALUE){
				return num;
			}
		}

		// 数値参照を解除 (16進数)
		m = NUM16_REF.matcher(entity);
		if(m.matches()){
			int num = Integer.parseInt(m.group(1), 16);
			if(num >= Character.MIN_VALUE && num <= Character.MAX_VALUE){
				return num;
			}
		}

		return -1;
	}

	// ======================================================================
	// 実体参照の参照
	// ======================================================================
	/**
	 * 指定された文字に対する実体参照または数値参照を参照します。このメソッドは有効な実体参照が
	 * 定義されていない有効な Unicode 文字であっても数値参照に変換します。
	 * <p>
	 * @param ch 実体参照を参照する文字
	 * @return 実体参照
	 */
	public static String getEntityReference(char ch){
		String entity = CHAR2ENTITY.get(ch);
		if(entity != null){
			return entity;
		}
		return "&#x" + Integer.toHexString(ch).toUpperCase() + ";";
	}

	// ======================================================================
	// HTML エスケープの実行
	// ======================================================================
	/**
	 * 指定された文字列を HTML でエスケープして返します。
	 * <p>
	 * @param text エスケープする文字列
	 * @return エスケープした文字列の出力先
	 */
	public static String escape(CharSequence text) {
		StringBuilder buffer = new StringBuilder((int)(text.length() * 1.1));
		try{
			escape(buffer, text);
		} catch(IOException ex){
			throw new IllegalStateException("StringBuilder thrown IOException!", ex);
		}
		return buffer.toString();
	}

	// ======================================================================
	// HTML エスケープの実行
	// ======================================================================
	/**
	 * 指定された文字列を HTML でエスケープして出力します。
	 * <p>
	 * @param out エスケープした文字列の出力先
	 * @param text エスケープする文字列
	 * @throws IOException 出力に失敗した場合
	 */
	public static void escape(Appendable out, CharSequence text) throws IOException{
		for(int i=0; i<text.length(); i++){
			char ch = text.charAt(i);
			String entity = CHAR2ENTITY.get(ch);
			if(entity == null){
				out.append(ch);
			} else {
				out.append(entity);
			}
		}
		return;
	}

	// ======================================================================
	// HTML エスケープの解除
	// ======================================================================
	/**
	 * HTML でエスケープされた文字列 (実体参照化された文字列) を解除して返します。
	 * <p>
	 * @param text エスケープを解除する文字列
	 * @return エスケープを解除した文字列の出力先
	 */
	public static String unescape(CharSequence text) {
		StringBuilder buffer = new StringBuilder((int)(text.length() * 1.1));
		try{
			unescape(buffer, text);
		} catch(IOException ex){
			throw new IllegalStateException("StringBuilder thrown IOException!", ex);
		}
		return buffer.toString();
	}

	// ======================================================================
	// HTML エスケープの解除
	// ======================================================================
	/**
	 * HTML でエスケープされた文字列 (実体参照化された文字列) を解除して出力します。
	 * <p>
	 * @param out エスケープを解除した文字列の格納先
	 * @param text 実体参照を戻す文字列
	 * @throws IOException 出力に失敗した場合
	 */
	public static void unescape(Appendable out, CharSequence text) throws IOException{
		Xml.unescape(out, text, new Xml.EntityMapper(){
			@Override
			public void map(Appendable out, String entity) throws IOException {
				entity = "&" + entity + ";";
				int ch = getCharacter(entity);
				if(ch < 0){
					out.append(entity);
				} else {
					out.append((char)ch);
				}
				return;
			}
		});
		return;
	}

	// ======================================================================
	// 入力ソースの推測
	// ======================================================================
	/**
	 * HTML の入力ストリームから適切な文字セットを推測し入力ソースを返します。この入力ソースは
	 * {@link InputSource#getCharacterStream()} が有効です。
	 * <p>
	 * Content-Type ヘッダなどから文字セットを取得するには {@link Xml#getCharset(String)}
	 * を使用してください。
	 * <p>
	 * @param in HTML が読み込まれる入力ストリーム
	 * @param charset 文字セットが不明な場合に適用される文字セット
	 * @param maxLength 文字セット推測のために先読みする最大バイト数
	 * @return 入力ソース
	 * @throws IOException 入力ソースの推測に失敗した場合
	 * @see HTMLDocumentBuilderFactory#guessInputSource(InputStream, String, int)
	 */
	public static InputSource guessInputSource(InputStream in, String charset, int maxLength) throws IOException{
		return new HTMLDocumentBuilderFactory().guessInputSource(in, charset, maxLength);
	}

}
