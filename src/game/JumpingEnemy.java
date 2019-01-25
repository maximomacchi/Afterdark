package game;

public class JumpingEnemy extends Entity {
	
	private int timerNum;

	public JumpingEnemy(String imgName, int startX, int startY, int imagesInWalk, int imagesInIdle) {
		super(imgName, startX, startY, 64, 64, imagesInWalk, imagesInIdle);
		timerNum = 0;
	}
	
	/* This routine will determine how a jumping enemy will move. The enemy will jump every couple
	 * of seconds and stay in the same place  */
	public void walkMovement(Difficulty diff) {
//		Apply physics to enemy so that gravity kicks in and brings the enemy down after they've jumped
		super.walkMovement(diff);
//		Add time to jump timer
		timerNum++;
//		If specific number of seconds has passed:
		switch(diff) {
		case EASY:
			if ((timerNum % 300) == 50) {
//				Make enemy jump
				super.jump();
			}
			break;
		case MEDIUM:
			if ((timerNum % 200) == 50) {
				super.jump();
			}
			break;
		case HARD:
			if ((timerNum % 100) == 50) {
				super.jump();
			}
			break;
		default:
			if ((timerNum % 300) == 50) {
//				Make enemy jump
				super.jump();
			}
			break;
			
		}
		
	}

}
