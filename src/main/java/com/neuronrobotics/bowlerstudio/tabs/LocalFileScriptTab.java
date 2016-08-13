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

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class LocalFileScriptTab extends VBox implements IScriptEventListener, EventHandler<WindowEvent> {

	private ScriptingFileWidget scripting;

	IScriptEventListener l = null;
	private RSyntaxTextArea textArea;
	private MySwingNode sn;
	private RTextScrollPane sp;

	private Highlighter highlighter;

	private HighlightPainter painter;
	private int pos = 0;
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
		textArea = new RSyntaxTextArea(100, 150);
		textArea.setSyntaxEditingStyle(type);
		textArea.setCodeFoldingEnabled(true);
		textArea.setText(getScripting().getCode());
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
		sn.setOnMouseEntered(mouseEvent -> {
			sn.requestFocus();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					textArea.requestFocusInWindow();
					KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK);
					textArea.getInputMap().put(keystroke, "f");
					textArea.getActionMap().put("f", new AbstractAction() {
						public void actionPerformed(ActionEvent e) {
							try {
								// System.out.println("Got ctrl f "+
								// textArea.getSelectedText());
								// Get the text to find...convert it to lower
								// case for eaiser comparision
								String find = textArea.getSelectedText().toLowerCase();
								// Focus the text area, otherwise the
								// highlighting won't show up
								textArea.requestFocusInWindow();
								// Make sure we have a valid search term
								if (find != null && find.length() > 0) {
									Document document = textArea.getDocument();
									int findLength = find.length();
									try {
										boolean found = false;
										// Rest the search position if we're at
										// the end of the document
										if (pos + findLength > document.getLength()) {
											pos = 0;
										}
										// While we haven't reached the end...
										// "<=" Correction
										while (pos + findLength <= document.getLength()) {
											// Extract the text from teh
											// docuemnt
											String match = document.getText(pos, findLength).toLowerCase();
											// Check to see if it matches or
											// request
											if (match.equals(find)) {
												found = true;
												break;
											}
											pos++;
										}

										// Did we find something...
										if (found) {
											// Get the rectangle of the where
											// the text would be visible...
											Rectangle viewRect = textArea.modelToView(pos);
											// Scroll to make the rectangle
											// visible
											textArea.scrollRectToVisible(viewRect);
											// Highlight the text
											textArea.setCaretPosition(pos + findLength);
											textArea.moveCaretPosition(pos);
											// Move the search position beyond
											// the current match
											pos += findLength;
										}

									} catch (Exception exp) {
										exp.printStackTrace();
									}

								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					});
				}

			});
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
			Platform.runLater(() -> {
				textArea.setText(current);
			});
		}
	}

	@Override
	public void onScriptError(Exception except, File source) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				textArea.requestFocusInWindow();
			}
		});

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

		painter = new DefaultHighlighter.DefaultHighlightPainter(color);
		int startIndex = textArea.getLineStartOffset(lineNumber - 1);
		int endIndex = textArea.getLineEndOffset(lineNumber - 1);

		try {

			textArea.moveCaretPosition(startIndex);
		} catch (Error ex) {
			// ex.printStackTrace();
		}
		textArea.getHighlighter().addHighlight(startIndex, endIndex, painter);

	}

	public void clearHighlits() {
		highlighter.removeAllHighlights();
	}
}
