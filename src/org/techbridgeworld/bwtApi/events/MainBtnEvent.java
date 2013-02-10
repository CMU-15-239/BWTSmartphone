package org.techbridgeworld.bwtApi.events;

import java.util.Locale;

import org.techbridgeworld.bwtApi.Board;

import javaEventing.EventObject;
import android.util.Log;


public class MainBtnEvent extends EventObject{
	
	private String message; 	// Represents the raw signal
	private int dot; 			// Represents the dot pressed.
	private Board board;		// Gives us a reference to the state of the board.
	//TODO: Add param for what letter was just modified.
	private char update;

	public MainBtnEvent(String message, Board board){
		this.message = message;
		this.board = board;
		//TODO: this.update from this.board.braille
		
		// Set the appropriate dot depending on the character.
		switch (message.toLowerCase(Locale.getDefault()).charAt(0)){
			case 'b': 
				this.dot = 4;
				break;
			case 'c': 
				this.dot = 5;
				break;
			case 'd': 
				this.dot = 6;
				break;
			case 'e': 
				this.dot = 1;
				break;
			case 'f': 
				this.dot = 2;
				break;
			case 'g': 
				this.dot = 3;
				break;
			default: 
				this.dot = -1;
				Log.e ("Salem", "Received unhandled button event '" + message + "'");
		}
	}
	
	// Getters
	public String getMessage(){
		return message;
	}
	public int getDot(){
		return dot;
	}
	public char getUpdate(){
		return update;
	}
}
