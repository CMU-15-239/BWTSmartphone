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

public class GameActivity extends Activity implements TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetectorCompat detector; 
	
	private String home_prompt;
	private TextView student_game;
	
	private String[] options;
	private int numOptions = 3;
	private int currentOption = 0; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student_game);
		
		options = new String[numOptions]; 
		options[0] = getResources().getString(R.string.learn_dots);
		options[1] = getResources().getString(R.string.learn_letters);
		options[2] = getResources().getString(R.string.animal_game);
		
		home_prompt = getResources().getString(R.string.game_prompt);
		student_game = (TextView) findViewById(R.id.student_game);
		
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
			else
				speakOut(home_prompt);
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
				Intent intent = new Intent(GameActivity.this, WelcomeActivity.class);
				startActivity(intent);
			}

			// Swipe down
			else if (event2.getY() - event1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				switch(currentOption) {
					case 0: 
						Intent intent1 = new Intent(GameActivity.this, LearnDots.class);
						startActivity(intent1);
						break;
					case 1:
						Intent intent2 = new Intent(GameActivity.this, LearnLetters.class);
						startActivity(intent2);
						break;
					case 2:
						Intent intent3 = new Intent(GameActivity.this, AnimalGame.class);
						startActivity(intent3);
					default:
						break;
				}
			}
			
			// Swipe left
			else if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				currentOption = (currentOption - 1) % numOptions; 
				if(currentOption == -1) 
					currentOption += numOptions;
				student_game.setText(options[currentOption]);
				student_game.setContentDescription(options[currentOption]);
			}
			
			// Swipe right
			else {
				currentOption = (currentOption + 1) % numOptions; 
				student_game.setText(options[currentOption]);
				student_game.setContentDescription(options[currentOption]);
			}

			return true;
		}
	}
}