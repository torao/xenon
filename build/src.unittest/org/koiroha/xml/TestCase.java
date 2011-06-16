/* **************************************************************************
 * Copyright (C) 2011 BJoRFUAN. All Rights Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * Apache License Ver. 2.0, and comes with NO WARRANTY.
 *
 *                                           takami torao <koiroha@gmail.com>
 *                                                   http://www.bjorfuan.com/
 */
package org.koiroha.xml;

import static org.junit.Assert.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.Date;
import java.util.logging.*;

import org.junit.BeforeClass;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// TestCase: テストケースクラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * テストケース用ののスーパークラスです。
 * <p>
 * @version $Revision: 1.3 $ $Date: 2010/07/18 15:30:18 $
 * @author torao
 * @since 2009/04/09 Java2 SE 5.0
 */
public class TestCase {

	// ======================================================================
	// ログ出力の初期化
	// ======================================================================
	/**
	 * ログ出力を初期化します。
	 * <p>
	 */
	@BeforeClass
	public static void initLogging(){

		// フォーマッターの構築
		Formatter formatter = new Formatter(){
			@Override
			public String format(LogRecord r) {
				return String.format("[%1$tY-%1$tm-%1$td %1$tT] %2$s - %3$s%n",
					new Date(r.getMillis()), r.getLevel(), r.getMessage());
			}
		};

		// ログ出力先から全てのハンドラを取り外し
		Logger logger = Logger.getLogger("org.koiroha.xml");
		for(Handler h: logger.getHandlers()){
			logger.removeHandler(h);
		}

		// ハンドラとレベルを再設定
		Handler handler = new ConsoleHandler();
		logger.addHandler(handler);
		handler.setLevel(Level.ALL);
		logger.setLevel(Level.ALL);
		handler.setFormatter(formatter);
		return;
	}

	// ======================================================================
	// ユーティリティクラスのテスト
	// ======================================================================
	/**
	 * 指定されたクラスがユーティリティクラスとして定義されていることをテストします。具体的には
	 * リフレクションを使用して以下を確認します。
	 * <p>
	 * <ul>
	 * <li> クラスが final 宣言されている。</li>
	 * <li> private 宣言されているデフォルトコンストラクタのみが定義されている。</li>
	 * <li> 全ての public なメソッドが static 宣言されている。</li>
	 * </ul>
	 * <p>
	 * また全パス確認のためにデフォルトコンストラクタを起動します。
	 * <p>
	 * @param clazz 検証するクラス
	 */
	public static void verifyUtilityClass(Class<?> clazz){

		// クラスが final 宣言されていることを確認
		assertTrue("ユーティリティクラスが final 宣言されていません", Modifier.isFinal(clazz.getModifiers()));

		// private 宣言されたデフォルトコンストラクタのみが定義されていることを確認
		assertEquals("コンストラクタが複数定義されています", 1, clazz.getDeclaredConstructors().length);
		try{

			// デフォルトコンストラクタが private 宣言されていることを確認
			Constructor<?> c = clazz.getDeclaredConstructor();
			assertTrue("デフォルトコンストラクタが private 宣言されていません", Modifier.isPrivate(c.getModifiers()));

			// 全パス確認用にインスタンスを生成する
			c.setAccessible(true);
			c.newInstance();

		} catch(NoSuchMethodException ex){
			fail("デフォルトコンストラクタが定義されていません");
		} catch(Exception ex){
			fail("コンストラクタの呼び出しで例外が発生しました");
		}

		// アクセス可能な全てのメソッドが public static 宣言されていることを確認
		Method[] m = clazz.getDeclaredMethods();
		for(int i=0; i<m.length; i++){
			assertTrue("メソッドが static 宣言されていません: " + m[i].toGenericString(),
					Modifier.isStatic(m[i].getModifiers()));
		}
		return;
	}

	// ======================================================================
	// シリアライズのテスト
	// ======================================================================
	/**
	 * 指定されたインスタンスがシリアライズ可能かどうかを確認します。
	 * <p>
	 * @param obj 検証するオブジェクト
	 */
	public static void verifySerializable(Object obj){
		try{

			// シリアライズの実行
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(obj);
			out.flush();
			byte[] binary = baos.toByteArray();

			// 復元の実行
			ByteArrayInputStream bais = new ByteArrayInputStream(binary);
			ObjectInputStream in = new ObjectInputStream(bais);
			in.readObject();

		} catch(Exception ex){
			fail(ex.toString());
		}
		return;
	}

}
