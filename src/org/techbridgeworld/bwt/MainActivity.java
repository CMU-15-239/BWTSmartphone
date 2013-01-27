package org.techbridgeworld.bwt;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.hardware.usb.*;

public class MainActivity extends Activity {
	
	String s = ""; 
	
	TextView textView; 
	UsbDevice usbDevice;
	UsbManager usbManager; 
	UsbInterface usbInterface;
	UsbDeviceConnection usbConnection;
	UsbEndpoint usbEndpoint;
	
	private byte[] bytes;
	private static int TIMEOUT = 0;
	private boolean forceClaim = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textView = (TextView) findViewById(R.id.textview);
        
        Intent intent = getIntent(); 
        usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		
		usbInterface = usbDevice.getInterface(0);
		
		usbEndpoint = usbInterface.getEndpoint(0);
        
		usbConnection = usbManager.openDevice(usbDevice); 
		
		usbConnection.claimInterface(usbInterface, forceClaim);
		
		//usbConnection.bulkTransfer(usbEndpoint, bytes, bytes.length, TIMEOUT);
    }
}
