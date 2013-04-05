package org.techbridgeworld.bwt.student;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class MyApplication extends Application implements TextToSpeech.OnInitListener {

	public TextToSpeech myTTS;
	public MediaPlayer myPlayer; 
	
	public Context context; 
	public String dir;
	public int currentFile;
	public ArrayList<String> filenames;
	
	public String prompt, help;
	
	@Override
	public void onCreate () {
		myTTS = new TextToSpeech(this, this); 
		myPlayer = new MediaPlayer();
		
		currentFile = 0;
		filenames = new ArrayList<String>();

		try {
			context = createPackageContext("org.techbridgeworld.bwt.teacher", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		dir = context.getFilesDir().getPath().toString();
	}
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = myTTS.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");
		}
		else
			Log.e("TTS", "Initilization Failed!");
	}
	
	/**
	 * Use TextToSpeech to speak a string out loud
	 * @param text
	 */
	public void speakOut(String text) {
		myTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	/**
	 * Adds the resourceId to the filenames
	 * @param resourceId = R.string.___ (id)
	 */
	public void queueAudio(int resourceId) {
		filenames.add(getResources().getString(resourceId));
	}
	
	public void queueAudio(String str) {
		filenames.add(str);
	}
	
	/**
	 * Clears everything in the audio queue
	 */
	public void clearAudioQueue() {
		if (myPlayer.isPlaying()) {
			filenames.clear();
			currentFile = 0;
		}
	}
	
	public void playAudio() {
		playAudio(filenames.get(0));
	}
	
	/**
	 * Plays audio from a given file
	 * @param filename = file to play audio from. 
	 */
	public void playAudio(String filename) {
		myPlayer.reset();
		
		FileInputStream fis;
		try {
			filename = filename.replaceAll(" ", "_");
			fis = new FileInputStream(dir + "/" + filename + ".m4a");
			myPlayer.setDataSource(fis.getFD());
			fis.close();
			myPlayer.prepare();
		} catch (FileNotFoundException e) {
			speakOut(filename); 
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		myPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				myPlayer.stop(); 
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

		myPlayer.start();
	}

}