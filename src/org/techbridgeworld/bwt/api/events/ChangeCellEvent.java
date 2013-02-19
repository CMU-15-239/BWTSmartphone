package org.techbridgeworld.bwt.api.events;

import org.techbridgeworld.bwt.api.Board;

import javaEventing.EventObject;
import android.util.Log;

public class ChangeCellEvent extends EventObject {

	private Board board;
	private int oldCell; // Cell before the change 
	private int newCell; // Cell after the change
	private int oldCellBits;
	private char oldCellGlyph;
	
	public class ChangeCellException extends Exception{

		public ChangeCellException(String message, int oldCell, int newCell) {
			super(message);
			Log.e("Salem", "CellChangeEvent changes from " + oldCell + " to " + newCell + ".");
		}
		
	}

	
	// In case you're nice and give us integers.
	public ChangeCellEvent(int oldCell, int newCell, Board board) {
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
		this.board = board;
		this.oldCellGlyph = (oldCell < 0) ? 0 : board.getGlyphAtCell(oldCell);
		this.oldCellBits = (oldCell < 0) ? 0 : board.getBitsAtCell(oldCell);
	}

	
	// In case you're sadistic and give us strings.
	public ChangeCellEvent(String oldCell, String newCell, Board board) throws Exception{
		this(Integer.parseInt(oldCell), Integer.parseInt(newCell), board);
	}


	// Getters
	public int getOldCell() {
		return oldCell;
	}
	public char getOldCellGlyph() {
		return oldCellGlyph;
	}
	public int getOldCellBits() {
		return oldCellBits;
	}
	
	public int getNewCell() {
		return newCell;
	}
	

	
}
