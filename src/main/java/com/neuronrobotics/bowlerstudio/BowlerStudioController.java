package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.scripting.IScriptEventListener;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTab;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import com.neuronrobotics.bowlerstudio.threed.MobileBaseCadManager;
import com.neuronrobotics.imageprovider.AbstractImageProvider;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.CSG;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

@SuppressWarnings("restriction")
public class BowlerStudioController implements IScriptEventListener {
    private ConnectionManager connectionManager;
    private BowlerStudio3dEngine jfx3dmanager;
    private AbstractImageProvider vrCamera;
    private static BowlerStudioController bowlerStudioControllerStaticReference = null;
    private boolean doneLoadingTutorials = false;

    public BowlerStudioController(BowlerStudio3dEngine jfx3dmanager) {
        if (getBowlerStudio() != null)
            throw new RuntimeException("There can be only one Bowler Studio controller");
        bowlerStudioControllerStaticReference = this;
        this.setJfx3dmanager(jfx3dmanager);
    }

    private HashMap<String, Tab> openFiles = new HashMap<>();
    private HashMap<String, LocalFileScriptTab> widgets = new HashMap<>();

    // Custom function for creation of New Tabs.
    public ScriptingFileWidget createFileTab(File file) {
        if (openFiles.get(file.getAbsolutePath()) != null && widgets.get(file.getAbsolutePath()) != null) {
            BowlerStudioModularFrame.getBowlerStudioModularFrame().setSelectedTab(openFiles.get(file.getAbsolutePath()));
            return widgets.get(file.getAbsolutePath()).getScripting();
        }

        Tab fileTab = new Tab(file.getName());
        openFiles.put(file.getAbsolutePath(), fileTab);

        try {
            Log.warning("Loading local file from: " + file.getAbsolutePath());
            LocalFileScriptTab t = new LocalFileScriptTab(file);
            String key = t.getScripting().getGitRepo() + ":" + t.getScripting().getGitFile();
            ArrayList<String> files = new ArrayList<>();
            files.add(t.getScripting().getGitRepo());
            files.add(t.getScripting().getGitFile());
            ConfigurationDatabase.setObject("studio-open-git", key, files);

            fileTab.setContent(t);
            fileTab.setGraphic(AssetFactory.loadIcon("Script-Tab-" + ScriptingEngine.getShellType(file.getName()) + ".png"));
            addTab(fileTab, true);
            widgets.put(file.getAbsolutePath(), t);
            fileTab.setOnCloseRequest(event -> {
                widgets.remove(file.getAbsolutePath());
                openFiles.remove(file.getAbsolutePath());
                ConfigurationDatabase.removeObject("studio-open-git", key);
            });
            return t.getScripting();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearHighlits() {
        for (Entry<String, LocalFileScriptTab> set : widgets.entrySet())
            set.getValue().clearHighlits();
    }

    public void setHighlight(File fileEngineRunByName, int lineNumber, Color color) {
        System.out.println("Highlighting line " + lineNumber + " in " + fileEngineRunByName);

        if (openFiles.get(fileEngineRunByName.getAbsolutePath()) == null) {
            createFileTab(fileEngineRunByName);
            ThreadUtil.wait(100);
        }

        //BowlerStudioModularFrame.getBowlerStudioModularFrame().setSelectedTab(openFiles.get(fileEngineRunByName.getAbsolutePath()));
        //System.out.println("Highlighting "+fileEngineRunByName+" at line "+lineNumber+" to color "+color);
        try {
            widgets.get(fileEngineRunByName.getAbsolutePath()).setHighlight(lineNumber, color);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void highlightException(File fileEngineRunByName, Exception ex) {
        bowlerStudioControllerStaticReference.highlightExceptionLocal(fileEngineRunByName, ex);
    }

    public static void clearHighlight() {
        bowlerStudioControllerStaticReference.clearHighlits();
    }

    private void highlightExceptionLocal(File fileEngineRunByName, Exception ex) {
        new Thread() {
            public void run() {
                setName("Highlighter thread");
                if (fileEngineRunByName != null) {
                    if (openFiles.get(fileEngineRunByName.getAbsolutePath()) == null) {
                        createFileTab(fileEngineRunByName);
                    }
                    BowlerStudioModularFrame.getBowlerStudioModularFrame().setSelectedTab(openFiles.get(fileEngineRunByName.getAbsolutePath()));
                    widgets.get(fileEngineRunByName.getAbsolutePath()).clearHighlits();
                    //System.out.println("Highlighting "+fileEngineRunByName+" at line "+lineNumber+" to color "+color);
                    for (StackTraceElement el : ex.getStackTrace()) {
                        try {
                            //System.out.println("Compairing "+fileEngineRunByName.getName()+" to "+el.getFileName());
                            if (el.getFileName().contentEquals(fileEngineRunByName.getName())) {
                                widgets.get(fileEngineRunByName.getAbsolutePath()).setHighlight(el.getLineNumber(), Color.CYAN);
                            }
                        } catch (Exception e) {
//							StringWriter sw = new StringWriter();
//							PrintWriter pw = new PrintWriter(sw);
//							e.printStackTrace(pw);
//							System.out.println(sw.toString());
                        }
                    }
                }
                try {
                    if (widgets.get(fileEngineRunByName.getAbsolutePath()) != null) {
                        String message = ex.getMessage();
                        //System.out.println(message);
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
                } catch (Exception ignored) {
                }

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                System.out.println(sw.toString());
                ex.printStackTrace();
            }
        }.start();
    }

    public void addTab(Tab tab, boolean closable) {
        //new RuntimeException().printStackTrace();
        Platform.runLater(() -> BowlerStudioModularFrame.getBowlerStudioModularFrame().addTab(tab, closable));
    }

    private boolean removeObject(Object p) {
        if (p instanceof CSG) {
            Platform.runLater(() -> getJfx3dmanager().removeObjects());
            return true;
        } else if (p instanceof Node) {
            getJfx3dmanager().clearUserNode();
            return true;
        }
        ThreadUtil.wait(20);
        return false;
    }

    public static void setCsg(List<CSG> toadd, File source) {
        Platform.runLater(() -> {
            getBowlerStudio().getJfx3dmanager().removeObjects();
            if (toadd != null)
                for (CSG c : toadd)
                    Platform.runLater(() -> getBowlerStudio().getJfx3dmanager().addObject(c, source));
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
            getBowlerStudio().getJfx3dmanager().clearUserNode();
            if (toadd != null)
                for (Node c : toadd) {
                    getBowlerStudio().getJfx3dmanager().addUserNode(c);
                }
        });
    }

    public static void addUserNode(Node toadd) {
        Platform.runLater(() -> {
            if (toadd != null)
                getBowlerStudio().getJfx3dmanager().addUserNode(toadd);
        });
    }

    public static void addCsg(CSG toadd, File source) {
        Platform.runLater(() -> {
            if (toadd != null)
                getBowlerStudio().getJfx3dmanager().addObject(toadd, source);
        });
    }

    private void addObject(Object o, File source) {
        if (o instanceof List) {
            List<Object> c = Collections.singletonList(o);
            for (Object aC : c) {
                //Log.warning("Loading array Lists with removals " + c.get(i));
                addObject(aC, source);
            }
        } else if (o instanceof CSG) {
            CSG csg = (CSG) o;
            Platform.runLater(() -> getJfx3dmanager().addObject(csg, source));
        } else if (o instanceof Tab) {
            addTab((Tab) o, true);
        } else if (o instanceof Node) {
            addNode((Node) o);
        } else if (o instanceof BowlerAbstractDevice) {
            BowlerAbstractDevice bad = (BowlerAbstractDevice) o;
            ConnectionManager.addConnection((BowlerAbstractDevice) o, bad.getScriptingName());
        }
    }

    public void addNode(Node o) {
        getJfx3dmanager().addUserNode(o);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void onScriptFinished(Object result, Object Previous, File source) {
        Log.warning("Loading script results " + result + " previous "
                    + Previous);
        // this is added in the script engine when the connection manager is
        // loaded

        ThreadUtil.wait(20);

        clearObjects(Previous);

        //Check if a CSG is coming in and clear the screen first
        clearObjects(result);

        if (List.class.isInstance(result)) {
            List<Object> c = (List<Object>) result;
            for (Object aC : c) {
                //Log.warning("Loading array Lists with removals " + c.get(i));
                addObject(aC, source);
            }
        } else
            addObject(result, source);
    }

    private void clearObjects(Object o) {
        if (o instanceof List) {
            List<Object> c = Collections.singletonList(o);
            for (Object aC : c)
                removeObject(aC);
        } else
            removeObject(o);
    }

    @Override
    public void onScriptChanged(String previous, String current, File source) {
    }

    @Override
    public void onScriptError(Exception except, File source) {
    }

    public void disconnect() {
        ConnectionManager.disconnectAll();
    }

    public Stage getPrimaryStage() {
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
    }

    public static void clearCSG() {
        Platform.runLater(() -> getBowlerStudio().getJfx3dmanager().removeObjects());
    }

    public static void setCsg(CSG legAssembly, File cadScript) {
        Platform.runLater(() -> {
            getBowlerStudio().getJfx3dmanager().removeObjects();
            if (legAssembly != null)
                Platform.runLater(() -> getBowlerStudio().getJfx3dmanager().addObject(legAssembly, cadScript));
        });
    }

    public static void setCsg(MobileBaseCadManager thread, File cadScript) {
        setCsg(thread.getAllCad(), cadScript);
    }

    public BowlerStudio3dEngine getJfx3dmanager() {
        return jfx3dmanager;
    }

    private void setJfx3dmanager(BowlerStudio3dEngine jfx3dmanager) {
        this.jfx3dmanager = jfx3dmanager;
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
}
