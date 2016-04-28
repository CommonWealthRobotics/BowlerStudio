/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.*;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;
import com.neuronrobotics.bowlerstudio.twod.TwoDCad;
import com.neuronrobotics.bowlerstudio.twod.TwoDCadFactory;
import com.neuronrobotics.bowlerstudio.utils.BowlerStudioResourceFactory;
import com.neuronrobotics.imageprovider.CHDKImageProvider;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.PromptForGit;
import com.neuronrobotics.pidsim.LinearPhysicsEngine;
import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.pid.VirtualGenericPIDDevice;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.Polygon;
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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

//import javafx.scene.control.ScrollPane;

/**
 * FXML Controller class
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MainController implements Initializable {
	/**
	 * class vatiables
	 */
	private static int sizeOfTextBuffer = 4000;
	private static ByteArrayOutputStream out = null;
	static boolean opencvOk = true;
	private static String newString = null;
	private static TextArea logViewRefStatic = new TextArea();
	private SubScene subScene;
	private BowlerStudio3dEngine jfx3dmanager;
	private File openFile;
	private BowlerStudioController application;
	private Stage primaryStage;
	private boolean loginWindowOpen = false;
	private GithubLoginFX controller = null;
	public static void clearConsole() {
		Platform.runLater(() -> {
			logViewRefStatic.setText("");
		});
	}
	private static boolean logLock = false;
	private Image icon;
	private static Stage stage=null;
	private CommandLineWidget cmdLine;
	protected EventHandler<? super KeyEvent> normalKeyPessHandle;
	
	/**
	 * FXML Widgets
	 */
	@FXML MenuBar BowlerStudioMenue;
	@FXML Menu CreaturesMenu;
	@FXML Menu GitHubRoot;
	@FXML MenuItem logoutGithub;
	@FXML MenuItem createNewGist;
	@FXML Menu myGists;
	@FXML Menu myOrganizations;
	@FXML Menu myRepos;
	@FXML Menu watchingRepos;
	@FXML MenuItem clearCache;
	@FXML AnchorPane editorContainer;
	@FXML TextArea logViewRef;
	@FXML AnchorPane logView;
	@FXML TitledPane commandLineTitledPane;
	@FXML AnchorPane CommandLine;
	@FXML AnchorPane jfx3dControls;
	@FXML AnchorPane viewContainer;




	public static void updateLog() {
		if (logViewRefStatic != null) {

			if (getOut().size() == 0) {
				newString = null;

			} else {
				if (!logLock) {
					logLock = true;
					new Thread(() -> {
						String current;
						String finalStr;
						newString = getOut().toString();
						getOut().reset();
						if (newString != null) {
							current = logViewRefStatic.getText() + newString;
							try {
								finalStr = new String(current.substring(current.getBytes().length - sizeOfTextBuffer));
							} catch (StringIndexOutOfBoundsException ex) {
								finalStr = current;
							}
							int strlen = finalStr.length() - 1;
							String outStr = finalStr;
							Platform.runLater(() -> {
								logViewRefStatic.setText(outStr);
								logViewRefStatic.positionCaret(strlen);
								logLock = false;
							});
						}
					}).start();

				}
			}

		}
		FxTimer.runLater(java.time.Duration.ofMillis(500), () -> {

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
		logViewRefStatic = logViewRef;
		System.out.println("Main controller inializing");
		// THis initialization needs to be launched from a thread to avoid
		// blocking the UI thread that spawwns it
		MainController mainControllerRef = this;
		new Thread(new Runnable() {
			

			@Override
			public void run() {
				ScriptingEngine.setLoginManager(new IGitHubLoginManager() {

					@Override
					public String[] prompt(String username) {
						boolean loginWas = loginWindowOpen;
						
						if(stage==null){						
							if (!loginWas && controller != null)
								controller.reset();
							controller = null;
							System.err.println("Calling login from BowlerStudio");
							// new RuntimeException().printStackTrace();
							FXMLLoader fxmlLoader = BowlerStudioResourceFactory.getGithubLogin();
							Parent root = fxmlLoader.getRoot();
							if (controller == null) {
								controller = fxmlLoader.getController();
								Platform.runLater(() -> {
									if(!loginWindowOpen){
										controller.reset();
										controller.getUsername().setText(username);
										stage = new Stage();
										stage.setTitle("GitHub Login");
										stage.initModality(Modality.APPLICATION_MODAL);
										controller.setStage(stage, root);
										stage.centerOnScreen();
									
										loginWindowOpen = true;
										stage.show();
										stage =null;
									}
								});
							}
						}
						// setContent(root);
						while (!controller.isDone()) {
							ThreadUtil.wait(100);
						}
						String[] creds = controller.getCreds();
						loginWindowOpen = false;
						return creds;
					}
				});
				try {
					ScriptingEngine.runLogin();
					icon = AssetFactory.loadAsset("BowlerStudio.png");

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				//ScriptingEngine.getGithub().getMyself().getGravatarId()
				// System.out.println("Loading 3d engine");
				jfx3dmanager = new BowlerStudio3dEngine();

				setApplication(new BowlerStudioController(jfx3dmanager, mainControllerRef));
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
				//
				// new Thread() {
				// public void run() {
				// setName("Load Haar Thread");
				// try {
				// HaarFactory.getStream(null);
				// } catch (Exception ex) {
				// }
				// }
				// }.start();

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
				// System.out.println("Laoding ommand line widget");
				cmdLine = new CommandLineWidget();

				Platform.runLater(() -> {
					//CadDebugger.getChildren().add(jfx3dmanager.getDebuggerBox());
					AnchorPane.setTopAnchor(jfx3dmanager.getDebuggerBox(), 0.0);
					AnchorPane.setRightAnchor(jfx3dmanager.getDebuggerBox(), 0.0);
					AnchorPane.setLeftAnchor(jfx3dmanager.getDebuggerBox(), 0.0);
					AnchorPane.setBottomAnchor(jfx3dmanager.getDebuggerBox(), 0.0);
					CommandLine.getChildren().add(cmdLine);
					AnchorPane.setTopAnchor(cmdLine, 0.0);
					AnchorPane.setRightAnchor(cmdLine, 0.0);
					AnchorPane.setLeftAnchor(cmdLine, 0.0);
					AnchorPane.setBottomAnchor(cmdLine, 0.0);
				});
				try {
					ScriptingEngine.setAutoupdate(true);
					File f = ScriptingEngine
							.fileFromGit(
									"https://github.com/madhephaestus/BowlerStudioExampleRobots.git",// git repo, change this if you fork this demo
								"exampleRobots.json"// File from within the Git repo
							);
					
					@SuppressWarnings("unchecked")
					HashMap<String,HashMap<String,Object>> map = (HashMap<String, HashMap<String, Object>>) ScriptingEngine.inlineFileScriptRun(f, null);
					for(String menuTitle:map.keySet()){
						HashMap<String,Object> script = map.get(menuTitle);
						MenuItem item = new MenuItem(menuTitle);
						item.setOnAction(event -> {
							loadMobilebaseFromGit(	(String)script.get("scriptGit"),
													(String)script.get("scriptFile"));
						});
						Platform.runLater(()->{
							CreaturesMenu.getItems().add(item);
						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();
		Platform.runLater(()->{
			commandLineTitledPane.setGraphic(AssetFactory.loadIcon("Command-Line.png"));
		});
	}

	private void setToLoggedIn(final String name) {
		// new Exception().printStackTrace();
		FxTimer.runLater(Duration.ofMillis(100), () -> {
			logoutGithub.disableProperty().set(false);
			logoutGithub.setText("Log out " + name);
			new Thread() {
				public void run() {
					try {
						ScriptingEngine.setAutoupdate(false);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
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
										// TODO add the implementation of add
										// file, make sure its modular to be
										// reused elsewhere
									}
								}.start();
							});
							Platform.runLater(() -> {
								// tmpGist.getItems().addAll(addFile,
								// loadWebGist);
								tmpGist.getItems().add(loadWebGist);
							});
							EventHandler<Event> loadFiles = new EventHandler<Event>() {
								boolean gistFlag = false;

								@Override
								public void handle(Event ev) {
									if (gistFlag)
										return;// another thread is servicing this gist
									// for(ScriptingEngine.)
									new Thread() {
										public void run() {

											ThreadUtil.wait(500);
											if (!tmpGist.isShowing())
												return;
											if (gistFlag)
												return;// another thread is
														// servicing this gist
											gistFlag = true;
											System.out.println("Loading files for " + description);
											ArrayList<String> listofFiles;
											try {
												listofFiles = ScriptingEngine.filesInGit(gist.getGitPushUrl(), "master",
														null);

											} catch (Exception e1) {
												e1.printStackTrace();
												return;
											}
											if (tmpGist.getItems().size() !=1)
												return;// menue populated by
														// another thread
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
													// removing this listener
													// after menue is activated
													// for the first time
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
						// Now load the users GIT repositories
						// github.getMyOrganizations();
						Map<String, GHOrganization> orgs = github.getMyOrganizations();
						for (String org : orgs.keySet()) {
							// System.out.println("Org: "+org);
							Menu OrgItem = new Menu(org);
							GHOrganization ghorg = orgs.get(org);
							Map<String, GHRepository> repos = ghorg.getRepositories();
							for (String orgRepo : repos.keySet()) {
								setUpRepoMenue(OrgItem, repos.get(orgRepo));
							}
							Platform.runLater(() -> {
								myOrganizations.getItems().add(OrgItem);
							});
						}
						GHMyself self = github.getMyself();
						// Repos I own
						Map<String, GHRepository> myPublic = self.getAllRepositories();
						HashMap<String, Menu> myownerMenue = new HashMap<>();
						for (String myRepo : myPublic.keySet()) {
							GHRepository g = myPublic.get(myRepo);
							if (myownerMenue.get(g.getOwnerName()) == null) {
								myownerMenue.put(g.getOwnerName(), new Menu(g.getOwnerName()));
								Platform.runLater(() -> {
									myRepos.getItems().add(myownerMenue.get(g.getOwnerName()));
								});
							}
							setUpRepoMenue(myownerMenue.get(g.getOwnerName()), g);
						}
						// Watched repos
						PagedIterable<GHRepository> watching = self.listSubscriptions();
						HashMap<String, Menu> ownerMenue = new HashMap<>();
						for (GHRepository g : watching) {
							if (ownerMenue.get(g.getOwnerName()) == null) {
								ownerMenue.put(g.getOwnerName(), new Menu(g.getOwnerName()));
								Platform.runLater(() -> {
									watchingRepos.getItems().add(ownerMenue.get(g.getOwnerName()));
								});
							}
							setUpRepoMenue(ownerMenue.get(g.getOwnerName()), g);
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}.start();

		});
	}

	private void setUpRepoMenue(Menu repoMenue, GHRepository repo) {
		new Thread() {
			public void run() {

				Menu orgRepo = new Menu(repo.getFullName());
				Menu orgFiles = new Menu("Files");
				MenuItem loading = new MenuItem("Loading...");
				MenuItem addFile = new MenuItem("Add file to Git Repo...");
				addFile.setOnAction(event -> {
					new Thread() {
						public void run() {
							// TODO add the implementation of add file, make
							// sure its modular to be reused elsewhere

						}
					}.start();
				});

				Platform.runLater(() -> {
					orgFiles.getItems().add(loading);
					orgRepo.getItems().addAll(addFile, orgFiles);
				});

				String url = repo.getGitTransportUrl().replace("git://", "https://");
				EventHandler<Event> loadFiles = new EventHandler<Event>() {
					boolean gistFlag = false;

					@Override
					public void handle(Event ev) {

						// for(ScriptingEngine.)
						new Thread() {
							public void run() {

								ThreadUtil.wait(500);
								if (!orgFiles.isShowing())
									return;
								if (gistFlag)
									return;// another thread is
											// servicing this gist
								gistFlag = true;
								System.out.println(
										"Loading files for " + repo.getFullName() + " " + repo.getDescription());
								ArrayList<String> listofFiles;
								try {
									listofFiles = ScriptingEngine.filesInGit(url, "master", null);
									System.out.println("Clone Done for "+url+listofFiles.size()+" files");
								} catch (Exception e1) {
									e1.printStackTrace();
									return;
								}
								if (orgFiles.getItems().size() != 1){
									Log.warning("Bailing out of loading thread");
									return;// menue populated by
											// another thread
								}
								
								for (String s : listofFiles) {
									//System.out.println("Adding file: "+s);
									MenuItem tmp = new MenuItem(s);
									tmp.setOnAction(event -> {
										new Thread() {
											public void run() {
												try {
													File fileSelected = ScriptingEngine.fileFromGit(url, s);
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
										orgFiles.getItems().add(tmp);
										// removing this listener
										// after menue is activated
										// for the first time
										orgFiles.setOnShowing(null);

									});
									
								}
								System.out.println("Refreshing menu");
								Platform.runLater(() -> {
									orgFiles.hide();
									orgFiles.getItems().remove(loading);
									Platform.runLater(() -> {
										orgFiles.show();
									});
								});
							}
						}.start();
					}
				};
				orgFiles.setOnShowing(loadFiles);
				Platform.runLater(() -> {
					repoMenue.getItems().add(orgRepo);
				});
			}
		}.start();
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
						new ExtensionFilter("GCODE", "*.gcode", "*.nc", "*.ncg", "*.txt"),
						new ExtensionFilter("Image", "*.jpg", "*.jpeg", "*.JPG", "*.png", "*.PNG"),
						new ExtensionFilter("STL", "*.stl","*.STL","*.Stl"),
						new ExtensionFilter("All", "*.*"));
				if (openFile == null) {
					return;
				}
				ArrayList<Polygon> points = TwoDCadFactory.pointsFromFile(openFile);
				if (null != points) {
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
		try{
			getApplication().disconnect();
		}catch (NullPointerException ex){
			
		}
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

//	@FXML
//	public void onConnectJavaCVCamera() {
//
//		Platform.runLater(() -> ConnectionManager.onConnectJavaCVCamera());
//
//	}

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

	
//	public Menu getCreatureLabMenue() {
//		return CreatureLabMenue;
//	}
//
//	public void setCreatureLabMenue(Menu creatureLabMenue) {
//		CreatureLabMenue = creatureLabMenue;
//	}

	public void loadMobilebaseFromGist(String id, String file) {
		loadMobilebaseFromGit("https://gist.github.com/" + id + ".git", file);
	}
	public void loadMobilebaseFromGit(String id, String file) {
		new Thread() {
			public void run() {
				try {
					// BowlerStudio.openUrlInNewTab(new
					// URL("https://gist.github.com/" + id));
					String xmlContent = ScriptingEngine.codeFromGit(id, file)[0];
					MobileBase mb = new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"));

					mb.setGitSelfSource(new String[] { id, file });
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

		PromptForGit.prompt("Select a Creature From a Gist", "bcb4760a449190206170", (gitsId, file) -> {
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

	

	public static ByteArrayOutputStream getOut() {
		if (out == null)
			out = new ByteArrayOutputStream();
		return out;
	}

	@FXML
	public void onCreatenewGist() {
		Stage s = new Stage();
		new Thread(){
			public void run(){
				NewGistController controller = new NewGistController();
				try {
					controller.start( s);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	@FXML
	public void onAddFileToGist() {
		// AddFileToGistController controller = new AddFileToGistController();
		// try
		// {
		// controller.start(new Stage());
		// }
		// catch (Exception e)
		// {
		// e.printStackTrace();
		// }
	}

	@FXML
	public void onOpenGitter() {
		String url = "https://gitter.im";
		try {
			BowlerStudio.openUrlInNewTab(new URL(url));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML public void clearScriptCache() {
		Platform.runLater(()->{
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Are you sure you have published all your work?");
			alert.setHeaderText("This will wipe out the local cache");
			alert.setContentText("All files that are not published will be deleted");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				new Thread(){
					public void run(){
						File cache = new File(ScriptingEngine.getWorkspace().getAbsolutePath()+"/gistcache/");
						deleteFolder(cache);
					}
				}.start();
			} else {
			   System.out.println("Nothing was deleted");
			}
		});


	}
	private static void deleteFolder(File folder) {

		System.out.println("Deleting "+folder.getAbsolutePath());
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

	@FXML public void onMobileBaseFromGit() {
		PromptForGit.prompt("Select a Creature From a Git", "https://gist.github.com/bcb4760a449190206170.git", (gitsId, file) -> {
			loadMobilebaseFromGit(gitsId, file);
		});
	}

}
