package org.techbridgeworld.bwt;

import java.nio.charset.Charset;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	TextView textView; 
	UsbDevice usbDevice;
	UsbManager usbManager; 
	UsbInterface usbInterface;
	UsbDeviceConnection usbConnection;
	UsbEndpoint usbEndpointOut;
	UsbEndpoint usbEndpointIn;
	
	private static int TIMEOUT = 10000;
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
		
		//Endpoint 0 is in, Endpoint 1 is out
		usbEndpointIn = usbInterface.getEndpoint(0);
		usbEndpointOut = usbInterface.getEndpoint(1);
		
		usbConnection = usbManager.openDevice(usbDevice); 
		
		usbConnection.claimInterface(usbInterface, forceClaim);
		
//		try {
//			usbConnection.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
//			usbConnection.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,
//					0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);
//		}
//		catch(Exception e) {
//			textView.setText(e.getMessage());
//		}
		
		usbConnection.controlTransfer(0x20, 34, 0, 0, null, 0, 0);
		usbConnection.controlTransfer(0x20, 32, 0, 0, new byte[] { (byte) 0x80,
				0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);
		usbConnection.controlTransfer(0x20, 0x03, 0x0034, 0, null, 0, 0);
		
		String b = "b";  
	    byte[] bytes = b.getBytes();
		Integer result = usbConnection.bulkTransfer(usbEndpointOut, bytes, bytes.length, TIMEOUT);
		if (result > 0) { textView.setText(result +  " bytes of data transferred!!"); }
		
		String t = "t";  
	    bytes = t.getBytes();
		result = usbConnection.bulkTransfer(usbEndpointOut, bytes, bytes.length, TIMEOUT);
        if (result > 0) { textView.setText(result +  " bytes of data transferred!! x2"); }
		
		final Handler handler = new Handler();
		final Runnable r = new Runnable()
		{
		    public void run() 
		    {
	            byte[] buffer = new byte[64];

	            StringBuilder s = new StringBuilder(); 

	 			if(usbConnection.bulkTransfer(usbEndpointIn, buffer, 64, TIMEOUT) >= 0) {
	 				for(int i = 0; i < 64; i++) {
	 					//if(buffer[i] != 0)
	 					s.append(buffer[i]);
	 				}
	 			}

	 			textView.setText(textView.getText() + "\n" + s); 
		        handler.postDelayed(this, 1000);
		    }
		};

		handler.postDelayed(r, 1000);
    }
}
