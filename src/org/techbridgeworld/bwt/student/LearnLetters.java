package org.techbridgeworld.bwt.student;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.BoardEvent;
import org.techbridgeworld.bwt.libs.Braille;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

public class LearnLetters extends Activity implements
		TextToSpeech.OnInitListener {

	private TextToSpeech tts;

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetectorCompat detector;

	private static final Braille braille = new Braille();

	private Context context;
	private MediaPlayer player;
	private String dir;
	private int currentFile;
	private ArrayList<String> filenames;

	private String[] numbers = { "one", "two", "three", "four", "five", "six" };

	private static final char[][] letters = { { 'a', 'b', 'c', 'd', 'e' },
			{ 'f', 'g', 'h', 'i', 'j' }, { 'k', 'l', 'm', 'n', 'o' },
			{ 'p', 'q', 'r', 's', 't' }, { 'u', 'v', 'w', 'x', 'y', 'z' } };

	private static int[][] shuffledIndices;
	private int groupInd;
	private int currLetterInd;
	private int countLetterInd;
	private int currentBrailleCode;
	private int expectedBrailleCode;
	private int attemptNum;
	private boolean introducing; // introduce letters if true; test letters if
									// false

	private final BWT bwt = new BWT(this, LearnLetters.this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.learn_letters);

		tts = new TextToSpeech(this, this);
		detector = new GestureDetectorCompat(this, new MyGestureListener());

		try {
			context = createPackageContext("org.techbridgeworld.bwt.teacher", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		player = new MediaPlayer();
		dir = context.getFilesDir().getPath().toString();
		currentFile = 0;
		filenames = new ArrayList<String>();

		bwt.init();
		Log.i("Learn letters", "BWT Inited...");

		groupInd = 0;
		currLetterInd = 0;

		attemptNum = 0;

		// Shuffle order of indices for testing phase
		shuffleIndices();
	}

	/**
	 * Shuffle the indices for testing mode
	 */
	private void shuffleIndices() {
		shuffledIndices = new int[letters.length][];
		for (int i = 0; i < letters.length; i++) {
			shuffledIndices[i] = new int[letters[i].length];
			for (int j = 0; j < letters[i].length; j++) {
				shuffledIndices[i][j] = j;
			}
		}

		for (int i = 0; i < letters.length; i++) {
			shuffledIndices[i] = shuffleIndicesArr(shuffledIndices[i]);
		}
	}

	@Override
	protected void onStop() {
		if (player != null)
			player.release();
		super.onStop();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");
			else {
				bwt.start();
				Log.i("Learn letters", "BWT Started...");
				bwt.startTracking();
				runProgram();
			}
		} else
			Log.e("TTS", "Initilization Failed!");
	}

	/**
	 * Plays the audio files
	 * 
	 * @param filename
	 */
	public void playAudio(String filename) {
		try {
			player.reset();
			FileInputStream fis = new FileInputStream(dir + "/" + filename
					+ ".m4a");
			player.setDataSource(fis.getFD());
			fis.close();
			player.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				player.stop();
				if (currentFile < filenames.size() - 1) {
					currentFile++;
					playAudio(filenames.get(currentFile));
				} else {
					filenames.clear();
					currentFile = 0;
				}
			}
		});

		player.start();
	}

	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2,
				float velocityX, float velocityY) {
			// Swipe up
			Log.d("Learn letters", "Swipe up occurred");
			if (event1.getY() - event2.getY() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				Intent intent = new Intent(LearnLetters.this,
						GameActivity.class);
				bwt.stopTracking();
				bwt.stop();
				startActivity(intent);
			}
			return true;
		}
	}

	private void createListener() {
		Log.i("Learn letters", "Created own listeners...");
		bwt.replaceListener("onBoardEvent", new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				bwt.defaultBoardHandler(sender, event);

				Log.i("Learn letters", "Own event triggered for board...");
				BoardEvent e = (BoardEvent) event;

				/** DEMO PURPOSES **/
				if (e.getCellInd() == -1)
					return;
				/*****************/

				currentBrailleCode |= (1 << (e.getDot() - 1));
				Log.i("Learn letters", "Dot input number: " + e.getDot());
				Log.i("Learn letters",
						"Current input: "
								+ Integer.toBinaryString(currentBrailleCode)
								+ "; " + "Expected input: "
								+ Integer.toBinaryString(expectedBrailleCode));
				// ((c & e) ^ c) > 0 if extra bits set in c that's not in e
				boolean isWrong = (((currentBrailleCode & expectedBrailleCode) ^ currentBrailleCode) > 0);
				if (currentBrailleCode == expectedBrailleCode) {
					// User is right
					if (player.isPlaying()) {
						filenames.clear();
						currentFile = 0;
					}
					filenames.add(getResources().getString(R.string.good));
					playAudio(filenames.get(0));

					prepNextLetter();
					Log.d("Learn letters", "user is correct");
				} else if (isWrong) {
					// User is wrong
					if (player.isPlaying()) {
						filenames.clear();
						currentFile = 0;
					}
					filenames.add(getResources().getString(R.string.no));
					playAudio(filenames.get(0));

					redoCurrLetter();
					Log.d("Learn letters", "user is wrong");
				} else {
					// User is still in progress
					Log.d("Learn letters", "user is still writing");
				}
			}
		});

		/** DEMO PURPOSES **/
		bwt.replaceListener("onAltBtnEvent", new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				bwt.defaultAltBtnHandler(sender, event);

				if (player.isPlaying()) {
					filenames.clear();
					currentFile = 0;
				}
				filenames.add(getResources().getString(R.string.good));
				playAudio(filenames.get(0));

				prepNextLetter();
			}
		});
		/***************/
	}

	private void runProgram() {
		groupInd = 0;
		currLetterInd = 0;
		countLetterInd = 0;
		introducing = true;
		currentBrailleCode = 0;
		expectedBrailleCode = braille.get(letters[groupInd][currLetterInd]);

		createListener();
		instructionSpellLetter(groupInd, currLetterInd);
	}

	/**
	 * Called when input is correct. Moves on.
	 */
	private void prepNextLetter() {
		currentBrailleCode = 0;
		attemptNum = 1;
		countLetterInd++;
		if (countLetterInd >= letters[groupInd].length) {
			if (!introducing) {
				// Go to next group if done with testing mode
				groupInd++;
				if (groupInd >= letters.length) {
					// The real game doesn't end... what should we do?
					groupInd = 0;
					introducing = true;
					countLetterInd = 0;
					currLetterInd = 0;
					attemptNum = 0;
					expectedBrailleCode = braille
							.get(letters[groupInd][currLetterInd]);
					instructionSpellLetter(groupInd, currLetterInd);
					return;
				}
			}
			// Regardless of mode, at end, need to reset ind, and flip boolean
			countLetterInd = 0;
			currLetterInd = 0;
			introducing = !introducing;
		}

		// Grab letter from random array if in testing mode
		if (!introducing) {
			currLetterInd = shuffledIndices[groupInd][countLetterInd];
			instructionTestLetter(groupInd, currLetterInd);
		}

		// Grab letter in order if in introducing mode
		else {
			currLetterInd = countLetterInd;
			instructionSpellLetter(groupInd, currLetterInd);
		}
		expectedBrailleCode = braille.get(letters[groupInd][currLetterInd]);
	}

	/**
	 * Called when incorrect. updates attemptNums
	 */
	private void redoCurrLetter() {
		currentBrailleCode = 0;
		attemptNum++;
		if (introducing || attemptNum >= 3) {
			instructionSpellLetter(groupInd, currLetterInd);
		} else if (introducing) {
			instructionTestLetter(groupInd, currLetterInd);
		}
		// Note: In test mode, don't spell out until 3rd attempt
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

		filenames.add(getResources().getString(R.string.to_write_the_letter));
		filenames.add(((Character) let).toString());
		filenames.add(getResources().getString(R.string.please_press));
		String[] buttons = btnStrBuf.toString().split(" ");
		for (int i = 0; i < buttons.length; i++)
			filenames.add(numbers[Integer.parseInt(buttons[i]) - 1]);
		playAudio(filenames.get(0));
	}

	/**
	 * Provides instruction "Write letter _"
	 * 
	 * @param groupInd
	 * @param letterInd
	 */
	private void instructionTestLetter(int groupInd, int letterInd) {
		char let = letters[groupInd][letterInd];

		filenames.add(getResources().getString(R.string.please_write));
		filenames.add(((Character) let).toString());
		playAudio(filenames.get(0));
	}

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
}