package org.techbridgeworld.bwt.student;

import java.util.Locale;

import org.techbridgeworld.bwt.student.libs.FlingHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class WelcomeActivity extends Activity implements TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	private GestureDetectorCompat detector;
	
	private String welcome_prompt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student_welcome);
		welcome_prompt = getResources().getString(R.string.welcome_prompt);
		
		tts = new TextToSpeech(this, this);
		detector = new GestureDetectorCompat(this, new MyGestureListener());
	}
	
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
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
				speakOut(welcome_prompt);
		}
		else
			Log.e("TTS", "Initilization Failed!");
	}

	private void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent start, MotionEvent end, float velocityX, float velocityY) {
			FlingHelper fling = new FlingHelper(start, end, velocityX, velocityY);
			// Swipe up
			if (fling.isUp()) {
				speakOut("Please rotate the phone 180 degrees.");
			}

			// Swipe down
			else if (fling.isDown()) {
		        tts.stop();
		        tts.shutdown();
				Intent intent = new Intent(WelcomeActivity.this, GameActivity.class);
				startActivity(intent);
			}
			
			// Swipe left
			else if (fling.isLeft()) {
				speakOut("Please rotate the phone 90 degrees counter-clockwise.");
			}
			
			// Swipe right
			else if (fling.isRight()) {
				speakOut("Please rotate the phone 90 degrees clockwise.");
			}

			return true;
		}
	}
}
