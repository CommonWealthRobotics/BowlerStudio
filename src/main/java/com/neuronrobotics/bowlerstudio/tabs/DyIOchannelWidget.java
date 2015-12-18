package com.neuronrobotics.bowlerstudio.tabs;

import java.io.PrintWriter;
import java.io.StringWriter;






import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;






import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;






import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.scripting.ShellType;
import com.neuronrobotics.bowlerstudio.utils.BowlerStudioResourceFactory;
import com.neuronrobotics.nrconsole.plugin.DyIO.DyIOConsole;
import com.neuronrobotics.sdk.dyio.DyIOChannel;
import com.neuronrobotics.sdk.dyio.DyIOChannelEvent;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.IChannelEventListener;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;




import com.neuronrobotics.sdk.util.ThreadUtil;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DyIOchannelWidget {
	
	private boolean fireValue=false;
	private int latestValue=0;
	private boolean isVisable=false;
	private final class ChangeListenerImplementation implements
			ChangeListener<Number> {
		public void changed(ObservableValue<? extends Number> ov,
				Number old_val, Number new_val) {
			int newVal = new_val.intValue();
			chanValue.setText(new Integer(newVal).toString());
			if(currentMode==DyIOChannelMode.SERVO_OUT && timeSlider.getValue()>.1){
				//servo should only set on release when time is defined
				return;
			}
			setLatestValue(newVal);
			setFireValue(true);
		}
	}

	@FXML Button setListenerButton;
	@FXML AnchorPane listenerCodeBox;
	@FXML LineChart<Integer,Integer> channelGraph;
	@FXML Slider timeSlider;
	@FXML Slider positionSlider;
	@FXML Label chanValue;
	@FXML Label deviceType;
	@FXML Label deviceNumber;
	@FXML ImageView deviceModeIcon;
	private DyIOChannel channel;
	private  XYChart.Series<Integer, Integer> series = new XYChart.Series<Integer, Integer>();
	private long startTime=0;
	@FXML Label secondsLabel;
	private IChannelEventListener myLocalListener=null;
	private RSyntaxTextArea textArea;
	private SwingNode sn;
	private RTextScrollPane sp;
	private ServoChannel srv= null;
	private DyIOChannelMode currentMode;
	private ChangeListenerImplementation imp = new ChangeListenerImplementation();
	@FXML NumberAxis graphValueAxis;
	@FXML NumberAxis graphTimeAxis;
	private Integer value=null;
	
	public void setChannel(DyIOChannel c){

			this.channel = c;
			startTime=System.currentTimeMillis();
			setMode( channel.getMode());
			Platform.runLater(()->deviceNumber.setText(new Integer(channel.getChannelNumber()).toString()));
			Platform.runLater(()->chanValue.setText(new Integer(channel.getValue()).toString()));
			Platform.runLater(()->secondsLabel.setText(String.format("%.2f", 0.0)));
			Platform.runLater(()->positionSlider.setValue(channel.getValue()));
			Platform.runLater(()->graphValueAxis.setAnimated(false));
			Platform.runLater(()->graphTimeAxis.setAnimated(false));
			Platform.runLater(()->positionSlider.valueProperty().addListener(imp));
			
			positionSlider.valueChangingProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
	
				chanValue.setText(new Integer((int) positionSlider.getValue()).toString());
				if(currentMode==DyIOChannelMode.SERVO_OUT && timeSlider.getValue()>.1){
					new Thread(){
						public void run(){
							setName("Setting servo Pos");
							srv.SetPosition((int) positionSlider.getValue(), timeSlider.getValue());
						}
					}.start();
					
				}
				
			});
			timeSlider.valueProperty().addListener(new ChangeListener<Number>() {
				public void changed(ObservableValue<? extends Number> ov,
						Number old_val, Number new_val) {
					secondsLabel.setText(String.format("%.2f", new_val));
				}
			});
			channel.addChannelModeChangeListener(newMode -> {
				setMode( newMode);
			});
			
			
			channel.addChannelEventListener(new IChannelEventListener() {
				

				@Override
				public void onChannelEvent(DyIOChannelEvent dyioEvent) {
					if(isVisable()){	
						setValue(new Integer(dyioEvent.getValue()));
					}
					
				}
			});
			Platform.runLater(()->{
				channelGraph.getData().clear();
				channelGraph.getData().add(series);
				
			});
	
			setUpListenerPanel();
			Platform.runLater(()->setListenerButton.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY))));
		
	}
	
	public void updateValue(){
		
		if(getValue()!=null){
			int current = getValue();
			Data<Integer, Integer> newChart = new XYChart.Data<Integer, Integer>(
	        		(int) (startTime-System.currentTimeMillis()),
	        		current);
			Platform.runLater(()->chanValue.setText(new Integer(current).toString()));
				if(!positionSlider.isValueChanging()){// only updae the slider position if the user is not sliding it
					positionSlider.valueProperty().removeListener(imp);
					Platform.runLater(()->{
						positionSlider.setValue(current);
						setLatestValue(current);
						setFireValue(false);
						positionSlider.valueProperty().addListener(imp);
					});
				}
				Platform.runLater(()->{
					if(series.getData().size()>10){// if you keep many more points in the graph it will lag the rendering realy badly
						series.getData().remove(0);
					}
					
					Platform.runLater(()->{
				        series.getData().add(newChart);
				        value=null;
			        });
		        });
		}
	}
	
	private void setMode(DyIOChannelMode newMode){
		currentMode = newMode;
		Platform.runLater(()->{

			deviceModeIcon.setImage(BowlerStudioResourceFactory.getModeImage(newMode));	
			series.setName(currentMode.toSlug()+" values");
			deviceType.setText(currentMode.toSlug());
			//set slider bounds
			graphValueAxis.setAutoRanging(false);
			switch(currentMode){
			case ANALOG_IN:
				positionSlider.setMin(0);
				positionSlider.setMax(1024);
				graphValueAxis.setLowerBound(0);
				graphValueAxis.setUpperBound(1024);

				break;
			case COUNT_IN_DIR:
			case COUNT_IN_HOME:
			case COUNT_IN_INT:
			case COUNT_OUT_DIR:
			case COUNT_OUT_HOME:
			case COUNT_OUT_INT:
				positionSlider.setMin(-5000);
				positionSlider.setMax(5000);
				graphValueAxis.setLowerBound(-5000);
				graphValueAxis.setUpperBound(5000);
				break;
			case DC_MOTOR_DIR:			
			case DC_MOTOR_VEL:
			case PWM_OUT:
			case SERVO_OUT:
				positionSlider.setMin(0);
				positionSlider.setMax(255);
				graphValueAxis.setLowerBound(0);
				graphValueAxis.setUpperBound(255);
				break;
			case DIGITAL_IN:
			case DIGITAL_OUT:
				positionSlider.setMin(0);
				positionSlider.setMax(1);
				graphValueAxis.setLowerBound(0);
				graphValueAxis.setUpperBound(1);
				break;
			default:
				break;
			
			}
			// allow slider to be disabled for inputs
			switch(currentMode){
			case ANALOG_IN:
			case COUNT_IN_DIR:
			case COUNT_IN_HOME:
			case COUNT_IN_INT:
			case DIGITAL_IN:
				timeSlider.setDisable(true);
				positionSlider.setDisable(true);
				break;
	
			default:
				timeSlider.setDisable(false);
				positionSlider.setDisable(false);
				break;
			
			}
			setLatestValue(channel.getValue());
			if(currentMode==DyIOChannelMode.SERVO_OUT && srv==null){
				new Thread(){
					public void run(){
						setName("Connectiong servo object");
						srv = new ServoChannel(channel);	
					}
				}.start();
			}
		});
		
	}

	@FXML public void onListenerButtonClicked(ActionEvent event) {
		new Thread(){
			public void run(){
				setName("compiling listener");
				try{
					if(myLocalListener==null){
						Platform.runLater(()->{
							sn.setDisable(true);
							textArea.setEditable(false);
						});
						myLocalListener=(IChannelEventListener) ScriptingEngine.inlineScriptStringRun(textArea.getText(), null, ShellType.GROOVY);
						channel.addChannelEventListener(myLocalListener);
						Platform.runLater(()->{
							setListenerButton.setText("Kill Listener");
							setListenerButton.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
						});
					}else{
						channel.removeChannelEventListener(myLocalListener);
						Platform.runLater(()->{
							sn.setDisable(false);
							textArea.setEditable(true);
							myLocalListener=null;
							setListenerButton.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
							setListenerButton.setText("Set Listener");
						});
					}
				}catch(Exception e){
					  StringWriter sw = new StringWriter();
				      PrintWriter pw = new PrintWriter(sw);
				      e.printStackTrace(pw);
				      System.out.println(sw.toString());
				}
			}
		}.start();

		
	}
	
	private void setUpListenerPanel(){
		//Platform.runLater(()->{
			textArea = new RSyntaxTextArea(15, 80);
			textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
			textArea.setCodeFoldingEnabled(true);
			textArea.setText("return new IChannelEventListener() { \n"+
				"\tpublic \n"
				+ "\tvoid onChannelEvent(DyIOChannelEvent dyioEvent){\n"+
				"\t\tprintln \"From Listener=\"+dyioEvent.getValue();\n"+
				"\t}\n"+
			"}"
				);
			sp = new RTextScrollPane(textArea);
			
			sn = new SwingNode();
			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	            	sn.setContent(sp);
	            }
	        });
			
			listenerCodeBox.getChildren().setAll(sn);
			
			listenerCodeBox.setFocusTraversable(false);
			
			sn.setOnMouseEntered(mouseEvent -> {
				sn.requestFocus();
				SwingUtilities.invokeLater(new Runnable() {
		            @Override
		            public void run() {
		            	textArea.requestFocusInWindow();
		            }
		        });
			});
		//});
	}

	public boolean isVisable() {
		return isVisable;
	}

	public void setVisable(boolean isVisable) {
		Platform.runLater(()->series.getData().clear());
		this.isVisable = isVisable;
	}

	public boolean isFireValue() {
		return fireValue;
	}

	public void setFireValue(boolean fireValue) {
		this.fireValue = fireValue;
	}

	public int getLatestValue() {
		return latestValue;
	}

	public void setLatestValue(int latestValue) {
		this.latestValue = latestValue;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

}
