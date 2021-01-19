package com.neuronrobotics.bowlerstudio;
/**
 * Sample Skeleton for 'newVitaminWizard.fxml' Controller Class
 */

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.kohsuke.github.GHCreateRepositoryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;

import eu.mihosoft.vrl.v3d.JavaFXInitializer;
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
	private static String typeOfVitaminString = null;

	private static String sizeOfVitaminString = null;

	@FXML
    void onConfirmAndCreate(ActionEvent event) {
		sizePane.setDisable(true);
        measurmentPane.setDisable(true);
        typePane.setDisable(true);
		new Thread(() -> {
			try {
				
				if(newTypeRadio.isSelected()) {
					if(isShaft.isSelected())
						Vitamins.setIsShaft(typeOfVitaminString);
					if(isMotor.isSelected())
						Vitamins.setIsActuator(typeOfVitaminString);
					GitHub github = PasswordManager.getGithub();
					
					String newName =typeOfVitaminString+"CadGenerator";
					GHRepository gist = ScriptingEngine.makeNewRepo(newName,newName + " Generates CAD vitamins ");
					
					String gitURL = gist.getHtmlUrl().toExternalForm()+".git";
					String filename = typeOfVitaminString+".groovy";
					Vitamins.setScript(typeOfVitaminString, gitURL, filename);
					
					String measurments ="";
					for(String key:Vitamins.getConfiguration( typeOfVitaminString,sizeOfVitaminString).keySet().stream().sorted().collect(Collectors.toList())) {
						measurments+="\n	def "+key+"Value = measurments."+key;
					}
					measurments+="\n\tfor(String key:measurments.keySet().stream().sorted().collect(Collectors.toList())){";
					measurments+="\n\t\tprintln \""+ typeOfVitaminString+" value \"+key+\" \"+measurments.get(key);\n}";
//					for(String key:Vitamins.getConfiguration( typeOfVitaminString,sizeOfVitaminString).keySet().stream().sorted().collect(Collectors.toList())) {
//						String string = key+"Value";
//						measurments+="\n	println \"Measurment "+string+" =  \"+"+string;
//					}
					String loader = "import eu.mihosoft.vrl.v3d.parametrics.*;\n" + 
							"import java.util.stream.Collectors;\n" + 
							"import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;\n" + 
							"import eu.mihosoft.vrl.v3d.CSG;\n" + 
							"import eu.mihosoft.vrl.v3d.Cube;\n" + 
							"CSG generate(){\n" + 
							"	String type= \""+typeOfVitaminString+"\"\n" + 
							"	if(args==null)\n" + 
							"		args=[\""+sizeOfVitaminString+"\"]\n" + 
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
					new Thread(() -> BowlerStudio.createFileTab(Vitamins.getScriptFile(typeOfVitaminString))).start();
				}
				
				Vitamins.saveDatabaseForkIfMissing(typeOfVitaminString);
				
				if(newTypeRadio.isSelected()) {
					callback.addVitaminType(typeOfVitaminString);
				}else
					if(!editExisting.isSelected())
						callback.addSizesToMenu(sizeOfVitaminString, typeOfVitaminString);
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
				ArrayList<String> sizes = Vitamins.listVitaminSizes(typeOfVitaminString);
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
				HashMap<String, Object> configsOld = Vitamins.getConfiguration(typeOfVitaminString, sizeComboBox.getSelectionModel().getSelectedItem());

				for(String key:configsOld.keySet().stream().sorted().collect(Collectors.toList())) {
					setupKeyValueToTable(key, configsOld.get(key),sizeOfVitaminString);
				}
			}).start();

		}
		if(newTypeRadio.isSelected()) {
			Vitamins.getConfiguration(typeOfVitaminString, sizeOfVitaminString);
		}
		
		if(Vitamins.isActuator(typeOfVitaminString) ||
				(newTypeRadio.isSelected() && isMotor.isSelected())
				) {
			new Thread(() -> {
				HashMap<String, Object> required = new HashMap<String, Object>();
				required.put("MaxTorqueNewtonmeters", 0.001);
				required.put("MaxFreeSpeedRadPerSec", 1);
				required.put("massKg", 0.001);
				required.put("shaftType", "dShaft");
				required.put("shaftSize", "5mm");
				setRequiredFields(required);
			}).start();
		}
		new Thread(() -> {
			HashMap<String, Object> required = new HashMap<String, Object>();
			required.put("massKg", 0.001);
			required.put("source", "https://commonwealthrobotics.com");
			required.put("price", 0.01);
			required.put("massCentroidX", 0.0);
			required.put("massCentroidY", 0.0);
			required.put("massCentroidZ", 0.0);
			setRequiredFields(required);
		}).start();
    	sizePane.setDisable(true);
        measurmentPane.setDisable(false);
        typePane.setDisable(true);
    }

	private void setRequiredFields(HashMap<String, Object> required) {
		// For each vitamin size in a given type
		
		for(String size:Vitamins.listVitaminSizes(typeOfVitaminString)) {
			HashMap<String, Object> configs = Vitamins.getConfiguration(typeOfVitaminString, size);
			// For every required key
			for(String key:required.keySet().stream().sorted().collect(Collectors.toList())) {
				// check to see if the current size has this key already
				if(!configs.containsKey(key)) {
					// enter a default value, but ensure that the key exists for downstream code
					setupKeyValueToTable(key, required.get(key),size);
				}
			}
		}
	}

	private void setupKeyValueToTable(String key, Object value, String size) {
		Vitamins.getConfiguration(typeOfVitaminString, size).put(key, value);
		if (size.contentEquals(sizeOfVitaminString))
			Platform.runLater(() -> measurmentsTable.getItems()
					.add(new MeasurmentConfig(key, Vitamins.getConfiguration(typeOfVitaminString, sizeOfVitaminString))));
	}

    @FXML
    void onConfirmType(ActionEvent event) {

    	if(newTypeRadio.isSelected()) {
    		typeOfVitaminString=newTypeNameField.getText();
    		if(typeOfVitaminString.length()<2) {
    			Platform.runLater(() -> {
    				Alert alert = new Alert(AlertType.WARNING);
    				alert.setTitle("No name specified!");
    				alert.setHeaderText("Names must be at least 2 charrectors long");
    				alert.setContentText("Try again...");
    				alert.showAndWait();
    			});
    			return;
    		}
    		String slug = BowlerStudioMenu.slugify(typeOfVitaminString);
    		if(!typeOfVitaminString.contentEquals(slug)) {
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
    		typeOfVitaminString=slug;
    		sizeComboBox.setDisable(true);
    		editExisting.setDisable(true);
    		saveAndFork();
    	}else {
    		typeOfVitaminString=typeComboBox.getSelectionModel().getSelectedItem();
    		saveAndFork();
    		ArrayList<String> sizes = Vitamins.listVitaminSizes(typeOfVitaminString);
			boolean hasSize=false;

    		for(String size:sizes) {
    			sizeComboBox.getItems().add(size);
    			if(typeOfVitaminString!=null)
    				if(size.contentEquals(typeOfVitaminString)) {
    					hasSize=true;
    				}
    		}
    		if(sizes.size()>0) {
    			if(hasSize) {
    				sizeComboBox.getSelectionModel().select(typeOfVitaminString);
    			}else {
    				sizeComboBox.getSelectionModel().select(sizes.get(0));
    			}
    		}else {
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
				Vitamins.saveDatabaseForkIfMissing(typeOfVitaminString);
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
		TextInputDialog dialog = new TextInputDialog("lengthOfThing");
		dialog.setTitle("Add new measurment to "+typeOfVitaminString);
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
				setupKeyValueToTable(name,name2,sizeOfVitaminString);
				for(String size:Vitamins.listVitaminSizes(typeOfVitaminString)) {
					Vitamins.getConfiguration(typeOfVitaminString, size).put(name,name2);
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
		if(typeOfVitaminString==null)
			typeComboBox.getSelectionModel().select(types.get(0));
		else
			typeComboBox.getSelectionModel().select(typeOfVitaminString);
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
		FXMLLoader loader = AssetFactory.loadLayout("layout/newVitaminWizard.fxml",true);
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
