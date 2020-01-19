package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class AdjustbodyMassWidget extends Group {

  private MobileBase device;
  private MobileBaseCadManager manager;

  double textToNum(TextField mass) {
    try {
      return Double.parseDouble(mass.getText().trim());
    } catch (Throwable t) {
      mass.setText("0");
      return 0;
    }
  }

  public AdjustbodyMassWidget(MobileBase device) {
    this.device = device;
    manager = MobileBaseCadManager.get(device);
    GridPane pane = new GridPane();

    TextField mass = new TextField(CreatureLab.getFormatted(device.getMassKg()));
    mass.setOnAction(
        event -> {
          device.setMassKg(textToNum(mass));
          if (manager != null) manager.generateCad();
        });
    TransformNR currentCentroid = device.getCenterOfMassFromCentroid();
    TextField massx = new TextField(CreatureLab.getFormatted(currentCentroid.getX()));
    massx.setOnAction(
        event -> {
          currentCentroid.setX(textToNum(massx));
          device.setCenterOfMassFromCentroid(currentCentroid);
          ;
          if (manager != null) manager.generateCad();
        });

    TextField massy = new TextField(CreatureLab.getFormatted(currentCentroid.getY()));
    massy.setOnAction(
        event -> {
          currentCentroid.setY(textToNum(massy));
          device.setCenterOfMassFromCentroid(currentCentroid);
          ;
          if (manager != null) manager.generateCad();
        });

    TextField massz = new TextField(CreatureLab.getFormatted(currentCentroid.getZ()));
    massz.setOnAction(
        event -> {
          currentCentroid.setZ(textToNum(massz));
          device.setCenterOfMassFromCentroid(currentCentroid);
          ;
          if (manager != null) manager.generateCad();
        });

    pane.add(new Text("Mass"), 0, 0);
    pane.add(mass, 1, 0);

    pane.add(new Text("Mass Centroid x"), 0, 1);
    pane.add(massx, 1, 1);

    pane.add(new Text("Mass Centroid y"), 0, 2);
    pane.add(massy, 1, 2);
    pane.add(new Text("Mass Centroid z"), 0, 3);
    pane.add(massz, 1, 3);
    getChildren().add(pane);
  }
}
