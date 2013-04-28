package org.techbridgeworld.bwt.student;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

/**
 * MyApplication initializes the TTS and media player. It also obtains
 * information from the server (hangmanWords through http request).
 * MyApplication is created before any activity in the application.
 * 
 * @author neharathi
 */
public class MyApplication extends Application implements OnInitListener {

	public Context context;

	// Variables for text-to-speech
	public TextToSpeech myTTS;
	HashMap<String, String> params;

	// Variables used for playing audio files
	public MediaPlayer myPlayer;
	public String dir;
	public int currentFile;
	public ArrayList<String> filenames;

	// The text given upon opening an activity
	public String prompt;

	// The text given upon shaking the phone
	public String help;

	// The IP address of the server
	public final String SERVER_ADDRESS = "http://128.237.196.208:3000";

	// Contains the Hangman words from the server
	public ArrayList<String> hangmanWords;

	@Override
	public void onCreate() {
		// Initializes TTS and media player variables
		myTTS = new TextToSpeech(this, this);
		params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId");

		myPlayer = new MediaPlayer();
		currentFile = 0;
		filenames = new ArrayList<String>();

		// Check for teacher app
		context = null;
		try {
			context = createPackageContext("org.techbridgeworld.bwt.teacher",
					MODE_PRIVATE);
			dir = context.getFilesDir().getPath().toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		new HTTPAsyncTask().execute();
	}

	@Override
	// Initialize the TTS
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
				@Override
				public void onDone(String utteranceId) {
					// Stops the TTS and finishes up the media player files
					if (utteranceId.equals("utteranceId")) {
						myTTS.stop();
						if (currentFile < filenames.size() - 1) {
							currentFile++;
							playAudio(filenames.get(currentFile));
						} else {
							filenames.clear();
							currentFile = 0;
						}
					}
				}

				@Override
				public void onError(String utteranceId) {
				}

				@Override
				public void onStart(String utteranceId) {
				}
			});

			// Sets up the TTS
			int result = myTTS.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED)
				Log.e("TTS", "This language is not supported");

			if (prompt != null)
				speakOut(prompt);
		} else
			Log.e("TTS", "Initilization Failed!");
	}

	/**
	 * Use TextToSpeech to speak some text out loud
	 * 
	 * @param text
	 *            (the text)
	 */
	public void speakOut(String text) {
		myTTS.speak(text, TextToSpeech.QUEUE_ADD, params);
	}

	/**
	 * Adds the resourceId to filenames
	 * 
	 * @param resourceId
	 *            (i.e. R.string.___)
	 */
	public void queueAudio(int resourceId) {
		filenames.add(getResources().getString(resourceId));
	}

	/**
	 * Adds the string to filenames
	 * 
	 * @param str
	 *            (the string)
	 */
	public void queueAudio(String str) {
		filenames.add(str);
	}

	/**
	 * Clears everything in the audio queue
	 */
	public void clearAudio() {
		myTTS.stop();
		if (myPlayer.isPlaying()) {
			myPlayer.stop();
		}
		filenames.clear();
		currentFile = 0;
	}

	/**
	 * Begin playing the audio files in filenames
	 */
	public void playAudio() {
		if (filenames.isEmpty())
			return;
		playAudio(filenames.get(0));
	}

	/**
	 * Play a certain audio file
	 * 
	 * @param filename
	 *            (the audio file)
	 */
	public void playAudio(String filename) {
		FileInputStream fis;
		Log.i("Audio", "playing " + filename + ".");
		try {
			filename = filename.replaceAll(" ", "_");
			fis = new FileInputStream(dir + "/" + filename + ".m4a");
			Log.i("Audio", "Could find file " + filename + ".m4a.");
			myPlayer.reset();
			myPlayer.setDataSource(fis.getFD());
			fis.close();
			myPlayer.prepare();
			myPlayer.start();
			myPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					myPlayer.stop();
					if (currentFile < filenames.size() - 1) {
						currentFile++;
						playAudio(filenames.get(currentFile));
					} else {
						filenames.clear();
						currentFile = 0;
					}
				}
			});
		} catch (FileNotFoundException e) {
			Log.w("Audio", "Could not find file " + filename + ".m4a.");
			filename = filename.replaceAll("_", " ");
			speakOut(filename);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * HTTPAsyncTask populates hangmanWords on a background thread. To do this,
	 * it makes a POST request to the server with the admin's credentials. The
	 * server sends back a response containing the Hangman words.
	 * 
	 * @author neharathi
	 */
	public class HTTPAsyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// Initialize HTTP Post
			HttpPost post = new HttpPost(SERVER_ADDRESS + "/login");

			// Initialize the POST parameters
			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
			nameValuePair.add(new BasicNameValuePair("username", "admin"));
			nameValuePair.add(new BasicNameValuePair("password", "admin"));

			// URL encode the POST parameters
			try {
				post.setEntity(new UrlEncodedFormEntity(nameValuePair));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			HttpResponse response = null;
			hangmanWords = new ArrayList<String>();
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet();
				get.setURI(new URI(SERVER_ADDRESS + "/words"));
				response = client.execute(get);
				populateHangmanWords(response.getEntity().getContent());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * Populates hangmanWords using the response stream.
	 * 
	 * @param responseStream
	 */
	private void populateHangmanWords(InputStream responseStream) {
		// Convert responseStream to a JSON-encoded string (json)
		String json = "";
		if (responseStream != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(
						responseStream, "UTF-8"), 1024);
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					responseStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			json = writer.toString();
		}

		// Use json to create a JSONArray (hangmanJSON)
		JSONArray hangmanJSON = null;
		try {
			hangmanJSON = new JSONArray(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Use hangmanJSON to populate hangmanWords
		if (hangmanJSON != null) {
			for (int i = 0; i < hangmanJSON.length(); i++) {
				JSONObject row;
				try {
					row = hangmanJSON.getJSONObject(i);
					hangmanWords.add(row.getString("word"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

}