package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.tabs.DyIOPanel;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.python.antlr.op.Add;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Ryan Benasutti on 2/6/2016.
 */

public class AddFileToGistController extends Application
{
    @FXML
    public TextField filenameField;

    @FXML
    public Button addFileButton, cancelButton;

    private String gistID;

    public AddFileToGistController()
    {
        this.gistID = MainController.currentGistID;
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader loader = AssetFactory.loadLayout("layout/addFileToGist.fxml", true);
        Parent root;

        loader.setClassLoader(getClass().getClassLoader());
        root = loader.load();

        Platform.runLater(() -> {
            primaryStage.setTitle("Add File to Gist");

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.initModality(Modality.WINDOW_MODAL);
            primaryStage.setResizable(true);
            primaryStage.show();
        });
    }

    @FXML
    public void onAddFile(ActionEvent event)
    {
        GistHelper.addFileToGist(filenameField.getText(), "//Your code here", gistID);

        Platform.runLater(() -> {
            Stage stage = (Stage) addFileButton.getScene().getWindow();
            stage.close();
        });
    }

    @FXML
    public void onCancel(ActionEvent event)
    {
        Platform.runLater(() -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
    }

    public String getGistID()
    {
        return gistID;
    }

    public void setGistID(String gistID)
    {
        this.gistID = gistID;
    }
}
