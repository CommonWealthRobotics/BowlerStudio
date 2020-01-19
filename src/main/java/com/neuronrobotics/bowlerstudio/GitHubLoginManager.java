package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.BowlerStudioResourceFactory;
import com.neuronrobotics.bowlerstudio.scripting.GithubLoginFX;
import com.neuronrobotics.bowlerstudio.scripting.IGitHubLoginManager;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GitHubLoginManager implements IGitHubLoginManager {
  private boolean loginWindowOpen = false;
  private GithubLoginFX githublogin = null;
  private Stage stage;
  private boolean AnonSelected = false;
  private String[] creds;

  public GitHubLoginManager() {}

  @SuppressWarnings("restriction")
  @Override
  public String[] prompt(String username) {
    if (AnonSelected) {
      return null;
    }
    boolean loginWas = loginWindowOpen;

    if (githublogin != null) githublogin.reset();
    if (stage == null) {
      if (!loginWas && githublogin != null) githublogin.reset();
      githublogin = null;
      // System.out.println("Calling login from BowlerStudio "+username);
      // new RuntimeException().printStackTrace();
      FXMLLoader fxmlLoader = BowlerStudioResourceFactory.getGithubLogin();
      if (fxmlLoader == null) return null;
      Parent root = fxmlLoader.getRoot();
      if (githublogin == null) {
        githublogin = fxmlLoader.getController();
        githublogin.reset();
        Platform.runLater(
            () -> {
              if (!loginWindowOpen) {
                githublogin.reset();
                githublogin.getUsername().setText(username);
                stage = new Stage();
                stage.setTitle("GitHub Login");
                // stage.initModality(Modality.APPLICATION_MODAL);
                githublogin.setStage(stage, root);
                stage.centerOnScreen();

                loginWindowOpen = true;
                stage.setAlwaysOnTop(true);
                stage.show();

                stage = null;
              }
            });
      }
    }
    // setContent(root);
    do {
      // System.err.println("Waiting for user login");

      ThreadUtil.wait(100);
    } while (!githublogin.isDone());
    creds = githublogin.getCreds();
    // System.err.println(" login = "+creds);
    if (creds == null) {
      // System.err.println("Anon mode");
      AnonSelected = true;
    }
    loginWindowOpen = false;
    return creds;
  }

  @Override
  public String twoFactorAuthCodePrompt() {
    JFrame jframe = new JFrame();
    String answer = JOptionPane.showInputDialog(jframe, "Enter 2 factor auth code:");
    jframe.dispose();
    return answer;
  }
}
