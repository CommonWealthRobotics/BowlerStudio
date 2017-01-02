package com.neuronrobotics.bowlerstudio.utils;
/**
 * Sample Skeleton for 'findWidget.fxml' Controller Class
 */

import java.awt.Rectangle;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FindTextWidget extends Application {

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="matchCase"
	private CheckBox matchCase; // Value injected by FXMLLoader

	@FXML // fx:id="findBox"
	private TextField findBox; // Value injected by FXMLLoader

	@FXML // fx:id="replaceBox"
	private TextField replaceBox; // Value injected by FXMLLoader

	private Stage primaryStage;

	private RSyntaxTextArea textArea;
	private int pos = 0;

	private int find(double direction) {
		// SwingUtilities.invokeLater(() -> {
		try {
			// System.out.println("Got ctrl f "+
			// textArea.getSelectedText());
			// Get the text to find...convert it to
			// lower
			// case for eaiser comparision

			String find = findBox.getText();
			if (!matchCase.isSelected()) {
				find = find.toLowerCase();
			}
			// Focus the text area, otherwise the
			// highlighting won't show up
			textArea.requestFocusInWindow();
			// Make sure we have a valid search term
			if (find != null && find.length() > 0) {
				Document document = textArea.getDocument();
				int findLength = find.length();
				try {
					boolean found = false;
					// Rest the search position if we're
					// at
					// the end of the document
					if (pos + findLength > document.getLength()) {
						pos = 0;
					}
					if (pos < 0) {
						pos = document.getLength() - findLength;
					}
					// While we haven't reached the
					// end...
					// "<=" Correction
					while (pos + findLength <= document.getLength() && pos >= 0) {
						// Extract the text from teh
						// docuemnt
						String match = document.getText(pos, findLength);
						if (!matchCase.isSelected()) {
							match = match.toLowerCase();
						}
						// Check to see if it matches or
						// request
						if (match.equals(find)) {
							found = true;
							break;
						}
						pos += 1 * direction;
					}
					int baseOfFind = pos;
					// Did we find something...
					if (found) {
						SwingUtilities.invokeLater(() -> {
							// Get the rectangle of the
							// where
							// the text would be visible...
							Rectangle viewRect;
							try {
								viewRect = textArea.modelToView(pos);
								// Scroll to make the rectangle
								// visible
								textArea.scrollRectToVisible(viewRect);
								// Highlight the text
								textArea.setCaretPosition((int) (pos));
								textArea.moveCaretPosition(pos + findLength);
								// Move the search position
								// beyond
								// the current match
								pos += findLength * direction;
							} catch (BadLocationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						});
					}
					return baseOfFind;

				} catch (Exception exp) {
					exp.printStackTrace();
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// });
		return pos;
	}

	private void replace(double direction) {
		new Thread(() -> {
			String find = findBox.getText();
			String replace = replaceBox.getText();
			if(replace==null)
				replace="";
			String current = textArea.getText();
			int intLengthOfRemove = find.length();

			if(pos>=intLengthOfRemove){
				String firstHalf = current.substring(0,pos-intLengthOfRemove);
				String secondtHalf = current.substring(pos );
				String newContent = firstHalf + replace + secondtHalf;
				SwingUtilities.invokeLater(() ->{
					textArea.setText(newContent);
					find(direction);
				});
			}else{
				find(direction);
			}
		}).start();
	}

	@FXML
	void findPrevious(ActionEvent event) {
		find(-1);
	}

	@FXML
	void findNext(ActionEvent event) {
		find(1);
	}

	@FXML
	void replaceNext(ActionEvent event) {
		replace(1);
	}

	@FXML
	void replacePrevious(ActionEvent event) {
		replace(-1);
	}

	@FXML // This method is called by the FXMLLoader when initialization is
			// complete
	void initialize() {
		assert matchCase != null : "fx:id=\"matchCase\" was not injected: check your FXML file 'findWidget.fxml'.";
		assert findBox != null : "fx:id=\"findBox\" was not injected: check your FXML file 'findWidget.fxml'.";
		assert replaceBox != null : "fx:id=\"replaceBox\" was not injected: check your FXML file 'findWidget.fxml'.";

		if (textArea.getSelectedText() != null) {
			Platform.runLater(() -> findBox.setText(textArea.getSelectedText()));
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		FXMLLoader loader = AssetFactory.loadLayout("layout/findWidget.fxml", true);
		Parent root;
		loader.setController(this);
		// This is needed when loading on MAC
		loader.setClassLoader(getClass().getClassLoader());
		root = loader.load();

		Platform.runLater(() -> {
			primaryStage.setTitle("Find/Replace");
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			// primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.setResizable(true);
			primaryStage.show();
		});
	}

	public void setTextArea(RSyntaxTextArea textArea) {
		this.textArea = textArea;
		pos = 0;
		// TODO Auto-generated method stub

	}
}
