	package org.techbridgeworld.bwt;
	
	import java.util.Locale;

import org.techbridgeworld.bwtApi.BWT;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
	
	public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

	    private static final int SWIPE_MIN_DISTANCE = 120;
	    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	    private GestureDetectorCompat detector; 

		private final BWT bwt = new BWT(this, MainActivity.this);
		
		private TextView teacher_welcome;
		private TextToSpeech tts;
		
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.teacher_welcome);
	        
	        bwt.init();
	        tts = new TextToSpeech(this, this);
	        teacher_welcome = (TextView) findViewById(R.id.teacher_welcome);
	        detector = new GestureDetectorCompat(this, new MyGestureListener());
	    }
	    
	    @Override 
	    public boolean onTouchEvent(MotionEvent event){ 
	        this.detector.onTouchEvent(event);
	        return super.onTouchEvent(event);
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        bwt.stop();
	    }
	    
	    @Override
	    protected void onResume() {	    	
	        super.onResume();	        
	        bwt.start();
	    }
	    
	    @Override
	    public void onInit(int status) {
	 
	        if (status == TextToSpeech.SUCCESS) {
	 
	            int result = tts.setLanguage(Locale.US);
	 
	            if (result == TextToSpeech.LANG_MISSING_DATA
	                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	                Log.e("TTS", "This Language is not supported");
	            } else {
	                speakOut();
	            }
	 
	        } 
	        else {
	            Log.e("TTS", "Initilization Failed!");
	        }
	    }
	    
	    private void speakOut() {
	        String text = teacher_welcome.getText().toString();
	        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	    }
	    
	    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
	    	
	        @Override
	        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {

	        	// Swipe up
                if (event1.getY() - event2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            		//Intent intent = new Intent(MainActivity.this, LanguageActivity.class);
            		//startActivity(intent);
                	Log.i("neha", "up");
                }
                
                // Swipe down
                else if (event2.getY() - event1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            		//Intent intent = new Intent(MainActivity.this, LanguageActivity.class);
            		//startActivity(intent);
                	Log.i("neha", "down");
                }
                // Swipe left
                else if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            		//Intent intent = new Intent(MainActivity.this, LanguageActivity.class);
            		//startActivity(intent);
                	Log.i("neha", "left");
                }
                // Swipe right
                else {
                	//Intent intent = new Intent(MainActivity.this, LanguageActivity.class);
            		//startActivity(intent);
                	Log.i("neha", "right");
                }
	            
	            return true;
	        }
	    }
	}
