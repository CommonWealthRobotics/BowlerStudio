package com.neuronrobotics.bowlerstudio;
/**
 * Sample Skeleton for "BowlerStudioMenuBar.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import com.google.common.collect.Lists;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader;
import com.neuronrobotics.bowlerstudio.scripting.IGithubLoginListener;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTab;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
//import com.neuronrobotics.imageprovider.CHDKImageProvider;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.PromptForGit;
import com.neuronrobotics.pidsim.LinearPhysicsEngine;
import com.neuronrobotics.replicator.driver.NRPrinter;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.pid.VirtualGenericPIDDevice;
import com.neuronrobotics.sdk.util.ThreadUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.github.*;
import org.reactfx.util.FxTimer;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class BowlerStudioMenu implements MenuRefreshEvent,INewVitaminCallback {

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
    @FXML
    private Menu vitaminsMenu;

    @FXML
    private MenuItem addNewVitamin;

	private BowlerStudioModularFrame bowlerStudioModularFrame;

	private String name;
	private static BowlerStudioMenu selfRef = null;
	private File openFile;
	private Map<String, GHRepository> myPublic;
	// PagedIterable<GHGist> gists ;
	private HashMap<String, String> messages = new HashMap<String, String>();
	private static SimpleDateFormat format = new SimpleDateFormat("E 'the' dd 'in' MMM-yyyy 'at' HH:mm");
	private static SimpleDateFormat formatSimple = new SimpleDateFormat("MM-dd");
	private static IssueReportingExceptionHandler exp = new IssueReportingExceptionHandler();
	private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
	private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
	private HashMap<String,Menu> vitaminTypeMenus = new HashMap<String, Menu>();
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
					MobileBase mb;
					if (file.toLowerCase().endsWith(".xml")) {
						mb = MobileBaseLoader.fromGit(id, file);
					} else {
						mb = (MobileBase) ScriptingEngine.gitScriptRun(id, file, null);
					}
					ConnectionManager.addConnection(mb, mb.getScriptingName());

				} catch (Exception e) {
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
		if(name==null)
			return;
		if(this.name!=null && name.contentEquals(this.name))
			return;
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
						e2.printStackTrace();
					}
					if (!PasswordManager.hasNetwork())
						return;
					GitHub github = PasswordManager.getGithub();
					while (github == null && !PasswordManager.loggedIn()) {
						github = PasswordManager.getGithub();
						ThreadUtil.wait(200);
					}
					try {
						GHMyself myself = github.getMyself();
						PagedIterable<GHGist> gists = myself.listGists();
						Platform.runLater(() -> {
							myGists.getItems().clear();
						});
						ThreadUtil.wait(20);
						for (GHGist gist : gists) {

							String url = gist.getGitPushUrl();
							String desc = gist.getDescription();
							if (desc == null || desc.length() == 0
									|| desc.contentEquals("Adding new file from BowlerStudio")) {
								desc = gist.getFiles().keySet().toArray()[0].toString();
							}
							String descriptionString = desc;
							selfRef.messages.put(url, "GIST: " + descriptionString);
							// Menu tmpGist = new Menu(desc);
							// setUpRepoMenue(ownerMenue.get(g.getOwnerName()), g);
							setUpRepoMenue(myGists, url, true, true);
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
								resetRepoMenue(OrgItem, entry1.getValue());
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

							resetRepoMenue(myownerMenue.get(g.getOwnerName()), g);
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
									} catch (Exception e) {

									}
								});
							}
							resetRepoMenue(ownerMenue.get(g.getOwnerName()), g);
						}

					} catch (Exception e) {
						exp.uncaughtException(Thread.currentThread(), e);
					}

				}
			}.start();

		});

	}

	public static String gitURLtoMessage(String url) {
		while (true) {
			try {
				if (selfRef.messages.get(url) != null)
					break;
				throw new RuntimeException();
			} catch (Exception e) {
				// System.err.println("Waiting for API to load message data..."+url);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					exp.uncaughtException(Thread.currentThread(), e);
				}
			}
		}
		return selfRef.messages.get(url);
	}

	public static void setUpRepoMenue(Menu repoMenue, String url, boolean useAddToWorkspaceItem, boolean threaded) {
		setUpRepoMenue(repoMenue, url, useAddToWorkspaceItem, threaded, gitURLtoMessage(url));
	}

	private static void resetRepoMenue(Menu repoMenue, GHRepository repo) {
		String url = repo.getGitTransportUrl().replace("git://", "https://");
		selfRef.messages.put(url, repo.getFullName());
		setUpRepoMenue(repoMenue, url, true, true);
	}

	public static void setUpRepoMenue(Menu repoMenue, String url, boolean useAddToWorkspaceItem, boolean threaded,
			String message) {

		Thread t = new Thread() {
			public void run() {

				// String menueMessage = repo.getFullName();
				Menu orgRepo = new Menu(message);
				Menu orgFiles = new Menu("Files");
				Menu orgCommits = new Menu("Commits");
				Menu orgBranches = new Menu("Branches");


				MenuItem updateRepo = new MenuItem("Update Repo...");
				MenuItem addToWs = new MenuItem("Add Repo to Workspace");
				addToWs.setOnAction(event -> {
					new Thread() {
						public void run() {
							BowlerStudioMenuWorkspace.add(url);
						}
					}.start();
				});
				// String url = repo.getGitTransportUrl().replace("git://", "https://");

				MenuResettingEventHandler loadCommitsEvent = createLoadCommitsEvent(url, orgCommits);
				MenuResettingEventHandler loadBranchesEvent = createLoadBranchesEvent(url, orgBranches);
				MenuResettingEventHandler loadFilesEvent = createLoadFileEvent(url, orgFiles);
				Runnable myEvent = new Runnable() {
					@Override
					public void run() {
						try {
//							System.err.println("\n\nCommit event Detected " + url + " on branch "
//									+ ScriptingEngine.getBranch(url));
							//new RuntimeException().printStackTrace();
							Platform.runLater(() ->resetMenueForLoadingFiles("Files:",orgFiles,  loadFilesEvent));
							Platform.runLater(() ->resetMenueForLoadingFiles("Commits:",orgCommits, loadCommitsEvent));
							Platform.runLater(() ->resetMenueForLoadingFiles("Branches:",orgBranches, loadBranchesEvent));
							
						} catch (Throwable e) {
							exp.uncaughtException(Thread.currentThread(), e);
						}

					}
				};
				loadCommitsEvent.setMenuReset(myEvent);
				loadBranchesEvent.setMenuReset(myEvent);
				loadFilesEvent.setMenuReset(myEvent);

				updateRepo.setOnAction(event -> {
					new Thread() {
						public void run() {
							try {
								ScriptingEngine.pull(url, ScriptingEngine.getBranch(url));
							} catch (Exception e) {
								exp.uncaughtException(Thread.currentThread(), e);
							}
							myEvent.run();
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
						myEvent.run();
						selfRef.setToLoggedIn();
					});
				});
				
				ScriptingEngine.addOnCommitEventListeners(url, myEvent);
				orgRepo.setOnShowing(event -> {
					//On showing the menu, set up the rest of the handlers
					new Thread(myEvent).start();
				});
				Platform.runLater(() -> {
					if (useAddToWorkspaceItem)
						orgRepo.getItems().add(addToWs);
					orgRepo.getItems().addAll(updateRepo, addFile, orgFiles, orgCommits, orgBranches);
					Platform.runLater(() -> {
						repoMenue.getItems().add(orgRepo);
					});
				});

			}

		};
		if (threaded)
			t.start();
		else
			t.run();
	}

	private static MenuResettingEventHandler createLoadCommitsEvent(String url, Menu orgCommits) {
		return new MenuResettingEventHandler() {
			public boolean gistFlag = false;

			@Override
			public void handle(Event event) {
				if (gistFlag) {
					System.err.println("Another thread is managing this event " + url);
					return;// another thread is
							// servicing this gist
				}
				gistFlag = true;
				String branchName;
				try {
					branchName = ScriptingEngine.getFullBranch(url);
				} catch (IOException e1) {
					exp.uncaughtException(Thread.currentThread(), e1);
					return;
				}
				System.out.println("Load Commits event " + url + " on branch " + branchName);
				new Thread(() -> {
					Platform.runLater(() -> {
						// removing this listener
						// after menue is activated
						// for the first time
						orgCommits.setOnShowing(null);
						gistFlag = false;
					});
					try {
						ScriptingEngine.checkout(url, branchName);
						Repository repo = ScriptingEngine.getRepository(url);
						Git git = new Git(repo);

						// System.out.println("Commits of branch: " + branchName);
						// System.out.println("-------------------------------------");

						Iterable<RevCommit> commits = git.log().add(repo.resolve(branchName)).call();

						List<RevCommit> commitsList = Lists.newArrayList(commits.iterator());
						Platform.runLater(() -> {
							try {
								orgCommits.getItems().add(new MenuItem("On Branch " + ScriptingEngine.getBranch(url)));
							} catch (IOException e) {
								exp.uncaughtException(Thread.currentThread(), e);
							}
							orgCommits.getItems().add(new SeparatorMenuItem());
						});
						RevCommit previous =null;
						for (RevCommit commit : commitsList) {
							String date = format.format(new Date(commit.getCommitTime() * 1000L));
							String fullData = commit.getName() + "\r\n" + commit.getAuthorIdent().getName() + "\r\n"
									+ date + "\r\n" + commit.getFullMessage()
									+ "\r\n" + "---------------------------------------------------\r\n";//+
									//previous==null?"":getDiffOfCommit(previous,commit, repo, git);
							
							previous=commit;
							String string = date + " " +commit.getAuthorIdent().getName()+" "+ commit.getShortMessage();
							if (string.length() > 80)
								string = string.substring(0, 80);
							//MenuItem tmp = new MenuItem(string);
							CustomMenuItem tmp = new CustomMenuItem(new Label(string));
							Tooltip tooltip = new Tooltip(fullData);
							Tooltip.install(tmp.getContent(), tooltip);
							tmp.setOnAction(ev -> {
								new Thread() {
									public void run() {
										System.out.println("Selecting \r\n\r\n" + fullData);
										
										
										String branch;
										try {
											branch = ScriptingEngine.getBranch(url);
										} catch (IOException e1) {
											branch="newBranch";
										}
										

										String dateString = formatSimple.format( new Date(commit.getCommitTime() * 1000L)   );
										promptForNewBranch(branch+"-"+dateString,"Creating Branch From Commit:\n\n"+fullData,newBranch -> {
											new Thread() {
												public void run() {
													try {
														String slugify = slugify(newBranch);
														System.out.println("Creating " + slugify);
														ScriptingEngine.setCommitContentsAsCurrent(url, slugify,
																commit);
													} catch (IOException e) {
														exp.uncaughtException(Thread.currentThread(), e);
													} catch (GitAPIException e) {
														exp.uncaughtException(Thread.currentThread(), e);
													}
												}
											}.start();
										});

									}
								}.start();

							});
							Platform.runLater(() -> {
								orgCommits.getItems().add(tmp);
							});
						}
						git.close();
						Platform.runLater(() -> {
							orgCommits.hide();
							Platform.runLater(() -> {
								orgCommits.show();
							});
						});
					} catch (IOException e) {
						exp.uncaughtException(Thread.currentThread(), e);
					} catch (RevisionSyntaxException e) {
						exp.uncaughtException(Thread.currentThread(), e);
					} catch (NoHeadException e) {
						exp.uncaughtException(Thread.currentThread(), e);
					} catch (GitAPIException e) {
						exp.uncaughtException(Thread.currentThread(), e);
					}

				}).start();
			}
		};
	}

	public static String slugify(String input) {
		String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
	    String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
	    String slug = NONLATIN.matcher(normalized).replaceAll("").replace('-', '_');
	    
	    return slug.toLowerCase(Locale.ENGLISH);
	}

	private static void promptForNewBranch(String exampleName,String reasonForCreating,Consumer<String> resultEvent) {
		Platform.runLater(() -> {
			TextInputDialog dialog = new TextInputDialog(exampleName);
			dialog.setTitle("Create New Branch");
			dialog.setHeaderText(reasonForCreating);
			dialog.setContentText("Enter a new branch name: ");
			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			// The Java 8 way to get the response value (with lambda expression).
			result.ifPresent(resultEvent);
		});
	}

	private static MenuResettingEventHandler createLoadBranchesEvent(String url, Menu orgBranches) {
		return new MenuResettingEventHandler() {
			public boolean gistFlag = false;
			EventHandler<Event> thisEvent = this;
			@Override
			public void handle(Event event) {
				if (gistFlag) {
					System.err.println("Another thread is managing this event " + url);
					return;// another thread is
							// servicing this gist
				}
				gistFlag = true;
				System.out.println("Load Branches event " + url);
				final MenuItem onBranch;
				try {
					onBranch = new MenuItem("On Branch " + ScriptingEngine.getBranch(url));
				} catch (IOException e1) {
					exp.uncaughtException(Thread.currentThread(), e1);
					return;
				}
				;
				new Thread(() -> {
					Platform.runLater(() -> {
						// removing this listener
						// after menue is activated
						// for the first time
						orgBranches.setOnShowing(null);
						gistFlag = false;
					});
					MenuItem newBranchItem = new MenuItem("New Branch...");
					String newBranchName="";
					try {
						newBranchName = ScriptingEngine.getBranch(url);
					} catch (IOException e1) {}
					String newBranchName1=newBranchName;

					String dateString = formatSimple.format( new Date()   );

					newBranchItem.setOnAction(event1 -> {
						promptForNewBranch(newBranchName1+"-"+dateString,
								"Create a new Branch from "+newBranchName1,
								newBranch -> {
									new Thread() {
										public void run() {
											try {
												String slugify = slugify(newBranch);
												System.out.println("Creating Branch " + slugify);
												ScriptingEngine.newBranch(url, slugify);
												getMenuReset().run();
											} catch (IOException e) {
												exp.uncaughtException(Thread.currentThread(), e);
											} catch (GitAPIException e) {
												exp.uncaughtException(Thread.currentThread(), e);
											}
										}
									}.start();
								});

					});
					Platform.runLater(() -> {
						
						try {
							onBranch.setText("On Branch " + ScriptingEngine.getBranch(url));
							orgBranches.getItems().add(onBranch);
						} catch (IOException e) {
							exp.uncaughtException(Thread.currentThread(), e);
							
						}
						orgBranches.getItems().add(new SeparatorMenuItem());
						orgBranches.getItems().add(newBranchItem);
						orgBranches.getItems().add(new SeparatorMenuItem());
					});

					try {
						Collection<Ref> branches = ScriptingEngine.getAllBranches(url);
						for (Ref r : branches) {
							createRepoMenuItem(url, orgBranches, onBranch, r,getMenuReset());
						}
					} catch (IOException e) {
						exp.uncaughtException(Thread.currentThread(), e);
					} catch (GitAPIException e) {
						exp.uncaughtException(Thread.currentThread(), e);
					}
					System.err.println("Refreshing menu Branches");
					Platform.runLater(() -> {
						orgBranches.hide();
						Platform.runLater(() -> {
							orgBranches.show();
						});
					});
				}).start();
			}

			
		};
	}
	private static void createRepoMenuItem(String url, Menu orgBranches,  final MenuItem onBranch, Ref r, Runnable menureset) {
		String[] name2 = r.getName().split("/");
		MenuItem tmp = new MenuItem(name2[name2.length - 1]);
		Ref select = r;
		String[] name = select.getName().split("/");
		String myName = name[name.length - 1];
		// System.out.println("Selecting Branch\r\n"+url+" \t\t"+myName);
		tmp.setOnAction(ev -> {
			new Thread() {
				public void run() {
					try {
						switchToThisNewBranch(url, onBranch, select, myName);
						menureset.run();

					} catch (IOException e) {
						exp.uncaughtException(Thread.currentThread(), e);
					}

				}

				private void switchToThisNewBranch(String url, final MenuItem onBranch, Ref select, String myName)
						throws IOException {
					String was = ScriptingEngine.getBranch(url);
					ScriptingEngine.checkout(url, select);
					String s = ScriptingEngine.getBranch(url);
					if (myName.contentEquals(s))
						System.out.println("Changing from " + was + " to " + myName + " is now "
								+ s + "... Success!");
					onBranch.setText("On Branch " + s);
				}
			}.start();

		});
		Platform.runLater(() -> {
			orgBranches.getItems().add(tmp);
		});
	}
	@FXML
	public void onLoadFile(ActionEvent e) {
		new Thread() {

			public void run() {
				setName("Load File Thread");
				if (openFile == null)
					openFile = ScriptingEngine.getLastFile();
				openFile = FileSelectionFactory.GetFile(openFile, new ExtensionFilter("All", "*.*"),
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

	private static void resetMenueForLoadingFiles(String string, Menu orgFiles,  EventHandler<Event> loadFiles) {
		Platform.runLater(() -> {
			try {
				Platform.runLater(() ->{
					orgFiles.getItems().clear();
					Platform.runLater(() ->{
						orgFiles.getItems().add(new MenuItem(string));
						orgFiles.getItems().add(new SeparatorMenuItem());
						orgFiles.setOnShowing(loadFiles);
					});
				});
			} catch (Throwable t) {
				t.printStackTrace();
			}
		});
	}

	private static MenuResettingEventHandler createLoadFileEvent(String url, Menu orgFiles) {
		return new MenuResettingEventHandler() {
			public boolean gistFlag = false;

			@Override
			public void handle(Event ev) {
				if (gistFlag) {
					System.err.println("Another thread is managing this event");
					return;// another thread is
							// servicing this gist
				}
				gistFlag = true;
				System.out.println("Load file event " + url);
				new Thread() {
					public void run() {
						setName("Load file Thread " + url);

						System.out.println("Loading files for " + url + " ");
						ArrayList<String> listofFiles;
						try {
							listofFiles = ScriptingEngine.filesInGit(url, ScriptingEngine.getFullBranch(url), null);
							System.out.println("Clone Done for " + url + listofFiles.size() + " files");
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
//						if (orgFiles.getItems().size() != 1) {
//							Log.warning("Bailing out of loading thread");
//							return;// menue populated by
//									// another thread
//						}
						Platform.runLater(() -> {
							// removing this listener
							// after menue is activated
							// for the first time
							orgFiles.setOnShowing(null);
							gistFlag = false;
						});
						for (String s : listofFiles) {
							System.err.println("Adding file: " + s);
							String string = s;
							if (s.length() > 80)
								s = s.substring(0, 10) + "..." + s.substring(s.length() - 70, s.length() - 1);
							MenuItem tmp = new MenuItem(s);
							tmp.setOnAction(event -> {
								new Thread() {
									public void run() {
										try {
											File fileSelected = ScriptingEngine.fileFromGit(url, string);
											BowlerStudio.createFileTab(fileSelected);
											BowlerStudioMenuWorkspace.add(url);
											getMenuReset().run();
										} catch (Exception e) {
											exp.uncaughtException(Thread.currentThread(), e);
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
							Platform.runLater(() -> {
								orgFiles.show();
								
							});
						});
					}
				}.start();
			}
		};
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
//				exp.uncaughtException(Thread.currentThread(), e);
//			}
//		});
	}

	@FXML
	public void onConnectCVCamera(ActionEvent event) {

		// Platform.runLater(() -> ConnectionManager.onConnectCVCamera());

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
						exp.uncaughtException(Thread.currentThread(), e);
					}
					ScriptingEngine.login();
				} catch (IOException e) {
					exp.uncaughtException(Thread.currentThread(), e);
				}
			}
		}.start();

	}

	@FXML
	public void onLogout(ActionEvent event) {
		try {
			ScriptingEngine.logout();
		} catch (IOException e) {
			exp.uncaughtException(Thread.currentThread(), e);
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
						exp.uncaughtException(Thread.currentThread(), e);
					}
				});
			}
		}.start();

	}

	@FXML
	public void onCreatenewGist(ActionEvent event) {
		Stage s = new Stage();
		new Thread(() -> {
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
			exp.uncaughtException(Thread.currentThread(), e);
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
				new Thread(() -> {
					BowlerStudio.setDeleteFlag(true);
					BowlerStudio.exit();
				}).start();
			} else {
				System.out.println("Nothing was deleted");
			}
		});

	}

	@FXML
	public void changeAssetRepoButtonPressed(ActionEvent event) {
		Stage s = new Stage();
		new Thread(() -> {
			ChangeAssetRepoController controller = new ChangeAssetRepoController();

			try {
				controller.start(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
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
		assert vitaminsMenu != null : "fx:id=\"vitaminsMenu\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert addNewVitamin != null : "fx:id=\"addNewVitamin\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";

		selfRef = this;
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
					exp.uncaughtException(Thread.currentThread(), e);
				}
			}
		}.start();
		

		addMarlinGCODEDevice.setOnAction(event -> {
			Platform.runLater(() -> ConnectionManager.onMarlinGCODE());
		});
		loadFirmata.setOnAction(event -> {
			Platform.runLater(() -> ConnectionManager.onFirmata());
		});
		Thread t = new Thread(new Runnable() {

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
					if (PasswordManager.getUsername() != null) {
						setToLoggedIn(PasswordManager.getUsername());
					} else {
						setToLoggedOut();
					}

				});
				IGithubLoginListener listener = new IGithubLoginListener() {
					private boolean loggingIn = false;

					@Override
					public void onLogout(String arg0) {

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
											if (!f.exists() || createFileTab(f) == null) {
												openGits.remove(s);
												System.err.println("Removing missing " + s);
											}

										} catch (Throwable e) {
											openGits.remove(s);
											System.out.println("Error loading file " + s);
										}
									}
								}
								HashMap<String, Object> openWeb = ConfigurationDatabase.getParamMap("studio-open-web");
								for (String s : openWeb.keySet()) {
									String repoFile = (String) openWeb.get(s);
									try {
										bowlerStudioModularFrame.openUrlInNewTab(new URL(repoFile));
									} catch (Exception e) {
										exp.uncaughtException(Thread.currentThread(), e);
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
				if (PasswordManager.hasNetwork() && !PasswordManager.loggedIn()) {
					new Thread(() -> {
						try {
							ScriptingEngine.login();
						} catch (IOException e) {
							exp.uncaughtException(Thread.currentThread(), e);
						}
					}).start();
				} else {
					setToLoggedIn(PasswordManager.getUsername());
				}
			}
		});
		if (ScriptingEngine.hasNetwork())
			t.start();
		else if (PasswordManager.getUsername() != null) {
			setToLoggedIn(PasswordManager.getUsername());
		} else {
			setToLoggedOut();
		}

		// WindowMenu
		int[] fonts = new int[] { 6, 8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 40 };
		Menu fontSelect = new Menu("Font Size");
		ToggleGroup toggleGroup = new ToggleGroup();
		int defSize = ((Number) ConfigurationDatabase.getObject("BowlerStudioConfigs", "fontsize", 12)).intValue();
		for (int i = 0; i < fonts.length; i++) {
			int myFoneNum = fonts[i];
			RadioMenuItem ftmp = new RadioMenuItem(myFoneNum + " pt");

			if (defSize == myFoneNum) {
				ftmp.setSelected(true);
			} else
				ftmp.setSelected(false);
			ftmp.setOnAction((event) -> {
				if (ftmp.isSelected()) {
					BowlerStudioController.getBowlerStudio().setFontSize(myFoneNum);
				}
			});
			ftmp.setToggleGroup(toggleGroup);
			fontSelect.getItems().add(ftmp);

		}
		WindowMenu.getItems().add(fontSelect);
		
		new Thread() {
			public void run() {
				setUncaughtExceptionHandler(new IssueReportingExceptionHandler());
				try {
					if(vitaminsMenu==null)
						throw new RuntimeException("Vitamins menu was not inserted");
					if(vitaminsMenu.getItems()==null)
						throw new RuntimeException("Vitamins menu items are null");
					Platform.runLater(()->{
						vitaminsMenu.getItems().add(new SeparatorMenuItem());
					});	
					ArrayList<String> types = Vitamins.listVitaminTypes();
					for(String s:types) {
						addVitaminType(s);
					}
				} catch (Exception e) {
					exp.uncaughtException(Thread.currentThread(), e);
				}
			}
		}.start();
		
	}
	public void addVitaminType(String s) {
		getTypeMenu(s);
		ArrayList<String> sizes = Vitamins.listVitaminSizes(s);
		for(String size:sizes) {
			addSizesToMenu(size,s);
		}
		
	}
	public Menu getTypeMenu(String type) {
		if(vitaminTypeMenus.get(type)==null) {
			Menu typeMenu = new Menu(type);
			vitaminTypeMenus.put(type, typeMenu);
			Platform.runLater(()->{
				vitaminsMenu.getItems().add(typeMenu);
			});
			Platform.runLater(()->{
				typeMenu.getItems().add(new MenuItem("Sizes:"));
				typeMenu.getItems().add(new SeparatorMenuItem());
			});
		}
		return vitaminTypeMenus.get(type);
	}
	
	public  void addSizesToMenu( String size,String type) {
		MenuItem sizeMenu =new MenuItem(size);
		Platform.runLater(()->{
			getTypeMenu(type).getItems().add(sizeMenu);
		});
		sizeMenu.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new Thread() {
					public void run() {
						LocalFileScriptTab tab =LocalFileScriptTab.getSelectedTab();
						
						tab.insertString("CSG vitamin_"+slugify(type)+"_"+slugify(size)+" = Vitamins.get(\""+type+"\", \""+size+"\")\n");
					}
				}.start();
			}
		});
	}

	@FXML
	void onRefresh(ActionEvent event) {
		setToLoggedIn();

	}
    @FXML
    void onCreateNewVitamin(ActionEvent event) {
    	try {
			NewVitaminWizardController.launchWizard(this);
		} catch (Exception e) {
			new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);
			
		}
    }
    @FXML
    void onBowlerStudioHelp(ActionEvent event) {
    	new Thread(()->{
    		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
    		    try {
					Desktop.getDesktop().browse(new URI("https://hackaday.io/project/6423-bowlerstudio-a-robotics-development-platform"));
				} catch (IOException e) {
					new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);
					
				} catch (URISyntaxException e) {
					new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);
					
				}
    		}
    	}) .start();
    }
}
