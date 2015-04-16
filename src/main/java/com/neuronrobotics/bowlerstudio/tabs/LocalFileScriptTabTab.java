package com.neuronrobotics.bowlerstudio.tabs;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.fxmisc.richtext.StyleSpansBuilder;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.PluginManager;

import javafx.scene.control.Tab;

public class LocalFileScriptTabTab extends Tab implements IScriptEventListener{
	
	private ScriptingEngineWidget scripting;
	private ConnectionManager dyio;
	private File file;
	
    private static final String[] KEYWORDS = new String[]{
        "def", "in", "as", "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import",
        "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while"
    };

    private static final Pattern KEYWORD_PATTERN
            = Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b");
    private final CodeArea codeArea = new CodeArea();
	private VBox vBox;

    
	public LocalFileScriptTabTab(ConnectionManager connectionManager, File file) throws IOException {
		this.dyio = connectionManager;
		this.file = file;
		scripting = new ScriptingEngineWidget(connectionManager, file );
		setText(file.getName());
        codeArea.textProperty().addListener(
                (ov, oldText, newText) -> {
                    Matcher matcher = KEYWORD_PATTERN.matcher(newText);
                    int lastKwEnd = 0;
                    StyleSpansBuilder<Collection<String>> spansBuilder
                    = new StyleSpansBuilder<>();
                    while (matcher.find()) {
                        spansBuilder.add(Collections.emptyList(),
                                matcher.start() - lastKwEnd);
                        spansBuilder.add(Collections.singleton("keyword"),
                                matcher.end() - matcher.start());
                        lastKwEnd = matcher.end();
                    }
                    spansBuilder.add(Collections.emptyList(),
                            newText.length() - lastKwEnd);
                    codeArea.setStyleSpans(0, spansBuilder.create());
                });

        EventStream<Change<String>> textEvents
                = EventStreams.changesOf(codeArea.textProperty());

        textEvents.reduceSuccessions((a, b) -> b, Duration.ofMillis(1000)).
                subscribe(code -> {
                    //code in text box changed
                	scripting.setCode(codeArea.getText());
                	//scripting.save();
                });
    	Platform.runLater(()->{
            codeArea.replaceText(scripting.getCode());
		});

        
        scripting.addIScriptEventListener(this);
		
        
		// Layout logic
		HBox hBox = new HBox(5);
		hBox.getChildren().setAll(codeArea);
		HBox.setHgrow(codeArea, Priority.ALWAYS);

		vBox = new VBox(5);
		vBox.getChildren().setAll(hBox, scripting);
		VBox.setVgrow(codeArea, Priority.ALWAYS);
		
		codeArea.setPrefSize(1000, 1000);
		setContent(vBox);
	}


	@Override
	public void onGroovyScriptFinished(GroovyShell shell, Script script,
			Object result) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onGroovyScriptChanged(String previous, String current) {
		 Cursor place = codeArea.getCursor();
		 codeArea.replaceText(current);
		 codeArea.setCursor(place);
	}


	@Override
	public void onGroovyScriptError(GroovyShell shell, Script script,
			Exception except) {
		// TODO Auto-generated method stub
		
	}
}
