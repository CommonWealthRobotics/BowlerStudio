package com.neuronrobotics.bowlerstudio.scripting;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.neuronrobotics.bowlerstudio.BowlerKernel;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.imageprovider.OpenCVImageProvider;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class CommandLineWidget  extends BorderPane{

	private TextField cmdLineInterface = new TextField ();
	private ArrayList<String> history = new ArrayList<>();
	private Button runfx = new Button("Run");
	private int historyIndex=0;
	private HBox controlPane;
	private String codeText="";
	private boolean running = false;
	private Thread scriptRunner = null;
	public CommandLineWidget(){
		runfx.setOnAction(e -> {
	    	new Thread(){
	    		public void run(){
	    			
	    			startStopAction();
	    		}
	    	}.start();
		});
		cmdLineInterface.setOnAction(event -> {

			startStopAction();
		});
		cmdLineInterface.setPrefWidth(80*4);
		cmdLineInterface.addEventFilter( KeyEvent.KEY_PRESSED, event -> {
			//Platform.runLater(() -> {
			    if( (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) ) {
			    	System.err.println("Key pressed "+event.getCode()+" history index = "+historyIndex+" history size= "+history.size());
			    	if(historyIndex==0){
					       String text = cmdLineInterface.getText();
					       if(text.length()>0){
					    	   // store what was in the box into he history
					    	   history.add(text);
					       }
			    	}
			       
			       if(event.getCode() == KeyCode.UP)
			    	   historyIndex++;
			       else
			    	   historyIndex--;
			       if(history.size()>0){
				       if(historyIndex>history.size()){
				    	   historyIndex =  history.size();
				       }
				       if(historyIndex<0)
				    	   historyIndex=0;
				       //History index established
				       if(historyIndex>0)
					       Platform.runLater(() -> {
								cmdLineInterface.setText(history.get(history.size()-historyIndex));
					       });
				       else
				    	   Platform.runLater(() -> {
								cmdLineInterface.setText("");
					       }); 
			       }
			       event.consume();
			    } 
			//});
		});
		try {
			history= BowlerKernel.loadHistory();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				
				BowlerKernel.writeHistory(history);
			}
		});
		
		controlPane = new HBox(10);
		controlPane.getChildren().add(new Label("Bowler CMD:"));
		controlPane.getChildren().add(cmdLineInterface);
		controlPane.getChildren().add(runfx);
		setPadding(new Insets(1, 0, 3, 10));
		setTop(controlPane);
	}
	
	private void reset() {
		running = false;
		Platform.runLater(() -> {

				runfx.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
				runfx.setText("Go");

		});

	}
	
	private void start() {

		running = true;
		Platform.runLater(()->{
			runfx.setText("Kill");
			runfx.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
		});
		scriptRunner = new Thread() {

			public void run() {
				String name;
	
				try {
					Object obj = ScriptingEngine.inlineScriptStringRun(getCode(), null,ShellType.GROOVY);
					reset();
				} 
				catch (groovy.lang.MissingPropertyException |org.python.core.PyException d){
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Device missing error");
						String message = "This script needs a device connected: ";
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						d.printStackTrace(pw);
						
						String stackTrace = sw.toString();
						
						if(stackTrace.contains("dyio"))
							message+="dyio";
						else if(stackTrace.contains("camera"))
							message+="camera";
						else if(stackTrace.contains("gamepad"))
							message+="gamepad";
						else
							message+=stackTrace;
						alert.setHeaderText(message);
						alert.setContentText("You need to connect it before running again");
						alert.showAndWait();
						if(stackTrace.contains("dyio"))
							ConnectionManager.addConnection();
						else if(stackTrace.contains("camera"))
							ConnectionManager.addConnection(new OpenCVImageProvider(0),"camera0");
						else if(stackTrace.contains("gamepad"))
							ConnectionManager.onConnectGamePad("gamepad");
						reset();
					});
					
				}
				catch (Exception ex) {
					System.err.println("Script exception of type= "+ex.getClass().getName());
					Platform.runLater(() -> {
						reset();
					});

					throw new RuntimeException(ex);
				}

			}
		};

		scriptRunner.start();

	}
	public String getCode() {
		return codeText;
	}
	public void setCode(String string) {
		String pervious = codeText;
		codeText = string;
	}
	private void startStopAction(){
		String text = cmdLineInterface.getText();
		text+="\r\n";
		Platform.runLater(() -> {
			cmdLineInterface.setText("");
		});
		System.out.println(text);
		history.add(text);
		BowlerKernel.writeHistory(history);
		if(historyIndex!=0)
			historyIndex--;
		else
			historyIndex=0;
		setCode(text);
		Platform.runLater(()->runfx.setDisable(true));
		if (running)
			stop();
		else
			start();
		Platform.runLater(()->runfx.setDisable(false));
	}
	public void stop() {
		// TODO Auto-generated method stub

		reset();
		if (scriptRunner != null)
			while (scriptRunner.isAlive()) {

				Log.debug("Interrupting");
				ThreadUtil.wait(10);
				try {
					scriptRunner.interrupt();
					scriptRunner.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

	}
}
