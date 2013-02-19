package org.techbridgeworld.bwt.api;

import javaEventing.EventManager;

import org.techbridgeworld.bwt.api.events.ChangeCellEvent;
import org.techbridgeworld.bwt.libs.Braille;


public class Board {
	private static Braille braille = new Braille();
	
	private final Cell board[];	
	private int currCellInd;
	
	/**
	 * Constructor. Initializes all the cells to 0, 
	 * and sets the current cell to -1.
	 */
	public Board() {
		Cell[] temp = new Cell[33];
		
		for(int i = 0; i < 33; i++){
			temp[i] = new Cell();
		}
		this.board = temp;
		currCellInd = -1;
	}
	
	/**
	 * Converts a raw braille representation to a glyph.
	 * @param i = raw int 
	 * @return converted glyph from i.
	 */
	public char getBraille(int i){
		return braille.get(i);
	}
	
	/**
	 * Converts a character to a raw braille representation.
	 * @param c = character to convert
	 * @return raw braille representation of c.
	 */
	public int getBraille(char c){
		return braille.get(c);
	}

	/**
	 * Clears the board of all values.
	 */
	public void clearBoard() {
		for (final Cell c : board) {
			c.setValue(0);
		}
	}
	
	/**
	 * Getter method for the currently active cell.
	 * @return currently active cell index.
	 */
	public int getCurrentCellInd() {
		return currCellInd;
	}
	
	/**
	 * Returns the braille code at the given cell index.
	 * @param cellInd = cell index.
	 * @return braille code at board[cellInd].
	 */
	public int getBitsAtCell(int cellInd) {
		return board[cellInd].getBrailleCode();
	}
	
	/**
	 * Returns the glyph representation stored at the given cell index. 
	 * @param cellInd = cell index.
	 * @return glyph at board[cellInd].
	 */
	public char getGlyphAtCell(int cellInd) {
		return board[cellInd].getGlyph();
	}
	
	/** 
	 * Sets a cell to a given value
	 * @param cellInd = cell index.
	 * @param value = value to store in cell.
	 */
	public void setBitsAsCell(int cellInd, int value) {
		board[cellInd].setValue(value);
	}
	
	/**Returns the 6-bits of what's raised in cellInd
	 * 
	 * @param cellInd - index of cell that was pushed on
	 * @param buttonNum - ranges from 1 to 6
	 * @return
	 */
	public int handleNewInput(int cellInd, int buttonNum) {
		if(currCellInd != cellInd) {
			EventManager.triggerEvent(this, new ChangeCellEvent(currCellInd, cellInd, this));
			currCellInd = cellInd;
		}
		
		return board[cellInd].setDot(buttonNum);
	}
}
