package com.sierputowski.mateusz;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author Mateusz Sierputowski msierputowski@wi.zut.edu.pl
 *
 */

public class Main {

	public static void main(String[] args) {
		setUIManager();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				PitchDetector program = new PitchDetector();
				program.makeGUI();
			}
		});
	}

	private static void setUIManager() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
}
