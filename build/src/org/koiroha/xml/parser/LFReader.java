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

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LFReader: LF 入力ストリーム
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 改行を LF に統一するためのストリームです。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/09 Java2 SE 5.0
 */
class LFReader extends FilterReader {

	// ======================================================================
	// LF スキップフラグ
	// ======================================================================
	/**
	 * 次に読み込んだ文字が LF だった場合に読み飛ばすフラグです。
	 * <p>
	 */
	private boolean skipLF = false;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 復帰コードを取り除くストリームを指定して構築を行います。
	 * <p>
	 * @param in 仮装の入力ストリーム
	 */
	public LFReader(Reader in) {
		super(in);
		return;
	}

	// ======================================================================
	// 文字の読み込み
	// ======================================================================
	/**
	 * 次の文字を読み込みます。
	 * <p>
	 * @return 読み込んだ文字
	 * @throws IOException 読み込みに失敗した場合
	*/
	@Override
	public int read() throws IOException {
		int ch = super.read();
		if(ch == '\n' && skipLF){
			ch = super.read();
			skipLF = false;
		}
		if(ch == '\r'){
			ch = '\n';
			skipLF = true;
		} else {
			skipLF = false;
		}
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
	 * 次の文字を読み込みます。
	 * <p>
	 * @param cbuf バッファ
	 * @param off バッファの開始位置
	 * @param len 読み込む長さ
	 * @return 読み込んだ長さ
	 * @throws IOException 読み込みに失敗した場合
	*/
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int dst = 0;
		do {

			// バッファへ文字列の読み込み
			int length = super.read(cbuf, off, len);
			if(length < 0){
				return length;
			}

			int src = 0;
			dst = 0;

			// 直前に CR を読み込んでいて今回の最初に LF を検出した場合はその改行を潰す
			if(skipLF && cbuf[off] == '\n'){
				src ++;
			}
			skipLF = false;

			// バッファ内の CR, CRLF を LF に変換
			for(/* */; src+1<length; src++){
				if(cbuf[off+src] != '\r'){
					cbuf[off+dst] = cbuf[off+src];
					dst ++;
				} else if(cbuf[off+src+1] == '\n'){
					/* */
				} else {
					cbuf[off+dst] = '\n';
					dst ++;
				}
			}

			// バッファ内の最後に CR が存在する場合は LF に変換して次回LF無視フラグを立てる
			if(src < length){
				char last = cbuf[off + length - 1];
				if(last == '\r'){
					last = '\n';
					skipLF = true;
				}
				cbuf[off + dst] = last;
				dst ++;
			}

		} while(dst == 0);
		return dst;
	}

}
