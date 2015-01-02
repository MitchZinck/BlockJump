package com.mzinck.blockjump.blocks;

import java.security.SecureRandom;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.mzinck.blockjump.Constants;
import com.mzinck.blockjump.GameScreen;
import com.mzinck.blockjump.Player;

public class BlockLogic {

	private ArrayList<Block> blocksMoving = new ArrayList<Block>(); // Redo this so that it updates in the Block.java for eachblock
	private ArrayList<Block> blocksStationary = new ArrayList<Block>();
	private long lastDropTime;
	private static Player player;
	private boolean fall;
	private Rectangle lastSpawn;

	public BlockLogic(Player player) {
		this.setPlayer(player);
	}
	
	/**
	 * Collision and block updating method. TO-DO: Clean and seperate collision and block updating.	Make Blocks update in the Block.java class
	 * @param twice
	 * 		Determines whether the thread is updating for a second time. Updates a second time if the player is halfway across the screen.
	 */
	public void update(boolean twice) {
		GameScreen.cameraFallSpeed = player.getFallSpeed();
		Rectangle plr = player.getPlayerRectangle();
		Block bl;
		
		if(twice == false) {
			fall = true;
			player.setMaxSpeedLeft(20);
			player.setMaxSpeedRight(20);
		} else { 		
			int x = 0;			
			if(player.getX() + player.getDimension() > Constants.SCREEN_WIDTH) {
				x = player.getX() - Constants.SCREEN_WIDTH;
			} else if(player.getX() < 0) {
				x = player.getX() + Constants.SCREEN_WIDTH;
			}
			plr.x = x;
		}
				
		for(Block block : blocksStationary) {
			block.setBlockCurrent(block.getBlockSleep());
			overLappCheck(block, plr, false);
		}
		
		for(int z = 0; z < blocksMoving.size(); z++) {
			boolean moving = true;
			bl = blocksMoving.get(z);
			bl.setBlockCurrent(bl.getBlockSleep());
			
			for(int i = 0; i < blocksMoving.size(); i++) {
				Rectangle zv = blocksMoving.get(i).getBlockRectangle();
				if(bl.getBlockRectangle().overlaps(zv) && bl.getBlockRectangle() != zv) {
					moving = false;
					blocksMoving.remove(bl);
					blocksStationary.add(bl);
				}
			}
			
			for(int i = 0; i < blocksStationary.size(); i++) {
				Block zv = blocksStationary.get(i);
				if(bl.getBlockRectangle().overlaps(zv.getBlockRectangle()) && bl != zv) {
					moving = false;
					blocksMoving.remove(bl);
					blocksStationary.add(bl);
				}
			}
			
			if(twice == false && moving == true) {
				if(bl.getBlockRectangle().y - 5 > Constants.BASE_HEIGHT) {
					//bl.y -= 200 * Gdx.graphics.getDeltaTime();
					bl.getBlockRectangle().y -= 5;
				} else {
					bl.getBlockRectangle().y = Constants.BASE_HEIGHT;
				}
			}
			
			overLappCheck(bl, plr, moving);
		}
		
		player.setFalling(fall);
		if(twice == false) {			
			if(player.checkBoth()) {
				update(true);
			}
		}
	}
	
	public void overLappCheck(Block block, Rectangle playerRectangle, boolean blockIsFalling) {
		if(block.getBlockRectangle().overlaps(playerRectangle)) {	
			if(isOnBlock(block.getBlockRectangle(), playerRectangle)) {
				GameScreen.cameraFallSpeed = 5;
				fall = false;
				player.setY((int) (block.getBlockRectangle().y + block.getBlockRectangle().height - 1));
				if(player.getJump() > Constants.JUMP_LENGTH) {
					player.setJump(0);
					player.setJumping(false);
				}
				player.setWaitTime(player.getWaitTime() == 0 ? 5 : player.getWaitTime());
			} else {
				if(isUnderBlock(block.getBlockRectangle(), playerRectangle)) {
					if(fall == false && blockIsFalling == true) {
						player.setDead(true);
					} else {
						player.setJump(Constants.JUMP_LENGTH + 1);
					}
					
				}
				
				if(overLapsRightSide(block.getBlockRectangle(), playerRectangle)) {
					player.setMaxSpeedLeft((int) 0);
					player.setX((int) (block.getBlockRectangle().x + block.getBlockRectangle().width - 1));
//					if(-Gdx.input.getAccelerometerX() < -0.1F) {
//						player.setFallSpeed(3); //avalanche side jumping
//						player.setJump(0);
//						player.setJumping(false);
//					}
				} else if(overLapsLeftSide(block.getBlockRectangle(), playerRectangle)){				
					player.setMaxSpeedRight((int) 0);
					player.setX((int) (block.getBlockRectangle().x - player.getDimension() + 1));
//					if(-Gdx.input.getAccelerometerX() > 0.1F) {
//						player.setFallSpeed(3);
//						player.setJump(0);
//						player.setJumping(false);
//					}
				}	
			}
			
			block.setBlockCurrent(block.getBlockAwake());
		}
	}
	
	public static boolean isUnderBlock(Rectangle block, Rectangle playerRectangle) {
		return playerRectangle.y + player.getDimension() >= block.y && playerRectangle.y + player.getDimension() <= block.y + player.getJumpSpeed();
	}
	
	public static boolean isOnBlock(Rectangle block, Rectangle playerRectangle) {
		return block.y + block.height >= playerRectangle.y && block.y + (block.height - player.getFallSpeed()) <= playerRectangle.y;
	}
	
	public static boolean overLapsRightSide(Rectangle block, Rectangle playerRectangle) {
		return block.x + block.width >= playerRectangle.x && block.x + (block.width - player.getMaxSpeedLeft()) <= playerRectangle.x;
	}
	
	public static boolean overLapsLeftSide(Rectangle block, Rectangle playerRectangle) {
		return block.x <= playerRectangle.x + player.getDimension() && block.x + player.getMaxSpeedRight() >= playerRectangle.x + player.getDimension() ;
	}

	public void spawnBlock() {
		Rectangle rect = new Rectangle();
		int rand = MathUtils.random(Constants.SCREEN_WIDTH - 153);
		rect.x = rand;	
		if(lastSpawn != null) {
			rect.y = lastSpawn.y + 170; 
		} else {
			rect.y = player.getY() + 1300;
		}
		rand =  MathUtils.random(100);
		rect.width = rand > 50 ? 102 : 153;
		rect.height = rand > 50 ? 108 : 162;
		
		if(lastSpawn != null) {
			if(lastSpawn.x < 360) {
				int xSpawn = (int) (lastSpawn.x + 153 > 360 ? lastSpawn.x + 160 : 360);
				rect.x = MathUtils.random(xSpawn, Constants.SCREEN_WIDTH - 153);
			} else {
				int xSpawn = (int) (lastSpawn.x < 360 ? lastSpawn.x - 10 : 360);
				rect.x = MathUtils.random(0, xSpawn);
			}
		}
		
		Block block = new Block(new Texture(Gdx.files.internal("blockasleep.png")), new Texture(Gdx.files.internal("blockawake.png")), rect);
		block.setBlockCurrent(new Texture(Gdx.files.internal("blockasleep.png")));
		
		blocksMoving.add(block);
		lastDropTime = TimeUtils.millis();
		lastSpawn = new Rectangle(rect.x, rect.y, rect.width, rect.height);
	}

	public long getLastDropTime() {
		return lastDropTime;
	}

	public ArrayList<Block> getBlocksMoving() {
		return blocksMoving;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public ArrayList<Block> getBlocksStationary() {
		return blocksStationary;
	}

	public void setBlocksStationary(ArrayList<Block> blocksStationary) {
		this.blocksStationary = blocksStationary;
	}

}
