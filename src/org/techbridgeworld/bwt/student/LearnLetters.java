package org.techbridgeworld.bwt.student;

import java.util.Locale;
import java.util.Random;

import javaEventing.EventManager;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.BoardEvent;
import org.techbridgeworld.bwt.libs.Braille;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class LearnLetters extends Activity implements TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetectorCompat detector; 

	private static final Braille braille = new Braille();
	private static final int IS_WRONG = -1;
	private static final int IS_RIGHT = 1;
	private static final int IS_IN_PROGRESS = 0;
	private static final int LEARN_LETTERS_TIME_LIMIT = 10000;

	private static final String strStart = "Let's learn letters!";
	private static final String instruction1 = "To write letter ";
	private static final String instruction2 = ", press dot ";
	private static final String strTest = "Write letter ";
	private static final String strPass = "Good.";
	private static final String strFail = "No.";
	private static final String strEnd = "Great! You've now learned all the letters! Slide up to go back to menu.";

	private static final char[][] letters = {
		{'a','b','c','d','e'},
		{'f','g','h','i','j'},
		{'k','l','m','n','o'},
		{'p','q','r','s','t'},
		{'u','v','w','x','y','z'}};
	
	private static int[][] shuffledIndices;
	private int groupInd;
	private int currLetterInd;
	private int countLetterInd;
	private int currentBrailleCode;
	private int expectedBrailleCode;
	private int userStatus;
	private int attemptNum;
	private boolean introducing;	//introduce letters if true; test letters if false


	private final BWT bwt = new BWT(this, LearnLetters.this);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.learn_letters);
		
		tts = new TextToSpeech(this, this);
		detector = new GestureDetectorCompat(this, new MyGestureListener());
		bwt.init();
		Log.i("Learn letters", "BWT Inited...");
		
		groupInd = 0;
		currLetterInd = 0;
		
		
		userStatus = IS_IN_PROGRESS;
		attemptNum = 0;
		
		//Shuffle order of indices for testing phase
		shuffleIndices();
	}
	
	private void shuffleIndices() {
		shuffledIndices = new int[letters.length][];
		for(int i = 0; i < letters.length; i++){
			shuffledIndices[i] = new int[letters[i].length];
			for (int j = 0; j < letters[i].length; j++) {
				shuffledIndices[i][j] = j;
			}
		}

		for(int i = 0; i < letters.length; i++){
			shuffledIndices[i] = shuffleIndicesArr(shuffledIndices[i]);
		}
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent event){ 
		this.detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");
			else {
		        bwt.start();
				Log.i("Learn letters", "BWT Started...");
		        bwt.startTracking();
				runProgram();
			}
		}
		else
			Log.e("TTS", "Initilization Failed!");
	}

   
    
	private void speakOutReplace(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	private void speakOutQueue(String text) {
		tts.speak(text, TextToSpeech.QUEUE_ADD, null);
	}
	
	
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
			// Swipe up
			Log.d("Learn letters", "Swipe up occurred");
			if (event1.getY() - event2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		        bwt.stopTracking();
		        bwt.stop();
				Intent intent = new Intent(LearnLetters.this, GameActivity.class);
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
				BoardEvent e = (BoardEvent)event;
				
				/**DEMO PURPOSES**/
				if(e.getCellInd() == -1)
					return;
				/*****************/
				
				currentBrailleCode |= (1 << (e.getDot() - 1));
				Log.i("Learn letters", "Dot input number: " + e.getDot());
				Log.i("Learn letters", "Current input: " + Integer.toBinaryString(currentBrailleCode) + "; " + 
						"Expected input: " + Integer.toBinaryString(expectedBrailleCode));
				//((c & e) ^ c) > 0 if extra bits set in c that's not in e 
				boolean isWrong = (((currentBrailleCode & expectedBrailleCode) ^ currentBrailleCode) > 0);
				if(currentBrailleCode == expectedBrailleCode) {
					userStatus = IS_RIGHT;
					speakOutReplace(strPass);
					prepNextLetter();
					Log.d("Learn letters", "user is correct");
				}
				else if(isWrong) {
					userStatus = IS_WRONG;
					speakOutReplace(strFail);
					redoCurrLetter();
					Log.d("Learn letters", "user is wrong");
				}
				else {
					userStatus = IS_IN_PROGRESS;
					Log.d("Learn letters", "user is still writing");
				}
			}
    	});
    	
    	/**DEMO PURPOSES**/
    	bwt.replaceListener("onAltBtnEvent", new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				bwt.defaultAltBtnHandler(sender, event);
				speakOutReplace(strPass);
				prepNextLetter();
			}
    	});
    	/***************/
    }
    
	private void runProgram() {
        speakOutQueue(strStart);
		groupInd = 0;
		currLetterInd = 0;
		countLetterInd = 0;
		introducing = true;
		currentBrailleCode = 0;
		expectedBrailleCode = braille.get(letters[groupInd][currLetterInd]);

        createListener();
		instructionSpellLetter(groupInd, currLetterInd);
	}
	
	private void prepNextLetter() {
		currentBrailleCode = 0;
		attemptNum = 1;
		countLetterInd++;
		if(countLetterInd >= letters[groupInd].length){
			if(!introducing) {
				//Go to next group if done with testing mode
				groupInd++;
				if(groupInd >= letters.length){
					speakOutReplace(strEnd);
					return;
				}
			}
			//Regardless of mode, at end, need to reset ind, and flip boolean
			countLetterInd = 0;
			currLetterInd = 0;
			introducing = !introducing;
		}

		//Grab letter from random array if in testing mode
		if(!introducing) {
			currLetterInd = shuffledIndices[groupInd][countLetterInd];
			instructionTestLetter(groupInd, currLetterInd);
		}
		
		//Grab letter in order if in introducing mode
		else {
			currLetterInd = countLetterInd;
			instructionSpellLetter(groupInd, currLetterInd);
		}
		expectedBrailleCode = braille.get(letters[groupInd][currLetterInd]);
	}
	
	private void redoCurrLetter() {
		currentBrailleCode = 0;
		attemptNum++;
		if(introducing || attemptNum >= 3) {
			instructionSpellLetter(groupInd, currLetterInd);
		}
		else if (introducing) {
			instructionTestLetter(groupInd, currLetterInd);
		}
		//Note: In test mode, don't spell out until 3rd attempt
	}
	
	private void instructionSpellLetter(int groupInd, int letterInd) {
		char let = letters[groupInd][letterInd];
		int btns = braille.get(let);
		StringBuffer btnStrBuf = new StringBuffer("");
		for (int i = 0; i < 6; i++) {
			if((btns & (1 << i)) > 0) {
				btnStrBuf.append(i+1 + " ");
			}
		}
		speakOutQueue(instruction1 + let + instruction2 + btnStrBuf.toString());
		
	}
	private void instructionTestLetter(int groupInd, int letterInd) {
		char let = letters[groupInd][letterInd];
		speakOutQueue(strTest + let);
		
	}
	
	private int[] shuffleIndicesArr(int[] givenArr) {
		int n = givenArr.length;
		Random r = new Random();
		for(int i = 0; i < n; i++) {
			int randInt = r.nextInt(n);
			int tmp = givenArr[randInt];
			givenArr[randInt] = givenArr[i];
			givenArr[i] = tmp;
		}
		return givenArr;
	}
	
	
	
//	private void introduceLetters() {
//		while(currLetterInd < letters[groupInd].length) {
//			currentBrailleCode = 0;
//			expectedBrailleCode = braille.get(letters[groupInd][currLetterInd]);
//			/*Once numAttempts turns 0, indicates can move on
//			 *Keeps track of the current attempt number
//			 */
//			int attemptNum = 1;
//			instructionSpellLetter(groupInd, currLetterInd);
//
//			while (attemptNum > 0) {
//				userStatus = IS_WRONG;		//default -- ie: run out of time
//				Log.d("Learn letters", "about to call waitUntilTriggered; [groupInd, currLetterInd] = [" + 
//										groupInd + "," + currLetterInd + "]");
//				boolean triggered = EventManager.waitUntilTriggered(BoardEvent.class, LEARN_LETTERS_TIME_LIMIT);
//				Log.d("Learn letters", "just called waitUntilTriggered");
//				
//				
//				if(triggered) {
//					//if correct
//					if(userStatus == IS_RIGHT) {
//						Log.d("Learn letters", "Correct on attempt num " + attemptNum);
//						attemptNum = 0;
//						currentBrailleCode = 0;
//						speakOutReplace(strPass);
//					}
//					//if wrong
//					else if(userStatus == IS_WRONG) {
//						Log.d("Learn letters", "Incorrect; attempt num " + attemptNum);
//						attemptNum++;
//						currentBrailleCode = 0;
//						speakOutReplace(strFail);
//						instructionSpellLetter(groupInd, currLetterInd);
//					}
//				}
//			}
//			currLetterInd += 1;
//		}
//	}
//	
//	private void testLetters() {
//		int n = letters[groupInd].length;
//		int[] indices = new int[n];
//		//Initialize indices
//		for (int i = 0; i < n; i++) {
//			indices[i] = i;
//		}
//		indices = shuffleIndicesArr(indices);
//		int ind = 0;
//		while(ind < indices.length) {
//			currLetterInd = indices[ind];
//			currentBrailleCode = 0;
//			expectedBrailleCode = braille.get(letters[groupInd][currLetterInd]);
//			int attemptNum = 1;
//			
//
//			instructionTestLetter(groupInd, currLetterInd);
//			while(attemptNum > 0) {
//				userStatus = IS_WRONG;		//default -- ie: run out of time
//				boolean triggered = EventManager.waitUntilTriggered(BoardEvent.class, LEARN_LETTERS_TIME_LIMIT);
//				if(triggered) {
//					//if correct
//					if(userStatus == IS_RIGHT) {
//						attemptNum = 0;
//						currentBrailleCode = 0;
//						speakOutReplace(strPass);
//					}
//					//incorrect
//					else if (userStatus == IS_WRONG) {
//						attemptNum++;
//						currentBrailleCode = 0;
//						speakOutReplace(strFail);
//						if(attemptNum > 2) {
//							//After 2nd attempt, show how to spell
//							instructionSpellLetter(groupInd, currLetterInd);
//						}
//					}
//				}
//			}
//			ind++;
//		}
//	}
}