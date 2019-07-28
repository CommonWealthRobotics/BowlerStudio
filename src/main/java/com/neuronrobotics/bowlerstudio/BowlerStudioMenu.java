package com.neuronrobotics.bowlerstudio;
/**
 * Sample Skeleton for "BowlerStudioMenuBar.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader;
import com.neuronrobotics.bowlerstudio.scripting.IGithubLoginListener;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
//import com.neuronrobotics.imageprovider.CHDKImageProvider;
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
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.*;
import org.reactfx.util.FxTimer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
@SuppressWarnings("restriction")
public class BowlerStudioMenu implements MenuRefreshEvent {


	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="CreaturesMenu"
	private Menu CreaturesMenu; // Value injected by FXMLLoader

	@FXML // fx:id="GitHubRoot"
	private Menu GitHubRoot; // Value injected by FXMLLoader
	
	@FXML // fx:id="workspacemenu"
	private Menu workspacemenuHandle; // Value injected by FXMLLoader
	
	@FXML // fx:id="MeneBarBowlerStudio"
	private MenuBar MeneBarBowlerStudio; // Value injected by FXMLLoader

	@FXML // fx:id="addMarlinGCODEDevice"
	private MenuItem addMarlinGCODEDevice; // Value injected by FXMLLoader
	@FXML // fx:id="addMarlinGCODEDevice"
	private MenuItem loadFirmata; // Value injected by FXMLLoader
	@FXML // fx:id="clearCache"
	private MenuItem clearCache; // Value injected by FXMLLoader

	@FXML // fx:id="createNewGist"
	private MenuItem createNewGist; // Value injected by FXMLLoader

	@FXML // fx:id="logoutGithub"
	private MenuItem logoutGithub; // Value injected by FXMLLoader

	@FXML // fx:id="myGists"
	private Menu myGists; // Value injected by FXMLLoader

	@FXML // fx:id="myOrganizations"
	private Menu myOrganizations; // Value injected by FXMLLoader

	@FXML // fx:id="myRepos"
	private Menu myRepos; // Value injected by FXMLLoader

	@FXML // fx:id="showDevicesPanel"
	private MenuItem showDevicesPanel; // Value injected by FXMLLoader
	@FXML // fx:id="showCreatureLab"
	private MenuItem showCreatureLab; // Value injected by FXMLLoader
	@FXML // fx:id="showTerminal"
	private MenuItem showTerminal;
	@FXML // fx:id="watchingRepos"
	private Menu WindowMenu;
	@FXML // fx:id="watchingRepos"
	private Menu watchingRepos; // Value injected by FXMLLoader

	private BowlerStudioModularFrame bowlerStudioModularFrame;

	private String name;
	private static BowlerStudioMenu selfRef =null;
	private File openFile;
	private Map<String, GHRepository> myPublic;
	//PagedIterable<GHGist> gists ;
	private HashMap<String, String> messages = new HashMap<String, String>();
	
	public BowlerStudioMenu(BowlerStudioModularFrame tl) {
		bowlerStudioModularFrame = tl;
	}

	@FXML
	public void onMobileBaseFromGist(ActionEvent event) {
		PromptForGit.prompt("Select a Creature From a Gist", "bcb4760a449190206170", (gitsId, file) -> {
			loadMobilebaseFromGist(gitsId, file);
		});
	}

	public ScriptingFileWidget createFileTab(File file) {
		return bowlerStudioModularFrame.createFileTab(file);
	}

	public void loadMobilebaseFromGist(String id, String file) {
		loadMobilebaseFromGit("https://gist.github.com/" + id + ".git", file);
	}

	public MenuBar getMeneBarBowlerStudio() {
		return MeneBarBowlerStudio;
	}

	public void setMeneBarBowlerStudio(MenuBar meneBarBowlerStudio) {
		MeneBarBowlerStudio = meneBarBowlerStudio;
	}

	public void loadMobilebaseFromGit(String id, String file) {
		new Thread() {
			public void run() {
				try {
					MobileBase mb ;
					if(file.toLowerCase().endsWith(".xml")) {
						mb = MobileBaseLoader.fromGit(id, file);
					}else {
						mb=(MobileBase) ScriptingEngine.gitScriptRun(id, file, null);
					}
					ConnectionManager.addConnection(mb, mb.getScriptingName());
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					BowlerStudio.printStackTrace(e);
				}
			}
		}.start();

	}

	public void openUrlInNewTab(URL url) {
		bowlerStudioModularFrame.openUrlInNewTab(url);
	}

	public void setToLoggedOut() {
		Platform.runLater(() -> {
			myGists.getItems().clear();
			logoutGithub.disableProperty().set(true);
			logoutGithub.setText("Anonymous");
		});
	}

	public void setToLoggedIn() {
		setToLoggedIn(name);
	}

	private void setToLoggedIn(final String name) {
		this.name = name;
		// new Exception().printStackTrace();
		FxTimer.runLater(Duration.ofMillis(100), () -> {
			logoutGithub.disableProperty().set(false);
			logoutGithub.setText("Log out " + name);
			new Thread() {
				public void run() {
					ConfigurationDatabase.loginEvent(name);
					BowlerStudioMenuWorkspace.loginEvent();
					try {
						ScriptingEngine.setAutoupdate(false);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					if(!PasswordManager.hasNetwork())
						return;
					GitHub github = PasswordManager.getGithub();
					while (github == null) {
						github = PasswordManager.getGithub();
						ThreadUtil.wait(20);
					}
					try {
						GHMyself myself = github.getMyself();
						PagedIterable<GHGist>	gists = myself.listGists();
						Platform.runLater(() -> {
							myGists.getItems().clear();
						});
						ThreadUtil.wait(20);
						for (GHGist gist : gists) {
							
							String url=gist.getGitPushUrl();
							String desc = gist.getDescription();
							if (desc == null || desc.length() == 0 || desc.contentEquals("Adding new file from BowlerStudio")) {
								desc = gist.getFiles().keySet().toArray()[0].toString();
							}
							String descriptionString =desc;
							selfRef.messages.put(url,"GIST: "+descriptionString);
							//Menu tmpGist = new Menu(desc);
							//setUpRepoMenue(ownerMenue.get(g.getOwnerName()), g);
							setUpRepoMenue( myGists, url,true,true) ;
						}
						// Now load the users GIT repositories
						// github.getMyOrganizations();
						Platform.runLater(() -> myOrganizations.getItems().clear());
						Platform.runLater(() -> myRepos.getItems().clear());
						Platform.runLater(() -> watchingRepos.getItems().clear());

						Map<String, GHOrganization> orgs = github.getMyOrganizations();
						for (Map.Entry<String, GHOrganization> entry : orgs.entrySet()) {
							// System.out.println("Org: "+org);
							Menu OrgItem = new Menu(entry.getKey());
							GHOrganization ghorg = entry.getValue();
							Map<String, GHRepository> repos = ghorg.getRepositories();
							for (Map.Entry<String, GHRepository> entry1 : repos.entrySet()) {
								setUpRepoMenue(OrgItem, entry1.getValue());
							}
							Platform.runLater(() -> {
								myOrganizations.getItems().add(OrgItem);
							});
						}
						GHMyself self = github.getMyself();
						// Repos I own
						myPublic = self.getAllRepositories();
						HashMap<String, Menu> myownerMenue = new HashMap<>();
						for (Map.Entry<String, GHRepository> entry : myPublic.entrySet()) {
							GHRepository g = entry.getValue();
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
									try {
										watchingRepos.getItems().add(ownerMenue.get(g.getOwnerName()));
									}catch(Exception e) {
										
									}
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
	private static void setUpRepoMenue(Menu repoMenue,GHRepository repo) {
		String url = repo.getGitTransportUrl().replace("git://", "https://");
		selfRef.messages.put(url,repo.getFullName());
		setUpRepoMenue( repoMenue,  url,true,true) ;
	}
	
	public static String gitURLtoMessage(String url) {
		while(true) {
			try {
				if(selfRef.messages.get(url)!=null)
					break;
				throw new RuntimeException();
			}catch(Exception e) {
				System.err.println("Waiting for API to load message data..."+url);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return selfRef.messages.get(url);
	}
	public static void setUpRepoMenue(Menu repoMenue,String url, boolean useAddToWorkspaceItem, boolean threaded)
	{
		setUpRepoMenue(repoMenue,url,useAddToWorkspaceItem,threaded,gitURLtoMessage(url));
	}
	public static void setUpRepoMenue(Menu repoMenue,String url, boolean useAddToWorkspaceItem, boolean threaded, String message) {
		
		Thread t =new Thread() {
			public void run() {

				//String menueMessage = repo.getFullName();
				Menu orgRepo = new Menu(message );
				Menu orgFiles = new Menu("Files");
				MenuItem loading = new MenuItem("Loading...");
				MenuItem updateRepo = new MenuItem("Update Repo...");
				MenuItem addToWs = new MenuItem("Add Repo to Workspace");
				addToWs.setOnAction(event -> {
					new Thread() {
						public void run() {
							BowlerStudioMenuWorkspace.add(url);
						}
					}.start();
				});
				//String url = repo.getGitTransportUrl().replace("git://", "https://");



				EventHandler<Event> loadFiles = new EventHandler<Event>() {
					public boolean gistFlag = false;

					@Override
					public void handle(Event ev) {

						System.out.println("Load file event "+url);
						new Thread() {
							public void run() {

								ThreadUtil.wait(500);
								System.out.println("Load file Thread "+url);
								if (!orgFiles.isShowing())
									return;
								if (gistFlag)
									return;// another thread is
											// servicing this gist
								gistFlag = true;
								System.out.println(
										"Loading files for " + message + " " );
								ArrayList<String> listofFiles;
								try {
									listofFiles = ScriptingEngine.filesInGit(url, ScriptingEngine.getFullBranch(url), null);
									System.out.println("Clone Done for " + url + listofFiles.size() + " files");
								} catch (Exception e1) {
									e1.printStackTrace();
									return;
								}
								if (orgFiles.getItems().size() != 1) {
									Log.warning("Bailing out of loading thread");
									return;// menue populated by
											// another thread
								}
								Platform.runLater(() -> {
									// removing this listener
									// after menue is activated
									// for the first time
									orgFiles.setOnShowing(null);
									gistFlag = false;
								});
								for (String s : listofFiles) {
									System.err.println("Adding file: "+s);
									MenuItem tmp = new MenuItem(s);
									tmp.setOnAction(event -> {
										new Thread() {
											public void run() {
												try {
													File fileSelected = ScriptingEngine.fileFromGit(url, s);
													BowlerStudio.createFileTab(fileSelected);
													BowlerStudioMenuWorkspace.add(url);
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
				updateRepo.setOnAction(event -> {
					new Thread() {
						public void run() {
							try {
								ScriptingEngine.pull(url, ScriptingEngine.getBranch(url));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							orgFiles.setOnShowing(loadFiles);
							orgFiles.getItems().clear();
							Platform.runLater(() ->orgFiles.getItems().add(loading));
							selfRef.setToLoggedIn();
						}
					}.start();
				});
				MenuItem addFile = new MenuItem("Add file to Git Repo...");
				addFile.setOnAction(event -> {
					System.out.println("Adding file to : " + url);
					Platform.runLater(() -> {
						Stage s = new Stage();

						AddFileToGistController controller = new AddFileToGistController(url, selfRef);
						try {
							controller.start(s);
						} catch (Exception e) {
							e.printStackTrace();
						}
						orgFiles.setOnShowing(loadFiles);
						orgFiles.getItems().clear();
						Platform.runLater(() ->orgFiles.getItems().add(loading));
						selfRef.setToLoggedIn();
					});
				});
				Platform.runLater(() -> {
					orgFiles.getItems().add(loading);
					if(useAddToWorkspaceItem)
						orgRepo.getItems().add(addToWs);
					orgRepo.getItems().addAll(updateRepo,addFile, orgFiles);
				});
				orgFiles.setOnShowing(loadFiles);
				Platform.runLater(() -> {
					repoMenue.getItems().add(orgRepo);
				});

			}
		};
		if(threaded)
			t.start();
		else
			t.run();
	}

	@FXML
	public void onLoadFile(ActionEvent e) {
		new Thread() {
			

			public void run() {
				setName("Load File Thread");
				if(openFile==null)
					openFile=ScriptingEngine.getLastFile();
				openFile = FileSelectionFactory.GetFile(openFile,
						new ExtensionFilter("All", "*.*"),
						new ExtensionFilter("Groovy Scripts", "*.groovy", "*.java", "*.txt"),
						new ExtensionFilter("Clojure", "*.cloj", "*.clj", "*.txt", "*.clojure"),
						new ExtensionFilter("Python", "*.py", "*.python", "*.txt"),
						new ExtensionFilter("DXF", "*.dxf", "*.DXF"),
						new ExtensionFilter("GCODE", "*.gcode", "*.nc", "*.ncg", "*.txt"),
						new ExtensionFilter("Image", "*.jpg", "*.jpeg", "*.JPG", "*.png", "*.PNG"),
						new ExtensionFilter("STL", "*.stl", "*.STL", "*.Stl"));
				if (openFile == null) {
					return;
				}

				bowlerStudioModularFrame.createFileTab(openFile);
			}
		}.start();
	}

	@FXML
	public void onConnect(ActionEvent e) {

		ConnectionManager.addConnection();

	}

	@FXML
	public void onConnectVirtual(ActionEvent e) {

		ConnectionManager.addConnection(new VirtualGenericPIDDevice(10000), "virtual");
	}

	@FXML
	public void onClose(ActionEvent e) {
		BowlerStudio.closeBowlerStudio();
	}

	@FXML
	public void onConnectCHDKCamera(ActionEvent event) {
//		Platform.runLater(() -> {
//			try {
//				ConnectionManager.addConnection(new CHDKImageProvider(), "cameraCHDK");
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
	}

	@FXML
	public void onConnectCVCamera(ActionEvent event) {

		//Platform.runLater(() -> ConnectionManager.onConnectCVCamera());

	}

	@FXML
	public void onConnectFileSourceCamera(ActionEvent event) {
		Platform.runLater(() -> ConnectionManager.onConnectFileSourceCamera());

	}

	@FXML
	public void onConnectURLSourceCamera(ActionEvent event) {

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

	@FXML
	public void onLogin(ActionEvent event) {
		new Thread() {
			public void run() {
				PasswordManager.checkInternet();
				ScriptingEngine.setLoginManager(new GitHubLoginManager());
				setName("Login Gist Thread");
				try {
					ScriptingEngine.logout();	
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ScriptingEngine.login();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	@FXML
	public void onLogout(ActionEvent event) {
		try {
			ScriptingEngine.logout();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	public void onConnectPidSim(ActionEvent event) {
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
	public void onMobileBaseFromFile(ActionEvent event) {
		new Thread() {
			public void run() {
				setName("Load Mobile Base Thread");
				File openFile = FileSelectionFactory.GetFile(ScriptingEngine.getLastFile(),
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
	public void onCreatenewGist(ActionEvent event) {
		Stage s = new Stage();
		new Thread(() ->
        {
            AddFileToGistController controller = new AddFileToGistController(null, selfRef);

            try {
                controller.start(s);
                setToLoggedIn(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
	}

	@FXML
	public void onOpenGitter(ActionEvent event) {
		String url = "https://gitter.im";
		try {
			BowlerStudio.openUrlInNewTab(new URL(url));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	public void clearScriptCache(ActionEvent event) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Are you sure you have published all your work?");
			alert.setHeaderText("This will wipe out the local cache");
			alert.setContentText("All files that are not published will be deleted");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				new Thread(() ->
                {
                    File cache = new File(ScriptingEngine.getWorkspace().getAbsolutePath() + "/gistcache/");
                    deleteFolder(cache);
                    System.exit(0);
                }).start();
			} else {
				System.out.println("Nothing was deleted");
			}
		});

	}

    @FXML
    public void changeAssetRepoButtonPressed(ActionEvent event)
    {
        Stage s = new Stage();
        new Thread(() ->
        {
            ChangeAssetRepoController controller = new ChangeAssetRepoController();

            try
            {
                controller.start(s);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }).start();
    }

	private static void deleteFolder(File folder) {

		System.out.println("Deleting " + folder.getAbsolutePath());
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	@FXML
	public void onMobileBaseFromGit(ActionEvent event) {
		PromptForGit.prompt("Select a Creature From a Git", "https://github.com/madhephaestus/carl-the-hexapod.git",
				(gitsId, file) -> {
					loadMobilebaseFromGit(gitsId, file);
				});
	}

	@FXML
	void onSaveConfiguration(ActionEvent event) {
		System.err.println("Saving database");
		new Thread() {
			public void run() {

				ConfigurationDatabase.save();
			}
		}.start();
	}

	@FXML // This method is called by the FXMLLoader when initialization is
			// complete
	void initialize() {
		assert CreaturesMenu != null : "fx:id=\"CreaturesMenu\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert GitHubRoot != null : "fx:id=\"GitHubRoot\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert getMeneBarBowlerStudio() != null : "fx:id=\"MeneBarBowlerStudio\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert addMarlinGCODEDevice != null : "fx:id=\"addMarlinGCODEDevice\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert clearCache != null : "fx:id=\"clearCache\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert createNewGist != null : "fx:id=\"createNewGist\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert logoutGithub != null : "fx:id=\"logoutGithub\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert myGists != null : "fx:id=\"myGists\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert myOrganizations != null : "fx:id=\"myOrganizations\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert myRepos != null : "fx:id=\"myRepos\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert watchingRepos != null : "fx:id=\"watchingRepos\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert workspacemenuHandle != null : "fx:id=\"workspacemenuHandle\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		selfRef=this;
		BowlerStudioMenuWorkspace.init(workspacemenuHandle);
		
		showDevicesPanel.setOnAction(event -> {
			bowlerStudioModularFrame.showConectionManager();
		});
		showCreatureLab.setOnAction(event -> {
			bowlerStudioModularFrame.showCreatureLab();
			;
		});
		showTerminal.setOnAction(event -> {
			bowlerStudioModularFrame.showTerminal();
		});
		new Thread() {
			public void run() {
				
				ThreadUtil.wait(500);
				try {
					ScriptingEngine.setAutoupdate(true);
					File f = ScriptingEngine.fileFromGit(
							"https://github.com/CommonWealthRobotics/BowlerStudioExampleRobots.git", // git
																								// repo,
																								// change
																								// this
																								// if
																								// you
																								// fork
																								// this
																								// demo
							"exampleRobots.json"// File from within the Git repo
					);

					@SuppressWarnings("unchecked")
					HashMap<String, HashMap<String, Object>> map = (HashMap<String, HashMap<String, Object>>) ScriptingEngine
							.inlineFileScriptRun(f, null);
					for (Map.Entry<String, HashMap<String, Object>> entry : map.entrySet()) {
						HashMap<String, Object> script = entry.getValue();
						MenuItem item = new MenuItem(entry.getKey());
						item.setOnAction(event -> {
							loadMobilebaseFromGit((String) script.get("scriptGit"), (String) script.get("scriptFile"));
						});
						Platform.runLater(() -> {
							CreaturesMenu.getItems().add(item);
						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

		addMarlinGCODEDevice.setOnAction(event -> {
			Platform.runLater(() -> ConnectionManager.onMarlinGCODE());
		});
		loadFirmata.setOnAction(event -> {
			Platform.runLater(() -> ConnectionManager.onFirmata());
		});
		Thread t= new Thread(new Runnable() {

			@Override
			public void run() {
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
				
				FxTimer.runLater(Duration.ofMillis(100), () -> {
					if (PasswordManager.getUsername()  != null) {
						setToLoggedIn(PasswordManager.getUsername() );
					} else {
						setToLoggedOut();
					}

				});
				IGithubLoginListener listener = new IGithubLoginListener() {
					private boolean loggingIn = false;

					@Override
					public void onLogout(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLogin(String arg0) {
						if (loggingIn)
							return;
						loggingIn = true;
						new Thread(new Runnable() {

							@Override
							public void run() {
								HashMap<String, Object> openGits = ConfigurationDatabase.getParamMap("studio-open-git");
								Object[] set = openGits.keySet().toArray();
								for (int i = 0; i < set.length; i++) {
									try {
										Thread.sleep(300);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
									if (String.class.isInstance(set[i])) {
										String s = (String) set[i];
										try {
											@SuppressWarnings("unchecked")
											ArrayList<String> repoFile = (ArrayList<String>) openGits.get(s);
											File f = ScriptingEngine.fileFromGit(repoFile.get(0), repoFile.get(1));
											if (!f.exists() ||createFileTab(f) == null ) {
												openGits.remove(s);
												System.err.println("Removing missing "+s);
											}
											
										} catch (Throwable e) {
											openGits.remove(s);
											System.out.println("Error loading file "+s);
										}
									}
								}
								HashMap<String, Object> openWeb = ConfigurationDatabase.getParamMap("studio-open-web");
								for (String s : openWeb.keySet()) {
									String repoFile = (String) openWeb.get(s);
									try {
										bowlerStudioModularFrame.openUrlInNewTab(new URL(repoFile));
									} catch (Exception e) {
										// TODO Auto-generated catch block
										// e.printStackTrace();
									}
								}
								loggingIn = false;
							}
						}).start();
					}
				};
				if (ScriptingEngine.isLoginSuccess()) {
					listener.onLogin(null);
				}
				ScriptingEngine.addIGithubLoginListener(listener);
				if(PasswordManager.hasNetwork() && !PasswordManager.loggedIn()) {
					new Thread(()->{
						try {
							ScriptingEngine.login();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}).start(); 	
				}else {
					setToLoggedIn(PasswordManager.getUsername() );
				}
			}
		});
		if(ScriptingEngine.hasNetwork())
			t.start();
		else
			if (PasswordManager.getUsername() != null) {
				setToLoggedIn(PasswordManager.getUsername() );
			} else {
				setToLoggedOut();
			}
		
		//WindowMenu
		int [] fonts = new int [] { 6,8,10,12,14,16,18,20,24,28,32,36,40};
		Menu fontSelect = new Menu("Font Size");
		ToggleGroup toggleGroup = new ToggleGroup();
		int defSize = ((Number) ConfigurationDatabase.getObject("BowlerStudioConfigs", "fontsize",
				12)).intValue();
		for(int i=0;i<fonts.length;i++){
			int myFoneNum = fonts[i];
			RadioMenuItem ftmp = new RadioMenuItem(myFoneNum+" pt");
			
			if(defSize==myFoneNum){
				ftmp.setSelected(true);
			}else
				ftmp.setSelected(false);
			ftmp.setOnAction((event)->{
				if(ftmp.isSelected()){
					BowlerStudioController.getBowlerStudio().setFontSize(myFoneNum);
				}
			});
			ftmp.setToggleGroup(toggleGroup);
			fontSelect.getItems().add(ftmp);
			
		}
		WindowMenu.getItems().add(fontSelect);
	}

	@FXML
	void onRefresh(ActionEvent event) {
		setToLoggedIn();
		
	}

}
