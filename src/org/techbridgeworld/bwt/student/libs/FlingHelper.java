package org.techbridgeworld.bwt.student.libs;

import android.view.MotionEvent;

public class FlingHelper {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private MotionEvent start, end;
	private float vX, vY;
	
	public FlingHelper(MotionEvent start, MotionEvent end, float velocityX, float velocityY) {
		this.start = start;
		this.end = end;
		this.vX = velocityX;
		this.vY = velocityY;
	}
	
	public boolean isUp() {
		return (start.getY() - end.getY() > SWIPE_MIN_DISTANCE &&
				Math.abs(vY) > SWIPE_THRESHOLD_VELOCITY);
	}
	
	public boolean isDown() {
		return (end.getY() - start.getY() > SWIPE_MIN_DISTANCE &&
				Math.abs(vY) > SWIPE_THRESHOLD_VELOCITY);
		
	}
	
	public boolean isLeft() {
		return (start.getX() - end.getX() > SWIPE_MIN_DISTANCE &&
				Math.abs(vX) > SWIPE_THRESHOLD_VELOCITY);
	}
	
	public boolean isRight() {
		return (end.getX() - start.getX() > SWIPE_MIN_DISTANCE &&
				Math.abs(vX) > SWIPE_THRESHOLD_VELOCITY);
	}
}
