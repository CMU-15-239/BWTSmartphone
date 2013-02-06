package org.techbridgeworld.bwtApi;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.techbridgeworld.bwt.MainActivity;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class BWT {

	// Activity context
	private Context context;
	private MainActivity activity;
	
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
            } catch (IOException e) {
                try {
                	Log.e("Salem", "Error starting USB driver, attempting to close.");
                	usbDriver.close();
                } catch (IOException e2) {
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
