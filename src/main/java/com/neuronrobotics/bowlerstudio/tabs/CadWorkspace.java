package com.neuronrobotics.bowlerstudio.tabs;

import java.util.List;
import java.util.Random;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;


//this is a test class for loading images and outline/contour them
public class CadWorkspace extends AbstractBowlerStudioTab {
	private boolean open = true;
	private String imagePath;



	public CadWorkspace(String anImagePath) { //supposing we get an image/file 
		super();						//when a CadWorkspace object is created
		this.imagePath = anImagePath;
	}


	@SuppressWarnings("null")
	public void FindContours()  {
		Mat src; Mat src_gray = null;
		int thresh = 100;
		int max_thresh = 255;

		/// Load source image and convert it to gray
		src = Highgui.imread( imagePath, 1 );

		/// Convert image to gray and blur it
		Imgproc.cvtColor( src, src_gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.blur( src_gray, src_gray,  new Size(3,3) );

		//***************************************************
		//Here we open the image window of the initial image
		/// Create Window

		Mat canny_output = null;
		List<MatOfPoint> contours = null;
		MatOfInt4 hierarchy = null;

		/// Detect edges using canny
		Imgproc.Canny( src_gray, canny_output, thresh, thresh*2, 3,true);
		/// Find contours
		Imgproc.findContours( canny_output, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0) );


		/// Draw contours
		Mat drawing = Mat.zeros( canny_output.size(), CvType.CV_8UC3 );
		for( int i = 0; i< contours.size(); i++ )
		{
			Random rn = new Random();
			int n = 255;
			int k1 = rn.nextInt() % n;
			int k2=rn.nextInt() % n;
			int k3=rn.nextInt() % n;
			Scalar color= new Scalar(  k1, k2, k3 );
			//Imgproc.drawContours( drawing, contours, i, color, 2, 8, hierarchy, 0, new Point() );
			MatOfPoint acountour = contours.get(i);
			
			//***************************************************
			/// Show in a window to be added
		}

	}


	@Override
	public void onTabClosing() {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getMyNameSpaces() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		// TODO Auto-generated method stub
		setGraphic(AssetFactory.loadIcon("2d-Cad-Workspace-Tab.png"));
	}

	@Override
	public void onTabReOpening() {
		// TODO Auto-generated method stub

	}

}
