package com.mzinck.blockjump;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.mzinck.blockjump.blocks.Blocks;

public class Player {
	
	private int x = 480 / 2 - 64 / 2;
	private int y = 20;
	private int jump = 0, jumpSpeed = 20, fallSpeed = 20, width = 64, height = 64, waitTime = 0;
	private int maxSpeedRight = 20, maxSpeedLeft = 20;
	private boolean jumping = false, falling = true, checkBoth = false;
	private Sound jumpSound;
	private Blocks blocks;
	private boolean dead = false;

	public Player() {
		jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
	}
	
	public void update() {
		float acceleration = -Gdx.input.getAccelerometerX();
		if(acceleration >= 0.1F || acceleration <= -0.1F) {
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
			
			if(x + width > 480 && acceleration > 0) {
				x = x - 480;
			} else if(x < 0 && acceleration < 0) {
				x = x + 480;
			}
		}

		if(Gdx.input.isTouched() && jumping == false && waitTime == 0) {
			jumpSound.play();
			jumping = true;
		}

		if(jumping == true) {
			if(jump < 15) {
				y += jumpSpeed;
			} else {
				y -= fallSpeed;
			}
			jump++;
			if(y <= 20) {
				y = 20;
				jump = 0;
				jumping = false;
			}
		} else if(y > 20 && falling == true) {
			y = y - jumpSpeed > 20 ? y - jumpSpeed : 20;
		}
		
		fallSpeed = 20;
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
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public void setBlocks(Blocks blocks) {
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
