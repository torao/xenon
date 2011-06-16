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

import org.koiroha.xml.Xml;
import org.w3c.dom.Node;
import org.xml.sax.ext.Locator2;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LocatorReader: ロケータ入力ストリーム
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Locator 機能を備えた入力ストリームクラスです。現在の読み出し位置をマークする事が出来ます。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/03/31 Java2 SE 5.0
 */
public class LocatorReader extends PushbackReader implements Locator2 {

	// ======================================================================
	// PUBLIC ID
	// ======================================================================
	/**
	 * PUBLIC ID です。
	 * <p>
	 */
	private final String publicId;

	// ======================================================================
	// SYSTEM ID
	// ======================================================================
	/**
	 * SYSTEM ID です。
	 * <p>
	 */
	private final String systemId;

	// ======================================================================
	// XML バージョン
	// ======================================================================
	/**
	 * XML バージョンです。
	 * <p>
	 */
	private String xmlVersion = null;

	// ======================================================================
	// エンコーディング
	// ======================================================================
	/**
	 * エンコーディングです。
	 * <p>
	 */
	private String encoding = null;

	// ======================================================================
	// 行番号
	// ======================================================================
	/**
	 * 現在の読み出し位置の行番号です。
	 * <p>
	 */
	private int currentLine = 1;

	// ======================================================================
	// 文字番号
	// ======================================================================
	/**
	 * 現在の読み出し位置の行中の文字番号です。
	 * <p>
	 */
	private int currentColumn = 1;

	// ======================================================================
	// 行番号
	// ======================================================================
	/**
	 * 現在の読み出し位置の行番号です。
	 * <p>
	 */
	private int line = 1;

	// ======================================================================
	// 文字番号
	// ======================================================================
	/**
	 * 現在の読み出し位置の行中の文字番号です。
	 * <p>
	 */
	private int column = 1;

	// ======================================================================
	// テキストモード要素
	// ======================================================================
	/**
	 * テキストモードで読み込み中の場合の終了を示す要素名です。
	 * <p>
	 */
	private String textModeEnd = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param in 下層の入力ストリーム
	 * @param publicId PUBLIC ID
	 * @param systemId SYSTEM ID
	 */
	LocatorReader(Reader in, String publicId, String systemId) {
		super(new LFReader(in), 64);
		this.publicId = publicId;
		this.systemId = systemId;
		return;
	}

	// ======================================================================
	// 現在位置のマーク
	// ======================================================================
	/**
	 * 現在の読み出し位置をマークし {@link #getLineNumber()}, {@link #getColumnNumber()}
	 * で返すように設定します。
	 * <p>
	*/
	public void markLocation(){
		assert(currentLine > 0 && currentColumn > 0): "(" + currentLine + "," + currentColumn + ")";
		line = currentLine;
		column = currentColumn;
		return;
	}

	// ======================================================================
	// XML バージョンの参照
	// ======================================================================
	/**
	 * XML バージョンを参照します。
	 * <p>
	 * @return XML バージョン
	*/
	public String getXMLVersion() {
		return xmlVersion;
	}

	// ======================================================================
	// XML バージョンの設定
	// ======================================================================
	/**
	 * XML バージョンを設定します。
	 * <p>
	 * @param xmlVersion XML バージョン
	 */
	public void setXmlVersion(String xmlVersion) {
		this.xmlVersion = xmlVersion;
		return;
	}

	// ======================================================================
	// エンコーディングの参照
	// ======================================================================
	/**
	 * エンコーディングを参照します。
	 * <p>
	 * @return エンコーディング
	*/
	public String getEncoding() {
		return encoding;
	}

	// ======================================================================
	// エンコーディングの設定
	// ======================================================================
	/**
	 * エンコーディングを設定します。
	 * <p>
	 * @param encoding エンコーディング
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
		return;
	}

	// ======================================================================
	// PUBLIC ID の参照
	// ======================================================================
	/**
	 * このストリームの PUBLIC ID を参照します。
	 * <p>
	 * @return PUBLIC ID
	*/
	public String getPublicId() {
		return publicId;
	}

	// ======================================================================
	// SYSTEM ID の参照
	// ======================================================================
	/**
	 * このストリームの SYSTEM ID を参照します。
	 * <p>
	 * @return SYSTEM ID
	*/
	public String getSystemId() {
		return systemId;
	}

	// ======================================================================
	// 行番号の参照
	// ======================================================================
	/**
	 * 行番号を参照します。
	 * <p>
	 * @return 行番号
	*/
	public int getLineNumber() {
		return line;
	}

	// ======================================================================
	// 列番号の参照
	// ======================================================================
	/**
	 * 列番号を参照します。
	 * <p>
	 * @return 列番号
	*/
	public int getColumnNumber() {
		return column;
	}

	// ======================================================================
	// 文字の読み込み
	// ======================================================================
	/**
	 * 文字を読み込みます。
	 * <p>
	 * @return 文字
	 * @throws IOException 読み込みに失敗した場合
	*/
	@Override
	public int read() throws IOException {

		// 次の文字の読み込み
		int ch = super.read();
		if(ch < 0){
			return ch;
		}

		// ※LFReader を使用しているため CR は読み込まれない想定
		assert(ch != '\r');

		// LF を読み込んだら次の行へ移動
		if(ch == '\n'){
			currentLine ++;
			currentColumn = 1;
			return '\n';
		}

		// 次の列へ移動
		currentColumn ++;
		return ch;
	}

	// ======================================================================
	// 文字の読み込み
	// ======================================================================
	/**
	 * ストリームから指定されたバッファに読み込みます。
	 * <p>
	 * @param cbuf 読み込みバッファ
	 * @return 読み込んだ長さ
	 * @throws IOException 読み込みに失敗した場合
	*/
	@Override
	public int read(char[] cbuf) throws IOException {
		return this.read(cbuf, 0, cbuf.length);
	}

	// ======================================================================
	// 文字の読み込み
	// ======================================================================
	/**
	 * ストリームから指定されたバッファに読み込みます。
	 * <p>
	 * @param cbuf 読み込みバッファ
	 * @param off バッファのオフセット
	 * @param len 読み込む長さ
	 * @return 読み込んだ長さ
	 * @throws IOException 読み込みに失敗した場合
	*/
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int length = super.read(cbuf, off, len);
		for(int i=0; i<length; i++){
			if(cbuf[i] == '\n'){
				currentLine ++;
				currentColumn = 1;
			} else {
				currentColumn ++;
			}
		}
		return length;
	}

	// ======================================================================
	// 読み込みのキャンセル
	// ======================================================================
	/**
	 * 指定された文字の読み込みをキャンセルします。
	 * <p>
	 * @param c 戻す文字
	 * @throws IOException 戻しに失敗した場合
	*/
	@Override
	public void unread(int c) throws IOException {
		super.unread(c);
		if(c == '\n'){
			currentLine --;
			currentColumn = 1;
		} else if(currentColumn > 1){
			currentColumn --;
		}
		return;
	}

	// ======================================================================
	// 読み込みのキャンセル
	// ======================================================================
	/**
	 * 指定されたバッファの読み込みをキャンセルします。
	 * <p>
	 * @param cbuf バッファ
	 * @throws IOException
	*/
	@Override
	public void unread(char[] cbuf) throws IOException {
		this.unread(cbuf, 0, cbuf.length);
		return;
	}

	// ======================================================================
	// 読み込みのキャンセル
	// ======================================================================
	/**
	 * 指定されたバッファの読み込みをキャンセルします。
	 * <p>
	 * @param cbuf バッファ
	 * @param off 開始オフセット
	 * @param len 長さ
	 * @throws IOException
	*/
	@Override
	public void unread(char[] cbuf, int off, int len) throws IOException {
		for(int i=len-1; i>=0; i--){
			unread(cbuf[off + i]);
		}
		return;
	}

	// ======================================================================
	// EOF の判定
	// ======================================================================
	/**
	 * ストリームが EOF に達しているかどうかを判定します。
	 * <p>
	 * @return EOF に達している場合 true
	 * @throws IOException 読み込みに失敗した場合
	 */
	public boolean isEOF() throws IOException{
		int ch = read();
		if(ch < 0){
			return true;
		}
		unread(ch);
		return false;
	}

	// ======================================================================
	// ストリームの判定
	// ======================================================================
	/**
	 * 現在のストリームから読み出される次のデータの型を判定します。EOF に達している場合は null
	 * を返します。
	 * <p>
	 * @return 次のデータの型
	 * @throws IOException 読み込みに失敗した場合
	 */
	public Short getNextType() throws IOException{
		if(isEOF()){
			return null;
		}

		// テキスト
		if(! startsWith("<") || startsWith("<\uFFFF")){
			return Node.TEXT_NODE;
		}

		// コメント
		if(startsWith("<!--")){
			return Node.COMMENT_NODE;
		}

		// CDATA セクション
		if(startsWith("<![CDATA[", true)){
			return Node.CDATA_SECTION_NODE;
		}

		// テキストモードに設定されている場合は該当要素の終了までをテキストとして認識
		if(textModeEnd != null){
			if(! startsWith("</" + textModeEnd + ">", true)){
				return Node.TEXT_NODE;
			}
			textModeEnd = null;
		}

		// 処理命令
		if(startsWith("<?")){
			return Node.PROCESSING_INSTRUCTION_NODE;
		}

		// ドキュメント型
		if(startsWith("<!DOCTYPE\0", true)){
			return Node.DOCUMENT_TYPE_NODE;
		}

		// 要素
		return Node.ELEMENT_NODE;
	}

	// ======================================================================
	// テキストモードの設定
	// ======================================================================
	/**
	 * 現在のストリーム以降、指定された要素の終了を検知するまで {@link #getNextType()} で
	 * テキストとコメントのみを認識するよう設定します。このメソッドは HTML の &lt;script&gt;
	 * や &lt;style&gt; の内部を適切に読み込むために使用します。
	 * <p>
	 * null を指定するとテキストモードを終了します。
	 * <p>
	 * @param textModeEnd テキストモードを終了する要素の名前
	 */
	public void setTextModeEnd(String textModeEnd){
		this.textModeEnd = textModeEnd;
		return;
	}

	// ======================================================================
	// テキストモードの判定
	// ======================================================================
	/**
	 * 現在のストリームがテキストモードかどうかを判定します。
	 * <p>
	 * @return テキストモードの場合 true
	 */
	public boolean isTextMode(){
		return (textModeEnd != null);
	}

	// ======================================================================
	// ストリーム開始の判定
	// ======================================================================
	/**
	 * 現在のストリームの先頭が指定された文字シーケンスで開始しているかを判定します。
	 * <p>
	 * @param sequence 評価する文字シーケンス
	 * @return 開始している場合 true
	 * @throws IOException 読み込みに失敗した場合
	 */
	public boolean startsWith(String sequence) throws IOException{
		return startsWith(sequence, false);
	}

	// ======================================================================
	// ストリーム開始の判定
	// ======================================================================
	/**
	 * 現在のストリームの先頭が指定された文字シーケンスで開始しているかを判定します。
	 * <p>
	 * @param sequence 評価する文字シーケンス
	 * @param ignoreCase 大文字と小文字を無視する場合 true
	 * @return 開始している場合 true
	 * @throws IOException 読み込みに失敗した場合
	 */
	public boolean startsWith(String sequence, boolean ignoreCase) throws IOException{
		return Toolkit.streamStartsWith(this, sequence, ignoreCase);
	}

	// ======================================================================
	// 文字の読み飛ばし
	// ======================================================================
	/**
	 * ストリームから指定された文字シーケンスを読み飛ばします。
	 * <p>
	 * @param sequence 読み飛ばす文字シーケンス
	 * @param validate 読み飛ばす文字を検証する場合 true
	 * @return 実際に読み飛ばした文字列
	 * @throws IOException 読み込みに失敗した場合
	 */
	public String skipSequence(String sequence, boolean validate) throws IOException{
		return skipSequence(sequence, validate, false);
	}

	// ======================================================================
	// 文字の読み飛ばし
	// ======================================================================
	/**
	 * ストリームから指定された文字シーケンスを読み飛ばします。
	 * <p>
	 * @param sequence 読み飛ばす文字シーケンス
	 * @param validate 読み飛ばす文字を検証する場合 true
	 * @param ignoreCase 大文字と小文字を無視する場合 true
	 * @return 実際に読み飛ばした文字列
	 * @throws IOException 読み込みに失敗した場合
	 */
	public String skipSequence(String sequence, boolean validate, boolean ignoreCase) throws IOException{
		StringBuilder buffer = new StringBuilder();
		for(int i=0; i<sequence.length(); i++){
			int ch = read();
			assert(! validate || Toolkit.matches(sequence.charAt(i), ch, ignoreCase)): sequence + "[" + i + "] != " + (char)ch;
			buffer.append((char)ch);
		}
		return buffer.toString();
	}

	// ======================================================================
	// 空白文字の読み飛ばし
	// ======================================================================
	/**
	 * ストリーム中の空白文字を読み飛ばします。
	 * <p>
	 * @throws IOException 読み込みに失敗した場合
	 */
	public void skipWhitespace() throws IOException{
		while(true){
			int ch = read();
			if(ch < 0){
				break;
			}
			if(! Xml.isWhitespace(ch)){
				unread(ch);
				break;
			}
		}
		return;
	}

}
