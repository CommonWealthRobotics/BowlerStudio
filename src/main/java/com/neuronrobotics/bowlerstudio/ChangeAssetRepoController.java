package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Created by Ryan Benasutti on 2/6/2016.
 */

public class ChangeAssetRepoController extends Application {
	@FXML
	private TextField repoField;

	@FXML
	private Button changeRepoButton, cancelButton;

	public ChangeAssetRepoController() {}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = AssetFactory.loadLayout("layout/changeAssetRepo.fxml", true);
		Parent root;
		loader.setController(this);
		// This is needed when loading on MAC
		loader.setClassLoader(getClass().getClassLoader());
		root = loader.load();

		Platform.runLater(() -> {
			primaryStage.setTitle("Change Asset Repository");

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.setResizable(true);
			primaryStage.show();
		});
	}

	@FXML
	public void onChangeRepo(ActionEvent event)
    {
        ConfigurationDatabase.setObject("BowlerStudioConfigs", "skinRepo",
                repoField.getText()
                .replaceAll("git://", "https://"));
        Stage stage = (Stage) changeRepoButton.getScene().getWindow();
        stage.close();
    }

	@FXML
	public void onCancel(ActionEvent event) {
		Platform.runLater(() -> {
			Stage stage = (Stage) cancelButton.getScene().getWindow();
			stage.close();
		});
	}
}
