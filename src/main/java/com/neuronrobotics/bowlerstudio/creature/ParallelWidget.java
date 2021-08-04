package com.neuronrobotics.bowlerstudio.creature;

import org.jfree.util.Log;

import com.neuronrobotics.bowlerstudio.IssueReportingExceptionHandler;
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
	VBox boxTop = new VBox();
	VBox box = new VBox();
	VBox relativeToControls = new VBox();
    CheckBox useRelative = new CheckBox("This limb is relative to another link");
    CheckBox useParallel = new CheckBox("This limb is part of a parallel group");
	TransformWidget e;
	TextField groupName = new TextField();
	ComboBox<String> relativeName = new ComboBox<String>();
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
		this.base = b;
		this.dh = d;
		this.creatureLab = c;
		
		
		useParallel.setSelected(false);
		useParallel.setOnAction(event -> {
			box.setDisable(!useParallel.isSelected());
			if(useParallel.isSelected()) {
				if(groupName.getText().length()>0) {
					setupAddReferenceSection();
				}
			}else {
				getGroup().removeLimb(dh);
				useRelative.setSelected(false);
			}
		});
			
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
				relativeToControls.setDisable(true);
			}
			home();
		});

		relativeName.setOnAction(event -> {
			if (resetting)
				return;
			String refLimbName = relativeName.getValue();
			setNewReferencedLimb(base, refLimbName);
			relIndex.setDisable(false);
			
		});
		relIndex.setOnAction(event -> {
			if (resetting)
				return;
			try {
				getGroup().setupReferencedLimb(dh, robotToFiducialTransform, relativeName.getValue(), relIndex.getValue());
				e.setDisable(false);
			}catch(java.lang.NullPointerException e) {}
			home();
		});


		e = new TransformWidget("Parallel Tip Offset", robotToFiducialTransform, new IOnTransformChange() {

			@Override
			public void onTransformFinished(TransformNR newTrans) {
				if (resetting)
					return;
				home();
			}

			

			@Override
			public void onTransformChaging(TransformNR newTrans) {
				if (resetting)
					return;
				robotToFiducialTransform = newTrans;
				System.out.println("Tip offset for "+dh.getScriptingName()+" "+newTrans);
				getGroup().setTipOffset(dh, newTrans);
				dh.refreshPose();
				home();
			}
		});
		Platform.runLater(() -> getChildren().add(boxTop));
		Platform.runLater(() -> boxTop.getChildren().add(useParallel));
		Platform.runLater(() -> boxTop.getChildren().add(box));
		Platform.runLater(() -> box.getChildren().add(row("Parallel Group Name", groupName)));
		Platform.runLater(() -> box.getChildren().add(useRelative));
		Platform.runLater(() -> box.getChildren().add(relativeToControls));

		Platform.runLater(() -> relativeToControls.getChildren().add(row("Limb Relative", relativeName)));
		Platform.runLater(() -> relativeToControls.getChildren().add(row("Limb Relative index", relIndex)));
		Platform.runLater(() -> relativeToControls.getChildren().add(e));
	}
	private void home() {
		try {
			getGroup().setDesiredTaskSpaceTransform(getGroup().calcHome(),0);
		} catch (Exception e) {}
	}
	private void setupAddReferenceSection() {
		base.getParallelGroup(groupName.getText()).setupReferencedLimbStartup(dh, null, "", 0);
		Platform.runLater(() -> relativeName.getItems().clear());
		for (DHParameterKinematics l : base.getAllDHChains()) {
			if (!l.getScriptingName().contentEquals(dh.getScriptingName())) {
				Platform.runLater(() -> relativeName.getItems().add(l.getScriptingName()));
			}
		}
		
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
		Platform.runLater(() -> relativeName.getItems().clear());
		Platform.runLater(() -> relIndex.getItems().clear());
		
		Platform.runLater(() -> relIndex.setDisable(true));
		Platform.runLater(() -> e.setDisable(true));

		if (getGroup() == null) {
			useParallel.setSelected(false);
			box.setDisable(true);
		} else {
			useParallel.setSelected(true);
			box.setDisable(false);
			Platform.runLater(() -> groupName.setText(getGroup().getNameOfParallelGroup()));
			for (DHParameterKinematics l : base.getAllDHChains()) {
				if (!l.getScriptingName().contentEquals(dh.getScriptingName())) {
					System.out.println("Adding Option "+l.getScriptingName());
					Platform.runLater(() -> relativeName.getItems().add(l.getScriptingName()));
				}
			}

			if (getGroup().getTipOffset(dh) != null) {
				Platform.runLater(() ->useRelative.setSelected(true));
				Platform.runLater(() ->relativeToControls.setDisable(false));
				Platform.runLater(() -> relIndex.setDisable(false));
				Platform.runLater(() -> e.setDisable(false));
				robotToFiducialTransform = getGroup().getTipOffset(dh);
				Platform.runLater(() -> e.updatePose(robotToFiducialTransform));
				String refLimbName = getGroup().getTipOffsetRelativeName(dh);
				setNewReferencedLimb(base, refLimbName);
				Platform.runLater(() -> relativeName.setValue(refLimbName));
				Platform.runLater(() -> relIndex.setValue(getGroup().getTipOffsetRelativeIndex(dh)));
			}else {
				Platform.runLater(() ->useRelative.setSelected(false));
				Platform.runLater(() ->relativeToControls.setDisable(true));
			}
		}
		e.updatePose(robotToFiducialTransform);
		Platform.runLater(() -> resetting = false);
		home();
	}

	private void setNewReferencedLimb(MobileBase base, String refLimbName) {
		
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
