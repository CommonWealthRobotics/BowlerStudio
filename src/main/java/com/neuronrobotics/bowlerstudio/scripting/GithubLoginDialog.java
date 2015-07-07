package com.neuronrobotics.bowlerstudio.scripting;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GithubLoginDialog extends Stage implements EventHandler<ActionEvent> {
    private TextField userNameFld;
	private PasswordField passwordFld;

	public GithubLoginDialog(Stage owner) {
        super();
        initOwner(owner);
        setTitle("title");
        Group root = new Group();
        Scene scene = new Scene(root, 350, 150, Color.WHITE);
        setScene(scene);

        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(5);
        gridpane.setVgap(5);

        Label userNameLbl = new Label("User Name: ");
        gridpane.add(userNameLbl, 0, 1);

        Label passwordLbl = new Label("Password: ");
        gridpane.add(passwordLbl, 0, 2);
        userNameFld = new TextField("");
        gridpane.add(userNameFld, 1, 1);

        passwordFld = new PasswordField();
        passwordFld.setText("");
        gridpane.add(passwordFld, 1, 2);

        Button login = new Button("Login");
        login.setOnAction(this);
        passwordFld.setOnAction(this);
        
        gridpane.add(login, 1, 3);
        GridPane.setHalignment(login, HPos.RIGHT);
        root.getChildren().add(gridpane);
    }
    
    String getUsername(){
    	if(userNameFld.getText().contains("@")){
    		Platform.runLater(()->userNameFld.setText("Username not email"));
    		return null;
    	}
    	return userNameFld.getText();
    }
    String getPw(){
    	return passwordFld.getText();
    }

	@Override
	public void handle(ActionEvent event) {
		// TODO Auto-generated method stub
		 close();
	}
}
