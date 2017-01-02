package com.neuronrobotics.bowlerstudio.tabs;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.security.AccessControlContext;
import java.time.Duration;
import java.util.regex.Pattern;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;

import javax.swing.AbstractAction;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingNode;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.fxmisc.richtext.StyleSpansBuilder;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.util.ThreadUtil;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.tk.TKStage;
import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.PluginManager;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.utils.BowlerConnectionMenu;
import com.neuronrobotics.bowlerstudio.utils.FindTextWidget;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class LocalFileScriptTab extends VBox implements IScriptEventListener, EventHandler<WindowEvent> {

	private ScriptingFileWidget scripting;

	IScriptEventListener l = null;
	private RSyntaxTextArea textArea = new RSyntaxTextArea(100, 150);
	private MySwingNode sn;
	private RTextScrollPane sp;

	private Highlighter highlighter;

	private HighlightPainter painter;
	private int lineSelected = 0;

	private class MySwingNode extends SwingNode {
		/**
		 * Returns the {@code SwingNode}'s minimum width for use in layout
		 * calculations. This value corresponds to the minimum width of the
		 * Swing component.
		 * 
		 * @return the minimum width that the node should be resized to during
		 *         layout
		 */
		@Override
		public double minWidth(double height) {

			return 200;
		}

		/**
		 * Returns the {@code SwingNode}'s minimum height for use in layout
		 * calculations. This value corresponds to the minimum height of the
		 * Swing component.
		 * 
		 * @return the minimum height that the node should be resized to during
		 *         layout
		 */
		@Override
		public double minHeight(double width) {
			return 200;
		}
	}

	public LocalFileScriptTab(File file) throws IOException {

		setScripting(new ScriptingFileWidget(file));
		setSpacing(5);
		l = this;

		getScripting().addIScriptEventListener(l);
		String type;
		switch (ScriptingEngine.getShellType(file.getName())) {
		case "Clojure":
			type = SyntaxConstants.SYNTAX_STYLE_CLOJURE;
			break;
		default:
		case "Groovy":
			type = SyntaxConstants.SYNTAX_STYLE_GROOVY;
			break;
		case "Jython":
			type = SyntaxConstants.SYNTAX_STYLE_PYTHON;
			break;
		case "RobotXML":
			type = SyntaxConstants.SYNTAX_STYLE_XML;
			break;

		}
		textArea.setSyntaxEditingStyle(type);
		textArea.setCodeFoldingEnabled(true);
		SwingUtilities.invokeLater(() -> textArea.setText(getScripting().getCode()));
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
						getScripting().removeIScriptEventListener(l);
						getScripting().setCode(textArea.getText());
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
				}
				if (lineSelected != linenum) {
					lineSelected = linenum;
					// System.err.println("Select "+lineSelected);
					BowlerStudio.select(file, lineSelected);

				}

			}

		});

		textArea.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 3) {
					highlighter.removeAllHighlights();
				}
				// System.out.println("Number of click: " + e.getClickCount());
				// System.out.println("Click position (X, Y): " + e.getX() + ",
				// " + e.getY());
			}
		});

		sp = new RTextScrollPane(textArea);

		sn = new MySwingNode();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				sn.setContent(sp);

			}
		});

		getScripting().setFocusTraversable(false);

		getChildren().setAll(sn, getScripting());
		sn.setOnMouseEntered(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override
			public void handle(javafx.scene.input.MouseEvent mouseEvent) {
				sn.requestFocus();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						textArea.requestFocusInWindow();
						KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK);
						textArea.getInputMap().put(keystroke, "f");
						textArea.getActionMap().put("f", new AbstractAction() {
							public void actionPerformed(ActionEvent e) {
								
								findTextWidget();

							}
						});
					}

				});
			}
		});

		highlighter = textArea.getHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

		highlighter.removeAllHighlights();

	}

	@Override
	public void onScriptFinished(Object result, Object previous, File source) {
		// // TODO Auto-generated method stub
		// SwingUtilities.invokeLater(new Runnable() {
		// @Override
		// public void run() {
		// textArea.requestFocusInWindow();
		// }
		// });

	}

	@Override
	public void onScriptChanged(String previous, String current, File source) {
		// Cursor place = codeArea.getCursor();
		// codeArea.replaceText(current);
		// codeArea.setCursor(place);
		if (current.length() > 3 && !textArea.getText().contentEquals(current)) {// no
																					// empty
																					// writes
			SwingUtilities.invokeLater(() -> textArea.setText(current));

		}
	}

	@Override
	public void onScriptError(Exception except, File source) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				System.out.println("script error");
				textArea.requestFocusInWindow();
			}
		});

	}

	public void findTextWidget() {
		Platform.runLater(() -> {
			Stage s = new Stage();
			new Thread() {
				public void run() {
			
					FindTextWidget controller = new FindTextWidget();
					controller.setTextArea(textArea);
					try {
						controller.start(s);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		});
		// DeviceManager.addConnection();
	}

	@Override
	public void handle(WindowEvent event) {
		// TODO Auto-generated method stub
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

			textArea.moveCaretPosition(startIndex);
		} catch (Error | Exception ex) {
			ex.printStackTrace();
		}
		textArea.getHighlighter().addHighlight(startIndex, endIndex, painter);

	}

	public void clearHighlits() {
		highlighter.removeAllHighlights();
	}
}
