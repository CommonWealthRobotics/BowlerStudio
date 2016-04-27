package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan Benasutti on 2/6/2016.
 */

public class AddFileToGistController extends Application
{
    @FXML
    public ListView<String> gistListView;

    @FXML
    public TextField filenameField;

    @FXML
    public Button addFileButton, cancelButton;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("Add file to Gist");
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("addFileToGist.fxml"));
        Parent root = loader.load();
        new Thread(() -> {
            GitHub gitHub = ScriptingEngine.getGithub();
            while (gitHub == null)
            {
                gitHub = ScriptingEngine.getGithub();
                ThreadUtil.wait(20);
            }

            try
            {
                GHMyself myself = gitHub.getMyself();
                PagedIterable<GHGist> gists = myself.listGists();
                List<String> gistList = new ArrayList<>();
                for (GHGist gist : gists)
                    gistList.add(gist.getDescription());
                ObservableList<String> observableGistList = FXCollections.observableArrayList("One", "Two"); //FXCollections.observableList(gistList);
                //Platform.runLater(() -> gistListView = new ListView<>(observableGistList));
                Platform.runLater(() -> {
                    for (String s : observableGistList)
                    {
                        gistListView.getItems().add(s);
                    }
                });
                //throw new IllegalStateException(observableGistList.toString());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }).start();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.initModality(Modality.WINDOW_MODAL);
        primaryStage.show();
    }

    @FXML
    public void onAddFile(ActionEvent actionEvent)
    {

    }

    @FXML
    public void onCancel(ActionEvent actionEvent)
    {
        Platform.runLater(() -> {
            Stage stage = (Stage)cancelButton.getScene().getWindow();
            stage.close();
        });
    }
}
