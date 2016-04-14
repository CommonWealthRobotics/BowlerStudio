package com.neuronrobotics.bowlerstudio.assets;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

public class AssetFactory {
	private static final String repo = "BowlerStudioImageAssets";
	private static String gitSource = "https://github.com/madhephaestus/"+repo+".git";
	private static HashMap<String , BufferedImage> cache =new HashMap<>();
	private static boolean checked =false;
	public static BufferedImage loadAsset(String file ) throws InvalidRemoteException, TransportException, GitAPIException, IOException{
		if(cache.get(file)==null){
			File f =ScriptingEngine
			.fileFromGit(
					getGitSource(),// git repo, change this if you fork this demo
					file// File from within the Git repo
			);
			if(f==null){
				BufferedImage obj_img =new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
			    byte alpha = (byte)0; 
			    for (int cx=0;cx<obj_img.getWidth();cx++) {          
			        for (int cy=0;cy<obj_img.getHeight();cy++) {
			            int color = obj_img.getRGB(cx, cy);

			            int mc = (alpha << 24) | 0x00ffffff;
			            int newcolor = color & mc;
			            obj_img.setRGB(cx, cy, newcolor);            

			        }

			    }
			    cache.put(file, obj_img);
			    try {
					File imageFile = ScriptingEngine.createFile(getGitSource(),file,"create file");
					try {
						String FileName =imageFile.getName();
					    ImageIO.write(obj_img, FileName
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
				cache.put(file, ImageIO.read(f));
			}
			
		}
		
		return cache.get(file);
	}
	
	public static String getGitSource() throws IOException {
		if(!checked){
			checked=true;
			ScriptingEngine.setAutoupdate(true);
			org.kohsuke.github.GitHub github = ScriptingEngine.getGithub();
			GHMyself self = github.getMyself();
			Map<String, GHRepository> myPublic = self.getAllRepositories();
			for (String myRepo :myPublic.keySet()){
				if(myRepo.contentEquals(repo)){
					GHRepository ghrepo= myPublic.get(myRepo);
					String myAssets = ghrepo.getGitTransportUrl().replaceAll("git://", "http://");
					System.out.println("Using my version of assets: "+myAssets);
					setGitSource(myAssets);
				}
			}
		}
		return gitSource;
	}
	public static void setGitSource(String gitSource) {
		AssetFactory.gitSource = gitSource;
	}
}
