package com.neuronrobotics.pidsim;

import com.neuronrobotics.bowlerstudio.tabs.AbstractBowlerStudioTab;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.ScrollPane;

public class PidLab extends AbstractBowlerStudioTab {
	LinearPhysicsEngine engine;
	private SwingNode wrapper;
	
	@Override
	public void onTabClosing() {
	}

	@Override
	public String[] getMyNameSpaces() {
		return new String[0];
	}

	@Override
	public void initializeUI(BowlerAbstractDevice pm) {
		engine = (LinearPhysicsEngine)pm;
		wrapper = new SwingNode();

		wrapper.setContent(engine.getPid().getGraphingPanel());
        ScrollPane s1 = new ScrollPane();
	       
        s1.setContent(wrapper);
        setContent(s1);
        setText("PID Lab");
		
	}

	@Override
	public void onTabReOpening() {
	}

}
