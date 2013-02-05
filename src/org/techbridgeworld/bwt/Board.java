package org.techbridgeworld.bwt;

import java.util.ArrayList;

import android.util.SparseArray;

public class Board {
	private final Cell					board[];
	public SparseArray<Character>		bitToGlyphMap;
	private final ArrayList<Integer>	expectedInput;
	private int							expectedInputInd;
	private int							currCellInd;

	public Board() {
		bitToGlyphMap = initializeBitToGlyph();
		board = new Cell[33];
		for (final Cell c : board) {
			c.setCharacterMap(bitToGlyphMap);
		}

		expectedInput = new ArrayList<Integer>();
		expectedInputInd = -1;
		currCellInd = -1;

	}

	/*
	 * Maps bits to related Braille letter
	 */
	private SparseArray<Character> initializeBitToGlyph() {
		final SparseArray<Character> map = new SparseArray<Character>();
		// Initialize all bits to map to character

		return map;
	}

	/**
	 * Returns false if still waiting for input to finish
	 * Else sets expectedInput as indicated by target, return true
	 * 
	 * @param target
	 * @return
	 */
	public boolean setExpectedInput(final String target) {
		if (expectedInput.size() != 0) {
			return false;
		}
		// TODO: turn target to array of integers expected
		expectedInput.add(1);

		expectedInputInd = 0;
		return true;
	}

	/*
	 * isDone returns true if finished with expectedInput
	 */
	public boolean isDone() {
		return expectedInput.size() == 0;
	}

	/**
	 * Return 2 if handled successfully/input finished word
	 * Return 1 if input finished next letter
	 * Return 0 if still finishing letter
	 * Return -1 if wrote incorrect letter
	 * Can setExpectedInput, then call
	 * while(handleInput(data) == 0) continue of some sort;
	 * Or if(handleInput(data) == -1) alert error sound
	 * else if(handleInput(data) == 1) alert next letter
	 * else if(handleInput(data) == 2) alert next word
	 * else keep waiting for input, make no sound.
	 * 
	 * @param data
	 * @return
	 */
	public int handleInput(final String data) {
		if (data == "a") {

			return 2; // returns -1 if failed
		} else if (isDone()) {
			// simply record what they input? Not sure
			// Not applicable for current implementation
			return 2;
		} else {
			return handleExpectedGlyphs(data);
		}
	}

	public int handleExpectedGlyphs(final String btnHitData) {
		final int newCellInd = 0; // get from btnHitData
		if (currCellInd == -1) {
			currCellInd = newCellInd;
		}

		final Cell c = board[currCellInd];

		final int nextExpInput = expectedInput.get(expectedInputInd);
		if (currCellInd != newCellInd) {
			expectedInputInd = 0;
			currCellInd = -1;
			return -1;
		}

		final int btnHit = 1; // ranges from button 1 - 6
		final int currInput = c.pressDot(1 << btnHit - 1);
		if (currInput == nextExpInput) {
			expectedInputInd++;
			currCellInd = -1;

			if (expectedInputInd == expectedInput.size()) {
				expectedInput.clear();
				expectedInputInd = -1;
				return 2;
			} else {
				return 1;
			}
		} else {
			// check wrong button hit
			final int extras = (currInput | nextExpInput) ^ currInput;
			if (extras != 0) {
				expectedInputInd = 0;
				currCellInd = -1;
				return -1;
			}
		}
		return 0;

	}

	public void clearBoard() {
		for (final Cell c : board) {
			c.setValue(0);
		}
	}

}
