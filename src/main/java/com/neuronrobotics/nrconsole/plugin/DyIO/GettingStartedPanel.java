package com.neuronrobotics.nrconsole.plugin.DyIO;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.sdk.config.SDKBuildInfo;

public class GettingStartedPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7822822213729180361L;
	private JButton onLineDocs = new JButton("Open");
	private JButton javaDocs = new JButton("Open");
	
	private String wiki = "http://wiki.neuronrobotics.com/Getting_started_main_page";
	private String jDoc1 = "http://downloads.neuronrobotics.com/nrdk/";
	private String jDoc2 = "/java/docs/api/index.html";
	private String jDoc;
	private static Desktop desktop = null;
	public GettingStartedPanel (){
		setName("Getting Started");
		setLayout(new MigLayout());
		
		jDoc = jDoc1+SDKBuildInfo.getVersion()+jDoc2;
		if (Desktop.isDesktopSupported()) {
	        setDesktop(Desktop.getDesktop());
		}else{
			System.err.println("Desktop not supported");
		}
		
		add(new JLabel("Getting Started Documents:"),"wrap");
		add(new DocWidget("Online Getting Started Overview", wiki),"wrap");
		add(new DocWidget("Online DyIO Details", "http://wiki.neuronrobotics.com/DyIO"),"wrap");
		add(new DocWidget("Online 'JavaDoc' Programming guide", jDoc),"wrap");
		
	}
	
	//Method to launch a page in a browser
	//used to allow object "helpButton" to function
	public static void openPage(URI uri) throws Exception {
		getDesktop().browse(uri);
	}
	
	public static Desktop getDesktop() throws Exception {
		if(desktop == null) {
			if (Desktop.isDesktopSupported()) {
		        setDesktop(Desktop.getDesktop());
			}else{
				throw new Exception("Desktop not supported for documentation");
			}
		}
		return desktop;
	}
	public static void setDesktop(Desktop d) {
		desktop = d;
	}
	private class DocWidget extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = -174323483559948677L;
		URI uri;
		private JButton button = new JButton("Open");
		public DocWidget(String description, String location){
			setLayout(new MigLayout());
			try {
				uri = new URI(location);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			add(button );
			add(new JLabel(description),"wrap");
			
			button.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
			        try {
						getDesktop().browse(uri);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});
		}
	}
}
