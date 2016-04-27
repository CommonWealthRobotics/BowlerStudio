package com.neuronrobotics.bowlerstudio.twod;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFSpline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.SplinePoint;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.Polygon;
import java.awt.Font;
import eu.mihosoft.vrl.v3d.Vector3d;

import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.PathIterator;


public class TwoDCadFactory {

	private TwoDCadFactory() {
	}

	public static ArrayList<Polygon> pointsFromString(Font font, String text){
		ArrayList<Polygon> sections = new ArrayList<>();
		ArrayList<Vector3d> points = new ArrayList<>();
		FontRenderContext frc = new FontRenderContext(null,(boolean)true,(boolean)true);
		TextLayout textLayout = new TextLayout(text, font, frc);
		Shape s = textLayout.getOutline(null);
		
		PathIterator pi = s.getPathIterator(null);
		
		float [] coords = new float[6];
		float [] start = new float[6];
		float tmp=0,tmp1=0;
		  while(pi.isDone() == (boolean)false ) {
			  	coords = new float[6];
		        int type = pi.currentSegment(coords);
		        switch(type) {
		        case PathIterator.SEG_CLOSE:
					//points.add(new Vector3d(tmp, tmp1 ,0));
					points.add(new Vector3d(start[0], start[1],0));
					
					if(points.size()>3){
						sections.add(Polygon.fromPoints(Extrude.toCCW(points)));
					}
		            points = new ArrayList<Vector3d>();
		            break;
				case PathIterator.SEG_QUADTO:
					//println "SEG_QUADTO from ( "+coords[0]+" , "+coords[1]+" ) to ( "+coords[2]+" , "+coords[3]+" )";
					for(float t=0.05f;t<=1.0f;t+=0.05f) {
						//(1-t)²*P0 + 2t*(1-t)*P1 + t²*P2
						float u = (1.0f-t);
						float tt=u*u;
						float ttt=2.0f*t*u;
						float tttt=t*t;
						float p1 = tmp*tt + (coords[0]*ttt) + (coords[2]*tttt);
						float p2 = tmp1*tt + (coords[1]*ttt) + (coords[3]*tttt);
						points.add(new Vector3d(p1, p2,0));
						//println "SEG_QUADTO "+p1+" and "+p2
					}
					tmp = coords[2];
					tmp1 = coords[3];
					points.add(new Vector3d(tmp, tmp1,0));
					break;
		        case PathIterator.SEG_LINETO:
					//println "SEG_LINETO "+coords
					tmp = coords[0];
					tmp1 = coords[1];
		            points.add(new Vector3d(tmp, tmp1,0));
		            break;
		        case PathIterator.SEG_MOVETO:
					
		            // move without drawing
		            start[0] = tmp  =  coords[0];
		            start[1] = tmp1 =  coords[1];
					//println "Moving to "+start
					points.add(new Vector3d(tmp, tmp1,0));
		            break;
		        case PathIterator.SEG_CUBICTO:
		            for(float t=0.0f;t<=1.05f;t+=0.1f) {
		                // p = a0 + a1*t + a2 * tt + a3*ttt;
		                float tt=t*t;
		                float ttt=tt*t;
		                float p1 = tmp + (coords[0]*t) + (coords[2]*tt) + (coords[4]*ttt);
		                float p2 = tmp1 + (coords[1]*t) + (coords[3]*tt) + (coords[5]*ttt);
		                points.add(new Vector3d(p1, p2,0));
						//println "SEG_CUBICTO "+p1+" and "+p2
		            }
		            tmp = coords[4];
		            tmp1 = coords[5];
		            break;
		
				default:
					throw new RuntimeException("Unknown iterator type: "+type);
		        }
		        pi.next();
				//println "pi.isDone() "+pi.isDone()
		    }
		  
		return sections;
	}
	public static ArrayList<Polygon> pointsFromFile(File incoming){
		ArrayList<Polygon> points = new ArrayList<>();
		if(incoming.getAbsolutePath().toLowerCase().endsWith(".dxf")){
			try {
				points=dxf(incoming);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(incoming.getAbsolutePath().toLowerCase().endsWith(".gcode")||
				incoming.getAbsolutePath().toLowerCase().endsWith(".nc")||
				incoming.getAbsolutePath().toLowerCase().endsWith(".ncg")){
			// load points
		}else if(incoming.getAbsolutePath().toLowerCase().endsWith(".jpg")){
			// load points from OpenCV
			// TODO dimitrisTim
		}else if(incoming.getAbsolutePath().toLowerCase().endsWith(".png")){
			// load points from OpenCV
			// TODO dimitrisTim
		}else
			return null;
	
		return points;
	}

	private static ArrayList<Polygon> dxf(File source) throws FileNotFoundException, ParseException{
		ArrayList<Polygon> polys = new ArrayList<>();
		ArrayList<Vector3d> points = new ArrayList<>();
		Parser parser = ParserBuilder.createDefaultParser();
		// parse
		parser.parse(new FileInputStream(source), DXFParser.DEFAULT_ENCODING);

		// get the documnet and the layer
		DXFDocument doc = parser.getDocument();
		Iterator layerIterable = doc.getDXFLayerIterator();
		if (layerIterable != null) {
			for (; layerIterable.hasNext();) {
				// iterate over all the layers
				DXFLayer layer = (DXFLayer) layerIterable.next();
				Iterator entityIterator = layer.getDXFEntityTypeIterator();
				if (entityIterator != null) {
					for (; entityIterator.hasNext();) {
						String entityType = (String) entityIterator.next();
						if (entityType.contentEquals(DXFConstants.ENTITY_TYPE_POLYLINE)) {
							// get all polylines from the layer
							List plines = layer.getDXFEntities(entityType);
							if (plines != null) {
								for (Object p : plines) {
									DXFPolyline pline = (DXFPolyline) p;
									for (int i = 0; i < pline.getVertexCount(); i++) {
										DXFVertex vertex = pline.getVertex(i);
										Point point = vertex.getPoint();
										Vector3d center=new Vector3d(point.getX(), point.getY(), point.getZ());
										points.add(center);

										//BowlerStudioController.addCsg(new Cube(1,1,1).setCenter(center).toCSG());
									}
								}
							}
							polys.add(Polygon.fromPoints(Extrude.toCCW(points)));
							points.clear();
							
						}
						else if (entityType.contentEquals(DXFConstants.ENTITY_TYPE_LINE)) {
							// get all polylines from the layer
							List plines = layer.getDXFEntities(entityType);
							if (plines != null) {
								for (Object p : plines) {
									DXFLine pline = (DXFLine) p;
									Point point = pline.getStartPoint();
									points.add(new Vector3d(point.getX(), point.getY(), point.getZ()));
									point = pline.getEndPoint();
									Vector3d center=new Vector3d(point.getX(), point.getY(), point.getZ());
									points.add(center);
									
								}
							}
							polys.add(Polygon.fromPoints(Extrude.toCCW(points)));
							points.clear();
						}
						else if (entityType.contentEquals(DXFConstants.ENTITY_TYPE_SPLINE)) {
							// get all polylines from the layer

							List plines = layer.getDXFEntities(entityType);
							if (plines != null) {
								for (Object p : plines) {
									DXFSpline pline = (DXFSpline) p;
									Iterator splinePointIterator = pline.getSplinePointIterator();
									if(splinePointIterator!=null)
										for (;splinePointIterator.hasNext();) {
											SplinePoint point =(SplinePoint) splinePointIterator.next();
											Vector3d center=new Vector3d(point.getX(), point.getY(), point.getZ());
											points.add(center);
										}
								}
							}
							polys.add(Polygon.fromPoints(Extrude.toCCW(points)));
							points.clear();
						}
						else {
							System.out.println("Found type: " + entityType);

						}
					}
				}
				return polys;
			}
		}
		return polys;
	}
}
