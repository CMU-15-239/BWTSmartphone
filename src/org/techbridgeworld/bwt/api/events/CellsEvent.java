package org.techbridgeworld.bwt.api.events;

import javaEventing.EventObject;

import org.techbridgeworld.bwt.api.Board;

public class CellsEvent extends EventObject {
	
	private String message; // Raw button details 
	private int cell;		// The number of the cell
	private int dot;		// The number of the dot
	
	public CellsEvent(String message, Board board){
		this.message = message;
		
		String[] details = message.split(" ");
		
		this.cell = Integer.parseInt(details[0]);
		this.dot =  Integer.parseInt(details[1]);
	}
	
	// Getters
	public String getMessage(){
		return message;
	}
	public int getCell(){
		return cell;
	}
	public int getDot(){
		return dot;
	}

}
