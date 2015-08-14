import eu.mihosoft.vrl.v3d.Extrude;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import javafx.scene.paint.Color;
import javax.vecmath.Matrix4d;
import Jama.Matrix;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.jniloader.NativeResource;
import com.neuronrobotics.nrconsole.plugin.BowlerCam.RGBSlider.ColorBox;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.gui.PosePanelNR;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.bowlerstudio.vitamins.IVitamin;
import com.neuronrobotics.bowlerstudio.vitamins.MicroServo;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.STL;
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.Vector3d;
import javafx.scene.paint.Color;
/**
 * This cad script generates a 1:10 scale rendering of a robot made from flat cut pieces
 * @author hephaestus
 *
 */

return new ICadGenerator(){
	double scaleOfRobot= 1.0/10.0;
	double boardThickness = 18.0*scaleOfRobot;
	
	//CSG servoReference= new MicroServo().toCSG();
	CSG servoReference=  Vitamins.get("smallservo.stl")
	.transformed(new Transform().rotZ(-90))
//	.transformed(new Transform().translateZ(12.0))
//	.transformed(new Transform().translateX(5.4));
	
	//CSG horn=  STL.file(NativeResource.inJarLoad(IVitamin.class,"smallmotorhorn.stl").toPath())
	CSG horn = new Cube(6,4,18).toCSG();
	private double attachmentRodWidth=10;
	private double attachmentBaseWidth=15;
	private double printerTollerence =0.5;
	
	double cylandarRadius = 14;

	private CSG reverseDHValues(CSG incoming,DHLink dh ){
		return incoming
		.transformed(new Transform().rotX(-Math.toDegrees(dh.getAlpha())))
		//.transformed(new Transform().rotZ(Math.toDegrees(dh.getTheta())))
	}
	
	private CSG moveDHValues(CSG incoming,DHLink dh ){
		return incoming.transformed(new Transform().translateZ(-dh.getD()))
		.transformed(new Transform().rotZ(-Math.toDegrees(dh.getTheta())))
		.transformed(new Transform().rotZ((90+Math.toDegrees(dh.getTheta()))))
		.transformed(new Transform().translateX(-dh.getR()))
		.transformed(new Transform().rotX(Math.toDegrees(dh.getAlpha())));
	}
	
	ArrayList<CSG> generateBodyParts(MobileBase base ,boolean printing){
		ArrayList<CSG> allCad=new ArrayList<>();
		ArrayList<Vector3d> points=new ArrayList<>();
		ArrayList<CSG> cutouts=new ArrayList<>();
		for(DHParameterKinematics l:base.getAllDHChains()){
			TransformNR position = l.getRobotToFiducialTransform();
			RotationNR rot = position.getRotation()
			Matrix vals =position.getMatrixTransform();
			double [] elemenents = [ 
				vals.get(0, 0),
				vals.get(0, 1),
				vals.get(0, 2),
				vals.get(0, 3),
				
				vals.get(1, 0),
				vals.get(1, 1),
				vals.get(1, 2),
				vals.get(1, 3),
				
				vals.get(2, 0),
				vals.get(2, 1),
				vals.get(2, 2),
				vals.get(2, 3),
				
				vals.get(3, 0),
				vals.get(3, 1),
				vals.get(3, 2),
				vals.get(3, 3),
				
				 ] as double[];
			
			
			Matrix4d rotation=	new Matrix4d(elemenents);

			points.add(new Vector3d(position.getX(), position.getY()));
			
		}
		double distance = 5
		CSG upperBody = Extrude.points(	new Vector3d(0, 0, attachmentBaseWidth/2),
               						points)
		CSG lowerBody = Extrude.points(	new Vector3d(0, 0,attachmentBaseWidth/2),
               						points
		   						)
						.movez(-attachmentBaseWidth/2)
		for(CSG c:cutouts){
			upperBody= upperBody.difference(c);
			lowerBody= lowerBody.difference(c);
			//allCad.add(c)
		}
		if(!printing){			
			upperBody.setColor(Color.CYAN);
			lowerBody.setColor(Color.ALICEBLUE);
			upperBody.setManipulator(base.getRootListener());
			lowerBody.setManipulator(base.getRootListener());
		}else{
			upperBody=upperBody
					.transformed(new Transform().rotX(180))
					.toZMin()
			lowerBody=	lowerBody
				.toZMin()
		}
		allCad.addAll(upperBody,
			lowerBody
		)
		
		return allCad;
	}
	ArrayList<CSG> generateBody(MobileBase base ){
		
		ArrayList<CSG> allCad=new ArrayList<>();
		//Start by generating the legs using the DH link based generator
		for(DHParameterKinematics l:base.getAllDHChains()){
			for(CSG csg:generateCad(l.getChain().getLinks())){
				allCad.add(csg);
			}
		}
		try{
			//now we genrate the base pieces
			for(CSG csg:generateBodyParts( base ,false)){
				allCad.add(csg);
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return allCad;
	}
	
	ArrayList<File> generateStls(MobileBase base , File baseDirForFiles ){
		ArrayList<File> allCadStl = new ArrayList<>();
		int leg=0;
		//Start by generating the legs using the DH link based generator
		for(DHParameterKinematics l:base.getAllDHChains()){
			int link=0;
			for(CSG csg:generateCad(l.getChain().getLinks(),true)){
				File dir = new File(baseDirForFiles.getAbsolutePath()+"/"+base.getScriptingName()+"/"+l.getScriptingName())
				if(!dir.exists())
					dir.mkdirs();
				File stl = new File(dir.getAbsolutePath()+"/Leg_"+leg+"_part_"+link+".stl");
				FileUtil.write(
						Paths.get(stl.getAbsolutePath()),
						csg.toStlString()
				);
				allCadStl.add(stl);
				link++;
			}
			leg++;
		}
		int link=0;
		//now we genrate the base pieces
		for(CSG csg:generateBodyParts( base,true )){
			File dir = new File(baseDirForFiles.getAbsolutePath()+"/"+base.getScriptingName()+"/")
			if(!dir.exists())
				dir.mkdirs();
			File stl = new File(dir.getAbsolutePath()+"/Body_part_"+link+".stl");
			FileUtil.write(
					Paths.get(stl.getAbsolutePath()),
					csg.toStlString()
			);
			allCadStl.add(stl);
			link++;
		}
		 
		return allCadStl;
	}
	public ArrayList<CSG> generateCad(ArrayList<DHLink> dhLinks ){
		return generateCad(dhLinks ,false);
	}
	
	public ArrayList<CSG> generateCad(ArrayList<DHLink> dhLinks,boolean printBed ){
		
		ArrayList<CSG> csg = new ArrayList<CSG>();

	
		return csg;
	}
};
