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
import java.util.Stack;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// XmlStreamWriter:
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 同名クラスの J2SE 5.0 版です。
 * <p>
 * @version $Revision: 1.3 $ $Date: 2010/02/24 14:16:33 $
 * @author torao
 * @since 2010/02/24 Java2 SE 5.0
 */
public class XMLStreamWriter {

	// ======================================================================
	// 出力ストリーム
	// ======================================================================
	/**
	 * 出力先のストリームです。
	 * <p>
	 */
	private final PrintWriter out;

	// ======================================================================
	// 要素スタック
	// ======================================================================
	/**
	 * 要素名のスタックです。
	 * <p>
	 */
	private final Stack<String> elem = new Stack<String>();

	// ======================================================================
	// 要素内出力中フラグ
	// ======================================================================
	/**
	 * 要素内を出力中かどうかを表すフラグです。
	 * <p>
	 */
	private int inelem = 0;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param out 出力先のストリーム
	 */
	public XMLStreamWriter(Writer out) {
		this.out = new PrintWriter(out);
		return;
	}

	// ======================================================================
	// ストリームのフラッシュ
	// ======================================================================
	/**
	 * ストリームをフラッシュします。
	 * <p>
	 */
	public void flush() {
		out.flush();
		return;
	}

	/** デフォルト名前空間の出力
	 * @param ns デフォルトの名前空過 */
	public void setDefaultNamespace(String ns){
		return;
	}

	/** コメントの出力
	 * @param comment コメント*/
	public void writeComment(String comment){
		ensureClose();
		out.write("<!--" + comment + "-->");
		return;
	}

	/** テキストの出力
	 * @param text テキスト
	 */
	public void writeCharacters(String text){
		ensureClose();
		out.write(escape(text));
		return;
	}

	/** テキストの出力
	 * @param text テキスト
	 */
	public void writeCData(String text){
		ensureClose();
		out.write("<![CDATA" + text + "]]>");
		return;
	}

	/** PIの出力
	 * @param name 名前
	 * @param target ターゲット
	 */
	public void writeProcessingInstruction(String name, String target){
		ensureClose();
		out.write("<?" + name + " " + target + "?>");
		return;
	}

	/** DTD の出力
	 * @param dtd DTD
	 */
	public void writeDTD(String dtd){
		ensureClose();
		out.write(dtd);
		return;
	}

	/** 要素の出力
	 * @param name 要素名
	 */
	public void writeEmptyElement(String name){
		ensureClose();
		out.write("<" + name);
		inelem = 2;
		return;
	}

	/** 要素の出力
	 * @param name 要素名
	 */
	public void writeStartElement(String name){
		ensureClose();
		out.write("<" + name);
		inelem = 1;
		elem.push(name);
		return;
	}

	/** 属性の出力
	 * @param name 属性名
	 * @param value 属性値
	 */
	public void writeAttribute(String name, String value){
		assert(inelem != 0);
		out.write(" " + name + "=\"" + escape(value) + "\"");
		return;
	}

	/** 要素終了の出力
	 */
	public void writeEndElement(){
		ensureClose();
		String name = elem.pop();
		out.write("</" + name + ">");
		return;
	}

	/** ドキュメント開始の出力
	 * @param cs 文字セット
	 * @param ver バージョン
	 */
	public void writeStartDocument(String cs, String ver){
		out.write("<?xml version=\"" + ver + "\" encoding=\"" + cs + "\"?>");
		return;
	}

	/** ドキュメント終了の出力
	 */
	public void writeEndDocument(){
		return;
	}

	/** ドキュメント終了の出力
	 */
	public void close(){
		out.close();
		return;
	}

	/** 要素クローズの保証
	 */
	private void ensureClose(){
		if(inelem == 1){
			out.write('>');
		} else if(inelem == 2){
			out.write("/>");
		}
		inelem = 0;
		return;
	}

	// ======================================================================
	// 文字列のエスケープ
	// ======================================================================
	/**
	 * 文字列をエスケープして返します。
	 * <p>
	 * @param text エスケープする文字列
	 * @return エスケープした文字列
	 */
	private static String escape(String text){
		StringBuilder buffer = new StringBuilder();
		for(int i=0; i<text.length(); i++){
			char ch = text.charAt(i);
			switch(ch){
			case '<':	buffer.append("&lt;");		break;
			case '>':	buffer.append("&gt;");		break;
			case '&':	buffer.append("&amp;");		break;
			case '\"':	buffer.append("&quot;");	break;
			default:
				if(Character.isDefined(ch)){
					buffer.append(ch);
				} else {
					buffer.append("&#" + ch + ";");
				}
			}
		}
		return buffer.toString();
	}

}
