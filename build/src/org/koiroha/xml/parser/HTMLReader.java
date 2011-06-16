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

import java.util.Map;


// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// HTMLReader: HTML パーサ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * HTML を解析するためのクラスです。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/03/31 Java2 SE 5.0
 */
class HTMLReader extends LooseXMLReader {

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 機能とプロパティを指定して構築を行います。
	 * <p>
	 * @param feature 機能フラグ
	 * @param property プロパティ
	 */
	public HTMLReader(Map<String,Boolean> feature, Map<String,Object> property) {
		super(feature, property);
		initEntityReference();
		return;
	}

	// ======================================================================
	// 他言語要素の判定
	// ======================================================================
	/**
	 * 指定された要素が他言語の要素かどうかを判定します。多言語が記述される要素の中は全てテキス
	 * ト要素として認識されます。
	 * <p>
	 * @param uri 名前空間URI
	 * @param localName ローカル名
	 * @param qName 修飾名
	 * @return 多言語要素の場合 true
	 */
	@Override
	protected boolean isNotXmlSemantics(String uri, String localName, String qName){
		return localName.equalsIgnoreCase("script") || localName.equalsIgnoreCase("style");
	}

	// ======================================================================
	// HTML 実体参照の設定
	// ======================================================================
	/**
	 * HTML で定義されている実体参照を設定します。
	 * <p>
	 */
	private void initEntityReference(){
		removeEntityReference("apos");
		setEntityReference("quot",	"\"");
		setEntityReference("amp",	"&");
		setEntityReference("lt",	"<");
		setEntityReference("gt",	">");
		setEntityReference("nbsp",	"\u00A0");
		setEntityReference("iexcl",	"\u00A1");
		setEntityReference("cent",	"\u00A2");
		setEntityReference("pound",	"\u00A3");
		setEntityReference("curren",	"\u00A4");
		setEntityReference("yen",	"\u00A5");
		setEntityReference("brvbar",	"\u00A6");
		setEntityReference("sect",	"\u00A7");
		setEntityReference("uml",	"\u00A8");
		setEntityReference("copy",	"\u00A9");
		setEntityReference("ordf",	"\u00AA");
		setEntityReference("laquo",	"\u00AB");
		setEntityReference("not",	"\u00AC");
		setEntityReference("shy",	"\u00AD");
		setEntityReference("reg",	"\u00AE");
		setEntityReference("macr",	"\u00AF");
		setEntityReference("deg",	"\u00B0");
		setEntityReference("plusmn",	"\u00B1");
		setEntityReference("sup2",	"\u00B2");
		setEntityReference("sup3",	"\u00B3");
		setEntityReference("acute",	"\u00B4");
		setEntityReference("micro",	"\u00B5");
		setEntityReference("para",	"\u00B6");
		setEntityReference("middot",	"\u00B7");
		setEntityReference("cedil",	"\u00B8");
		setEntityReference("sup1",	"\u00B9");
		setEntityReference("ordm",	"\u00BA");
		setEntityReference("raquo",	"\u00BB");
		setEntityReference("frac14",	"\u00BC");
		setEntityReference("frac12",	"\u00BD");
		setEntityReference("frac34",	"\u00BE");
		setEntityReference("iquest",	"\u00BF");
		setEntityReference("Agrave",	"\u00C0");
		setEntityReference("Aacute",	"\u00C1");
		setEntityReference("Acirc",	"\u00C2");
		setEntityReference("Atilde",	"\u00C3");
		setEntityReference("Auml",	"\u00C4");
		setEntityReference("Aring",	"\u00C5");
		setEntityReference("AElig",	"\u00C6");
		setEntityReference("Ccedil",	"\u00C7");
		setEntityReference("Egrave",	"\u00C8");
		setEntityReference("Eacute",	"\u00C9");
		setEntityReference("Ecirc",	"\u00CA");
		setEntityReference("Euml",	"\u00CB");
		setEntityReference("Igrave",	"\u00CC");
		setEntityReference("Iacute",	"\u00CD");
		setEntityReference("Icirc",	"\u00CE");
		setEntityReference("Iuml",	"\u00CF");
		setEntityReference("ETH",	"\u00D0");
		setEntityReference("Ntilde",	"\u00D1");
		setEntityReference("Ograve",	"\u00D2");
		setEntityReference("Oacute",	"\u00D3");
		setEntityReference("Ocirc",	"\u00D4");
		setEntityReference("Otilde",	"\u00D5");
		setEntityReference("Ouml",	"\u00D6");
		setEntityReference("times",	"\u00D7");
		setEntityReference("Oslash",	"\u00D8");
		setEntityReference("Ugrave",	"\u00D9");
		setEntityReference("Uacute",	"\u00DA");
		setEntityReference("Ucirc",	"\u00DB");
		setEntityReference("Uuml",	"\u00DC");
		setEntityReference("Yacute",	"\u00DD");
		setEntityReference("THORN",	"\u00DE");
		setEntityReference("szlig",	"\u00DF");
		setEntityReference("agrave",	"\u00E0");
		setEntityReference("aacute",	"\u00E1");
		setEntityReference("acirc",	"\u00E2");
		setEntityReference("atilde",	"\u00E3");
		setEntityReference("auml",	"\u00E4");
		setEntityReference("aring",	"\u00E5");
		setEntityReference("aelig",	"\u00E6");
		setEntityReference("ccedil",	"\u00E7");
		setEntityReference("egrave",	"\u00E8");
		setEntityReference("eacute",	"\u00E9");
		setEntityReference("ecirc",	"\u00EA");
		setEntityReference("euml",	"\u00EB");
		setEntityReference("igrave",	"\u00EC");
		setEntityReference("iacute",	"\u00ED");
		setEntityReference("icirc",	"\u00EE");
		setEntityReference("iuml",	"\u00EF");
		setEntityReference("eth",	"\u00F0");
		setEntityReference("ntilde",	"\u00F1");
		setEntityReference("ograve",	"\u00F2");
		setEntityReference("oacute",	"\u00F3");
		setEntityReference("ocirc",	"\u00F4");
		setEntityReference("otilde",	"\u00F5");
		setEntityReference("ouml",	"\u00F6");
		setEntityReference("divide",	"\u00F7");
		setEntityReference("oslash",	"\u00F8");
		setEntityReference("ugrave",	"\u00F9");
		setEntityReference("uacute",	"\u00FA");
		setEntityReference("ucirc",	"\u00FB");
		setEntityReference("uuml",	"\u00FC");
		setEntityReference("yacute",	"\u00FD");
		setEntityReference("thorn",	"\u00FE");
		setEntityReference("yuml",	"\u00FF");
		setEntityReference("fnof",	"\u0192");
		setEntityReference("Alpha",	"\u0391");
		setEntityReference("Beta",	"\u0392");
		setEntityReference("Gamma",	"\u0393");
		setEntityReference("Delta",	"\u0394");
		setEntityReference("Epsilon",	"\u0395");
		setEntityReference("Zeta",	"\u0396");
		setEntityReference("Eta",	"\u0397");
		setEntityReference("Theta",	"\u0398");
		setEntityReference("Iota",	"\u0399");
		setEntityReference("Kappa",	"\u039A");
		setEntityReference("Lambda",	"\u039B");
		setEntityReference("Mu",	"\u039C");
		setEntityReference("Nu",	"\u039D");
		setEntityReference("Xi",	"\u039E");
		setEntityReference("Omicron",	"\u039F");
		setEntityReference("Pi",	"\u03A0");
		setEntityReference("Rho",	"\u03A1");
		setEntityReference("Sigma",	"\u03A3");
		setEntityReference("Tau",	"\u03A4");
		setEntityReference("Upsilon",	"\u03A5");
		setEntityReference("Phi",	"\u03A6");
		setEntityReference("Chi",	"\u03A7");
		setEntityReference("Psi",	"\u03A8");
		setEntityReference("Omega",	"\u03A9");
		setEntityReference("alpha",	"\u03B1");
		setEntityReference("beta",	"\u03B2");
		setEntityReference("gamma",	"\u03B3");
		setEntityReference("delta",	"\u03B4");
		setEntityReference("epsilon",	"\u03B5");
		setEntityReference("zeta",	"\u03B6");
		setEntityReference("eta",	"\u03B7");
		setEntityReference("theta",	"\u03B8");
		setEntityReference("iota",	"\u03B9");
		setEntityReference("kappa",	"\u03BA");
		setEntityReference("lambda",	"\u03BB");
		setEntityReference("mu",	"\u03BC");
		setEntityReference("nu",	"\u03BD");
		setEntityReference("xi",	"\u03BE");
		setEntityReference("omicron",	"\u03BF");
		setEntityReference("pi",	"\u03C0");
		setEntityReference("rho",	"\u03C1");
		setEntityReference("sigmaf",	"\u03C2");
		setEntityReference("sigma",	"\u03C3");
		setEntityReference("tau",	"\u03C4");
		setEntityReference("upsilon",	"\u03C5");
		setEntityReference("phi",	"\u03C6");
		setEntityReference("chi",	"\u03C7");
		setEntityReference("psi",	"\u03C8");
		setEntityReference("omega",	"\u03C9");
		setEntityReference("thetasym",	"\u03D1");
		setEntityReference("upsih",	"\u03D2");
		setEntityReference("piv",	"\u03D6");
		setEntityReference("bull",	"\u2022");
		setEntityReference("hellip",	"\u2026");
		setEntityReference("prime",	"\u2032");
		setEntityReference("Prime",	"\u2033");
		setEntityReference("oline",	"\u203E");
		setEntityReference("frasl",	"\u2044");
		setEntityReference("weierp",	"\u2118");
		setEntityReference("image",	"\u2111");
		setEntityReference("real",	"\u211C");
		setEntityReference("trade",	"\u2122");
		setEntityReference("alefsym",	"\u2135");
		setEntityReference("larr",	"\u2190");
		setEntityReference("uarr",	"\u2191");
		setEntityReference("rarr",	"\u2192");
		setEntityReference("darr",	"\u2193");
		setEntityReference("harr",	"\u2194");
		setEntityReference("crarr",	"\u21B5");
		setEntityReference("lArr",	"\u21D0");
		setEntityReference("uArr",	"\u21D1");
		setEntityReference("rArr",	"\u21D2");
		setEntityReference("dArr",	"\u21D3");
		setEntityReference("hArr",	"\u21D4");
		setEntityReference("forall",	"\u2200");
		setEntityReference("part",	"\u2202");
		setEntityReference("exist",	"\u2203");
		setEntityReference("empty",	"\u2205");
		setEntityReference("nabla",	"\u2207");
		setEntityReference("isin",	"\u2208");
		setEntityReference("notin",	"\u2209");
		setEntityReference("ni",	"\u220B");
		setEntityReference("prod",	"\u220F");
		setEntityReference("sum",	"\u2211");
		setEntityReference("minus",	"\u2212");
		setEntityReference("lowast",	"\u2217");
		setEntityReference("radic",	"\u221A");
		setEntityReference("prop",	"\u221D");
		setEntityReference("infin",	"\u221E");
		setEntityReference("ang",	"\u2220");
		setEntityReference("and",	"\u2227");
		setEntityReference("or",	"\u2228");
		setEntityReference("cap",	"\u2229");
		setEntityReference("cup",	"\u222A");
		setEntityReference("int",	"\u222B");
		setEntityReference("there4",	"\u2234");
		setEntityReference("sim",	"\u223C");
		setEntityReference("cong",	"\u2245");
		setEntityReference("asymp",	"\u2248");
		setEntityReference("ne",	"\u2260");
		setEntityReference("equiv",	"\u2261");
		setEntityReference("le",	"\u2264");
		setEntityReference("ge",	"\u2265");
		setEntityReference("sub",	"\u2282");
		setEntityReference("sup",	"\u2283");
		setEntityReference("nsub",	"\u2284");
		setEntityReference("sube",	"\u2286");
		setEntityReference("supe",	"\u2287");
		setEntityReference("oplus",	"\u2295");
		setEntityReference("otimes",	"\u2297");
		setEntityReference("perp",	"\u22A5");
		setEntityReference("sdot",	"\u22C5");
		setEntityReference("lceil",	"\u2308");
		setEntityReference("rceil",	"\u2309");
		setEntityReference("lfloor",	"\u230A");
		setEntityReference("rfloor",	"\u230B");
		setEntityReference("lang",	"\u2329");
		setEntityReference("rang",	"\u232A");
		setEntityReference("loz",	"\u25CA");
		setEntityReference("spades",	"\u2660");
		setEntityReference("clubs",	"\u2663");
		setEntityReference("hearts",	"\u2665");
		setEntityReference("diams",	"\u2666");
		setEntityReference("OElig",	"\u0152");
		setEntityReference("oelig",	"\u0153");
		setEntityReference("Scaron",	"\u0160");
		setEntityReference("scaron",	"\u0161");
		setEntityReference("Yuml",	"\u0178");
		setEntityReference("circ",	"\u02C6");
		setEntityReference("tilde",	"\u02DC");
		setEntityReference("ensp",	"\u2002");
		setEntityReference("emsp",	"\u2003");
		setEntityReference("thinsp",	"\u2009");
		setEntityReference("zwnj",	"\u200C");
		setEntityReference("zwj",	"\u200D");
		setEntityReference("lrm",	"\u200E");
		setEntityReference("rlm",	"\u200F");
		setEntityReference("ndash",	"\u2013");
		setEntityReference("mdash",	"\u2014");
		setEntityReference("lsquo",	"\u2018");
		setEntityReference("rsquo",	"\u2019");
		setEntityReference("sbquo",	"\u201A");
		setEntityReference("ldquo",	"\u201C");
		setEntityReference("rdquo",	"\u201D");
		setEntityReference("bdquo",	"\u201E");
		setEntityReference("dagger",	"\u2020");
		setEntityReference("Dagger",	"\u2021");
		setEntityReference("permil",	"\u2030");
		setEntityReference("lsaquo",	"\u2039");
		setEntityReference("rsaquo",	"\u203A");
		setEntityReference("euro",	"\u20AC");
		return;
	}

}
