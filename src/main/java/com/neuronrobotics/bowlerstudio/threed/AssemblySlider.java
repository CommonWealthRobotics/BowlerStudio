package com.neuronrobotics.bowlerstudio.threed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.PropertyStorage;
import eu.mihosoft.vrl.v3d.Transform;
import javafx.scene.control.Slider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.transform.Affine;



public class AssemblySlider {
	public static Slider getSlider(Set<CSG> listOfObjects) {
		int s=0;
		
		for (CSG c : listOfObjects.toArray(new CSG[0])) {
			PropertyStorage incomingGetStorage = c.getStorage();
			if(incomingGetStorage.getValue("MaxAssemblyStep")!=Optional.empty()) {
				Integer max = (Integer) incomingGetStorage.getValue("MaxAssemblyStep").get();
				if(max>s) {
					s=max;
				}				
			}
		}
		int numSteps=s;
		Slider slider = new Slider(0, numSteps, numSteps);
		slider.setShowTickMarks(true);
		slider.setShowTickLabels(true);
		slider.setMajorTickUnit(0.25f);
		slider.setBlockIncrement(0.1f);
		slider.setPrefWidth(300);
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue <?extends Number>observable, Number oldValue, Number newValue){
				int step = (int)(newValue.doubleValue()+1);
				double fraction =-1*(newValue.doubleValue()-step);
				
				for (Iterator<CSG> iterator = listOfObjects.iterator(); iterator.hasNext();) {
					CSG c = iterator.next();
					PropertyStorage incomingGetStorage = c.getStorage();
					String key = "AssemblySteps";
					if(incomingGetStorage.getValue(key)!=Optional.empty()) {
						HashMap<Integer,Transform> map=(HashMap<Integer, Transform>) incomingGetStorage.getValue(key).get();
						boolean set=false;
						TransformNR target=new TransformNR();
						for(int i=step;i<=numSteps;i++) {
							if(map.get(i)!=null) {
								double myScale= (i==step)?fraction:1;
								TransformNR scaled =TransformFactory.csgToNR(map.get(i)).scale(myScale);
								target=target.times(scaled);
								//println c.getName()+" sliderval="+newValue+" step="+step+" fraction:"+myScale+" || "+i						
								TransformFactory.nrToAffine(target,(Affine) incomingGetStorage.getValue("AssembleAffine").get());
								set=true;
							}
						}
						if(!set) {
							TransformFactory.nrToAffine(new TransformNR(),(Affine) incomingGetStorage.getValue("AssembleAffine").get());
						}
						
					}
				}
			}
		 });
		if(numSteps==0)
			slider.setDisable(true);
		return slider;
	}
}
