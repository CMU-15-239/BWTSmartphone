package org.techbridgeworld.bwt;

import org.techbridgeworld.bwtApi.BWT;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
	
	public class MainActivity extends Activity {
		
		// UI stuff
		private ScrollView scrollView; 
		private TextView textView; 
		
		// BWT object
		private final BWT bwt = new BWT(this, MainActivity.this);
		
		
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        scrollView = (ScrollView) findViewById(R.id.scrollview);
	        textView = (TextView) findViewById(R.id.textview);
	        textView.append("NOTE onCreate, created stuff\n");
	        
	        bwt.init();
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        textView.append("NOTE onPause, about to stopIoManager()\n");
	        
	        bwt.stop();
	    }
	    
	    @Override
	    protected void onResume() {	    	
	        super.onResume();
	        textView.append("NOTE onResume, about to deviceStateChange()\n");
	        
	        bwt.start();
	    }
	    
	    // Used to print simple text to the screen
	    private void display(String text){
	        textView.append(text);
	    	scrollView.smoothScrollTo(0, textView.getBottom());
	    }
	    
	    // Prints simple text + a line break
	    private void displayln(String text){
	        display(text + "\n");
	    }

	}
