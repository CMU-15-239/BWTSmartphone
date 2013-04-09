package org.techbridgeworld.bwt.student;

import java.util.Random;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.BoardEvent;
import org.techbridgeworld.bwt.api.libs.Braille;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;

public class LearnLetters extends Activity {

	private MyApplication application;
	private TextToSpeech tts;
	
	private static final Braille braille = new Braille();

	private String[] numbers = { "one", "two", "three", "four", "five", "six" };

	private static final char[][] letters = { { 'a', 'b', 'c', 'd', 'e' },
			{ 'f', 'g', 'h', 'i', 'j' }, { 'k', 'l', 'm', 'n', 'o' },
			{ 'p', 'q', 'r', 's', 't' }, { 'u', 'v', 'w', 'x', 'y', 'z' } };

	private static int[][] shuffledIndices;
	private int groupInd;
	private int countLetterInd;
	private int expectedBrailleCode;
	private int attemptNum;
	private boolean introducing; // introduce letters if true; test letters if
									// false

	private final BWT bwt = new BWT(this, LearnLetters.this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		
		application = ((MyApplication) getApplicationContext());
		tts = application.myTTS;
		
		application.currentFile = 0;
		application.filenames.clear();

		// Initialize the BWT connection.
		bwt.init();
		Log.i("Learn letters", "BWT Inited...");
		
		// Start the BWT
		bwt.start();
		
		// Start tracking the state of the BWT
		bwt.initializeEventListeners();
		Log.i("Learn letters", "BWT Started...");
		bwt.startTracking();
		runGame();
	}

	
	@Override
	public void onPause() {
		application.clearAudio();
		bwt.stopTracking();
		bwt.removeEventListeners();
        bwt.stop();
		super.onPause();
	}

    @Override
    public void onDestroy() {
    	// Stop text-to-speech
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

	private void runGame() {
		// Shuffle order of indices for testing phase
		shuffleIndices();
		
		groupInd = 0;
		attemptNum = 0;
		countLetterInd = 0;
		introducing = true;
		BWT.getBoard().setBitsAtUnivCell(0);
		expectedBrailleCode = braille.get(letters[groupInd][countLetterInd]);

		createListener();
		instructionSpellLetter(groupInd, countLetterInd);
		application.playAudio();
	}
	
	private void createListener() {
		Log.i("Learn letters", "Created own listeners...");
		bwt.replaceListener("onBoardEvent", new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				Log.i("Learn letters", "Board Event Triggered");
				BoardEvent e = (BoardEvent) event;
				
				//Do nothing on the AltBtn press
				if (e.getCellInd() == -1) {
					return;
				}

				int currentBrailleCode = BWT.getBoard().getBitsAtUnivCell();
				
//				/*****DEBUGGING PURPOSES******/
//				Log.i("Learn letters", "Dot input number: " + e.getDot());
//				Log.i("Learn letters",
//						"Current input: "
//								+ Integer.toBinaryString(currentBrailleCode)
//								+ "; " + "Expected input: "
//								+ Integer.toBinaryString(expectedBrailleCode));
//				/*******************************/
				
				// ((c & e) ^ c) > 0 if extra bits set in c that's not in e
				boolean isWrong = (((currentBrailleCode & expectedBrailleCode) ^ currentBrailleCode) > 0);
				
				//User is correct
				if (currentBrailleCode == expectedBrailleCode) {
					//application.clearAudio(); 
					application.queueAudio(R.string.good);

					prepNextLetter();
					Log.d("Learn letters", "user is correct");
				}
				//User is wrong
				else if (isWrong) {
					//application.clearAudio(); 
					application.queueAudio(R.string.no);

					redoCurrLetter();
					Log.d("Learn letters", "user is wrong");
				}
				//User is still in progress
				else {
					Log.d("Learn letters", "user is still writing");
				}

				application.playAudio();
			}
		});
	}

	/**
	 * Called when input is correct. Moves on to next letter.
	 */
	private void prepNextLetter() {
		BWT.getBoard().setBitsAtUnivCell(0);
		attemptNum = 1;
		countLetterInd++;
		if (countLetterInd >= letters[groupInd].length) {
			if (!introducing) {
				// Go to next group if done with testing mode
				groupInd++;
				if (groupInd >= letters.length) {
					// The real game doesn't end... what should we do? Let's start over
					groupInd = 0;
					introducing = true;
					countLetterInd = 0;
					attemptNum = 0;
					expectedBrailleCode = braille
							.get(letters[groupInd][countLetterInd]);
					instructionSpellLetter(groupInd, countLetterInd);
					return;
				}
			}
			// Regardless of mode, at end, need to reset ind, and flip boolean
			countLetterInd = 0;
			introducing = !introducing;
		}
		
		int letterInd = introducing ? countLetterInd : shuffledIndices[groupInd][countLetterInd];
		// Grab letter from random array if in testing mode
		if (!introducing) {
			instructionTestLetter(groupInd, letterInd);
		}
		// Grab letter in order if in introducing mode
		else {
			instructionSpellLetter(groupInd, letterInd);
		}
		expectedBrailleCode = braille.get(letters[groupInd][letterInd]);
	}

	/**
	 * Called when incorrect. updates attemptNums
	 */
	private void redoCurrLetter() {
		BWT.getBoard().setBitsAtUnivCell(0);
		attemptNum++;
		int letterInd = introducing ? countLetterInd : shuffledIndices[groupInd][countLetterInd];

		// Note: In test mode, don't spell out until 3rd attempt
		if (introducing || attemptNum >= 3) {
			instructionSpellLetter(groupInd, letterInd);
		} else if (!introducing) {
			instructionTestLetter(groupInd, letterInd);
		}
	}

	/**
	 * Provides instruction "To write letter __ press dots _,_,_,..."
	 * 
	 * @param groupInd
	 * @param letterInd
	 */
	private void instructionSpellLetter(int groupInd, int letterInd) {
		char let = letters[groupInd][letterInd];
		int btns = braille.get(let);
		StringBuffer btnStrBuf = new StringBuffer("");
		for (int i = 0; i < 6; i++) {
			if ((btns & (1 << i)) > 0) {
				btnStrBuf.append(i + 1 + " ");
			}
		}

		application.queueAudio(R.string.to_write_the_letter);
		application.queueAudio(((Character) let).toString());
		application.queueAudio(R.string.please_press);
		String[] buttons = btnStrBuf.toString().split(" ");
		for (int i = 0; i < buttons.length; i++)
			application.queueAudio(numbers[Integer.parseInt(buttons[i]) - 1]);
	}

	/**
	 * Provides instruction "Write letter _"
	 * 
	 * @param groupInd
	 * @param letterInd
	 */
	private void instructionTestLetter(int groupInd, int letterInd) {
		char let = letters[groupInd][letterInd];

		application.queueAudio(R.string.please_write);
		application.queueAudio(((Character) let).toString());
	}

	
	/**
	 * Shuffle the indices for testing-students mode
	 */
	private void shuffleIndices() {
		//initialize shuffledIndices
		shuffledIndices = new int[letters.length][];
		for (int i = 0; i < letters.length; i++) {
			shuffledIndices[i] = new int[letters[i].length];
			for (int j = 0; j < letters[i].length; j++) {
				shuffledIndices[i][j] = j;
			}
		}
		
		//shuffle the indices
		for (int i = 0; i < letters.length; i++) {
			shuffledIndices[i] = shuffleIndicesArr(shuffledIndices[i]);
		}
	}

	/**
	 * Helper function to scramble order in which student is tested
	 * @param givenArr
	 * @return
	 */
	private int[] shuffleIndicesArr(int[] givenArr) {
		int n = givenArr.length;
		Random r = new Random();
		for (int i = 0; i < n; i++) {
			int randInt = r.nextInt(n);
			int tmp = givenArr[randInt];
			givenArr[randInt] = givenArr[i];
			givenArr[i] = tmp;
		}
		return givenArr;
	}
	
	// If the user presses back, go back properly
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
	        Intent intent = new Intent(LearnLetters.this, GameActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}