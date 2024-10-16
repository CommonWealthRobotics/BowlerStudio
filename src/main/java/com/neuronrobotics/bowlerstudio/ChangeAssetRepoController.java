package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;

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

	@SuppressWarnings("restriction")
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = AssetFactory.loadLayout("layout/changeAssetRepo.fxml", true);
		Parent root;
		loader.setController(this);
		// This is needed when loading on MAC
		loader.setClassLoader(getClass().getClassLoader());
		root = loader.load();
		FontSizeManager.addListener(fontNum->{
			int tmp = fontNum-10;
			if(tmp<12)
				tmp=12;
			root.setStyle("-fx-font-size: "+tmp+"pt");
		});
		BowlerStudio.runLater(() -> {
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
		String repo =  repoField.getText()
                .replaceAll("git://", "https://");
		new Thread(()->{
	        ConfigurationDatabase.setObject("BowlerStudioConfigs", "skinRepo",
	        		repo);
			ConfigurationDatabase.save();

		}).start();

        Stage stage = (Stage) changeRepoButton.getScene().getWindow();
        stage.close();
        
    }

	@FXML
	public void onCancel(ActionEvent event) {
		BowlerStudio.runLater(() -> {
			Stage stage = (Stage) cancelButton.getScene().getWindow();
			stage.close();
		});
	}
}
