package org.techbridgeworld.bwt.student;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GameActivity extends Activity {

	private MyApplication application;
	private TextToSpeech tts;
	private Button[] buttons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		application = ((MyApplication) getApplicationContext());

		application.prompt = getResources().getString(R.string.game_prompt);
		application.help = getResources().getString(R.string.game_help);
		
		tts = application.myTTS;

		SharedPreferences prefs = application.context.getSharedPreferences("BWT", 0);
		if(prefs.getBoolean("firstRun", true)) {
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
		
		String[] options = new String[4];
		options[0] = getResources().getString(R.string.learn_dots);
		options[1] = getResources().getString(R.string.learn_letters);
		options[2] = getResources().getString(R.string.animal_game);
		options[3] = getResources().getString(R.string.hangman);
		
		buttons = new Button[5];
		buttons[0] = (Button) findViewById(R.id.one);
		buttons[1] = (Button) findViewById(R.id.two);
		buttons[2] = (Button) findViewById(R.id.three);
		buttons[3] = (Button) findViewById(R.id.four);
		
		for(int i = 0; i < options.length; i++) {
			final int j = i; 
			buttons[i].setText(options[i]);
			buttons[i].setContentDescription(options[i]);
			buttons[i].setVisibility(View.VISIBLE);
			buttons[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent; 
					switch(j) {
					case 0:
						tts.stop();
						intent = new Intent(GameActivity.this, LearnDots.class);
						startActivity(intent);
						break;
					case 1:
						tts.stop();
						intent = new Intent(GameActivity.this, LearnLetters.class);
						startActivity(intent);
						break;
					case 2:
						tts.stop();
						intent = new Intent(GameActivity.this, AnimalGame.class);
						startActivity(intent);
						break;
					case 3:
						tts.stop();
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