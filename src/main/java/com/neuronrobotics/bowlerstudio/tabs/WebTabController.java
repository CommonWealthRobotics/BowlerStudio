/**
 * Sample Skeleton for 'WebTabLayout.fxml' Controller Class
 */

package com.neuronrobotics.bowlerstudio.tabs;

import java.net.URL;
import java.util.ResourceBundle;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.sdk.common.Log;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class WebTabController {
	private boolean initialized=false;

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="pane"
	private TabPane pane; // Value injected by FXMLLoader

	@FXML // fx:id="tab"
	private Tab tab; // Value injected by FXMLLoader

	@FXML // fx:id="back"
	private Button back; // Value injected by FXMLLoader

	@FXML // fx:id="forward"
	private Button forward; // Value injected by FXMLLoader

	@FXML // fx:id="home"
	private Button home; // Value injected by FXMLLoader

	@FXML // fx:id="refresh"
	private Button refresh; // Value injected by FXMLLoader

	@FXML // fx:id="urlField"
	private TextField urlField; // Value injected by FXMLLoader

	@FXML // fx:id="run"
	private Button run; // Value injected by FXMLLoader

	@FXML // fx:id="iconHolder"
	private VBox iconHolder; // Value injected by FXMLLoader

	@FXML // fx:id="copy"
	private Button copy; // Value injected by FXMLLoader

	@FXML // fx:id="fileCHoice"
	private ComboBox<String> fileCHoice; // Value injected by FXMLLoader

	@FXML // fx:id="scrollpane"
	private ScrollPane scrollpane; // Value injected by FXMLLoader

	@FXML // fx:id="webview"
	private WebView webview; // Value injected by FXMLLoader

	private WebEngine webEngine;

	private String Current_URL;

	private boolean isTutorialTab;

	@FXML
	void onCopy(ActionEvent event) {

	}

	@FXML
	void onFileSelect(ActionEvent event) {

	}

	@FXML
	void onRunStop(ActionEvent event) {

	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert back != null : "fx:id=\"back\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert forward != null : "fx:id=\"forward\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert home != null : "fx:id=\"home\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert refresh != null : "fx:id=\"refresh\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert urlField != null : "fx:id=\"urlField\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert run != null : "fx:id=\"run\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert iconHolder != null : "fx:id=\"iconHolder\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert copy != null : "fx:id=\"copy\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert fileCHoice != null : "fx:id=\"fileCHoice\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert scrollpane != null : "fx:id=\"scrollpane\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		assert webview != null : "fx:id=\"webview\" was not injected: check your FXML file 'WebTabLayout.fxml'.";
		webEngine = webview.getEngine();
		FontSizeManager.addListener(fontNum -> {
			double scale = ((double) fontNum - 10) / 12.0;
			if (scale < 1)
				scale = 1;
			webview.setScaleX(scale);
			webview.setScaleY(scale);
		});
		
		
		setInitialized(true);
	}
	public void loadUrl(String url){

			BowlerStudio.runLater(() -> {
				webEngine.load(url);
				System.out.println("Go TO URL "+url);
			});
		
	}
	private boolean processNewTab(){
		Current_URL = urlField.getText().startsWith("http://") || urlField.getText().startsWith("https://") || urlField.getText().startsWith("file:")
				? urlField.getText() 
				: "http://" + urlField.getText();
		if(isTutorialTab() ){
			if(		!((Current_URL.toLowerCase().contains("commonwealthrobotics.com") ||
					Current_URL.contains("gist.github.com/"+PasswordManager.getUsername() )||
					Current_URL.contains("localhost") ))){
				try {
					
					Log.error("Non demo page found, opening new tab "+Current_URL);
					BowlerStudioController.getBowlerStudio().addTab(new WebTab(null, Current_URL), true);
					return false;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			Log.debug("no load new tab");
//			if(getScripting()!=null){
//				try{
//					myTab.setText(getScripting().getFileName());
//				}catch(java.lang.NullPointerException ex){
//					try {
//						getScripting().loadCodeFromGist(Current_URL, webEngine);
//						myTab.setText(getScripting().getFileName());
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}			
//			}
		}
		return true;
	}
	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @param initialized the initialized to set
	 */
	private void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * @return the isTutorialTab
	 */
	public boolean isTutorialTab() {
		return isTutorialTab;
	}

	/**
	 * @param isTutorialTab the isTutorialTab to set
	 * @return 
	 */
	public WebTabController setTutorialTab(boolean isTutorialTab) {
		this.isTutorialTab = isTutorialTab;
		return this;
	}



}
