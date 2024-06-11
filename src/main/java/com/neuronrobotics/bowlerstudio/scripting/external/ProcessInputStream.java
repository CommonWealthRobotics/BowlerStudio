package com.neuronrobotics.bowlerstudio.scripting.external;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ProcessInputStream extends InputStream {

	private InputStream in;
	private int length, sumRead;
	private java.util.List<Listener> listeners;
	private double percent;

	public ProcessInputStream(InputStream inputStream, int length) throws IOException {
		this.in = inputStream;
		listeners = new ArrayList<>();
		sumRead = 0;
		this.length = length;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int readCount = in.read(b);
		evaluatePercent(readCount);
		return readCount;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int readCount = in.read(b, off, len);
		evaluatePercent(readCount);
		return readCount;
	}

	@Override
	public long skip(long n) throws IOException {
		long skip = in.skip(n);
		evaluatePercent(skip);
		return skip;
	}

	@Override
	public int read() throws IOException {
		int read = in.read();
		if (read != -1) {
			evaluatePercent(1);
		}
		return read;
	}

	public ProcessInputStream addListener(Listener listener) {
		this.listeners.add(listener);
		return this;
	}

	private void evaluatePercent(long readCount) {
		if (readCount != -1) {
			sumRead += readCount;
			percent = sumRead * 1.0 / length;
		}
		notifyListener();
	}

	private void notifyListener() {
		for (Listener listener : listeners) {
			listener.process(percent);
		}
	}
}