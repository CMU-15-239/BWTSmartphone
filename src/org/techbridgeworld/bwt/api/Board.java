package org.techbridgeworld.bwt.api;

import javaEventing.EventManager;

import org.techbridgeworld.bwt.api.events.ChangeCellEvent;
import org.techbridgeworld.bwt.libs.Braille;


public class Board {
	private static Braille braille = new Braille();
	
	private final Cell					board[];
	
	private int							currCellInd;
	
	public Board() {
		Cell[] temp = new Cell[33];
		
		for(int i = 0; i < 33; i++){
			temp[i] = new Cell();
		}
		this.board = temp;
		currCellInd = -1;
	}
	
	public char getBraille(int i){
		return braille.get(i);
	}
	public int getBraille(char c){
		return braille.get(c);
	}


	public void clearBoard() {
		for (final Cell c : board) {
			c.setValue(0);
		}
	}
	
	public int getCurrentCellInd() {
		return currCellInd;
	}
	
	public int getBitsAtCell(int cellInd) {
		return board[cellInd].getBrailleCode();
	}
	
	public char getGlyphAtCell(int cellInd) {
		return board[cellInd].getGlyph();
	}
	
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
