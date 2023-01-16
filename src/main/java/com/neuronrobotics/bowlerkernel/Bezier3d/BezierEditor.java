package com.neuronrobotics.bowlerkernel.Bezier3d;


//import com.neuronrobotics.bowlerstudio.BowlerStudio
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import eu.mihosoft.vrl.v3d.*;

public class BezierEditor{
	Type TT_mapStringString = new TypeToken<HashMap<String, HashMap<String,List<Double>>>>() {}.getType();
	Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	File	cachejson;
	TransformNR end = new TransformNR();
	TransformNR cp1 = new TransformNR();
	TransformNR cp2 = new TransformNR();
	TransformNR strt = new TransformNR();
	ArrayList<CSG> parts = new ArrayList<CSG>();
	CSG displayPart=new Cylinder(5,0,20,10).toCSG()
	.toZMax()
	.roty(-90);

	CartesianManipulator endManip;
	CartesianManipulator cp1Manip;
	CartesianManipulator cp2Manip;
	CartesianManipulator start;
	HashMap<String, HashMap<String,List<Double>>> database;
	boolean updating = false;
	private String url;
	private String gitfile;
	public BezierEditor(String URL, String file, int numPoints) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		this(ScriptingEngine.fileFromGit(URL, file),numPoints);
		url=URL;
		gitfile=file;
	}
	public BezierEditor(File data, int numPoints) {
		cachejson = data;
		String jsonString = null;
		boolean loaded=false;
		try {
			if(cachejson.exists()) {
				InputStream inPut = null;
				inPut = FileUtils.openInputStream(cachejson);
				jsonString = IOUtils.toString(inPut);
				database = gson.fromJson(jsonString, TT_mapStringString);

				List<Double> cp1in = (List<Double>)database.get("bezier").get("control one");
				List<Double> cp2in = (List<Double>)database.get("bezier").get("control two");
				List<Double> ep = (List<Double>)database.get("bezier").get("end point");
				List<Double> st = (List<Double>)database.get("bezier").get("start point");
				end.setX(ep.get(0));
				end.setY(ep.get(1));
				end.setZ(ep.get(2));
				cp1.setX(cp1in.get(0));
				cp1.setY(cp1in.get(1));
				cp1.setZ(cp1in.get(2));
				cp2.setX(cp2in.get(0));
				cp2.setY(cp2in.get(1));
				cp2.setZ(cp2in.get(2));

				strt.setX(st.get(0));
				strt.setY(st.get(1));
				strt.setZ(st.get(2));
				loaded=true;
			}
		}catch(Throwable t) {
			t.printStackTrace();
		}

		if(!loaded) {
			end.setX(100);
			end.setY(100);;
			end.setZ(100);
			cp1.setX(50);
			cp1.setY(-50);
			cp1.setZ(50);
			cp2.setX(0);
			cp2.setY(50);
			cp2.setZ(-50);

			database= new HashMap<>();
		}


		endManip=new CartesianManipulator(end,()->{save();},()->{update();});
		cp1Manip=new CartesianManipulator(cp1,()->{save();},()->{update();});
		cp2Manip=new CartesianManipulator(cp2,()->{save();},()->{update();});
		start=new CartesianManipulator(strt,()->{save();},()->{update();});

		for(int i=0;i<numPoints;i++){
			CSG part=displayPart.clone();
			part.setManipulator(new Affine());
			parts.add(part);
		}
		update();
		save();
	}
	public ArrayList<CSG> get(){

		ArrayList<CSG> back= new ArrayList<CSG>();
		back.addAll(endManip.get());
		back.addAll(cp1Manip.get());
		back.addAll(cp2Manip.get());
		back.addAll(start.get());
		back.addAll(parts);
		return back;
	}

	public void update() {
		if(updating) {
			return;
		}
		updating=true;
		ArrayList<Transform> transforms = transforms ();
		for(int i=0;i<parts.size();i++) {
			TransformNR nr=TransformFactory.csgToNR(transforms.get(i));
			Affine partsGetGetManipulator = parts.get(i).getManipulator();
			Platform.runLater(()->{
				TransformFactory.nrToAffine(nr, partsGetGetManipulator);
			});
		}
		updating=false;
	}
	public ArrayList<Transform> transforms (){
		ArrayList<Transform> tf=Extrude.bezierToTransforms(
		new Vector3d(	cp1Manip.manipulationMatrix.getTx()-start.manipulationMatrix.getTx(),
						cp1Manip.manipulationMatrix.getTy()-start.manipulationMatrix.getTy(),
						cp1Manip.manipulationMatrix.getTz()-start.manipulationMatrix.getTz()), // Control point one
		new Vector3d(	cp2Manip.manipulationMatrix.getTx()-start.manipulationMatrix.getTx(),
						cp2Manip.manipulationMatrix.getTy()-start.manipulationMatrix.getTy(),
						cp2Manip.manipulationMatrix.getTz()-start.manipulationMatrix.getTz()), // Control point two
		new Vector3d(	endManip.manipulationMatrix.getTx()-start.manipulationMatrix.getTx(),
						endManip.manipulationMatrix.getTy()-start.manipulationMatrix.getTy(),
						endManip.manipulationMatrix.getTz()-start.manipulationMatrix.getTz()), // Endpoint
		parts.size()// Iterations
		);
		
		for(int i=0;i<tf.size();i++) {
			tf.set(i, tf
					.get(i)
					.movex(start.manipulationMatrix.getTx())
					.movey(start.manipulationMatrix.getTy())
					.movez(start.manipulationMatrix.getTz())
					);
		}
		return tf;
	}
	public void save() {
		database.clear();
		HashMap<String,List<Double>> bezData=new HashMap<>();

		bezData.put("control one",Arrays.asList(
				cp1Manip.manipulationMatrix.getTx(),
				cp1Manip.manipulationMatrix.getTy(),
				cp1Manip.manipulationMatrix.getTz()
				));
		bezData.put("control two",Arrays.asList(
				cp2Manip.manipulationMatrix.getTx(),
				cp2Manip.manipulationMatrix.getTy(),
				cp2Manip.manipulationMatrix.getTz()
				));
		bezData.put("end point",Arrays.asList(
				endManip.manipulationMatrix.getTx(),
				endManip.manipulationMatrix.getTy(),
				endManip.manipulationMatrix.getTz()
				));
		bezData.put("start point",Arrays.asList(
				start.manipulationMatrix.getTx(),
				start.manipulationMatrix.getTy(),
				start.manipulationMatrix.getTz()
				));
		bezData.put("number of points",Arrays.asList((double)parts.size()));
		database.put("bezier",bezData);

		new Thread(()->{
			System.out.println("Saving to file "+cachejson.getAbsolutePath());
			String writeOut = gson.toJson(database, TT_mapStringString);
			if(url!=null) {
				try {
					ScriptingEngine.pushCodeToGit(url, ScriptingEngine.getFullBranch(url), gitfile, writeOut, "Saving Bezier");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				if(!cachejson.exists())
					try {
						cachejson.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				OutputStream out = null;
				try {
					out = FileUtils.openOutputStream(cachejson, false);
					IOUtils.write(writeOut, out);
					out.close(); // don't swallow close Exception if copy
					// completes
					// normally
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					IOUtils.closeQuietly(out);
				}
			}
		}).start();
	}
}