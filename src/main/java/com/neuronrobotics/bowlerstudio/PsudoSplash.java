package com.neuronrobotics.bowlerstudio;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.neuronrobotics.bowlerstudio.assets.StudioBuildInfo;

public class PsudoSplash {
	JFrame interfaceFrame;
	private String message = "";
	class CustomPanel extends JPanel {
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
				image = ImageIO.read(getClass().getResource("splash.png"));

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

		/*
		 * This is where the actual Painting Code for the JPanel/JComponent goes. Here
		 * we will draw the image. Here the first line super.paintComponent(...), means
		 * we want the JPanel to be drawn the usual Java way first, then later on we
		 * will add our image to it, by writing the other line, g.drawImage(...).
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image, 0, 0, this);
			Graphics2D splashGraphics=(Graphics2D) g;
			splashGraphics.setComposite(AlphaComposite.Clear);
//			splashGraphics.fillRect(65, 270, 200, 40);
			splashGraphics.setPaintMode();
			splashGraphics.setColor(Color.WHITE);
			splashGraphics.drawString(StudioBuildInfo.getVersion(), 65, 45);
			
			
			splashGraphics.setComposite(AlphaComposite.Clear);
//			splashGraphics.fillRect(65, 270, 200, 40);
			splashGraphics.setPaintMode();
			splashGraphics.setColor(Color.WHITE);
			splashGraphics.drawString( getMessage(), 65, 280);

		}
	}

	public PsudoSplash() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException ex) {
				} catch (InstantiationException ex) {
				} catch (IllegalAccessException ex) {
				} catch (UnsupportedLookAndFeelException ex) {
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
			}
		});
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	 boolean isVisableSplash() {
		 return interfaceFrame.isVisible();
	 }
	 void closeSplashLocal() {
		 interfaceFrame.setVisible(false);
	 }
	 void updateSplash() {
		 interfaceFrame.invalidate();
		 interfaceFrame.repaint();
	 }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
