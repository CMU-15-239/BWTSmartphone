package org.techbridgeworld.bwt.api;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javaEventing.EventManager;
import javaEventing.interfaces.Event;
import javaEventing.interfaces.GenericEventListener;

import org.techbridgeworld.bwt.api.events.AltBtnEvent;
import org.techbridgeworld.bwt.api.events.BoardEvent;
import org.techbridgeworld.bwt.api.events.CellsEvent;
import org.techbridgeworld.bwt.api.events.ChangeCellEvent;
import org.techbridgeworld.bwt.api.events.MainBtnEvent;
import org.techbridgeworld.bwt.student.MainActivity;

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
	
	// BWT information/state
	private Board board;
	private boolean isTracking;
	private StringBuffer stringBuffer;
	private int lastCell = -1;
	
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
	
	
	/**BWT class contains all the interaction done between the main activities and
	 * the board/cell classes.
	 * 
	 * @param context
	 * @param mainActivity
	 */
	public BWT(Context context, MainActivity mainActivity){
		this.context = context;
		this.activity = mainActivity;
		this.board = new Board();
		this.stringBuffer = new StringBuffer();
        isTracking = false;
	}	

	/**Init function should be called onCreate of the BWT.
	 */
	public void init(){
		Log.i("Salem", "BWT.init()");
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        startIoManager();
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
            	removeEventListeners();
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
    	    @Override
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
		    		triggerNewDataEvent(message);
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

	
	/**Allows event handlers to go off; updates board's state
	 */
	public void startTracking() {
		isTracking = true;
	}
	
	/**Disregards changing state of board if stopped tracking
	 * @return stringBuffer's remaining letters
	 */
	public String stopTracking() {
		isTracking = false;
		return emptyBuffer();
	}
	
	/**Returns and empties everything in current 'buffer'
	 * @return what was left in the buffer
	 * If not tracking, returns empty string
	 */
	public String dumpTracking() {
		if (!isTracking) return "";
		return emptyBuffer();
	}
	
	/**Empties the stringBuffer and returns its content
	 * @return stringBuffer's data
	 */
	public String emptyBuffer() {
		if(stringBuffer.length() <= 0) return "";

		String str = new String(stringBuffer);
		stringBuffer.delete(0, stringBuffer.length());
		return str;
	}
	
	/**Called by updateReceivedData to trigger necessary events
	 * 
	 * @param String sent from BWT firmware through USB
	 */
    private void triggerNewDataEvent(String message) {
    	if(!isTracking) return;
    	
    	message = message.toLowerCase().replaceAll("n","").trim();
    	Log.i("Salem", "Cleaned message: '" + message + "'");
    	if(message.equals("bt")) return;
    	
    	String referenceStr = "abcdefg";
    	
    	// See if it's a, b-g, or two numbers
    	if(referenceStr.indexOf(message) == 0) {
    		EventManager.triggerEvent(this, new AltBtnEvent(message), "onAltBtnEvent");    		
    	}
    	else if (referenceStr.indexOf(message) > 0) {
    		EventManager.triggerEvent(this, new MainBtnEvent(message, board), "onMainBtnEvent");
    		
    		// Determine if there has been a cell change.
    		if(lastCell > 0){
    			EventManager.triggerEvent(this, new ChangeCellEvent(lastCell, 0), "onChangeCellEvent");
    		}
    	}
    	else {
    		EventManager.triggerEvent(this, new CellsEvent(message, board), "onCellsEvent");
    		
    		// Determine if there has been a cell change.
    		int currCell = Integer.parseInt(message.split(" ")[0]);
    		if(currCell != lastCell){
    			EventManager.triggerEvent(this, new ChangeCellEvent(lastCell, currCell), "onChangeCellEvent");
    		}
    	}

    	// Trigger board event regardless
		EventManager.triggerEvent(this, new BoardEvent(message), "onBoardEvent");
    	
    	
    }
    
	/**Registers default event handlers; called in bwt.start();
	 */
	public void initializeEventListeners() {
		EventManager.registerEventListener("onBoardEvent",
				createOnBoardListener(), BoardEvent.class);

		EventManager.registerEventListener("onMainBtnEvent",
				createOnMainBtnListener(), MainBtnEvent.class);

		EventManager.registerEventListener("onAltBtnEvent",
				createOnAltBtnListener(), AltBtnEvent.class);
		
		EventManager.registerEventListener("onCellsEvent",
				createOnCellsListener(), CellsEvent.class);
		
		EventManager.registerEventListener("onChangeCellEvent",
				createOnChangeCellListener(), ChangeCellEvent.class);
	}
	
	/**Unregisters all event listeners; called in bwt.stop();
	 */
	public void removeEventListeners() {
		EventManager.unregisterAllEventListenersForContext("onBoardEvent");
		EventManager.unregisterAllEventListenersForContext("onMainBtnEvent");
		EventManager.unregisterAllEventListenersForContext("onAltBtnEvent");
		EventManager.unregisterAllEventListenersForContext("onCellsEvent");
		EventManager.unregisterAllEventListenersForContext("onChangeCellEvent");
	}
	

	/**
	 * Lets developers add their own event listeners; replaces current listeners
	 * Returns false if didn't recognize context; returns true otherwise
	 * @param context
	 * @param customizedListener
	 * @return
	 */
	public boolean replaceListener(String context, GenericEventListener customizedListener) {
		Class<? extends Event> c = null;
		if(context == "onBoardEvent") c = BoardEvent.class;
		else if(context == "onAltBtnEvent") c = AltBtnEvent.class;
		else if(context == "onMainBtnEvent") c = MainBtnEvent.class;
		else if(context == "onCellsEvent") c = CellsEvent.class;
		else if(context == "onChangeCellEvent") c = ChangeCellEvent.class;
		else return false;
		
		EventManager.unregisterAllEventListenersForContext(context);
		EventManager.registerEventListener(context, customizedListener, c);
		return true;
		
	}
	
	/* Default Handlers of the board (accessible to developers)
	 * 
	 */
	public void defaultBoardHandler(Object sender, Event event) {
		//API doesn't have a default function. Here for developers
	}

	public void defaultMainBtnHandler(Object sender, Event event) {
		MainBtnEvent e = (MainBtnEvent) event;
		board.handleNewInput(0, e.getDot());
	}
	
	public void defaultAltBtnHandler(Object sender, Event event) {
		//Doesn't do anything. Let developers decide functionality
	}
	
	public void defaultCellsHandler(Object sender, Event event) {	
		CellsEvent e = (CellsEvent) event;
		board.handleNewInput(e.getCell(), e.getDot());
	}
	
	public void defaultChangeCellHandler(Object sender, Event event) {
		ChangeCellEvent e = (ChangeCellEvent) event;
		
		/*pushes the char at this cell into the stringbuffer
		 *then resets old cell value*/ 
		int oldCellInd = e.getOldCell();
		
		//first time ChangeCell is called, oldCellInd = -1
		if(oldCellInd < 0) return;	

		Log.i("Jessica", "Triggered default onCellChange event");
		stringBuffer.append(board.getGlyphAtCell(oldCellInd));
		board.setBitsAsCell(oldCellInd, 0);
		lastCell = e.getNewCell();
	}
	
	/* Event Listeners originally set up for BWT
	 */
	private GenericEventListener createOnBoardListener() {
		return new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				defaultBoardHandler(sender, event);
			}
		};
	}
	
	private GenericEventListener createOnMainBtnListener() {
		return new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				defaultMainBtnHandler(sender, event);
			}
		};
	}

	private GenericEventListener createOnAltBtnListener() {
		return new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				defaultAltBtnHandler(sender, event);
			}
		};
	}

	private GenericEventListener createOnCellsListener() {
		return new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				defaultCellsHandler(sender, event);
			}
		};
	}
	
	private GenericEventListener createOnChangeCellListener() {
		return new GenericEventListener() {
			@Override
			public void eventTriggered(Object sender, Event event) {
				defaultChangeCellHandler(sender, event);
			}
		};
	}


}
