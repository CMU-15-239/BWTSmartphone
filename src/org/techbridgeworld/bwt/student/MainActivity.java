package org.techbridgeworld.bwt.student;

import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.BoardEvent;
import org.techbridgeworld.bwt.api.events.ChangeCellEvent;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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
    
    private void createListeners() {
    	bwt.addListener("onBoardEvent", new GenericEventListener() {
    			@Override
    			public void eventTriggered(Object sender, Event event) {
    				BoardEvent e = (BoardEvent)event;
		    		Log.i("Jessica", "Triggered own ONBOARD event, message: " + e.getMessage());
		    		
		    		//RESULT: Warning of CalledFromWrongThreadException: only the
		    		//original thread that created a view hierarchy
		    		//displayln("Got onBoard event");
		    		
		    		
		    					
    			}
    	});
    	
    	bwt.addListener("onChangeCellEvent", new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				ChangeCellEvent e = (ChangeCellEvent)event;
	    		Log.i("Jessica", "Triggered own CHANGECELL event, message: " + e.getNewCell());
	    		Log.i("Jessica", bwt.dumpTracking());			
			}
    	});
    }

    @Override
    protected void onPause() {
        super.onPause();
        textView.append("NOTE onPause, about to stopIoManager()\n");
        
        //bwt.stopTracking();
        bwt.stop();
    }
    
    @Override
    protected void onResume() {	    	
        super.onResume();
        textView.append("NOTE onResume, about to deviceStateChange()\n");
        
        bwt.start();
        bwt.startTracking();
        createListeners();
    }
    
    // Used to print simple text to the screen
    public void display(String text){
        textView.append(text);
    	scrollView.smoothScrollTo(0, textView.getBottom());
    }
    
    // Prints simple text + a line break
    public void displayln(String text){
        display(text + "\n");
    }

}
