package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.creature.IMobileBaseUI;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTab;
import com.neuronrobotics.bowlerstudio.threed.Line3D;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DMDevice;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vertex;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

import javax.swing.text.BadLocationException;

import java.awt.Color;
//import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
		public void highlightException(File fileEngineRunByName, Exception ex) {
			BowlerStudioController.highlightException(fileEngineRunByName, ex);
		}

		@Override
		public void setAllCSG(Collection<CSG> toAdd, File source) {
			try {
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
		ConfigurationDatabase.setObject("BowlerStudioConfigs", "fontsize", size);
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
			Log.warning("Loading local file from: " + file.getAbsolutePath());
			LocalFileScriptTab t = new LocalFileScriptTab(file);
			
			new Thread() {
				public void run() {
					BowlerStudioMenuWorkspace.add(t.getScripting().getGitRepo());
				}
			}.start();

			String key = t.getScripting().getGitRepo() + ":" + t.getScripting().getGitFile();
			ArrayList<String> files = new ArrayList<>();
			files.add(t.getScripting().getGitRepo());
			files.add(t.getScripting().getGitFile());
			if(key.length()>3 && files.get(0).length()>0 && files.get(1).length()>0)// catch degenerates
				ConfigurationDatabase.setObject("studio-open-git", key, files);

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
		} catch (BadLocationException e) {
			// e.printStackTrace();
		}
	}

	public static void highlightException(File fileEngineRunByName, Throwable ex) {
		if(bowlerStudioControllerStaticReference!=null)
			bowlerStudioControllerStaticReference.highlightExceptionLocal(fileEngineRunByName, ex);
	}

	public static void clearHighlight() {
		bowlerStudioControllerStaticReference.clearHighlits();
	}

	private void highlightExceptionLocal(File fileEngineRunByName, Throwable ex) {
		// THis needs to gate on checking if this thread is running already
		if(runningExceptionHighlight) {
			ex.printStackTrace();
			
			new RuntimeException("Only one exception Highlight can be called at once!").printStackTrace();
			return;
		}
		
		new Thread() {
			public void run() {
				runningExceptionHighlight=true;
				setName("Highlighter thread");
				if (fileEngineRunByName != null) {
					if (openFiles.get(fileEngineRunByName.getAbsolutePath()) == null) {
						createFileTab(fileEngineRunByName);
					}
					BowlerStudioModularFrame.getBowlerStudioModularFrame()
							.setSelectedTab(openFiles.get(fileEngineRunByName.getAbsolutePath()));
					try {
						widgets.get(fileEngineRunByName.getAbsolutePath()).clearHighlits();
					}catch(java.lang.NullPointerException e) {
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
					String sw = org.apache.commons.lang.exception.ExceptionUtils
							.getStackTrace(ex);
					System.out.println(sw.toString());
					// space out the exception highlights, ensure any sub threads spawned here have time to finish
					Thread.sleep(100);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				runningExceptionHighlight=false;
			}
		}.start();

	}

	public void addTab(Tab tab, boolean closable) {

		// new RuntimeException().printStackTrace();

		Platform.runLater(() -> {
			BowlerStudioModularFrame.getBowlerStudioModularFrame().addTab(tab, closable);
		});

	}
	
	public static boolean removeObject(Object p) {
		if (CSG.class.isInstance(p) ) {
			Platform.runLater(() -> {
				CreatureLab3dController.getEngine().removeObject((CSG) p);
			});
			return true;
		}
		if ( Node.class.isInstance(p) || Polygon.class.isInstance(p)) {
			Platform.runLater(() -> {
				CreatureLab3dController.getEngine().clearUserNode();
			});
			return true;
		}
		// ThreadUtil.wait(20);
		return false;
	}


//	private boolean removeObject(Object p) {
//		if (CSG.class.isInstance(p) || Node.class.isInstance(p) || Polygon.class.isInstance(p)) {
//			Platform.runLater(() -> {
//				CreatureLab3dController.getEngine().removeObjects();
//				CreatureLab3dController.getEngine().clearUserNode();
//			});
//			return true;
//		}
//		// ThreadUtil.wait(20);
//		return false;
//	}

	public static void setCsg(List<CSG> toadd, File source) {
		Platform.runLater(() -> {
			CreatureLab3dController.getEngine().removeObjects();
			if (toadd != null)
				for (CSG c : toadd) {
					if(c!=null)
					Platform.runLater(() -> CreatureLab3dController.getEngine().addObject(c, source));
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
		Platform.runLater(() -> {
			CreatureLab3dController.getEngine().clearUserNode();
			if (toadd != null)
				for (Node c : toadd) {
					CreatureLab3dController.getEngine().addUserNode(c);
				}
		});
	}

	public static void addUserNode(Node toadd) {
		Platform.runLater(() -> {
			if (toadd != null)
				CreatureLab3dController.getEngine().addUserNode(toadd);

		});
	}
	
	public static void setSelectedCsg(CSG obj) {
		CreatureLab3dController.getEngine().setSelectedCsg(obj);
	}

	public static void addCsg(CSG toadd, File source) {
		Platform.runLater(() -> {
			if (toadd != null)
				CreatureLab3dController.getEngine().addObject(toadd, source);

		});
	}

	public void addObject(Object o, File source) {

		if (List.class.isInstance(o)) {
			List<Object> c = (List<Object>) o;
			for (int i = 0; i < c.size(); i++) {
				// Log.warning("Loading array Lists with removals " + c.get(i));
				addObject(c.get(i), source);
			}
			return;
		}

		if (CSG.class.isInstance(o)) {
			CSG csg = (CSG) o;
			Platform.runLater(() -> {
				// new RuntimeException().printStackTrace();
				CreatureLab3dController.getEngine().addObject(csg, source);
			});
			return;

		} else if (Tab.class.isInstance(o)) {

			addTab((Tab) o, true);
			return;

		} else if (Node.class.isInstance(o)) {

			addNode((Node) o);
			return;

		} else if (Polygon.class.isInstance(o)) {
			Polygon p = (Polygon) o;
			List<Vertex> vertices = p.vertices;
			javafx.scene.paint.Color color = new javafx.scene.paint.Color(Math.random() * 0.5 + 0.5,
					Math.random() * 0.5 + 0.5, Math.random() * 0.5 + 0.5, 1);
			double stroke = 0.5;
			for (int i = 1; i < vertices.size(); i++) {
				Line3D line = new Line3D(vertices.get(i - 1), vertices.get(i));
				line.setStrokeWidth(stroke);
				line.setStroke(color);
				addNode(line);
			}
			// Connecting line
			Line3D line = new Line3D(vertices.get(0), vertices.get(vertices.size() - 1));
			line.setStrokeWidth(stroke);
			line.setStroke(color);
			addNode(line);
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
		if (List.class.isInstance(result)) {
			List<Object> c = (List<Object>) result;
			for (int i = 0; i < c.size(); i++) {
				// Log.warning("Loading array Lists with removals " + c.get(i));
				addObject(c.get(i), source);
			}
		} else {
			addObject(result, source);
		}
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
		Platform.runLater(() -> {
			CreatureLab3dController.getEngine().removeObjects();
		});
	}

	public static void setCsg(CSG legAssembly, File cadScript) {
		Platform.runLater(() -> {
			CreatureLab3dController.getEngine().removeObjects();
			if (legAssembly != null)

				Platform.runLater(() -> CreatureLab3dController.getEngine().addObject(legAssembly, cadScript));

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