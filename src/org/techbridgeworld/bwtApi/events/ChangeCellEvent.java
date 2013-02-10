package org.techbridgeworld.bwtApi.events;

import android.util.Log;

public class ChangeCellEvent {

	private int oldCell; // Cell before the change 
	private int newCell; // Cell after the change
	
	private class ChangeCellException extends Exception{

		public ChangeCellException(String message, int oldCell, int newCell) {
			super(message);
			Log.e("Salem", "CellChangeEvent changes from " + oldCell + " to " + newCell + ".");
		}
		
	}

	
	// In case you're nice and give us integers.
	public ChangeCellEvent(int oldCell, int newCell) throws ChangeCellException{
		if(oldCell == newCell){
			throw new ChangeCellException("ChangeCellEvent changes to same cell", oldCell, newCell);
		}
		this.oldCell = oldCell;
		this.newCell = newCell;
	}

	
	// In case you're sadistic and give us strings.
	public ChangeCellEvent(String oldCell, String newCell) throws Exception{
		this(Integer.parseInt(oldCell), Integer.parseInt(newCell));
	}


	// Getters
	public int getOldCell() {
		return oldCell;
	}
	public int getNewCell() {
		return newCell;
	}
	

	
}
