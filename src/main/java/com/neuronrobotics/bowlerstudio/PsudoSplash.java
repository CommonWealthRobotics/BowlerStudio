package com.neuronrobotics.bowlerstudio;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;
import com.neuronrobotics.bowlerstudio.scripting.DownloadManager;
import com.neuronrobotics.bowlerstudio.scripting.GitLogProgressMonitor;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.video.OSUtil;

public class PsudoSplash implements GitLogProgressMonitor {
	JFrame interfaceFrame;
	private String message = "";
	private String log = "";
	private static URL resource = PsudoSplash.class.getResource("splash.png");

	private static PsudoSplash singelton = null;

	public static boolean isInitialized() {
		return singelton != null;
	}

	public static PsudoSplash get() {
		if (singelton == null)
			singelton = new PsudoSplash();
		return singelton;
	}

	public static void close() {
		if (singelton != null)
			singelton.closeSplashLocal();
		singelton = null;

	}

	@Override
	public void onUpdate(String update, Exception e) {
		// e.printStackTrace(System.err);
		log = update;
		updateSplash();
		System.out.println(update);
	}

	class CustomPanel extends JPanel {
	    private BufferedImage offscreenImage;
	    private Graphics2D offscreenGraphics;
	    private void createOffscreenImage() {
	        offscreenImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
	        offscreenGraphics = offscreenImage.createGraphics();
	        offscreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    }
	    @Override
	    public void addNotify() {
	        super.addNotify();
	        createOffscreenImage();
	    }

		/**
		 * 
		 */
		private static final long serialVersionUID = 8749662598444052868L;
		private BufferedImage image;

		public CustomPanel() {
			setOpaque(false);
			try {
				/*
				 * Since Images are Application Resources, it's always best to access them in
				 * the form of a URL, instead of File, as you are doing. Uncomment this below
				 * line and watch this answer of mine, as to HOW TO ADD IMAGES TO THE PROJECT
				 * https://stackoverflow.com/a/9866659/1057230 In order to access images with
				 * getClass().getResource(path) here your Directory structure has to be like
				 * this Project | ------------------------ | | bin src | | --------- .java files
				 * | | package image(folder) ( or | .class 404error.jpg files, if no package
				 * exists.)
				 */
				image = ImageIO.read(getResource());

			} catch (IOException ioe) {
				System.out.println("Unable to fetch image.");
				ioe.printStackTrace();
			}
		}

		/*
		 * Make this one customary habbit, of overriding this method, when you extends a
		 * JPanel/JComponent, to define it's Preferred Size. Now in this case we want it
		 * to be as big as the Image itself.
		 */
		@Override
		public Dimension getPreferredSize() {
			return (new Dimension(image.getWidth(), image.getHeight()));
		}

	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);

	        if (offscreenImage == null) {
	            createOffscreenImage();
	        }

	        // Clear the offscreen image with a fully transparent color
	        offscreenGraphics.setComposite(AlphaComposite.Clear);
	        offscreenGraphics.fillRect(0, 0, getWidth(), getHeight());
	        offscreenGraphics.setComposite(AlphaComposite.SrcOver);

	        // Your custom painting code goes here
	        paintCustomGraphics(offscreenGraphics);

	        // Draw the offscreen image onto the panel
	        ((Graphics2D)g).setComposite(AlphaComposite.SrcOver);
	        g.drawImage(offscreenImage, 0, 0, this);
	    }
		/*
		 * This is where the actual Painting Code for the JPanel/JComponent goes. Here
		 * we will draw the image. Here the first line super.paintComponent(...), means
		 * we want the JPanel to be drawn the usual Java way first, then later on we
		 * will add our image to it, by writing the other line, g.drawImage(...).
		 */
		protected void paintCustomGraphics(Graphics g) {

			// super.paintComponent(g);
			g.drawImage(image, 0, 0, this);
			Graphics2D splashGraphics = (Graphics2D) g;
			splashGraphics.setComposite(AlphaComposite.Clear);
			// splashGraphics.fillRect(65, 270, 200, 40);
			splashGraphics.setPaintMode();
			splashGraphics.setColor(Color.WHITE);
			splashGraphics.drawString(StudioBuildInfo.getVersion(), 65, 45);

			splashGraphics.setComposite(AlphaComposite.Clear);
			// splashGraphics.fillRect(65, 270, 200, 40);
			splashGraphics.setPaintMode();
			splashGraphics.setColor(Color.WHITE);
			splashGraphics.drawString(getMessage(), 65, 280);

			splashGraphics.setComposite(AlphaComposite.Clear);
			// splashGraphics.fillRect(65, 270, 200, 40);
			splashGraphics.setPaintMode();
			splashGraphics.setColor(Color.WHITE);
			splashGraphics.drawString(log, 15, 120);

		}
	}

	private PsudoSplash() {

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					// if(!OSUtil.isLinux())
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				interfaceFrame = new JFrame("Loading BowlerStudio...");
				interfaceFrame.setUndecorated(true);
				interfaceFrame.setLayout(new BorderLayout());
				CustomPanel contentPane = new CustomPanel();
				// interfaceFrame.setBackground(Color.black);
				interfaceFrame.setContentPane(contentPane);
				interfaceFrame.pack();
				interfaceFrame.setLocationRelativeTo(null);
				interfaceFrame.setVisible(true);
				interfaceFrame.setBackground(new Color(0, 0, 0, 0));
				try {
					interfaceFrame.setIconImage(ImageIO.read(resource));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				interfaceFrame.setAlwaysOnTop(true);

			}
		});
//		while (interfaceFrame == null)
//			try {
//				Thread.sleep(100);
//				System.out.println("Waiting for spalsh...");
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

//	public void setIcon(Image img) {
//		BufferedImage image = javafx.embed.swing.SwingFXUtils.fromFXImage(img, null);
//		if (interfaceFrame != null)
//			interfaceFrame.setIconImage(image);
//	}
	boolean isVisableSplash() {
		if (interfaceFrame != null)
			return interfaceFrame.isVisible();
		return false;
	}

	private void closeSplashLocal() {
		if (interfaceFrame != null)
			interfaceFrame.setVisible(false);
		// ScriptingEngine.removeLogListener(this);

	}

	void updateSplash() {
		if (interfaceFrame != null) {
			SwingUtilities.invokeLater(() -> {
				interfaceFrame.invalidate();
				interfaceFrame.repaint();
			});

		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if (message.length() > 23) {
			this.message = message.subSequence(0, 23).toString();
			// new RuntimeException().printStackTrace();
		} else
			this.message = message;
		if (interfaceFrame != null) {
			interfaceFrame.setVisible(true);
		}
		ScriptingEngine.addLogListener(this);
		DownloadManager.addLogListener(this);
		log = "";
	}

	public static URL getResource() {
		return resource;
	}

	public static void setResource(URL r) {
		resource = r;
	}

}
