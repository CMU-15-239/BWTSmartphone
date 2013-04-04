package org.techbridgeworld.bwt.student;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.SubmitEvent;
import org.techbridgeworld.bwt.student.libs.FlingHelper;

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

public class Hangman extends Activity implements TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	private GestureDetectorCompat detector;

	private Context context;
	private MediaPlayer player;
	private String dir;
	private int currentFile;
	private ArrayList<String> filenames;
	
	private Random generator = new Random(new Date().getTime());

	private TextView hangman;

	private String[] options;
	private int numOptions = 2;
	private int currentOption = 0;

	private final BWT bwt = new BWT(this, Hangman.this);
	private GenericEventListener HangmanListener;

	private final String[] wordBank = { "fast", "dash", "quit", "milk", "computer", "money",
			"dishes", "phone", "school", "teacher"};
	private final int MAX_MISTAKES = 8;
	
	private String currWord = "";
	private int currWordInd;
	private int numCorrectLetters;
	private char[] wordStatus;
	private ArrayList<Character> guessedBank;
	private int numMistakes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hangman);

		options = new String[numOptions];
		options[0] = getResources().getString(R.string.replay);
		options[1] = getResources().getString(R.string.delete);

		hangman = (TextView) findViewById(R.id.hangman);

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
	}

	@Override
	protected void onStop() {
		// Stop media player.
		if(player != null)
			player.release();
	    super.onStop();
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	
	@Override
	public void onInit(int status) {
		bwt.start();
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");
			else{
				bwt.initializeEventListeners();
				bwt.startTracking();
				
				runGame();
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


	/**
	 * Clears everything in the audio queue
	 */
	private void clearAudioQueue() {
		if (player.isPlaying()) {
			filenames.clear();
			currentFile = 0;
		}
	}
	
	/**
	 * Adds the resourceId to the filenames
	 * @param resourceId = R.string.___ (id)
	 */
	private void queueAudio(int resourceId) {
		filenames.add(getResources().getString(resourceId));
	}
	private void queueAudio(String str) {
		filenames.add(str);
	}
	
	
	private void runGame() {
		currWordInd = -1;
		regenerate();

		createListeners();
	}

	private void regenerate() {
		currWordInd++;
		numMistakes = 0;
		guessedBank = new ArrayList<Character>();
		
		if(currWordInd >= wordBank.length) {
//			speakOutQueue("Congratulations! You've beat hangman! Starting Over.");
			//start over if they beat the game
			runGame();
			return;
		}

		//Won't repeat words already done
		int nextWordInd = generator.nextInt(wordBank.length - currWordInd) + currWordInd;
		currWord = wordBank[nextWordInd];
		numCorrectLetters = 0;
		
		int numLetters = currWord.length();
		queueAudio(R.string.the_new_word);
		queueAudio(((Integer)numLetters).toString());
		queueAudio(R.string.letters);
//		speakOutQueue("The new word has " + numLetters + " letters.");
		
		wordStatus = new char[numLetters];
		for (int i = 0; i < numLetters; i++) {
			wordStatus[i] = '-';
			queueAudio(R.string.dash);
//			speakOutQueue("Dash.");
		}
		queueAudio(R.string.guess_a_letter);
//		speakOutQueue("Guess a letter.");
		
		playAudio(filenames.get(0));
		
		//swap strings in array; everything before currWordInd have been done
		wordBank[nextWordInd] = wordBank[currWordInd];
		wordBank[currWordInd] = currWord;
	}
	
	private void spellWordStatus() {
		for (int i = 0; i < currWord.length(); i++) {
			Character c = wordStatus[i];
			if (c == '-') {
				queueAudio(R.string.dash);
//				speakOutQueue("Dash.");
			}
			else {
				queueAudio(c.toString());
//				speakOutQueue(c + ". ");
			}
		}
		playAudio(filenames.get(0));
	}
	
	/**
	 * Called when game is ready for next letter to be guessed.
	 * Alerts user of current word status, and number of mistakes made.
	 */
	private void promptGuess() {
		queueAudio(R.string.so_far);
//		speakOutQueue("So far the word is ");
		spellWordStatus();
		
		queueAudio(R.string.youve_made);
		if(numMistakes == 1) {
			queueAudio(R.string.one);
			queueAudio(R.string.mistake);
//			speakOutQueue("You've made " + numMistakes + " mistake.");
		}

		else {
			queueAudio(((Integer)numMistakes).toString());
			queueAudio(R.string.mistakes);
//			speakOutQueue("You've made " + numMistakes + " mistakes.");
		}
			
		
		//Warning of last chance.
		if(numMistakes == MAX_MISTAKES - 1) {
			queueAudio(R.string.but_you_have);
//			speakOutQueue("But you have one last chance to guess the entire word");
		}
			
		queueAudio(R.string.guess_a_letter);
//		speakOutQueue("Guess a letter.");
		
		playAudio(filenames.get(0));
	}
	
	/**
	 * If guessed letter is correct:
	 * - update wordStatus
	 * - if word is now complete, move onto next word
	 * - otherwise, re-prompt
	 */
	private void correctGuessHandler(char guessedLetter) {

		guessedBank.add(guessedLetter);
		
		//update wordStatus
		int start = 0;
		while (start >= 0 && start < currWord.length()) {
			int index = currWord.indexOf(guessedLetter, start);
			if(index < 0) break;

			numCorrectLetters++;
			wordStatus[index] = guessedLetter;
			start = index + 1;
		}
		
		//Move onto next word if all the letters are there
		if(numCorrectLetters == currWord.length()) {
			queueAudio(R.string.good);
			playAudio(filenames.get(0));
//			speakOutQueue("Good.");
			
			revealCurrWord();
			regenerate();
		}
		else {
			promptGuess();
		}		
	}
	
	private void revealCurrWord() {
		queueAudio(R.string.the_correct_answer_was);
//		speakOutQueue("The correct word was ");
		for (int i = 0; i < currWord.length(); i++) {
			Character ch = currWord.charAt(i);
			queueAudio(ch.toString());
//			speakOutQueue(currWord.charAt(i) + ".");
		}
		playAudio(filenames.get(0));
				
	}
	
	private void wrongGuessHandler() {
		numMistakes++;
		queueAudio(R.string.no);
		playAudio(filenames.get(0));
		
//		speakOutQueue("No.");
		
		//Reached max number of mistakes, move onto new word
		if(numMistakes == MAX_MISTAKES) {
			revealCurrWord();
			regenerate();
		}
		//Can still guess another letter
		else {
			promptGuess();	
		}
	}


	private void createListeners() {

		// Handles the checking and comparing of the expected word vs user input
		HangmanListener = new GenericEventListener() {
			@Override
			public void eventTriggered(Object arg0, Event arg1) {
				bwt.defaultSubmitHandler(arg0, arg1);
				SubmitEvent e = (SubmitEvent) arg1;

				int cellInd = e.getCellInd();
				char glyphAtCell = bwt.getGlyphAtCell(cellInd);
				bwt.clearTouchedCells();

				clearAudioQueue();
				
				//Input wasn't a Braille character
				if(glyphAtCell == '-') {
					queueAudio(R.string.invalid_input);
					playAudio(filenames.get(0));
//					speakOutReplace("Invalid input.");
					wrongGuessHandler();
					return;
				}
				
				queueAudio(((Character)glyphAtCell).toString());
				playAudio(filenames.get(0));
//				speakOutReplace(glyphAtCell + ". ");
				
				if(guessedBank.contains(glyphAtCell)) {
					queueAudio(R.string.youve_already);
					playAudio(filenames.get(0));
					promptGuess();
					return;
				}
				else guessedBank.add(glyphAtCell);
				
				// Guesses incorrectly
				if (currWord.indexOf(glyphAtCell) < 0) {
					wrongGuessHandler();
				}
				// Guesses correctly
				else {
					correctGuessHandler(glyphAtCell);
				}
			}
		};
		

		bwt.replaceListener("onSubmitEvent", HangmanListener);
	}

//	// Add a string to the text-to-speech queue.
//	private void speakOutQueue(String text) {
//		tts.speak(text, TextToSpeech.QUEUE_ADD, null);
//	}
//
//	// Replace the text-to-speech queue with the given string.
//	private void speakOutReplace(String text) {
//		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//	}

	// Listen for swipes, and enact the appropriate menu item if necessary.
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

		/**
		 * For replaying the instruction
		 */
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			promptGuess();
			return true;
		}
		
		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2,
				float velocityX, float velocityY) {
			FlingHelper fling = new FlingHelper(event1, event2, velocityX, velocityY);
			// Swipe up
			if (fling.isUp()) {
				Intent intent = new Intent(Hangman.this, GameActivity.class);
				bwt.stopTracking();
				bwt.removeEventListeners();
		        bwt.stop();
				startActivity(intent);
			}

			// Swipe left (Rotate left through menu items)
			else if (fling.isLeft()) {
				currentOption = (currentOption - 1) % numOptions;
				if (currentOption == -1)
					currentOption += numOptions;
				hangman.setText(options[currentOption]);
				hangman.setContentDescription(options[currentOption]);
			}

			// Swipe right (Rotate right through menu items)
			else if (fling.isRight()) {
				currentOption = (currentOption + 1) % numOptions;
				hangman.setText(options[currentOption]);
				hangman.setContentDescription(options[currentOption]);
			}

			return true;
		}
	}
}