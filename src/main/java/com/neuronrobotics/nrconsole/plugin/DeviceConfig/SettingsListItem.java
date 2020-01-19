package com.neuronrobotics.nrconsole.plugin.DeviceConfig;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class SettingsListItem extends JPanel {
  private JLabel lblNewLabel;
  private JTextField txtSetVal;
  private String labelTxt;
  private String currentValue;

  public SettingsListItem() {
    add(getLblNewLabel());
    add(getTxtSetVal());
  }

  public SettingsListItem(String _lblText, String _value) {
    add(getLblNewLabel());
    add(getTxtSetVal());
    loadValues(_lblText, _value);
  }

  public SettingsListItem(String _lblText, int _value) {
    add(getLblNewLabel());
    add(getTxtSetVal());
    loadValues(_lblText, _value);
  }

  public SettingsListItem(String _lblText, double _value) {
    add(getLblNewLabel());
    add(getTxtSetVal());
    loadValues(_lblText, _value);
  }

  public void loadValues(String _lblTxt, String _value) {
    currentValue = _value;
    labelTxt = _lblTxt;
    getLblNewLabel().setText(_lblTxt);
    getTxtSetVal().setText(_value);
  }

  public void loadValues(String _lblTxt, int _value) {
    currentValue = Integer.toString(_value);
    labelTxt = _lblTxt;
    getLblNewLabel().setText(_lblTxt);
    getTxtSetVal().setText(Integer.toString(_value));
  }

  public void loadValues(String _lblTxt, double _value) {
    currentValue = Double.toString(_value);
    labelTxt = _lblTxt;
    getLblNewLabel().setText(_lblTxt);
    getTxtSetVal().setText(Double.toString(_value));
  }

  private JLabel getLblNewLabel() {
    if (lblNewLabel == null) {
      lblNewLabel = new JLabel("New label");
    }
    return lblNewLabel;
  }

  public int getIntValue() {
    try {
      return Integer.valueOf(getTxtSetVal().getText());
    } catch (Exception e) {
      return 1;
    }
  }

  public double getDoubleValue() {
    try {

      return Double.valueOf(getTxtSetVal().getText());
    } catch (Exception e) {
      return 1;
    }
  }

  public String getStringValue() {
    return getTxtSetVal().getText();
  }

  private JTextField getTxtSetVal() {
    if (txtSetVal == null) {
      txtSetVal = new JTextField();
      txtSetVal.setText("Setting");

      txtSetVal.addCaretListener(
          new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent e) {
              if (txtSetVal.getText().equals(currentValue)) {
                txtSetVal.setBackground(Color.WHITE);
              } else {
                txtSetVal.setBackground(Color.ORANGE);
              }
            }
          });

      txtSetVal.setColumns(10);
    }
    return txtSetVal;
  }
}
