package com.neuronrobotics.bowlerstudio.creature;

import org.jfree.util.Log;

import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.addons.kinematics.parallel.ParallelGroup;

import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ParallelWidget extends Group {
	VBox box = new VBox();
	VBox relativeToControls = new VBox();
    CheckBox useRelative = new CheckBox("This limb is relative to another link");
	TransformWidget e;
	TextField groupName = new TextField();
	ComboBox<String> relName = new ComboBox<String>();
	ComboBox<Integer> relIndex = new ComboBox<Integer>();
	TransformNR robotToFiducialTransform = new TransformNR();
	private MobileBase base;
	private DHParameterKinematics dh;
	private CreatureLab creatureLab;
	boolean resetting = false;

	private HBox row(String label, Node tf) {
		HBox h = new HBox();
		h.getChildren().add(new Label(label));
		h.getChildren().add(tf);
		return h;
	}

	public ParallelWidget(MobileBase b, DHParameterKinematics d, CreatureLab c) {
		Platform.runLater(() -> getChildren().add(box));

		this.base = b;
		this.dh = d;
		this.creatureLab = c;
		useRelative.setSelected(false);
		useRelative.setOnAction(event -> {
			relativeToControls.setDisable(!useRelative.isSelected());
			if(useRelative.isSelected()) {
				setupAddReferenceSection();
			}else {
				getGroup().clearReferencedLimb(dh);
			}
		});

		groupName.setOnAction(event -> {
			if (resetting)
				return;
			if (getGroup() != null) {
				base.shutDownParallel(getGroup());
			}
			if (groupName.getText().length() > 0) {
				setupAddReferenceSection();
			} else {
				relName.setDisable(true);
				relIndex.setDisable(true);
				e.setDisable(true);
			}
		});

		relName.setOnAction(event -> {
			if (resetting)
				return;
			String refLimbName = relName.getValue();
			setNewReferencedLimb(base, refLimbName);
			getGroup().setupReferencedLimb(dh, robotToFiducialTransform, relName.getValue(), 0);
			relIndex.setDisable(false);
		});
		relIndex.setOnAction(event -> {
			if (resetting)
				return;
			getGroup().setupReferencedLimb(dh, robotToFiducialTransform, relName.getValue(), relIndex.getValue());
			e.setDisable(false);
		});

		Platform.runLater(() -> box.getChildren().add(row("Parallel Group Name", groupName)));
		Platform.runLater(() -> box.getChildren().add(useRelative));
		Platform.runLater(() -> box.getChildren().add(relativeToControls));

		Platform.runLater(() -> relativeToControls.getChildren().add(row("Limb Relative", relName)));
		Platform.runLater(() -> relativeToControls.getChildren().add(row("Limb Relative index", relIndex)));

		e = new TransformWidget("Parallel Tip Offset", robotToFiducialTransform, new IOnTransformChange() {

			@Override
			public void onTransformFinished(TransformNR newTrans) {
				if (resetting)
					return;
				// Force a cad regeneration
				creatureLab.onSliderDoneMoving(null, 0);
			}

			@Override
			public void onTransformChaging(TransformNR newTrans) {
				if (resetting)
					return;
				robotToFiducialTransform = newTrans;
				System.out.println("Tip offset for "+dh.getScriptingName()+" "+newTrans);
				getGroup().setTipOffset(dh, newTrans);
				dh.refreshPose();
			}
		});

		Platform.runLater(() -> relativeToControls.getChildren().add(e));
	}

	private void setupAddReferenceSection() {
		base.getParallelGroup(groupName.getText()).addLimb(dh, null, "", 0);
		relName.getItems().clear();
		for (DHParameterKinematics l : base.getAllDHChains()) {
			if (!l.getScriptingName().contentEquals(dh.getScriptingName())) {
				relName.getItems().add(l.getScriptingName());
			}
		}
		relName.setDisable(false);
		relIndex.setDisable(true);
		e.setDisable(true);
	}

	public void configure(MobileBase b, DHParameterKinematics dh, CreatureLab creatureLab) {
		resetting = true;
		this.base = b;
		this.dh = dh;
		this.creatureLab = creatureLab;
		System.out.println("Configuring arm " + dh.getScriptingName());
		robotToFiducialTransform = new TransformNR();
		Platform.runLater(() -> groupName.setText(""));
		Platform.runLater(() -> relName.getItems().clear());
		Platform.runLater(() -> relIndex.getItems().clear());
		Platform.runLater(() -> relName.setDisable(true));
		Platform.runLater(() -> relIndex.setDisable(true));
		Platform.runLater(() -> e.setDisable(true));

		if (getGroup() == null) {
			Platform.runLater(() -> relName.setDisable(true));
			Platform.runLater(() -> relIndex.setDisable(true));
			Platform.runLater(() -> e.setDisable(true));
		} else {
			Platform.runLater(() -> groupName.setText(getGroup().getNameOfParallelGroup()));
			for (DHParameterKinematics l : base.getAllDHChains()) {
				if (!l.getScriptingName().contentEquals(dh.getScriptingName())) {
					System.out.println("Adding Option "+l.getScriptingName());
					Platform.runLater(() -> relName.getItems().add(l.getScriptingName()));
				}
			}
			Platform.runLater(() -> relName.setDisable(false));

			if (getGroup().getTipOffset(dh) != null) {
				Platform.runLater(() ->useRelative.setSelected(true));
				Platform.runLater(() ->relativeToControls.setDisable(false));
				Platform.runLater(() -> relIndex.setDisable(false));
				Platform.runLater(() -> e.setDisable(false));
				robotToFiducialTransform = getGroup().getTipOffset(dh);
				Platform.runLater(() -> e.updatePose(robotToFiducialTransform));
				String refLimbName = getGroup().getTipOffsetRelativeName(dh);
				setNewReferencedLimb(base, refLimbName);
				Platform.runLater(() -> relIndex.setValue(getGroup().getTipOffsetRelativeIndex(dh)));
			}
		}
		e.updatePose(robotToFiducialTransform);
		Platform.runLater(() -> resetting = false);
	}

	private void setNewReferencedLimb(MobileBase base, String refLimbName) {
		Platform.runLater(() -> relName.setValue(refLimbName));
		DHParameterKinematics referencedLimb = null;
		for (DHParameterKinematics lm : base.getAllDHChains()) {
			if (lm.getScriptingName().toLowerCase().contentEquals(refLimbName.toLowerCase())) {
				// FOund the referenced limb
				referencedLimb = lm;
			}
		}
		Platform.runLater(() -> relIndex.getItems().clear());
		DHParameterKinematics rl = referencedLimb;
		Platform.runLater(() -> {
			for (int i = 0; i < rl.getNumberOfLinks(); i++) {
				relIndex.getItems().add(i);
			}
		});
	}

	public ParallelGroup getGroup() {
		return base.getParallelGroup(dh);
	}

}
