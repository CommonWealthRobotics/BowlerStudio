package com.neuronrobotics.bowlerstudio.creature;


import org.jfree.util.Log;

import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.addons.kinematics.parallel.ParallelGroup;

import javafx.scene.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ParallelWidget extends Group {
	static VBox box = new VBox();
	ParallelGroup group;
	private HBox row(String label,Node tf) {
		HBox h= new HBox();
		h.getChildren().add(new Label(label));
		h.getChildren().add(tf);
		return h;
	}
	public ParallelWidget(MobileBase base,DHParameterKinematics dh, CreatureLab creatureLab) {
		super(box);
		group = base.getParallelGroup(dh);
		TextField groupName = new TextField();
		TextField relName = new TextField();
		TextField relIndex = new TextField();
		
		if(group ==null) {
			relName.setDisable(true);
			relIndex.setDisable(true);
		}else {
			groupName.setText(group.getNameOfParallelGroup());
		}
		
		
		box.getChildren().add(row("Parallel Group Name",groupName));
		box.getChildren().add(row("Limb Relative",relName));
		box.getChildren().add(row("Limb Relative index",relIndex));
		box.getChildren().add(new TransformWidget("Parallel Tip Offset",
									dh.getRobotToFiducialTransform(), new IOnTransformChange() {

										@Override
										public void onTransformFinished(TransformNR newTrans) {
											// Force a cad regeneration
											creatureLab.onSliderDoneMoving(null, 0);
										}

										@Override
										public void onTransformChaging(TransformNR newTrans) {
											Log.debug("Limb to base" + newTrans.toString());
											dh.setRobotToFiducialTransform(newTrans);
											dh.refreshPose();
										}
									}));
	}
}
