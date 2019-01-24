package game;

/* Stores information of Entity objects within a certain scene
 * Handles bullet and Entity collision
 * Calls to SceneLayout.java to draw actual scene with blocks
 */

import java.awt.Rectangle;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

import acm.graphics.GImage;

import acm.graphics.GObject;


import java.awt.event.KeyEvent;

import utilities.AudioPlayer;
import utilities.MainApplication;

public class Scene implements ActionListener {

	private MainApplication program;
	private SceneLayout layout;
	private List<Bullet> bullets;
	private Entity player;
	private List<Entity> npcs;
	private Direction playerWalkDirection;
	public static int TILE_WIDTH;
	public static int TILE_HEIGHT;
	private AudioPlayer music;
	private static final String BULLET_EAST = "bullet_east.png";
	private static final String BULLET_WEST = "bullet_west.png";
	private String death;
	
	Timer enemyMovementTimer = new Timer(10, this);
	
	public Scene(int tileWidth, int tileHeight, MainApplication app) {
		program = app;
		TILE_WIDTH = tileWidth;
		TILE_HEIGHT = tileHeight;
		layout = new SceneLayout(tileWidth, tileHeight);
		player = new Player("girl", 1000, MainApplication.WINDOW_HEIGHT - 200, 63.0, 63.0, 8, 20);
		bullets = new ArrayList<Bullet>();
		npcs = new ArrayList<Entity>();
		for (int i = 2000; i < 12001; i = i + 2000) {
			Enemy e = new Enemy("sprite", i, MainApplication.WINDOW_HEIGHT - 200, 3, 0);
			npcs.add(e);
		}
//		Add in JumpingEnemy objects to scene
		for (int i = 2500; i < 12001; i += 3000) {
			JumpingEnemy e = new JumpingEnemy("sprite", i, MainApplication.WINDOW_HEIGHT - 200, 3, 0);
			npcs.add(e);
		}
		music = AudioPlayer.getInstance();
		center(player);
		death = ""; 
	}

	/*
	 * This method is called whenever the timer ticks from game.java.
	 * It performs multiple operations every 20ms to keep track of the
	 * character and its various properties.
	 */
	public void tick(Direction walk) {
		player.tick();
		((Player) player).tick();
		Block playerGround = findGround(player);
		if(playerGround == null) {
			player.setJumping(true);
		} else {
			player.setLocation((int) player.getX(), (int) (playerGround.getY() - player.getHeight()));
			player.setJumping(false);
		}
		// Move character in specified direction
		if (walk == Direction.WEST) {
			player.walk(walk);
		} else if (walk == Direction.EAST) {
			player.walk(walk);
		}
		player.incrementIdle();
		checkTerrainCollisions(player);
		Entity entityToRemove = null;
		// Move all enemies and handle all bullet collisions
		for (Entity e : npcs) {
			Block enemyGround = findGround(e);
			if(enemyGround == null) {
				e.setJumping(true);
			} else {
				e.setLocation((int) e.getX(), (int) (enemyGround.getY() - e.getHeight())); 
				e.setJumping(false);
			}
			checkTerrainCollisions(e);
			Bullet bulletToRemove = bulletCollision(e);
			if (bulletToRemove != null) {
				program.remove(bulletToRemove.getSprite());
				bullets.remove(bulletToRemove);
				program.remove(e.getSprite());
				if(program.isSoundOn()){
					music.playSound("../sounds/explosion.wav");
				}
				entityToRemove = e;
			}
			// Handle physics of enemy movement
			e.walkMovement();
			// Damage enemy if hit by bullet
			if (enemyCollision(e)) {
				if (!player.isInvulnerable()) {
					if (program.getDifficulty() == Difficulty.EASY) {
						player.damage(10);
					} else if (program.getDifficulty() == Difficulty.MEDIUM) {
						player.damage(25);
					} else if (program.getDifficulty() == Difficulty.HARD) {
						player.damage(50);
					}
					player.makeInvulnerable();
					death = "You were mauled by a goon!";
				}
			}
			if (e.belowLevel()) {
				e.damage(100000);
			}
		}
		if (entityToRemove != null) {
			npcs.remove(entityToRemove);
		}
		// Remove bullet from screen if it collides with wall
		List<Bullet> bulletsToBeRemoved = new ArrayList<Bullet>();
		for (Bullet b: bullets) {
			if (layout.checkBulletCollisions(b) != Direction.NO_DIRECTION) {
				program.remove(b.getSprite());
				bulletsToBeRemoved.add(b);
			}
		}
		for (Bullet b: bulletsToBeRemoved) {
			bullets.remove(b);
		}
		// Handle physics and side scrolling based on character movement
		player.walkMovement();
		handleScrolling();
		if (wasGoalHit()) {
			program.switchToGameWon();
		}
		if (player.belowLevel()) {
			player.damage(10000000); //More than enough to kill something.
			death = "You were crushed by the fall.";
		}
		if (player.getHealth() < 1) {
			playerKill(death);
		}
	}
	
	/*
	 * This will switch to the game over screen when the character dies
	 * and it will play the appropriate sound as well.
	 */
	private void playerKill(String methodOfDeath) {
		if(program.isSoundOn()){
			music.stopSound("../sounds", "game_music.mp3");
			music.playSound("../sounds/gameOver_sound.wav");
		}
		program.switchToGameOver(methodOfDeath);
	}
	
	private void handleScrolling() {
		if (checkForVerticalScrolling() == Direction.NORTH) {
			vertScroll(amountToScroll(Direction.NORTH));
		} else if (checkForVerticalScrolling() == Direction.SOUTH) {
			vertScroll(amountToScroll(Direction.SOUTH));
		}
		if (checkForHorizontalScrolling() == Direction.WEST) {
			horzScroll(amountToScroll(Direction.WEST));
		} else if (checkForHorizontalScrolling() == Direction.EAST) {
			horzScroll(amountToScroll(Direction.EAST));
		}
	}
	
	// Determine how much to scroll screen based on
	// player movement and position
	private double amountToScroll(Direction d) {
		if (d == Direction.WEST) {
			return Game.leftThreshold() - player.getX() - 1;
		}
		if (d == Direction.EAST) {
			return Game.rightThreshold() - player.getX() + 1;
		}
		if (d == Direction.NORTH) {
			return Game.topThreshold() - player.getY() - 1;
		}
		if (d == Direction.SOUTH) {
			return Game.bottomThreshold() - player.getY() + 1;
		}
		return 0;
	}
	
	private Direction checkForHorizontalScrolling() {
		if (player.getX() < Game.leftThreshold() && terrainLeftOfWindow()) {
			return Direction.WEST;
		}
		if (player.getX() > Game.rightThreshold() && terrainRightOfWindow()) {
			return Direction.EAST;
		}
		return Direction.NO_DIRECTION;
	}
	
	private boolean terrainRightOfWindow() {
		return layout.terrainRightOfWindow();
	}

	private boolean terrainLeftOfWindow() {
		return layout.terrainLeftOfWindow();
	}

	private Direction checkForVerticalScrolling() {
		if (player.getY() < Game.topThreshold() && terrainAboveWindow()) {
			return Direction.NORTH;
		}
		if (player.getY() > Game.bottomThreshold() && terrainBelowWindow()) {
			return Direction.SOUTH;
		}
		return Direction.NO_DIRECTION;
	}
	
	private boolean terrainBelowWindow() {
		return layout.terrainBelowWindow();
	}

	private boolean terrainAboveWindow() {
		return layout.terrainAboveWindow();
	}

	public Direction checkTerrainCollisions(Entity e) {
		return layout.checkCollisions(e);
	}
	
	public Block findGround(Entity e) {
		return layout.findGround(e);
	}
	
	public List<List<Block>> getTerrain() {
		return layout.getTerrain();
	}
	
	/*
	 * This will add enemies with specific characteristics in different locations.
	 */
	public Enemy addEnemy(String sprite, int startX, int startY, int imgsToAnimate) {
		Enemy enemy = new Enemy(sprite, startX, startY, imgsToAnimate, 0);
		GImage e = enemy.getSprite();
		e.setLocation(startX, startY);
		npcs.add(enemy);
		return enemy;
	}
	
	/*
	 * This will add a bullet to the screen starting from particular locations and move it.
	 */
	public Bullet addBullet(Entity owner, double x, double y, Direction d) {
		String sprite;
		if (d == Direction.WEST) {
			sprite = BULLET_WEST;
		} else {
			sprite = BULLET_EAST;
		}
		Bullet bullet = new Bullet(sprite, owner, d);
		GImage b = bullet.getSprite();
		b.setLocation(x, y);
		bullet.move();
		bullets.add(bullet);
		return bullet;
	}
	
	public void removeBullet(Bullet bullet) {
		bullets.remove(bullet);
	}
	
	public void horzScroll(double distance) {
		player.horzScroll(distance);
		if (bullets != null) {
			for (Bullet b : bullets) {
				if (b != null) {
					b.move(distance, 0.0);
				}
			} 
		}
		for (Entity e : npcs) {
			if (e != null) {
				e.move(distance, 0.0);
			}
		}
		layout.horzScroll(distance);
	}
	
	public void vertScroll(double distance) {
		player.vertScroll(distance);
		if (bullets != null) {
			for (Bullet b : bullets) {
				if (b != null) {
					b.move(0.0, distance);
				}
			} 
		}
		for (Entity e : npcs) {
			if (e != null) {
				e.move(0.0, distance); 
			}
		}
		layout.vertScroll(distance);
	}
	
	public void center(Entity focus) {
		double xShift = focus.horzCenterDifference();
		double yShift = focus.vertCenterDifference();
		horzScroll(xShift);
		vertScroll(yShift);
	}
	
	public void playerWalk(Direction d) {
		player.walk(d);
	}
	
	public boolean isPlayerJumping() {
		return player.amIJumping();
	}
	
	public void playerJump() {
		player.jump();
	}

	public Entity getPlayer() {
		return player;
	}
	
	public List<Bullet> getBullets() {
		return bullets;
	}

	public Direction getPlayerWalkDirection() {
		return playerWalkDirection;
	}

	public void setPlayerWalkDirection(Direction playerWalkDirection) {
		this.playerWalkDirection = playerWalkDirection;
	}
	
	public Entity getNPCAtIndex(int i) {
		return npcs.get(i);
	}
	
	// Adds blocks (terrain) to scene level
	public void drawScene() {
		program.add(player.getSprite());
		for (List<Block> row: getTerrain()) {
			for (Block b: row) {
				if (b != null) {
					program.add(b);
				}
			}
		}
	}
	
	// Detect enemy collision with player
	public boolean enemyCollision(Entity npc){
		int i = 0;
		double enemyXPos = 0;
		double enemyYPos = 0;
		double enemyXRegion = 0;
		double enemyYRegion = 0;
		double playerLeftPos = 0;
		double playerRightPos = 0;
		
		enemyXPos = npc.getX();
		enemyYPos = npc.getY();
		enemyXRegion = enemyXPos + npc.getWidth();
		enemyYRegion = enemyYPos + npc.getHeight();
		
		playerLeftPos = player.getX();
		playerRightPos = player.getX() + player.getWidth();
		
		double playerFeetPos = player.getY();
		double playerHeadPos = player.getY() + player.getHeight();
		
		if(playerLeftPos>enemyXPos && playerLeftPos<enemyXRegion &&
		   playerFeetPos > enemyYPos && playerFeetPos < enemyYRegion){
			return true;
		}
		if(playerRightPos>enemyXPos && playerRightPos<enemyXRegion &&
		   playerFeetPos > enemyYPos && playerFeetPos < enemyYRegion){
			return true;
		}
		if(playerHeadPos>enemyYPos && playerHeadPos<enemyYRegion &&
		   playerLeftPos>enemyXPos && playerLeftPos<enemyXRegion){
			return true;
		}
		if(playerHeadPos>enemyYPos && playerHeadPos<enemyYRegion &&
		   playerRightPos>enemyXPos && playerRightPos<enemyXRegion ){
			return true;
		}
		return false;
	}

	// Detects bullet collision with enemy
	public Bullet bulletCollision(Entity e) {
		double bulletXPos = 0;
		double bulletYPos = 0;
		double bulletXRegion = 0;
		double bulletYRegion = 0;
		double enemyLeftPos = 0;
		double enemyRightPos = 0;
		double enemyFeetPos = 0;
		double enemyHeadPos = 0;
		
		for(Bullet b : bullets) {
			bulletXPos = b.getSprite().getX();
			bulletYPos = b.getSprite().getY();
			bulletXRegion = bulletXPos + b.getWidth();
			bulletYRegion = bulletYPos + b.getHeight();
			
			enemyLeftPos = e.getX();
			enemyRightPos = e.getX() + e.getWidth();
			enemyHeadPos = e.getY();
			enemyFeetPos = e.getY() + e.getHeight();
			
			if(enemyLeftPos>bulletXPos && enemyLeftPos<bulletXRegion &&
			   enemyHeadPos < bulletYRegion && enemyFeetPos > bulletYPos){
				return b;
			}
			if(enemyRightPos>bulletXPos && enemyRightPos<bulletXRegion &&
			   enemyFeetPos > bulletYPos && enemyFeetPos < bulletYRegion){
				return b;
			}
			if(enemyHeadPos>bulletYPos && enemyHeadPos<bulletYRegion &&
			   enemyLeftPos>bulletXPos && enemyLeftPos<bulletXRegion){
				return b;
			}
			if(enemyHeadPos>bulletYPos && enemyHeadPos<bulletYRegion &&
			   enemyRightPos>bulletXPos && enemyRightPos<bulletXRegion ){
				return b;
			}
		}
		return null;
	}
	
	public List<Entity> getNPCs() {
		return npcs;
	}
	
	public boolean wasGoalHit() {
		return layout.wasGoalHit();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
