package com.neuronrobotics.jniloader;

import java.io.File;

import javax.management.RuntimeErrorException;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class OpenCVJNILoader {
	static NativeResource resource=null;
	public static void load() {
		if( resource!=null)
			return;
		resource= new NativeResource();
		
		if(NativeResource.isLinux()){
			String [] possibleLocals = new String[]{
					"/usr/local/share/OpenCV/java/lib"+Core.NATIVE_LIBRARY_NAME+".so",
					"/usr/lib/jni/lib"+Core.NATIVE_LIBRARY_NAME+".so"
			};
			String erBack ="";
			for(String lo:possibleLocals){
				if(new File(lo).exists()){
					try{
						System.load(lo);
						Mat m  = Mat.eye(3, 3, CvType.CV_8UC1);
						return;
					}catch(Error e){
						//try the next one
						erBack+=" "+e.getMessage();
						e.printStackTrace();
					}
				}else{
					erBack+="No file "+lo;
				}
			}
			
			throw new RuntimeException(erBack);
		}else
		if(NativeResource.isOSX())
			resource.load("lib"+Core.NATIVE_LIBRARY_NAME);
		else if(NativeResource.isWindows()){
			String basedir =System.getenv("OPENCV_DIR");
			if(basedir == null)
				throw new RuntimeException("OPENCV was not found, environment variable OPENCV_DIR needs to be set");
			System.err.println("OPENCV found at "+ basedir);
			basedir+="\\..\\..\\java\\";
			if(basedir.contains("x64")){
				System.load(basedir+"x64\\"+Core.NATIVE_LIBRARY_NAME+".dll");
			}else{
				System.load(basedir+"x86\\"+Core.NATIVE_LIBRARY_NAME+".dll");
			}
		}
		
		Mat m  = Mat.eye(3, 3, CvType.CV_8UC1);
        //System.out.println("m = " + m.dump());
		
	}

}
