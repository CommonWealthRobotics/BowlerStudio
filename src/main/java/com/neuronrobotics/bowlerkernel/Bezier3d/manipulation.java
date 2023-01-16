package com.neuronrobotics.bowlerkernel.Bezier3d;

import java.util.HashMap;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.*;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Vector3d;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Affine;

public class manipulation {
	HashMap<EventType<MouseEvent>,EventHandler<MouseEvent>> map=new HashMap<>();
	double startx=0;
	double starty=0;
	double newx=0;
	double newy=0;
	double newz=0;
	TransformNR camFrame=null;
	boolean dragging=false;
	double depth =0;
	public manipulation(Affine manipulationMatrix,Vector3d orintation,CSG manip,TransformNR globalPose,Runnable eve,Runnable moving) {
		BowlerStudio.runLater(()->{
			TransformFactory.nrToAffine(globalPose, manipulationMatrix);
		});
		map.put(MouseEvent.MOUSE_PRESSED,  new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						new Thread(()->{
							camFrame= BowlerStudio.getCamerFrame();
							depth=-1600 /BowlerStudio.getCamerDepth();
							event.consume();
							dragging=false;
						}).start();
					}
				});

		map.put(MouseEvent.MOUSE_DRAGGED,  new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						BowlerStudio.runLater(()->{
							if(dragging==false) {
								startx=event.getScreenX();
								starty=event.getScreenY();
							}
							dragging=true;
							double deltx=(startx-event.getScreenX());
							double delty=(starty-event.getScreenY());
							TransformNR trans=new TransformNR(deltx / depth,
									delty / depth, 0, new RotationNR());

							TransformNR global = camFrame.times(trans);
							newx=(global.getX()*orintation.x+globalPose.getX());
							newy=(global.getY()*orintation.y+globalPose.getY());
							newz=(global.getZ()*orintation.z+globalPose.getZ());
							global.setX(newx);
							global.setY(newy);
							global.setZ(newz);

							global.setRotation(new RotationNR());
							BowlerStudio.runLater(()->{
								TransformFactory.nrToAffine(global, manipulationMatrix);
							});
							double dist = Math.sqrt(Math.pow(deltx, 2)+Math.pow(delty, 2));
							//System.out.println(" drag "+global.getX()+" , "+global.getY()+" , "+global.getZ()+" "+deltx+" "+delty);
							moving.run();
						});
						event.consume();
					}
				});

		map.put(MouseEvent.MOUSE_RELEASED,  new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if(dragging) {
							dragging=false;
							globalPose.setX(newx);
							globalPose.setY(newy);
							globalPose.setZ(newz);
							event.consume();
							new Thread(()->{eve.run();}).start();
						}
					}
				});
		manip.getStorage().set("manipulator",map);
		manip.setManipulator(manipulationMatrix);
	}
}
