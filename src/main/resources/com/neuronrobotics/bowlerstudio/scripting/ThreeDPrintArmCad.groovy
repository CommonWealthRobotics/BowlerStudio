import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.scene.paint.Color;

import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.jniloader.NativeResource;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.bowlerstudio.vitamins.MicroServo;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.STL;
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Cylinder;
import javafx.scene.paint.Color;
return new ICadGenerator(){
	CSG servoReference=  new MicroServo().toCSG()
	CSG horn = new Cube(6,4,18).toCSG();
	private double attachmentRodWidth=10;
	private double attachmentBaseWidth=15;
	private CSG toZMin(CSG incoming,CSG target){
		return incoming.transformed(new Transform().translateZ(-target.getBounds().getMin().z));
	}
	private CSG toZMax(CSG incoming,CSG target){
		return incoming.transformed(new Transform().translateZ(-target.getBounds().getMax().z));
	}
	private CSG toXMin(CSG incoming,CSG target){
		return incoming.transformed(new Transform().translateX(-target.getBounds().getMin().x));
	}
	private CSG toXMax(CSG incoming,CSG target){
		return incoming.transformed(new Transform().translateX(-target.getBounds().getMax().x));
	}
	private CSG toYMin(CSG incoming,CSG target){
		return incoming.transformed(new Transform().translateY(-target.getBounds().getMin().y));
	}
	private CSG toYMax(CSG incoming,CSG target){
		return incoming.transformed(new Transform().translateY(-target.getBounds().getMax().y));
	}
	
	private CSG toZMin(CSG incoming){
		return toZMin(incoming,incoming);
	}
	private CSG toZMax(CSG incoming){
		return toZMax(incoming,incoming);
	}
	private CSG toXMin(CSG incoming){
		return toXMin(incoming,incoming);
	}
	private CSG toXMax(CSG incoming){
		return toXMax(incoming,incoming);
	}
	private CSG toYMin(CSG incoming){
		return toYMin(incoming,incoming);
	}
	private CSG toYMax(CSG incoming){
		return toYMax(incoming,incoming);
	}
	
	private CSG getAttachment(){
		CSG attachmentbase = toZMin(new Cube(attachmentBaseWidth,attachmentBaseWidth,4).toCSG());
		CSG post = toZMin(new Cube(	attachmentRodWidth,
									attachmentRodWidth,
									Math.abs(servoReference.getBounds().getMax().x)+5+attachmentRodWidth/2).toCSG());
		attachmentbase = toZMax(attachmentbase.union(post))
		.transformed(new Transform().translateZ( attachmentRodWidth/2));
		CSG hornAttach =toZMin(toYMin(	toYMax( toZMax(horn).transformed(new Transform().translateZ( 4))) , 
										post),
									post
									);
		attachmentbase =attachmentbase.difference(hornAttach);
		
		CSG bearingPin =toYMax( new Cylinder(attachmentRodWidth/2,7.5/2, 3.0,(int)20).toCSG()
			.transformed(new Transform().rotX(-90)),
										post);
		attachmentbase =attachmentbase.union(bearingPin);
		return attachmentbase.transformed(new Transform().rot(-90, -90, 0));

	}
	
	private CSG getFoot(){
		CSG attach = getAttachment();
		CSG foot = new Sphere(attachmentRodWidth).toCSG();
		return  toXMax(attach.union(foot));
	}
	
	private CSG moveDHValues(CSG incoming,DHLink dh ){
		return incoming.transformed(new Transform().translateZ(-dh.getD()))
		.transformed(new Transform().rotZ(-Math.toDegrees(dh.getTheta())))
		.transformed(new Transform().rotZ((90+Math.toDegrees(dh.getTheta()))))
		.transformed(new Transform().translateX(-dh.getR()))
		.transformed(new Transform().rotX(Math.toDegrees(dh.getAlpha())));
		
	}
				
	public ArrayList<CSG> generateCad(ArrayList<DHLink> dhLinks ){
		
		ArrayList<CSG> csg = new ArrayList<CSG>();

		DHLink dh = dhLinks.get(0);
		
		CSG rootAttachment=getAttachment();
		rootAttachment.setManipulator(dh.getRootListener());
		//csg.add(rootAttachment);//This is the root that attaches to the base
		rootAttachment.setColor(Color.CHOCOLATE);
		
		CSG foot=getFoot();

		
		CSG servoKeepaway = toXMin(toZMax(	new Cube(Math.abs(servoReference.getBounds().getMin().x) +
			Math.abs(servoReference.getBounds().getMax().x),
			Math.abs(servoReference.getBounds().getMin().y) +
			Math.abs(servoReference.getBounds().getMax().y),
			Math.abs(servoReference.getBounds().getMax().z)).toCSG(),
		
		)
		)
		servoKeepaway = servoKeepaway
		.transformed(new Transform().translateX(-Math.abs(servoReference.getBounds().getMin().x)))
		.transformed(new Transform().translateZ(-Math.abs(servoReference.getBounds().getMax().z -Math.abs(servoReference.getBounds().getMin().z) )/2))
		//servoReference = servoReference.union(servoKeepaway)

		
		if(dhLinks!=null){
			for(int i=0;i<dhLinks.size();i++){
				dh = dhLinks.get(i);
				CSG nextAttachment=getAttachment();

				CSG servo=servoReference.transformed(new Transform().translateZ(-12.5))// allign to the horn
				.union(servoKeepaway)
				.transformed(new Transform().rotX(180))// allign to the horn
				.transformed(new Transform().rotZ(-90))// allign to the horn
				;
				

				double rOffsetForNextLink;
				if(i==dhLinks.size()-1){
						 rOffsetForNextLink = dh.getR()-
					(	2.1+
						Math.abs(foot.getBounds().getMin().x) 
					)
				}else{
					rOffsetForNextLink = dh.getR()-
					(	2.1+
						Math.abs(nextAttachment.getBounds().getMin().x)
					)
				}
				println "Link # "+i+" offset = "+(dh.getR()-rOffsetForNextLink)
				if(rOffsetForNextLink<attachmentBaseWidth){
					rOffsetForNextLink=attachmentBaseWidth
				}
				double linkThickness = dh.getD();
				if(linkThickness<attachmentBaseWidth/2)
					linkThickness=attachmentBaseWidth/2
				linkThickness +=3;
				servo= moveDHValues(servo,dh);
				double cylandarRadius = 13
				CSG upperLink = toZMin(new Cylinder(cylandarRadius,linkThickness,(int)20).toCSG())
				// adding the radius rod
				CSG rod = toYMin(
									toZMin(
										new Cube( 
											attachmentBaseWidth+3,
											rOffsetForNextLink,
											upperLink.getBounds().getMax().z
											).toCSG()
										)
									)
				CSG clip = toYMin(
					toZMax(
						new Cube(
							attachmentBaseWidth+3,
							9,
							attachmentBaseWidth+3
							).toCSG()
						)
					)
					.transformed(new Transform().translateY(rOffsetForNextLink))// allign to the NEXT ATTACHMENT
					.transformed(new Transform().translateZ(linkThickness))// allign to the NEXT ATTACHMENT
					
				upperLink=upperLink.union(rod,clip);
				upperLink=upperLink.transformed(new Transform().translateZ(Math.abs(servoReference.getBounds().getMax().z-3)))

				upperLink= moveDHValues(upperLink,dh).difference(servo);
				if(i== dhLinks.size()-1)
					upperLink= upperLink.difference(foot);
				else
					upperLink= upperLink.difference(nextAttachment);
				double LowerLinkThickness = attachmentRodWidth/2-2
				CSG lowerLink = toZMax(new Cylinder(
					cylandarRadius,
					 LowerLinkThickness,
					 (int)20).toCSG()

				)
				lowerLink=lowerLink.transformed(new Transform().translateZ(-attachmentRodWidth/2))
				CSG lowerClip = toYMin(
						toZMin(
						new Cube(
							attachmentBaseWidth+3,
							rOffsetForNextLink,
							LowerLinkThickness +linkThickness+3
							).toCSG()
						)
					)
					.transformed(new Transform().translateY(9))// allign to the NEXT ATTACHMENT
					.transformed(new Transform().translateZ(-attachmentRodWidth/2 -LowerLinkThickness ))
				lowerLink=lowerLink.union(
					lowerClip
					);
				//Remove the divit or the bearing
				lowerLink= lowerLink.difference(nextAttachment);
				lowerLink= moveDHValues(lowerLink,dh);
				//remove the next links connector and the upper link for mating surface
				lowerLink= lowerLink.difference(nextAttachment,upperLink,servo);
				
				if(dhLinks.size()>4){
					if(i== dhLinks.size()-2){
						nextAttachment=nextAttachment.transformed(new Transform().translateZ(dhLinks.get(dhLinks.size()-1).getD()/3));// allign to the horn
					}
					if(i== dhLinks.size()-1){
						servo=servo.transformed(new Transform().translateY(-dhLinks.get(dhLinks.size()-1).getD()/3));// allign to the horn
					}
				}
				nextAttachment.setManipulator(dh.getListener());
				nextAttachment.setColor(Color.CHOCOLATE);
				servo.setManipulator(dh.getListener());
				upperLink.setColor(Color.GREEN);
				upperLink.setManipulator(dh.getListener());
				csg.add(upperLink);//This is the root that attaches to the base
				
				lowerLink.setColor(Color.WHITE);
				lowerLink.setManipulator(dh.getListener());
				csg.add(lowerLink);//This is the root that attaches to the base
				//csg.add(servo);//This is the root that attaches to the base
//				if(i<dhLinks.size()-1)
//					csg.add(nextAttachment);//This is the root that attaches to the base
					
			}
			foot.setManipulator(dhLinks.get(dhLinks.size()-1).getListener());
			foot.setColor(Color.GOLD);
			csg.add(foot);//This is the root that attaches to the base
		}
		return csg;
	}
};