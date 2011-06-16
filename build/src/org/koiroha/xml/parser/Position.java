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

import java.io.Serializable;

import org.w3c.dom.Node;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Position: 位置情報
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 解析位置情報を保持するためのクラスです。このパッケージのドキュメントビルダーファクトリを使用し
 * て構築した DOM の要素にその位置情報を付加するために使用されています。
 * <p>
 * <pre>Position pos = (Position)elem.{@link Node#getUserData(String) getUserData}(Position.USERDATA_NAME);
 * </pre>
 * <p>
 * @version $Revision: 1.4 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/08 Java2 SE 5.0
 */
public final class Position implements Comparable<Position>, Serializable {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// ユーザデータ名
	// ======================================================================
	/**
	 * {@link HTMLDocumentBuilderFactory} を使用して構築された DOM のノードに設定され
	 * ている位置情報 (ユーザデータ) を参照する名前です。定数 {@value} を示します。
	 * <p>
	 * @see HTMLDocumentBuilderFactory
	 * @see Node#getUserData(String)
	 */
	public static final String USERDATA_NAME = "org.koiroha.xml.parser.position";

	// ======================================================================
	// 行番号
	// ======================================================================
	/**
	 * 行番号です。
	 * <p>
	 */
	private final int lineNumber;

	// ======================================================================
	// 桁番号
	// ======================================================================
	/**
	 * 桁番号です。
	 * <p>
	 */
	private final int columnNumber;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定された行番号/桁番号の位置情報を構築します。双方のパラメータは 1 以上である必要があり
	 * ます。
	 * <p>
	 * @param line 行番号
	 * @param col 桁番号
	 */
	public Position(int line, int col) {
		assert(line > 0 && col > 0): "invalid position(" + line + "," + col + ")";
		this.lineNumber = line;
		this.columnNumber = col;
		return;
	}

	// ======================================================================
	// 行番号の参照
	// ======================================================================
	/**
	 * 行番号を参照します。先頭の行に対して 1 が返されます。
	 * <p>
	 * @return 行番号
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	// ======================================================================
	// 桁番号の参照
	// ======================================================================
	/**
	 * 桁番号を参照します。行の最初の文字に対して 1 が返されます。
	 * <p>
	 * @return 桁番号
	 */
	public int getColumnNumber() {
		return columnNumber;
	}

	// ======================================================================
	// インスタンスの比較
	// ======================================================================
	/**
	 * 指定されたインスタンスとこのインスタンスを比較します。
	 * <p>
	 * @param other 比較するインスタンス
	 * @return このインスタンスが大きい場合正の値、小さい場合負の値、等しい場合 0
	*/
	public int compareTo(Position other) {
		if(this.getLineNumber() > other.getLineNumber()){
			return 1;
		}
		if(this.getLineNumber() < other.getLineNumber()){
			return -1;
		}
		if(this.getColumnNumber() > other.getColumnNumber()){
			return 1;
		}
		if(this.getColumnNumber() < other.getColumnNumber()){
			return -1;
		}
		return 0;
	}

	// ======================================================================
	// ハッシュ値の参照
	// ======================================================================
	/**
	 * ハッシュ値を参照します。
	 * <p>
	 * @return ハッシュ値
	*/
	@Override
	public int hashCode(){
		return (getLineNumber() << 16) | getColumnNumber();
	}

	// ======================================================================
	// 等価性の評価
	// ======================================================================
	/**
	 * 指定されたインスタンスとこのインスタンスの内容が等しいかどうかを判定します。
	 * <p>
	 * @param o 比較するオブジェクト
	 * @return 等しい場合 true
	*/
	@Override
	public boolean equals(Object o){
		if(! (o instanceof Position)){
			return false;
		}
		Position other = (Position)o;
		return (this.getLineNumber() == other.getLineNumber())
			&& (this.getColumnNumber() == other.getColumnNumber());
	}

	// ======================================================================
	// インスタンスの文字列化
	// ======================================================================
	/**
	 * このインスタンスを文字列化します。
	 * <p>
	 * @return インスタンスの文字列
	*/
	@Override
	public String toString() {
		return "(" + getLineNumber() + "," + getColumnNumber() + ")";
	}

}
