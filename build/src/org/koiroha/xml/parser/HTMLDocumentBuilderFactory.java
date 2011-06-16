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
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.validation.Schema;

import org.koiroha.xml.Xml;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// HTMLDocumentBuilderFactory: HTML ドキュメントビルダーファクトリ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML ドキュメントビルダーファクトリです。well-formed でない HTML 文書を解析して DOM を
 * 構築することが出来ます。このファクトリから生成されるビルダーは XML として正しくない文書構造を
 * 内部で補正し、警告のみで DOM を作り上げます。この強引な方法は必ずしも全ての人の「想定通り」の
 * DOM を生成するわけではない事に注意してください。特に、既存の HTML を XHTML に変換するよう
 * な使い方をする場合には変換後の内容をよく確認してください。場合によってはビルダーが生成した
 * DOM を「想定通り」に補正するプログラムが必要になるかもしれません。
 * <p>
 * このビルダーから生成されるパーサは解析対象が well-formed な XML であればおおよそ完全な
 * DOM を生成する事が出来ますが完全な JAXP 互換を保証するものではなく、また標準の実装と置き換え
 * られるものではありません。DOM の構築に実行環境の JAXP 実装を使用しています。
 * <p>
 * このクラスは JAXP で用意されている標準のドキュメントビルダーファクトリと同じ方法で使用する
 * ことが出来ます。DocumentBuilderFactory の newInstance() メソッドにクラス名を指定して
 * 下さい。
 * <p>
 * <pre>DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(
 *     "org.koiroha.xml.parser.HTMLDocumentBuilderFactory", loader);
 * DocumentBuilder builder = factory.newDocumentBuilder();
 * Document html = builder.parse(in);</pre>
 * <p>
 * また解析対象が HTML であることが明確である場合の簡易的な方法として new によって構築する事も
 * できます。
 * <p>
 * <pre>HTMLBuilderFactory factory = new HTMLBuilderFactory();</pre>
 * <p>
 * @version $Revision: 1.6 $ $Date: 2010/07/18 15:28:03 $
 * @author torao
 * @since 2009/04/01 Java2 SE 5.0
 */
public class HTMLDocumentBuilderFactory extends DocumentBuilderFactory {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(HTMLDocumentBuilderFactory.class.getName());

	// ======================================================================
	// HTML 構造変更
	// ======================================================================
	/**
	 * HTML 要素を認識して適切な位置を設定する機能名です。定数値 {@value} を示します。
	 * <p>
	 * @see #setHtmlOptimize(boolean)
	 */
	public static final String FEATURE_HTML_OPTIMIZE = "http://www.koiroha.org/sax/features/htmloptimize";

	// ======================================================================
	// 小文字変換
	// ======================================================================
	/**
	 * 小文字変換の機能名です。
	 * <p>
	 */
	static final String FEATURE_LOWERCASE_NAME = LooseXMLReader.FEATURE_LOWERCASE_NAME;

	// ======================================================================
	// CDATA 展開機能
	// ======================================================================
	/**
	 * CDATA 展開の機能名です。
	 * <p>
	 */
	static final String FEATURE_COALESCING = "http://www.koiroha.org/sax/features/coalescing";

	// ======================================================================
	// コメント無視機能
	// ======================================================================
	/**
	 * コメント無視の機能名です。
	 * <p>
	 */
	static final String FEATURE_IGNORE_COMMENT = "http://www.koiroha.org/sax/features/ignorecomment";

	// ======================================================================
	// 属性順序維持機能
	// ======================================================================
	/**
	 * 属性の設定順序を維持する機能名です。
	 * ※動的プロキシを使用した Element の機能置き換えに Xerces が対応していない
	 * <p>
	 */
	// static final String FEATURE_KEEP_ATTRIBUTE_ORDER = "http://www.koiroha.org/sax/features/sequentialattribute";

	// ======================================================================
	// 機能フラグ
	// ======================================================================
	/**
	 * 機能フラグです。
	 * <p>
	 */
	private final Map<String,Boolean> feature = new HashMap<String,Boolean>();

	// ======================================================================
	// ドキュメントビルダーファクトリ
	// ======================================================================
	/**
	 * ドキュメントビルダーファクトリです。
	 * <p>
	 */
	private final DocumentBuilderFactory factory;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public HTMLDocumentBuilderFactory() {
		this(DocumentBuilderFactory.newInstance());
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定されたドキュメントビルダーファクトリを用いて HTML ドキュメントを作成するインスタンス
	 * を構築します。
	 * <p>
	 * @param factory ベースとなるファクトリ
	 */
	public HTMLDocumentBuilderFactory(DocumentBuilderFactory factory) {
		this.factory = factory;
		setLowerCaseName(true);
		setHtmlOptimize(false);
		return;
	}

	// ======================================================================
	// 属性の参照
	// ======================================================================
	/**
	 * このドキュメントビルダーファクトリの属性を参照します。
	 * <p>
	 * @param name 属性名
	 * @return 属性の値
	 * @throws IllegalArgumentException 属性名が認識できない場合
	 */
	@Override
	public Object getAttribute(String name) throws IllegalArgumentException {
		return factory.getAttribute(name);
	}

	// ======================================================================
	// 属性の設定
	// ======================================================================
	/**
	 * ファクトリに属性値を設定します。
	 * <p>
	 * @param name 属性名
	 * @param value 属性値
	 * @throws IllegalArgumentException 属性名が認識できない場合
	 */
	@Override
	public void setAttribute(String name, Object value) throws IllegalArgumentException {
		factory.setAttribute(name, value);
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
	@Override
	public boolean getFeature(String name) {
		Boolean value = this.feature.get(name);
		if(value == null){
			return false;
		}
		return value;
	}

	// ======================================================================
	// 機能フラグの設定
	// ======================================================================
	/**
	 * ファクトリの機能フラグを設定します。
	 * <p>
	 * @param name 機能名
	 * @param value 機能フラグ
	 */
	@Override
	public void setFeature(String name, boolean value) {
		logger.finest("setFeature(" + name + "," + value + ")");
		this.feature.put(name, value);
		return;
	}

	// ======================================================================
	// 小文字変換の設定
	// ======================================================================
	/**
	 * HTML 解析時に要素名や属性名を小文字に変換するかどうかを設定します。
	 * デフォルト値は true です。
	 * <p>
	 * @param lowerCase 小文字に変換する場合 true
	 * @see LooseXMLReader#FEATURE_LOWERCASE_NAME
	*/
	public void setLowerCaseName(boolean lowerCase){
		setFeature(LooseXMLReader.FEATURE_LOWERCASE_NAME, lowerCase);
		return;
	}

	// ======================================================================
	// 小文字変換の参照
	// ======================================================================
	/**
	 * HTML 解析時に要素名や属性名を小文字に変換するかどうかを参照します。
	 * <p>
	 * @return 小文字に変換する場合 true
	*/
	public boolean isLowerCaseName(){
		return getFeature(LooseXMLReader.FEATURE_LOWERCASE_NAME);
	}

	// ======================================================================
	// HTML 要素補正の設定
	// ======================================================================
	/**
	 * HTML 要素を認識して適切な位置への補正を行います。この機能は親が明らかな要素を正しい位置の
	 * 階層に移動します。この機能を使用することで &lt;/tr&gt; などの記述忘れ、あるいは
	 * &lt;li&gt;, &lt;p&gt;, &lt;option&gt; のように終了記述の省略に寛容な要素を含む
	 * HTML から意図した DOM を構築できる可能性があります。
	 * <p>
	 * ただしこの機能を有効にした場合の副作用として、要素の意図していない移動が行われるかもしれ
	 * ません。
	 * <p>
	 * <pre>
	 * &lt;head&gt;
	 *   &lt;noscript&gt;
	 *     &lt;meta http-equiv="Refresh" content="0;url=http://..."&gt;
	 *   &lt;/noscript&gt;
	 * &lt;/head&gt;
	 * </pre>
	 * <p>
	 * 上記の例は &lt;meta&gt; 要素の親が &lt;head&gt; と認識しているために下記のように
	 * 補正されます。
	 * <p>
	 * <pre>
	 * &lt;head&gt;
	 *   &lt;noscript&gt;
	 *     &lt;/noscript&gt;&lt;meta http-equiv="Refresh" content="0;url=http://..."&gt;
	 *
	 * &lt;/head&gt;
	 * </pre>
	 * <p>
	 * この機能を使用する場合はエラーハンドラに通知される警告に注目し、その補正が意図した通りの
	 * ものかどうかを確認する事を推奨します。特にトリッキーな (行儀の悪い) 記述を含む HTML から
	 * DOM を生成する場合はこの機能を無効にして自分で補正した方が良い場合があります。
	 * <p>
	 * この機能のデフォルト値は false です。
	 * <p>
	 * @param html 小文字に変換する場合 true
	 * @see #FEATURE_HTML_OPTIMIZE
	*/
	public void setHtmlOptimize(boolean html){
		setFeature(FEATURE_HTML_OPTIMIZE, html);
		return;
	}

	// ======================================================================
	// 小文字変換の参照
	// ======================================================================
	/**
	 * HTML 解析時に要素名や属性名を小文字に変換するかどうかを参照します。
	 * <p>
	 * @return 小文字に変換する場合 true
	*/
	public boolean isHtmlOptimize(){
		return getFeature(FEATURE_HTML_OPTIMIZE);
	}

//	// ======================================================================
//	// 属性設定順序維持の設定
//	// ======================================================================
//	/**
//	 * 要素に対する属性の列挙 (Element.getAttributes()) において属性設定時の
//	 * 順序を維持するかどうかを設定します。
//	 * <p>
//	 * この機能のデフォルト値は false です。
//	 * <p>
//	 * @param flag 小文字に変換する場合 true
//	*/
//	public void setKeepAttributeOrder(boolean flag){
//		setFeature(FEATURE_KEEP_ATTRIBUTE_ORDER, flag);
//		return;
//	}
//
//	// ======================================================================
//	// 属性設定順序位置の判定
//	// ======================================================================
//	/**
//	 * 属性の列挙順序を設定順に行うかを参照します。
//	 * <p>
//	 * @return 設定順に列挙する場合 true
//	*/
//	public boolean isKeepAttributeOrder(){
//		return getFeature(FEATURE_KEEP_ATTRIBUTE_ORDER);
//	}

	// ======================================================================
	// CDATA セクション展開の設定
	// ======================================================================
	/**
	 * HTML 中の CDATA セクションを通常の{@link Text テキスト}としてバインドするかを設定
	 * します。true を指定した場合、ビルダーが生成する DOM に CDATA セクションは含まれません。
	 * <p>
	 * @param coalescing CDATA セクションをテキストとして扱う場合 true
	*/
	@Override
	public void setCoalescing(boolean coalescing) {
		setFeature(FEATURE_COALESCING, coalescing);
		factory.setCoalescing(coalescing);
		return;
	}

	// ======================================================================
	// CDATA セクション展開の参照
	// ======================================================================
	/**
	 * このファクトリから生成されるビルダーが CDATA セクションをテキストに展開するかどうかを
	 * 参照します。
	 * <p>
	 * @return CDATA セクションをテキストとして扱う場合 true
	*/
	@Override
	public boolean isCoalescing() {
		return factory.isCoalescing();
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * 実装されていません。HTML で定義されている実体参照は常に展開され、認識できないものは
	 * テキストとして扱われます。
	 * <p>
	 * @param expandEntityRef 実体参照を展開する場合 true
	*/
	@Override
	public void setExpandEntityReferences(boolean expandEntityRef) {
		factory.setExpandEntityReferences(expandEntityRef);
		return;
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * 実装されていません。HTML で定義されている実体参照は常に展開され、認識できないものは
	 * テキストとして扱われます。
	 * <p>
	 * @return 実体参照を展開する場合 true
	*/
	@Override
	public boolean isExpandEntityReferences() {
		return factory.isExpandEntityReferences();
	}

	// ======================================================================
	// コメント無視の設定
	// ======================================================================
	/**
	 * このファクトリから生成されるビルダーがコメントを無視するかどうかを設定します。true を
	 * 指定した場合、ビルダーから生成される DOM がコメントが省略されています。デフォルトは
	 * false が設定されています。
	 * <p>
	 * @param ignoreComments コメントを無視する場合 true
	*/
	@Override
	public void setIgnoringComments(boolean ignoreComments) {
		setFeature(FEATURE_IGNORE_COMMENT, ignoreComments);
		factory.setIgnoringComments(ignoreComments);
		return;
	}

	// ======================================================================
	// コメント無視の参照
	// ======================================================================
	/**
	 * このファクトリから生成されるビルダーがコメントを無視するかどうかを参照します。
	 * <p>
	 * @return コメントを無視する場合 true
	*/
	@Override
	public boolean isIgnoringComments() {
		return factory.isIgnoringComments();
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * 実装されていません。
	 * <p>
	 * @param whitespace 無視可能な空白を無視する場合 true
	*/
	@Override
	public void setIgnoringElementContentWhitespace(boolean whitespace) {
		factory.setIgnoringElementContentWhitespace(whitespace);
		return;
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * 実装されていません。
	 * <p>
	 * @return 無視可能な空白を無視する場合 true
	*/
	@Override
	public boolean isIgnoringElementContentWhitespace() {
		return factory.isIgnoringElementContentWhitespace();
	}

	// ======================================================================
	// 名前空間の設定
	// ======================================================================
	/**
	 * このファクトリから生成されるビルダーが XML の名前空間を認識するかどうかを設定します。
	 * 名前空間を有効にした場合、ビルダーが生成する DOM の要素/属性は名前空間 URI とローカル名
	 * を使用することが出来ます。
	 * デフォルトは false に設定されています。
	 * <p>
	 * HTML ビルダーは文書中に名前空間が未定義の接頭辞が含まれていても DOM を生成する事が
	 * 出来ますが、そのような DOM は Transformer などで使用できない可能性がありますので注意
	 * してください。
	 * <p>
	 * @param awareness 名前空間を有効にする場合 true
	*/
	@Override
	public void setNamespaceAware(boolean awareness) {
		setFeature(Xml.FEATURE_NAMESPACES, awareness);
		factory.setNamespaceAware(awareness);
		return;
	}

	// ======================================================================
	// 名前空間の有効性参照
	// ======================================================================
	/**
	 * このファクトリが生成したビルダーが名前空間を認識するかどうかを参照します。
	 * <p>
	 * @return 名前空間が有効な場合 true
	*/
	@Override
	public boolean isNamespaceAware() {
		return factory.isNamespaceAware();
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * スキーマによる検証は実装されていません。
	 * <p>
	 * @param schema 使用するスキーマ
	*/
	@Override
	public void setSchema(Schema schema) {
		factory.setSchema(schema);
		return;
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * スキーマによる検証は実装されていません。
	 * <p>
	 * @return 使用するスキーマ
	*/
	@Override
	public Schema getSchema() {
		return factory.getSchema();
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * DTD による検証は実装されていません。
	 * <p>
	 * @param validating 検証を行う場合 true
	*/
	@Override
	public void setValidating(boolean validating) {
		setFeature(Xml.FEATURE_VALIDATION, validating);
		factory.setValidating(validating);
		return;
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * DTD による検証は実装されていません。
	 * <p>
	 * @return 検証を行う場合 true
	*/
	@Override
	public boolean isValidating() {
		return factory.isValidating();
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * XInclude は実装されていません。
	 * <p>
	 * @param state XInclude が有効な場合 true
	*/
	@Override
	public void setXIncludeAware(boolean state) {
		factory.setXIncludeAware(state);
		return;
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * XInclude は実装されていません。
	 * <p>
	 * @return XInclude が有効な場合 true
	*/
	@Override
	public boolean isXIncludeAware() {
		return factory.isXIncludeAware();
	}

	// ======================================================================
	// ドキュメントビルダーの参照
	// ======================================================================
	/**
	 * 新規のドキュメントビルダーを構築します。
	 * <p>
	 * @return ドキュメントビルダー
	 * @throws ParserConfigurationException パーサの構成が不正な場合
	 */
	@Override
	public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return new HTMLDocumentBuilder(factory.newDocumentBuilder(), feature);
	}

	// ======================================================================
	// 内容からの入力ソースの参照
	// ======================================================================
	/**
	 * 指定されたバイナリストリームの内容を先読みして文字エンコーディングを決定し、解析に使用可能な
	 * 入力ソースとして返します。HTTP の Content-Type ヘッダを用いて内容のエンコーディングを
	 * 決定するよりもコンテンツ内に記述されているエンコーディングを優先した方が適切な場合に使用
	 * してください。
	 * <p>
	 * @param in 入力ストリーム
	 * @param charset デフォルトのエンコーディング
	 * @param maxLength ストリームから先読みする最大バイトサイズ
	 * @return エンコーディングを推測した入力ソース
	 * @throws IOException ストリームからの先読みに失敗した場合
	 * @see HTMLParserFactory#guessInputSource(InputStream, String, int)
	 */
	public InputSource guessInputSource(InputStream in, String charset, int maxLength) throws IOException{
		return new HTMLParserFactory().guessInputSource(in, charset, maxLength);
	}

}
