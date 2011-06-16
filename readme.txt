Propject Property Information

default file encoding  : UTF-8
default line separator : LF ('\n')
target java environment: Java2 SE 5.0
additional libraries   : -
project id             : xmllib


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
