package com.sierputowski.mateusz;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import jm.music.data.Note;

public class MIDIHandler {
	private Instrument[] instr;
	private MidiChannel[] mChannels;
	private Synthesizer midiSynth;

	public MIDIHandler() throws MidiUnavailableException {
		midiSynth = MidiSystem.getSynthesizer();
		midiSynth.open();

		instr = midiSynth.getDefaultSoundbank().getInstruments();
		mChannels = midiSynth.getChannels();

		midiSynth.loadInstrument(instr[0]);// load an instrument
	}

	void playMidiFromHz(double value) {
		int pitchFromHz = Note.freqToMidiPitch(value);
		playMIDI(pitchFromHz);
	}

	void playMIDI(int value) {
		midiSynth.loadInstrument(instr[0]);// load an instrument
		mChannels[0].noteOn(value, 100);// On channel 0, play note number (value) with velocity 100
		try {
			Thread.sleep(500); // wait time in milliseconds to control duration
		} catch (InterruptedException e) {
		}
	}

}
