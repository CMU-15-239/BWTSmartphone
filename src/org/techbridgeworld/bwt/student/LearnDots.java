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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.learn_dots);

		tts = new TextToSpeech(this, this);
		detector = new GestureDetectorCompat(this, new MyGestureListener());

		try {
			context = createPackageContext("org.techbridgeworld.bwt.teacher", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} 

		player = new MediaPlayer();
		dir = context.getFilesDir().getPath().toString();
		currentFile = 0;
		filenames = new ArrayList<String>(); 

		bwt.init();
	}

	@Override
	protected void onStop() {
		if(player != null)
			player.release();
	    super.onStop();
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

	private void regenerate(){
		currentDot = generator.nextInt(6) + 1;
		if(player.isPlaying()) {
			filenames.clear();
			currentFile = 0;
		}
		filenames.add(getResources().getString(R.string.good));
		filenames.add(getResources().getString(R.string.find_dot));
		filenames.add(numbers[currentDot-1]);
		playAudio(filenames.get(0));
	}

	private int getCurrent(){
		return currentDot;
	}

	@Override
	public void onInit(int status) {
		bwt.start();
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");
			else {
				bwt.startTracking();

				//regenerate();
				currentDot = generator.nextInt(6) + 1;
				filenames.add(getResources().getString(R.string.find_dot));
				filenames.add(numbers[getCurrent()-1]);
				playAudio(filenames.get(0));

				DotListener = new GenericEventListener(){

					@Override
					public void eventTriggered(Object arg0, Event arg1) {
						BoardEvent e = (BoardEvent) arg1;
						int trial = e.getDot();
						int goal = getCurrent();
						Log.i("Dot Game", "Just pressed dot " + trial + ". We want dot " + goal + ".");
						if(trial == goal){
							regenerate();
						}
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
				EventManager.registerEventListener(DotListener, BoardEvent.class);

			}
		}
		else
			Log.e("TTS", "Initilization Failed!");
	}

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
					currentFile = 0; 
					filenames.clear();
				}
			}
		});

		player.start();
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