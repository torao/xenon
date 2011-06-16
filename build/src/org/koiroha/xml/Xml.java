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

import java.io.IOException;
import java.nio.charset.*;
import java.util.regex.*;

import org.xml.sax.ext.LexicalHandler;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Xml: XML 用ユーティリティクラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * XML 用のユーティリティクラスです。
 * <p>
 * @version $Revision: 1.4 $ $Date: 2010/07/18 15:26:47 $
 * @author torao
 * @since 2009/04/06 Java2 SE 5.0
 */
public final class Xml {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Xml.class.getName());

	// ======================================================================
	// 標準接頭辞
	// ======================================================================
	/**
	 * 標準機能の接頭辞です。
	 * <p>
	 */
	private static final String FEATURE_PREFIX = "http://xml.org/sax/features/";

	// ======================================================================
	// 名前空間認識
	// ======================================================================
	/**
	 * 解析時に名前空間を認識するかどうかの機能フラグ名です。
	 * 定数値 {@value} を示します。
	 * <p>
	 */
	public static final String FEATURE_NAMESPACES = FEATURE_PREFIX + "namespaces";

	// ======================================================================
	// DTD 検証認識
	// ======================================================================
	/**
	 * 解析時に DTD 検証を行うかどうかの機能フラグ名です。
	 * 定数値 {@value} を示します。
	 * <p>
	 */
	public static final String FEATURE_VALIDATION = FEATURE_PREFIX + "validation";

	// ======================================================================
	// 標準接頭辞
	// ======================================================================
	/**
	 * 標準機能の接頭辞です。
	 * <p>
	 */
	private static final String PROPERTY_PREFIX = "http://xml.org/sax/properties/";

	// ======================================================================
	// ドキュメント XML バージョン
	// ======================================================================
	/**
	 * ドキュメント XML バージョンを参照するための SAX パーサのプロパティ名です。
	 * 定数値 {@value} を示します。
	 * <p>
	 */
	public static final String PROPERTY_DOCUMENT_XML_VERSION = PROPERTY_PREFIX + "document-xml-version";

	// ======================================================================
	// 構文ハンドラプロパティ名
	// ======================================================================
	/**
	 * 解析時に{@link LexicalHandler 構文ハンドラ}を指定する場合の SAX パーサのプロパティ
	 * 名です。定数値 {@value} を示します。
	 * <p>
	 */
	public static final String PROPERTY_LEXICAL_HANDLER = PROPERTY_PREFIX + "lexical-handler";

	// ======================================================================
	// 文字セット解析パターン
	// ======================================================================
	/**
	 * Content-Type から文字セット部分を取得するためのパターンです。
	 * <p>
	 */
	private static final Pattern CHARSET_ATTRIBUTE = Pattern.compile(";\\s*charset\\s*=\\s*[\"\']?([^\"\'\\s;]*)[\"\']?", Pattern.CASE_INSENSITIVE);

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタはクラス内に隠蔽されています。
	 * <p>
	 */
	private Xml() {
		return;
	}

	// ======================================================================
	// 空白文字の判定
	// ======================================================================
	/**
	 * 指定された文字が XML での空白を表すかどうかを判定します。空白文字 ({@code ' '})、
	 * 水平タブ ({@code '\t'})、復帰 ({@code '\r'})、改行 ({@code '\n'}) を空白文字
	 * とします。
	 * <p>
	 * @param ch 判定する文字
	 * @return 空白文字の場合 true
	 */
	public static boolean isWhitespace(int ch) {
		return (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n');
	}

	// ======================================================================
	// 文字のエスケープ
	// ======================================================================
	/**
	 * 指定された文字列を XML のテキストとしてそのまま使用できるようにエスケープします。
	 * <p>
	 * @param text エスケープする文字列
	 * @return エスケープした文字列
	 */
	public static String escape(CharSequence text){
		StringBuilder buffer = new StringBuilder(text.length());
		try{
			escape(buffer, text);
		} catch(IOException ex){
			throw new IllegalStateException("StringBuilder thrown IOException!", ex);
		}
		return buffer.toString();
	}

	// ======================================================================
	// 文字のエスケープ
	// ======================================================================
	/**
	 * 指定された文字列を XML のテキストとしてそのまま使用できるようにエスケープします。
	 * <p>
	 * @param out エスケープした文字列の格納先
	 * @param text エスケープする文字列
	 * @throws IOException out への出力に失敗した場合
	 */
	public static void escape(Appendable out, CharSequence text) throws IOException{
		for(int i=0; i<text.length(); i++){
			char ch = text.charAt(i);
			switch(ch){
			case '<':	out.append("&lt;");		break;
			case '>':	out.append("&gt;");		break;
			case '&':	out.append("&amp;");	break;
			case '\"':	out.append("&quot;");	break;
			case '\'':	out.append("&apos;");	break;
			case '\n':	out.append('\n');		break;
			case '\t':	out.append('\t');		break;
			case '\r':
				out.append('\n');
				if(i+1<text.length() && text.charAt(i+1) == '\n'){
					i ++;
				}
				break;
			default:
				if(Character.isDefined(ch) && ! Character.isISOControl(ch)){
					out.append(ch);
				} else {
					out.append("&#" + (int)ch + ";");
				}
				break;
			}
		}
		return;
	}

	// ======================================================================
	// 文字のエスケープ解除
	// ======================================================================
	/**
	 * 指定された文字列を XML エンコードを解除します。
	 * <p>
	 * @param text エスケープを解除する文字列
	 * @return エスケープを解除した文字列
	 */
	public static String unescape(CharSequence text){
		StringBuilder buffer = new StringBuilder(text.length());
		try{
			unescape(buffer, text);
		} catch(IOException ex){
			throw new IllegalStateException("StringBuilder thrown IOException!", ex);
		}
		return buffer.toString();
	}

	// ======================================================================
	// 文字のエスケープ解除
	// ======================================================================
	/**
	 * 指定された文字列を XML エンコードを解除します。
	 * <p>
	 * @param out エスケープした文字列の格納先
	 * @param text エスケープを解除する文字列
	 * @throws IOException 文字列の出力に失敗した場合
	 */
	public static void unescape(Appendable out, CharSequence text) throws IOException{
		Xml.unescape(out, text, new Xml.EntityMapper(){
			@Override
			public void map(Appendable out, String entity) throws IOException {
				if(entity.equals("amp")){
					out.append('&');
				} else if(entity.equals("lt")){
					out.append('<');
				} else if(entity.equals("gt")){
					out.append('>');
				} else if(entity.equals("quot")){
					out.append('\"');
				} else if(entity.equals("apos")){
					out.append('\'');
				} else {
					super.map(out, entity);
				}
				return;
			}
		});
		return;
	}

	// ======================================================================
	// 文字セットの参照
	// ======================================================================
	/**
	 * <code>"text/html; charset=UTF-8"</code> のような Content-Type 値から
	 * 文字セットを参照します。Content-Type に null を指定した場合や、値を文字セットとして
	 * 認識できない場合は null を返します。
	 * <p>
	 * @param contentType Content-Type
	 * @return charset 属性に対する文字セット
	 */
	public static Charset getCharset(String contentType){
		logger.finest("getCharset(" + contentType + ")");
		if(contentType == null){
			return null;
		}

		// 正規表現を使用して文字セットを取得
		Matcher matcher = CHARSET_ATTRIBUTE.matcher(contentType);
		if(! matcher.find()){
			logger.finest("no charset specified: " + contentType);
			return null;
		}
		String charset = matcher.group(1);

		// 指定された文字セットがサポートされているかを確認
		boolean supported = false;
		try{
			supported = Charset.isSupported(charset);
		} catch(IllegalCharsetNameException ex){/* */}
		if(! supported){
			logger.finest("unsupported charset specified: " + charset + ": " + contentType);
			return null;
		}

		logger.finest("charset retrieved: " + charset + ": " + contentType);
		return Charset.forName(charset);
	}

	// ======================================================================
	// 文字のエスケープ解除
	// ======================================================================
	/**
	 * 指定された文字列を XML エンコードを解除します。
	 * <p>
	 * @param out エスケープした文字列の格納先
	 * @param text エスケープを解除する文字列
	 * @param mapper エンティティマッパー
	 * @throws IOException 文字列の出力に失敗した場合
	 */
	static void unescape(Appendable out, CharSequence text, EntityMapper mapper) throws IOException{

		int begin = 0;
		while(begin < text.length()){

			// エンティティの開始位置を参照
			int head = indexOf(text, begin, '&');
			if(head < 0){
				break;
			}

			// エンティティの終了位置を参照
			// TODO 文字数を限定すると効率が良くなる
			int tail = indexOf(text, head+1, ';');
			if(tail < 0){
				break;
			}

			// エンティティの前方の文字列を追加
			out.append(text, begin, head);

			// エンティティを追加
			String entity = text.subSequence(head + 1, tail).toString();
			mapper.map(out, entity);

			begin = tail + 1;
		}

		// 残った文字列を出力
		if(begin < text.length()){
			out.append(text, begin, text.length());
		}
		return;
	}

	// ======================================================================
	// インデックスの参照
	// ======================================================================
	/**
	 * 指定された文字の出現位置を参照します。文字が見付からない場合は負の値を返します。
	 * <p>
	 * @param text 文字シーケンス
	 * @param begin 検索の開始位置
	 * @param ch 検索する文字
	 * @return 文字の出現位置
	 */
	private static int indexOf(CharSequence text, int begin, char ch){
		for(int i=begin; i<text.length(); i++){
			if(text.charAt(i) == ch){
				return i;
			}
		}
		return -1;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// EntityMapper: エンティティマッパー
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 実体参照を文字に変換するマッパーインターフェースです。
	 * <p>
	 */
	static class EntityMapper {

		// ==================================================================
		// エンティティのマップ
		// ==================================================================
		/**
		 * 指定されたエンティティに対応する文字列を出力します。
		 * <p>
		 * @param out 出力先
		 * @param entity エンティティ
		 * @throws IOException 出力に失敗した場合
		 */
		public void map(Appendable out, String entity) throws IOException{
			if(entity.length() > 1 && entity.charAt(0) == '#'){
				boolean hex = (entity.length() > 2 && Character.toLowerCase(entity.charAt(1)) == 'x');
				int ch = 0;
				try{
					if(hex){
						ch = Integer.parseInt(entity.substring(2), 16);
					} else {
						ch = Integer.parseInt(entity.substring(1), 10);
					}
					if(ch < Character.MIN_VALUE || ch > Character.MAX_VALUE){
						throw new NumberFormatException(String.valueOf(ch));
					}
					out.append((char)ch);
				} catch(NumberFormatException ex){
					logger.fine("invalid number entity reference: &" + entity + ";");
					out.append('&').append(entity).append(';');
				}
			} else {
				logger.fine("unrecognized entity character reference: &" + entity + ";");
				out.append('&').append(entity).append(';');
			}
			return;
		}

	}

}
