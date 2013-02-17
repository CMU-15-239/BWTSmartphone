package org.techbridgeworld.bwt.student;

import java.util.Locale;

import org.techbridgeworld.bwt.api.BWT;
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
	private static char[][] letters = {
		{'a','b','c','d','e'},
		{'f','g','h','i','j'},
		{'k','l','m','n','o'},
		{'p','q','r','s','t'},
		{'u','v','w','x','y','z'}};
	private int groupInd;
	private int currLetterInd;

	private String strStart;
	private String instruction1, instruction2;
	private String strPass, strFail;
	private String strTest;

	private static Braille braille = new Braille();
	private BWT bwt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.learn_letters);
		
		tts = new TextToSpeech(this, this);
		detector = new GestureDetectorCompat(this, new MyGestureListener());
		
		bwt.init();
		
		strStart = "Let's learn letters";
		instruction1 = "To write letter ";
		instruction2 = ", press ";
		strTest = "Write letter ";
		strPass = "Good.";
		strFail = "No.";
		
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
		}
		else
			Log.e("TTS", "Initilization Failed!");
	}

    @Override
    protected void onPause() {
        super.onPause();
        
        bwt.stopTracking();
        bwt.stop();
    }
    
    @Override
    protected void onResume() {	    	
        super.onResume();
        
        bwt.start();
        bwt.startTracking();
		runProgram();
    }
    
	private void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
			// Swipe up
			if (event1.getY() - event2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				Intent intent = new Intent(LearnLetters.this, GameActivity.class);
				startActivity(intent);
			}

			return true;
		}
	}
	
	private void runProgram() {
		while (groupInd < letters.length) {
			assignLetter(groupInd, currLetterInd);
			currLetterInd += 1;
		}
	}
	private void assignLetter(int groupInd, int letterInd) {
		char let = letters[groupInd][letterInd];
		int btns = braille.get(let);
		speakOut(instruction1 + let + instruction2 + btns);
		
	}
}