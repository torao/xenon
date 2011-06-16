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

import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.*;

import javax.xml.parsers.*;

import org.koiroha.xml.Xml;
import org.xml.sax.SAXException;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// HTMLParserFactory: HTML パーサファクトリ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML 文書解析するためのルーズ XML パーサファクトリクラスです。
 * <p>
 * @version $Revision: 1.4 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/03/31 Java2 SE 5.0
 */
public class HTMLParserFactory extends AbstractSAXParserFactory {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(HTMLParserFactory.class.getName());

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。通常はこのコンストラクタを直接使用せず
	 * {@link SAXParserFactory#newInstance()} 経由で使用してください。
	 * <p>
	 * <pre>import org.koiroha.xml.parser.*;
	 * ...
	 * SAXParserFactory factory = SAXParserFactory.newInstance(
	 *     HTMLParserFactory.class.getName(), HTMLParserFactory.getClassLoader());
	 * </pre>
	 * <p>
	 */
	public HTMLParserFactory() {
		setLowerCaseName(true);
		return;
	}

	// ======================================================================
	// 名前の小文字変換設定
	// ======================================================================
	/**
	 * このファクトリから生成されるパーサが要素名や属性名を暗黙的に小文字として扱うよう設定します。
	 * HTML では大文字と小文字を区別しない
	 * <p>
	 * @param lowercase 小文字として扱う場合 true
	 */
	public void setLowerCaseName(boolean lowercase){
		try{
			setFeature(LooseXMLReader.FEATURE_LOWERCASE_NAME, lowercase);
		} catch(SAXException ex){
			throw new IllegalStateException(ex);
		}
		return;
	}

	// ======================================================================
	// 名前の小文字変換設定
	// ======================================================================
	/**
	 * 要素名や属性名を暗黙的に小文字として扱うよう設定します。
	 * <p>
	 * @return 小文字として扱う場合 true
	 */
	public boolean isLowerCaseName(){
		try{
			return getFeature(LooseXMLReader.FEATURE_LOWERCASE_NAME);
		} catch(SAXException ex){
			throw new IllegalStateException(ex);
		}
	}

	// ======================================================================
	// SAX パーサの参照
	// ======================================================================
	/**
	 * 新規の HTML パーサを作成します。
	 * <p>
	 * @return 新規の HTML パーサ
	 */
	@Override
	public SAXParser newSAXParser(){
		SAXParser parser = new HTMLParser(feature, isXIncludeAware(), getSchema());
		return parser;
	}

	// ======================================================================
	// エンコーディングの推測
	// ======================================================================
	/**
	 * 指定された先読みバッファの &lt;meta http-equiv="content-type"&gt; 要素から
	 * ストリームのエンコーディングを推測します。
	 * <p>
	 * @param sample 先読みした文字列
	 * @return 推測した文字セット
	 */
	@Override
	protected String guessEncodingFromPrereadSampling(String sample) {

		// スーパークラスで決定できていればそれを返す
		String encoding = super.guessEncodingFromPrereadSampling(sample);
		if(encoding != null){
			return encoding;
		}

		// <meta> 要素に指定された Content-Type 属性からエンコーディングを決定
		Pattern pattern = Pattern.compile("<meta\\s+([^>]*)/?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(sample);
		while(matcher.find()){
			String meta = matcher.group(1);
			Map<String,String> attr = Toolkit.parseAttributesSimply(meta);
			String httpEquiv = attr.get("http-equiv");
			String content = attr.get("content");
			if(httpEquiv != null && httpEquiv.toLowerCase().equals("content-type") && content != null){
				Charset charset = Xml.getCharset(content);
				if(charset != null){
					return charset.name();
				}
			}
		}
		logger.finest("meta element with http-quiv=\"content-type\" is not specified, or charset attribute unrecognized");
		return null;
	}

}
