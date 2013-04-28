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

/**
 * This activity is called "Animal Game" and prompts students to spell out the
 * name of an animal by providing the sound the animal makes.
 * If the user inputs an incorrect letter 3 times in a row, the stage will
 * change to provide more hints to the user.
 * 
 * Different stages:
 * 		Provide the sound of the animal
 * 		Spell out the name of the animal
 * 		Teach student how to write a letter with dot numbers
 * 
 * @author Jessica
 *
 */
public class AnimalGame extends Activity {

	// The global application
	private MyApplication application;

	// Speaks text aloud
	private TextToSpeech tts;

	// The BWT
	private final BWT bwt = new BWT(this, AnimalGame.this);

	// Contains Braille library for mapping braille input to alphabet
	private static final Braille braille = new Braille();
	
	// Helps to randomly choose an animal to test
	private Random generator = new Random(new Date().getTime());

	// List of all the animals to be tested
	private final String[] animals = { "bee", "camel", "cat", "cow", "dog", "horse",
			"pig", "rooster", "sheep", "zebra" };

	//Keeps track of what stage student is in
	private int stage;
	
	// Helps define what stage student is in
	private final int ANIM_SOUND_STAGE = 0;
	private final int SPELL_ANIM_STAGE = 1;
	private final int GIVE_DOTS_STAGE = 2;
	
	// Maximum number of mistakes before stage changes
	private final int MAX_WRONG = 3;
	
	// Number of incorrect input in a row
	private int wrongCounter;
	
	// Current animal being tested
	private String currAnimal = "";
	
	// Current letter index of the animal name the student is on
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
	
	
	/**
	 * @return string of the wav file of the animal's sound
	 */
	private String getCurrSound() {
		return currAnimal + "_sound";
	}

	/**
	 * Generate the next animal to be written with random generator.
	 * Resets the variables as necessary
	 */
	private void regenerate() {
		currAnimal = animals[generator.nextInt(animals.length)];
		currLetterInd = 0;
		wrongCounter = 0;
		stage = ANIM_SOUND_STAGE;
	}

	/**
	 * Run the Animal Game.
	 * Generate a new word, give first instructions, then continue as 
	 * user inputs expected word
	 */
	private void runGame() {
		regenerate();
		speakDirections();
		//playAudio not called in functions above
		application.playAudio();
		
		createListeners();
	}
	
	/**
	 * stage ANIM_SOUND: Makes the noise of the animal
	 * stage SPELL_ANIM: Spell out animal
	 * stage GIVE_DOTS : Give dots to make required letter
	 * In a separate function for Replay purposes
	 */
	private void speakDirections() {
		//Make the sound of the animal
		if(stage == ANIM_SOUND_STAGE) {
			application.queueAudio(R.string.please_write_the_name);
			application.queueAudio(getCurrSound());
			
		}
		//Spell out the name of the animal
		else if (stage == SPELL_ANIM_STAGE) {
			application.queueAudio(R.string.please_write);
			
			spellCurrWord();
		}
		//Provide dots to write the last incorrect letter for student
		else if (stage == GIVE_DOTS_STAGE) {
			char currLetter = currAnimal.charAt(currLetterInd);
			int btns = braille.get(currLetter);
			
			application.queueAudio(R.string.to_write_the_letter);
			application.queueAudio(((Character)currLetter).toString());
			application.queueAudio(R.string.please_press);
			
			//Speak out dots that represent the letter
			for (int i = 0; i < 6; i++) {
				if ((btns & (1 << i)) > 0) {
					String num = ((Integer)(i+1)).toString();
					application.queueAudio(num);
				}
			}
		}
	}
	
	/**
	 * Spell out the animal (currWord) for the student
	 */
	private void spellCurrWord() {
		application.queueAudio(currAnimal);
		
		for (int i = 0; i < currAnimal.length(); i++) {
			Character let = currAnimal.charAt(i);
			application.queueAudio(let.toString());
		}
	}
	

	/**
	 * Create listener for a SubmitEvent to process every glyph written
	 * and generate new animal sound or instruction based on input
	 */
	private void createListeners() {
		// Handles the checking and comparing of the expected word vs user input
		GenericEventListener AnimalListener = new GenericEventListener() {
			@Override
			public void eventTriggered(Object arg0, Event arg1) {
				bwt.defaultSubmitHandler(arg0, arg1);
				SubmitEvent e = (SubmitEvent) arg1;
				Log.d("Animal Game", "Triggered Submit Event");

				//get finished glyph and reset bits at that cell to be 0
				int cellInd = e.getCellInd();
				char glyphAtCell = bwt.getGlyphAtCell(cellInd);
				bwt.setBitsAtCell(cellInd, 0);
				
				// Speak out character typed
				if(glyphAtCell == '-') {
					application.queueAudio(R.string.invalid_input);
				}
				else {
					String chStr = ((Character)glyphAtCell).toString();
					application.queueAudio(chStr);
				}

				char expectedChar = currAnimal.charAt(currLetterInd);
				
				//Check input against expected char and handle accordingly
				if (glyphAtCell != expectedChar)	wrongCharacterHandler();
				else	correctCharacterHandler();
				
				application.playAudio();
			}
		};
		bwt.replaceListener("onSubmitEvent", AnimalListener);
	}

	/**
	 * Handle wrong character according to stage and number of wrongs
	 * in a row so far
	 */
	private void wrongCharacterHandler() {
		application.queueAudio(R.string.no);
		
		wrongCounter++;
		
		if(stage == GIVE_DOTS_STAGE) {
			//do nothing special. Repeat same instructions
			speakDirections();
			return;
		}
		
		//Increase stage number if committed max_wrong mistakes in a row
		if(wrongCounter >= MAX_WRONG) {
			if(stage == SPELL_ANIM_STAGE) {
				stage = GIVE_DOTS_STAGE;
			}
			else {	//stage == ANIM_SOUND_STAGE
				stage = SPELL_ANIM_STAGE;
				application.queueAudio(R.string.the_correct_answer_was);
				spellCurrWord();
				currLetterInd = 0;
			}
			wrongCounter = 0;
		}
		else {
			currLetterInd = 0;
		}
		speakDirections();
	}
	
	/**
	 * On a correct input, clear wrongCounter and update stage
	 * and instructions as necessary
	 */
	private void correctCharacterHandler() {
		
		//Special case: Re-learning how to write a letter
		if(stage == GIVE_DOTS_STAGE) {
			wrongCounter = 0;
			application.queueAudio(R.string.good);
			stage = SPELL_ANIM_STAGE;
			currLetterInd = 0;
			speakDirections();
		}
		//General case: wait until animal word is completed
		else {
			currLetterInd++;
			
			//SPELL_ANIM case: wrong counter should be kept until student
			//reaches the end of the word
			if(stage != SPELL_ANIM_STAGE || currLetterInd == currAnimal.length()) {
				wrongCounter = 0;
			}
			
			// Finished word, go onto next word
			if (currLetterInd == currAnimal.length()) {
				application.queueAudio(R.string.good);			
				bwt.resetBoard();
				regenerate();
				speakDirections();
			}
		}
	}
	
	/** If the user presses back, go back properly */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			bwt.removeEventListeners();
	        Intent intent = new Intent(AnimalGame.this, GameActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}