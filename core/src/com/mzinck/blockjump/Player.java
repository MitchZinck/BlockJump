package com.mzinck.blockjump;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.mzinck.blockjump.blocks.BlockLogic;

public class Player {
	
	private int x = Constants.SCREEN_WIDTH / 2 - 64 / 2;
	private int y = Constants.BASE_HEIGHT;
	private int jump = 0, jumpSpeed = 20, fallSpeed = 20, dimension = 64, waitTime = 0;
	private int maxSpeedRight = 20, maxSpeedLeft = 20, currentScore = 0, highScore = 0;
	private boolean jumping = false, falling = true, checkBoth = false;
	private Sound jumpSound;
	private Texture jumpTexture = new Texture(Gdx.files.internal("playerjump.png")); // Make texture region
	private Texture neutralTexture = new Texture(Gdx.files.internal("player.png"));
	private Texture deadTexture = new Texture(Gdx.files.internal("playerdead.png"));
	private Texture currentTexture = neutralTexture;
	private boolean dead = false, playMenu = false;
	private BlockLogic blocks;

	public Player() {
		jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
	}
	
	public void update() {
		currentScore = (y - Constants.BASE_HEIGHT) / 10;
		float acceleration = -Gdx.input.getAccelerometerX();
		if(acceleration >= 0.3F || acceleration <= -0.3F) {
			if(10 * acceleration > maxSpeedRight) {
				x += maxSpeedRight;
			} else if(10 * acceleration < -maxSpeedLeft) {
				x -= maxSpeedLeft;
			} else {
				if(acceleration > 0) {
					x += Math.round(10 * acceleration);
				} else if(acceleration < 0) {
					x += Math.floor(10 * acceleration);
				}
			}
			
			if(x + dimension > Constants.SCREEN_WIDTH && acceleration > 0) {
				x = x - Constants.SCREEN_WIDTH;
			} else if(x < 0 && acceleration < 0) {
				x = x + Constants.SCREEN_WIDTH;
			}
		}

		if((Gdx.input.isTouched() || playMenu == true) && jumping == false && waitTime == 0) {
			jumpSound.play();
			jumping = true;
		}

		if(jumping == true) {
			currentTexture = jumpTexture;
			if(jump < Constants.JUMP_LENGTH) {
				y += jumpSpeed;
			} else {
				y -= fallSpeed;
			}
			jump++;
			if(y <= Constants.BASE_HEIGHT) {
				y = Constants.BASE_HEIGHT;
				jump = 0;
				jumping = false;
			}
		} else if(falling == true) {
			y = y - jumpSpeed > Constants.BASE_HEIGHT ? y - jumpSpeed : Constants.BASE_HEIGHT;
			currentTexture = jumpTexture;
		} else {
			currentTexture = neutralTexture;
		}
		
		fallSpeed = 20;
	}
	
	public Texture getDeadTexture() {
		return deadTexture;
	}

	public void setDeadTexture(Texture deadTexture) {
		this.deadTexture = deadTexture;
	}

	public void setPlayMenu(boolean playMenu) {
		this.playMenu = playMenu;
	}
	
	public int getCurrentScore() {
		return currentScore;
	}

	public int getHighScore() {
		return highScore;
	}
	
	public Texture getCurrentTexture() {
		return currentTexture;
	}

	public void setCurrentTexture(Texture currentTexture) {
		this.currentTexture = currentTexture;
	}

	public void setHighScore(int highScore) {
		this.highScore = highScore;
	}
	
	public void setDead(boolean dead) {
		this.dead = dead;
	}
	
	public Rectangle getPlayerRectangle() {
		Rectangle pz = new Rectangle();
		pz.x = x;
		pz.y = y;
		pz.width = 64;
		pz.height = 64;
		return pz;
	}
	
	public int getX() {
		return x;
	}
	
	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public void setBlocks(BlockLogic blocks) {
		this.blocks = blocks;
	}	

	public void setX(int x) {
		this.x = x;
	}
	
	public boolean checkBoth() {
		return checkBoth;
	}

	public void setCheckBoth(boolean checkBoth) {
		this.checkBoth = checkBoth;
	}


	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	public int getJump() {
		return jump;
	}
	
	public int getFallSpeed() {
		return fallSpeed;
	}

	public void setFallSpeed(int fallSpeed) {
		this.fallSpeed = fallSpeed;
	}

	public void setJump(int jump) {
		this.jump = jump;
	}
	
	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	public boolean isJumping() {
		return jumping;
	}
	
	public void setJumping(boolean jumping) {
		this.jumping = jumping;
	}
	
	public boolean isFalling() {
		return falling;
	}	

	public void setFalling(boolean falling) {
		this.falling = falling;
	}

	public int getMaxSpeedRight() {
		return maxSpeedRight;
	}

	public void setMaxSpeedRight(int maxSpeedRight) {
		this.maxSpeedRight = maxSpeedRight;
	}

	public int getMaxSpeedLeft() {
		return maxSpeedLeft;
	}

	public int getJumpSpeed() {
		return jumpSpeed;
	}

	public void setJumpSpeed(int direction) {
		this.jumpSpeed = direction;
	}

	public void setMaxSpeedLeft(int maxSpeedLeft) {
		this.maxSpeedLeft = maxSpeedLeft;
	}
}
