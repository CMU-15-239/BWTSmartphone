package org.techbridgeworld.bwt.student;

import java.util.Locale;
import java.util.Random;
import javaEventing.EventManager;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.BoardEvent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class LearnDots extends Activity implements TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetectorCompat detector; 
	private Random generator = new Random(15239);
	
	private final BWT bwt = new BWT(this, LearnDots.this);
	private GenericEventListener DotListener;
	
	private int currDot = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.learn_dots);
		
		tts = new TextToSpeech(this, this);
		detector = new GestureDetectorCompat(this, new MyGestureListener());
		
		bwt.init();
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent event){ 
		this.detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	private void regenerate(){
		currDot = generator.nextInt(6) + 1;
	}
	
	private int getCurr(){
		return currDot;
	}
	
	@Override
	public void onInit(int status) {
		currDot = -1;
		bwt.start();
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");
			else{
				speakOut("Dots Game!");
				bwt.startTracking();

				currDot = generator.nextInt(6) + 1;
				speakOut("Press dot " + getCurr() + ".");
				
				DotListener = new GenericEventListener(){

					@Override
					public void eventTriggered(Object arg0, Event arg1) {
						BoardEvent e = (BoardEvent) arg1;
						int trial = e.getDot();
						int goal = getCurr();
						Log.i("Dot Game", "Just pressed dot " + trial + ". We want dot " + goal + ".");
						if(trial == goal){
							regenerate();
							speakOut("Good. Press dot " + getCurr());

						}
						else{
							speakOut("Dot " + trial + ". No. Press dot " + goal);
						}
					}
				};
				EventManager.registerEventListener(DotListener, BoardEvent.class);
				
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
				Intent intent = new Intent(LearnDots.this, GameActivity.class);
				
				// If we've started the dot listener, remove it since we're done with it.
				if(DotListener != null){
					EventManager.unregisterEventListener(DotListener, BoardEvent.class);
				}
				bwt.stop();
				startActivity(intent);
			}

			return true;
		}
	}
}