package com.neuronrobotics.bowlerstudio;

/**
 * Sample Skeleton for "BowlerStudioMenuBar.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import com.google.common.collect.Lists;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.bowlerstudio.assets.FontSizeManager;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader;
import com.neuronrobotics.bowlerstudio.scripting.IGithubLoginListener;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingFileWidget;
import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTab;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import com.neuronrobotics.nrconsole.util.CommitWidget;
//import com.neuronrobotics.imageprovider.CHDKImageProvider;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.PromptForGit;
import com.neuronrobotics.pidsim.LinearPhysicsEngine;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.pid.VirtualGenericPIDDevice;
import com.neuronrobotics.sdk.util.ThreadUtil;

import eu.mihosoft.vrl.v3d.CSG;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.github.*;

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
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
public class BowlerStudioMenu implements MenuRefreshEvent, INewVitaminCallback {

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
	@FXML // fx:id="showTerminal"
	private Menu WindowMenu;
	@FXML // fx:id="watchingRepos"
	private Menu watchingRepos; // Value injected by FXMLLoader
	@FXML
	private Menu vitaminsMenu;

	@FXML
	private MenuItem addNewVitamin;

	private BowlerStudioModularFrame bowlerStudioModularFrame;

	private String username;
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
	private HashMap<String, Menu> vitaminTypeMenus = new HashMap<String, Menu>();

	private CreatureLab3dController creatureLab3dController;

	public BowlerStudioMenu(BowlerStudioModularFrame tl, CreatureLab3dController creatureLab3dController) {
		bowlerStudioModularFrame = tl;
		this.creatureLab3dController = creatureLab3dController;


		
	}

	@FXML
	public void onMobileBaseFromGist(ActionEvent event) {
		PromptForGit.prompt("Select a Creature From a Gist", "bcb4760a449190206170", (gitsId, file) -> {
			loadMobilebaseFromGist(gitsId, file);
		});
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
			Exception ex = new Exception("Error Loading " + id + ":" + file);

			// String stacktraceFromCatch =
			// org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex);
			public void run() {
				File f = null;
				try {
					f = ScriptingEngine.fileFromGit(id, file);
					runScriptFromGit(id, file);
				} catch (Throwable e) {
					System.out.println("Error Loading " + id + ":" + file);
					BowlerStudio.printStackTrace(e, f);
					BowlerStudio.printStackTrace(ex, f);
					// exp.except(ex,stacktraceFromCatch);
				}
			}

			private void runScriptFromGit(String id, String file) throws Exception {
				MobileBase mb;
				ScriptingEngine.pull(id);

				mb = (MobileBase) ScriptingEngine.gitScriptRun(id, file, null);

				if (mb != null)
					ConnectionManager.addConnection(mb, mb.getScriptingName());
				else
					System.out.println("\r\n\r\nNO MOBILE BASE found at " + id + "\t" + file);
			}
		}.start();

	}

	public void openUrlInNewTab(URL url) {
		bowlerStudioModularFrame.openUrlInNewTab(url);
	}

	public void setToLoggedOut() {
		this.username="";
		BowlerStudio.runLater(() -> {
			myGists.getItems().clear();
			logoutGithub.disableProperty().set(true);
			logoutGithub.setText("Anonymous");
			//ConfigurationDatabase.loginEvent(null);
			this.username=null;
		});
		while(this.username!=null)
			ThreadUtil.wait(4);
		
	}

	public void setToLoggedIn() {
		setToLoggedIn(username);
	}

	private void setToLoggedIn(final String n) {
		//new Exception().printStackTrace();
		if (n == null)
			return;
		this.username = n;
		
		BowlerStudio.runLater(() -> {
			logoutGithub.disableProperty().set(false);
			logoutGithub.setText("Log out " + username);
			new Thread() {
				public void run() {
					//ConfigurationDatabase.loginEvent(username);
					ConfigurationDatabase.getParamMap("workspace");
					BowlerStudioMenuWorkspace.loginEvent();
					if (!PasswordManager.hasNetwork())
						return;
					GitHub gh = PasswordManager.getGithub();
					while (gh == null || !PasswordManager.loggedIn()) {
						gh = PasswordManager.getGithub();
						ThreadUtil.wait(200);
					}
					new Thread(()->{
						openFilesInUI();
					}).start();
					GitHub github = gh;
					loadOrganizations(github);
					loadMyRepos(github);
					loadWatchingRepos(github);
					LoadGistMenu(github);
					
				}



			}.start();

		});

	}
	private void openFilesInUI() {
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
					if (!f.exists() || BowlerStudio.createFileTab(f) == null) {
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
	}
	private void loadWatchingRepos(GitHub github) {
		new Thread(() -> {
			BowlerStudio.runLater(() -> watchingRepos.getItems().clear());
			ThreadUtil.wait(20);
			GHMyself self;
			try {
				self = github.getMyself();
				// Watched repos
				List<GHRepository> watching = self.listSubscriptions().asList();
				HashMap<String, Menu> ownerMenue = new HashMap<>();
				for (GHRepository g : watching) {
					if (ownerMenue.get(g.getOwnerName()) == null) {
						ownerMenue.put(g.getOwnerName(), new Menu(g.getOwnerName()));
						BowlerStudio.runLater(() -> {
							try {
								watchingRepos.getItems().add(ownerMenue.get(g.getOwnerName()));
							} catch (Exception e) {

							}
						});
					}
					resetRepoMenue(ownerMenue.get(g.getOwnerName()), g);
				}
			} catch (IOException e1) {
				new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e1);

			}
		}).start();
	}

	private void loadMyRepos(GitHub github) {
		new Thread(() -> {
			BowlerStudio.runLater(() -> myRepos.getItems().clear());
			ThreadUtil.wait(20);
			// Repos I own
			try {
				GHMyself self = github.getMyself();
				myPublic = self.getAllRepositories();
				HashMap<String, Menu> myownerMenue = new HashMap<>();
				for (Map.Entry<String, GHRepository> entry : myPublic.entrySet()) {
					GHRepository g = entry.getValue();
					if (myownerMenue.get(g.getOwnerName()) == null) {
						myownerMenue.put(g.getOwnerName(), new Menu(g.getOwnerName()));
						BowlerStudio.runLater(() -> {
							String ownerName = g.getOwnerName();
							if(ownerName==null)
								throw new RuntimeException("ownerName can not be null");
							Menu e = myownerMenue.get(ownerName);
							if(e==null)
								throw new RuntimeException("Menu can not be null");
							ObservableList<MenuItem> items = myRepos.getItems();
							if(items==null)
								throw new RuntimeException("Menue items can not be null");
							items.add(e);
						});
					}

					resetRepoMenue(myownerMenue.get(g.getOwnerName()), g);
				}
			} catch (Exception ex) {
				new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), ex);
				// i have no public repso
			}
		}).start();
	}

	private void loadOrganizations(GitHub github) {
		new Thread(() -> {
			BowlerStudio.runLater(() -> myOrganizations.getItems().clear());
			ThreadUtil.wait(20);

			Map<String, GHOrganization> orgs;
			try {
				orgs = github.getMyOrganizations();
				for (Map.Entry<String, GHOrganization> entry : orgs.entrySet()) {
					// System.out.println("Org: "+org);
					Menu OrgItem = new Menu(entry.getKey());
					GHOrganization ghorg = entry.getValue();
					Map<String, GHRepository> repos = ghorg.getRepositories();
					for (Map.Entry<String, GHRepository> entry1 : repos.entrySet()) {
						resetRepoMenue(OrgItem, entry1.getValue());
					}
					BowlerStudio.runLater(() -> {
						myOrganizations.getItems().add(OrgItem);
					});
				}
			} catch (Exception e) {
				PasswordManager.checkInternet();
				if (PasswordManager.hasNetwork())
					new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);

			}

		}).start();
	}

	private void LoadGistMenu(GitHub github) {
		new Thread(() -> {
			GHMyself myself;
			try {
				myself = github.getMyself();
				System.out.println("Loading all my Gists");
				BowlerStudio.runLater(() -> {
					myGists.getItems().clear();
				});
				List<GHGist> gists = myself.listGists().asList();
				for (GHGist gist : gists) {

					String url = gist.getGitPushUrl();
					
					String desc = gist.getDescription();
					if (desc == null || desc.length() == 0 || desc.contentEquals("Adding new file from BowlerStudio")) {
						desc = gist.getFiles().keySet().toArray()[0].toString();
					}
					String descriptionString = desc;
					getSelfRef().messages.put(url, "GIST: " + descriptionString);
					// Menu tmpGist = new Menu(desc);
					// setUpRepoMenue(ownerMenue.get(g.getOwnerName()), g);
					setUpRepoMenue(myGists, url, true, true);
				}
			} catch (IOException e) {
				new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);

			}
		}).start();
	}

	public static String gitURLtoMessage(String url) {
		for (int i = 0; i < 5; i++) {
			try {
				if (getSelfRef().messages.get(url) != null)
					break;
				throw new RuntimeException();
			} catch (Exception e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		String string = getSelfRef().messages.get(url);
		if (string == null)
			string = url;
		return string;
	}

	public static void setUpRepoMenue(Menu repoMenue, String url, boolean useAddToWorkspaceItem, boolean threaded) {
		if(url.endsWith(".git"))
			setUpRepoMenue(repoMenue, url, useAddToWorkspaceItem, threaded, gitURLtoMessage(url));
	}

	private static void resetRepoMenue(Menu repoMenue, GHRepository repo) {
		String url = repo.getGitTransportUrl().replace("git://", "https://");
		getSelfRef().messages.put(url, repo.getFullName());
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
							// new RuntimeException().printStackTrace();
							BowlerStudio.runLater(() -> resetMenueForLoadingFiles("Files:", orgFiles, loadFilesEvent));
							BowlerStudio.runLater(
									() -> resetMenueForLoadingFiles("Commits:", orgCommits, loadCommitsEvent));
							BowlerStudio.runLater(
									() -> resetMenueForLoadingFiles("Branches:", orgBranches, loadBranchesEvent));

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
						@SuppressWarnings("restriction")
						public void run() {
							try {
								ScriptingEngine.pull(url, ScriptingEngine.getBranch(url));
							} catch(WrongRepositoryStateException ex) {
								// ignore unsaved files 
								BowlerStudio.runLater(() -> {
									@SuppressWarnings("restriction")
									Alert alert = new Alert(AlertType.CONFIRMATION);
									alert.setTitle("You have Un-Saved work, commit first");
									alert.setHeaderText("You have Un-Saved work, commit first");
									alert.setContentText("You have Un-Saved work, commit first");
								});
							}catch (Exception e) {
								BowlerStudioMenu.checkandDelete(url);
							}
							myEvent.run();
							// selfRef.onRefresh(null);
						}

					}.start();
				});

				MenuItem makeRelease = new MenuItem("Make Release...");
				makeRelease.setOnAction(event -> {
					System.out.println("Releasing " + url);
					BowlerStudio.runLater(() -> {
						Stage s = new Stage();

						MakeReleaseController controller = new MakeReleaseController(url);
						try {
							controller.start(s);
						} catch (Exception e) {
							e.printStackTrace();
						}
						myEvent.run();
						// selfRef.onRefresh(null);
					});
				});

				MenuItem addFile = new MenuItem("Add file to Git Repo...");
				addFile.setOnAction(event -> {
					System.out.println("Adding file to : " + url);
					BowlerStudio.runLater(() -> {
						Stage s = new Stage();

						AddFileToGistController controller = new AddFileToGistController(url, getSelfRef());
						try {
							controller.start(s);
						} catch (Exception e) {
							e.printStackTrace();
						}
						myEvent.run();
						// selfRef.onRefresh(null);
					});
				});
				
				MenuItem delete = new MenuItem("Delete Local Copy...");
				delete.setOnAction(event->{
					checkandDelete(url);
				});
				

				ScriptingEngine.addOnCommitEventListeners(url, myEvent);
				orgRepo.setOnShowing(event -> {
					// On showing the menu, set up the rest of the handlers
					new Thread(myEvent).start();
				});
				BowlerStudio.runLater(() -> {
					if (useAddToWorkspaceItem)
						orgRepo.getItems().add(addToWs);
					orgRepo.getItems().addAll(updateRepo, addFile, makeRelease, orgFiles, orgCommits, orgBranches,delete);
					// BowlerStudio.runLater(() -> {
					repoMenue.getItems().add(orgRepo);
					// });
				});

			}



		};
		if (threaded)
			t.start();
		else
			t.run();
	}
	public static void checkandDelete(String url) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Are you sure you have published all your work?");
		alert.setHeaderText("This will wipe out the local cache for "+url);
		alert.setContentText("All files that are not published will be deleted");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			new Thread(() -> {
				ScriptingEngine.deleteRepo(url);
				BowlerStudioMenuWorkspace.remove(url);
				BowlerStudio.runLater(()->{
					BowlerStudioMenuWorkspace.sort();
				});
			}).start();
		} else {
			System.out.println("Nothing was deleted");
		}
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
				} catch (Exception e1) {
					exp.uncaughtException(Thread.currentThread(), e1);
					return;
				}
				System.out.println("Load Commits event " + url + " on branch " + branchName);
				new Thread(() -> {
					BowlerStudio.runLater(() -> {
						// removing this listener
						// after menue is activated
						// for the first time
						orgCommits.setOnShowing(null);
						gistFlag = false;
					});
					Repository repo = null;
					Git git = null;
					try {
						ScriptingEngine.checkout(url, branchName);

						git = ScriptingEngine.openGit(url);
						repo = git.getRepository();
						// System.out.println("Commits of branch: " + branchName);
						// System.out.println("-------------------------------------");

						ObjectId resolve = repo.resolve(branchName);
						if (resolve != null) {
							Iterable<RevCommit> commits = git.log().add(resolve).call();

							List<RevCommit> commitsList = Lists.newArrayList(commits.iterator());
							BowlerStudio.runLater(() -> {
								try {
									orgCommits.getItems()
											.add(new MenuItem("On Branch " + ScriptingEngine.getBranch(url)));
								} catch (Exception e) {
									exp.uncaughtException(Thread.currentThread(), e);
								}
								orgCommits.getItems().add(new SeparatorMenuItem());
							});
							// RevCommit previous = null;
							for (RevCommit commit : commitsList) {
								String date = format.format(new Date(commit.getCommitTime() * 1000L));
								String fullData = commit.getName() + "\r\n" + commit.getAuthorIdent().getName() + "\r\n"
										+ date + "\r\n" + commit.getFullMessage() + "\r\n"
										+ "---------------------------------------------------\r\n";// +
								// previous==null?"":getDiffOfCommit(previous,commit, repo, git);

								// previous = commit;
								String string = date + " " + commit.getAuthorIdent().getName() + " "
										+ commit.getShortMessage();
								if (string.length() > 80)
									string = string.substring(0, 80);
								// MenuItem tmp = new MenuItem(string);
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
											} catch (Exception e1) {
												branch = "newBranch";
											}

											String dateString = formatSimple
													.format(new Date(commit.getCommitTime() * 1000L));
											promptForNewBranch(branch + "-" + dateString,
													"Creating Branch From Commit:\n\n" + fullData, newBranch -> {
														new Thread() {
															public void run() {
																try {
																	String slugify = slugify(newBranch);
																	System.out.println("Creating " + slugify);
																	ScriptingEngine.setCommitContentsAsCurrent(url,
																			slugify, commit);
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
								BowlerStudio.runLater(() -> {
									orgCommits.getItems().add(tmp);
								});
							}
						}

						BowlerStudio.runLater(() -> {
							orgCommits.hide();
							BowlerStudio.runLater(() -> {
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
					} catch (Throwable e) {
						exp.uncaughtException(Thread.currentThread(), e);
					}
					ScriptingEngine.closeGit(git);
				}).start();
			}
		};
	}

	public static String slugify(String input) {
		String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
		String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
		String slug = NONLATIN.matcher(normalized).replaceAll("").replace('-', '_');

		return slug;
	}

	private static void promptForNewBranch(String exampleName, String reasonForCreating, Consumer<String> resultEvent) {
		BowlerStudio.runLater(() -> {
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
			// EventHandler<Event> thisEvent = this;

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
				} catch (Exception e1) {
					exp.uncaughtException(Thread.currentThread(), e1);
					return;
				}
				;
				new Thread(() -> {
					BowlerStudio.runLater(() -> {
						// removing this listener
						// after menue is activated
						// for the first time
						orgBranches.setOnShowing(null);
						gistFlag = false;
					});
					MenuItem newBranchItem = new MenuItem("New Branch...");
					String newBranchName = "";
					try {
						newBranchName = ScriptingEngine.getBranch(url);
					} catch (Exception e1) {
						exp.uncaughtException(Thread.currentThread(), e1);
						return;
					}
					String newBranchName1 = newBranchName;

					String dateString = formatSimple.format(new Date());

					newBranchItem.setOnAction(event1 -> {
						promptForNewBranch(newBranchName1 + "-" + dateString,
								"Create a new Branch from " + newBranchName1, newBranch -> {
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
					BowlerStudio.runLater(() -> {

						try {
							onBranch.setText("On Branch " + ScriptingEngine.getBranch(url));
							orgBranches.getItems().add(onBranch);
						} catch (Exception e) {
							exp.uncaughtException(Thread.currentThread(), e);

						}
						orgBranches.getItems().add(new SeparatorMenuItem());
						orgBranches.getItems().add(newBranchItem);
						orgBranches.getItems().add(new SeparatorMenuItem());
					});
					if(PasswordManager.hasNetwork())
					try {
						Collection<Ref> branches = ScriptingEngine.getAllBranches(url);
						for (Ref r : branches) {
							createRepoMenuItem(url, orgBranches, onBranch, r, getMenuReset());
						}
					} catch (Throwable e) {
						exp.uncaughtException(Thread.currentThread(), e);
					}
					System.err.println("Refreshing menu Branches");
					BowlerStudio.runLater(() -> {
						orgBranches.hide();
						BowlerStudio.runLater(() -> {
							orgBranches.show();
						});
					});
				}).start();
			}

		};
	}

	private static void createRepoMenuItem(String url, Menu orgBranches, final MenuItem onBranch, Ref r,
			Runnable menureset) {
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

					} catch (Exception e) {
						exp.uncaughtException(Thread.currentThread(), e);
					}

				}

				private void switchToThisNewBranch(String url, final MenuItem onBranch, Ref select, String myName)
						throws Exception {
					String was = ScriptingEngine.getBranch(url);
					try {
					ScriptingEngine.checkout(url, select);
					}catch(org.eclipse.jgit.api.errors.CheckoutConflictException ex) {
						BowlerStudio.runLater(()->{
							Alert alert = new Alert(AlertType.ERROR);// line 1
							alert.setTitle("CheckoutConflictException");// line 2
							alert.setHeaderText("This repo is in an a dirty state");// line 3
							alert.setContentText("Please commit your changes before switching.\nAlternatly you can revert your changes.\nRepository must not have uncommitted changes before changing branches.");// line 4
							alert.showAndWait(); // line 5
						});
						return;
					}
					String s = ScriptingEngine.getBranch(url);
					if (myName.contentEquals(s))
						System.out.println("Changing from " + was + " to " + myName + " is now " + s + "... Success!");
					onBranch.setText("On Branch " + s);
				}
			}.start();

		});
		BowlerStudio.runLater(() -> {
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

	private static void resetMenueForLoadingFiles(String string, Menu orgFiles, EventHandler<Event> loadFiles) {
		BowlerStudio.runLater(() -> {
			try {
				BowlerStudio.runLater(() -> {
					orgFiles.getItems().clear();
					// orgFiles.hide();
					BowlerStudio.runLater(() -> {
						orgFiles.getItems().add(new MenuItem(string));
						orgFiles.getItems().add(new SeparatorMenuItem());
						orgFiles.setOnShowing(loadFiles);
						// BowlerStudio.runLater(() ->orgFiles.show());
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
						BowlerStudio.runLater(() -> {
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
							BowlerStudio.runLater(() -> {
								orgFiles.getItems().add(tmp);
							});

						}
						System.out.println("Refreshing menu");
						BowlerStudio.runLater(() -> {
							orgFiles.hide();
							BowlerStudio.runLater(() -> {
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

		ConnectionManager.addConnection(new VirtualGenericPIDDevice(10000,"virtual"), "virtual");
	}

	@FXML
	public void onClose(ActionEvent e) {
		BowlerStudio.closeBowlerStudio();
	}

	@FXML
	public void onConnectCHDKCamera(ActionEvent event) {
//		BowlerStudio.runLater(() -> {
//			try {
//				ConnectionManager.addConnection(new CHDKImageProvider(), "cameraCHDK");
//			} catch (Exception e) {
//				exp.uncaughtException(Thread.currentThread(), e);
//			}
//		});
	}

	@FXML
	public void onConnectCVCamera(ActionEvent event) {

		// BowlerStudio.runLater(() -> ConnectionManager.onConnectCVCamera());

	}

	@FXML
	public void onConnectFileSourceCamera(ActionEvent event) {
		BowlerStudio.runLater(() -> ConnectionManager.onConnectFileSourceCamera());

	}

	@FXML
	public void onConnectURLSourceCamera(ActionEvent event) {

		BowlerStudio.runLater(() -> ConnectionManager.onConnectURLSourceCamera());

	}

	@FXML
	public void onConnectHokuyoURG(ActionEvent event) {
		BowlerStudio.runLater(() -> ConnectionManager.onConnectHokuyoURG());

	}

	@FXML
	public void onConnectGamePad(ActionEvent event) {
		BowlerStudio.runLater(() -> ConnectionManager.onConnectGamePad());

	}

	@FXML
	public void onLogin(ActionEvent event) {
		//new Exception().printStackTrace();
		new Thread() {
			public void run() {
				PasswordManager.checkInternet();
				setName("Login Gist Thread");

				try {
					ScriptingEngine.logout();
					while (!ScriptingEngine.isLoginSuccess() && !PasswordManager.isAnonMode()) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							exp.uncaughtException(Thread.currentThread(), e);
						}
						ScriptingEngine.login();
					}
				} catch (IOException e) {
					exp.uncaughtException(Thread.currentThread(), e);
				}
			}
		}.start();

	}

	@FXML
	public void onLogout(ActionEvent event) {
		new Thread(()->{
			try {
				ScriptingEngine.logout();
			} catch (IOException e) {
				exp.uncaughtException(Thread.currentThread(), e);
			}
		}).start();
	}

	@FXML
	public void onConnectPidSim(ActionEvent event) {
		LinearPhysicsEngine eng = new LinearPhysicsEngine();
		eng.connect();
		ConnectionManager.addConnection(eng, "engine");
	}

	@FXML
	public void onPrint(ActionEvent event) {

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
				BowlerStudio.runLater(() -> {
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
			AddFileToGistController controller = new AddFileToGistController(null, getSelfRef());

			try {
				controller.start(s);
				setToLoggedIn(username);
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
		BowlerStudio.runLater(() -> {
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

	public void addVitaminType(String s) {
		getTypeMenu(s);
		ArrayList<String> sizes = Vitamins.listVitaminSizes(s);
		for (String size : sizes) {
			addSizesToMenu(size, s);
		}

	}

	public Menu getTypeMenu(String type) {
		if (vitaminTypeMenus.get(type) == null) {
			Menu typeMenu = new Menu(type);
			typeMenu.setMnemonicParsing(false);
			vitaminTypeMenus.put(type, typeMenu);
			BowlerStudio.runLater(() -> {
				vitaminsMenu.getItems().add(typeMenu);
			});
			setUpSizes(typeMenu, type);
		}
		return vitaminTypeMenus.get(type);
	}

	private void setUpSizes(Menu typeMenu, String type) {

		MenuItem editScript = new MenuItem("Edit " + type + " Cad Generator...");
		editScript.setMnemonicParsing(false);
		editScript.setOnAction(event -> {
			new Thread(() -> BowlerStudio.createFileTab(Vitamins.getScriptFile(type))).start();
		});

		BowlerStudio.runLater(() -> {
			typeMenu.getItems().add(new MenuItem("Sizes:"));
			typeMenu.getItems().add(new SeparatorMenuItem());
			typeMenu.getItems().add(editScript);
			typeMenu.getItems().add(new SeparatorMenuItem());
		});
	}

	public void addSizesToMenu(String size, String type) {
		MenuItem sizeMenu = new MenuItem(size);
		sizeMenu.setMnemonicParsing(false);
		BowlerStudio.runLater(() -> {
			getTypeMenu(type).getItems().add(sizeMenu);
		});
		sizeMenu.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new Thread() {
					public void run() {
						LocalFileScriptTab tab = LocalFileScriptTab.getSelectedTab();
						if (tab != null)
							tab.insertString("CSG vitamin_" + slugify(type) + "_" + slugify(size) + " = Vitamins.get(\""
									+ type + "\", \"" + size + "\")\n");
					}
				}.start();
			}
		});
	}

	@FXML
	void onRefresh(ActionEvent event) {
		String current = this.username;// =null;
		this.username = null;
		if (PasswordManager.loggedIn())
			setToLoggedIn(current);

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
	void onLoadGit(ActionEvent event) {
		try {

			// create a text input dialog
			BowlerStudio.runLater(() -> {
				TextInputDialog td = new TextInputDialog();
				td.setHeaderText("Enter Git URL");
				td.setResizable(true);
				td.showAndWait();

				// set the text of the label
				String s = td.getEditor().getText();
				if (s == null || s.length() < 4) {
					System.out.println("Cancle detected");
					return;
				}
				if (s.endsWith(".git")) {
					System.out.println("Loading file from git " + s);
					new Thread(() -> {
						try {
							ArrayList<String> f = ScriptingEngine.filesInGit(s);
							if (f.size() > 0) {
								System.out.println("Valid URL Detected");
								BowlerStudioMenuWorkspace.add(s);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}).start();
				} else {
					System.out.println("Invalid entry " + s);
					onLoadGit(event);
				}

			});
		} catch (Exception e) {
			new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);

		}
	}

	@FXML
	void onBowlerStudioHelp(ActionEvent event) {
		new Thread(() -> {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(
							new URI("https://hackaday.io/project/6423-bowlerstudio-a-robotics-development-platform"));
				} catch (IOException e) {
					new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);

				} catch (URISyntaxException e) {
					new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);

				}
			}
		}).start();
	}

	@FXML // This method is called by the FXMLLoader when initialization is
	// complete
	void initialize() {
		assert CreaturesMenu != null
				: "fx:id=\"CreaturesMenu\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert GitHubRoot != null
				: "fx:id=\"GitHubRoot\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert getMeneBarBowlerStudio() != null
				: "fx:id=\"MeneBarBowlerStudio\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert addMarlinGCODEDevice != null
				: "fx:id=\"addMarlinGCODEDevice\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert clearCache != null
				: "fx:id=\"clearCache\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert createNewGist != null
				: "fx:id=\"createNewGist\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert logoutGithub != null
				: "fx:id=\"logoutGithub\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert myGists != null : "fx:id=\"myGists\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert myOrganizations != null
				: "fx:id=\"myOrganizations\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert myRepos != null : "fx:id=\"myRepos\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert watchingRepos != null
				: "fx:id=\"watchingRepos\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert workspacemenuHandle != null
				: "fx:id=\"workspacemenuHandle\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert vitaminsMenu != null
				: "fx:id=\"vitaminsMenu\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		assert addNewVitamin != null
				: "fx:id=\"addNewVitamin\" was not injected: check your FXML file 'BowlerStudioMenuBar.fxml'.";
		GitHubRoot.setGraphic(AssetFactory.loadIcon("githubLogo"));

		setSelfRef(this);
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
					MenuItem newCreatureWiz = new MenuItem("New Creature...");
					newCreatureWiz.setOnAction(event -> {
						NewCreatureWizard.run();
					});
					BowlerStudio.runLater(() -> {
						CreaturesMenu.getItems().add(new SeparatorMenuItem());
						CreaturesMenu.getItems().add(newCreatureWiz);
						CreaturesMenu.getItems().add(new SeparatorMenuItem());
					});
					@SuppressWarnings("unchecked")
					HashMap<String, HashMap<String, Object>> map = (HashMap<String, HashMap<String, Object>>) ScriptingEngine
							.inlineFileScriptRun(f, null);
					
					List<String> entrySet = asSortedList(map.keySet());
					
					for (String entry : entrySet) {
						HashMap<String, Object> script = map.get(entry);
						MenuItem item = new MenuItem(entry);
						item.setOnAction(event -> {
							String id = (String) script.get("scriptGit");
							String file = (String) script.get("scriptFile");

							loadMobilebaseFromGit(id, file);
						});
						BowlerStudio.runLater(() -> {
							CreaturesMenu.getItems().add(item);
						});
					}
				} catch (Exception e) {
					exp.uncaughtException(Thread.currentThread(), e);
				}
			}
		}.start();

		addMarlinGCODEDevice.setOnAction(event -> {
			BowlerStudio.runLater(() -> ConnectionManager.onMarlinGCODE());
		});
		loadFirmata.setOnAction(event -> {
			BowlerStudio.runLater(() -> ConnectionManager.onFirmata());
		});
		ScriptingEngine.addIGithubLoginListener(new IGithubLoginListener() {
			@Override
			public void onLogout(String arg0) {
				setToLoggedOut();
			}
			@Override
			public void onLogin(String arg0) {
				setToLoggedIn(arg0);
			}
		});

		if (PasswordManager.getUsername() != null) {
			setToLoggedIn(PasswordManager.getUsername());
		} else {
			setToLoggedOut();
		}

// WindowMenu
		int[] fonts =FontSizeManager.getFontOptions();
		Menu fontSelect = new Menu("Font Size");
		ToggleGroup toggleGroup = new ToggleGroup();
		int defSize =FontSizeManager.getDefaultSize();
		for (int i = 0; i < fonts.length; i++) {
			int myFoneNum = fonts[i];
			RadioMenuItem ftmp = new RadioMenuItem(myFoneNum + " pt");

			if (defSize == myFoneNum) {
				ftmp.setSelected(true);
			} else
				ftmp.setSelected(false);
			EventHandler<ActionEvent> eventHandler = (event) -> {
				if (ftmp.isSelected()) {
					FontSizeManager.setFontSize(myFoneNum);
				}
			};
			ftmp.setOnAction(eventHandler);
			ftmp.setToggleGroup(toggleGroup);
			fontSelect.getItems().add(ftmp);
			FontSizeManager.addListener(nf->{
				ftmp.setOnAction(null);
				if(myFoneNum==nf) {
					ftmp.setSelected(true);
				}else
					ftmp.setSelected(false);
				ftmp.setOnAction(eventHandler);
			});
		}
		WindowMenu.getItems().add(fontSelect);
		CheckMenuItem  autohighlight = new CheckMenuItem("Auto Highlight 3d Items");
		autohighlight.setSelected(true);
		CheckMenuItem  idlespin = new CheckMenuItem("Idle Spin ");
		idlespin.setSelected(false);
		CheckMenuItem  showRuler = new CheckMenuItem("Show Ruler ");
		showRuler.setSelected(true);
		CheckMenuItem  showCSGProgress = new CheckMenuItem("Show CSG Update");
		CSG.setProgressMoniter((currentIndex, finalIndex, type, intermediateShape) -> {
			String x = intermediateShape.getName()+" "+type.trim()+"ing "+(currentIndex+1)+" of "+finalIndex;
			if(showCSGProgress.isSelected())
				System.out.println(x);
			else
				System.err.println(x);
		});
		showCSGProgress.setOnAction(event->{
			ConfigurationDatabase.setObject("MenueSettings", "printCSG", showCSGProgress.isSelected());
		});
		eu.mihosoft.vrl.v3d.svg.SVGLoad.getProgressDefault();
//		eu.mihosoft.vrl.v3d.svg.SVGLoad.setProgressDefault(new ISVGLoadProgress() {
//			@Override
//			public void onShape(CSG newShape) {
//				BowlerStudioController.addCsg(newShape);
//			}
//		});
		Runnable r= ()->{
		showCSGProgress.setSelected(Boolean.parseBoolean( ConfigurationDatabase.getObject("MenueSettings", "printCSG", true).toString()));
		};
		new Thread(r).start();
		
		CreatureLab3dController.getEngine().setControls(showRuler,idlespin,autohighlight);
		WindowMenu.getItems().addAll(showRuler,idlespin,autohighlight,showCSGProgress);

		new Thread() {
			public void run() {
				setUncaughtExceptionHandler(new IssueReportingExceptionHandler());
				try {
					if (vitaminsMenu == null)
						throw new RuntimeException("Vitamins menu was not inserted");
					if (vitaminsMenu.getItems() == null)
						throw new RuntimeException("Vitamins menu items are null");
					BowlerStudio.runLater(() -> {
						vitaminsMenu.getItems().add(new SeparatorMenuItem());
					});
					List<String> types = Vitamins.listVitaminTypes().stream().sorted().collect(Collectors.toList());
					for (String s : types) {
						addVitaminType(s);
						Vitamins.getVitaminFile(s, () -> {
							BowlerStudio.runLater(() -> {
								getTypeMenu(s).getItems().clear();
							});
							setUpSizes(getTypeMenu(s), s);
							addVitaminType(s);
						}, false);
					}
				} catch (Exception e) {
					exp.uncaughtException(Thread.currentThread(), e);
				}
				if(!PasswordManager.hasNetwork()) {
					new Thread(()->{
						openFilesInUI();
					}).start();
				}
			}
		}.start();
		

	}
	public static
	<T extends Comparable<? super T>> List<T> asSortedList(Set<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}

	public static BowlerStudioMenu getSelfRef() {
		return selfRef;
	}

	public static void setSelfRef(BowlerStudioMenu selfRef) {
		BowlerStudioMenu.selfRef = selfRef;
	}
}
