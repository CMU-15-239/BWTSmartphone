package org.techbridgeworld.bwt.api;

import org.techbridgeworld.bwt.libs.Braille;

public class Board {
//	private final int WRONG = -1;
//	private final int IN_PROCESS = 0;
//	private final int FINISHED_LETTER = 1;
//	private final int FINISHED_WORD = 2;
	
	private static Braille braille = new Braille();
	
	private final Cell					board[];
	
//	private final ArrayList<Integer>	expectedInput;
//	private int							expectedInputInd;
	private int							currCellInd;

	public Board() {
		board = new Cell[33];
		currCellInd = -1;

	}


	public void clearBoard() {
		for (final Cell c : board) {
			c.setValue(0);
		}
	}
	
	public int getCurrentCellInd() {
		return currCellInd;
	}
	
	public char getCharAtCell(int cellInd) {
		return board[cellInd].getGlyph();
	}
	
	public int getBitsAtCell(int cellInd) {
		return board[cellInd].getBrailleCode();
	}
	
	/**Returns the 6-bits of what's raised in cellInd
	 * 
	 * @param cellInd - index of cell that was pushed on
	 * @param buttonNum - ranges from 1 to 6
	 * @return
	 */
	public int handleNewInput(int cellInd, int buttonNum) {
		
		//TODO: if letter was entered/glyph was finished, fire event in BWT
		// - Finished a letter
		// - Or if they moved onto a different cell, fire invalid character
		
		
		
		
		return board[cellInd].setDot(buttonNum);
	}
	
	
	
//	/**
//	 * Returns false if still waiting for input to finish
//	 * Else sets expectedInput as indicated by target, return true
//	 * 
//	 * @param target
//	 * @return
//	 */
//	public boolean setExpectedInput(final String target) {
//		if (expectedInput.size() != 0) {
//			return false;
//		}
//		// TODO: turn target to array of integers expected
//		expectedInput.add(1);
//
//		expectedInputInd = 0;
//		return true;
//	}
//
//	public boolean isDone() {
//		return expectedInput.size() == 0;
//	}
//
//	/**
//	 * Return 2 if handled successfully/input finished word
//	 * Return 1 if input finished next letter
//	 * Return 0 if still finishing letter
//	 * Return -1 if wrote incorrect letter
//	 * while(handleInput(data) != 2) do stuff below;
//	 * {
//	 * if(handleInput(data) == -1) alert error sound
//	 * else if(handleInput(data) == 1) alert next letter
//	 * else if(handleInput(data) == 0) keep waiting for input, make no sound.
//	 * }
//	 * alert next word
//	 * 
//	 * @param data
//	 * @return
//	 */
//	public int handleInput(final String data) {
//		if (data == "a") {
//			
//			
//			return FINISHED_WORD;
//		}
//		else
//			if(isDone()) {
//				//simply record what they input? Not sure
//				//Not applicable for current implementation
//				return FINISHED_WORD;
//			}
//			else
//				return handleExpectedGlyphs(data);
//	}
//
//	public int handleExpectedGlyphs(final String btnHitData) {
//		final int newCellInd = 0; // get from btnHitData
//		if (currCellInd == -1) {
//			currCellInd = newCellInd;
//		}
//
//		final Cell c = board[currCellInd];
//
//		final int nextExpInput = expectedInput.get(expectedInputInd);
//		if (currCellInd != newCellInd) {
//			expectedInputInd = 0;
//			currCellInd = -1;
//			return WRONG;
//		}
//
//		final int btnHit = 1; // ranges from button 1 - 6
//		final int currInput = c.pressDot(1 << btnHit - 1);
//		if (currInput == nextExpInput) {
//			expectedInputInd++;
//			currCellInd = -1;
//
//			if (expectedInputInd == expectedInput.size()) {
//				expectedInput.clear();
//				expectedInputInd = -1;
//				return FINISHED_WORD;
//			}
//			else return FINISHED_LETTER;
//		}
//		else {
//			//check wrong button hit
//			int extras = (currInput | nextExpInput) ^ currInput;
//			if (extras != 0) {
//				expectedInputInd = 0;
//				currCellInd = -1;
//				return WRONG;
//			}			
//		}
//		return IN_PROCESS;
//	}

}
