package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.creature.IMobileBaseUI;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.bowlerstudio.printbed.PrintBedManager;
import com.neuronrobotics.bowlerstudio.scripting.CaDoodleLoader;
import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.scripting.cadoodle.CaDoodleFile;
import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTab;
import com.neuronrobotics.bowlerstudio.util.FileChangeWatcher;
import com.neuronrobotics.bowlerstudio.util.IFileChangeListener;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DMDevice;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.CSGtoJavafx;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.MeshContainer;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

import javax.swing.text.BadLocationException;

import org.eclipse.jgit.api.Git;

import java.awt.Color;
//import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings("restriction")
public class BowlerStudioController implements IScriptEventListener {

	/**
	 * 
	 */
	private ConnectionManager connectionManager;
	private AbstractImageProvider vrCamera;
	private static BowlerStudioController bowlerStudioControllerStaticReference = null;
	private boolean doneLoadingTutorials = false;
	private boolean runningExceptionHighlight = false;

	public BowlerStudioController() {
		if (getBowlerStudio() != null)
			throw new RuntimeException("There can be only one Bowler Studio controller");
		bowlerStudioControllerStaticReference = this;
		size = ((Number) ConfigurationDatabase.getObject("BowlerStudioConfigs", "fontsize", 12)).intValue();

	}

	private HashMap<String, Tab> openFiles = new HashMap<>();
	private HashMap<String, LocalFileScriptTab> widgets = new HashMap<>();
	private int size;

	private static IMobileBaseUI mbui = new IMobileBaseUI() {

		@Override
		public void highlightException(File fileEngineRunByName, Throwable ex) {
			BowlerStudioController.highlightException(fileEngineRunByName, ex);
		}

		@Override
		public void setAllCSG(Collection<CSG> toAdd, File source) {
			try {
				if (toAdd != null)
					BowlerStudioController.setCsg(new ArrayList<>(toAdd));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		@Override
		public void addCSG(Collection<CSG> toAdd, File source) {
			// TODO Auto-generated method stub
			for (CSG b : toAdd)
				BowlerStudioController.addCsg(b);
		}

		@Override
		public Set<CSG> getVisibleCSGs() {
			return CreatureLab3dController.getEngine().getCsgMap().keySet();
		}

		@Override
		public void setSelectedCsg(Collection<CSG> selectedCsg) {
			CreatureLab3dController.getEngine().setSelectedCsg(new ArrayList<>(selectedCsg));

		}

		@Override
		public void setSelected(Affine rootListener) {
			CreatureLab3dController.getEngine().setSelected(rootListener);
		}
	};

	public void setFontSize(int size) {
		this.size = size;
		for (String key : widgets.keySet()) {
			widgets.get(key).setFontSize(size);
		}
	}

	// Custom function for creation of New Tabs.
	public ScriptingFileWidget createFileTab(File file) {
		if (openFiles.get(file.getAbsolutePath()) != null && widgets.get(file.getAbsolutePath()) != null) {
			BowlerStudioModularFrame.getBowlerStudioModularFrame()
					.setSelectedTab(openFiles.get(file.getAbsolutePath()));
			return widgets.get(file.getAbsolutePath()).getScripting();
		}

		Tab fileTab = new Tab(file.getName());
		openFiles.put(file.getAbsolutePath(), fileTab);

		try {
			System.err.println("Loading local file from: " + file.getAbsolutePath());
			LocalFileScriptTab t = new LocalFileScriptTab(file);

			new Thread() {
				public void run() {
					String gitRepo = t.getScripting().getGitRepo();
					if (gitRepo != null) {
						String message = BowlerStudioMenu.gitURLtoMessage(gitRepo);
						if (gitRepo.length() < 5 || (message == null))
							message = "Project " + gitRepo;
						BowlerStudioMenuWorkspace.add(gitRepo, message);
					}
				}
			}.start();

			String key = t.getScripting().getGitRepo() + ":" + t.getScripting().getGitFile();
			ArrayList<String> files = new ArrayList<>();
			files.add(t.getScripting().getGitRepo());
			files.add(t.getScripting().getGitFile());
			try {
				if (key.length() > 3 && files.get(0).length() > 0 && files.get(1).length() > 0)// catch degenerates
					ConfigurationDatabase.setObject("studio-open-git", key, files);
			} catch (java.lang.NullPointerException ex) {
				// file can not be opened
			}

			fileTab.setContent(t);
			fileTab.setGraphic(
					AssetFactory.loadIcon("Script-Tab-" + ScriptingEngine.getShellType(file.getName()) + ".png"));

			addTab(fileTab, true);
			widgets.put(file.getAbsolutePath(), t);
			fileTab.setOnCloseRequest(event -> {
				widgets.remove(file.getAbsolutePath());
				openFiles.remove(file.getAbsolutePath());
				ConfigurationDatabase.removeObject("studio-open-git", key);
				t.getScripting().close();
				System.out.println("Closing " + file.getAbsolutePath());
			});
			FileChangeWatcher watcher = FileChangeWatcher.watch(file);
			watcher.addIFileChangeListener(new IFileChangeListener() {

				@Override
				public void onFileDelete(File fileThatIsDeleted) {
					BowlerStudioModularFrame.getBowlerStudioModularFrame().closeTab(fileTab);
				}

				@Override
				public void onFileChange(File fileThatChanged, WatchEvent event) {
				}
			});

			t.setFontSize(size);
			return t.getScripting();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void clearHighlits() {
		for (Entry<String, LocalFileScriptTab> set : widgets.entrySet()) {
			set.getValue().clearHighlits();
		}
	}

	public void setHighlight(File fileEngineRunByName, int lineNumber, Color color) {
		// System.out.println("Highlighting line "+lineNumber+" in
		// "+fileEngineRunByName);
		if (openFiles.get(fileEngineRunByName.getAbsolutePath()) == null) {
			createFileTab(fileEngineRunByName);
			ThreadUtil.wait(100);
		}

		// BowlerStudioModularFrame.getBowlerStudioModularFrame().setSelectedTab(openFiles.get(fileEngineRunByName.getAbsolutePath()));
		// System.out.println("Highlighting "+fileEngineRunByName+" at line
		// "+lineNumber+" to color "+color);
		try {
			widgets.get(fileEngineRunByName.getAbsolutePath()).setHighlight(lineNumber, color);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void highlightException(File fileEngineRunByName, Throwable ex) {
		if (bowlerStudioControllerStaticReference != null)
			bowlerStudioControllerStaticReference.highlightExceptionLocal(fileEngineRunByName, ex);
	}

	public static void clearHighlight() {
		bowlerStudioControllerStaticReference.clearHighlits();
	}

	private void highlightExceptionLocal(File fileEngineRunByName, Throwable ex) {
		// THis needs to gate on checking if this thread is running already
		if (runningExceptionHighlight) {
			ex.printStackTrace();

			new RuntimeException("Only one exception Highlight can be called at once!").printStackTrace();
			return;
		}

		new Thread() {
			public void run() {
				runningExceptionHighlight = true;
				setName("Highlighter thread");
				if (fileEngineRunByName != null) {
					if (openFiles.get(fileEngineRunByName.getAbsolutePath()) == null) {
						createFileTab(fileEngineRunByName);
					}
					BowlerStudioModularFrame.getBowlerStudioModularFrame()
							.setSelectedTab(openFiles.get(fileEngineRunByName.getAbsolutePath()));
					try {
						widgets.get(fileEngineRunByName.getAbsolutePath()).clearHighlits();
					} catch (java.lang.NullPointerException e) {
						return;
					}
					// System.out.println("Highlighting "+fileEngineRunByName+" at line
					// "+lineNumber+" to color "+color);
					StackTraceElement[] stackTrace = ex.getStackTrace();

					for (StackTraceElement el : stackTrace) {
						try {
							// System.out.println("Compairing "+fileEngineRunByName.getName()+" to
							// "+el.getFileName());
							if (el.getFileName().contentEquals(fileEngineRunByName.getName())) {
								widgets.get(fileEngineRunByName.getAbsolutePath()).setHighlight(el.getLineNumber(),
										Color.CYAN);
							}
						} catch (Exception e) {
							// StringWriter sw = new StringWriter();
							// PrintWriter pw = new PrintWriter(sw);
							// e.printStackTrace(pw);
							// System.out.println(sw.toString());
						}
					}
					if (ex.getCause() != null) {
						for (StackTraceElement el : ex.getCause().getStackTrace()) {
							try {
								// System.out.println("Compairing "+fileEngineRunByName.getName()+" to
								// "+el.getFileName());
								if (el.getFileName().contentEquals(fileEngineRunByName.getName())) {
									widgets.get(fileEngineRunByName.getAbsolutePath()).setHighlight(el.getLineNumber(),
											Color.CYAN);
								}
							} catch (Exception e) {
								// StringWriter sw = new StringWriter();
								// PrintWriter pw = new PrintWriter(sw);
								// e.printStackTrace(pw);
								// System.out.println(sw.toString());
							}
						}
					}

				}
				try {
					if (widgets.get(fileEngineRunByName.getAbsolutePath()) != null) {
						String message = ex.getMessage();
						// System.out.println(message);
						if (message != null && message.contains(fileEngineRunByName.getName()))
							try {
								int indexOfFile = message.lastIndexOf(fileEngineRunByName.getName());
								String fileSub = message.substring(indexOfFile);
								String[] fileAndNum = fileSub.split(":");
								String FileNum = fileAndNum[1];
								int linNum = Integer.parseInt(FileNum.trim());
								widgets.get(fileEngineRunByName.getAbsolutePath()).setHighlight(linNum, Color.CYAN);
							} catch (Exception e) {
								StringWriter sw = new StringWriter();
								PrintWriter pw = new PrintWriter(sw);
								e.printStackTrace(pw);
								System.out.println(sw.toString());
							}
					}
				} catch (Exception ex1) {

				}

				try {
					String sw = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex);
					System.out.println(sw.toString());
					// space out the exception highlights, ensure any sub threads spawned here have
					// time to finish
					Thread.sleep(100);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				runningExceptionHighlight = false;
			}
		}.start();

	}

	public void addTab(Tab tab, boolean closable) {

		Exception ex = new RuntimeException();

		BowlerStudio.runLater(() -> {
			BowlerStudioModularFrame.getBowlerStudioModularFrame().addTab(tab, closable);
		}, ex);

	}

	public static boolean removeObject(Object p) {
		if (CSG.class.isInstance(p)) {
			BowlerStudio.runLater(() -> {
				CreatureLab3dController.getEngine().removeObject((CSG) p);
			});
			return true;
		}
		if (Node.class.isInstance(p) || Polygon.class.isInstance(p)) {
			BowlerStudio.runLater(() -> {
				CreatureLab3dController.getEngine().clearUserNode();
			});
			return true;
		}
		if (Tab.class.isInstance(p)) {
			Tab newTab = (Tab) p;
			BowlerStudioModularFrame.getBowlerStudioModularFrame().closeTab(newTab);
			return true;
		}
		// ThreadUtil.wait(20);
		return false;
	}

//	private boolean removeObject(Object p) {
//		if (CSG.class.isInstance(p) || Node.class.isInstance(p) || Polygon.class.isInstance(p)) {
//			BowlerStudio.runLater(() -> {
//				CreatureLab3dController.getEngine().removeObjects();
//				CreatureLab3dController.getEngine().clearUserNode();
//			});
//			return true;
//		}
//		// ThreadUtil.wait(20);
//		return false;
//	}

	public static void setCsg(List<CSG> toadd, File source) {
		BowlerStudio.runLater(() -> {
			CreatureLab3dController.getEngine().removeObjects();
			if (toadd != null)
				for (CSG c : toadd) {
					if (c != null)
						BowlerStudio.runLater(() -> CreatureLab3dController.getEngine().addObject(c, source));
				}
		});
	}

	public static void setCsg(List<CSG> toadd) {
		setCsg(toadd, null);
	}

	public static void addCsg(CSG toadd) {
		addCsg(toadd, null);
	}

	public static void setUserNode(List<Node> toadd) {
		BowlerStudio.runLater(() -> {
			CreatureLab3dController.getEngine().clearUserNode();
			if (toadd != null)
				for (Node c : toadd) {
					CreatureLab3dController.getEngine().addUserNode(c);
				}
		});
	}

	public static void addUserNode(Node toadd) {
		BowlerStudio.runLater(() -> {
			if (toadd != null)
				CreatureLab3dController.getEngine().addUserNode(toadd);

		});
	}

	public static void setSelectedCsg(CSG obj) {
		CreatureLab3dController.getEngine().setSelectedCsg(obj);
	}
	public static void highlightCsg(CSG obj) {
		CreatureLab3dController.getEngine().setSelectedCsg(obj,true);
	}
	public static void setSelectedCsg(Vector3d v) {
		Affine manipulator2 = new Affine();
		TransformNR poseToMove = new TransformNR(v.x, v.y, v.z, new RotationNR());
		CreatureLab3dController.getEngine().focusToAffine(poseToMove, manipulator2);
	}
	public static void setSelectedCsg(TransformNR poseToMove) {
		Affine manipulator2 = new Affine();
		CreatureLab3dController.getEngine().focusToAffine(poseToMove, manipulator2);
	}
	public static void setSelectedAffine(TransformNR poseToMove, Affine manipulator2) {
		CreatureLab3dController.getEngine().focusToAffine(poseToMove, manipulator2);
	}
	public static void targetAndFollow(TransformNR poseToMove, Affine manipulator2) {
		CreatureLab3dController.getEngine().targetAndFollow(poseToMove, manipulator2);
	}
	public static void setSelectedAffine(Affine af) {
		CreatureLab3dController.getEngine().focusToAffine(af);
	}
	public static void addCsg(CSG toadd, File source) {
		BowlerStudio.runLater(() -> {
			if (toadd != null)
				CreatureLab3dController.getEngine().addObject(toadd, source);

		});
	}

	public static void addObject(Object o, File source) {
		addObject(o, source, null);
	}

	public static void addObject(Object o, File source, ArrayList<CSG> cache) {

		if (List.class.isInstance(o)) {
			List<Object> c = (List<Object>) o;
			for (int i = 0; i < c.size(); i++) {
				// Log.warning("Loading array Lists with removals " + c.get(i));
				addObject(c.get(i), source,cache);
			}
			return;
		}
		if(CaDoodleFile.class.isInstance(o)) {
			addObject(CaDoodleLoader.process((CaDoodleFile)o), source,cache);
			return;
		}
		if (CSG.class.isInstance(o)) {
			CSG csg = (CSG) o;
			if (cache == null) {
				BowlerStudio.runLater(() -> {
					// new RuntimeException().printStackTrace();
					CreatureLab3dController.getEngine().addObject(csg, source);
				});
			}else {
				cache.add(csg);
			}

			return;

		} else if (Tab.class.isInstance(o)) {

			getBowlerStudio().addTab((Tab) o, true);
			return;

		} else if (Node.class.isInstance(o)) {

			getBowlerStudio().addNode((Node) o);
			return;

		} else if (Polygon.class.isInstance(o)) {
			Polygon poly = (Polygon) o;
			List<Vertex> vertices = poly.vertices;
			javafx.scene.paint.Color color = new javafx.scene.paint.Color(Math.random() * 0.5 + 0.5,
					Math.random() * 0.5 + 0.5, Math.random() * 0.5 + 0.5, 1);
			double stroke = 0.5;
			BowlerStudio.runLater(()->{
				for (int i = 0; i < vertices.size(); i++) {
					CSG csg= new Cylinder(0,stroke/2,stroke,3).toCSG()
							.move(vertices.get(i))
							.setColor(new javafx.scene.paint.Color(Math.random() * 0.5 + 0.5,
									Math.random() * 0.5 + 0.5, Math.random() * 0.5 + 0.5, 1));
					csg.setIsWireFrame(true);
					getBowlerStudio().addNode(csg.getMesh());
				}
				for(Polygon p:PolygonUtil.concaveToConvex(poly)) {
					MeshContainer mesh = CSGtoJavafx.meshFromPolygon(p);
					javafx.scene.shape.MeshView current = mesh.getAsMeshViews().get(0);
					current.setMaterial(new PhongMaterial(color));
					current.setCullFace(CullFace.NONE);
					getBowlerStudio().addNode(current);
				}
			});
			BowlerStudioController.setSelectedCsg(poly.vertices.get(0).pos);
			return;
		}else if (Vector3d.class.isInstance(o)) {
			Vector3d v=(Vector3d)o;
			BowlerStudioController.setSelectedCsg(v);
			return;
		}else if (TransformNR.class.isInstance(o)) {
			TransformNR v=(TransformNR)o;
			BowlerStudioController.setSelectedCsg(v);
			return;
		} else if (BowlerAbstractDevice.class.isInstance(o)) {
			BowlerAbstractDevice bad = (BowlerAbstractDevice) o;
			ConnectionManager.addConnection((BowlerAbstractDevice) o, bad.getScriptingName());
			return;
		} else if (DMDevice.wrappable(o)) {
			BowlerAbstractDevice bad;
			try {
				bad = new DMDevice(o);
				ConnectionManager.addConnection(bad, bad.getScriptingName());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void addNode(Node o) {
		CreatureLab3dController.getEngine().addUserNode(o);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onScriptFinished(Object result, Object Previous, File source) {
		Log.warning("Loading script results " + result + " previous " + Previous);
		// this is added in the script engine when the connection manager is
		// loaded
		clearObjects(Previous);
		clearObjects(result);
		ThreadUtil.wait(40);
		ArrayList<CSG> cache =  new ArrayList<>();
		if (List.class.isInstance(result)) {
			List<Object> c = (List<Object>) result;
			for (int i = 0; i < c.size(); i++) {
				// Log.warning("Loading array Lists with removals " + c.get(i));
				addObject(c.get(i), source,cache);
			}
		} else {
			addObject(result, source,cache);
		}
		if(cache.size()>0)
			addObject(cache, source,null);
//		String git;
//		try {
//			git = ScriptingEngine.locateGitUrl(source);
//			if(cache.size()>0) {
//				if(git!=null) {
//					PrintBedManager manager=new PrintBedManager(git,cache);
//					addObject(manager.get(), source);
//				}else {
//					addObject(cache, source,null);
//				}
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			addObject(cache, source,null);
//		}
		
	
	}

	private void clearObjects(Object o) {
		if (List.class.isInstance(o)) {
			@SuppressWarnings("unchecked")
			List<Object> c = (List<Object>) o;
			for (int i = 0; i < c.size(); i++) {
				clearObjects(c.get(i));
			}
		} else {
			removeObject(o);
		}
	}

	@Override
	public void onScriptChanged(String previous, String current, File source) {

	}

	@Override
	public void onScriptError(Throwable except, File source) {
		// TODO Auto-generated method stub

	}

	public void disconnect() {
		ConnectionManager.disconnectAll();
	}

	public Stage getPrimaryStage() {
		// TODO Auto-generated method stub
		return BowlerStudioModularFrame.getPrimaryStage();
	}

	public AbstractImageProvider getVrCamera() {
		return vrCamera;
	}

	public void setVrCamera(AbstractImageProvider vrCamera) {
		this.vrCamera = vrCamera;
	}

	public static BowlerStudioController getBowlerStudio() {
		return bowlerStudioControllerStaticReference;
	}

	public static void setup() {
		// TODO Auto-generated method stub

	}

	public static void clearCSG() {
		BowlerStudio.runLater(() -> {
			CreatureLab3dController.getEngine().removeObjects();
		});
	}
	public static void clearUserNodes() {
		BowlerStudio.runLater(() -> {
			CreatureLab3dController.getEngine().clearUserNode();
		});
	}
	public static void setCsg(CSG legAssembly, File cadScript) {
		BowlerStudio.runLater(() -> {
			CreatureLab3dController.getEngine().removeObjects();
			if (legAssembly != null)

				BowlerStudio.runLater(() -> CreatureLab3dController.getEngine().addObject(legAssembly, cadScript));

		});
	}

	public static void setCsg(MobileBaseCadManager thread, File cadScript) {
		setCsg(thread.getAllCad(), cadScript);
	}

	public boolean isDoneLoadingTutorials() {
		return doneLoadingTutorials;
	}

	public void setDoneLoadingTutorials(boolean doneLoadingTutorials) {
		this.doneLoadingTutorials = doneLoadingTutorials;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public static IMobileBaseUI getMobileBaseUI() {
		return mbui;
	}

	



}