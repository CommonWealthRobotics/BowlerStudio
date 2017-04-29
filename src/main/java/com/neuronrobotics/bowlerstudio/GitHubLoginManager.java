package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.bowlerstudio.scripting.GithubLoginFX;
import com.neuronrobotics.bowlerstudio.scripting.IGitHubLoginManager;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class GitHubLoginManager implements IGitHubLoginManager {
    private boolean loginWindowOpen = false;
    private GithubLoginFX githublogin = null;
    private Stage stage;
    private boolean AnonSelected = false;
    private String[] creds;

    public GitHubLoginManager() {
    }

    @Override
    public String[] prompt(String username) {
        if (AnonSelected)
            return null;

        boolean loginWas = loginWindowOpen;

        if (stage == null) {
            if (!loginWas && githublogin != null)
                githublogin.reset();

            githublogin = null;
            System.err.println("Calling login from BowlerStudio");
            FXMLLoader fxmlLoader = BowlerStudioResourceFactory.getGithubLogin();
            Parent root = fxmlLoader.getRoot();
            if (githublogin == null) {
                githublogin = fxmlLoader.getController();
                Platform.runLater(() -> {
                    if (!loginWindowOpen) {
                        githublogin.reset();
                        githublogin.getUsername().setText(username);
                        stage = new Stage();
                        stage.setTitle("GitHub Login");
                        githublogin.setStage(stage, root);
                        stage.centerOnScreen();

                        loginWindowOpen = true;
                        stage.show();
                        stage = null;
                    }
                });
            }
        }

        while (!githublogin.isDone())
            ThreadUtil.wait(100);

        creds = githublogin.getCreds();

        if (creds == null)
            AnonSelected = true;

        loginWindowOpen = false;
        return creds;
    }
}
