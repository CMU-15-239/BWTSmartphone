package org.techbridgeworld.bwt;

import android.util.SparseArray;

public class Cell {
	/*value ranges from 000000 to 111111 to indicate
	 *which button of cell was pushed down
	 */
	private int value;
	private SparseArray<Character> map;
	
	public Cell() {
		value = 0;
		
	}
	public void setMap(SparseArray<Character> map) {
		this.map = map;
	}
	
	
	public int get() {
		return value;
	}
	
	/**Given they push button 3, bit = 000100.
	 * OR's current value with value passed in
	 */
	public int set(int bit) {
		value = value | bit;
		return value;
	}


	/**Resets value to newValue passed in 
	 */
	public void reset(int newValue) {
		value = newValue;
	}
	
	public boolean isGlyph(Character targetChar) {
		if(map.get(value) == targetChar)
			return true;
		return false;
	}
	
	public Character getGlyph() {
		Character glyph = map.get(value);
		if(glyph == null)
			return null;
		return glyph;
	}
	
}
