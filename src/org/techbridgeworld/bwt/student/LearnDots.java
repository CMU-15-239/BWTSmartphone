package org.techbridgeworld.bwt.student;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import javaEventing.EventManager;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.BoardEvent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class LearnDots extends Activity implements TextToSpeech.OnInitListener {

	private String[] numbers = {"one", "two", "three", "four", "five", "six"};

	private TextToSpeech tts;

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetectorCompat detector; 

	private Random generator = new Random(15239);

	private Context context; 
	private MediaPlayer player;
	private String dir; 
	private int currentFile; 
	private ArrayList<String> filenames;

	private final BWT bwt = new BWT(this, LearnDots.this);
	private GenericEventListener DotListener;

	private int currentDot = -1;

	@Override
	/**
	 * Runs on creation of the new activity.
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.learn_dots); // Initialize view

		// Initialize text to speech and gesture detector.
		tts = new TextToSpeech(this, this);
		detector = new GestureDetectorCompat(this, new MyGestureListener());

		// Attempt to get context of teacher app.
		try {
			context = createPackageContext("org.techbridgeworld.bwt.teacher", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} 

		// Initialize the media player, get the directory of the media files.
		player = new MediaPlayer();
		dir = context.getFilesDir().getPath().toString();
		currentFile = 0;
		filenames = new ArrayList<String>(); 

		// Initialize the BWT connection.
		bwt.init();
	}

	@Override
	protected void onStop() {
		// Stop media player.
		if(player != null)
			player.release();
	    super.onStop();
	}
	
    @Override
    public void onDestroy() {
    	// Stop text-to-speech
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
	
	@Override 
	public boolean onTouchEvent(MotionEvent event){ 
		// Pass any touch events to the detector. 
		this.detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	/**
	 * Get a new random dot, and trigger the associated audio.
	 */
	private void regenerate(){
		currentDot = generator.nextInt(6) + 1;
		if(player.isPlaying()) {
			filenames.clear();
			currentFile = 0;
		}
		// "Good."
		filenames.add(getResources().getString(R.string.good));
		// "Find dot"
		filenames.add(getResources().getString(R.string.find_dot));
		// "[Dot Number]."
		filenames.add(numbers[currentDot-1]);
		playAudio(filenames.get(0));
	}

	 /**
	  * A private getter for currentDot, so the listener can access it.
	  * @return curentDot.
	  */
	private int getCurrent(){
		return currentDot;
	}

	@Override
	public void onInit(int status) {
		// Start the BWT
		bwt.start();
		

		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");

			// If text-to-speech started successfully, and it has a supported language, start the game.
			else { 
				// Start tracking the state of the BWT
				bwt.startTracking();

				// Generate the first dot.
				currentDot = generator.nextInt(6) + 1;
				filenames.add(getResources().getString(R.string.find_dot));
				filenames.add(numbers[getCurrent()-1]);
				playAudio(filenames.get(0));

				// Listener to detect board input.
				DotListener = new GenericEventListener(){

					@Override
					public void eventTriggered(Object arg0, Event arg1) {

						// Cast the given event as a BoardEvent, and get the relevant dot information.
						BoardEvent e = (BoardEvent) arg1;
						int trial = e.getDot();
						int goal = getCurrent();
						Log.i("Dot Game", "Just pressed dot " + trial + ". We want dot " + goal + ".");
						
						// If they pressed the dot, then pick another dot.
						if(trial == goal){
							regenerate();
						}
						
						// Otherwise, tell the user that they are incorrect and repeat the prompt. 
						else{
							if(player.isPlaying()) {
								filenames.clear();
								currentFile = 0;
							}
							filenames.add(getResources().getString(R.string.no));
							filenames.add(getResources().getString(R.string.find_dot));
							filenames.add(numbers[currentDot-1]);
							playAudio(filenames.get(0));
						}
					}
				};
				
				// Start the listener. 
				EventManager.registerEventListener(DotListener, BoardEvent.class);

			}
		}
		else
			Log.e("TTS", "Initilization Failed!");
	}

	/**
	 * Plays audio from a given file
	 * @param filename = file to play audio from. 
	 */
	public void playAudio(String filename) {
		try {
			player.reset();
			FileInputStream fis = new FileInputStream(dir + "/" + filename + ".m4a");
			player.setDataSource(fis.getFD());
			fis.close();
			player.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				player.stop(); 
				if(currentFile < filenames.size() - 1) {
					currentFile++;
					playAudio(filenames.get(currentFile));
				}
				else {
					filenames.clear();
					currentFile = 0;
				}
			}
		});

		player.start();
	}

	/**
	 * Listens for a swipe up to exit the activity.
	 */
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