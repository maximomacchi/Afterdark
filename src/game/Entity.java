package game;
import java.awt.Rectangle;

import acm.graphics.GImage;
import acm.program.GraphicsProgram;
import utilities.MainApplication;

/* Stores all characteristics of any Entity object (player and enemies)
 * Moves Entity objects and handles health
 */

public class Entity {
	public static final String PATH = "../media/images/";
	public static final String EXTENSION = ".png";
	public final double MOVEMENT = 4;
	public final double GRAVITY = 2;
	public final double FRICTION = 1;
	public final int HEALTH_VALUE = 100;
	public final double JUMP_VELOCITY = 30;
	private final double MAX_GRAVITY = 30;
	private final double MAX_SPEED = 9;
	private final int MAX_INVULERABILITY = 50;
	
	private double height;
	private double width;
	
	private boolean idle;
	
	protected Direction directionFacing;
	protected String imageName;
	protected boolean amIJumping;
	protected boolean amIWalking;
	protected int currentStep;
	protected int currentIdle;
	protected int walkImages;
	protected int idleImages;
	protected GImage sprite;
	protected double dy;
	protected double dx;
	protected int health;
	protected int invulnerability;
	
	public Entity(String sprite, int startX, int startY, double height, double width, int imagesInWalk, int imagesInIdle) {
		idle = true;
		imageName = sprite;
		currentStep = 0;
		currentIdle = 0;
		walkImages = imagesInWalk;
		idleImages = imagesInIdle;
		this.height = height;
		this.width = width;
		this.sprite = new GImage(getCorrectSprite(), startX, startY);
		this.sprite.setSize(width, height);
		amIJumping = true;
		amIWalking = false;
		dy = 0;
		dx = 0;
		health = HEALTH_VALUE;
		directionFacing = Direction.EAST;
	}

	public boolean amIJumping() {
		return amIJumping;
	}

	public void setJumping(boolean amIJumpin) {
		this.amIJumping = amIJumpin;
	}

	public void move(double x, double y) {
		sprite.move(x, y);
		if(x != 0) {
			currentStep++;
		}
		sprite.setImage(getCorrectSprite());
		sprite.setSize(width, height);
	}
	
	
	public void incrementIdle() {
		currentIdle++;
	}
	
	// Looks for GImage for sprite animation
	private String getCorrectSprite() {
		if (idleImages == 0) {
			int imgToGet = currentStep % walkImages;
			return PATH + imageName+imgToGet+EXTENSION;
		} else {
			if (idle) {
				int imgToGet = currentIdle % idleImages;
				if (directionFacing == Direction.WEST) {
					return PATH + imageName + "/idle/left/" + imageName + imgToGet + ".gif";
				} else {
					return PATH + imageName + "/idle/right/" + imageName + imgToGet + ".gif";
				}
			} else {
				int imgToGet = currentStep % walkImages;
				if (directionFacing == Direction.WEST) {
					return PATH + imageName + "/walk/left/" + imageName + imgToGet + ".gif";
				} else {
					return PATH + imageName + "/walk/right/" + imageName + imgToGet + ".gif";
				}
			}
		}
	}
	
	public void jump() {
//		System.out.println("Jump");
		dy = -JUMP_VELOCITY;
		setJumping(true);
		move(dx, -MOVEMENT);
	}
	
	public void reflectHorizontally() {
		dx = -1 * dx;
		move(dx, 0);
	}
	
	public void reflectVertically() {
		dy = -1 * dy;
	}
	
	public void setLocation(int x, int y) {
		sprite.setLocation(x, y);
	}
	
	public double getX() {
		return sprite.getX();
	}
	
	public double getY() {
		return sprite.getY();
	}
	
	public Rectangle getBox() {
		return new Rectangle((int) sprite.getX(), (int) sprite.getY(), (int) sprite.getWidth(), (int) sprite.getHeight());
	}

	// Moves actual Entity object to new position based on direction
	public void walk(Direction d) {
		directionFacing = d;
		if(d == Direction.EAST) {
			dx += MOVEMENT;
			dx = Math.min(dx, MAX_SPEED);
		}else if(d == Direction.WEST) {
			dx -= MOVEMENT;
			dx = Math.max(dx, -MAX_SPEED);
		}
		idle = false;
		amIWalking = true;
	}
	
	// Applies laws of physics to Entity object, such as applying friction
	// and gravity
	public void walkMovement(Difficulty diff) {
		move(dx, dy);
		if(dx > 0) {
			dx -= FRICTION;
			dx = Math.max(dx, 0);
			directionFacing = Direction.EAST;
		}else if(dx < 0) {
			dx += FRICTION;
			dx = Math.min(dx, 0);
			directionFacing = Direction.WEST;
		} else {
			idle = true;
			amIWalking = false;
			sprite.setImage(getCorrectSprite());
			sprite.setSize(width, height);
		}
		if (amIJumping) {
			dy += GRAVITY;
			dy = Math.min(dy, MAX_GRAVITY);
			dy = Math.max(dy, -JUMP_VELOCITY);
		} else {
			dy = 0;
		}
	}
	
	public double horzCenterDifference() {
		return Game.horzCenter() - getX();
	}
	
	public double vertCenterDifference() {
		return Game.vertCenter() - getY();
	}
	
	public void horzScroll(double distance) {
		sprite.move(distance, 0);
	}
	
	public void vertScroll(double distance) {
		sprite.move(0, distance);
	}

	public double getSpriteHeight() {
		return sprite.getHeight();
	}
	
	public double getSpriteWidth() {
		return sprite.getWidth();
	}
	
	public GImage getSprite() {
		return sprite;
	}

	public boolean isWalking() {
		return amIWalking;
	}

	public void setWalking(boolean amIWalking) {
		this.amIWalking = amIWalking;
	}
	
	public void setHealth(int h) {
		health = h;
	}
	
	public int getHealth() {
		return health;
	}
	
	public void damage(int d) {
		health = Math.max(-1, health - d);
	}
	
	public Direction isDirectionFacing() {
		return directionFacing;
	}

	public void setDirectionFacing(Direction directionFacing) {
		this.directionFacing = directionFacing;
	}
	
	public boolean belowLevel() {
		if (getY() > MainApplication.WINDOW_HEIGHT) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean getAmIJumping() {
		return amIJumping;
	}
	
	public void setAmIJumping(boolean b) {
		amIJumping = b;
	}

	public void setHeight(double height) {
		this.height = height;
	}
	
	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}
	
	// Invulnerability is a variable which makes character invulnerable
	// for short period of time after being hit by enemy
	public void makeInvulnerable() {
		invulnerability = MAX_INVULERABILITY;
	}
	
	public void tick() {
		invulnerability = Math.max(0, invulnerability-1);
	}
	
	public boolean isInvulnerable() {
		if (invulnerability > 0) {
			return true;
		} else {
			return false;
		}
	}
}
