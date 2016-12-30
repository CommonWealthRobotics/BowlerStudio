package com.neuronrobotics.bowlerstudio.assets;

import com.neuronrobotics.bowlerstudio.StudioBuildInfo;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class AssetFactory {
	public static final String repo = "BowlerStudioImageAssets";
	private static String gitSource = "https://github.com/madhephaestus/" + repo + ".git";
	private static HashMap<String , Image> cache =new HashMap<>();
	private static HashMap<String , FXMLLoader> loaders =new HashMap<>();
	private static String assetRepoBranch = "master";
	private static boolean checked =false;
	static{

			try {
				
				loadAllAssets();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	private AssetFactory() {
	}
	public static FXMLLoader loadLayout(String file ,boolean refresh) throws Exception{
		File fxmlFIle =loadFile(file);
		URL fileURL= fxmlFIle.toURI().toURL();
		//System.err.println("FXML from "+fileURL);
		if(loaders.get(file)==null || refresh){

			loaders.put(file, new FXMLLoader(fileURL));

		}
		loaders.get(file).setLocation(fileURL);
		return loaders.get(file);
	}
	public static FXMLLoader loadLayout(String file ) throws Exception{
		return loadLayout(file, false);
	}
	
	public static File loadFile(String file) throws Exception {
		
		return ScriptingEngine
		.fileFromGit(
				getGitSource(),// git repo, change this if you fork this demo
				getAssetRepoBranch(),
				file// File from within the Git repo
		);
	}

	public static Image loadAsset(String file ) throws Exception{
		
		if(cache.get(file)==null){
			File f =loadFile(file);
			if(f.getName().endsWith(".fxml")){
				loadLayout(file);
				return null;
			}else
			if((f==null || !f.exists()) && f.getName().endsWith(".png")){
				WritableImage obj_img =new WritableImage(30, 30);
			    byte alpha = (byte)0; 
			    for (int cx=0;cx<obj_img.getWidth();cx++) {          
			        for (int cy=0;cy<obj_img.getHeight();cy++) {
			            int color = obj_img.getPixelReader().getArgb(cx, cy);

			            int mc = (alpha << 24) | 0x00ffffff;
			            int newcolor = color & mc;
			            obj_img.getPixelWriter().setArgb(cx, cy, newcolor);            

			        }

			    }
			    cache.put(file, obj_img);
			    System.out.println("No image at "+file);
			    try {
					File imageFile = ScriptingEngine.createFile(getGitSource(),file,"create file");
					try {
						String FileName =imageFile.getName();

						BufferedImage bImage = SwingFXUtils.fromFXImage(obj_img, null);

						ImageIO.write(bImage, FileName
					    		.substring(FileName.lastIndexOf('.')+1).toLowerCase(), imageFile);

					} catch (IOException e) {
					    
					}
					ScriptingEngine.createFile(getGitSource(),file,"saving new content");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			    //ScriptingEngine.pushFile(getGitSource(),file);
			    //obj_img.ge
			}else{
				//System.out.println("Asset: "+f.getAbsolutePath());
				cache.put(file, new Image(f.toURI().toString()));
			}
			
		}
		
		return cache.get(file);
	}
	
	public static ImageView loadIcon(String file ) {
		try {
			return new ImageView(loadAsset(file));
		} catch (Exception e) {
			return new ImageView();
		}
	}
	
	public static String getGitSource() throws Exception {

		return gitSource;
	}
	public static void setGitSource(String gitSource,String assetRepoBranch) throws Exception {
		System.err.println("Using my version of assets: "+gitSource+":"+assetRepoBranch);
		setAssetRepoBranch( assetRepoBranch);
		AssetFactory.gitSource = gitSource;
		cache.clear();
		loadAllAssets();
	}
	private static void loadAllAssets() throws Exception{
		ArrayList<String> files = ScriptingEngine.filesInGit(gitSource,StudioBuildInfo.getVersion(), null);
		for(String file:files){
			loadAsset(file);
		}
	}
	public static String getAssetRepoBranch() {
		return assetRepoBranch;
	}
	public static void setAssetRepoBranch(String assetRepoBranch) {
		AssetFactory.assetRepoBranch = assetRepoBranch;
		
	}
	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
}
