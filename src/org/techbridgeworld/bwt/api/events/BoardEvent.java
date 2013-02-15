package org.techbridgeworld.bwt.api.events;

import javaEventing.EventObject;


public class BoardEvent extends EventObject{

	String message;
	
	public BoardEvent(String message){
		this.message = message;
	}
	
	// Getters
	public String getMessage(){
		return message;
	}
}
