package org.techbridgeworld.bwt.student;

import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.BWT;
import org.techbridgeworld.bwt.api.events.BoardEvent;
import org.techbridgeworld.bwt.api.events.ChangeCellEvent;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
	
public class MainActivity extends Activity {
	
	// UI stuff
	private ScrollView scrollView; 
	private TextView textView; 
	
	// BWT object
	private final BWT bwt = new BWT(this, MainActivity.this);
	
    protected PowerManager.WakeLock mWakeLock;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        textView = (TextView) findViewById(R.id.textview);
        textView.append("NOTE onCreate, created stuff\n");
        
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
        
        bwt.init();
    }
    
    private void createListeners() {
    	bwt.replaceListener("onBoardEvent", new GenericEventListener() {
    			@Override
    			public void eventTriggered(Object sender, Event event) {
    				bwt.defaultBoardHandler(sender, event);
    				BoardEvent e = (BoardEvent)event;
		    		
		    		//RESULT: Warning of CalledFromWrongThreadException: only the
		    		//original thread that created a view hierarchy
		    		//displayln("Got onBoard event");
		    		
		    		
		    					
    			}
    	});
    	
    	bwt.replaceListener("onChangeCellEvent", new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				ChangeCellEvent e = (ChangeCellEvent)event;
				if(e.getOldCell() == -1) return;
				bwt.defaultChangeCellHandler(sender, event);
				String str = bwt.dumpTracking();
				Log.d("Jessica", "EmptyingBuffer: '" + str + "'");
			}
    	});
    }

    @Override
    protected void onPause() {
        super.onPause();
        textView.append("NOTE onPause, about to stopIoManager()\n");
        
        bwt.stopTracking();
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
    
    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
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
