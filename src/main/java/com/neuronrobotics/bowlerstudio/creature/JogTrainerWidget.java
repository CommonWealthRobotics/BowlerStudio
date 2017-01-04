package com.neuronrobotics.bowlerstudio.creature;
/**
 * Sample Skeleton for "jogTrainerWidget.fxml" Controller Class
 * You can copy and paste this code into your favorite IDE
 **/

import java.net.URL;
import java.util.ResourceBundle;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.assets.ConfigurationDatabase;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import com.neuronrobotics.sdk.addons.gamepad.IJInputEventListener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.java.games.input.Component;
import net.java.games.input.Event;


public class JogTrainerWidget extends Application implements IJInputEventListener {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="linkMinus"
    private ImageView linkMinus; // Value injected by FXMLLoader

    @FXML // fx:id="linkPlus"
    private ImageView linkPlus; // Value injected by FXMLLoader

    @FXML // fx:id="linkval"
    private TextField linkval; // Value injected by FXMLLoader

    @FXML // fx:id="rotzMinus"
    private ImageView rotzMinus; // Value injected by FXMLLoader

    @FXML // fx:id="rotzPlus"
    private ImageView rotzPlus; // Value injected by FXMLLoader

    @FXML // fx:id="rzval"
    private TextField rzval; // Value injected by FXMLLoader

    @FXML // fx:id="xminus"
    private ImageView xminus; // Value injected by FXMLLoader

    @FXML // fx:id="xplus"
    private ImageView xplus; // Value injected by FXMLLoader

    // WARNING: fx:id="xval" cannot be injected: several objects share the same fx:id;

    @FXML // fx:id="yminus"
    private ImageView yminus; // Value injected by FXMLLoader

    @FXML // fx:id="yplus"
    private ImageView yplus; // Value injected by FXMLLoader
    @FXML // fx:id="yval"
    private TextField xval; // Value injected by FXMLLoader
    
    @FXML // fx:id="yval"
    private TextField yval; // Value injected by FXMLLoader

    @FXML // fx:id="zminus"
    private ImageView zminus; // Value injected by FXMLLoader

    @FXML // fx:id="zplus"
    private ImageView zplus; // Value injected by FXMLLoader

    @FXML // fx:id="zval"
    private TextField zval; // Value injected by FXMLLoader

	private Stage primaryStage;

	private BowlerJInputDevice gameController;

	private String paramsKey;
	private TextField selected=null;

    public JogTrainerWidget(BowlerJInputDevice gameController) {
		this.gameController = gameController;
		paramsKey = gameController.getController().getName();
		gameController.addListeners(this);
	}
	// Handler for Button[Button[id=null, styleClass=button]] onAction
    @FXML
    void configure(ActionEvent event) {
    	primaryStage.close();
    	new Thread(()->{
        	//linkval.setText((String) ConfigurationDatabase.getObject(paramsKey, "jogLink", "x"));
        	ConfigurationDatabase.setObject(paramsKey, "jogLink", linkval.getText());
           // xval.setText((String) ConfigurationDatabase.getObject(paramsKey, "jogKinx", "x"));
        	ConfigurationDatabase.setObject(paramsKey, "jogKinx", xval.getText());
            //yval.setText((String) ConfigurationDatabase.getObject(paramsKey, "jogKiny", "y"));
        	ConfigurationDatabase.setObject(paramsKey, "jogKiny", yval.getText());
            //zval.setText((String) ConfigurationDatabase.getObject(paramsKey, "jogKinz", "rz"));
        	ConfigurationDatabase.setObject(paramsKey, "jogKinz", zval.getText());
            //rzval.setText((String) ConfigurationDatabase.getObject(paramsKey, "jogKinslider", "slider"));
        	ConfigurationDatabase.setObject(paramsKey, "jogKinslider", rzval.getText());
			ConfigurationDatabase.save();

		}).start();

    	gameController.removeListeners(this);
    }
    @FXML
    void selectTraining(MouseEvent event) {
    	if(TextField.class.isInstance(event.getSource()))
    		selected = (TextField) event.getSource();
    }
	@Override
    public void start(Stage primaryStage) throws Exception
    {      
    	this.primaryStage = primaryStage;
		FXMLLoader loader = AssetFactory.loadLayout("layout/jogTrainerWidget.fxml", true);
    	Parent root;
    	loader.setController(this);
		loader.setClassLoader(JogTrainerWidget.class.getClassLoader());
        root = loader.load();
        Platform.runLater(() -> {
            primaryStage.setTitle("Configure the controller");

                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                primaryStage.initModality(Modality.WINDOW_MODAL);
                primaryStage.setResizable(true);
                primaryStage.show();
 
        });
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert linkMinus != null : "fx:id=\"linkMinus\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert linkPlus != null : "fx:id=\"linkPlus\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert linkval != null : "fx:id=\"linkval\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert rotzMinus != null : "fx:id=\"rotzMinus\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert rotzPlus != null : "fx:id=\"rotzPlus\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert rzval != null : "fx:id=\"rzval\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert xminus != null : "fx:id=\"xminus\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert xplus != null : "fx:id=\"xplus\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert yminus != null : "fx:id=\"yminus\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert yplus != null : "fx:id=\"yplus\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert yval != null : "fx:id=\"yval\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert zminus != null : "fx:id=\"zminus\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert zplus != null : "fx:id=\"zplus\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";
        assert zval != null : "fx:id=\"zval\" was not injected: check your FXML file 'jogTrainerWidget.fxml'.";

        // Initialize your logic here: all @FXML variables will have been injected
        try {
			xplus.setImage(AssetFactory.loadAsset("Plus-X.png"));
			xminus.setImage(AssetFactory.loadAsset("Minus-X.png"));
			yplus.setImage(AssetFactory.loadAsset("Plus-Y.png"));
			yminus.setImage(AssetFactory.loadAsset("Minus-Y.png"));
			zplus.setImage(AssetFactory.loadAsset("Plus-Z.png"));
			zminus.setImage(AssetFactory.loadAsset("Minus-Z.png"));
			rotzPlus.setImage(AssetFactory.loadAsset("Rotation-Z.png"));
			rotzMinus.setImage(AssetFactory.loadAsset("Rotation-Neg-Z.png"));
			linkPlus.setImage(AssetFactory.loadAsset("Move-Single-Motor.png"));
			linkMinus.setImage(AssetFactory.loadAsset("Move-Single-Motor.png"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Platform.runLater(()->{
            linkval.setText((String) ConfigurationDatabase.getObject(paramsKey, "jogLink", "x"));
            xval.setText((String) ConfigurationDatabase.getObject(paramsKey, "jogKinx", "x"));
            yval.setText((String) ConfigurationDatabase.getObject(paramsKey, "jogKiny", "y"));
            zval.setText((String) ConfigurationDatabase.getObject(paramsKey, "jogKinz", "rz"));
            rzval.setText((String) ConfigurationDatabase.getObject(paramsKey, "jogKinslider", "slider"));
        });
        

    }
	@Override
	public void onEvent(Component arg0, Event arg1, float value, String arg3) {
		if(Math.abs(value)>0.75 && selected!=null){
			 Platform.runLater(()->selected.setText(arg0.getName()));
		}
		
	}

}

