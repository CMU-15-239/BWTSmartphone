package org.techbridgeworld.bwt.api.events;

import javaEventing.EventObject;

public class SubmitEvent extends EventObject{

	int cell;
	int cellBits;
	
	public SubmitEvent(int cell, int cellBits){
		this.cell = cell;
		this.cellBits = cellBits;
	}
	
	//Getters
	public int getCellInd() {
		return cell;
	}
	
	public int getCellBits() {
		return cellBits;
	}
}
