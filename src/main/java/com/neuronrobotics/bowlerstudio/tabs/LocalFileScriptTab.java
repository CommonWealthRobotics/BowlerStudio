package com.neuronrobotics.bowlerstudio.tabs;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import javafx.application.Platform;
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
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.PluginManager;
import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class LocalFileScriptTab extends VBox implements IScriptEventListener, EventHandler<WindowEvent> {
	
	private ScriptingFileWidget scripting;

    IScriptEventListener l=null;
	private RSyntaxTextArea textArea;
	private SwingNode sn;
	private RTextScrollPane sp;

	private Highlighter highlighter;

	private HighlightPainter painter;
	private int pos = 0;

    
	public LocalFileScriptTab( File file) throws IOException {
		
		setScripting(new ScriptingFileWidget( file ));
		setSpacing(5);
		l=this;


		getScripting().addIScriptEventListener(l);
		String type = SyntaxConstants.SYNTAX_STYLE_GROOVY;
		switch(ScriptingEngine.setFilename(file.getName())){
			case CLOJURE:
				type = SyntaxConstants.SYNTAX_STYLE_CLOJURE;
				break;
			case GROOVY:
				type = SyntaxConstants.SYNTAX_STYLE_GROOVY;
				break;
			case JYTHON:
				type = SyntaxConstants.SYNTAX_STYLE_PYTHON;
				break;
			case NONE:
				break;
			case ROBOT:
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
	        	new Thread(){
	        		public void run(){
	                	getScripting().removeIScriptEventListener(l);
	                	getScripting().setCode(textArea.getText());
	                	getScripting().addIScriptEventListener(l);
	        		}
	        	}.start();
	        }
	    });
		
		textArea.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					highlighter.removeAllHighlights();
				}

//				System.out.println("Number of click: " + e.getClickCount());
//				System.out.println("Click position (X, Y):  " + e.getX() + ", " + e.getY());
			}
		});

		sp = new RTextScrollPane(textArea);
		
		sn = new SwingNode();
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	sn.setContent(sp);
            	
            }
        });
		
		getScripting().setFocusTraversable(false);
		
		getChildren().setAll(sn,getScripting());
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
							//System.out.println("Got ctrl f "+ textArea.getSelectedText());
						      // Get the text to find...convert it to lower case for eaiser comparision
		                    String find = textArea.getSelectedText().toLowerCase();
		                    // Focus the text area, otherwise the highlighting won't show up
		                    textArea.requestFocusInWindow();
		                    // Make sure we have a valid search term
		                    if (find != null && find.length() > 0) {
		                        Document document = textArea.getDocument();
		                        int findLength = find.length();
		                        try {
		                            boolean found = false;
		                            // Rest the search position if we're at the end of the document
		                            if (pos + findLength > document.getLength()) {
		                                pos = 0;
		                            }
		                            // While we haven't reached the end...
		                            // "<=" Correction
		                            while (pos + findLength <= document.getLength()) {
		                                // Extract the text from teh docuemnt
		                                String match = document.getText(pos, findLength).toLowerCase();
		                                // Check to see if it matches or request
		                                if (match.equals(find)) {
		                                    found = true;
		                                    break;
		                                }
		                                pos++;
		                            }

		                            // Did we find something...
		                            if (found) {
		                                // Get the rectangle of the where the text would be visible...
		                                Rectangle viewRect = textArea.modelToView(pos);
		                                // Scroll to make the rectangle visible
		                                textArea.scrollRectToVisible(viewRect);
		                                // Highlight the text
		                                textArea.setCaretPosition(pos + findLength);
		                                textArea.moveCaretPosition(pos);
		                                // Move the search position beyond the current match
		                                pos += findLength;
		                            }

		                        } catch (Exception exp) {
		                            exp.printStackTrace();
		                        }

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
	public void onScriptFinished(	Object result,Object previous,File source) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	textArea.requestFocusInWindow();
            }
        });
		
	}


	@Override
	public void onScriptChanged(String previous, String current,File source) {
//		 Cursor place = codeArea.getCursor();
//		 codeArea.replaceText(current);
//		 codeArea.setCursor(place);
		Platform.runLater(()->{
			textArea.setText(current);
		});
		
	}


	@Override
	public void onScriptError(Exception except,File source) {
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
		int startIndex = textArea.getLineStartOffset(lineNumber-1);
        int endIndex = textArea.getLineEndOffset(lineNumber-1);
        textArea.getHighlighter().addHighlight(startIndex, endIndex, painter);
        try{
        	textArea.moveCaretPosition(startIndex);
        }catch (Error ex){
        	//ex.printStackTrace();
        }
	}


	public void clearHighlits() {
		highlighter.removeAllHighlights();
	}
}
