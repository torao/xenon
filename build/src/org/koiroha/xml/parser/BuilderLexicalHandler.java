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

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// BuilderLexicalHandler: ビルダー構文ハンドラ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * SAX パーサからのコールバックを受けるための {@link LexicalHandler} 拡張インターフェース
 * です。DOM を構築するための追加通知を定義します。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/01 Java2 SE 5.0
 */
interface BuilderLexicalHandler extends LexicalHandler {

	// ======================================================================
	// コメントの開始
	// ======================================================================
	/**
	 * コメントが開始する時に呼び出されます。
	 * <p>
	 * @throws SAXException 呼び出し側により処理を中断する場合
	*/
	public void startComment() throws SAXException;

	// ======================================================================
	// コメントの終了
	// ======================================================================
	/**
	 * コメントが終了する時に呼び出されます。
	 * <p>
	 * @throws SAXException 呼び出し側により処理を中断する場合
	*/
	public void endComment() throws SAXException;

}
