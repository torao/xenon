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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.util.logging.Formatter;
import java.util.prefs.*;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// WebCrawler: Web クローラー
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Web を巡回してリンクをたどる GUI アプリケーションです。HTML パーサの検証ツールとして使用し
 * ます。
 * <p>
 * @version $Revision: 1.4 $ $Date: 2010/02/24 13:59:38 $
 * @author torao
 * @since 2009/04/05 Java2 SE 5.0
 */
public class WebCrawler extends JFrame {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(WebCrawler.class.getName());

	// ======================================================================
	// アプリケーション設定
	// ======================================================================
	/**
	 * このアプリケーションの設定です。
	 * <p>
	 */
	private final static Preferences pref = Preferences.userNodeForPackage(WebCrawler.class);

	// ======================================================================
	// 開始 URL
	// ======================================================================
	/**
	 * 巡回の開始 URL です。
	 * <p>
	 */
	private final JTextField startURL = new JTextField();

	// ======================================================================
	// 対象 URL
	// ======================================================================
	/**
	 * 対象 URL のパターンです。
	 * <p>
	 */
	private final JTextField urlPattern = new JTextField();

	// ======================================================================
	// 確認済み URL テーブル
	// ======================================================================
	/**
	 * 確認済みの URL テーブルです。
	 * <p>
	 */
	private final JTable parsedPageTable = new JTable();

	// ======================================================================
	// 確認済み URL テーブル
	// ======================================================================
	/**
	 * 確認済みの URL テーブルです。
	 * <p>
	 */
	private final WebCrawlerModel model;

	// ======================================================================
	// 警告
	// ======================================================================
	/**
	 * 警告です。
	 * <p>
	 */
	private final JList warning = new JList();

	// ======================================================================
	//
	// ======================================================================
	/**
	 * <p>
	 */
	private final JTextPane before = new JTextPane();

	// ======================================================================
	//
	// ======================================================================
	/**
	 * <p>
	 */
	private final JTextPane after = new JTextPane();

	// ======================================================================
	//
	// ======================================================================
	/**
	 * <p>
	 */
	private final JScrollPane[] scroll = new JScrollPane[2];

	// ======================================================================
	//
	// ======================================================================
	/**
	 * <p>
	 */
	private final JScrollBar verScroll = new JScrollBar();

	// ======================================================================
	// キュー数
	// ======================================================================
	/**
	 * <p>
	 */
	private final JLabel queueSize = new JLabel();

	// ======================================================================
	// 解析済みページ数
	// ======================================================================
	/**
	 * <p>
	 */
	private final JLabel parsedURLCount = new JLabel();

	// ======================================================================
	// 解析エラーページ数
	// ======================================================================
	/**
	 * <p>
	 */
	private final JLabel errorURLCount = new JLabel();

	// ======================================================================
	// 解析中ページ
	// ======================================================================
	/**
	 * <p>
	 */
	private final JLabel parsingURL = new JLabel();

	// ======================================================================
	// 実行ボタン
	// ======================================================================
	/**
	 * 実行ボタンです。
	 * <p>
	 */
	private final JButton exec = new JButton("開始");

	/** キューのクリアボタン **/
	private final JButton clear = new JButton("キュークリア");

	/** 再実行ボタン **/
	private final JButton retry = new JButton("再実行");

	// ======================================================================
	// 実行スレッド
	// ======================================================================
	/**
	 * 実行スレッドです。
	 * <p>
	 */
	private Thread executor = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * データベースディレクトリを指定して構築を行います。
	 * <p>
	 * @param dir データベースディレクトリ
	 * @throws SQLException データベースへの接続に失敗した場合
	 */
	public WebCrawler(File dir) throws SQLException{
		this.model = new WebCrawlerModel(dir);
		this.setTitle("WebCrawler - " + dir);

		// 開始 URL 入力と開始ボタン
		JPanel toolbar = new JPanel();
		toolbar.setLayout(new GridBagLayout());
		JLabel label = new JLabel("開始URL");
		startURL.setText(pref.get("url", "http://www.yahoo.co.jp"));
		layout(toolbar, label, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0, new Insets(0, 5, 0, 0));
		layout(toolbar, startURL, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0, 0, 0, null);
		layout(toolbar, exec, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0, null);
		layout(toolbar, new JSeparator(SwingConstants.VERTICAL), 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0, null);
		layout(toolbar, retry, 4, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0, null);
		layout(toolbar, clear, 5, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0, null);
		label = new JLabel("URLパターン");
		urlPattern.setText(pref.get("pattern", "http://*"));
		layout(toolbar, label, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0, new Insets(0, 5, 0, 0));
		layout(toolbar, urlPattern, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0, 0, 0, null);
		exec.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				execute();
				return;
			}
		});
		clear.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				model.clearQueue();
				queueSize.setText("0");
				return;
			}
		});
		retry.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int i = parsedPageTable.getSelectedRow();
				if(i < 0){
					return;
				}
				URL url = model.remove(i);
				try {
					execute(url, getPattern());
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				parsedPageTable.getSelectionModel().setSelectionInterval(i, i);
				return;
			}
		});

		parsedPageTable.setModel(model);
		parsedPageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		parsedPageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		parsedPageTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		parsedPageTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		parsedPageTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		parsedPageTable.getColumnModel().getColumn(3).setPreferredWidth(90);
		parsedPageTable.getColumnModel().getColumn(4).setPreferredWidth(60);
		parsedPageTable.getColumnModel().getColumn(5).setPreferredWidth(60);
		parsedPageTable.getColumnModel().getColumn(6).setPreferredWidth(60);
		parsedPageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			/** @param e */
			public void valueChanged(ListSelectionEvent e) {
				int i = parsedPageTable.getSelectedRow();
				if(! model.setSelectedPage(i)){
					return;
				}
				before.setCaretPosition(0);
				after.setCaretPosition(0);
				warning.clearSelection();
				return;
			}
		});

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		before.setDocument(model.content);
		before.setEditable(false);
		scroll[0] = new JScrollPane(before);
		scroll[0].setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scroll[0].setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		layout(panel, scroll[0], 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1, 0, 0, null);
		before.addCaretListener(new CaretListener(){
			public void caretUpdate(CaretEvent e) {
				int pos = Math.min(e.getMark(), e.getDot());
				int len = (Math.max(e.getMark(), e.getDot()) - pos + 1);
				Position[] p = model.getContentPosition(pos, len);
				if(p != null){
					parsingURL.setText(p[0] + ((len==1)? "": ("-" + p[1])));
				}
				return;
			}
		});

		after.setDocument(model.parsedContent);
		after.setEditable(false);
		scroll[1] = new JScrollPane(after);
		scroll[1].setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scroll[1].setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		layout(panel, scroll[1], 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1, 0, 0, null);

		layout(panel, verScroll, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, 0, 1, 0, 0, null);
		verScroll.addAdjustmentListener(new AdjustmentListener(){
			public void adjustmentValueChanged(AdjustmentEvent e) {
				int value = verScroll.getValue();
				scroll[0].getVerticalScrollBar().setValue(value);
				scroll[1].getVerticalScrollBar().setValue(value);
			}
		});

		ComponentListener cl = new ComponentAdapter(){
			@Override
			public void componentResized(ComponentEvent e) {
				JScrollBar s1 = scroll[0].getVerticalScrollBar();
				JScrollBar s2 = scroll[1].getVerticalScrollBar();
				verScroll.setValue(0);
				verScroll.setMaximum(Math.max(s1.getMaximum(), s2.getMaximum()));
				verScroll.setUnitIncrement(s1.getUnitIncrement());
				verScroll.setVisibleAmount(s1.getVisibleAmount());
				verScroll.setBlockIncrement(s1.getBlockIncrement());
			}
		};
		MouseWheelListener mwl = new MouseWheelListener(){
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL){
					verScroll.setValue(verScroll.getValue() + e.getUnitsToScroll());
				}
				return;
			}
		};
		before.addMouseWheelListener(mwl);
		before.addComponentListener(cl);
		after.addMouseWheelListener(mwl);
		after.addComponentListener(cl);

		warning.setModel(model.warning);
		warning.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				int i = warning.getSelectedIndex();
				if(i < 0){
					return;
				}
				int[] p = model.hilightWarning(i);
				if(p != null){
					before.select(p[0], p[0] + p[1]);
					before.requestFocus();
					verScroll.setValue(scroll[0].getVerticalScrollBar().getValue());
				} else {
					parsingURL.setText("この警告が示す位置は内容の範囲外です");
				}
				return;
			}
		});

		JSplitPane split1 = new JSplitPane();
		split1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		split1.setTopComponent(new JScrollPane(parsedPageTable));
		split1.setBottomComponent(panel);
		split1.setDividerLocation(100);
		JSplitPane split2 = new JSplitPane();
		split2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		split2.setTopComponent(split1);
		split2.setBottomComponent(new JScrollPane(warning));
		split2.setBorder(BorderFactory.createEmptyBorder());
		split2.setResizeWeight(1.0);
		split2.setDividerLocation(300);

		JPanel status = new JPanel();
		status.setLayout(new GridBagLayout());
		layout(status, parsingURL, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0, 0, 0, null);

		// 解析待ち URL 件数
		NumberFormat nf = NumberFormat.getNumberInstance();
		queueSize.setText(nf.format(model.db.getQueuePageCount()));
		queueSize.setMinimumSize(new Dimension(60, queueSize.getMinimumSize().height));
		queueSize.setPreferredSize(new Dimension(60, queueSize.getPreferredSize().height));
		queueSize.setHorizontalAlignment(SwingConstants.RIGHT);
		layout(status, new JLabel("queue="), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 5, 0, null);
		layout(status, queueSize, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 2, 0, null);

		// 解析済み URL 件数
		parsedURLCount.setText(nf.format(model.db.getParsedPageCount()));
		parsedURLCount.setMinimumSize(new Dimension(60, parsedURLCount.getMinimumSize().height));
		parsedURLCount.setPreferredSize(new Dimension(60, parsedURLCount.getPreferredSize().height));
		parsedURLCount.setHorizontalAlignment(SwingConstants.RIGHT);
		layout(status, new JLabel("; complete="), 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 5, 0, null);
		layout(status, parsedURLCount, 4, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 2, 0, null);

		// 解析済み URL 件数
		errorURLCount.setText(nf.format(model.db.getErrorPageCount()));
		errorURLCount.setMinimumSize(new Dimension(60, errorURLCount.getMinimumSize().height));
		errorURLCount.setPreferredSize(new Dimension(60, errorURLCount.getPreferredSize().height));
		errorURLCount.setHorizontalAlignment(SwingConstants.RIGHT);
		layout(status, new JLabel("; error="), 5, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 5, 0, null);
		layout(status, errorURLCount, 6, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 2, 0, null);

		Container root = this.getContentPane();
		root.setLayout(new GridBagLayout());
		layout(root, toolbar, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0, 0, 0, null);
		layout(root, split2, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1, 0, 0, null);
		layout(root, status, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0, 0, 0, null);

		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setSize(800, 480);
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
				return;
			}
		});
		return;
	}

	// ======================================================================
	// アプリケーションの終了
	// ======================================================================
	/**
	 * アプリケーションを終了します。
	 * <p>
	*/
	private void exit(){
		logger.finest("exit()");

		// 実行中の処理がある場合は中断
		if(executor != null){
			executor.interrupt();
		}

		// 設定の保存
		pref.put("url", startURL.getText());
		pref.put("pattern", urlPattern.getText());
		try{
			pref.flush();
		} catch(BackingStoreException ex){
			logger.warning("設定の保存に失敗: " + ex);
		}

		// データベースの切断
		model.db.disconnect();
		return;
	}

	// ======================================================================
	// 処理の実行
	// ======================================================================
	/**
	 * 処理を実行します。
	 * <p>
	*/
	private void execute(){
		logger.finest("execute()");

		if(executor != null){
			executor.interrupt();
			return;
		}

		executor = new Thread(){
			@Override
			public void run(){
				exec.setText("停止");
				startURL.setEditable(false);
				clear.setEnabled(false);
				retry.setEnabled(false);
				parsedPageTable.setEnabled(false);
				try{
					executeLoop(new URL(startURL.getText()));
				} catch(InterruptedException ex){
					logger.fine("中断されました");
				} catch(final Throwable ex){
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							error(ex);
						}
					});
				} finally {
					executor = null;
					exec.setText("開始");
					startURL.setEditable(true);
					clear.setEnabled(true);
					retry.setEnabled(true);
					parsedPageTable.setEnabled(true);
				}
				return;
			}
		};
		executor.setDaemon(true);
		executor.setPriority(Thread.MIN_PRIORITY);
		executor.start();
		return;
	}

	// ======================================================================
	// 対象 URL パターンの参照
	// ======================================================================
	/**
	 * 対象 URL パターンを参照します。
	 * <p>
	 * @return 対象 URL パターン
	 */
	private Pattern getPattern(){
		StringBuilder buffer = new StringBuilder();
		StringTokenizer tk = new StringTokenizer(urlPattern.getText(), "*", true);
		while(tk.hasMoreTokens()){
			String token = tk.nextToken();
			if(token.equals("*")){
				buffer.append(".*");
			} else {
				buffer.append(Pattern.quote(token));
			}
		}
		return Pattern.compile(buffer.toString());
	}

	// ======================================================================
	// 処理の実行
	// ======================================================================
	/**
	 * 処理を実行します。
	 * <p>
	 * @param url URL
	 * @throws InterruptedException 処理が中断された場合
	 */
	private void executeLoop(URL url) throws InterruptedException {
		model.enqueue(url);
		while(! Thread.interrupted()){
			List<URL> list = model.select();
			if(list == null)	break;
			for(int i=0; i<list.size() && ! Thread.interrupted(); i++){
				execute(list.get(i), getPattern());
			}
		}
		return;
	}

	// ======================================================================
	// 処理の実行
	// ======================================================================
	/**
	 * 処理を実行します。
	 * <p>
	 * @param url URL
	 * @param pattern 対象 URL パターン
	 * @throws InterruptedException 処理が中断された場合
	 */
	private void execute(URL url, Pattern pattern) throws InterruptedException {
		NumberFormat nf = NumberFormat.getNumberInstance();
		logger.finest("=======================================================");
		logger.finest(url.toString());

		// 対象 URL をステータスバーに表示
		parsingURL.setText(url.toString());

		// ページの解析処理を実行
		List<URL> next = null;
		ParsedPage page = new ParsedPage(url);
		try{
			next = page.parse();
		} catch(InterruptedException ex){
			throw ex;
		} catch(IgnoreableException ex){
			logger.fine(ex.toString());
			model.dequeue(url);
			return;
		} catch(Exception ex){
			page.setException(ex);
			next = Collections.emptyList();
		} catch(AssertionError ex){
			page.setException(ex);
			next = Collections.emptyList();
		} finally {
			parsingURL.setText("");
		}

		// パターンに一致する URL のみをキューに追加
		for(int i=0; i<next.size(); i++){
			URL u = next.get(i);
			if(! pattern.matcher(u.toString()).matches()){
				logger.finest("パターン非一致: " + u);
				next.remove(u);
			}
		}

		// ページの解析結果と次の解析対象を設定
		model.addPage(page, next);

		// 統計情報を設定
		queueSize.setText(nf.format(model.db.getQueuePageCount()));
		parsedURLCount.setText(nf.format(model.db.getParsedPageCount()));
		errorURLCount.setText(nf.format(model.db.getErrorPageCount()));

		// キューから次の URL を参照
		return;
	}

	// ======================================================================
	// エラーの通知
	// ======================================================================
	/**
	 * エラーを通知します。
	 * <p>
	 * @param ex 発生した例外
	*/
	private void error(Throwable ex){
		ex.printStackTrace();
		String title = ex.getClass().getSimpleName();
		JTextArea msg = new JTextArea();
		msg.setEditable(false);
		msg.setText(ex.getMessage());
		JOptionPane.showMessageDialog(this, new JScrollPane(msg), title, JOptionPane.ERROR_MESSAGE);
		return;
	}

	// ======================================================================
	// コンポーネントのレイアウト
	// ======================================================================
	/**
	 * コンポーネントをレイアウトします。
	 * <p>
	 * @param parent 親コンポーネント
	 * @param cmp 配置するコンポーネント
	 * @param gridx x方向グリッド
	 * @param gridy y方向グリッド
	 * @param gridwidth x方向グリッド幅
	 * @param gridheight y方向グリッド幅
	 * @param anchor アンカー
	 * @param fill フィル
	 * @param weightx x方向ウェイト
	 * @param weighty y方向ウェイト
	 * @param ipadx x方向パディング
	 * @param ipady y方向パディング
	 * @param insets インセット
	 */
	private static void layout(Container parent, Component cmp, int gridx, int gridy, int gridwidth, int gridheight, int anchor, int fill, double weightx, double weighty, int ipadx, int ipady, Insets insets){
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = gridx;			c.gridy = gridy;
		c.gridwidth = gridwidth;	c.gridheight = gridheight;
		c.weightx = weightx;		c.weighty = weighty;
		c.anchor = anchor;			c.fill = fill;
		c.ipadx = ipadx;			c.ipady = ipady;
		c.insets = (insets!=null)? insets: new Insets(0, 0, 0, 0);
		parent.add(cmp, c);
		return;
	}

	// ======================================================================
	// アプリケーションの実行
	// ======================================================================
	/**
	 * アプリケーションを起動します。
	 * <p>
	 * @param args コマンドライン引数
	 */
	public static void main(String[] args) {

		// ログ出力の設定
		Logger logger = Logger.getLogger("org.koiroha.xml");
		for(Handler h: logger.getHandlers()){
			logger.removeHandler(h);
		}
		Handler handler = new ConsoleHandler();
		logger.addHandler(handler);
		handler.setLevel(Level.ALL);
		logger.setLevel(Level.ALL);
		handler.setFormatter(new Formatter(){
			@Override
			public String format(LogRecord r) {
				return String.format("[%1$tY-%1$tm-%1$td %1$tT] %2$s - %3$s%n",
					new Date(r.getMillis()), r.getLevel(), r.getMessage());
			}
		});

		// Apache Derby が利用可能であることを確認
		try{
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch(ClassNotFoundException ex){
			JOptionPane.showMessageDialog(
				null, "Apache Derby をクラスパスに追加してください", "実行環境エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// データベースディレクトリの選択
		File dir = selectDatabase();
		if(dir == null){
			return;
		}

		// ウィンドウのオープン
		try{
			JFrame frame = new WebCrawler(dir);
			frame.setVisible(true);
		} catch(SQLException ex){
			logger.log(Level.SEVERE, "データベースの接続に失敗しました", ex);
			JOptionPane.showMessageDialog(
					null, ex.getMessage(), "データベースの接続に失敗しました", JOptionPane.ERROR_MESSAGE);
		}
		return;
	}

	// ======================================================================
	// データベースディレクトリの選択
	// ======================================================================
	/**
	 * データベース用のディレクトリを選択します。
	 * <p>
	 * @return 選択されたディレクトリ
	 */
	private static File selectDatabase() {
		final String dirName = pref.get("current.dir", "./WEBCRAWLERDB");

		// ディレクトリ選択ダイアログの構築
		String msg = "<html>作業ディレクトリを選択してください<br>WebCrawlerのデータベースはここで選択したディレクトリを使用します";
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		JLabel label = new JLabel(msg);
		final JTextField dirInput = new JTextField();
		JButton refer = new JButton("参照...");
		dirInput.setEditable(false);
		dirInput.setText(new File(dirName).getAbsolutePath());
		dirInput.setPreferredSize(new Dimension(150, dirInput.getPreferredSize().height));
		dirInput.setCaretPosition(dirInput.getText().length()-1);
		layout(panel, label, 0, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 1, 0, 0, new Insets(0, 5, 5, 5));
		layout(panel, dirInput, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0, 0, 0, null);
		layout(panel, refer, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0, null);
		refer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JFileChooser dialog = new JFileChooser();
				dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				dialog.setCurrentDirectory(new File(dirName));
				dialog.setSelectedFile(new File(dirName, "WEBCRAWLERDB"));
				if(dialog.showOpenDialog(dirInput) != JFileChooser.APPROVE_OPTION){
					return;
				}
				String dirName = dialog.getSelectedFile().getAbsolutePath();
				dirInput.setText(dirName);
				pref.put("current.dir", dirName);
				return;
			}
		});

		File dir = null;
		do{

			// ダイアログの表示
			int result = JOptionPane.showConfirmDialog(
					null, panel, "データベースの選択", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if(result != JOptionPane.OK_OPTION){
				return null;
			}

			// ディレクトリの新規作成を確認
			dir = new File(dirInput.getText());
			if(! dir.isDirectory()){
				result = JOptionPane.showConfirmDialog(
					null, "ディレクトリ " + dir.getName() + " は存在しません\n新規に作成しますか?");
				if(result != JOptionPane.OK_OPTION){
					dir = null;
				}
			}
		} while(dir == null);

		return dir;
	}

}
