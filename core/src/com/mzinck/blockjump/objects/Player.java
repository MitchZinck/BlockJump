package com.mzinck.blockjump.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mzinck.blockjump.Constants;
import com.mzinck.blockjump.blocks.BlockLogic;

public class Player {
	
	private int x = Constants.SCREEN_WIDTH / 2 - 64 / 2;
	private int y = Constants.BASE_HEIGHT;
	private int jump = 0, jumpSpeed = 20, fallSpeed = 20, dimension = 64, waitTime = 0;
	private int maxSpeedRight = 20, maxSpeedLeft = 20;
	private long currentScore = 0, highScore = 0;
	private boolean jumping = false, falling = true, checkBoth = false;
	private Sound jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
	private TextureRegion jumpTexture; // Make texture region
	private TextureRegion neutralTexture;
	private TextureRegion deadTexture;
	private TextureRegion currentTexture;
	private boolean dead = false, playMenu = false;
	private BlockLogic blocks;
	private Rectangle pz = new Rectangle();
	private float accelVals = 0;
	private String decompiler = "......"; //Release change this

	/**
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Simple_infinite_impulse_response_filter
	 */
	public float lowPass(float input, float output ) {
	    if(output == 0) {
	    	accelVals = input;
	    	return input;
	    }
	    output = output + Constants.ALPHA * (input - output);
	    accelVals = output;
	    return output;
	}

	public Player(TextureRegion jumpTexture, TextureRegion neutralTexture, TextureRegion deadTexture) {
		this.jumpTexture = jumpTexture;
		this.neutralTexture = neutralTexture;
		this.deadTexture = deadTexture;
		currentTexture = neutralTexture;
	}
	
	public void update() {
		currentScore = (y - Constants.BASE_HEIGHT) / 10;
		float acceleration = lowPass(-Gdx.input.getAccelerometerX(), accelVals);
		//if(acceleration >= 0.3F || acceleration <= -0.3F) {
			if(7.5 * acceleration > maxSpeedRight) {
				x += maxSpeedRight;
			} else if(7.5 * acceleration < -maxSpeedLeft) {
				x -= maxSpeedLeft;
			} else {
				if(acceleration > 0) {
					x += Math.round(7.5 * acceleration);
				} else if(acceleration < 0) {
					x += Math.floor(7.5 * acceleration);
				}
			}
			
			if(x + dimension > Constants.SCREEN_WIDTH && acceleration > 0) {
				x = x - Constants.SCREEN_WIDTH;
			} else if(x < 0 && acceleration < 0) {
				x = x + Constants.SCREEN_WIDTH;
			}
		//}

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
	
	public TextureRegion getNeutralTexture() {
		return neutralTexture;
	}
	
	public TextureRegion getDeadTexture() {
		return deadTexture;
	}

	public void setDeadTexture(TextureRegion spriteSheet) {
		this.deadTexture = spriteSheet;
	}

	public void setPlayMenu(boolean playMenu) {
		this.playMenu = playMenu;
	}
	
	public long getCurrentScore() {
		return currentScore;
	}

	public void setJumpTexture(TextureRegion spriteSheet) {
		this.jumpTexture = spriteSheet;
	}

	public void setNeutralTexture(TextureRegion neutralTexture) {
		this.neutralTexture = neutralTexture;
	}

	public long getHighScore() {
		return highScore;
	}
	
	public TextureRegion getCurrentTexture() {
		return currentTexture;
	}

	public void setCurrentTexture(TextureRegion spriteSheet) {
		this.currentTexture = spriteSheet;
	}

	public void setHighScore(long highScore) {
		this.highScore = highScore;
	}
	
	public void kill() {
		dead = true;
	}
	
	public Rectangle getPlayerRectangle() {
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
