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
			String dir = "OpenCV-"+Core.VERSION.split(".0")[0];
			if(NativeResource.is64Bit()){
				System.load("C:\\"+dir+"\\build\\java\\x64\\"+Core.NATIVE_LIBRARY_NAME+".dll");
			}else{
				System.load("C:\\"+dir+"\\build\\java\\x86\\"+Core.NATIVE_LIBRARY_NAME+".dll");
			}
		}
		
		Mat m  = Mat.eye(3, 3, CvType.CV_8UC1);
        //System.out.println("m = " + m.dump());
		
	}

}
