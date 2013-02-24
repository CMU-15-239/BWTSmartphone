package org.techbridgeworld.bwt.api;

import java.util.LinkedList;
import java.util.Locale;

import javaEventing.EventManager;

import org.techbridgeworld.bwt.api.events.ChangeCellEvent;
import org.techbridgeworld.bwt.libs.Braille;

import android.util.Log;


public class Board {
	private static Braille braille = new Braille();
	
	private final Cell board[];
	private boolean altFlag = false;
	private LinkedList<Integer> inputInfo;
	
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
		this.inputInfo = new LinkedList<Integer>();
	}
	
	/**
	 * Setters and getters for the altFlag.
	 * altFlag is false unless an alt button is pressed. 
	 * It is then set to true until cleared.
	 */
	public boolean getAltFlag(){
		return this.altFlag;
	}
	public void setAltFlag(boolean altFlag){
		this.altFlag = altFlag;
	}
	public void clearAltFlag(){
		setAltFlag(false);
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
		this.altFlag = false;
	}
	
	/**
	 * Getter method for the currently active cell.
	 * @return currently active cell index.
	 */
	public int getCurrentCellInd() {
		return inputInfo.peekLast();
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
	public void setBitsAtCell(int cellInd, int value) {
		board[cellInd].setValue(value);
	}
	
	/**
	 * Gets the first cellInd accessed in inputInfo, and
	 * returns the trimmed String that includes glyphs from every
	 * cell ind that follows. 
	 * @return
	 */
	public String viewAsIndexed() {
		int index = inputInfo.getFirst();
		int end = inputInfo.getLast();
		StringBuffer buf = new StringBuffer();
		while(index <= end) {
			buf.append(getGlyphAtCell(index));
			index++;
		}
		return buf.toString();
	}
	
	public String viewAndEmptyAsIndexed() {
		int index = inputInfo.getFirst();
		int end = inputInfo.getLast();
		StringBuffer buf = new StringBuffer();
		while(index <= end) {
			buf.append(getGlyphAtCell(index));
			setBitsAtCell(index, 0);
			index++;
		}
		//clear buffer and board's cells within range
		inputInfo.clear();
		return buf.toString();
	}
	
	/**
	 * Inputted refers to just the cells the user has touched
	 * ie: Won't include spaces in returned String
	 * @return
	 */
	public String viewAsInputted() {
		int tmpInd = 0;
		StringBuffer buf = new StringBuffer();
		while(tmpInd != inputInfo.size()) {
			int cellInd = inputInfo.get(tmpInd);
			buf.append(this.getGlyphAtCell(cellInd));
			tmpInd++;
		}
		return buf.toString();
		
	}
	
	/**
	 * Empties the inputInfo AND clears the cells touched
	 * @return
	 */
	public String viewAndEmptyAsInputted() {
		StringBuffer buf = new StringBuffer();
		while(!inputInfo.isEmpty()) {
			int cellInd = inputInfo.remove();
			buf.append(this.getGlyphAtCell(cellInd));
			
			//clear cells touched
			setBitsAtCell(cellInd, 0);
		}
		return buf.toString();
	}
	
	/**
	 * Allows user to delete the last cell they've touched
	 */
	public void backspaceByInput() {
		int lastCell = inputInfo.pop();
		setBitsAtCell(lastCell, 0);
	}
	
	/**
	 * Update board based on given cellInd and buttonInd
	 * Triggers changeCellEvent if necessary
	 * 
	 * @param cellInd - index of cell that was pushed on
	 * @param buttonNum - ranges from 1 to 6
	 * @return the 6-bits of what's raised in cellInd
	 */
	public int update(int cellInd, int buttonNum) {
		if(cellInd == -1) {
			altFlag = true;
		}
		
		int currCellInd = inputInfo.peekLast();
		if(currCellInd != cellInd) {
			EventManager.triggerEvent(this, new ChangeCellEvent(currCellInd, cellInd, this));
			currCellInd = cellInd;
		}
		
		return board[cellInd].setDot(buttonNum);
	}

	/**
	 * Updates the board based on String from BWT hardware
	 * @param 
	 */
	public void update(String message) {		
		
		// If an alt button has been pressed, set the altFlag to true.
		if(message == "a"){
			altFlag = true;
		}
		
		// If it's a button, update the appropriate dot in cell 0 (i.e. the button cell).
		else if("bcdefg".indexOf(message) != -1){
			inputInfo.push(0);
			switch (message.toLowerCase(Locale.getDefault()).charAt(0)){
			case 'b': 
				this.board[0].setDot(4, true);
				break;
			case 'c': 
				this.board[0].setDot(5, true);
				break;
			case 'd': 
				this.board[0].setDot(6, true);
				break;
			case 'e': 
				this.board[0].setDot(1, true);
				break;
			case 'f': 
				this.board[0].setDot(2, true);
				break;
			case 'g': 
				this.board[0].setDot(3, true);
				break;
			default: 
				Log.e ("Salem", "Received unhandled button event '" + message + "'");
			}
		}
		
		// It's two integers, one for the cell and one for the dot.
		else{
			String[] details = message.split(" ");
			
			int cell = Integer.parseInt(details[0].trim());
			int dot =  Integer.parseInt(details[1].trim());
			// Update dot to reference the correct cell. 
			// Cell 1 should be the first one to write in, i.e. the top right cell. 
			dot = ((32-dot) + 16) %32 + 1;
			inputInfo.push(cell);
			
			this.board[cell].setDot(dot, true);
		}
	}
}
