package com.neuronrobotics.bowlerstudio.scripting;

import java.util.concurrent.CompletableFuture;

import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class AskToDeleteWidget {
	public static boolean askToDeleteFile(String name) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();

		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("File Exists");
			alert.setHeaderText(name);
			alert.setContentText("Delete existing and replace?");

			ButtonType yes = new ButtonType("Yes");
			ButtonType no = new ButtonType("No");

			alert.getButtonTypes().setAll(yes, no);
			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			stage.setOnCloseRequest(event -> alert.hide());
			Node root = alert.getDialogPane();
			FontSizeManager.addListener(fontNum -> {
				int tmp = fontNum - 10;
				if (tmp < 12)
					tmp = 12;
				root.setStyle("-fx-font-size: " + tmp + "pt");
				alert.getDialogPane().applyCss();
				alert.getDialogPane().layout();
				stage.sizeToScene();
			});
			boolean result = alert.showAndWait().map(response -> {
				if (response == yes)
					return true;
				if (response == no)
					return false;
				return null;
			}).orElse(null);

			future.complete(result);
		});

		try {
			return future.get();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
