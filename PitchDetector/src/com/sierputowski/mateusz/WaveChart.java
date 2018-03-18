package com.sierputowski.mateusz;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class WaveChart {
	public String title;
	private DefaultCategoryDataset dataset = new DefaultCategoryDataset();

	public ChartPanel getChart() {
		JFreeChart chart = ChartFactory.createLineChart("Test Chart", "x", "y", dataset, PlotOrientation.VERTICAL, true,
				true, false);
		ChartPanel chartPanel = new ChartPanel(chart);
		return chartPanel;
	}

	public WaveChart generateChartFromFile(File file) {
		dataset = new DefaultCategoryDataset();
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int bytesPerFrame = audioInputStream.getFormat().getFrameSize();
		if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
			bytesPerFrame = 1;
		}
		int bufferSize = 32;
		int totalFramesRead = 0;
		int numBytes = bufferSize * bytesPerFrame;
		byte[] audioBytes = new byte[numBytes];
		try {
			int numBytesRead = 0;
			int numFramesRead = 0;
			int j = 0;
			while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
				numFramesRead = numBytesRead / bytesPerFrame;
				totalFramesRead += numFramesRead;
				for (byte b : audioBytes) {
					Integer x = (int) b;
					dataset.addValue(x, String.valueOf(numFramesRead), String.valueOf(totalFramesRead));
					j += 1000;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return this;
	}

	public void generateRealTimeChart(byte[] data) {

	}
}
