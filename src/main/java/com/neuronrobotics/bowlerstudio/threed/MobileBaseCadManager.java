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
import org.python.core.exceptions;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.physics.MobileBasePhysicsManager;
import com.neuronrobotics.bowlerstudio.physics.PhysicsEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.nrconsole.util.FileWatchDeviceWrapper;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.ILinkListener;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.IDeviceConnectionEventListener;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;
import com.neuronrobotics.sdk.util.IFileChangeListener;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.FileUtil;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;

public class MobileBaseCadManager {
	
	//static
	private static HashMap<MobileBase,MobileBaseCadManager> cadmap = new HashMap<>();
	//static
	private ICadGenerator cadEngine;
	private MobileBase base;
	private ProgressIndicator pi;
	private File cadScript;

	private HashMap<DHParameterKinematics, ICadGenerator> dhCadGen = new HashMap<>();
	private HashMap<DHParameterKinematics, ArrayList<CSG>> DHtoCadMap = new HashMap<>();
	private HashMap<LinkConfiguration, ArrayList<CSG>> LinktoCadMap = new HashMap<>();
	private HashMap<MobileBase, ArrayList<CSG>> BasetoCadMap = new HashMap<>();

	private boolean cadGenerating = false;
	private boolean showingStl=false;
	private ArrayList<CSG> allCad;

	private CheckBox autoRegen;
	private boolean bail=false;
	private IFileChangeListener cadWatcher = new IFileChangeListener() {

		@Override
		public void onFileChange(File fileThatChanged, WatchEvent event) {
			try {
				System.out.println("Re-loading Cad Base Engine");
				cadEngine = (ICadGenerator) ScriptingEngine.inlineFileScriptRun(fileThatChanged, null);
				generateCad();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	public MobileBaseCadManager(MobileBase base,ProgressIndicator pi,CheckBox autoRegen){
		this.autoRegen = autoRegen;
		this.setProcesIndictor(pi);
		if(pi==null)
			this.setProcesIndictor(new ProgressIndicator());
		base.addConnectionEventListener(new IDeviceConnectionEventListener() {
			
			@Override
			public void onDisconnect(BowlerAbstractDevice arg0) {
				bail=true;
			}
			
			@Override
			public void onConnect(BowlerAbstractDevice arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		setMobileBase(base);
		//new Exception().printStackTrace();
	}
	public File getCadScript() {
		return cadScript;
	}

	public void setCadScript(File cadScript) {
		if (cadScript == null)
			return;
		FileWatchDeviceWrapper.watch(base, cadScript, cadWatcher);
		
		this.cadScript = cadScript;
	}

	public ArrayList<CSG> generateBody(MobileBase base) {
		getProcesIndictor().setProgress(0);
		setAllCad(new ArrayList<>());
		//DHtoCadMap = new HashMap<>();
		//private HashMap<MobileBase, ArrayList<CSG>> BasetoCadMap = new HashMap<>();
		
		
		MobileBase device = base;
		if(getBasetoCadMap().get(device)==null){
			getBasetoCadMap().put(device, new ArrayList<CSG>());
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
		getProcesIndictor().setProgress(0.3);
		try {
			getAllCad().clear();
			if(showingStl){
				//skip the regen
				for(CSG c:getBasetoCadMap().get(device)){
					getAllCad().add(c);	
				}
			}else{
				if(!bail){
					ArrayList<CSG>  newcad  = cadEngine.generateBody(device);
					for(CSG c:newcad){
						getAllCad().add(c);	
					}
				}else
					new Exception().printStackTrace();
				ArrayList<CSG> arrayList = getBasetoCadMap().get(device);
				arrayList.clear();
				for(CSG c:getAllCad()){
					arrayList.add(c);	
				}
				new Thread(()->{
					localGetBaseCad( device);// load the cad union in a thread to make it ready for physics
				}).start();
			}
		} catch (Exception e) {
			BowlerStudioController.highlightException(getCadScript(), e);
		}
		System.out.println("Displaying Body");
		getProcesIndictor().setProgress(0.35);
		// clears old robot and places base
		BowlerStudioController.setCsg(getBasetoCadMap().get(device),getCadScript());
		System.out.println("Rendering limbs");
		getProcesIndictor().setProgress(0.4);
		ArrayList<DHParameterKinematics> limbs = base.getAllDHChains();
		double numLimbs = limbs.size();
		double i = 0;
		for (DHParameterKinematics l : limbs) {
			if(getDHtoCadMap().get(l)==null){
				getDHtoCadMap().put(l, new ArrayList<CSG>());
			}
			ArrayList<CSG> arrayList = getDHtoCadMap().get(l);
			if(showingStl || !device.isAvailable()){
				for (CSG csg : arrayList) {
					getAllCad().add(csg);
					BowlerStudioController.addCsg(csg,getCadScript());
				}
			}else{
				
				arrayList.clear();
				ArrayList<CSG> linksCad = generateCad(l);
				
				for (CSG csg : linksCad) {
					getAllCad().add(csg);
					arrayList.add(csg);
					BowlerStudioController.addCsg(csg,getCadScript());
				}
				
			}

			i += 1;
			double progress = (1.0 - ((numLimbs - i) / numLimbs)) / 2;
			// System.out.println(progress);
			getProcesIndictor().setProgress(0.5 + progress);
		}

		

		showingStl=false;
		getProcesIndictor().setProgress(1);
//		PhysicsEngine.clear();
//		MobileBasePhysicsManager m = new MobileBasePhysicsManager(base, baseCad, getSimplecad());
//		PhysicsEngine.startPhysicsThread(50);
//		return PhysicsEngine.getCsgFromEngine();
		return getAllCad();
	}

	public ArrayList<File> generateStls(MobileBase base, File baseDirForFiles) throws IOException {
		ArrayList<File> allCadStl = new ArrayList<>();
		int leg = 0;
		ArrayList<DHParameterKinematics> limbs = base.getAllDHChains();
		double numLimbs = limbs.size();
		int i;
		// Start by generating the legs using the DH link based generator
		ArrayList<CSG> totalAssembly=new ArrayList<>();
		double offset=0;
		for (i=0;i<limbs.size();i+=1) {
			
			double progress = (1.0 - ((numLimbs - i) / numLimbs)) / 2;
			getProcesIndictor().setProgress( progress);
			
			CSG legAssembly=null;
			DHParameterKinematics l = limbs.get(i);
			for (CSG csg : getDHtoCadMap().get(l)) {
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
		for (CSG csg : getBasetoCadMap().get(base)) {
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
		getProcesIndictor().setProgress(1);
		return allCadStl;
	}


	public MobileBase getMobileBase() {
		return base;
	}

	public void setMobileBase(MobileBase base) {
		this.base = base;
		cadmap.put(base, this);

	}
	/**
	 * This function iterates through the links generating them
	 * @param dh
	 * @return
	 */
	public ArrayList<CSG> generateCad(DHParameterKinematics dh) {
		ArrayList<CSG> dhLinks = new ArrayList<>();

		if (cadEngine == null) {
			try {
				setDefaultLinkLevelCadEngine();
			} catch (Exception e) {
				BowlerStudioController.highlightException(getCadScript(), e);
			}
		}

		try {
			ICadGenerator generatorToUse=cadEngine;
			
			if (dhCadGen.get(dh) != null) {
				generatorToUse=dhCadGen.get(dh);
			}
			for(int i=0;i<dh.getNumberOfLinks();i++){

				if(!bail){
					ArrayList<CSG> tmp=generatorToUse.generateCad(dh, i);
					LinkConfiguration configuration = dh.getLinkConfiguration(i);
					if(getLinktoCadMap().get(configuration)==null){
						getLinktoCadMap().put(configuration, new ArrayList<>());
					}else
						getLinktoCadMap().get(configuration).clear();
					for(CSG c:tmp){
						dhLinks.add(c);
						getLinktoCadMap().get(configuration).add(c);// add to the regestration storage
					}
					AbstractLink link = dh.getFactory().getLink(configuration);
					link.addLinkListener(new ILinkListener() {
						
						@Override
						public void onLinkPositionUpdate(AbstractLink arg0, double arg1) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onLinkLimit(AbstractLink arg0, PIDLimitEvent arg1) {
							BowlerStudio.select( base,configuration);
							
						}
					});
					
				}
			}
			return dhLinks;
		} catch (Exception e) {
			BowlerStudioController.highlightException(getCadScript(), e);
		}
		return null;

	}


	
	public synchronized void generateCad() {
		if (cadGenerating || !autoRegen.isSelected())
			return;
		cadGenerating = true;
		// new RuntimeException().printStackTrace();
		//new Exception().printStackTrace();
		new Thread(){
			@Override
			public void run() {
				System.out.print("\r\nGenerating cad...");
				setName("MobileBaseCadManager Generating cad Thread ");
				// new Exception().printStackTrace();
				MobileBase device = base;
				try{
					setAllCad(generateBody(device));
				}catch(Exception e){
					BowlerStudioController.highlightException(getCadScript(), e);
				}
				System.out.print("Done!\r\n");
				BowlerStudioController.setCsg(MobileBaseCadManager.this,getCadScript());
				cadGenerating = false;
			}
		}.start();
	}
	private void setDefaultLinkLevelCadEngine() throws Exception {
		String[] cad;
			cad = base.getGitCadEngine();

		if (cadEngine == null) {
			setGitCadEngine(cad[0], cad[1], base);
		}
		for(DHParameterKinematics kin : base.getAllDHChains()){
			String[] kinEng = kin.getGitCadEngine();
			if(!cad[0].contentEquals(kinEng[0]) || !cad[1].contentEquals(kinEng[1])){
				setGitCadEngine(kinEng[0],kinEng[1],kin);
			}
		}
	}
	public void onTabClosing() {
		
	}
	public void setGitCadEngine(String gitsId, String file, DHParameterKinematics dh)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		dh.setGitCadEngine(new String[]{gitsId,file});
		File code = ScriptingEngine.fileFromGit(gitsId, file);
		try {
			ICadGenerator defaultDHSolver = (ICadGenerator) ScriptingEngine.inlineFileScriptRun(code, null);
			dhCadGen.put(dh, defaultDHSolver);
		} catch (Exception e) {
			BowlerStudioController.highlightException(code, e);
		}
		
		FileWatchDeviceWrapper.watch(dh, code, (fileThatChanged, event) -> {
			System.out.println("Re-loading Cad Limb Engine");

			try {
				ICadGenerator d = (ICadGenerator) ScriptingEngine.inlineFileScriptRun(code, null);
				dhCadGen.put(dh, d);
				generateCad();
			} catch (Exception ex) {
				BowlerStudioController.highlightException(code, ex);
			}
		});
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
		for(CSG part:allCad)
			for(String p:part .getParameters()){
				CSGDatabase.addParameterListener(p,(arg0, arg1) -> {
					//generateCad(); //TODO Undo this after debugging
				});
			}
		this.allCad = allCad;
	}
	 
	public static MobileBaseCadManager get(MobileBase device){
		if(cadmap.get(device)==null){
			CheckBox check = new CheckBox();
			check.setSelected(true);
			return new MobileBaseCadManager(device, new ProgressIndicator(), check);
		}
		else
			return cadmap.get(device);
	}
	
	public static HashMap<LinkConfiguration, ArrayList<CSG>> getSimplecad(MobileBase device) {
		return get(device).LinktoCadMap;
	}
	
	private ArrayList<CSG> localGetBaseCad(MobileBase device){


		return BasetoCadMap.get(device);
	}

	public static ArrayList<CSG> getBaseCad(MobileBase device) {
		return get(device).localGetBaseCad(device);
	}
	public ProgressIndicator getProcesIndictor() {
		return pi;
	}
	public void setProcesIndictor(ProgressIndicator pi) {
		this.pi = pi;
	}
	public HashMap<MobileBase, ArrayList<CSG>> getBasetoCadMap() {
		return BasetoCadMap;
	}
	public void setBasetoCadMap(HashMap<MobileBase, ArrayList<CSG>> basetoCadMap) {
		BasetoCadMap = basetoCadMap;
	}
	public HashMap<DHParameterKinematics, ArrayList<CSG>> getDHtoCadMap() {
		return DHtoCadMap;
	}
	public void setDHtoCadMap(HashMap<DHParameterKinematics, ArrayList<CSG>> dHtoCadMap) {
		DHtoCadMap = dHtoCadMap;
	}
	public HashMap<LinkConfiguration, ArrayList<CSG>> getLinktoCadMap() {
		return LinktoCadMap;
	}
	public void setLinktoCadMap(HashMap<LinkConfiguration, ArrayList<CSG>> linktoCadMap) {
		LinktoCadMap = linktoCadMap;
	}


}
