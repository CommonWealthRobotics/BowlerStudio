package com.neuronrobotics.bowlerstudio.av;

import java.awt.Dimension;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;

/**
 * The source stream to go along with ImageDataSource.
 */
public class ImageSourceStream implements PullBufferStream {

	Vector images;
	int width, height;
	VideoFormat format;

	int nextImage = 0; // index of the next image to be read.
	boolean ended = false;

	public ImageSourceStream(int width, int height, int frameRate, Vector images) {
		this.width = width;
		this.height = height;
		this.images = images;

		format = new VideoFormat(VideoFormat.JPEG, new Dimension(width, height), Format.NOT_SPECIFIED,
				Format.byteArray, (float) frameRate);
	}

	/**
	 * We should never need to block assuming data are read from files.
	 */
	public boolean willReadBlock() {
		return false;
	}

	/**
	 * This is called from the Processor to read a frame worth of video
	 * data.
	 */
	public void read(Buffer buf) throws IOException {

		// Check if we've finished all the frames.
		if (nextImage >= images.size()) {
			// We are done. Set EndOfMedia.
			System.err.println("Done reading all images.");
			buf.setEOM(true);
			buf.setOffset(0);
			buf.setLength(0);
			ended = true;
			return;
		}

		String imageFile = (String) images.elementAt(nextImage);
		nextImage++;

		System.err.println("  - reading image file: " + imageFile);

		// Open a random access file for the next image.
		RandomAccessFile raFile;
		raFile = new RandomAccessFile(imageFile, "r");

		byte data[] = null;

		// Check the input buffer type & size.

		if (buf.getData() instanceof byte[])
			data = (byte[]) buf.getData();

		// Check to see the given buffer is big enough for the frame.
		if (data == null || data.length < raFile.length()) {
			data = new byte[(int) raFile.length()];
			buf.setData(data);
		}

		// Read the entire JPEG image from the file.
		raFile.readFully(data, 0, (int) raFile.length());

		System.err.println("    read " + raFile.length() + " bytes.");

		buf.setOffset(0);
		buf.setLength((int) raFile.length());
		buf.setFormat(format);
		buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);

		// Close the random access file.
		raFile.close();
	}

	/**
	 * Return the format of each video frame. That will be JPEG.
	 */
	public Format getFormat() {
		return format;
	}

	public ContentDescriptor getContentDescriptor() {
		return new ContentDescriptor(ContentDescriptor.RAW);
	}

	public long getContentLength() {
		return 0;
	}

	public boolean endOfStream() {
		return ended;
	}

	public Object[] getControls() {
		return new Object[0];
	}

	public Object getControl(String type) {
		return null;
	}
}