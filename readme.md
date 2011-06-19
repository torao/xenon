# Introduction
Xenon is a library that provides the future to parse loose or imcomplete XML including HTML and
build DOM to your application. You can use almost in the same way with JAXP.

	String factoryName = "org.koiroha.xml.parser.HTMLDocumentBuilderFactory";
	DocumentBuilderFactory factory
	    = DocumentBuilderFactory.newInstance(factoryName, loader);
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document doc = builder.parse(htmlFile);

or more shortly

	DocumentBuilderFactory factory = new HTMLDocumentBuilderFactory();
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document doc = builder.parse(htmlFile);

This library includes type 2 SAX parser and level 3 DOM builder for loose XML and HTML. Of course
this recognize well-formed XML same as JAXP.

* Runtime Environment: Java2 SE 5.0 or later
* Additional Library: none
* Project Properties: Eclipse 3.6. standard file encoding is UTF-8, and line separator is LF.
* License: Apache License, Version 2.0

# Feature

* Build DOM from HTML or loose, imcomplete XML.
* Compatible to use for JAXP.
* Guess charset from &lt;?xml?&gt; or &lt;meta&gt; element in XML/HTML.
* Callback based parsing same as SAX (but it is faithfulness for loose structure, so begin-end
callback of element is asymmetric).

--- History ---
2010/02/25
 o Java2 SE 5.0 対応
 o Xerces 2.9 バグ回避対応

 o クォートで囲まれていない属性値の最後がスラッシュで終わっておりその直後に要素の終了が存在する場合について、空要素と
   して解釈されていたものを属性値として認識するように修正。具体的には <a href=/foo/bar/> のような記述に対して
   今までは <a href="/foo/bar"/> と解釈していたのを <a href="/foo/bar/"> と解釈するようになった。
   初期の HTML でクォートの省略が多く見られていた事と、その頃は空要素の記述に <a/> という書き方はしなかった事から、
   クォート省略 + "/>" が現れたらそのスラッシュは属性値の一部と見なすのがより正しいかと判断しています。

 o 属性値においてシングルクォートに囲まれた中のダブルクォート、ダブルクォートに囲まれた中のシングルクォートを属性値の
   一部として認識するよう修正。
