package com.neuronrobotics.nrconsole.plugin.bootloader.gui;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import java.net.MalformedURLException;
import javax.swing.*;

public class StatusLabel extends JLabel {
  private static final long serialVersionUID = 1L;
  public static final int OK = 1;
  public static final int ERROR = 2;

  public StatusLabel() {
    setStatus(0);
  }

  public void setStatus(int status) {
    switch (status) {
      case OK:
        setIcon(createImageIcon("dyio/ok.png"));
        break;
      case ERROR:
        setIcon(createImageIcon("dyio/error.png"));
        break;
      default:
        setIcon(createImageIcon("dyio/blank.png"));
        break;
    }

    invalidate();
    // repaint();
  }

  protected ImageIcon createImageIcon(String path) {
    java.net.URL imgURL;
    try {
      imgURL = AssetFactory.loadFile(path).toURI().toURL();
      if (imgURL != null) return new ImageIcon(imgURL);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.err.println("Couldn't find file: " + path);
    return null;
  }
}
