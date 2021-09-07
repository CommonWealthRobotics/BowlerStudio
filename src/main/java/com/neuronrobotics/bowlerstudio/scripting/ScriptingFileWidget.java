package com.neuronrobotics.bowlerstudio.scripting;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.bowlerstudio.BowlerStudioController;
import com.neuronrobotics.bowlerstudio.ConnectionManager;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.util.FileChangeWatcher;
import com.neuronrobotics.bowlerstudio.util.IFileChangeListener;
//import com.neuronrobotics.imageprovider.OpenCVImageProvider;
import com.neuronrobotics.nrconsole.util.CommitWidget;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser.ExtensionFilter;
import org.eclipse.jgit.api.Git;
import org.reactfx.util.FxTimer;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Scanner;

@SuppressWarnings("unused")
public class ScriptingFileWidget extends BorderPane implements IFileChangeListener {

	private boolean running = false;
	private Thread scriptRunner = null;
	private Dimension codeDimentions = new Dimension(1168, 768);
	// Label fileLabel = new Label();
	private Object scriptResult;
	private String codeText = "";

	private ArrayList<IScriptEventListener> listeners = new ArrayList<>();

	private Button runfx = new Button("Run");
	private Button publish = new Button("Save");
	private CheckBox autoRun = new CheckBox();

	private String addr;
	boolean loadGist = false;

	private ScriptingWidgetType type;

	final TextField fileListBox = new TextField();
	final TextField fileNameBox = new TextField();
	private File currentFile = null;
	ExternalEditorController externalEditorController;
	private HBox controlPane;
	private String currentGist;
	private boolean updateneeded = false;
	private IScriptingLanguage langaugeType;
//	private ImageView image=new ImageView();
	private boolean isOwnedByLoggedInUser=false;
	private String remote;
	public ScriptingFileWidget(File currentFile) throws IOException {
		this(ScriptingWidgetType.FILE, currentFile);

		loadCodeFromFile(currentFile);
		isOwnedByLoggedInUser = ScriptingEngine.checkOwner(currentFile);

		//publish.setDisable(!isOwnedByLoggedInUser);
		runfx.setGraphic(AssetFactory.loadIcon("Run.png"));
		if(isOwnedByLoggedInUser)
			publish.setGraphic(AssetFactory.loadIcon("Publish.png"));
		else
			publish.setGraphic(AssetFactory.loadIcon("Fork.png"));

	}

	private void startStopAction() {
		runfx.setDisable(true);
		if (running)
			stop();
		else
			start();
		runfx.setDisable(false);
	}

	private ScriptingFileWidget(ScriptingWidgetType type, File currentFile) {
		this.type = type;
		this.currentFile = currentFile;
		runfx.setOnAction(e -> {
			new Thread() {
				public void run() {

					if (langaugeType.getIsTextFile())
						save();
					// do not attempt to save no binary files
					startStopAction();
				}
			}.start();
		});
		runfx.setTooltip(new Tooltip("Run this code and display the result"));
		publish.setOnAction(e -> {
			new Thread(() -> {
				if(isOwnedByLoggedInUser) {
					save();
					CommitWidget.commit(currentFile, getCode());
				}else {
					String reponame = currentFile.getName().split("\\.")[0]+"_"+PasswordManager.getLoginID();
					String content = getCode();
					String newGit;
					try {
						newGit = ScriptingEngine.fork(remote, reponame, "Making fork from git: "+remote);
						ScriptingEngine.pushCodeToGit(newGit, null, currentFile.getName(), content, "Tmp save during fork");
						File file = ScriptingEngine.fileFromGit(newGit, currentFile.getName());
						ScriptingEngine.deleteRepo(remote);
						Thread.sleep(500);
						BowlerStudio.createFileTab(file);
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
			}).start();

		});
		publish.setTooltip(new Tooltip("Save this code to Git"));
		autoRun.setTooltip(new Tooltip("Check to auto-run files on file change"));

		// Set up the run controls and the code area
		// The BorderPane has the same areas laid out as the
		// BorderLayout layout manager
		setPadding(new Insets(1, 0, 3, 10));

		controlPane = new HBox(20);
		double lengthScalar = fileNameBox.getFont().getSize() * 1.5;
		fileNameBox.textProperty().addListener((ov, prevText, currText) -> {
			// Do this in a Platform.runLater because of Textfield has no padding at first
			// time and so on
			Platform.runLater(() -> {
				Text text = new Text(currText);
				text.setFont(fileNameBox.getFont()); // Set the same font, so the size is the same
				double width = text.getLayoutBounds().getWidth() // This big is the Text in the TextField
						+ fileNameBox.getPadding().getLeft() + fileNameBox.getPadding().getRight() // Add the padding of
																									// the TextField
						+ lengthScalar; // Add some spacing
				fileNameBox.setPrefWidth(width); // Set the width
				fileNameBox.positionCaret(fileNameBox.getCaretPosition()); // If you remove this line, it flashes a
																			// little bit
			});
		});
		fileListBox.textProperty().addListener((ov, prevText, currText) -> {
			// Do this in a Platform.runLater because of Textfield has no padding at first
			// time and so on
			Platform.runLater(() -> {
				Text text = new Text(currText);
				text.setFont(fileListBox.getFont()); // Set the same font, so the size is the same
				double width = text.getLayoutBounds().getWidth() // This big is the Text in the TextField
						+ fileListBox.getPadding().getLeft() + fileListBox.getPadding().getRight() // Add the padding of
																									// the TextField
						+ lengthScalar; // Add some spacing
				fileListBox.setPrefWidth(width); // Set the width
				fileListBox.positionCaret(fileListBox.getCaretPosition()); // If you remove this line, it flashes a
																			// little bit
			});
		});
		externalEditorController = new ExternalEditorController(currentFile, autoRun);

		Button openFile = new Button("Open...");
		openFile.setOnAction(event -> {
			new Thread(() -> {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.open(currentFile.getParentFile());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}).start();

		});
		openFile.setTooltip(new Tooltip("Click here to open the file in the OS browser"));
		controlPane.getChildren().add(runfx);
		controlPane.getChildren().add(externalEditorController.getControl());
		controlPane.getChildren().add(autoRun);
		controlPane.getChildren().add(publish);
		controlPane.getChildren().add(openFile);
		controlPane.getChildren().add(new Label("file:"));
		controlPane.getChildren().add(fileNameBox);
		fileNameBox.setMaxWidth(Double.MAX_VALUE);
		controlPane.getChildren().add(new Label("git:"));
		controlPane.getChildren().add(fileListBox);
		fileListBox.setMaxWidth(Double.MAX_VALUE);
		controlPane.setMaxWidth(Double.MAX_VALUE);
		

		// put the flowpane in the top area of the BorderPane
		setTop(controlPane);

		addIScriptEventListener(BowlerStudioController.getBowlerStudio());
		reset();
	}

	private void reset() {
		running = false;
		Platform.runLater(() -> {
			runfx.setText("Run");
			runfx.setGraphic(AssetFactory.loadIcon("Run.png"));
			runfx.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));

		});

	}

	public void addIScriptEventListener(IScriptEventListener l) {
		if (!listeners.contains(l))
			listeners.add(l);
	}

	public void removeIScriptEventListener(IScriptEventListener l) {
		if (listeners.contains(l))
			listeners.remove(l);
	}

	public void stop() {
		// TODO Auto-generated method stub

		reset();
		if (scriptRunner != null)
			while (scriptRunner.isAlive()) {

				Log.debug("Interrupting");
				ThreadUtil.wait(10);
				try {
					scriptRunner.interrupt();
					scriptRunner.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

	}

	public void loadCodeFromFile(File currentFile) throws IOException {
		if (!currentFile.exists()) {
			currentFile.createNewFile();
		}
		setUpFile(currentFile);
		if (!langaugeType.getIsTextFile())
			setCode("Binary File");
		else
			setCode(new String(Files.readAllBytes(currentFile.toPath())));

	}

	private void start() {
		try {
			if (!currentFile.getName().contentEquals("csgDatabase.json")) {
				String[] gitID = ScriptingEngine.findGitTagFromFile(currentFile);
				String remoteURI = gitID[0];
				ArrayList<String> f = ScriptingEngine.filesInGit(remoteURI);
				for (String s : f) {
					if (s.contentEquals("csgDatabase.json")) {
						File dbFile = ScriptingEngine.fileFromGit(gitID[0], s);
						if (!CSGDatabase.getDbFile().equals(dbFile))
							CSGDatabase.setDbFile(dbFile);
						CSGDatabase.saveDatabase();
					}
				}
			}
		} catch (Exception e) {
			// ignore CSG database
			e.printStackTrace();
		}
		BowlerStudio.clearConsole();
		BowlerStudioController.clearHighlight();
		try {
			ScriptingEngine.setAutoupdate(false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		running = true;
		Platform.runLater(() -> {
			runfx.setText("Stop");
			runfx.setGraphic(AssetFactory.loadIcon("Stop.png"));
			runfx.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
		});
		scriptRunner = new Thread() {

			public void run() {
//				String name;
//				try{
//					name = currentFile.getName();
//				}catch (NullPointerException e){
//					name="";
//				}
				try {
					Object obj = ScriptingEngine.inlineFileScriptRun(currentFile, null);
					for (IScriptEventListener l : listeners) {
						l.onScriptFinished(obj, scriptResult, currentFile);
					}

					scriptResult = obj;
					reset();

				} catch (groovy.lang.MissingPropertyException | org.python.core.PyException d) {
					BowlerStudioController.highlightException(currentFile, d);
				} catch (Throwable ex) {
					System.err.println("Script exception of type= " + ex.getClass().getName());

					try {
						if (ex.getMessage().contains("sleep interrupted")) {
							append("\n" + currentFile + " Interupted\n");
						} else {
							BowlerStudioController.highlightException(currentFile, new Exception(ex));
						}
					} catch (Exception e) {
						e.printStackTrace();
						BowlerStudioController.highlightException(currentFile, new Exception(ex));
					}

					reset();

					for (IScriptEventListener l : listeners) {
						try {
							l.onScriptError(new Exception(ex), currentFile);
						} catch (Throwable e) {
						}
					}
				}

			}
		};

		try {

			scriptRunner.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void append(String s) {
		System.out.println(s);
	}

	public String getGitRepo() {
		return fileListBox.getText();
	}

	public String getGitFile() {
		return fileNameBox.getText();
	}

	private void setUpFile(File f) {
		currentFile = f;
		String langType = ScriptingEngine.getShellType(currentFile.getName());
//		try {
//			image.setImage(AssetFactory.loadAsset("Script-Tab-"+ScriptingEngine.getShellType(currentFile.getName())+".png"));
//		} catch (Exception e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
		langaugeType = ScriptingEngine.getLangaugesMap().get(langType);
		// ScriptingEngine.setLastFile(f);
		Git git;
		try {
			git = ScriptingEngine.locateGit(currentFile);
			remote = git.getRepository().getConfig().getString("remote", "origin", "url");
			Platform.runLater(() -> {
				// fileListBox.setMinWidth(remote.getBytes().length*10);
				fileListBox.setText(remote);
				// fileListBox.res
				fileNameBox.setText(ScriptingEngine.findLocalPath(f, git));
				// These values are display only, so if hte user tries to change them, they
				// reset
				// the use of text field for static dats is so the user cna copy the vlaues and
				// use them in their scritpts
				fileNameBox.textProperty().addListener((observable, oldValue, newValue) -> {
					fileNameBox.setText(ScriptingEngine.findLocalPath(f, git));
				});
				fileListBox.textProperty().addListener((observable, oldValue, newValue) -> {
					fileListBox.setText(remote);
				});

				ScriptingEngine.closeGit(git);
			});
		} catch (Exception e1) {
			Platform.runLater(() -> {
				fileListBox.setText("none");
				fileListBox.setMinWidth(40);
				fileNameBox.setText(f.getAbsolutePath());
				// These values are display only, so if hte user tries to change them, they
				// reset
				// the use of text field for static dats is so the user cna copy the vlaues and
				// use them in their scritpts
				fileNameBox.textProperty().addListener((observable, oldValue, newValue) -> {
					fileNameBox.setText(f.getAbsolutePath());
				});
				fileListBox.textProperty().addListener((observable, oldValue, newValue) -> {
					fileListBox.setText("none");
				});

			});
			e1.printStackTrace();
		}
		if (!langaugeType.getIsTextFile())
			return;
		try {
			getWatcher().addIFileChangeListener(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void updateFile() {

		File last = FileSelectionFactory
				.GetFile(
						currentFile == null ? ScriptingEngine.getWorkspace()
								: new File(
										ScriptingEngine.getWorkspace().getAbsolutePath() + "/" + currentFile.getName()),
						true, new ExtensionFilter("Save Script", "*"));
		if (last != null) {
			setUpFile(last);
		}

	}

	public void open() {

		updateFile();
		try {
			setCode(new String(Files.readAllBytes(currentFile.toPath())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	public void save() {
		// TODO Auto-generated method stub
		try {
			System.out.println("Writing file contents");
			BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile));
			writer.write(getCode());
			writer.close();
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
	}

	@Override
	public void onFileChange(File fileThatChanged, @SuppressWarnings("rawtypes") WatchEvent event) {
		if (updateneeded)
			return;
		updateneeded = true;
		try {
			getWatcher().removeIFileChangeListener(this);
			FxTimer.runLater(Duration.ofMillis(500), new Runnable() {
				@Override
				public void run() {
					updateneeded = false;
					// TODO Auto-generated method stub
					if (fileThatChanged.getAbsolutePath().contains(currentFile.getAbsolutePath())) {

						System.out.println("Code in " + fileThatChanged.getAbsolutePath() + " changed");
						Platform.runLater(() -> {
							try {
								String content = new String(
										Files.readAllBytes(Paths.get(fileThatChanged.getAbsolutePath())));
								if (content.length() > 2)// ensures tha the file contents never get wiped out on the
															// user
									setCode(content);
								if (autoRun.isSelected()) {
									new Thread(() -> {
										stop();
										start();
									}).start();

								}
							} catch (UnsupportedEncodingException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
							try {
								getWatcher().addIFileChangeListener(ScriptingFileWidget.this);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});

					} else {
						// System.out.println("Othr Code in "+fileThatChanged.getAbsolutePath()+"
						// changed");
					}
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getCode() {
		return codeText;
	}

	public void setCode(String string) {
		String pervious = codeText;
		codeText = string;
		// System.out.println(codeText);
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onScriptChanged(pervious, string, currentFile);
		}
	}

	public String getFileName() {
		if (currentFile != null)
			return currentFile.getName();
		else
			return "Web";
	}

	public FileChangeWatcher getWatcher() throws IOException {
		return FileChangeWatcher.watch(currentFile);
	}

	public void close() {
		try {
			getWatcher().removeIFileChangeListener(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onFileDelete(File fileThatIsDeleted) {
		close();

	}

}
