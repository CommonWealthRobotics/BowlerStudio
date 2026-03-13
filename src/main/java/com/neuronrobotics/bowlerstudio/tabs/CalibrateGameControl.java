package com.neuronrobotics.bowlerstudio.tabs;

import java.io.File;
import java.net.URL;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.gamepad.JogTrainerWidget;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class CalibrateGameControl extends AbstractBowlerStudioTab {

	private JogTrainerWidget w;

	@Override
	public void onTabClosing() {
		// Auto-generated method stub

	}

	@Override
	public String[] getMyNameSpaces() {
		// Auto-generated method stub
		return new String[0];
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		BowlerJInputDevice dev = (BowlerJInputDevice) pm;

		File fxmlFIle;
		try {
			fxmlFIle = AssetFactory.loadFile("layout/jogTrainerWidget.fxml");
			URL fileURL = fxmlFIle.toURI().toURL();
			FXMLLoader loader = new FXMLLoader(fileURL);
			loader.setLocation(fileURL);
			Parent root;
			w = new JogTrainerWidget(dev);
			loader.setController(w);
			// This is needed when loading on MAC
			loader.setClassLoader(JogTrainerWidget.class.getClassLoader());
			root = loader.load();
			FontSizeManager.addListener(fontNum -> {
				int tmp = fontNum - 10;
				if (tmp < 12)
					tmp = 12;
				root.setStyle("-fx-font-size: " + tmp + "pt");
			});
			setContent(root);
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}

		setText("Calibrate Game Control");
		onTabReOpening();
	}

	@Override
	public void onTabReOpening() {

	}

}
