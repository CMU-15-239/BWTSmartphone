package org.techbridgeworld.bwtApi;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javaEventing.EventManager;
import javaEventing.interfaces.Condition;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.MainActivity;
import org.techbridgeworld.bwtApi.events.AltBtnEvent;
import org.techbridgeworld.bwtApi.events.BoardEvent;
import org.techbridgeworld.bwtApi.events.CellsEvent;
import org.techbridgeworld.bwtApi.events.MainBtnEvent;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class BWT {

	// App Context
	private Context context;
	private MainActivity activity;
	
	// Tracking Information
	private boolean isTracking;
	
	// Constants
	private static final int BAUDRATE = 57600;
	private static final int TIMEOUT = 1000;
	private static final int DEBOUNCE = 300; //Milliseconds to disable a button for.
	
	// Buffer / Debounce stuff
	private byte[] dataBuffer = new byte[6]; 
	private int bufferIdx = 0;
	private Hashtable<String, Boolean> debounceHash = new Hashtable<String, Boolean>();
	
	// USB connections
	private UsbManager usbManager; 
	private UsbSerialDriver usbDriver;
	private SerialInputOutputManager serialManager; 
	
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final SerialInputOutputManager.Listener listener =
		new SerialInputOutputManager.Listener() {	
		
			@Override
			public void onRunError(Exception e) {
				//Ignore
			}
	
			@Override
			public void onNewData(final byte[] data) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
			            //textView.append("NOTE onNewData, about to run()\n");
						BWT.this.updateReceivedData(data);
					}
				});
			}
		};
	
	
	// Constructor
	public BWT(Context context, MainActivity activity){
		this.context = context;
		this.activity = activity;
	}	

	// Initialize
	public void init(){
		Log.i("Salem", "BWT.init()");
        isTracking = false;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        startIoManager();
	}
	
	// Add listeners
	public void addListener() {
	}
	
	//Allows event handlers to go off; updates board's state
	public void startTracking() {
		isTracking = true;
	}
	
	//Disregards changing state of board if stopped tracking
	public void stopTracking() {
		isTracking = false;
	}
	
	//Returns and empties everything in current 'buffer'
	public char[] dumpTracking() {
		if (!isTracking) return null;
		
		//TODO:
		return new char['a'];
	}
	
	
	/**
	 * Registers the event handlers; called in bwt.start();
	 */
	public void initializeEventListeners() {
		EventManager.registerEventListener("onBoardEvent",
				createOnBoardListener(), BoardEvent.class);

		EventManager.registerEventListener("onMainBtnEvent",
				createOnMainBtnListener(), BoardEvent.class);

		EventManager.registerEventListener("onAltBtnEvent",
				createOnAltBtnListener(), BoardEvent.class);
		
		EventManager.registerEventListener("onCellsEvent",
				createOnCellsListener(), BoardEvent.class);

		EventManager.registerEventListener("onFinishedLetterEvent",
				createOnFinishedLetterListener(), BoardEvent.class);
	}
	
	public GenericEventListener createOnBoardListener() {
		return new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	public GenericEventListener createOnMainBtnListener() {
		return new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	public GenericEventListener createOnAltBtnListener() {
		return new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	public GenericEventListener createOnCellsListener() {
		return new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	public GenericEventListener createOnFinishedLetterListener() {
		return new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	
	//TODO: Called by onNewData
	public void triggerEventsOnNewData() {
		if(!isTracking) return;
		EventManager.triggerEvent(this, new BoardEvent(), "onBoardEvent");
		EventManager.triggerEvent(this, new MainBtnEvent(), "onMainBtnEvent");
		EventManager.triggerEvent(this, new AltBtnEvent(), "onAltBtnEvent");
		EventManager.triggerEvent(this, new CellsEvent(), "onCellsEvent");
	}
	
	
	// Starts USB connection
	public void start(){
		Log.i("Salem", "BWT.start()");
		
		usbDriver = UsbSerialProber.acquire(usbManager);
        if (usbDriver != null) {
            try {
            	Log.d("Salem", "About to open usbDriver()");
            	usbDriver.open();
				usbDriver.setBaudRate(BAUDRATE);
				byte[] bt = "bt".getBytes();
				usbDriver.write(bt, TIMEOUT);
				initializeEventListeners();
            } catch (IOException e) {
                try {
                	Log.e("Salem", "Error starting USB driver, attempting to close.");
                	usbDriver.close();
                } catch (IOException e2) {
                	Log.e("Salem", "Wut.");
                    // Ignore.
                }
                usbDriver = null;
                return;
            }
        }
        
        stateChange();
	}
	
	// Closes USB connection
	public void stop(){
		stopIoManager();
		Log.i("Salem", "BWT.pause()");
		if (usbDriver != null) {
            try {
				usbDriver.setBaudRate(BAUDRATE);
				byte[] bt = "bt".getBytes();
				usbDriver.write(bt, TIMEOUT);
                usbDriver.close();
            } catch (IOException e) {
                // Ignore.
            }
            usbDriver = null;
        }
	}
	
	// Restarts the IO manager.
	public void stateChange(){
		stopIoManager();
		startIoManager();
	}
	
	// Stop IO
    private void stopIoManager() {
        if (serialManager != null) {
            serialManager.stop();
            serialManager = null;
        }
    }
    
    // Start IO
    private void startIoManager() {
        if (usbDriver != null) {
        	Log.i("Salem", "Starting usb listener");
            serialManager = new SerialInputOutputManager(usbDriver, listener);
            executor.submit(serialManager);
        }
        else{
        	Log.e("Salem", "usbDriver == null");
        }
    }
    
    // Passes a byte array to the debounce hashtable.
    private void debounceKey(String key){
    	final String newKey = key;
    	debounceHash.put(newKey, true);
    	
    	//Start a runnable to un-block the key after a set time.
    	Handler handler=new Handler();
    	Runnable r=new Runnable() {
    	    public void run() {
    	    	debounceHash.put(newKey, false);
    	    }
    	};
    	handler.postDelayed(r, DEBOUNCE);
    }
    
    // Returns true if a key is currently being ignored.
    private boolean isDebounced(String key){
    	boolean query = (debounceHash.get(key) == null? false : debounceHash.get(key));
    	return query;
    }
    
	
    // Takes the received data, checks to see if it should be ignored/debounced,
    // and print the results to device screen (triggers events later).
    private void updateReceivedData(byte[] data) {	
    	Log.d("Salem", "updateReceivedData()");
    	
    	//For every byte in the incoming data...
    	for (int i = 0; i < data.length; i++){
    		
    		Log.d("Salem", "currently parsing " + (char)data[i] + " (" + (int)data[i] + ")");
    		
    		// If we are done, and if the buffer represents a non-blocked key, then
    		// log the buffer, clear it, and set its index to 0.    		    	
    		if(data[i] == 110 || data[i] == 116){
    			
    			// This is to catch the initial "bt" received from the device.
	    		if(data[i] == 116){
    				dataBuffer[bufferIdx] = data[i];	
    				bufferIdx++;
	    		}
    			
    			String message = "";
    			for(int j = 0; j < bufferIdx; j++){
    				message += (char)dataBuffer[j];
    			}
    			
    			if(!isDebounced(message)){ //Fire a trigger!
		    		Log.i("Salem", "Fired trigger '" + message + "'");
	    			debounceKey(message);

    			}
    			else{
    				Log.d("Salem", "Button press blocked!");
    			}
    			
    			bufferIdx = 0;
    			dataBuffer = new byte[6];

    		}
    		else{
    			if(bufferIdx >= 6){
    				Log.e("Salem", "bufferIdx out of range: " + bufferIdx);
    			}
    			else{
    				dataBuffer[bufferIdx] = data[i];	
    				bufferIdx++;
    			}
    		}	
    	}    				    	
    }


}
