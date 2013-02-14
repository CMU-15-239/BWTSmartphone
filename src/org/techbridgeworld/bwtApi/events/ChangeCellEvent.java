package org.techbridgeworld.bwtApi.events;

import javaEventing.EventObject;
import android.util.Log;

public class ChangeCellEvent extends EventObject {

	private int oldCell; // Cell before the change 
	private int newCell; // Cell after the change
	
	public class ChangeCellException extends Exception{

		public ChangeCellException(String message, int oldCell, int newCell) {
			super(message);
			Log.e("Salem", "CellChangeEvent changes from " + oldCell + " to " + newCell + ".");
		}
		
	}

	
	// In case you're nice and give us integers.
	public ChangeCellEvent(int oldCell, int newCell) {
		if(oldCell == newCell){
			try {
				throw new ChangeCellException("ChangeCellEvent changes to same cell", oldCell, newCell);
			} catch (ChangeCellException e) {
				Log.e("Salem", "ChangeCellEvent changes to same cell: " + oldCell + " to " + newCell);
				e.printStackTrace();
			}
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
