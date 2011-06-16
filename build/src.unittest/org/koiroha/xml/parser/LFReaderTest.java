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

import static org.junit.Assert.*;

import java.io.*;

import org.junit.Test;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LFReaderTest: LFReader テストケース
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * LFReader のテストケースです。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/09 Java2 SE 5.0
 */
public class LFReaderTest {

	// ======================================================================
	// テストの実行
	// ======================================================================
	/**
	 * ストリームから読み出される全ての CR, CRLF が LF に変換されていることを確認します。
	 * <p>
	 * @throws IOException テストに失敗した場合
	 */
	@Test
	public void test() throws IOException {
		String sample = "\r\n \n \r \r";
		String expected = "\n \n \n \n";

		// 1文字読み込みのテスト
		Reader r = new LFReader(new StringReader(sample));
		for(int i=0; i<expected.length(); i++){
			assertEquals((int)expected.charAt(i), r.read());
		}
		assertTrue(r.read() < 0);

		// バッファ読み込みのテスト
		r = new LFReader(new StringReader(sample));
		assertEquals(expected, read(r));

		// バッファ読み込みテスト
		r = new StringReader(sample){
			@Override
			public int read(char[] buf, int off, int len) throws IOException{
				int ch = super.read();
				if(ch < 0){
					return ch;
				}
				buf[off] = (char)ch;
				return 1;
			}
		};
		r = new LFReader(r);
		assertEquals(expected, read(r));

		return;
	}

	// ======================================================================
	// 文字列の読み込み
	// ======================================================================
	/**
	 * 指定されたストリームから文字列を読み込みます。
	 * <p>
	 * @param in 入力ストリーム
	 * @return ストリームから読み込んだ文字列
	 * @throws IOException 読み込みに失敗した場合
	 */
	private String read(Reader in) throws IOException{
		StringBuilder buffer = new StringBuilder();
		char[] buf = new char[1024];
		while(true){
			int len = in.read(buf);
			if(len < 0){
				break;
			}
			buffer.append(buf, 0, len);
		}
		return buffer.toString();
	}

}
