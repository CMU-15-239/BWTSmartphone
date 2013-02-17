package org.techbridgeworld.bwt.student;

import java.util.Locale;

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
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetectorCompat detector; 
	
	private TextView animal_game;
	
	private String[] options;
	private int numOptions = 2;
	private int currentOption = 0; 

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
	
	private void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
			// Swipe up
			if (event1.getY() - event2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				Intent intent = new Intent(AnimalGame.this, GameActivity.class);
				startActivity(intent);
			}
			
			// Swipe left
			else if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				currentOption = (currentOption - 1) % numOptions; 
				if(currentOption == -1) 
					currentOption += numOptions;
				animal_game.setText(options[currentOption]);
				animal_game.setContentDescription(options[currentOption]);
			}
			
			// Swipe right
			else {
				currentOption = (currentOption + 1) % numOptions; 
				animal_game.setText(options[currentOption]);
				animal_game.setContentDescription(options[currentOption]);
			}

			return true;
		}
	}
}