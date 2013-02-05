package org.techbridgeworld.bwt;

import android.util.SparseArray;

public class Cell {
/**
 * Valid braille codes (dots one through six)
 */
	public static final int ONE 	= 0x01;	//top right button
	public static final int TWO 	= 0x02;
	public static final int THREE 	= 0x04;
	public static final int FOUR 	= 0x08;
	public static final int FIVE 	= 0x10;
	public static final int SIX 	= 0x20;	//bottom left button
	
	/**
	 * The braille code for this cell is the result of bitwise OR
	 * on the dot values
	 */
	private int						brailleCode	= 0;
	private SparseArray<Character>	characterMap;

	
	public int getBrailleCode() {
		return brailleCode;
	}

	/**
	 * Given they push button 3, bit = 000100.
	 * OR's current brailleCode with brailleCode passed in
	 */
	public int pressDot(final int dot) {
		if (isValidDot(dot)) {
			brailleCode = brailleCode | dot;
		}
		return brailleCode;
	}

	/**
	 * Resets brailleCode to newValue passed in
	 */
	public void setValue(final int newValue) {
		brailleCode = newValue;
	}

	private boolean isValidDot(final int bit) {
		return 	ONE == bit ||TWO == bit || THREE == bit ||
				FOUR == bit || FIVE == bit || SIX == bit;
	}

	/**
	 * Return the character corresponding to this sequence of dots,
	 * according to the character map
	 * If the character is not present in the map, the return value is null
	 * 
	 * @return Character | null
	 */
	public Character getGlyph() {
		return characterMap.get(brailleCode);
	}

	/**
	 * Return true if this cell currently encodes a valid character
	 * 
	 * @return boolean true if this cell encodes a valid character
	 */
	public boolean isGlyph() {
		return getGlyph() != null;
	}

	/**
	 * Return true if this cell encodes the character passed as a parameter
	 * 
	 * @param targetChar
	 *            the character to check against
	 * @return boolean true if this cell is encoding the passed in character
	 */
	public boolean isGlyph(final Character targetChar) {
		return getGlyph() == targetChar;
	}
}
