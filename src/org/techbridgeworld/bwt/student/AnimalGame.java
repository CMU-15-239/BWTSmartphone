package org.techbridgeworld.bwt.student;

import java.util.Locale;
import java.util.Random;

import javaEventing.EventManager;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.BoardEvent;
import org.techbridgeworld.bwt.api.events.ChangeCellEvent;

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
	private Random generator = new Random(15239);

	
	private TextView animal_game;
	
	private String[] options;
	private int numOptions = 2;
	private int currentOption = 0; 
	
	private final BWT bwt = new BWT(this, AnimalGame.this);
	private GenericEventListener AnimalListener, ChangeListener;
	
	private String currAnimal = "";
	private String[] animals = {"bee", "camel", "cat", "cow", "dog", "horse", "pig", "rooster", "sheep", "zebra"};


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
		
		bwt.init();
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent event){ 
		this.detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	private String getCurr(){
		return currAnimal;
	}
	
	private void regenerate(){
		currAnimal = animals[generator.nextInt(10)];
	}

	@Override
	public void onInit(int status) {
		bwt.start();
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");
			else{
				speakOut("Animal Game!");
				bwt.startTracking();
				
				regenerate();
				speakOut("Spell the word " + getCurr() + ".");

				
				AnimalListener = new GenericEventListener(){
					@Override
					public void eventTriggered(Object arg0, Event arg1) {
						BoardEvent e = (BoardEvent) arg1;
						String trial = bwt.viewTrackingAsString();
						String goal = getCurr();
						if(trial == goal){
							regenerate();
							speakOut("Good. Spell the word " + getCurr() + ".");
						}
						else{
							if(bwt.viewTrackingAsString().length() > goal.length()){
								bwt.dumpTrackingAsString();
								speakOut("No. Try again.");
							}
						}
					}
				};
				
				ChangeListener = new GenericEventListener(){
					
					public void eventTriggered(Object arg0, Event arg1){
						ChangeCellEvent e = (ChangeCellEvent) arg1;
						
						char last = bwt.getBoard().getGlyphAtCell(e.getOldCell());
						Log.i("Animal Game","Just typed character " + last + ".");
						speakOut(last + ".");
						
					}
				};
				EventManager.registerEventListener(AnimalListener, BoardEvent.class);
				EventManager.registerEventListener(ChangeListener, ChangeCellEvent.class);
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