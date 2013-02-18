package org.techbridgeworld.bwt.api.events;

import javaEventing.EventObject;

public class BoardEvent extends EventObject{

	String message;
	int cell;
	int cellState;
	int dot;
	
	public BoardEvent(String message, int cell, int cellState, int dot){
		this.message = message;
		this.cell = cell;
		this.cellState = cellState;
		this.dot = dot;
	}
	
	// Getters
	public String getMessage(){
		return message;
	}
	
	public int getCellInd() {
		return cell;
	}
	
	public int getCellState() {
		return cellState;
	}
	
	public int getDot() {
		return dot;
	}
}
