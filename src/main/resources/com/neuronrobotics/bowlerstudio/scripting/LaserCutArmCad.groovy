import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.scene.paint.Color;

import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.common.Log;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.STL;
import eu.mihosoft.vrl.v3d.Transform;

return new ICadGenerator(){
				
				public ArrayList<CSG> generateCad(ArrayList<DHLink> dhLinks ){
					
					ArrayList<CSG> csg = new ArrayList<CSG>();
					CSG previousServo=null;
					CSG servoModel = null;

					try {
						servoModel = STL.file(Paths.get(CreatureLab.class.getResource("hxt900-servo.stl").toURI()));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(dhLinks!=null){
						for(int i=0;i<dhLinks.size();i++){
							Log.warning("Adding Link Widget: "+i);
							DHLink dh  =dhLinks.get(i);
							double minLinWidth=18;
							double radiusAddOn=40;
							// Create an axis to represent the link
							double y = dh.getD()>minLinWidth?dh.getD():minLinWidth;
							double  x= dh.getRadius()>0?(dh.getRadius()+radiusAddOn):2;
							double lasLinkOffset=30;
							double lastServoOffset=0;
							
							if(dhLinks.size()>4){
								lastServoOffset=dhLinks.get(4).getD()-lasLinkOffset;
							}
							CSG cube=null;
							if(i<3){
								cube = new Cube(x,y,2).toCSG();
								cube=cube.transformed(new Transform().translateX((-x/2)+radiusAddOn-10));
							}else if(i==3 && dhLinks.size()>4){
								
								cube = new Cube(y,2,lastServoOffset+lasLinkOffset).toCSG();
								cube=cube.transformed(new Transform().translateY(10));
								cube=cube.transformed(new Transform().translateZ(lasLinkOffset));
							}else{
								//cube = new Cube(x,y,2).toCSG();
							}

							CSG servo=null;
							if(i< (dhLinks.size()-1) ){
								servo=servoModel.transformed(new Transform().translateZ(-19.3));
								servo=servo.transformed(new Transform().translateX(5.4));
								if(i==3&&dhLinks.size()>4){
									
									servo=servo.transformed(new Transform().translateZ(lastServoOffset));
								}
								
								if(cube!=null ){
									cube=cube.difference(servo);
								}
								if(previousServo!=null){
									CSG attach = servo.transformed( new Transform().translateZ(-8.5));
									if(i==3){
										attach = servoModel.
										transformed(
											new Transform().rotX(90));
										attach=attach.transformed(new Transform().translateY(-18.3));
										attach=attach.transformed(new Transform().translateX(5.4));
										attach = attach.transformed( new Transform().translateZ(0));
										
									}else
										attach = attach.transformed( new Transform().translateX(-dhLinks.get(i).getR()));
										
									if(cube!=null && i!=3){

										cube=cube.difference(attach);
										cube=cube.difference(attach.transformed( new Transform().translateX(-6)));
										cube=cube.difference(attach.transformed( new Transform().translateX(6)));
										cube=cube.difference(attach.transformed( new Transform().translateY(-6)));
										cube=cube.difference(attach.transformed( new Transform().translateY(6)));
									}
									if(cube!=null && i==3){
										
										cube=cube.difference(attach);
										cube=cube.difference(attach.transformed( new Transform().translateZ(-6)));
										cube=cube.difference(attach.transformed( new Transform().translateZ(6)));
										cube=cube.difference(attach.transformed( new Transform().translateX(-6)));
										cube=cube.difference(attach.transformed( new Transform().translateX(6)));
									}
									attach.setManipulator(dh.getListener());
									//csg.add(attach);
								}
								previousServo=servo;
							}
							//add listner to axis
							if(cube!=null){
								cube.setManipulator(dh.getListener());
								cube.setColor(Color.GOLD);
								csg.add(cube);
							}
							if(servo!=null){
								servo.setManipulator(dh.getListener());
								//if(i>2)
								csg.add(servo);
							}
						}
					}
					return csg;
				}
			};