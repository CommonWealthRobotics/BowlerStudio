package com.neuronrobotics.bowlerstudio.threed;

import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Affine;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;

public class Line3D  extends Line {
	
	private double endZ = 0;
	private double startZ=0;
	
	public Line3D(Vertex start, Vertex end){
		this(start.pos,
				end.pos	);
	}
	public Line3D(double [] start, double[] end){
		this(start[0],start[1],start[2],
				end[0],end[1],end[2]	);
	}
	public Line3D(Vector3d start, Vector3d end){
		this(start.x,start.y,start.z,
				end.x,end.y,end.z	);
	}
	
	public Line3D(
            double endX,
            double endY,
            double endZ){
		this(0,0,0,endX,endY,endZ);
	}
	
	public Line3D(double startX,
            double startY,
            double startZ,
            double endX,
            double endY,
            double endZ){
		double xdiff = endX-startX;
		double ydiff = endY-startY;
		double zdiff = endZ-startZ;
		
		double lineLen = Math.sqrt(	Math.pow(xdiff, 2)+
									Math.pow(ydiff, 2)+
									Math.pow(zdiff, 2)
				);
		double xyProjection = Math.sqrt(	Math.pow(xdiff, 2)+
				Math.pow(ydiff, 2)
				);
		setStartX(0.0f);
		setStartY(0.0f);
		setEndX(lineLen);
		setEndY(0);
		
		double rotZ =  Math.toDegrees(Math.atan2(xdiff, ydiff));
		double rotY =  Math.toDegrees(Math.atan2(xyProjection, zdiff));
		Affine xy = new Affine();
		xy.appendRotation(-90-rotY, 0, 0, 0, 0, 1, 0);
		Affine zp = new Affine();
		zp.appendRotation(-90-rotZ, 0, 0, 0, 0, 0, 1);
		Affine zTrans = new Affine();
		zTrans.setTx(startX);
		zTrans.setTy(startY);
		zTrans.setTz(startZ);



		getTransforms().add(zTrans);
		getTransforms().add(zp);
		getTransforms().add(xy);
		smoothProperty().set(false);
		setStrokeLineCap(StrokeLineCap.BUTT);
	}
	
	public double getEndZ() {
		return endZ;
	}
	public void setEndZ(double endZ) {
		this.endZ = endZ;
	}
	public double getStartZ() {
		return startZ;
	}
	public void setStartZ(double startZ) {
		this.startZ = startZ;
	}
	
}

