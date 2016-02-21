package com.neuronrobotics.bowlerstudio.threed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ShellType;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.util.FileChangeWatcher;
import com.neuronrobotics.sdk.util.IFileChangeListener;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import javafx.scene.control.ProgressIndicator;

public class MobileBaseCadManager {
	private ICadGenerator cadEngine;
	private MobileBase base;
	private ProgressIndicator pi;
	private File cadScript;
	private FileChangeWatcher watcher;
	private HashMap<DHParameterKinematics, FileChangeWatcher> dhCadWatchers = new HashMap<>();

	private HashMap<DHParameterKinematics, ICadGenerator> dhCadGen = new HashMap<>();
	private HashMap<DHParameterKinematics, ArrayList<CSG>> DHtoCadMap = new HashMap<>();
	private HashMap<MobileBase, ArrayList<CSG>> BasetoCadMap = new HashMap<>();
	private boolean cadGenerating = false;
	private boolean showingStl=false;
	private ArrayList<CSG> allCad;
	
	
	public MobileBaseCadManager(MobileBase base,ProgressIndicator pi){
		this.pi = pi;
		setMobileBase(base);
		
	}
	public File getCadScript() {
		return cadScript;
	}

	public void setCadScript(File cadScript) {
		if (cadScript == null)
			return;
		if (watcher != null) {

			watcher.close();
		}
		try {
			watcher = new FileChangeWatcher(cadScript);
			watcher.addIFileChangeListener(new IFileChangeListener() {

				@Override
				public void onFileChange(File fileThatChanged, WatchEvent event) {
					try {
						cadEngine = (ICadGenerator) ScriptingEngine.inlineFileScriptRun(fileThatChanged, null);
						generateCad();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			watcher.start();
			base.addConnectionEventListener(new IDeviceConnectionEventListener() {
				
				@Override
				public void onDisconnect(BowlerAbstractDevice arg0) {
					if (watcher != null) {
						watcher.close();
					}
				}
				
				@Override
				public void onConnect(BowlerAbstractDevice arg0) {}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.cadScript = cadScript;
	}

	public ArrayList<CSG> generateBody(MobileBase base, boolean b) {
		pi.setProgress(0);
		setAllCad(new ArrayList<>());
		//DHtoCadMap = new HashMap<>();
		//private HashMap<MobileBase, ArrayList<CSG>> BasetoCadMap = new HashMap<>();
		
		if (MobileBase.class.isInstance(base)) {
			MobileBase device = base;
			if(BasetoCadMap.get(device)==null){
				BasetoCadMap.put(device, new ArrayList<CSG>());
			}
			
			if (cadEngine == null) {
				try {
					setDefaultLinkLevelCadEngine();
				} catch (Exception e) {
					BowlerStudioController.highlightException(null, e);
				}
				if (getCadScript() != null) {
					try {
						cadEngine = (ICadGenerator) ScriptingEngine.inlineFileScriptRun(getCadScript(), null);
					} catch (Exception e) {
						BowlerStudioController.highlightException(getCadScript(), e);
					}
				}
			}
			pi.setProgress(0.3);
			try {
				if(showingStl){
					//skip the regen
					for(CSG c:BasetoCadMap.get(device)){
						getAllCad().add(c);	
					}
				}else{
					setAllCad(cadEngine.generateBody(device));
					ArrayList<CSG> arrayList = BasetoCadMap.get(device);
					arrayList.clear();
					for(CSG c:getAllCad()){
						arrayList.add(c);	
					}
				}
			} catch (Exception e) {
				BowlerStudioController.highlightException(getCadScript(), e);
			}
			// clears old robot and places base
			BowlerStudioController.setCsg(BasetoCadMap.get(device),getCadScript());

			pi.setProgress(0.4);
			ArrayList<DHParameterKinematics> limbs = base.getAllDHChains();
			double numLimbs = limbs.size();
			double i = 0;
			for (DHParameterKinematics l : limbs) {
				if(DHtoCadMap.get(l)==null){
					DHtoCadMap.put(l, new ArrayList<CSG>());
				}
				ArrayList<CSG> arrayList = DHtoCadMap.get(l);
				if(showingStl){
					for (CSG csg : arrayList) {
						getAllCad().add(csg);
						BowlerStudioController.addCsg(csg,getCadScript());
					}
				}else{
					arrayList.clear();
					for (CSG csg : generateCad(l, b)) {
						getAllCad().add(csg);
						arrayList.add(csg);
						BowlerStudioController.addCsg(csg,getCadScript());
					}
				}

				i += 1;
				double progress = (1.0 - ((numLimbs - i) / numLimbs)) / 2;
				// System.out.println(progress);
				pi.setProgress(0.5 + progress);
			}

		} 

		showingStl=false;
		pi.setProgress(1);
		return getAllCad();
	}

	public ArrayList<File> generateStls(MobileBase base, File baseDirForFiles) throws IOException {
		ArrayList<File> allCadStl = new ArrayList<>();
		int leg = 0;
		ArrayList<DHParameterKinematics> limbs = base.getAllDHChains();
		double numLimbs = limbs.size();
		int i = 0;
		// Start by generating the legs using the DH link based generator
		ArrayList<CSG> totalAssembly=new ArrayList<>();
		double offset=0;
		for (i=0;i<limbs.size();i+=1) {
			
			double progress = (1.0 - ((numLimbs - i) / numLimbs)) / 2;
			pi.setProgress( progress);
			
			CSG legAssembly=null;
			DHParameterKinematics l = limbs.get(i);
			for (CSG csg : DHtoCadMap.get(l)) {
				csg = csg.prepForManufacturing();
				if(legAssembly==null)
					legAssembly=csg;
				else{
					legAssembly = legAssembly
							.union(csg
									.movey(.5+legAssembly.getMaxY()+Math.abs(csg.getMinY()))
									)
							;
				}
//				legAssembly.setManufactuing(new PrepForManufacturing() {
//					public CSG prep(CSG arg0) {
//						return null;
//					}
//				});
			}
			offset = -2-((legAssembly.getMaxX()+legAssembly.getMinX())*i);
			legAssembly=legAssembly.movex(offset);
			File dir = new File(
					baseDirForFiles.getAbsolutePath() + "/" + 
					base.getScriptingName() + "/" + 
					l.getScriptingName());
			if (!dir.exists())
				dir.mkdirs();
			File stl = new File(dir.getAbsolutePath() + "/Leg_" + leg + ".stl");
			FileUtil.write(Paths.get(stl.getAbsolutePath()), legAssembly.toStlString());
			allCadStl.add(stl);
			totalAssembly.add(legAssembly);
			BowlerStudioController.setCsg(totalAssembly,getCadScript());
			leg++;
		}
		
		int link = 0;
		// now we genrate the base pieces
		for (CSG csg : BasetoCadMap.get(base)) {
			csg=csg
					.prepForManufacturing()
					.toYMin()
					.movex(-2-csg.getMaxX()+offset);
			File dir = new File(baseDirForFiles.getAbsolutePath() + "/" + base.getScriptingName() + "/");
			if (!dir.exists())
				dir.mkdirs();
			File stl = new File(dir.getAbsolutePath() + "/Body_part_" + link + ".stl");
			FileUtil.write(Paths.get(stl.getAbsolutePath()), csg.toStlString());
			allCadStl.add(stl);
			totalAssembly.add(csg);
			BowlerStudioController.setCsg(totalAssembly,getCadScript());
			link++;
		}
//		BowlerStudioController.setCsg(BasetoCadMap.get(base),getCadScript());
//		for(CSG c: DHtoCadMap.get(base.getAllDHChains().get(0))){
//			BowlerStudioController.addCsg(c,getCadScript());
//		}
		showingStl=true;
		pi.setProgress(1);
		return allCadStl;
	}


	public MobileBase getMobileBase() {
		return base;
	}

	public void setMobileBase(MobileBase base) {
		this.base = base;
	}
	
	public ArrayList<CSG> generateCad(DHParameterKinematics dh, boolean b) {
		ArrayList<CSG> dhLinks = new ArrayList<>();
		if (getCadScript() != null) {
			try {
				cadEngine = (ICadGenerator) ScriptingEngine.inlineFileScriptRun(getCadScript(), null);
			} catch (Exception e) {
				BowlerStudioController.highlightException(getCadScript(), e);
			}
		}
		if (cadEngine == null) {
			try {
				setDefaultLinkLevelCadEngine();
			} catch (Exception e) {
				BowlerStudioController.highlightException(getCadScript(), e);
			}
		}
		try {
			if (dhCadGen.get(dh) != null) {
				try {
					for(int i=0;i<dh.getNumberOfLinks();i++){
						ArrayList<CSG> tmp=dhCadGen.get(dh).generateCad(dh, i);
						for(CSG c:tmp)
							dhLinks.add(c);
					}
					return dhLinks;
				} catch (Exception e) {
					BowlerStudioController.highlightException(dhCadWatchers.get(dh).getFileToWatch(), e);
				}
			}
			for(int i=0;i<dh.getNumberOfLinks();i++){
				ArrayList<CSG> tmp=cadEngine.generateCad(dh, i);
				for(CSG c:tmp)
					dhLinks.add(c);
			}
			return dhLinks;
		} catch (Exception e) {
			BowlerStudioController.highlightException(getCadScript(), e);
		}
		return null;

	}

	
	public synchronized void generateCad() {
		if (cadGenerating)
			return;
		cadGenerating = true;
		// new RuntimeException().printStackTrace();
		new Thread(()->{
			System.out.print("\r\nGenerating cad...");
			// new Exception().printStackTrace();
			MobileBase device = base;
			setAllCad(generateBody(device, false));

			System.out.print("Done!\r\n");
			BowlerStudioController.setCsg(this,getCadScript());
			cadGenerating = false;
		}).start();
	}
	private void setDefaultLinkLevelCadEngine() throws Exception {
		String[] cad = null;
			cad = (base).getGitCadEngine();

		if (cadEngine == null) {
			setGitCadEngine(cad[0], cad[1], base);
		}
	}
	public void onTabClosing() {
		// TODO Auto-generated method stub
		if (watcher != null) {
			watcher.close();
		}
	}
	public void setGitCadEngine(String gitsId, String file, DHParameterKinematics dh)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		dh.setGitCadEngine(new String[]{gitsId,file});
		File code = ScriptingEngine.fileFromGit(gitsId, file);
		try {
			ICadGenerator defaultDHSolver = (ICadGenerator) ScriptingEngine.inlineScriptRun(code, null,
					ShellType.GROOVY);
			dhCadGen.put(dh, defaultDHSolver);
		} catch (Exception e) {
			BowlerStudioController.highlightException(code, e);
		}

		if (dhCadWatchers.get(dh) != null) {
			dhCadWatchers.get(dh).close();
		}
		FileChangeWatcher w = new FileChangeWatcher(code);
		dhCadWatchers.put(dh, w);
		dh.addConnectionEventListener(new IDeviceConnectionEventListener() {
			
			@Override
			public void onDisconnect(BowlerAbstractDevice arg0) {
				w.close();
			}
			
			@Override
			public void onConnect(BowlerAbstractDevice arg0) {}
		});
		w.addIFileChangeListener((fileThatChanged, event) -> {
			try {
				ICadGenerator d = (ICadGenerator) ScriptingEngine.inlineScriptRun(code, null, ShellType.GROOVY);
				dhCadGen.put(dh, d);
				generateCad();
			} catch (Exception ex) {
				BowlerStudioController.highlightException(code, ex);
			}
		});
		w.start();

	}
	
	public void setGitCadEngine(String gitsId, String file, MobileBase device)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		setCadScript(ScriptingEngine.fileFromGit(gitsId, file));
		device.setGitCadEngine(new String[]{gitsId,file});
	}
	public ArrayList<CSG> getAllCad() {
		return allCad;
	}
	public void setAllCad(ArrayList<CSG> allCad) {
		this.allCad = allCad;
	}

}
