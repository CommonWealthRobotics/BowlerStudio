package com.neuronrobotics.jniloader;

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
			System.loadLibrary("/usr/local/share/OpenCV/java/lib"+Core.NATIVE_LIBRARY_NAME+".so");
		}else
		if(NativeResource.isOSX())
			resource.load("lib"+Core.NATIVE_LIBRARY_NAME);
		else{
			throw new RuntimeErrorException(null);
		}
	
		Mat m  = Mat.eye(3, 3, CvType.CV_8UC1);
        //System.out.println("m = " + m.dump());
		
	}

}
