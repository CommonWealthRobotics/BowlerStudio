package com.neuronrobotics.bowlerstudio.scripting;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;



public class GithubLoginFX implements javafx.fxml.Initializable {

	@FXML TextField username;
	@FXML PasswordField password;
	
	private boolean done=false;
	
	private String [] creds = new String[]{"",""};
	private Stage stage;
	private Parent root;
	private Scene scene;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		reset();
	}
	
	public void reset(){
		done=false;
		setCreds(new String[]{"",""});
		password.clear();
		username.clear();
		

	}

	@FXML public void anonMode() {
		setCreds(null);
		done=true;
		stage.close();
	}

	@FXML public void login() {
		getCreds()[0]= username.getText();
		getCreds()[1]= password.getText();
		if(getCreds()[0]==null||getCreds()[1]==null){
			setCreds(null);
		}else if(getCreds()[0].equals("")||getCreds()[1].equals("")){
			setCreds(null);
		}
		done=true;
		stage.close();

	}

	@FXML public void focusOnPw() {
		password.requestFocus();
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public String [] getCreds() {
		return creds;
	}

	public void setCreds(String [] creds) {
		this.creds = creds;
	}

	public void setStage(Stage stage, Parent root) {
		this.stage = stage;
		if(this.root==null){
			this.root = root;
			scene=  new Scene(root);
		}
		stage.setScene(scene);  
	}


}
