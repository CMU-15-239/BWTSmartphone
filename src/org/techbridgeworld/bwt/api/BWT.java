package org.techbridgeworld.bwt.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
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
import org.techbridgeworld.bwt.libs.Braille;

import android.app.Activity;
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
	private Activity currActivity;

	// BWT information/state
	private Board board;
	private static final Braille braille = new Braille();
	private boolean isTracking;
	private ArrayList<Integer> inputBuffer;
	private int lastCell = -1;

	// Constants
	private static final int BAUDRATE = 57600;
	private static final int TIMEOUT = 1000;
	private static final int DEBOUNCE = 300; // Milliseconds to disable a button
												// for.

	// Buffer / Debounce stuff
	private byte[] dataBuffer = new byte[6];
	private int bufferIdx = 0;
	private Hashtable<String, Boolean> debounceHash = new Hashtable<String, Boolean>();

	// USB connections
	private UsbManager usbManager;
	private UsbSerialDriver usbDriver;
	private SerialInputOutputManager serialManager;

	private Handler handler = new Handler();
	private final ExecutorService executor = Executors
			.newSingleThreadExecutor();
	private final SerialInputOutputManager.Listener listener = new SerialInputOutputManager.Listener() {

		@Override
		public void onRunError(Exception e) {
			// Ignore
			Log.e("DataTransfer",
					"In the onRunError function: " + e.getMessage());
		}

		@Override
		public void onNewData(final byte[] data) {
			updateReceivedData(data);
		}
	};

	/**
	 * BWT class contains all the interaction done between the main activities
	 * and the board/cell classes.
	 * 
	 * @param context
	 * @param activity
	 */
	public BWT(Context context, Activity activity) {
		this.context = context;
		this.currActivity = activity;
		this.board = new Board();
		this.inputBuffer = new ArrayList<Integer>();
		this.isTracking = false;
	}

	/**
	 * Init function should be called onCreate of the BWT.
	 */
	public void init() {
		usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		startIoManager();
	}

	/**
	 * BWT.start() should be called after BWT.init(). Will open the serial
	 * connection and create default event listeners
	 */
	public void start() {
		usbDriver = UsbSerialProber.acquire(usbManager);
		if (usbDriver != null) {
			try {
				usbDriver.open();
				usbDriver.setBaudRate(BAUDRATE);
				byte[] bt = "bt".getBytes();
				usbDriver.write(bt, TIMEOUT);
			} catch (IOException e) {
				try {
					Log.e("Connecting",
							"Error starting USB driver, attempting to close.");
					usbDriver.close();
				} catch (IOException e2) {
					Log.e("Connecting", "Wut.");
					// Ignore.
				}
				usbDriver = null;
				return;
			}
		}

		stateChange();
	}

	/**
	 * BWT.stop() should be called each time you leave the app or no longer need
	 * to read input from BWT. Also removes all event listeners, including
	 * developer's
	 */
	public void stop() {
		stopIoManager();
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

	/**
	 * Start IO
	 */
	private void startIoManager() {
		if (usbDriver != null) {
			serialManager = new SerialInputOutputManager(usbDriver, listener);
			executor.submit(serialManager);
		} else {
			Log.e("Connecting", "usbDriver == null");
		}
	}

	/**
	 * Stop IO
	 */
	private void stopIoManager() {
		if (serialManager != null) {
			serialManager.stop();
			serialManager = null;
		}
	}

	/**
	 * Restarts IO
	 */
	public void stateChange() {
		stopIoManager();
		startIoManager();
	}

	/**
	 * Manages the debounce hashtable for seeing whether it is a new press or
	 * different
	 * 
	 * @param key
	 *            is a byte array
	 */
	private void debounceKey(String key) {
		final String newKey = key;
		debounceHash.put(newKey, true);

		// Start a runnable to un-block the key after a set time.
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(DEBOUNCE);
				} catch (InterruptedException e) {
					Log.e("Debounce", "Failed to sleep thread.");
					e.printStackTrace();
				}
				debounceHash.put(newKey, false);
			}
		};

		handler.post(r);
	}

	/**
	 * Returns true if a key is currently being ignored.
	 * 
	 * @param key
	 *            to check
	 * @return
	 */
	private boolean isDebounced(String key) {
		boolean query = (debounceHash.get(key) == null ? false : debounceHash
				.get(key));
		return query;
	}

	/**
	 * Takes the received data, checks to see if it should be ignored/debounced,
	 * and triggers the corresponding events
	 * 
	 * @param data
	 */
	private void updateReceivedData(byte[] data) {

		// For every byte in the incoming data...
		for (int i = 0; i < data.length; i++) {

			// If we are done, and if the buffer represents a non-blocked key,
			// then
			// log the buffer, clear it, and set its index to 0.
			if (data[i] == 110 || data[i] == 116) {

				// This is to catch the initial "bt" received from the device.
				if (data[i] == 116) {
					dataBuffer[bufferIdx] = data[i];
					bufferIdx++;
				}

				String message = "";
				for (int j = 0; j < bufferIdx; j++) {
					message += (char) dataBuffer[j];
				}

				if (!isDebounced(message)) { // Fire a trigger!
					Log.i("DataTransfer", "Fired trigger '" + message + "'");

					triggerNewDataEvent(message);
					debounceKey(message);

				} else {
					// Log.d("DataTransfer", "Button press blocked!");
				}

				bufferIdx = 0;
				dataBuffer = new byte[6];

			} else {
				if (bufferIdx >= 6) {
					Log.e("DataTransfer", "bufferIdx out of range: "
							+ bufferIdx);
				} else {
					dataBuffer[bufferIdx] = data[i];
					bufferIdx++;
				}
			}
		}
	}

	/** Board getter */
	public Board getBoard() {
		return this.board;
	}

	/**
	 * Allows event handlers to go off; updates board's state
	 */
	public void startTracking() {
		isTracking = true;
	}

	/**
	 * Disregards changing state of board if stopped tracking
	 * 
	 * @return inputBuffer's remaining content
	 */
	public ArrayList<Integer> stopTracking() {
		isTracking = false;
		ArrayList<Integer> result = inputBuffer;
		inputBuffer.clear();
		return result;
	}

	/**
	 * Returns and empties everything in current 'buffer'
	 * 
	 * @return what was left in the buffer If not tracking, returns empty string
	 */
	public String dumpTrackingAsString() {
		String s = viewTrackingAsString();
		clearTracking();
		return s;
	}

	/**
	 * Returns everything in current 'buffer' (Does NOT empty)
	 * 
	 * @return what was left in the buffer If not tracking, returns empty string
	 */
	public String viewTrackingAsString() {
		if (!isTracking)
			return null;
		StringBuffer s = new StringBuffer();
		for (Integer i : inputBuffer) {
			s.append(braille.get(i));
		}
		return s.toString();
	}

	/**
	 * Dumps as an Array of Integers
	 * 
	 * @return
	 */
	public ArrayList<Integer> dumpTrackingAsBits() {
		if (!isTracking)
			return null;
		ArrayList<Integer> result = inputBuffer;
		clearTracking();
		return result;
	}

	/**
	 * Views as an Array of Integers
	 * 
	 * @return
	 */
	public ArrayList<Integer> viewTrackingAsBits() {
		if (!isTracking)
			return null;
		ArrayList<Integer> result = inputBuffer;
		return result;
	}

	/**
	 * Resets the inputBuffer; keeps lastInputtedCell info
	 */
	public void clearTracking() {
		inputBuffer.clear();
	}

	/**
	 * Resets lastCell to initial state
	 */
	public void clearLastInputtedCell() {
		if(lastCell > 0) {
			board.setBitsAtCell(lastCell, 0);
			lastCell = -1;
		}
	}

	/**
	 * Clears both inputBuffer and current cell info
	 */
	public void clearAllTracking() {
		clearTracking();
		clearLastInputtedCell();
	}

	/**
	 * Clears and resets state of the board
	 */
	public void resetBoard() {
		clearAllTracking();
		board.clearBoard();
	}

	/**
	 * Gets the bits pressed for 'lastCell'
	 * 
	 * @return
	 */
	public int getCurrentCellBits() {
		if (lastCell < 0)
			return 0;
		return board.getBitsAtCell(lastCell);
	}

	/**
	 * Gets the current char for 'lastCell'
	 * 
	 * @return
	 */
	public char getCurrentCellGlyph() {
		if (lastCell < 0)
			return '-';
		return board.getGlyphAtCell(lastCell);
	}

	/**
	 * Compares current input with given char Takes into account bits of
	 * lastCell
	 * 
	 * @param c
	 *            : the glyph you're aiming to match
	 * @return true : if off track of matching c
	 */
	public boolean offTrackFromChar(char c) {
		if (lastCell < 0)
			return false;
		int currBits = board.getBitsAtCell(lastCell);
		int expBits = braille.get(c);
		return (((currBits & expBits) ^ currBits) > 0);
	}

	/**
	 * Takes into account everything in inputBuffer AND lastCell
	 * 
	 * @param s
	 * @return
	 */
	public boolean offTrackFromString(String s) {

		// Check what's in trackingBuffer
		String currDump = viewTrackingAsString();
		int indexFound = s.indexOf(currDump);
		if (indexFound != 0 && currDump.length() > 0)
			return true;

		// Check the most recent cell
		return offTrackFromChar(s.charAt(currDump.length()));
	}

	public boolean currentMatchesChar(char c) {
		if (lastCell < 0)
			return false;
		int currBits = board.getBitsAtCell(lastCell);
		int expBits = braille.get(c);
		return (currBits == expBits);

	}

	public boolean currentMatchesString(String s) {
		// Check what's in trackingBuffer
		String currDump = viewTrackingAsString();
		int indexFound = s.indexOf(currDump);
		if (currDump.equals(s))
			return true;
		if (indexFound != 0 || currDump.length() != s.length() - 1)
			return false;

		// Check the most recent cell
		return currentMatchesChar(s.charAt(currDump.length()));

	}
	
	/**
	 * Called by updateReceivedData to trigger necessary events
	 * 
	 * @param String
	 *            sent from BWT firmware through USB
	 */
	private void triggerNewDataEvent(String message) {
		if (!isTracking)
			return;

		message = message.toLowerCase().replaceAll("n", "").trim();
		if (message.equals("bt"))
			return;

		String referenceStr = "abcdefg";

		int currCell = -1;
		int currCellBits = 0; // The current set bits of currCell
		int currDot = -1; // The button just hit

		// See if it's a, b-g, or two numbers
		if (referenceStr.indexOf(message) == 0) {
			EventManager.triggerEvent(this, new AltBtnEvent(message),
					"onAltBtnEvent");
		} else if (referenceStr.indexOf(message) > 0) {
			MainBtnEvent mainBtnEvent = new MainBtnEvent(message, board);
			EventManager.triggerEvent(this, mainBtnEvent, "onMainBtnEvent");
			currCell = 0;
			currDot = mainBtnEvent.getDot();
		} else {
			CellsEvent cellsEvent = new CellsEvent(message, board);
			EventManager.triggerEvent(this, cellsEvent, "onCellsEvent");

			// Determine if there has been a cell change.
			currCell = cellsEvent.getCell();
			currDot = cellsEvent.getDot();
		}

		// Determine if there has been a cell change (Event Handler updates
		// lastCell)
		if (currCell != lastCell)
			EventManager.triggerEvent(this, new ChangeCellEvent(lastCell,
					currCell, board), "onChangeCellEvent");

		if (currCell >= 0)
			currCellBits = board.getBitsAtCell(currCell);

		// Trigger board event regardless
		board.update(message);
		EventManager.triggerEvent(this, new BoardEvent(message, currCell,
				currCellBits, currDot), "onBoardEvent");

	}

	/**
	 * Registers default event handlers; called in bwt.start();
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

	/**
	 * Unregisters all event listeners; called in bwt.stop();
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
	 * 
	 * @param context
	 * @param customizedListener
	 * @return
	 */
	public boolean replaceListener(String context,
			GenericEventListener customizedListener) {
		Class<? extends Event> c = null;
		if (context == "onBoardEvent")
			c = BoardEvent.class;
		else if (context == "onAltBtnEvent")
			c = AltBtnEvent.class;
		else if (context == "onMainBtnEvent")
			c = MainBtnEvent.class;
		else if (context == "onCellsEvent")
			c = CellsEvent.class;
		else if (context == "onChangeCellEvent")
			c = ChangeCellEvent.class;
		else
			return false;

		EventManager.unregisterAllEventListenersForContext(context);
		EventManager.registerEventListener(context, customizedListener, c);
		return true;

	}

	/*
	 * Default Handlers of the board (accessible to developers)
	 */
	public void defaultBoardHandler(Object sender, Event event) {
		// API doesn't have a default function. Here for developers
	}

	public void defaultMainBtnHandler(Object sender, Event event) {
		//MainBtnEvent e = (MainBtnEvent) event;
		//board.handleNewInput(0, e.getDot());
		Log.i("EventTriggering", "Calling default onMainBtn event handler");
	}

	public void defaultAltBtnHandler(Object sender, Event event) {
		// Doesn't do anything. Let developers decide functionality
		Log.i("EventTriggering", "Calling default onAltBtn event handler");
	}
	
	public void defaultCellsHandler(Object sender, Event event) {	
		//CellsEvent e = (CellsEvent) event;
		//board.handleNewInput(e.getCell(), e.getDot());
		Log.i("EventTriggering", "Calling default onCells event handler");
	}

	public void defaultChangeCellHandler(Object sender, Event event) {
		ChangeCellEvent e = (ChangeCellEvent) event;
		
		/*pushes the glyph at this cell into the inputBuffer
		 *then resets old cell value*/ 
//		int oldCellInd = e.getOldCell();
//		first time ChangeCell is called, oldCellInd = -1
//		(oldCellInd < 0) return 0;	
//		
//		int oldCellBits = e.getOldCellBits();
//		inputBuffer.add(board.getBitsAtCell(oldCellInd));
//		board.setBitsAsCell(oldCellInd, 0);
//		lastCell = e.getNewCell();

		Log.i("EventTriggering", "Calling default onChangeCell event handler");
		//return oldCellBits;
	}

	/**
	 * Creates the default event Listeners set up for BWT
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
	/****************************************************************/

}
