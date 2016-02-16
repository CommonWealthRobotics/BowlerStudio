/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.scripting.*;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import com.neuronrobotics.bowlerstudio.twod.TwoDCad;
import com.neuronrobotics.bowlerstudio.twod.TwoDCadFactory;
import com.neuronrobotics.bowlerstudio.utils.BowlerStudioResourceFactory;
import com.neuronrobotics.imageprovider.CHDKImageProvider;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.PromptForGist;
import com.neuronrobotics.pidsim.LinearPhysicsEngine;
import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.pid.VirtualGenericPIDDevice;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.Polygon;
import haar.HaarFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.reactfx.util.FxTimer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.ResourceBundle;

//import javafx.scene.control.ScrollPane;

/**
 * FXML Controller class
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MainController implements Initializable {
	private static int sizeOfTextBuffer = 4000;
	private static ByteArrayOutputStream out = null;
	static boolean opencvOk = true;
	private static TextArea logViewRef = null;
	private static String newString = null;
	@FXML
	private MenuBar menuBar;
	@FXML
	private MenuItem logoutGithub;

	@FXML
	private Menu myGists;

	@FXML
	private Pane logView;

	@FXML
	private AnchorPane editorContainer;

	@FXML
	private Pane viewContainer;
	@FXML
	private Pane jfx3dControls;

	private SubScene subScene;
	private BowlerStudio3dEngine jfx3dmanager;

	private File openFile;

	private BowlerStudioController application;
	private Stage primaryStage;

	@FXML
	private CheckMenuItem AddDefaultRightArm;
	@FXML
	private CheckMenuItem AddVRCamera;
	private CommandLineWidget cmdLine;
	@FXML
	Menu CreatureLabMenue;
	private EventHandler<? super KeyEvent> normalKeyPessHandle;
	@FXML
	MenuItem createNewGist;
	@FXML
	MenuItem addFileToGist;
	private boolean loginWindowOpen=false;
	private GithubLoginFX controller= null;
	public static void updateLog() {
		if (logViewRef != null) {
			String current;
			String finalStr;
			if (getOut().size() == 0) {
				newString = null;
			} else {
				newString = getOut().toString();
				getOut().reset();
			}
			if (newString != null) {
				current = logViewRef.getText() + newString;
				try {
					finalStr = new String(current.substring(current.getBytes().length - sizeOfTextBuffer));
				} catch (StringIndexOutOfBoundsException ex) {
					finalStr = current;
				}
				int strlen = finalStr.length() - 1;
				String outStr=finalStr;
				Platform.runLater(() -> {
					logViewRef.setText(outStr);
					logViewRef.positionCaret(strlen);
				
				});
			}

		}
		FxTimer.runLater(Duration.ofMillis(500), () -> {

			updateLog();
		});
	}

	// private final CodeArea codeArea = new CodeArea();

	/**
	 * Initializes the controller class.
	 *
	 * @param url
	 * @param rb
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		ScriptingEngine.setLoginManager(new IGitHubLoginManager() {

			@Override
			public String[] prompt(String username) {
				if(!loginWindowOpen&&controller!=null)
					controller.reset();
				controller=null;
				loginWindowOpen=true;
				System.err.println("Calling login from BowlerStudio");
				// new RuntimeException().printStackTrace();
				FXMLLoader fxmlLoader = BowlerStudioResourceFactory.getGithubLogin();
				Parent root = fxmlLoader.getRoot();
				if(controller==null){
					controller = fxmlLoader.getController();
					Platform.runLater(() -> {
						controller.reset();
						controller.getUsername().setText(username);
						Stage stage = new Stage();
						stage.setTitle("GitHub Login");
						stage.initModality(Modality.APPLICATION_MODAL);
						controller.setStage(stage, root);
						stage.centerOnScreen();
						stage.show();
					});
				}
				// setContent(root);
				while (!controller.isDone()) {
					ThreadUtil.wait(100);
				}
				String[] creds = controller.getCreds();
				loginWindowOpen=false;
				return creds;
			}
		});

		jfx3dmanager = new BowlerStudio3dEngine();

		setApplication(new BowlerStudioController(jfx3dmanager, this));
		Platform.runLater(() -> {
			editorContainer.getChildren().add(getApplication());
			AnchorPane.setTopAnchor(getApplication(), 0.0);
			AnchorPane.setRightAnchor(getApplication(), 0.0);
			AnchorPane.setLeftAnchor(getApplication(), 0.0);
			AnchorPane.setBottomAnchor(getApplication(), 0.0);

			subScene = jfx3dmanager.getSubScene();
			subScene.setFocusTraversable(false);
			subScene.setOnMouseEntered(mouseEvent -> {
				// System.err.println("3d window requesting focus");
				Scene topScene = BowlerStudio.getScene();
				normalKeyPessHandle = topScene.getOnKeyPressed();
				jfx3dmanager.handleKeyboard(topScene);
			});

			subScene.setOnMouseExited(mouseEvent -> {
				// System.err.println("3d window dropping focus");
				Scene topScene = BowlerStudio.getScene();
				topScene.setOnKeyPressed(normalKeyPessHandle);
			});

			subScene.widthProperty().bind(viewContainer.widthProperty());
			subScene.heightProperty().bind(viewContainer.heightProperty());
		});

		Platform.runLater(() -> {
			jfx3dControls.getChildren().add(jfx3dmanager.getControlsBox());
			viewContainer.getChildren().add(subScene);
		});

		System.out.println("Welcome to BowlerStudio!");
		new Thread() {
			public void run() {
				setName("Load Haar Thread");
				try {
					HaarFactory.getStream(null);
				} catch (Exception ex) {
				}
			}
		}.start();

		// getAddDefaultRightArm().setOnAction(event -> {
		//
		// application.onAddDefaultRightArm(event);
		// });
		// getAddVRCamera().setOnAction(event -> {
		// if(AddVRCamera.isSelected())
		// application.onAddVRCamera(event);
		// });

		FxTimer.runLater(Duration.ofMillis(100), () -> {
			if (ScriptingEngine.getLoginID() != null) {
				setToLoggedIn(ScriptingEngine.getLoginID());
			} else {
				setToLoggedOut();
			}

		});

		ScriptingEngine.addIGithubLoginListener(new IGithubLoginListener() {

			@Override
			public void onLogout(String oldUsername) {
				setToLoggedOut();
			}

			@Override
			public void onLogin(String newUsername) {
				setToLoggedIn(newUsername);

			}
		});

		cmdLine = new CommandLineWidget();

		Platform.runLater(() -> {
			// logView.resize(250, 300);
			// after connection manager set up, add scripting widget
			logViewRef = new TextArea();
			logViewRef.prefWidthProperty().bind(logView.widthProperty().divide(2));
			logViewRef.prefHeightProperty().bind(logView.heightProperty().subtract(40));
			VBox box = new VBox();
			box.getChildren().add(logViewRef);
			box.getChildren().add(cmdLine);
			VBox.setVgrow(logViewRef, Priority.ALWAYS);
			box.prefWidthProperty().bind(logView.widthProperty().subtract(10));

			logView.getChildren().addAll(box);
		});
	}

	private void setToLoggedIn(final String name) {
		// new Exception().printStackTrace();
		FxTimer.runLater(Duration.ofMillis(100), () -> {
			logoutGithub.disableProperty().set(false);
			logoutGithub.setText("Log out " + name);
			new Thread() {
				public void run() {

					GitHub github = ScriptingEngine.getGithub();
					while (github == null) {
						github = ScriptingEngine.getGithub();
						ThreadUtil.wait(20);
					}
					try {
						GHMyself myself = github.getMyself();
						PagedIterable<GHGist> gists = myself.listGists();
						Platform.runLater(() -> {
							myGists.getItems().clear();
						});
						ThreadUtil.wait(20);
						for (GHGist gist : gists) {
							String desc = gist.getDescription();
							if (desc == null || desc.length() == 0) {
								desc = gist.getFiles().keySet().toArray()[0].toString();
							}
							Menu tmpGist = new Menu(desc);
							String description = desc;
							MenuItem loadWebGist = new MenuItem("Show Web Gist...");
							loadWebGist.setOnAction(event -> {
								String webURL = gist.getHtmlUrl();
								try {
									BowlerStudio.openUrlInNewTab(new URL(webURL));
								} catch (MalformedURLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});
							MenuItem addFile = new MenuItem("Add file to Gist...");
							addFile.setOnAction(event -> {
								new Thread() {
									public void run() {
										
									}
								}.start();
							});
							Platform.runLater(() -> {
								tmpGist.getItems().addAll(addFile, loadWebGist);
							});
							EventHandler<Event> loadFiles = new EventHandler<Event>() {
								boolean gistFlag = false;

								@Override
								public void handle(Event ev) {

									// for(ScriptingEngine.)
									new Thread() {
										public void run() {
											
											ThreadUtil.wait(500);
											if(!tmpGist.isShowing())
												return;
											if(gistFlag)
												return;//another thread is servicing this gist
											gistFlag = true;
											System.out.println("Loading files for "+description );
											ArrayList<String> listofFiles;
											try {
												listofFiles = ScriptingEngine
														.filesInGit(gist.getGitPushUrl(), "master", null);
												
											} catch (Exception e1) {
												e1.printStackTrace();
												return;
											}
											if(tmpGist.getItems().size()>2)
												return;// menue populated by another thread
											for (String s : listofFiles) {
												MenuItem tmp = new MenuItem(s);
												tmp.setOnAction(event -> {
													new Thread() {
														public void run() {
															try {
																File fileSelected = ScriptingEngine
																		.fileFromGit(gist.getGitPushUrl(), s);
																BowlerStudio.createFileTab(fileSelected);
															} catch (Exception e) {
																// TODO
																// Auto-generated
																// catch block
																e.printStackTrace();
															}
														}
													}.start();

												});
												Platform.runLater(() -> {
													tmpGist.getItems().add(tmp);
													// removing this listener after menue is activated for the first time
													tmpGist.setOnShowing(null);

												});
											}
											Platform.runLater(() -> {
												tmpGist.hide();
												Platform.runLater(() -> {
													tmpGist.show();
												});
											});
										}
									}.start();
								}
							};

							tmpGist.setOnShowing(loadFiles);
							Platform.runLater(() -> {
								myGists.getItems().add(tmpGist);
							});

						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();

		});
	}

	private void setToLoggedOut() {
		Platform.runLater(() -> {
			myGists.getItems().clear();
			logoutGithub.disableProperty().set(true);
			logoutGithub.setText("Anonymous");
		});
	}

	/**
	 * Returns the location of the Jar archive or .class file the specified
	 * class has been loaded from. <b>Note:</b> this only works if the class is
	 * loaded from a jar archive or a .class file on the locale file system.
	 *
	 * @param cls
	 *            class to locate
	 * @return the location of the Jar archive the specified class comes from
	 */
	public static File getClassLocation(Class<?> cls) {

		// VParamUtil.throwIfNull(cls);
		String className = cls.getName();
		ClassLoader cl = cls.getClassLoader();
		URL url = cl.getResource(className.replace(".", "/") + ".class");

		String urlString = url.toString().replace("jar:", "");

		if (!urlString.startsWith("file:")) {
			throw new IllegalArgumentException("The specified class\"" + cls.getName()
					+ "\" has not been loaded from a location" + "on the local filesystem.");
		}

		urlString = urlString.replace("file:", "");
		urlString = urlString.replace("%20", " ");

		int location = urlString.indexOf(".jar!");

		if (location > 0) {
			urlString = urlString.substring(0, location) + ".jar";
		} else {
			// System.err.println("No Jar File found: " + cls.getName());
		}

		return new File(urlString);
	}

	@FXML
	private void onLoadFile(ActionEvent e) {
		new Thread() {
			public void run() {
				setName("Load File Thread");
				openFile = FileSelectionFactory.GetFile(ScriptingEngine.getLastFile(),
						new ExtensionFilter("Groovy Scripts", "*.groovy", "*.java", "*.txt"),
						new ExtensionFilter("Clojure", "*.cloj", "*.clj", "*.txt", "*.clojure"),
						new ExtensionFilter("Python", "*.py", "*.python", "*.txt"),
						new ExtensionFilter("DXF", "*.dxf", "*.DXF"),
						new ExtensionFilter("GCODE", "*.gcode", "*.nc", "*.ncg"),
						new ExtensionFilter("JPG", "*.jpg", "*.jpeg", "*.JPG"),
						new ExtensionFilter("PNG", "*.png", "*.PNG"),new ExtensionFilter("All", "*.*"));

				if (openFile == null) {
					return;
				}
				ArrayList<Polygon> points = TwoDCadFactory.pointsFromFile(openFile);
				if(null!=points){
					getApplication().addTab(new TwoDCad(points), true);
					return;
				}
				getApplication().createFileTab(openFile);
			}
		}.start();
	}

	@FXML
	private void onConnect(ActionEvent e) {
		new Thread() {
			public void run() {
				setName("Load BowlerDevice Dialog Thread");
				ConnectionManager.addConnection();
			}
		}.start();
	}

	@FXML
	private void onConnectVirtual(ActionEvent e) {

		ConnectionManager.addConnection(new VirtualGenericPIDDevice(10000), "virtual");
	}

	@FXML
	private void onClose(ActionEvent e) {
		System.exit(0);
	}

	public TextArea getLogView() {
		return logViewRef;
	}

	public void disconnect() {

		getApplication().disconnect();
	}

	public void openUrlInNewTab(URL url) {
		getApplication().openUrlInNewTab(url);
	}

	@FXML
	public void onConnectCHDKCamera(ActionEvent event) {
		Platform.runLater(() -> {
			try {
				ConnectionManager.addConnection(new CHDKImageProvider(), "cameraCHDK");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	@FXML
	public void onConnectCVCamera(ActionEvent event) {

		Platform.runLater(() -> ConnectionManager.onConnectCVCamera());

	}

	@FXML
	public void onConnectJavaCVCamera() {

		Platform.runLater(() -> ConnectionManager.onConnectJavaCVCamera());

	}

	@FXML
	public void onConnectFileSourceCamera() {
		Platform.runLater(() -> ConnectionManager.onConnectFileSourceCamera());

	}

	@FXML
	public void onConnectURLSourceCamera() {

		Platform.runLater(() -> ConnectionManager.onConnectURLSourceCamera());

	}

	@FXML
	public void onConnectHokuyoURG(ActionEvent event) {
		Platform.runLater(() -> ConnectionManager.onConnectHokuyoURG());

	}

	@FXML
	public void onConnectGamePad(ActionEvent event) {
		Platform.runLater(() -> ConnectionManager.onConnectGamePad("gamepad"));

	}

	// public CheckMenuItem getAddVRCamera() {
	// return AddVRCamera;
	// }
	//
	//
	// public void setAddVRCamera(CheckMenuItem addVRCamera) {
	// AddVRCamera = addVRCamera;
	// }
	//
	//
	// public CheckMenuItem getAddDefaultRightArm() {
	// return AddDefaultRightArm;
	// }
	//
	//
	// public void setAddDefaultRightArm(CheckMenuItem addDefaultRightArm) {
	// AddDefaultRightArm = addDefaultRightArm;
	// }

	@FXML
	public void onLogin() {
		new Thread() {
			public void run() {
				setName("Login Gist Thread");
				try {
					ScriptingEngine.login();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	@FXML
	public void onLogout() {
		try {
			ScriptingEngine.logout();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	public void onConnectPidSim() {
		LinearPhysicsEngine eng = new LinearPhysicsEngine();
		eng.connect();
		ConnectionManager.addConnection(eng, "engine");
	}

	@FXML
	public void onPrint(ActionEvent event) {
		NRPrinter printer = (NRPrinter) ConnectionManager.pickConnectedDevice(NRPrinter.class);
		if (printer != null) {
			// run a print here
		}

	}

	@FXML
	public void onMobileBaseFromFile() {
		new Thread() {
			public void run() {
				setName("Load Mobile Base Thread");
				openFile = FileSelectionFactory.GetFile(ScriptingEngine.getLastFile(),
						new ExtensionFilter("MobileBase XML", "*.xml", "*.XML"));

				if (openFile == null) {
					return;
				}
				Platform.runLater(() -> {
					try {
						MobileBase mb = new MobileBase(new FileInputStream(openFile));
						ConnectionManager.addConnection(mb, mb.getScriptingName());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			}
		}.start();

	}

	@FXML
	public void onRobotArm(ActionEvent event) {
		loadMobilebaseFromGist("2b0cff20ccee085c9c36", "TrobotLinks.xml");
	}

	@FXML
	public void onHexapod() {
		loadMobilebaseFromGist("bcb4760a449190206170", "CarlTheRobot.xml");
	}

	@FXML
	public void onGrasshopper() {
		loadMobilebaseFromGist("a6cbefc11693162cf9d4", "GrassHopper.xml");
	}

	@FXML
	public void onInputArm() {
		loadMobilebaseFromGist("98892e87253005adbe4a", "TrobotMaster.xml");
	}

	@FXML
	public void onHumanoid() {
		loadMobilebaseFromGist("a991ca954460c1ba9860", "humanoid.xml");
	}

	@FXML
	public void onAddElephant() {
		loadMobilebaseFromGist("aef13d65093951d13235", "Elephant.xml");
	}

	public Menu getCreatureLabMenue() {
		return CreatureLabMenue;
	}

	public void setCreatureLabMenue(Menu creatureLabMenue) {
		CreatureLabMenue = creatureLabMenue;
	}

	public void loadMobilebaseFromGist(String id, String file) {
		new Thread() {
			public void run() {
				try {
					BowlerStudio.openUrlInNewTab(new URL("https://gist.github.com/" + id));
					String xmlContent = ScriptingEngine.codeFromGistID(id, file)[0];
					MobileBase mb = new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"));

					mb.setSelfSource(new String[] { id, file });
					ConnectionManager.addConnection(mb, mb.getScriptingName());

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

	@FXML
	public void onMobileBaseFromGist() {

		PromptForGist.prompt("Select a Creature From a Gist", "bcb4760a449190206170", (gitsId, file) -> {
			loadMobilebaseFromGist(gitsId, file);
		});
	}

	public ScriptingFileWidget createFileTab(File file) {
		return getApplication().createFileTab(file);
	}

	public BowlerStudioController getApplication() {
		return application;
	}

	public void setApplication(BowlerStudioController application) {
		this.application = application;
	}

	@FXML
	public void onAddCNC() {
		loadMobilebaseFromGist("51a9e0bc4ee095b03979", "CNC.xml");
	}

	public static ByteArrayOutputStream getOut() {
		if (out == null)
			out = new ByteArrayOutputStream();
		return out;
	}

	@FXML
	public void onCreatenewGist() {

	}

	@FXML
	public void onAddFileToGist() {

	}

}
