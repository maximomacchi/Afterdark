package game;
import java.awt.event.ActionListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.*;
/*
 * This class is in charge of creating the enemies and provides instructions
 * on where to move them and how fast to move them.
 */
public class Enemy extends Entity{
	
	public static final double MOVEMENT_SPEED = 1;
	
	private int timerNum;

	public Enemy(String imgName, int startX, int startY, int imagesInWalk, int imagesInIdle) {
		super(imgName, startX, startY, 64, 64, imagesInWalk, imagesInIdle);
		this.sprite.setSize(64, 64);
		timerNum = 0;
	}
	
	public void walkMovement(Difficulty diff) {
		super.walk(this.directionFacing);
		super.walkMovement(diff);
		timerNum++;
		if ((timerNum % 126) < 63) {
			dx = MOVEMENT_SPEED;
			this.directionFacing = Direction.EAST;
		}
		else {
			dx = -MOVEMENT_SPEED;
			this.directionFacing = Direction.WEST;
		}
	}

}
