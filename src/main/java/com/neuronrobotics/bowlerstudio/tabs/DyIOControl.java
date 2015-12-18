package com.neuronrobotics.bowlerstudio.tabs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.utils.BowlerStudioResourceFactory;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.dyio.DyIO;

public class DyIOControl extends AbstractBowlerStudioTab {

	private DyIO dyio;
	DyIOPanel controller ;
	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return new String[]{"neuronrobotics.dyio.*"};
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		this.dyio = (DyIO)pm;
		
		
		setText(dyio.getScriptingName());
		FXMLLoader fxmlLoader = BowlerStudioResourceFactory.getMainPanel();
        Parent root = fxmlLoader.getRoot();
        controller = fxmlLoader.getController();
        controller.setDyIO(dyio);
        setContent(root);
	}
	

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub

	}

}
