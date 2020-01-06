package com.neuronrobotics.bowlerstudio;
/**
 * Sample Skeleton for 'newVitaminWizard.fxml' Controller Class
 */

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

import org.kohsuke.github.GHCreateRepositoryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import com.neuronrobotics.sdk.addons.kinematics.JavaFXInitializer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class NewVitaminWizardController  extends Application {
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="typePane"
    private AnchorPane typePane; // Value injected by FXMLLoader

    @FXML // fx:id="x1"
    private Font x1; // Value injected by FXMLLoader

    @FXML // fx:id="x2"
    private Color x2; // Value injected by FXMLLoader

    @FXML // fx:id="existingTypeRadio"
    private RadioButton existingTypeRadio; // Value injected by FXMLLoader

    @FXML // fx:id="newTypeRadio"
    private RadioButton newTypeRadio; // Value injected by FXMLLoader

    @FXML // fx:id="typeComboBox"
    private ComboBox<String> typeComboBox; // Value injected by FXMLLoader

    @FXML // fx:id="sizePane"
    private AnchorPane sizePane; // Value injected by FXMLLoader

    @FXML // fx:id="sizeComboBox"
    private ComboBox<String> sizeComboBox; // Value injected by FXMLLoader

    @FXML // fx:id="measurmentPane"
    private AnchorPane measurmentPane; // Value injected by FXMLLoader

    @FXML // fx:id="measurmentsTable"
    private TableView<MeasurmentConfig> measurmentsTable; // Value injected by FXMLLoader
	@FXML 
	private TextField newTypeNameField;
	@FXML 
	private TextField newSizeField;
    @FXML // fx:id="nameColumn"
    private TableColumn<MeasurmentConfig, String> nameColumn; // Value injected by FXMLLoader

    @FXML // fx:id="measurmentColumn"
    private TableColumn<MeasurmentConfig, String> measurmentColumn; // Value injected by FXMLLoader
    @FXML // fx:id="isMotor"
    private CheckBox isMotor; // Value injected by FXMLLoader

    @FXML // fx:id="isShaft"
    private CheckBox isShaft; // Value injected by FXMLLoader
    @FXML // fx:id="editExisting"
    private CheckBox editExisting; // Value injected by FXMLLoader
    @FXML // fx:id="editExisting"
    private Button newMeasurmentButton;
    
	private static INewVitaminCallback callback=null;

	private static Stage primaryStage;
	private String typeOfVitamin = null;
	//private String sizeOfVitamin=null;

	private String sizeOfVitaminString;

	@FXML
    void onConfirmAndCreate(ActionEvent event) {
		sizePane.setDisable(true);
        measurmentPane.setDisable(true);
        typePane.setDisable(true);
		new Thread(() -> {
			try {
				
				if(newTypeRadio.isSelected()) {
					if(isShaft.isSelected())
						Vitamins.setIsShaft(typeOfVitamin);
					if(isMotor.isSelected())
						Vitamins.setIsActuator(typeOfVitamin);
					GitHub github = PasswordManager.getGithub();
					
					String newName =typeOfVitamin+"CadGenerator";
					GHCreateRepositoryBuilder builder = github.createRepository(newName );
					builder.description(newName + " Generates CAD vitamins " );
					GHRepository gist=null;
					try {
						gist = builder.create();
					}catch(org.kohsuke.github.HttpException ex) {
						if(ex.getMessage().contains("name already exists on this account")) {
							gist = github.getRepository(PasswordManager.getLoginID()+"/"+newName);
						}
					}
					String gitURL = gist.getHtmlUrl().toExternalForm()+".git";
					String filename = typeOfVitamin+".groovy";
					Vitamins.setScript(typeOfVitamin, gitURL, filename);
					String measurments ="";
					for(String key:Vitamins.getConfiguration( typeOfVitamin,sizeOfVitaminString).keySet()) {
						measurments+="\n	def "+key+"Value = measurments."+key;
					}
					for(String key:Vitamins.getConfiguration( typeOfVitamin,sizeOfVitaminString).keySet()) {
						String string = key+"Value";
						measurments+="\n	println \"Loaded from vitamins measurments "+string+":  \"+"+string+"+\" value is = \"+"+string;
					}
					String loader = "import eu.mihosoft.vrl.v3d.parametrics.*;\n" + 
							"CSG generate(){\n" + 
							"	String type= \""+typeOfVitamin+"\"\n" + 
							"	if(args==null)\n" + 
							"		args=[\""+sizeOfVitaminString+" \"]\n" + 
							"	// The variable that stores the current size of this vitamin\n"
							+ "	StringParameter size = new StringParameter(	type+\" Default\"," + 
							"args.get(0)," + 
							"Vitamins.listVitaminSizes(type))\n" + 
							"	HashMap<String,Object> measurments = Vitamins.getConfiguration( type,size.getStrValue())\n" + 
							measurments+"\n"+
							"	// Stub of a CAD object\n"+
							"	CSG part = new Cube().toCSG()\n"+
							"	return part\n" + 
							"		.setParameter(size)\n" + 
							"		.setRegenerate({generate()})\n" + 
							"}\n" + 
							"return generate() ";
					ScriptingEngine.pushCodeToGit(gitURL, ScriptingEngine.getFullBranch(gitURL), filename,
							loader, "new CAD loader script");
					
				}
				
				Vitamins.saveDatabaseForkIfMissing(typeOfVitamin);
				
				if(newTypeRadio.isSelected()) {
					callback.addVitaminType(typeOfVitamin);
				}else
					if(!editExisting.isSelected())
						callback.addSizesToMenu(sizeOfVitaminString, typeOfVitamin);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e1);
			}
			try {
				Platform.runLater(() -> {
					this.primaryStage.close();
					primaryStage=null;
				});
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);
				
			}
		}).start();

    }

    @FXML
    void onConfirmSize(ActionEvent event) {
    	if(!editExisting.isSelected()) {
	    	sizeOfVitaminString = newSizeField.getText();
	    	if(sizeOfVitaminString.length()<2) {
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("No name specified!");
					alert.setHeaderText("Names must be at least 2 charrectors long");
					alert.setContentText("Try again...");
					alert.showAndWait();
				});
				return;
			}
			String slug = BowlerStudioMenu.slugify(sizeOfVitaminString);
			if(!sizeOfVitaminString.contentEquals(slug)) {
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Name Format Wrong");
					alert.setHeaderText("Name must be without spaces or special chars");
					alert.setContentText("Changed to "+slug+" confirm to continue...");
					alert.showAndWait();
					Platform.runLater(() -> newSizeField.setText(slug));
				});
				return;
			}
			sizeOfVitaminString=slug;
    	}else {
    		sizeOfVitaminString=sizeComboBox.getSelectionModel().getSelectedItem();
    	}
    		
		Platform.runLater(() ->nameColumn.setCellValueFactory(new PropertyValueFactory<MeasurmentConfig, String>("key")));
		Platform.runLater(() ->measurmentColumn.setCellValueFactory(new PropertyValueFactory<MeasurmentConfig, String>("measurment")));
		Platform.runLater(() ->
			measurmentColumn
			.setCellFactory(
	                TextFieldTableCell.forTableColumn())
				);
		
		Platform.runLater(() ->measurmentsTable.getSelectionModel().cellSelectionEnabledProperty().set(true));
		measurmentsTable.setEditable(true);
		nameColumn.setEditable(false);
		measurmentColumn.setEditable(true);
		measurmentColumn.setOnEditCommit(ev -> {
			final String value = ev.getNewValue() != null ? (String) ev.getNewValue() : (String) ev.getOldValue();
			((MeasurmentConfig) ev.getTableView().getItems().get(ev.getTablePosition().getRow()))
					.setMeasurment(value);
			measurmentsTable.refresh();
	    });
		
		
		if(existingTypeRadio.isSelected()) {
			if(!editExisting.isSelected()) {
				boolean exists = false;
				// for existing types check for existing sizes
				ArrayList<String> sizes = Vitamins.listVitaminSizes(typeOfVitamin);
				for(String size:sizes) {
					if(size.contentEquals(sizeOfVitaminString))
						exists=true;
				}
				if(exists) {
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.WARNING);
						alert.setTitle("Size already Exists");
						alert.setHeaderText("Name must be unique");
						alert.setContentText("Rename and confirm to continue...");
						alert.showAndWait();
					});
					return;
				}
			}
			new Thread(() -> {
				HashMap<String, Object> configsOld = Vitamins.getConfiguration(typeOfVitamin, sizeComboBox.getSelectionModel().getSelectedItem());
				HashMap<String, Object> configs = Vitamins.getConfiguration(typeOfVitamin, sizeOfVitaminString);
				for(String key:configsOld.keySet()) {
					setupKeyValueToTable(key, configsOld.get(key));
				}
			}).start();

		}
		
		if(Vitamins.isActuator(typeOfVitamin)) {
			new Thread(() -> {
				HashMap<String, Object> required = new HashMap<String, Object>();
				required.put("MaxTorqueNewtonmeters", 0.0586);
				required.put("source", "https://commonwealthrobotics.com");
				required.put("MaxFreeSpeedRadPerSec", 46.5);
				required.put("massKg", 0.11);
				required.put("shaftType", "dShaft");
				required.put("shaftSize", "5mm");
				HashMap<String, Object> configs = Vitamins.getConfiguration(typeOfVitamin, sizeOfVitaminString);
				for(String key:required.keySet()) {
					Object value = required.get(key);
					if(!configs.containsKey(key)) {
						setupKeyValueToTable(key, value);
					}
				}
			}).start();
		}
    	sizePane.setDisable(true);
        measurmentPane.setDisable(false);
        typePane.setDisable(true);
    }

	private void setupKeyValueToTable(String key, Object value) {
		Vitamins.getConfiguration(typeOfVitamin, sizeOfVitaminString).put(key, value);
		Platform.runLater(() -> measurmentsTable.getItems()
				.add(new MeasurmentConfig(key, Vitamins.getConfiguration(typeOfVitamin, sizeOfVitaminString))));
	}

    @FXML
    void onConfirmType(ActionEvent event) {

    	if(newTypeRadio.isSelected()) {
    		typeOfVitamin=newTypeNameField.getText();
    		if(typeOfVitamin.length()<2) {
    			Platform.runLater(() -> {
    				Alert alert = new Alert(AlertType.WARNING);
    				alert.setTitle("No name specified!");
    				alert.setHeaderText("Names must be at least 2 charrectors long");
    				alert.setContentText("Try again...");
    				alert.showAndWait();
    			});
    			return;
    		}
    		String slug = BowlerStudioMenu.slugify(typeOfVitamin);
    		if(!typeOfVitamin.contentEquals(slug)) {
    			Platform.runLater(() -> {
    				Alert alert = new Alert(AlertType.WARNING);
    				alert.setTitle("Name Format Wrong");
    				alert.setHeaderText("Name must be without spaces or special chars");
    				alert.setContentText("Changed to "+slug+" confirm to continue...");
    				alert.showAndWait();
    				Platform.runLater(() -> newTypeNameField.setText(slug));
    			});
    			
    			return;
    		}
    		typeOfVitamin=slug;
    		sizeComboBox.setDisable(true);
    		editExisting.setDisable(true);
    		saveAndFork();
    	}else {
    		typeOfVitamin=typeComboBox.getSelectionModel().getSelectedItem();
    		saveAndFork();
    		ArrayList<String> sizes = Vitamins.listVitaminSizes(typeOfVitamin);
    		for(String size:sizes) {
    			sizeComboBox.getItems().add(size);
    		}
    		if(sizes.size()>0)
    			sizeComboBox.getSelectionModel().select(sizes.get(0));
    		else {
    			sizeComboBox.setDisable(true);
        		editExisting.setDisable(true);
        		editExisting.setSelected(false);
        		
    		}
    	}
        measurmentPane.setDisable(true);
        typePane.setDisable(true);
        
    }

	private void saveAndFork() {
		new Thread(() -> {
			try {
				Vitamins.saveDatabaseForkIfMissing(typeOfVitamin);
		    	sizePane.setDisable(false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				new IssueReportingExceptionHandler().uncaughtException(Thread.currentThread(), e);
				
			}
		}).start();

	}

 
	@FXML
    void onNewMeasurment(ActionEvent event) {
		newMeasurmentButton.setDisable(true);
		HashMap<String, Object> configs = Vitamins.getConfiguration(typeOfVitamin, sizeOfVitaminString);
		TextInputDialog dialog = new TextInputDialog("lengthOfThing");
		dialog.setTitle("Add new measurment to "+typeOfVitamin);
		dialog.setHeaderText("This measurment will be added to all instances of the vitamin");
		dialog.setContentText("New measurment name:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		// The Java 8 way to get the response value (with lambda expression).
		result.ifPresent(name -> { 
			TextInputDialog dialog2 = new TextInputDialog("0.0");
			dialog2.setTitle("Set value of "+name);
			dialog2.setHeaderText("This value will be added to all instances of the vitamin");
			dialog2.setContentText(name+" = ");

			// Traditional way to get the response value.
			Optional<String> result2 = dialog2.showAndWait();
			result2.ifPresent(name2 -> { 
				setupKeyValueToTable(name,name2);
				for(String size:Vitamins.listVitaminSizes(typeOfVitamin)) {
					Vitamins.getConfiguration(typeOfVitamin, size).put(name,name2);
				}
			});
			newMeasurmentButton.setDisable(false);
			
		});
		

    }

    @FXML
    void onSelectExistingTypeMode(ActionEvent event) {
    	newTypeNameField.setEditable(false);
    	typeComboBox.setDisable(false);
    	isShaft.setDisable(true);
		isMotor.setDisable(true);
    }

    @FXML
    void onSelectNewTypeMode(ActionEvent event) {
    	newTypeNameField.setEditable(true);
    	typeComboBox.setDisable(true);
    	isShaft.setDisable(false);
		isMotor.setDisable(false);
    }
    @FXML
    void onEditExisting(ActionEvent event) {
    	if(editExisting.isSelected()) {
    		newSizeField.setEditable(false);
    	}else {
    		
    		newSizeField.setEditable(true);
    	}
    }

    @FXML
    void onIsMotor(ActionEvent event) {
    	if(isMotor.isSelected())
    		isShaft.setSelected(false);
    }

    @FXML
    void onIsShaft(ActionEvent event) {
    	
    	if(isShaft.isSelected())
    		isMotor.setSelected(false);
    	
    }
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert x1 != null : "fx:id=\"x1\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert x2 != null : "fx:id=\"x2\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert existingTypeRadio != null : "fx:id=\"existingTypeRadio\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert newTypeRadio != null : "fx:id=\"newTypeRadio\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert typeComboBox != null : "fx:id=\"typeComboBox\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert sizeComboBox != null : "fx:id=\"sizeComboBox\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        assert measurmentsTable != null : "fx:id=\"measurmentsTable\" was not injected: check your FXML file 'newVitaminWizard.fxml'.";
        
        sizePane.setDisable(true);
        measurmentPane.setDisable(true);
        ToggleGroup groupForType= new ToggleGroup();
        existingTypeRadio.setSelected(true);
        newTypeRadio.setSelected(false);
        existingTypeRadio.setToggleGroup(groupForType);
        newTypeRadio.setToggleGroup(groupForType);
        newTypeNameField.setEditable(false);
        ArrayList<String> types = Vitamins.listVitaminTypes();
		for(String s:types) {
			typeComboBox.getItems().add(s);
		}
		typeComboBox.getSelectionModel().select(types.get(0));
		isShaft.setDisable(true);
		isMotor.setDisable(true);
    }
    
    public static void launchWizard(INewVitaminCallback callback) throws Exception {
    	NewVitaminWizardController.callback=callback;
		Platform.runLater(() -> {
			Stage s = new Stage();
			primaryStage = s;
			new Thread(() -> {
				NewVitaminWizardController controller = new NewVitaminWizardController();
				
				try {
					controller.start(s);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		});
    }

    public static void main(String [] args) throws Exception {
    	JavaFXInitializer.go();
    	NewVitaminWizardController.launchWizard(new INewVitaminCallback() {
			
			@Override
			public Menu getTypeMenu(String type) {
				System.out.println("Get Vitamin Menu");

				return new Menu(type);
			}
			
			@Override
			public void addVitaminType(String s) {
				getTypeMenu(s);
				ArrayList<String> sizes = Vitamins.listVitaminSizes(s);
				for(String size:sizes) {
					addSizesToMenu(size,s);
				}
				System.out.println("Add addVitaminType "+s);
			}
			
			@Override
			public void addSizesToMenu(String size, String type) {
				System.out.println("Add addSizesToMenu "+type+" "+size );
			}
		});
    }
    
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = AssetFactory.loadLayout("layout/newVitaminWizard.fxml");
		Parent root;
		//loader.setController(this);
		// This is needed when loading on MAC
		loader.setClassLoader(getClass().getClassLoader());
		root = loader.load();
		Platform.runLater(() -> {
			primaryStage.setTitle("Edit Vitamins Wizard");

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.setResizable(true);
			primaryStage.show();
		});
	}
}
