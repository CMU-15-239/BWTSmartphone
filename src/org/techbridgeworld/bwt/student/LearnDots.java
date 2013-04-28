package org.techbridgeworld.bwt.student;

import java.util.Random;
import javaEventing.EventManager;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.BoardEvent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Teach students the numbering of the dots on the BWT
 * 
 * @author salemhilal
 */
public class LearnDots extends Activity {

	// Mapping of 1-6 to their string counterparts
	private String[] numbers = { "one", "two", "three", "four", "five", "six" };

	// The global application
	private MyApplication application;

	// Speaks text aloud
	private TextToSpeech tts;

	// The BWT
	private final BWT bwt = new BWT(this, LearnDots.this);

	// Randomly chooses the dot to test
	private Random generator = new Random(15239);

	// The current dot being tested
	private int currentDot = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		// Get the global application and global text to speech
		application = ((MyApplication) getApplicationContext());
		tts = application.myTTS;

		// Initialize currentFile and filenames
		application.currentFile = 0;
		application.filenames.clear();

		// Initialize the BWT connection
		bwt.init();

		// Start the BWT
		bwt.start();

		// Start tracking the state of the BWT
		bwt.initializeEventListeners();
		bwt.startTracking();
		runGame();
	}

	@Override
	public void onPause() {
		// Clear the audio queue and stop the bwt
		application.clearAudio();
		bwt.stopTracking();
		bwt.removeEventListeners();
		bwt.stop();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		// Stop and shutdown text to speech
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	/**
	 * Get a new random dot, and trigger the associated audio. (Audio:
	 * "Good. Find dot __")
	 */
	private void regenerate() {
		currentDot = generator.nextInt(6) + 1;
		application.queueAudio(R.string.good);
		application.queueAudio(R.string.find_dot);
		application.queueAudio(numbers[currentDot - 1]);
	}

	/**
	 * Provides the first instructions, then creates a listener for the BWT
	 * board.
	 */
	private void runGame() {
		// Generate the first dot.
		currentDot = generator.nextInt(6) + 1;
		application.queueAudio(R.string.find_dot);
		application.queueAudio(numbers[currentDot - 1]);
		application.playAudio();

		createListener();
	}

	/**
	 * Listens for input from the user from anywhere on the board. Gives
	 * feedback based on the dot they pushed and queues and plays audio as
	 * necessary.
	 */
	private void createListener() {
		// Listener to detect board input
		GenericEventListener DotListener = new GenericEventListener() {

			@Override
			public void eventTriggered(Object arg0, Event event) {
				Log.d("Learn Dots", "Triggered Board Event");

				// Do nothing in regards to AltBtn being pressed
				if (((BoardEvent) event).getCellInd() == -1) {
					return;
				}

				// Cast the given event as a BoardEvent, and get the relevant
				// dot information
				int trial = ((BoardEvent) event).getDot();

				bwt.clearTouchedCells();

				// If they pressed the dot, then pick another dot
				if (trial == currentDot) {
					regenerate();
				}

				/*
				 *  Otherwise, tell the user that they are incorrect and repeat
				 *  the prompt.
				 */
				else {
					application.queueAudio(R.string.no);
					application.queueAudio(R.string.find_dot);
					application.queueAudio(numbers[currentDot - 1]);
				}
				application.playAudio();
			}
		};

		// Start the listener
		EventManager.registerEventListener(DotListener, BoardEvent.class);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// If the user presses back, go to GameActivity
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			bwt.removeEventListeners();
			Intent intent = new Intent(LearnDots.this, GameActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}