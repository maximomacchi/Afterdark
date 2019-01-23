package game;

public class JumpingEnemy extends Entity {

	public JumpingEnemy(String imgName, int startX, int startY, int imagesInWalk, int imagesInIdle) {
		super(imgName, startX, startY, 64, 64, imagesInWalk, imagesInIdle);
	}
	
	/* This routine will determine how a jumping enemy will move. The enemy will jump every couple
	 * of seconds and stay in the same place  */
	public void walkMovement() {
//		Add time to jump timer
//		If specific number of seconds has passed:
//			Make enemy jump
//			Apply physics to enemy so that gravity kicks in and brings the enemy down after they've jumped
	}

}
