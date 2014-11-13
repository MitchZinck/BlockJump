package com.mzinck.blockjump.blocks;

import java.security.SecureRandom;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.mzinck.blockjump.Player;

public class Blocks {

	private ArrayList<Rectangle> blocksMoving = new ArrayList<Rectangle>();
	private ArrayList<Rectangle> blocksStationary = new ArrayList<Rectangle>();
	private long lastDropTime;
	private static Player player;
	private boolean fall;
	private SecureRandom random = new SecureRandom();

	public Blocks(Player player) {
		this.setPlayer(player);
	}
	
	/**
	 * Collision and block updating method. TO-DO: Clean and seperate collision and block updating.	
	 * @param twice
	 * 		Determines whether the thread is updating for a second time. Updates a second time if the player is halfway across the screen.
	 */
	public void update(boolean twice) {
		Rectangle plr = player.getPlayerRectangle();
		
		if(twice == false) {
			fall = true;
			player.setMaxSpeedLeft(20);
			player.setMaxSpeedRight(20);
		} else { 		
			int x = 0;			
			if(player.getX() + player.getWidth() > 480) {
				x = player.getX() - 480;
			} else if(player.getX() < 0) {
				x = player.getX() + 480;
			}
			plr.x = x;
		}
				
		for(Rectangle rect : blocksStationary) {
			overLappCheck(rect, plr, false);
		}
		
		for(int z = 0; z < blocksMoving.size(); z++) {
			Rectangle bl = blocksMoving.get(z);
			Rectangle pl = plr;
			
			for(int i = 0; i < blocksMoving.size(); i++) {
				Rectangle zv = blocksMoving.get(i);
				if(bl.overlaps(zv) && bl != zv) {
					blocksMoving.remove(bl);
					blocksStationary.add(bl);
				}
			}
			
			for(int i = 0; i < blocksStationary.size(); i++) {
				Rectangle zv = blocksStationary.get(i);
				if(bl.overlaps(zv) && bl != zv) {
					blocksMoving.remove(bl);
					blocksStationary.add(bl);
				}
			}
			
			if(twice == false) {
				if(bl.y - 5 > 20) {
					//bl.y -= 200 * Gdx.graphics.getDeltaTime();
					bl.y -= 5;
				} else {
					bl.y = 20;
				}
			}
			
			overLappCheck(bl, pl, true);
		}
		
		player.setFalling(fall);
		if(twice == false) {			
			if(player.checkBoth()) {
				update(true);
			}
		}
	}
	
	public void overLappCheck(Rectangle block, Rectangle playerRectangle, boolean blockIsFalling) {
		if(block.overlaps(playerRectangle)) {	
			if(isOnBlock(block, playerRectangle)) {
				fall = false;
				player.setY((int) (block.y + block.width + 1));
				if(player.getJump() > 15) {
					player.setJump(0);
					player.setJumping(false);
				}
				player.setWaitTime(player.getWaitTime() == 0 ? 5 : player.getWaitTime());
			} else {
				if(isUnderBlock(block, playerRectangle)) {
					if(fall == false && blockIsFalling == false) {
						player.setDead(true);
					} else {
						player.setJump(20);
					}
				}
				
				if(overLapsRightSide(block, playerRectangle)) {
					player.setMaxSpeedLeft((int) 0);
					player.setX((int) (block.x + block.width - 1));
//					if(-Gdx.input.getAccelerometerX() < -0.1F) {
//						player.setFallSpeed(3); //avalanche side jumping
//						player.setJump(0);
//						player.setJumping(false);
//					}
				} else if(overLapsLeftSide(block, playerRectangle)){				
					player.setMaxSpeedRight((int) 0);
					player.setX((int) (block.x - player.getWidth() + 1));
//					if(-Gdx.input.getAccelerometerX() > 0.1F) {
//						player.setFallSpeed(3);
//						player.setJump(0);
//						player.setJumping(false);
//					}
				}	
			}		
		}
	}
	
	public static boolean isUnderBlock(Rectangle block, Rectangle playerRectangle) {
		return playerRectangle.y + player.getWidth() >= block.y && playerRectangle.y + player.getWidth() <= block.y + 20;
	}
	
	public static boolean isOnBlock(Rectangle block, Rectangle playerRectangle) {
		return block.y + block.height >= playerRectangle.y && block.y + (block.height - 20) <= playerRectangle.y;
	}
	
	public static boolean overLapsRightSide(Rectangle block, Rectangle playerRectangle) {
		return block.x + block.width >= playerRectangle.x && block.x + (block.width - player.getMaxSpeedLeft()) <= playerRectangle.x;
	}
	
	public static boolean overLapsLeftSide(Rectangle block, Rectangle playerRectangle) {
		return block.x <= playerRectangle.x + player.getWidth() && block.x + player.getMaxSpeedRight() >= playerRectangle.x + player.getWidth() ;
	}

	public void spawnBlock() {
		Rectangle block = new Rectangle();
		int rand = random.nextInt(380) + 50;
		block.x = rand;
		block.y = player.getY() + 1000;
		rand = random.nextInt(100);
		block.width = rand > 50 ? 102 : 153;
		block.height = rand > 50 ? 108 : 162;
		blocksMoving.add(block);
		lastDropTime = TimeUtils.millis();
	}

	public long getLastDropTime() {
		return lastDropTime;
	}

	public ArrayList<Rectangle> getBlocksMoving() {
		return blocksMoving;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public ArrayList<Rectangle> getBlocksStationary() {
		return blocksStationary;
	}

	public void setBlocksStationary(ArrayList<Rectangle> blocksStationary) {
		this.blocksStationary = blocksStationary;
	}

}
