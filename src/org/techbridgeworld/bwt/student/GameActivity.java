package org.techbridgeworld.bwt.student;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.widget.TextView;

public class GameActivity extends Activity implements TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetectorCompat detector; 
	
	private Context context;
	private String dir;
	private MediaPlayer player;
	
	private String game_prompt;
	private TextView student_game;
	
	private String[] options;
	private int numOptions = 3;
	private int currentOption = 0; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student_game);
		
		// Load game names/strings.
		options = new String[numOptions]; 
		options[0] = getResources().getString(R.string.learn_dots);
		options[1] = getResources().getString(R.string.learn_letters);
		options[2] = getResources().getString(R.string.animal_game);
		
		game_prompt = getResources().getString(R.string.game_prompt);
		student_game = (TextView) findViewById(R.id.student_game);
		
		// Load text-to-speech and gesture listener. 
		tts = new TextToSpeech(this, this);
		detector = new GestureDetectorCompat(this, new MyGestureListener());

		try {
			context = createPackageContext("org.techbridgeworld.bwt.teacher", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		SharedPreferences prefs = context.getSharedPreferences("BWT", 0);
		if(prefs.getBoolean("firstRunWelcome", true)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
			builder.setMessage(R.string.open_message)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_HOME);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
		// Initialize player and audio directory.
		player = new MediaPlayer();
		dir = context.getFilesDir().getPath().toString();
		
		// Trigger audio files to play on hover. 
		student_game.setOnHoverListener(new OnHoverListener() {
			@Override
			public boolean onHover(View v, MotionEvent event) {
				if(!player.isPlaying()) {
					FileInputStream fis;
					try {
						String filename = options[currentOption].replaceAll(" ", "_");
						fis = new FileInputStream(dir + "/" + filename + ".m4a");
						player.reset();
						player.setDataSource(fis.getFD());
						fis.close();
						player.prepare();
						player.start();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace(); 
					}
				}
				
				return true;
			}	
		});
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

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");
			else
				speakOut(game_prompt);
		}
		else
			Log.e("TTS", "Initilization Failed!");
	}
	
	private void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	
	// Gesture listener that changes the activity based on the current option. 
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
			else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				currentOption = (currentOption + 1) % numOptions; 
				student_game.setText(options[currentOption]);
				student_game.setContentDescription(options[currentOption]);
			}

			return true;
		}
	}
}