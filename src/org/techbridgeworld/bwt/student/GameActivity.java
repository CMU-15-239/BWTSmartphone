package org.techbridgeworld.bwt.student;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GameActivity extends Activity {

	// The global application
	private MyApplication application;

	// Speaks text aloud
	private TextToSpeech tts;

	// The UI buttons
	private Button[] buttons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		// Get the global application
		application = ((MyApplication) getApplicationContext());

		// Retrieve the global objects from application
		tts = application.myTTS;

		// Set the prompt and help text
		application.prompt = getResources().getString(R.string.game_prompt);
		application.help = getResources().getString(R.string.game_help);

		// Speak the prompt text aloud
		application.speakOut(application.prompt);
		
		/*
		 * If the teacher app has not yet been opened, provide the user with
		 * an AlertDialog indicating that the teacher app must be opened
		 * first.
		 */
		if(application.context == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					GameActivity.this);
			builder.setMessage(R.string.install_message).setPositiveButton(
					R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
							Process.killProcess(Process.myPid());
						}
					});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		else {
			SharedPreferences prefs = application.context.getSharedPreferences("BWT",
					MODE_PRIVATE);
			if (prefs.getBoolean("firstRun", true)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						GameActivity.this);
				builder.setMessage(R.string.open_message).setPositiveButton(
						R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								finish();
								Process.killProcess(Process.myPid());
							}
						});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		}

		// Create an array containing the game options
		String[] options = new String[4];
		options[0] = getResources().getString(R.string.learn_dots);
		options[1] = getResources().getString(R.string.learn_letters);
		options[2] = getResources().getString(R.string.animal_game);
		options[3] = getResources().getString(R.string.hangman);

		// Initialize buttons such that it has the same length as options
		buttons = new Button[5];
		buttons[0] = (Button) findViewById(R.id.one);
		buttons[1] = (Button) findViewById(R.id.two);
		buttons[2] = (Button) findViewById(R.id.three);
		buttons[3] = (Button) findViewById(R.id.four);

		/*
		 * For each option, set the corresponding buttons' text and content
		 * description to that option and visibility to true.
		 */
		for(int i = 0; i < options.length; i++) {
			final int j = i; 
			buttons[i].setText(options[i]);
			buttons[i].setContentDescription(options[i]);
			buttons[i].setVisibility(View.VISIBLE);
			
			/*
			 * When a button is clicked, set application's game variable
			 * accordingly and start specified game activity.
			 */
			buttons[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent; 
					switch(j) {
					case 0:
						intent = new Intent(GameActivity.this, LearnDots.class);
						startActivity(intent);
						break;
					case 1:
						intent = new Intent(GameActivity.this, LearnLetters.class);
						startActivity(intent);
						break;
					case 2:
						intent = new Intent(GameActivity.this, AnimalGame.class);
						startActivity(intent);
						break;
					case 3:
						intent = new Intent(GameActivity.this, Hangman.class);
						startActivity(intent);
						break;
					default:	
					}
				}
			}); 
		}
	}

	@Override
	public void onPause() {
		application.clearAudio();
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	// If the user presses back, go to the home screen
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}