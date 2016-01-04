package com.neuronrobotics.bowlerstudio.tabs;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.usb.UsbDisconnectedException;

import org.reactfx.util.FxTimer;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.PluginManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingWebWidget;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

public class ScriptingGistTab extends Tab implements EventHandler<Event>{
	
	private String Current_URL = "http://gist.github.com/";

	private ScriptingGistTab myTab;
	boolean loaded=false;
	boolean initialized=false;
	private WebView webView;
	private WebEngine webEngine;
	private VBox vBox;
	private Button goButton = new Button("Go");
	private Button homeButton = new Button("Home");
	private Button backButton = new Button("<");
	private Button forwardButton = new Button(">");
	
	private TextField urlField;
	//private String currentAddress;
	private ScriptingWebWidget scripting;
    final static SplashScreen splash = SplashScreen.getSplashScreen();
    private Graphics2D splashGraphics;
    private static boolean firstBoot=true;

	private boolean isTutorialTab =false;

	private boolean finishedLoadingScriptingWidget;

	public ScriptingGistTab(String title, String Url) throws IOException, InterruptedException{
		this(title,Url,false);
	}
	
	public ScriptingGistTab(String title, String Url,boolean isTutorialTab) throws IOException, InterruptedException{



		this.isTutorialTab = isTutorialTab;
		myTab = this;

		if(title==null)
			myTab.setText("               ");
		else
			myTab.setText(title);
		Log.debug("Loading Gist Tab: "+Url);
		webView = new WebView();
		webEngine = webView.getEngine();
		//webEngine.setUserAgent("bowlerstudio");
		if(Url!=null)
			Current_URL=Url;
	    if (splash != null) {
	    	try{
	        splashGraphics = splash.createGraphics();
	    	}catch (IllegalStateException e){}
	    }
	    
		
		loaded=false;
		setOnCloseRequest(this);
		webEngine.getLoadWorker().workDoneProperty().addListener((ChangeListener<Number>) (observableValue, oldValue, newValue) -> Platform.runLater(() -> {
		    if(!(newValue.intValue()<100)){
		    	//System.err.println("Just finished! "+webEngine.getLocation());
		    	
	    		new Thread(){
	    			public void run(){
	    				if(!initialized){
	    		    		initialized=true;
	    		    		loaded=true;
							setName("Start finalizing components");
		    				finishLoadingComponents();
	    				}
	    				
	    				else{
	    					try {
	    						scripting.loadCodeFromGist(Current_URL, webEngine);
	    					} catch (Exception e) {
	    						// TODO Auto-generated catch block
	    						//e.printStackTrace();
	    					} 
	    				}
	    			}
    			}.start();
		    	
    			
		    }else{
		    	loaded=false;
		    	if(splashGraphics!=null && splash.isVisible()){
		    		//BowlerStudio.renderSplashFrame(splashGraphics, newValue.intValue());
		            //splash.update();
		    	}
		    	//System.err.println("Not Done Loading to: "+webEngine.getLocation());
		    }
		}));
		urlField = new TextField(Current_URL);
		webEngine.locationProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable1,String oldValue, String newValue) {
				
						//System.out.println("Location Changed: "+newValue);
						Platform.runLater(() -> {
							urlField.setText(newValue);
						});
			}
		});
		
		//goButton.setDefaultButton(true);
	
		webEngine.getLoadWorker().stateProperty().addListener(
				new ChangeListener<Object>() {
					public void changed(ObservableValue<?> observable,
							Object oldValue, Object newValue) {
						State oldState = (State)oldValue;
						State newState = (State)newValue;
						if (State.SUCCEEDED == newValue) {
							Current_URL = urlField.getText().startsWith("http://")|| urlField.getText().startsWith("https://")
									? urlField.getText() 
									: "http://" + urlField.getText();
									
							Log.debug("Load Worker State Changed "+Current_URL);	
							if( !processNewTab(urlField.getText())){
								goBack();
							}
						}else{
							//Log.error("State load fault: "+newValue+" object:" +observable);
						}
					}
				});
		backButton.setOnAction(arg0 -> {
			goBack();
		});
		forwardButton.setOnAction(arg0 -> {
			// TODO Auto-generated method stub
			goForward();
		});
		homeButton.setOnAction(arg0 -> {
			// TODO Auto-generated method stub
			loadUrl("http://neuronrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/");
		});

		// Layout logic
		HBox hBox = new HBox(5);
		hBox.getChildren().setAll(backButton,forwardButton,homeButton,goButton,urlField);
		HBox.setHgrow(urlField, Priority.ALWAYS);

		vBox = new VBox(5);
		vBox.getChildren().setAll(hBox, webView);
		VBox.setVgrow(webView, Priority.ALWAYS);

		myTab.setContent(vBox);
		//Action definition for the Button Go.
		EventHandler<ActionEvent> goAction = event -> {
			Log.debug("Hitting load");
			if(processNewTab(urlField.getText())){
				Log.debug("Loading "+Current_URL);
				loadUrl(	Current_URL);
			}
		};
		urlField.setOnAction(goAction);
		goButton.setOnAction(goAction);
		//Once all components are loaded, load URL
		FxTimer.runLater(
				Duration.ofMillis(200) ,() -> goAction.handle(null));
		
		
		
	}
	
	
	public void loadUrl(String url){
		
		if(processNewTab(Current_URL)){
			Platform.runLater(() -> {
				webEngine.load(url);
			});
		}
	}

	
	private boolean processNewTab(String url){
		Current_URL = urlField.getText().startsWith("http://") || urlField.getText().startsWith("https://")
				? urlField.getText() 
				: "http://" + urlField.getText();
		if(isTutorialTab ){
			if(!(Current_URL.contains("neuronrobotics.com") || Current_URL.contains("gist.github.com/"+ScriptingEngine.getLoginID()) )){
				try {
					Log.debug("Non demo page found, opening new tab "+Current_URL);
					BowlerStudioController.getBowlerStudio().addTab(new ScriptingGistTab(null, Current_URL), true);
					return false;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			Log.debug("no load new tab");
			if(scripting!=null){
				try{
					myTab.setText(scripting.getFileName());
				}catch(java.lang.NullPointerException ex){
					try {
						scripting.loadCodeFromGist(Current_URL, webEngine);
						myTab.setText(scripting.getFileName());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}			
			}
		}
		return true;
	}
	
	
	
	
	
	private void finishLoadingComponents(){
		//System.err.println("Finalizing: "+webEngine.getLocation());
		try{
			if(splashGraphics!=null && splash.isVisible()){
	    		splash.close();
	    		splashGraphics=null;
	    	}
			if(scripting!=null){
				//when navagating to a new file, stop the script that is running
				scripting.stop();
			}
		}catch(Exception E){
			E.printStackTrace();
		}
		new Thread() {
			public void run() {
				finishedLoadingScriptingWidget=false;
				try{
					scripting = new ScriptingWebWidget( null ,Current_URL, webEngine);
					Platform.runLater(() -> {
						vBox.getChildren().add(scripting);
						if(!isTutorialTab){
							Platform.runLater(()->{
								try{
									
									myTab.setText(scripting.getFileName());
								}catch(java.lang.NullPointerException ex){
									// web page contains no gist
									ex.printStackTrace();
									myTab.setText("Web");
								}
								finishedLoadingScriptingWidget=true;
							});
						}
						else
							finishedLoadingScriptingWidget=true;
					});
					
				}catch(Exception ex){
					ex.printStackTrace();
					finishedLoadingScriptingWidget=true;
				}
				
				while(!finishedLoadingScriptingWidget){
					ThreadUtil.wait(10);
				}
				System.out.println("Loading code from "+Current_URL);
				try {
					scripting.loadCodeFromGist(Current_URL, webEngine);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(firstBoot){
					firstBoot=false;
					//now that the application is totally loaded check for connections to add
		
					new Thread() {
						public void run() {
							setName("Get First Connection");
							List<String> devs = SerialConnection.getAvailableSerialPorts();
							if (devs.size() == 0) {
								return;
							} else {
								DeviceManager.addConnection();
							}
						}
					}.start();
				}
			}
		}.start();
	}
	
    public String goBack()
    {    
      final WebHistory history=webEngine.getHistory();
      ObservableList<WebHistory.Entry> entryList=history.getEntries();
      int currentIndex=history.getCurrentIndex();
//      Out("currentIndex = "+currentIndex);
//      Out(entryList.toString().replace("],","]\n"));

      Platform.runLater(() ->{
    	  try{
    		  history.go(-1);
    	  }catch(Exception e){
    		  e.printStackTrace();
    	  }
      });
      return entryList.get(currentIndex>0?currentIndex-1:currentIndex).getUrl();
    }

    public String goForward()
    {    
      final WebHistory history=webEngine.getHistory();
      ObservableList<WebHistory.Entry> entryList=history.getEntries();
      int currentIndex=history.getCurrentIndex();
//      Out("currentIndex = "+currentIndex);
//      Out(entryList.toString().replace("],","]\n"));

      Platform.runLater(() -> 
      history.go(1));
      return entryList.get(currentIndex<entryList.size()-1?currentIndex+1:currentIndex).getUrl();
    }
	

	public static String getDomainName(String url) throws URISyntaxException {
	    URI uri = new URI(url);
	    String domain = uri.getHost();
	    return domain.startsWith("www.") ? domain.substring(4) : domain;
	}

	@Override
	public void handle(Event event) {
		if(scripting!=null)
			scripting.stop();
	}

	
}
