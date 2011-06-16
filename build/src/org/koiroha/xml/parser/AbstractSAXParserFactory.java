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
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.koiroha.xml.Xml;
import org.xml.sax.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// AbstractSAXParserFactory: SAX パーサファクトリ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * SAX パーサファクトリの抽象実装クラスです。
 * <p>
 * @version $Revision: 1.6 $ $Date: 2010/07/18 15:27:42 $
 * @author torao
 * @since 2009/03/31 Java2 SE 5.0
 */
public abstract class AbstractSAXParserFactory extends SAXParserFactory {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AbstractSAXParserFactory.class.getName());

	// ======================================================================
	// パーサ機能
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサの機能です。
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
	private boolean xincludeAware = false;

	// ======================================================================
	// スキーマ
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが使用するスキーマです。
	 * <p>
	 */
	private Schema schema = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	protected AbstractSAXParserFactory() {
		return;
	}
	// ======================================================================
	// 機能の参照
	// ======================================================================
	/**
	 * このファクトリインスタンスに設定されている機能を参照します。機能名に対する設定が行われて
	 * いない場合は false を返します。
	 * <p>
	 * @param name 機能名
	 * @return 機能が有効な場合 true
	 * @throws SAXNotRecognizedException 指定された名前の機能名を使用できない場合
	 */
	@Override
	public boolean getFeature(String name) throws SAXNotRecognizedException{
		Boolean value = feature.get(name);
		if(value == null){
			return false;
		}
		return value;
	}

	// ======================================================================
	// 機能の設定
	// ======================================================================
	/**
	 * 指定された機能の有効/無効を切り替えます。
	 * <p>
	 * @param name 機能名
	 * @param value 機能を有効にする場合 true
	 * @throws SAXNotRecognizedException 指定された名前の機能名を使用できない場合
	 * @throws SAXNotSupportedException 指定された七前の機能をサポートしていない場合
	 */
	@Override
	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException{
		logger.finest("setFuture(" + name + "," + value + ")");
		feature.put(name, value);
		return;
	}

	// ======================================================================
	// 名前空間有効性の参照
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが名前空間を認識するかどうかを参照します。
	 * <p>
	 * @return 名前空間を認識する場合 true
	 */
	@Override
	public boolean isNamespaceAware() {
		try{
			return getFeature(Xml.FEATURE_NAMESPACES);
		} catch(SAXException ex){
			throw new IllegalStateException(ex);
		}
	}

	// ======================================================================
	// 名前空間有効性の設定
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが名前空間を認識するかどうかを設定します。
	 * <p>
	 * @param awareness 名前空間を認識する場合 true
	 */
	@Override
	public void setNamespaceAware(boolean awareness) {
		try{
			setFeature(Xml.FEATURE_NAMESPACES, awareness);
		} catch(SAXException ex){
			throw new IllegalStateException(ex);
		}
		return;
	}

	// ======================================================================
	// DTD 検証有効性の参照
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが DTD 検証を行うかどうかを参照します。
	 * <p>
	 * @return DTD 検証を行う場合 true
	 */
	@Override
	public boolean isValidating() {
		try{
			return getFeature(Xml.FEATURE_VALIDATION);
		} catch(SAXException ex){
			throw new IllegalStateException(ex);
		}
	}

	// ======================================================================
	// DTD 検証有効性の設定
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが DTD 検証を行うかどうかを設定します。
	 * <p>
	 * @param awareness DTD 検証を行う場合 true
	 */
	@Override
	public void setValidating(boolean awareness) {
		try{
			setFeature(Xml.FEATURE_VALIDATION, awareness);
		} catch(SAXException ex){
			throw new IllegalStateException(ex);
		}
		return;
	}

	// ======================================================================
	// XInclude の有効性設定
	// ======================================================================
	/**
	 * XInclude の有効性を設定します。
	 * <p>
	 * @param state XInclude を有効にする場合 true
	*/
	@Override
	public void setXIncludeAware(boolean state) {
		xincludeAware = state;
		return;
	}

	// ======================================================================
	// XInclude の有効性参照
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが XInclude を認識するかどうかを参照します。
	 * <p>
	 * @return XInclude が有効な場合 true
	*/
	@Override
	public boolean isXIncludeAware() {
		return xincludeAware;
	}

	// ======================================================================
	// スキーマの設定
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが使用するスキーマを設定します。
	 * <p>
	 * @param schema スキーマ
	*/
	@Override
	public void setSchema(Schema schema) {
		this.schema = schema;
		return;
	}

	// ======================================================================
	// スキーマの参照
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが使用するスキーマを参照します。
	 * <p>
	 * @return スキーマ
	*/
	@Override
	public Schema getSchema() {
		return schema;
	}

	// ======================================================================
	// 内容からの入力ソースの参照
	// ======================================================================
	/**
	 * 指定されたバイナリストリームの内容から文字エンコーディングを決定し、解析に使用可能な入力
	 * ソースとして返します。この機能はストリームを先読みすることに注意してください。メソッドは
	 * ストリームから最大 maxLength バイトのバイナリデータを読み込み {@link
	 * #guessEncodingFromPrereadSampling(byte[], int)} に渡してエンコーディングを
	 * 推測します。先読みしたバイナリデータは {@link SequenceInputStream} で元の
	 * ストリームと連結され入力ストリームに設定されます。
	 * <p>
	 * HTTP によって取得した内容のエンコーディングは Content-Type で指定されている charset
	 * 属性よりコンテンツ内に記述されている XML 宣言や HTML の META 要素を優先した方が、
	 * 多くの場合により良い結果を得られます。
	 * <p>
	 * 返値の入力ソースには {@link InputSource#getCharacterStream()} と {@link
	 * InputSource#getEncoding()} が設定されています。
	 * <p>
	 * @param in 入力ストリーム
	 * @param charset デフォルトのエンコーディング
	 * @param maxLength ストリームから先読みする最大バイトサイズ
	 * @return 推測した入力ソース
	 * @throws IOException ストリームからの先読みに失敗した場合
	 * @throws UnsupportedEncodingException 指定された charset が認識できない場合
	 * @see Xml#getCharset(String)
	 */
	public InputSource guessInputSource(InputStream in, String charset, int maxLength) throws IOException{

		// エンコーディング参照のためにストリームを先読み
		logger.finest("reading " + maxLength + " bytes stream heading to determine stream encoding...");
		byte[] preread = new byte[maxLength];
		int length = 0;
		while(preread.length - length > 0){
			int len = in.read(preread, length, preread.length - length);
			if(len < 0){
				break;
			}
			length += len;
		}
		in = new SequenceInputStream(
				new ByteArrayInputStream(preread, 0, length), in);

		// 先読みした内容からエンコーディングを決定
		String encoding = guessEncodingFromPrereadSampling(preread, length);

		// ストリームの内容からエンコーディングを決定できない場合は指定されたエンコーディングを使用
		if(encoding == null || ! Charset.isSupported(encoding)){
			logger.finest("encoding is not specified in stream or not supported: " + encoding + ", apply " + charset);
			encoding = charset;
			if(! Charset.isSupported(encoding)){
				logger.warning("specified encoding is not supported in this environment: " + encoding);
				throw new UnsupportedEncodingException(encoding);
			}
		}

		// 入力ソースを構築して返す
		logger.finest("creating text reader: " + encoding);
		Reader r = new InputStreamReader(in, encoding);
		InputSource is = new InputSource(r);
		is.setEncoding(encoding);
		return is;
	}

	// ======================================================================
	// エンコーディングの推測
	// ======================================================================
	/**
	 * ストリームの内容からエンコーディングを決定するために {@link
	 * #guessInputSource(InputStream, String, int)} から呼び出されます。このクラスの
	 * メソッドでは先読みバッファ内の最初の 2 バイトが Unicode の Byte Order Mark (BOM)
	 * かどうかを評価し、BOM でない場合は {@link #guessEncodingFromPrereadSampling(String)}
	 * を呼び出します。
	 * <p>
	 * サブクラスでこのメソッドをオーバーライドして先読みした内容からエンコーディングを推測する
	 * 事が出来ます。
	 * <p>
	 * @param binary 先読みしたバッファ
	 * @param length バッファ内の有効文字の長さ
	 * @return 推測したエンコーディング
	 */
	protected String guessEncodingFromPrereadSampling(byte[] binary, int length) {

		// Unicode の BOM を検出した場合
		if(length >= 2){
			if(((binary[0] & 0xFF) == 0xFE && (binary[1] & 0xFF) == 0xFF)
			|| ((binary[0] & 0xFF) == 0xFF && (binary[1] & 0xFF) == 0xFE)){
				logger.finest("byte order mark detected");
				return "UTF-16";	// J2SE5.0 では Charset に "Unicode" は使えない
			}
		}

		// US-ASCII の文字列として解析
		Charset usascii = Charset.forName("us-ascii");
		try{
			String sample = new String(binary, 0, length, usascii.name());
			return guessEncodingFromPrereadSampling(sample);
		} catch(UnsupportedEncodingException ex){
			throw new IllegalStateException(ex);
		}
	}

	// ======================================================================
	// エンコーディングの推測
	// ======================================================================
	/**
	 * バイナリストリームの文字エンコーディングを推測するために
	 * {@link #guessEncodingFromPrereadSampling(byte[], int)} から呼び出されます。
	 * 引数 <i>sample</i> は先読みしたバイナリを us-ascii でエンコーディングした文字列です。
	 * <p>
	 * このクラスでは先読みした文字列内から XML 宣言を参照してその encoding 値を返します。
	 * 文字列内に XML 宣言が存在しない場合は null を返します。XML 宣言が存在し encoding
	 * 指定が省略されている場合は XML 仕様に基づいて UTF-8 と判定します。
	 * <p>
	 * このメソッドをサブクラスでオーバーライドして先読みした内容からエンコーディングを推測する
	 * 事が出来ます。
	 * <p>
	 * @param sample 先読みした文字列
	 * @return 内容から推測した文字エンコーディング
	 */
	protected String guessEncodingFromPrereadSampling(String sample) {
		Pattern pattern = Pattern.compile("<\\?xml\\s+(.*)\\?>", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(sample);
		if(matcher.find()){
			String meta = matcher.group(1);
			Map<String,String> attr = Toolkit.parseAttributesSimply(meta);
			String encoding = attr.get("encoding");
			if(encoding == null){
				encoding = "UTF-8";
			}
			return encoding;
		}
		return null;
	}

}
