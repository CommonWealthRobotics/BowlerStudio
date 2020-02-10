package javafx.embed.swing;

import com.sun.javafx.stage.WindowHelper;

import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class MySwingNode extends SwingNode {
	
	VBox myEnclosingNode;
	
	public MySwingNode(VBox myEnclosingNode) {
		this.myEnclosingNode=myEnclosingNode;
	}

    /*
     * Called on EDT
     */
	@Override
	void setImageBuffer(final int[] data,
                        final int x, final int y,
                        final int w, final int h,
                        final int linestride,
                        final int scale)
    {
       try {
    	   Window win = getScene().getWindow();
    	   super.setImageBuffer(data,
                     x, y,
                    w,  h,
                    linestride,
                    scale);
       }catch(Exception ex){
    	   ex.printStackTrace();
       }
    }
	
	@Override
	void setImageBounds(final int x, final int y, final int w, final int h) {
		try {
			// this can throw an NPE
			Window win = getScene().getWindow();
			super.setImageBounds(x, y, w, h);

		} catch (java.lang.NullPointerException ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public double prefWidth(double height) {
		try {
			return super.prefWidth(height);
		} catch (Exception e) {
			// System.out.println("Error in "+file);
			// e.printStackTrace();
			return myEnclosingNode.prefWidthProperty().doubleValue();
		}

	}

	@Override
	public double prefHeight(double height) {
		try {
			return super.prefHeight(height);
		} catch (Exception e) {
			// System.out.println("Error in "+file);
			// e.printStackTrace();
			return myEnclosingNode.prefHeightProperty().doubleValue();
		}

	}
	/**
	 * Returns the {@code SwingNode}'s minimum width for use in layout calculations.
	 * This value corresponds to the minimum width of the Swing component.
	 * 
	 * @return the minimum width that the node should be resized to during layout
	 */
	@Override
	public double maxWidth(double height) {
		try {
			return super.maxWidth(height);
		} catch (Exception e) {
			// System.out.println("Error in "+file);
			// e.printStackTrace();
			return myEnclosingNode.maxWidthProperty().doubleValue();
		}

	}

	/**
	 * Returns the {@code SwingNode}'s minimum height for use in layout
	 * calculations. This value corresponds to the minimum height of the Swing
	 * component.
	 * 
	 * @return the minimum height that the node should be resized to during layout
	 */
	@Override
	public double maxHeight(double width) {
		try {
			return super.maxHeight(width);
		} catch (Exception e) {
			// System.out.println("Error in "+file);
			// e.printStackTrace();
			return myEnclosingNode.maxHeightProperty().doubleValue();
		}
	}

	/**
	 * Returns the {@code SwingNode}'s minimum width for use in layout calculations.
	 * This value corresponds to the minimum width of the Swing component.
	 * 
	 * @return the minimum width that the node should be resized to during layout
	 */
	@Override
	public double minWidth(double height) {
		try {
			return super.minWidth(height);
		} catch (Exception e) {
			// System.out.println("Error in "+file);
			// e.printStackTrace();
			return myEnclosingNode.minWidthProperty().doubleValue();
		}

	}

	/**
	 * Returns the {@code SwingNode}'s minimum height for use in layout
	 * calculations. This value corresponds to the minimum height of the Swing
	 * component.
	 * 
	 * @return the minimum height that the node should be resized to during layout
	 */
	@Override
	public double minHeight(double width) {
		try {
			return super.minHeight(width);
		} catch (Exception e) {
			// System.out.println("Error in "+file);
			// e.printStackTrace();
			return myEnclosingNode.minHeightProperty().doubleValue();
		}
	}
}