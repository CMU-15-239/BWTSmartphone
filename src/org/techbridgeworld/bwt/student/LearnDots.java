package org.techbridgeworld.bwt.student;

import java.util.Locale;
import java.util.Random;
import javaEventing.EventManager;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.BoardEvent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

	@Override
	public void onInit(int status) {
		bwt.start();
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");
			else{
				speakOut("Dots Game!");
				bwt.startTracking();
				while(true){
					int i = generator.nextInt(6) + 1;
					speakOut("Press dot " + i + ".");
					Log.i("Dots game", "Waiting...");
					boolean triggered = EventManager.waitUntilTriggered(BoardEvent.class, 10000); 
					if(triggered){
						String dump = bwt.dumpTrackingAsString();
						Log.i("Dots game", "Dumped '" + dump + "'.");
					}
					
				}
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
				bwt.stop();
				startActivity(intent);
			}

			return true;
		}
	}
}