package com.neuronrobotics.bowlerstudio.tabs;

import com.neuronrobotics.sdk.addons.kinematics.FirmataBowler;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.ScrollPane;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ui.JPinboard;

import javax.swing.*;

public class FirmataTab extends AbstractBowlerStudioTab {
    @Override
    public void onTabClosing() {
    }

    @Override
    public String[] getMyNameSpaces() {
        return new String[0];
    }

    @Override
    public void initializeUI(BowlerAbstractDevice pm) {
        FirmataDevice device = ((FirmataBowler) pm).getFirmataDevice();
        JFrame frame = new JFrame("Pinboard Example");
        frame.add(new JPinboard(device));
        frame.pack();
        frame.setVisible(true);

        JPinboard pinboard = new JPinboard(device);
        pinboard.setVisible(true);
        SwingNode sn = new SwingNode();
        sn.setContent(pinboard);
        ScrollPane s1 = new ScrollPane();

        s1.setContent(sn);
        setContent(s1);
        setText("Firmata Pinpoard");
        onTabReOpening();
    }

    @Override
    public void onTabReOpening() {
    }
}
