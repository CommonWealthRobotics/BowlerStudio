package com.neuronrobotics.bowlerstudio.tabs;

import java.awt.Color;
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

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
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
            	getScripting().removeIScriptEventListener(l);
            	getScripting().setCode(textArea.getText());
            	getScripting().addIScriptEventListener(l);
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
	            }
	        });
		});

		highlighter = textArea.getHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);
		
		highlighter.removeAllHighlights();

	}



	@Override
	public void onGroovyScriptFinished(	Object result,Object previous) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	textArea.requestFocusInWindow();
            }
        });
		
	}


	@Override
	public void onGroovyScriptChanged(String previous, String current) {
//		 Cursor place = codeArea.getCursor();
//		 codeArea.replaceText(current);
//		 codeArea.setCursor(place);
		Platform.runLater(()->{
			textArea.setText(current);
		});
		
	}


	@Override
	public void onGroovyScriptError(Exception except) {
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
