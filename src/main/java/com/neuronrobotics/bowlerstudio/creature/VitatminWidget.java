/**
 * Sample Skeleton for 'AddRemoveVitamins.fxml' Controller Class
 */

package com.neuronrobotics.bowlerstudio.creature;

import java.awt.event.InputMethodEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.google.crypto.tink.subtle.EngineWrapper.TMac;
import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import com.neuronrobotics.sdk.addons.kinematics.IVitaminHolder;
import com.neuronrobotics.sdk.addons.kinematics.VitaminFrame;
import com.neuronrobotics.sdk.addons.kinematics.VitaminLocation;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Affine;

public class VitatminWidget implements IOnTransformChange {

	private String selectedType = null;
	private String sizeSelected = null;
	private TransformWidget tf;

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="add"
	private Button add; // Value injected by FXMLLoader

	@FXML // fx:id="name"
	private TextField name; // Value injected by FXMLLoader

	@FXML // fx:id="type"
	private ComboBox<String> type; // Value injected by FXMLLoader

	@FXML // fx:id="size"
	private ComboBox<String> size; // Value injected by FXMLLoader
	@FXML // fx:id="listOfItems"
	private ListView<GridPane> listOfItems; // Value injected by FXMLLoader
	@FXML
	private ComboBox<VitaminFrame> frameType;
	@FXML // fx:id="transformPanel"
	private AnchorPane transformPanel; // Value injected by FXMLLoader

	private IVitaminHolder holder;
	private HashMap<GridPane, VitaminLocation> locationMap = new HashMap<>();
	private VitaminLocation selectedVitamin;
	private ITransformProvider currentTipProvider;

	@FXML
	void onAdd(ActionEvent event) {
		VitaminLocation newVit = new VitaminLocation(name.getText(), selectedType, sizeSelected, new TransformNR());
		holder.addVitamin(newVit);
		add(newVit);
		validateInput();
	}

	private void add(VitaminLocation newVit) {
		GridPane box = new GridPane();
		box.getColumnConstraints().add(new ColumnConstraints(30)); // translate text
		box.getColumnConstraints().add(new ColumnConstraints(120)); // translate values
		box.getColumnConstraints().add(new ColumnConstraints(120)); // units
		box.getColumnConstraints().add(new ColumnConstraints(120)); // rotate text
		box.setHgap(20);// gab between elements
		box.setVgap(10);// gab between elements
		locationMap.put(box, newVit);
		Button remove = new Button();
		remove.setGraphic(AssetFactory.loadIcon("Clear-Screen.png"));
		remove.setOnAction(action -> {
			listOfItems.getSelectionModel().clearSelection();
			listOfItems.getItems().remove(box);
			transformPanel.setDisable(true);
			frameType.setDisable(true);
			holder.removeVitamin(newVit);
			validateInput();
			locationMap.remove(box);
		});
		box.add(remove, 0, 0);
		box.add(new Label(newVit.getName()), 1, 0);
		box.add(new Label(newVit.getType()), 2, 0);
		box.add(new Label(newVit.getSize()), 3, 0);

		listOfItems.getItems().add(box);
	}

	void validateInput() {
		add.setDisable(true);
		String nameTmp = name.getText();
		System.out.println("Validating " + nameTmp);

		if (nameTmp.length() == 0)
			return;
		if (selectedType == null)
			return;
		if (sizeSelected == null)
			return;
		for (VitaminLocation l : holder.getVitamins()) {
			String name2 = l.getName();
			if (name2.contentEquals(nameTmp))
				return;
			//System.out.println(nameTmp + " is not " + name2);
		}
		add.setDisable(false);
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert add != null : "fx:id=\"add\" was not injected: check your FXML file 'AddRemoveVitamins.fxml'.";
		assert name != null : "fx:id=\"name\" was not injected: check your FXML file 'AddRemoveVitamins.fxml'.";
		assert type != null : "fx:id=\"type\" was not injected: check your FXML file 'AddRemoveVitamins.fxml'.";
		assert size != null : "fx:id=\"size\" was not injected: check your FXML file 'AddRemoveVitamins.fxml'.";
		List<String> types = Vitamins.listVitaminTypes().stream().sorted().collect(Collectors.toList());
		for (String s : types) {
			type.getItems().add(s);
		}
		type.setOnAction(action -> {
			add.setDisable(true);
			size.getItems().clear();
			selectedType = type.getSelectionModel().getSelectedItem();
			sizeSelected = null;
			List<String> sizes = Vitamins.listVitaminSizes(selectedType).stream().sorted().collect(Collectors.toList());
			for (String s : sizes) {
				size.getItems().add(s);
			}
		});
		size.setOnAction(action -> {
			add.setDisable(true);
			sizeSelected = size.getSelectionModel().getSelectedItem();
			validateInput();
		});
		name.textProperty().addListener((observable, oldValue, newValue) -> {
			validateInput();
		});
		listOfItems.getSelectionModel().selectedItemProperty().addListener((ob, old, fresh) -> {
			selectedVitamin = locationMap.get(fresh);
			if (selectedVitamin != null) {
				fireVitaminSelectedUpdate();
			}
		});
		tf = new TransformWidget("Vitamin Location", new TransformNR(), this);
		transformPanel.getChildren().add(tf);
		transformPanel.setDisable(true);
		frameType.setDisable(true);
		frameType.setOnAction(event->{
			if(frameType.getValue()!=selectedVitamin.getFrame())
				selectedVitamin.setFrame(frameType.getValue());
		});
		for(VitaminFrame vf:VitaminFrame.values()) {
			frameType.getItems().add(vf);
		}
	}

	private void fireVitaminSelectedUpdate() {
		System.out.println("Selected " + selectedVitamin.getName());
		name.setText(selectedVitamin.getName());
		type.getSelectionModel().select(selectedVitamin.getType());
		size.getSelectionModel().select(selectedVitamin.getSize());
		
		tf.updatePose(selectedVitamin.getLocation());
		transformPanel.setDisable(false);
		frameType.setDisable(false);
		frameType.getSelectionModel().select(selectedVitamin.getFrame());
		MobileBaseCadManager manager = MobileBaseCadManager.get(holder);
		try {
			Affine af = manager.getVitaminAffine(selectedVitamin);
			TransformNR poseToMove = currentTipProvider.get().copy();
			//poseToMove.setRotation(new RotationNR());
			BowlerStudioController.targetAndFollow(poseToMove,af);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	public void setVitaminProvider(IVitaminHolder h,ITransformProvider currentTipProvider) {
		this.holder = h;
		this.currentTipProvider = currentTipProvider;
		for (VitaminLocation l : h.getVitamins()) {
			add(l);
		}
	}

	@Override
	public void onTransformChaging(TransformNR newTrans) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTransformFinished(TransformNR newTrans) {
		selectedVitamin.setLocation(newTrans);
	}
}
