package org.techbridgeworld.bwt.student;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.SubmitEvent;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;

public class Hangman extends Activity {

	private MyApplication application;
	private TextToSpeech tts;
	private MediaPlayer player;
	
	private Random generator = new Random(new Date().getTime());

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

		application = ((MyApplication) getApplicationContext());
		tts = application.myTTS;
		player = application.myPlayer;
		
		application.currentFile = 0;
		application.filenames.clear();
		
		bwt.init();
		bwt.start();
		bwt.initializeEventListeners();
		bwt.startTracking();
		runGame();
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
		application.queueAudio(R.string.the_new_word);
		application.queueAudio(((Integer)numLetters).toString());
		application.queueAudio(R.string.letters);
//		speakOutQueue("The new word has " + numLetters + " letters.");
		
		wordStatus = new char[numLetters];
		for (int i = 0; i < numLetters; i++) {
			wordStatus[i] = '-';
			application.queueAudio(R.string.dash);
//			speakOutQueue("Dash.");
		}
		application.queueAudio(R.string.guess_a_letter);
//		speakOutQueue("Guess a letter.");
		
		application.playAudio();
		
		//swap strings in array; everything before currWordInd have been done
		wordBank[nextWordInd] = wordBank[currWordInd];
		wordBank[currWordInd] = currWord;
	}
	
	private void spellWordStatus() {
		for (int i = 0; i < currWord.length(); i++) {
			Character c = wordStatus[i];
			if (c == '-') {
				application.queueAudio(R.string.dash);
//				speakOutQueue("Dash.");
			}
			else {
				application.queueAudio(c.toString());
//				speakOutQueue(c + ". ");
			}
		}
		application.playAudio();
	}
	
	/**
	 * Called when game is ready for next letter to be guessed.
	 * Alerts user of current word status, and number of mistakes made.
	 */
	private void promptGuess() {
		application.queueAudio(R.string.so_far);
//		speakOutQueue("So far the word is ");
		spellWordStatus();
		
		application.queueAudio(R.string.youve_made);
		if(numMistakes == 1) {
			application.queueAudio(R.string.one);
			application.queueAudio(R.string.mistake);
//			speakOutQueue("You've made " + numMistakes + " mistake.");
		}

		else {
			application.queueAudio(((Integer)numMistakes).toString());
			application.queueAudio(R.string.mistakes);
//			speakOutQueue("You've made " + numMistakes + " mistakes.");
		}
		
		//Warning of last chance.
		if(numMistakes == MAX_MISTAKES - 1) {
			application.queueAudio(R.string.but_you_have);
//			speakOutQueue("But you have one last chance to guess the entire word");
		}
			
		application.queueAudio(R.string.guess_a_letter);
//		speakOutQueue("Guess a letter.");
		
		application.playAudio();
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
			application.queueAudio(R.string.good);
			application.playAudio();
//			speakOutQueue("Good.");
			
			revealCurrWord();
			regenerate();
		}
		else {
			promptGuess();
		}		
	}
	
	private void revealCurrWord() {
		application.queueAudio(R.string.the_correct_answer_was);
//		speakOutQueue("The correct word was ");
		for (int i = 0; i < currWord.length(); i++) {
			Character ch = currWord.charAt(i);
			application.queueAudio(ch.toString());
//			speakOutQueue(currWord.charAt(i) + ".");
		}
		application.playAudio();
				
	}
	
	private void wrongGuessHandler() {
		numMistakes++;
		application.queueAudio(R.string.no);
		application.playAudio();
		
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

				application.clearAudioQueue();
				
				//Input wasn't a Braille character
				if(glyphAtCell == '-') {
					application.queueAudio(R.string.invalid_input);
					application.playAudio();
//					speakOutReplace("Invalid input.");
					wrongGuessHandler();
					return;
				}
				
				application.queueAudio(((Character)glyphAtCell).toString());
				application.playAudio();
//				speakOutReplace(glyphAtCell + ". ");
				
				if(guessedBank.contains(glyphAtCell)) {
					application.queueAudio(R.string.youve_already);
					application.playAudio();
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
	
	// If the user presses back, go to the home screen
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(Hangman.this, GameActivity.class);
			bwt.stopTracking();
			bwt.removeEventListeners();
	        bwt.stop();
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}