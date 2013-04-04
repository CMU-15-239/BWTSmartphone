package org.techbridgeworld.bwt.student;

import java.util.Date;
import java.util.Locale;
import java.util.Random;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.SubmitEvent;
import org.techbridgeworld.bwt.api.libs.Braille;
import org.techbridgeworld.bwt.student.libs.FlingHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

public class AnimalGame extends Activity implements TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	private GestureDetectorCompat detector;
	
	private Random generator = new Random(new Date().getTime());

	private TextView animal_game;

	private String[] options;
	private int numOptions = 2;
	private int currentOption = 0;

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
		setContentView(R.layout.animal_game);

		options = new String[numOptions];
		options[0] = getResources().getString(R.string.replay);
		options[1] = getResources().getString(R.string.delete);

		animal_game = (TextView) findViewById(R.id.animal_game);

		tts = new TextToSpeech(this, this);
		detector = new GestureDetectorCompat(this, new MyGestureListener());

		bwt.init();
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

	private String getCurr() {
		return currAnimal;
	}

	private void regenerate() {
		currAnimal = animals[generator.nextInt(animals.length)];
		currLetterInd = 0;
		wrongCounter = 0;
		stage = ANIM_SOUND_STAGE;
		
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
			speakOutQueue("Please write the name of the animal that makes the sound ");
			speakOutQueue(getCurr() + ".");
		}
		else if (stage == SPELL_ANIM_STAGE) {
			speakOutQueue("Please write ");
			spellCurrWord();
		}
		else if (stage == GIVE_DOTS_STAGE) {
			char currLetter = getCurr().charAt(currLetterInd);
			int btns = braille.get(currLetter);
			
			speakOutQueue("To write the letter ");
			speakOutQueue(currLetter + ".");
			speakOutQueue("please press ");
			
			//Speak out dots that represent the letter
			for (int i = 0; i < 6; i++) {
				if ((btns & (1 << i)) > 0) {
					speakOutQueue(((Integer)i).toString() + ".");
				}
			}
		}
	}
	
	/**
	 * Spell out the animal for the student
	 */
	private void spellCurrWord() {
		for (int i = 0; i < getCurr().length(); i++) {
			speakOutQueue(getCurr().charAt(i) + ".");
		}
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

				// Speak out character typed
				if(glyphAtCell == '-') {
					speakOutReplace("Invalid Input.");
				}
				else {
					speakOutReplace(glyphAtCell + ".");
				}

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
		speakOutQueue("No.");
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
				speakOutQueue("The correct answer was ");
				spellCurrWord();
			}
			wrongCounter = 0;
		}
		currLetterInd = 0;
		speakDirections();
		
	}
	
	private void correctCharacterHandler() {
		
		//Special case: Re-learning how to write a letter
		if(stage == GIVE_DOTS_STAGE) {
			wrongCounter = 0;
			speakOutQueue("Good.");
			stage = SPELL_ANIM_STAGE;
			currLetterInd = 0;
			speakDirections();
			return;
		}
		
		currLetterInd++;
		
		// Finished word, go onto next word
		if (currLetterInd == getCurr().length()) {
			wrongCounter = 0;
			speakOutQueue("Good.");
			bwt.resetBoard();
			regenerate();
			speakDirections();
		}
	}
	
	
	// Add a string to the text-to-speech queue.
	private void speakOutQueue(String text) {
		tts.speak(text, TextToSpeech.QUEUE_ADD, null);
	}

	// Replace the text-to-speech queue with the given string.
	private void speakOutReplace(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

	// Listen for swipes, and enact the appropriate menu item if necessary.
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

		/**
		 * For replaying the instruction
		 */
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			speakDirections();
			return true;
		}
		
		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2,
				float velocityX, float velocityY) {
			FlingHelper fling = new FlingHelper(event1, event2, velocityX, velocityY);
			// Swipe up
			if (fling.isUp()) {
				Intent intent = new Intent(AnimalGame.this, GameActivity.class);
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
				animal_game.setText(options[currentOption]);
				animal_game.setContentDescription(options[currentOption]);
			}

			// Swipe right (Rotate right through menu items)
			else if (fling.isRight()) {
				currentOption = (currentOption + 1) % numOptions;
				animal_game.setText(options[currentOption]);
				animal_game.setContentDescription(options[currentOption]);
			}

			return true;
		}
	}
}