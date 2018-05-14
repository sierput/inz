package com.sierputowski.mateusz;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import jm.JMC;
import jm.music.data.Note;
import jm.util.Play;
import jm.util.Read;

public class PitchDetector {
	private String algoritm;
	private File file;
	public JTextArea result;
	public String resultMessage;
	public double actualPitch = 0;
	private Icon iconButtonRed;
	private Icon iconButtonGreen;
	public boolean setRec;
	public byte[] data;
	public float sampleRate = 44100;
	public int audioBufferSize = 2048;
	public int bufferOverlap = audioBufferSize / 2;
	public MIDIHandler midi;
	public static String PITCH_DETECTOR = "Pitch Detector";
	boolean let = false;
	public String FIRST_MY_IMPLEMENT = "Autocorelation";
	public String SECOND_MY_IMPLEMENT = "AMDF_my_impl";

	public PitchDetector() {
		this.algoritm = "YIN";
		iconButtonGreen = new ImageIcon(
				"/home/msierputowski/inz/PitchDetector/src/com/sierputowski/mateusz/img/Green.png");
		iconButtonRed = new ImageIcon("/home/msierputowski/inz/PitchDetector/src/com/sierputowski/mateusz/img/Red.png");
		try {
			midi = new MIDIHandler();
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void makeGUI() {
		JFrame frame = new JFrame(PITCH_DETECTOR);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 1000);
		JPanel panel = new JPanel(new BorderLayout());
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		JPanel chartPanel = new JPanel();
		JLabel chartInfo = new JLabel("should load file");
		JLabel label = new JLabel("start");
		JButton playMIDI = new JButton("Play MIDI");
		JButton loadFile = new JButton("Load file", iconButtonRed);
		JButton detectPitch = new JButton("Detect Pitch");
		JButton showChart = new JButton("Show chart");
		JButton rec = new JButton("Rec");
		JButton recOff = new JButton("Stop");
		JFileChooser fileChooser = new JFileChooser("./src/com/sierputowski/mateusz/sounds");
		fileChooser.setVisible(false);
		result = new JTextArea();
		result.setEditable(false);
		JScrollPane pane = new JScrollPane(result);
		JScrollBar vertical = pane.getVerticalScrollBar();
		vertical.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// TODO Auto-generated method stub
				vertical.setValue(vertical.getMaximum());
			}
		});
		List<String> algoArray = new ArrayList<>();
		for (PitchEstimationAlgorithm pitchEstimationAlgorithm : PitchEstimationAlgorithm.values()) {
			algoArray.add(pitchEstimationAlgorithm.toString());
		}
		algoArray.add(FIRST_MY_IMPLEMENT);
		algoArray.add(SECOND_MY_IMPLEMENT);
		JComboBox<String> algoritms = new JComboBox<>(algoArray.toArray(new String[algoArray.size()]));
		WaveChart chart = new WaveChart();
		detectPitch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resultMessage = "";
				if (file != null) {
					detectPitchYin(file, chartPanel, chart);
				}
			}
		});

		algoritms.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				algoritm = algoritms.getSelectedItem().toString();
			}
		});

		playMIDI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Note note = new Note();
				note.setPitch(JMC.C4);
				note.setLength(5);
				Play.midi(note);
			}
		});

		loadFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser.setVisible(true);

			}
		});
		fileChooser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.getSelectedFile() != null) {
					fileChooser.setVisible(false);
					file = fileChooser.getSelectedFile();
					loadFile.setIcon(iconButtonGreen);
				} else {
					loadFile.setSelectedIcon(iconButtonRed);
					fileChooser.setVisible(true);
				}
			}
		});
		showChart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (chartPanel.isVisible()) {
					chartPanel.setVisible(false);
				} else {
					if (file != null) {
						chart.generateChartFromFile(file);
						chartPanel.add(chart.getChart());
						chartPanel.revalidate();
						chartInfo.setText("");
					} else {
						chartInfo.setText("should load file");
					}
					chartPanel.setVisible(true);
				}

			}
		});
		rec.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Thread thread = new Thread() {
					public void run() {
						setRec = true;
						transmisionInput();
					}
				};
				thread.start();
				realTimeChart();
			}
		});
		recOff.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setRec = false;

			}
		});

		top.add(label);
		top.add(algoritms);
		top.add(loadFile);
		top.add(fileChooser);
		top.add(playMIDI);
		top.add(detectPitch);
		top.add(showChart);
		top.add(chartPanel);
		top.add(rec);
		top.add(recOff);
		chartPanel.setVisible(false);
		chartPanel.add(chartInfo);
		panel.add(top, BorderLayout.PAGE_START);
		panel.add(pane, BorderLayout.CENTER);
		frame.add(panel);
		frame.setVisible(true);

	}

	public void realTimeChart() {
		// TODO real time chart
	}

	protected void transmisionInput() {
		AudioFormat format = getAudioFormat();
		TargetDataLine line = null;
		AudioInputStream audioInputStream = null;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an
		// AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
			// Handle the error ...
		}
		// Obtain and open the line.
		try {
			line = AudioSystem.getTargetDataLine(format);
			line.open(format);
		} catch (LineUnavailableException ex) {
			// Handle the error ...
		}
		// Assume that the TargetDataLine, line, has already
		// been obtained and opened.
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int numBytesRead;
		data = new byte[line.getBufferSize() / 5];
		// Begin audio capture.
		line.start();

		// Here, stopped is a global boolean set by another thread.
		while (setRec) {

			// Read the next chunk of data from the TargetDataLine.
			numBytesRead = line.read(data, 0, data.length);
			audioInputStream = new AudioInputStream(line);
			// for (byte b : data) {
			detectPitchRealTime(audioInputStream);
			// System.out.print(b + "\n");
			// }
			// Save this chunk of data.
			out.write(data, 0, numBytesRead);
		}
	}

	private AudioFormat getAudioFormat() {
		float sampleRate = 44100.0F;
		int sampleSizeBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;

		return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
	}

	private void detectPitchYin(File file, JPanel chartPanel, WaveChart chart) {
		if (getAlgoritm().equals(FIRST_MY_IMPLEMENT)) {
			detectPitchMyFirstImpl();
		} else if (getAlgoritm().equals(SECOND_MY_IMPLEMENT)) {
			detectPitchMySecondImpl();
		} else
			detectPitch(file);
	}

	private void detectPitchMySecondImpl() {
		// AMDF
		int bufforLenght = 512;
		double sampleRate = 44100;
		float[] data = Read.audio(file.getAbsolutePath());
		float[] outR = new float[bufforLenght];
		int maxT = 0;
		float max = 0;
		for (int i = 0; i < data.length; i += bufforLenght) {
			// wzór Rxx(k) = E x(n) * x(n+k)
			for (int k = 20; k < bufforLenght; k++) {
				for (int n = 0; n < bufforLenght; n++) {
					if (n + k >= bufforLenght)
						break;
					if (n + k + i >= data.length)
						break;
					outR[k] += data[i + n] * data[i + n + k];
				}
				if (outR[k] > max) {
					max = outR[k];
					maxT = k;
				}
			}
			outR = new float[bufforLenght];
			double time = i / sampleRate;
			double f = 1 / (maxT / sampleRate);
			String message = String.format("częstotliwość: %4.2f ; czas: %5.4f \n", f, time);
			resultMessage += message;
			result.setText(resultMessage);
			max = 0;
			maxT = 0;
		}
	}

	private void detectPitchMyFirstImpl() {
		// Autokorelacja
		int bufforLenght = 512;
		double sampleRate = 44100;
		float[] data = Read.audio(file.getAbsolutePath());
		float[] outR = new float[bufforLenght];
		int maxT = 0;
		float max = 0;
		for (int i = 0; i < data.length; i += bufforLenght) {
			for (int k = 20; k < bufforLenght; k++) {
				for (int n = 0; n < bufforLenght; n++) {
					if (n + k >= bufforLenght)
						break;
					if (n + k + i >= data.length)
						break;
					outR[k] += data[i + n] * data[i + n + k];
				}
				if (outR[k] > max) {
					max = outR[k];
					maxT = k;
				}
			}
			outR = new float[bufforLenght];
			double time = i / sampleRate;
			double f = 1 / (maxT / sampleRate);
			String message = String.format("częstotliwość: %4.2f ; czas: %5.4f \n", f, time);
			resultMessage += message;
			result.setText(resultMessage);
			max = 0;
			maxT = 0;
		}
	}

	protected void detectPitch(File file) {
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Convert into TarsosDSP API
		JVMAudioInputStream audioStream = new JVMAudioInputStream(audioInputStream);
		AudioDispatcher dispatcher = new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
		GetInfoFromPitchDetector myPitchDetector = new GetInfoFromPitchDetector();
		dispatcher
				.addAudioProcessor((AudioProcessor) new PitchProcessor(PitchEstimationAlgorithm.valueOf(getAlgoritm()),
						sampleRate, audioBufferSize, myPitchDetector));
		setRec = true;
		dispatcher.run();
	}

	protected void detectPitchRealTime(AudioInputStream audioInputStream) {
		// Convert into TarsosDSP API
		ComplexOnsetDetector onsetDetector;
		JVMAudioInputStream audioStream = new JVMAudioInputStream(audioInputStream);
		AudioDispatcher dispatcher = new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
		GetInfoFromPitchDetector myPitchDetector = new GetInfoFromPitchDetector();
		dispatcher
				.addAudioProcessor((AudioProcessor) new PitchProcessor(PitchEstimationAlgorithm.valueOf(getAlgoritm()),
						sampleRate, audioBufferSize, myPitchDetector));
		double threshold = 0.4;
		PercussionHandler myPercussionHandler = new PercussionHandler();
		onsetDetector = new ComplexOnsetDetector(audioBufferSize, threshold, 0.07, -60);
		onsetDetector.setHandler(myPercussionHandler);
		dispatcher.addAudioProcessor(onsetDetector);
		dispatcher.run();
	}

	public String getAlgoritm() {
		return algoritm;
	}

	public void setAlgoritm(String algoritm) {
		this.algoritm = algoritm;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	class GetInfoFromPitchDetector implements PitchDetectionHandler {

		// Here the result of pitch is always less than half.
		@Override
		public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
			if (!setRec) {
				// TODO Better handling stop stream.
				return;
			}
			if (pitchDetectionResult.getPitch() != -1) {
				double timeStamp = audioEvent.getTimeStamp();
				float pitch = pitchDetectionResult.getPitch() / 2;
				System.out.print(pitch + "\n");
				String message = String.format("Pitch detected at %.2fs: %.2fHz \n", timeStamp, pitch);
				if (pitch > 100 && let == true) {
					midi.playMidiFromHz(pitch);
					let = false;
					return;
				}
				resultMessage += message;
				result.setText(resultMessage);
			}
		}
	}

	class PercussionHandler implements OnsetHandler {

		@Override
		public void handleOnset(double time, double salience) {
			let = true;
		}

	}
}
