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

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// SimpleTest: URL テスト
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 入力された URL を解析するテストを行います。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2010/02/24 Java2 SE 5.0
 */
public class SimpleTest {

	// ======================================================================
	//
	// ======================================================================
	/**
	 *
	 * <p>
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		HTMLDocumentBuilderFactory factory = new HTMLDocumentBuilderFactory();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while(true){
			System.out.print("URL> ");
			String line = in.readLine();
			if(line == null){
				break;
			}
			Document doc = factory.newDocumentBuilder().parse(line.trim());
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(System.out));
		}
		return;
	}

}
