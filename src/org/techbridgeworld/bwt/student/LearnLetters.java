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

	private final BWT bwt = new BWT(this, LearnLetters.this);
	private static final Braille braille = new Braille();
	private static final int IS_WRONG = -1;
	private static final int IS_RIGHT = 1;
	private static final int IS_IN_PROGRESS = 0;
	private static final int TIME_LIMIT = 20000;

	private static final String strStart = "Let's learn letters";
	private static final String instruction1 = "To write letter ";
	private static final String instruction2 = ", press ";
	private static final String strTest = "Write letter ";
	private static final String strPass = "Good.";
	private static final String strFail = "No.";
	private static final String strEnd = "Great! You've now learned all the letters!";

	private static final char[][] letters = {
		{'a','b','c','d','e'},
		{'f','g','h','i','j'},
		{'k','l','m','n','o'},
		{'p','q','r','s','t'},
		{'u','v','w','x','y','z'}};
	private int groupInd;
	private int currLetterInd;
	private int currentBrailleCode;
	private int expectedBrailleCode;
	private int userStatus;

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
		        createListeners();
				runProgram();
			}
		}
		else
			Log.e("TTS", "Initilization Failed!");
	}

   
    
	private void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
			// Swipe up
			if (event1.getY() - event2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		        bwt.stopTracking();
		        bwt.stop();
				Intent intent = new Intent(LearnLetters.this, GameActivity.class);
				startActivity(intent);
			}
			return true;
		}
	}

    private void createListeners() {
		Log.i("Learn letters", "Created own listeners...");
    	bwt.replaceListener("onBoardEvent", new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				Log.i("Learn letters", "Event triggered for board...");
				bwt.defaultBoardHandler(sender, event);
				BoardEvent e = (BoardEvent)event;
				currentBrailleCode |= (1 << (e.getDot() - 1));
				
				//((c & e) ^ c) > 0 if extra bits set in c that's not in e 
				boolean isWrong = (((currentBrailleCode & expectedBrailleCode) ^ currentBrailleCode) > 0) ;
				if(currentBrailleCode == expectedBrailleCode) {
					userStatus = IS_RIGHT;
				}
				else if(isWrong) {
					userStatus = IS_WRONG;
				}
				else {
					userStatus = IS_IN_PROGRESS;
				}
			}
    	});
    }
    
	private void runProgram() {
        speakOut(strStart);
		Log.i("Learn letters", "under runProgram()...");
		while (groupInd < letters.length) {
			introduceLetters();
			testLetters();
			groupInd++;			
		}
		speakOut(strEnd);
	}
	
	private void introduceLetters() {
		while(currLetterInd < letters[groupInd].length) {
			currentBrailleCode = 0;
			expectedBrailleCode = letters[groupInd][currLetterInd];
			/*Once numAttempts turns 0, indicates can move on
			 *Keeps track of the current attempt number
			 */
			int attemptNum = 1;

			instructionSpellLetter(groupInd, currLetterInd);
			while (attemptNum > 0) {
				userStatus = IS_WRONG;		//default -- ie: run out of time
				boolean triggered = EventManager.waitUntilTriggered(BoardEvent.class, TIME_LIMIT);
				
				
				if(triggered) {
					//if correct
					if(userStatus == IS_RIGHT) {
						attemptNum = 0;
						currentBrailleCode = 0;
						speakOut(strPass);
					}
					//if wrong
					else if(userStatus == IS_WRONG) {
						attemptNum++;
						currentBrailleCode = 0;
						speakOut(strFail);
						instructionSpellLetter(groupInd, currLetterInd);
					}
				}
			}
			currLetterInd += 1;
		}
	}
	
	private void testLetters() {
		int n = letters[groupInd].length;
		int[] indices = new int[n];
		//Initialize indices
		for (int i = 0; i < n; i++) {
			indices[i] = i;
		}
		indices = shuffleIndicesArr(indices);
		int ind = 0;
		while(ind < indices.length) {
			currLetterInd = indices[ind];
			currentBrailleCode = 0;
			expectedBrailleCode = letters[groupInd][currLetterInd];
			int attemptNum = 1;
			

			instructionTestLetter(groupInd, currLetterInd);
			while(attemptNum > 0) {
				userStatus = IS_WRONG;		//default -- ie: run out of time
				boolean triggered = EventManager.waitUntilTriggered(BoardEvent.class, TIME_LIMIT);
				if(triggered) {
					//if correct
					if(userStatus == IS_RIGHT) {
						attemptNum = 0;
						currentBrailleCode = 0;
						speakOut(strPass);
					}
					//incorrect
					else if (userStatus == IS_WRONG) {
						attemptNum++;
						currentBrailleCode = 0;
						speakOut(strFail);
						if(attemptNum > 2) {
							//After 2nd attempt, show how to spell
							instructionSpellLetter(groupInd, currLetterInd);
						}
					}
				}
			}
			ind++;
		}
	}
	
	private void instructionSpellLetter(int groupInd, int letterInd) {
		char let = letters[groupInd][letterInd];
		int btns = braille.get(let);
		speakOut(instruction1 + let + instruction2 + btns);
		
	}
	private void instructionTestLetter(int groupInd, int letterInd) {
		char let = letters[groupInd][letterInd];
		speakOut(strTest + let);
		
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
}