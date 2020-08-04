package com.neuronrobotics.bowlerstudio.creature;


import org.jfree.util.Log;

import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.addons.kinematics.parallel.ParallelGroup;

import javafx.scene.*;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ParallelWidget extends Group {
	static VBox box = new VBox();
	ParallelGroup group;
	TransformWidget e;
	TextField groupName = new TextField();
	ComboBox<String> relName = new ComboBox<String>();
	ComboBox<Integer> relIndex = new ComboBox<Integer>();
	TransformNR robotToFiducialTransform = new TransformNR();
	private HBox row(String label,Node tf) {
		HBox h= new HBox();
		h.getChildren().add(new Label(label));
		h.getChildren().add(tf);
		return h;
	}
	public ParallelWidget(MobileBase base,DHParameterKinematics dh, CreatureLab creatureLab) {
		super(box);
		group = base.getParallelGroup(dh);
		

		groupName.setOnAction(event->{
			if(group!=null) {
				base.shutDownParallel(group);
			}
			if(groupName.getText().length()>0) {
				group = base.getParallelGroup(groupName.getText());
				group.addLimb(dh, null, "", 0);
				relName.getItems().clear();
				for(DHParameterKinematics l:base.getAllDHChains()) {
					if(!l.getScriptingName().contentEquals(dh.getScriptingName())) {
						relName.getItems().add(l.getScriptingName());
					}
				}
				relName.setDisable(false);
				relIndex.setDisable(true);
				e.setDisable(true);
			}else {
				relName.setDisable(true);
				relIndex.setDisable(true);
				e.setDisable(true);
			}
		});
		
		relName.setOnAction(event->{
			String refLimbName = relName.getValue();
			setNewReferencedLimb(base, refLimbName);
			group.setupReferencedLimb(dh,robotToFiducialTransform,relName.getValue(),0);
			relIndex.setDisable(false);
		});
		relIndex.setOnAction(event->{
			group.setupReferencedLimb(dh,robotToFiducialTransform,relName.getValue(),relIndex.getValue());
			e.setDisable(false);
		});
		
		if(group ==null) {
			relName.setDisable(true);
			relIndex.setDisable(true);
		}else {
			groupName.setText(group.getNameOfParallelGroup());
			if(group.getTipOffset(dh)!=null) {
				robotToFiducialTransform=group.getTipOffset(dh);
				relName.getItems().clear();
				for(DHParameterKinematics l:base.getAllDHChains()) {
					if(!l.getScriptingName().contentEquals(dh.getScriptingName())) {
						relName.getItems().add(l.getScriptingName());
					}
				}
				String refLimbName = group.getTipOffsetRelativeName(dh);
				setNewReferencedLimb(base, refLimbName);
				relIndex.setValue(group.getTipOffsetRelativeIndex(dh));
			}
		}
		
		
		box.getChildren().add(row("Parallel Group Name",groupName));
		box.getChildren().add(row("Limb Relative",relName));
		box.getChildren().add(row("Limb Relative index",relIndex));
		
		e = new TransformWidget("Parallel Tip Offset",
									robotToFiducialTransform, new IOnTransformChange() {

										@Override
										public void onTransformFinished(TransformNR newTrans) {
											// Force a cad regeneration
											creatureLab.onSliderDoneMoving(null, 0);
										}

										@Override
										public void onTransformChaging(TransformNR newTrans) {
											Log.debug("Limb to base" + newTrans.toString());
											robotToFiducialTransform=newTrans;
											group.setTipOffset(dh, newTrans);
											dh.refreshPose();
										}
									});
		if(group.getTipOffset(dh)==null) {
			 e.setDisable(true);
		}
		box.getChildren().add(e);
	}
	private void setNewReferencedLimb(MobileBase base, String refLimbName) {
		relName.setValue(refLimbName);
		DHParameterKinematics referencedLimb = null;
		for (DHParameterKinematics lm : base.getAllDHChains()) {
			if (lm.getScriptingName().toLowerCase().contentEquals(refLimbName.toLowerCase())) {
				// FOund the referenced limb
				referencedLimb = lm;
			}
		}
		relIndex.getItems().clear();
		for(int i=0;i<referencedLimb.getNumberOfLinks();i++) {
			relIndex.getItems().add(i);
		}
	}
}
