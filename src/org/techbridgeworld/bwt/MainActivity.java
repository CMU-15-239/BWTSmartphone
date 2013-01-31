package org.techbridgeworld.bwt;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class MainActivity extends Activity {
	
	int BAUDRATE = 57600;
	int TIMEOUT = 1000;
	
	private ScrollView scrollView; 
	private TextView textView; 
	
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
			MainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					MainActivity.this.updateReceivedData(data);
				}
			});
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        textView = (TextView) findViewById(R.id.textview);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (usbDriver != null) {
            try {
                usbDriver.close();
            } catch (IOException e) {
                // Ignore.
            }
            usbDriver = null;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        usbDriver = UsbSerialProber.acquire(usbManager);
        if (usbDriver != null) {
            try {
            	usbDriver.open();
				usbDriver.setBaudRate(BAUDRATE);
				byte[] bt = "bt".getBytes();
				usbDriver.write(bt, TIMEOUT);
            } catch (IOException e) {
                try {
                	usbDriver.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                usbDriver = null;
                return;
            }
        }
        onDeviceStateChange();
    }
    
    private void stopIoManager() {
        if (serialManager != null) {
            serialManager.stop();
            serialManager = null;
        }
    }

    private void startIoManager() {
        if (usbDriver != null) {
            serialManager = new SerialInputOutputManager(usbDriver, listener);
            executor.submit(serialManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }
    
    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
        textView.append(message);
        scrollView.smoothScrollTo(0, textView.getBottom());
    }
}
