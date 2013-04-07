package org.techbridgeworld.bwt.student;

import java.util.Date;
import java.util.Random;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.SubmitEvent;
import org.techbridgeworld.bwt.api.libs.Braille;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;

public class AnimalGame extends Activity {

	private MyApplication application;
	private TextToSpeech tts;

	private Random generator = new Random(new Date().getTime());

	private final BWT bwt = new BWT(this, AnimalGame.this);
	private final Braille braille = new Braille();
	private GenericEventListener AnimalListener;

	private final String[] animals = { "bee", "camel", "cat", "cow", "dog", "horse",
			"pig", "rooster", "sheep", "zebra" };
	
	private final int ANIM_SOUND_STAGE = 0;
	private final int SPELL_ANIM_STAGE = 1;
	private final int GIVE_DOTS_STAGE = 2;
	
	private final int MAX_WRONG = 3;
	
	private int stage;
	private int wrongCounter;
	
	private String currAnimal = "";
	private int currLetterInd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		
		application = ((MyApplication) getApplicationContext());
		tts = application.myTTS;

		application.currentFile = 0;
		application.filenames.clear();
		
		//Initialize bwt connection
		bwt.init();
		bwt.start();
		bwt.initializeEventListeners();
		bwt.startTracking();
		runGame();
	}
	
	@Override
	public void onPause() {
		application.clearAudio();
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
	
	private String getCurr() {
		return currAnimal;
	}

	private void regenerate() {
		currAnimal = animals[generator.nextInt(animals.length)];
		currLetterInd = 0;
		wrongCounter = 0;
		stage = ANIM_SOUND_STAGE;
	}

	private void runGame() {
		regenerate();
		speakDirections();
		createListeners();
	}
	
	/**
	 * stage ANIM_SOUND: Makes the noise of the animal
	 * stage SPELL_ANIM: Spell out animal
	 * stage GIVE_DOTS : Give dots to make required letter
	 * In a separate function for Replay purposes
	 */
	private void speakDirections() {
		if(stage == ANIM_SOUND_STAGE) {
			application.queueAudio(R.string.please_write_the_name);
			application.queueAudio(getCurr());

			application.playAudio();
			
//			speakOutQueue("Please write the name of the animal that makes the sound ");
//			speakOutQueue(getCurr() + ".");
		}
		else if (stage == SPELL_ANIM_STAGE) {
			application.queueAudio(R.string.please_write);			
			application.playAudio();
//			speakOutQueue("Please write ");
			spellCurrWord();
		}
		else if (stage == GIVE_DOTS_STAGE) {
			char currLetter = getCurr().charAt(currLetterInd);
			int btns = braille.get(currLetter);
			
			application.queueAudio(R.string.to_write_the_letter);
			application.queueAudio(((Character)currLetter).toString());
			application.queueAudio(R.string.please_press);
			
//			speakOutQueue("To write the letter ");
//			speakOutQueue(currLetter + ".");
//			speakOutQueue("please press ");
			
			//Speak out dots that represent the letter
			for (int i = 0; i < 6; i++) {
				if ((btns & (1 << i)) > 0) {
					String num = ((Integer)i).toString();
					application.queueAudio(num);
//					speakOutQueue(((Integer)i).toString() + ".");
				}
			}
			application.playAudio();
		}
	}
	
	/**
	 * Spell out the animal for the student
	 */
	private void spellCurrWord() {
		for (int i = 0; i < getCurr().length(); i++) {
			Character let = getCurr().charAt(i);
			application.queueAudio(let.toString());
//			speakOutQueue(getCurr().charAt(i) + ".");
		}
		application.playAudio();
	}
	

	private void createListeners() {

		// Handles the checking and comparing of the expected word vs user input
		AnimalListener = new GenericEventListener() {
			@Override
			public void eventTriggered(Object arg0, Event arg1) {
				bwt.defaultSubmitHandler(arg0, arg1);
				SubmitEvent e = (SubmitEvent) arg1;
				Log.d("Animal Game", "Triggered Submit Event");

//				/** FOR DEBUGGING **/
//
//				String trial = bwt.viewTrackingAsString();
//				
//				Log.d("Animal Game", "Trial viewing: " + trial + "; Goal: "
//						+ goal);
//
//				int cellstate = e.getCellBits();
//				Log.i("Animal Game", "Submitted cell (" + e.getCellInd()
//						+ ") bits: " + Integer.toBinaryString(cellstate));
//				/*********************/

				int cellInd = e.getCellInd();
				char glyphAtCell = bwt.getGlyphAtCell(cellInd);
				bwt.clearTouchedCells();
				
				application.clearAudio();
				
				// Speak out character typed
				if(glyphAtCell == '-') {
					application.queueAudio(R.string.invalid_input);
				}
				else {
					String chStr = ((Character)glyphAtCell).toString();
					application.queueAudio(chStr);
				}
				application.playAudio();

				char expectedChar = getCurr().charAt(currLetterInd);
				
				//Check input against expected char and handle accordingly
				if (glyphAtCell != expectedChar)
					wrongCharacterHandler();
				else
					correctCharacterHandler();
			}
		};
		

		bwt.replaceListener("onSubmitEvent", AnimalListener);
	}

	private void wrongCharacterHandler() {
		application.queueAudio(R.string.no);
		application.playAudio();
		
//		speakOutQueue("No.");
		wrongCounter++;
		
		if(stage == GIVE_DOTS_STAGE) {
			//do nothing special. Repeat same instructions
			speakDirections();
			return;
		}
		
		if(wrongCounter >= MAX_WRONG) {
			if(stage == SPELL_ANIM_STAGE) {
				stage = GIVE_DOTS_STAGE;
			}
			else if(stage == ANIM_SOUND_STAGE) {
				stage = SPELL_ANIM_STAGE;
				application.queueAudio(R.string.the_correct_answer_was);
				application.playAudio();
				spellCurrWord();
			}
			wrongCounter = 0;
		}
		currLetterInd = 0;
		speakDirections();
		
	}
	
	private void correctCharacterHandler() {
		wrongCounter = 0;
		
//		speakOutQueue("Good.");
		
		//Special case: Re-learning how to write a letter
		if(stage == GIVE_DOTS_STAGE) {
			application.queueAudio(R.string.good);
			application.playAudio();
			stage = SPELL_ANIM_STAGE;
			currLetterInd = 0;
			speakDirections();
		}
		else {
			currLetterInd++;
			
			// Finished word, go onto next word
			if (currLetterInd == getCurr().length()) {
				application.queueAudio(R.string.good);
				application.playAudio();				
				bwt.resetBoard();
				regenerate();
				speakDirections();
			}
		}
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
	
	// If the user presses back, go back properly
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			bwt.stopTracking();
			bwt.removeEventListeners();
	        bwt.stop();
	        
	        Intent intent = new Intent(AnimalGame.this, GameActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}