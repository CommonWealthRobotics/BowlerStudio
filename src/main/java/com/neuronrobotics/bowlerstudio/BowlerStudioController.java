package com.neuronrobotics.bowlerstudio;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.neuronrobotics.bowlerstudio.tabs.LocalFileScriptTabTab;
import com.neuronrobotics.bowlerstudio.tabs.ScriptingGistTab;
import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.nrconsole.util.GroovyFilter;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.dyio.DyIO;

public class BowlerStudioController extends TabPane{

	private static final String HOME_URL = "http://neuronrobotics.github.io/Java-Code-Library/Digital-Input-Example-Simple/";
	/**
	 * 
	 */
	private static final long serialVersionUID = -2686618188618431477L;
	private ConnectionManager connectionManager;


	public BowlerStudioController() {
		createScene();
	}
	
	//Custom function for creation of New Tabs.
	public void createFileTab(File file) {

		try {
			addTab(new LocalFileScriptTabTab( connectionManager,  file),true);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Custom function for creation of New Tabs.
	private void createAndSelectNewTab(final TabPane tabPane, final String title) {


		try {
			addTab(new ScriptingGistTab(title,connectionManager , getHomeUrl(),tabPane), false);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	private Tab createTab() throws IOException, InterruptedException{
		final ScriptingGistTab tab = new ScriptingGistTab(null,connectionManager,   null,null);

		return tab;
	}
	
	public void addTab(Tab tab, boolean closable){
		Platform.runLater(()->{
			final ObservableList<Tab> tabs = getTabs();
			tab.setClosable(closable);
			tabs.add(tabs.size() - 1, tab);
			getSelectionModel().select(tab);
		});
	}


	public void createScene() {

		//BorderPane borderPane = new BorderPane();


		//Placement of TabPane.
		setSide(Side.TOP);

		/* To disable closing of tabs.
		 * tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);*/

		final Tab newtab = new Tab();
		newtab.setText("New Gist");
		newtab.setClosable(false);

		//Addition of New Tab to the tabpane.
		getTabs().addAll(newtab);
		
		connectionManager = new ConnectionManager(this);
		addTab(connectionManager,false);
		
		createAndSelectNewTab(this, "About NrConsole");


		//Function to add and display new tabs with default URL display.
		getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> observable,
					Tab oldSelectedTab, Tab newSelectedTab) {
				if (newSelectedTab == newtab) {

					try {
						addTab(createTab(),true);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	


				}
			}
		});
	}


	public static String getHomeUrl() {
		return HOME_URL;
	}


	public void open() {
		 File last=FileSelectionFactory.GetFile(null, new GroovyFilter());
		 if(last != null){
			 createFileTab(last);
		 }
	}

	public void addConnection() {
		connectionManager.addConnection();
	}

}