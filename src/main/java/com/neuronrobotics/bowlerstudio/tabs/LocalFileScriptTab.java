package com.neuronrobotics.bowlerstudio.tabs;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Duration;
import java.util.HashMap;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import javafx.application.Platform;
import javafx.embed.swing.MySwingNode;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.scene.layout.VBox;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.IssueReportingExceptionHandler;
import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.utils.FindTextWidget;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class LocalFileScriptTab extends VBox implements IScriptEventListener, EventHandler<WindowEvent> {
	private static final int MaxTextSize = 75000;
	private static final UncaughtExceptionHandler ISSUE_REPORTING_EXCEPTION_HANDLER = new UncaughtExceptionHandler() {
		IssueReportingExceptionHandler reporter = new IssueReportingExceptionHandler();

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			if (reporter.getTitle(e).contains("java.awt.datatransfer.DataFlavor at line 503")) {
				System.err.println("Known bug in the Swing system, nothing we can do but ignore it");
				e.printStackTrace();
				return;
			}
			reporter.uncaughtException(t, e);
		}
	};// new IssueReportingExceptionHandler();
	private long lastRefresh = 0;
	private ScriptingFileWidget scripting;

	IScriptEventListener l = null;

	private SwingNode swingNode;
	private RTextScrollPane spscrollPane;

	private Highlighter highlighter;

	private HighlightPainter painter;
	private int lineSelected = 0;

	private MyRSyntaxTextArea textArea = new MyRSyntaxTextArea(200, 300);

	private final File file;

	private Font myFont;
	private String content = "";

	private static HashMap<String, String> langaugeMapping = new HashMap<>();

	private static LocalFileScriptTab selectedTab = null;
	static {
		SwingUtilities
				.invokeLater(() -> Thread.setDefaultUncaughtExceptionHandler(new IssueReportingExceptionHandler()));

	}

	public class MyRSyntaxTextArea extends RSyntaxTextArea implements ComponentListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MyRSyntaxTextArea() {
			this.addComponentListener(this);
		}

		public MyRSyntaxTextArea(int i, int j) {
			super(i, j);
		}

		public void componentResized(ComponentEvent e) {
			System.err.println("componentResized");

		}

		public void componentHidden(ComponentEvent e) {
			System.err.println("componentHidden");
		}

		public void componentMoved(ComponentEvent e) {
			System.err.println("componentMoved");
		}

		public void componentShown(ComponentEvent e) {
			System.err.println("componentShown");
		}

	}

	public static void setExtentionSyntaxType(String shellType, String syntax) {
		langaugeMapping.put(shellType, syntax);
	}

	public LocalFileScriptTab(File file) throws IOException {
		Thread.setDefaultUncaughtExceptionHandler(new IssueReportingExceptionHandler());
		this.file = file;
		setScripting(new ScriptingFileWidget(file));
		setSpacing(5);
		l = this;

		getScripting().addIScriptEventListener(l);
		String type;

		String shellType = ScriptingEngine.getShellType(file.getName());
		switch (shellType) {
		case "Clojure":
			type = SyntaxConstants.SYNTAX_STYLE_CLOJURE;
			break;
		default:
			type = langaugeMapping.get(shellType);
			if (type == null) {
				type = SyntaxConstants.SYNTAX_STYLE_NONE;
				if (shellType.toLowerCase().contains("arduino")) {
					type = SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
				}
			}
		case "JSON":
			type = SyntaxConstants.SYNTAX_STYLE_JSON;
			break;
		case "ArduingScriptingLangauge":
			type = SyntaxConstants.SYNTAX_STYLE_LISP;
			break;
		case "Arduino":
			type = SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
			break;
		case "Groovy":
			type = SyntaxConstants.SYNTAX_STYLE_GROOVY;
			break;
		case "Jython":
			type = SyntaxConstants.SYNTAX_STYLE_PYTHON;
			break;
		case "MobilBaseXML":
			type = SyntaxConstants.SYNTAX_STYLE_XML;
			break;
		case "Kotlin":
			type = SyntaxConstants.SYNTAX_STYLE_JAVA;
			break;
		case "SVG":
			type = SyntaxConstants.SYNTAX_STYLE_XML;
			break;
		case "Bash":
			type = SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
			break;
		}
		textArea.setSyntaxEditingStyle(type);
		textArea.setCodeFoldingEnabled(true);

		textArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {

			}

			@Override
			public void insertUpdate(DocumentEvent e) {

			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				new Thread() {
					public void run() {
						if (textArea.isEnabled())
							setContent(textArea.getText());
						getScripting().removeIScriptEventListener(l);
						getScripting().setCode(content);
						getScripting().addIScriptEventListener(l);
					}
				}.start();
			}
		});
		textArea.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {

				// Lets start with some default values for the line and column.
				int linenum = 1;
				int columnnum = 1;

				// We create a try catch to catch any exceptions. We will simply
				// ignore such an error for our demonstration.
				try {
					// First we find the position of the caret. This is the
					// number of where the caret is in relation to the start of
					// the JTextArea
					// in the upper left corner. We use this position to find
					// offset values (eg what line we are on for the given
					// position as well as
					// what position that line starts on.
					int caretpos = textArea.getCaretPosition();
					linenum = textArea.getLineOfOffset(caretpos);

					// We subtract the offset of where our line starts from the
					// overall caret position.
					// So lets say that we are on line 5 and that line starts at
					// caret position 100, if our caret position is currently
					// 106
					// we know that we must be on column 6 of line 5.
					columnnum = caretpos - textArea.getLineStartOffset(linenum);

					// We have to add one here because line numbers start at 0
					// for getLineOfOffset and we want it to start at 1 for
					// display.
					linenum += 1;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (lineSelected != linenum) {
					lineSelected = linenum;
					// System.err.println("Select "+lineSelected);
					new Thread(() -> {
						BowlerStudio.select(file, lineSelected);
					}).start();
				}

			}

		});

		textArea.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 2) {
					highlighter.removeAllHighlights();
				}
				// System.out.println("Number of click: " + e.getClickCount());
				// System.out.println("Click position (X, Y): " + e.getX() + ",
				// " + e.getY());
			}
		});

		spscrollPane = new RTextScrollPane(textArea);

		swingNode = new MySwingNode(this);
		// swingNode=new javafx.embed.swing.SwingNode();
		SwingUtilities.invokeLater(() -> swingNode.setContent(spscrollPane));

		getScripting().setFocusTraversable(false);

		getChildren().setAll(swingNode, getScripting());
		swingNode.setOnMouseEntered(mouseEvent -> {
			// System.err.println("On mouse entered " + file.getName());
			// resizeEvent();
			SwingUtilities.invokeLater(() -> {
				resizeEvent();
				setSelectedTab(this);
//				spscrollPane.setSize((int) spscrollPane.getWidth(), (int) spscrollPane.getHeight());
//				spscrollPane.invalidate();
//				spscrollPane.repaint();
//				textArea.invalidate();
//				textArea.repaint();
//				textArea.requestFocusInWindow();
//				FxTimer.runLater(Duration.ofMillis((int) 16), () -> {
//					swingNode.requestFocus();
//				});
			});
		});
		// textArea
		// Set event listener to listen for CTRL+S and save file
		KeyStroke keystroke_s = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
		textArea.getInputMap().put(keystroke_s, "s");
		textArea.getActionMap().put("s", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3361326129563407389L;

			public void actionPerformed(ActionEvent e) {

				System.out.println("Save " + file + " now.");
				getScripting().saveTheFile(file);

			}
		});
		// Set event listener to listen for CTRL+F and find text
		KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK);
		textArea.getInputMap().put(keystroke, "f");
		textArea.getActionMap().put("f", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -4698223073831405851L;

			public void actionPerformed(ActionEvent e) {

				findTextWidget();

			}
		});

		myFont = textArea.getFont();
		highlighter = textArea.getHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

		highlighter.removeAllHighlights();

//		widthProperty().addListener((w, o, n) -> {
//			resizeEvent();
//
//		});
//		heightProperty().addListener((w, o, n) -> {
//			resizeEvent();
//		});
		SwingUtilities.invokeLater(() -> {
			try {
				if (getScripting() != null && getScripting().getCode() != null) {
					onScriptChanged(null, getScripting().getCode(), file);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		});

	}

	private void resizeEvent() {
		if (!((lastRefresh + 60) < System.currentTimeMillis())
				|| spscrollPane.getVerticalScrollBar().getValueIsAdjusting()
				|| spscrollPane.getHorizontalScrollBar().getValueIsAdjusting()) {
			return;
		}
		lastRefresh = System.currentTimeMillis();
		SwingUtilities.invokeLater(() -> {
			spscrollPane.setSize((int) spscrollPane.getWidth(), (int) spscrollPane.getHeight());
			spscrollPane.invalidate();
			spscrollPane.repaint();
			textArea.invalidate();
			textArea.repaint();

			textArea.requestFocusInWindow();
			BowlerStudio.runLater(Duration.ofMillis((int) 16), () -> {
				swingNode.setContent(spscrollPane);
				swingNode.requestFocus();
			});
		});

	}

	@Override
	public void onScriptFinished(Object result, Object previous, File source) {
		// SwingUtilities.invokeLater(new Runnable() {
		// @Override
		// public void run() {
		// textArea.requestFocusInWindow();
		// }
		// });

	}

	@Override
	public void onScriptChanged(String previous, String current, File source) {
		// int place = textArea.getCaretPosition();
		// System.err.println("Carrot position is= "+place);
		// codeArea.replaceText(current);
		// codeArea.setCursor(place);
		System.out.println(file.getAbsolutePath()+" changed ");
		// empty
		SwingUtilities.invokeLater(() -> {
			setContent(current);
			if (previous == null)
				SwingUtilities.invokeLater(() -> textArea.setCaretPosition(0));
		});

	}

	private void setContent(String current) {
		if (current.length() > 3 && !content.contentEquals(current)) {
			content = current; // writes
			if (current.length() > MaxTextSize) {
				textArea.setText(
						"File too big for this text editor: " + current.length() + " larger than " + MaxTextSize);
				textArea.setEnabled(false);
			} else {
				if(!textArea.getText().contentEquals(content))
					textArea.setText(current);
			}
		}
	}

	@Override
	public void onScriptError(Throwable except, File source) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				System.out.println("script error");
				textArea.requestFocusInWindow();
			}
		});

	}

	public void findTextWidget() {
		BowlerStudio.runLater(() -> {
			Stage s = new Stage();
			new Thread() {
				public void run() {
					Thread.setDefaultUncaughtExceptionHandler(ISSUE_REPORTING_EXCEPTION_HANDLER);

					FindTextWidget controller = new FindTextWidget();
					controller.setTextArea(textArea);
					try {
						controller.start(s);
					} catch (Exception e) {
						ISSUE_REPORTING_EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), e);
					}
				}
			}.start();
		});
	}

	@Override
	public void handle(WindowEvent event) {
		getScripting().stop();
	}

	public ScriptingFileWidget getScripting() {
		return scripting;
	}

	public void setScripting(ScriptingFileWidget scripting) {
		this.scripting = scripting;
	}

	public void setHighlight(int lineNumber, Color color) throws BadLocationException {
		if (textArea == null) {
			return;
		}
		painter = new DefaultHighlighter.DefaultHighlightPainter(color);
		int startIndex = textArea.getLineStartOffset(lineNumber - 1);
		int endIndex = textArea.getLineEndOffset(lineNumber - 1);

		try {

			SwingUtilities.invokeLater(() -> {
				textArea.moveCaretPosition(startIndex);
			});
		} catch (Error | Exception ex) {
			ex.printStackTrace();
		}
		SwingUtilities.invokeLater(() -> {
			try {
				textArea.getHighlighter().addHighlight(startIndex, endIndex, painter);
			} catch (BadLocationException e) {
				ISSUE_REPORTING_EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), e);

			}
		});

	}

	public void clearHighlits() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				highlighter.removeAllHighlights();
			}
		});
	}

	public int getFontSize() {
		return myFont.getSize();
	}

	public void setFontSize(int size) {
		myFont = new Font(myFont.getName(), myFont.getStyle(), size);
		setFontLoop();
	}

	private void setFontLoop() {
		BowlerStudio.runLater(Duration.ofMillis(200), new Runnable() {
			@Override
			public void run() {
				Thread.setDefaultUncaughtExceptionHandler(ISSUE_REPORTING_EXCEPTION_HANDLER);
				SwingUtilities.invokeLater(() -> {
					try {
						textArea.setFont(myFont);
					} catch (Throwable ex) {
						// ISSUE_REPORTING_EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(),
						// ex);
						setFontLoop();
					}
				});
			}
		});
	}

	public static LocalFileScriptTab getSelectedTab() {
		return selectedTab;
	}

	public static void setSelectedTab(LocalFileScriptTab selectedTab) {
		// System.err.println("Currently selected "+selectedTab.file.getAbsolutePath());
		LocalFileScriptTab.selectedTab = selectedTab;
	}

	public void insertString(String string) {
		int caretpos = textArea.getCaretPosition();
		String text = content;
		String substring = text.substring(0, caretpos);
		String substring2;
		try {
			substring2 = text.substring(caretpos, text.length());
		} catch (java.lang.StringIndexOutOfBoundsException ex) {
			substring2 = "";
		}
		String combined = substring + string + substring2;
		onScriptChanged(text, combined, file);
		SwingUtilities.invokeLater(() -> {
			textArea.setCaretPosition(caretpos + string.length());
		});
	}
}
