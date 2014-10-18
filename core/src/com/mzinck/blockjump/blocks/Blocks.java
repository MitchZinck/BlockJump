package com.mzinck.blockjump.blocks;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.mzinck.blockjump.Player;

public class Blocks {

	private ArrayList<Rectangle> blocksMoving = new ArrayList<Rectangle>();
	private ArrayList<Rectangle> blocksStationary = new ArrayList<Rectangle>();
	private long lastDropTime;
	private Player player;
	private boolean fall;

	public Blocks(Player player) {
		this.setPlayer(player);
	}

	public void update() {
		fall = true;
		player.setMaxSpeedLeft(20);
		player.setMaxSpeedRight(20);
				
		for(Rectangle rect : blocksStationary) {
			overLappCheck(rect, player.getPlayerRectangle());
		}
		
		for(int z = 0; z < blocksMoving.size(); z++) {
			Rectangle bl = blocksMoving.get(z);
			Rectangle pl = player.getPlayerRectangle();
			
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
			
			if(bl.y - 5 > 20) {
				//bl.y -= 200 * Gdx.graphics.getDeltaTime();
				bl.y -= 5;
			} else {
				bl.y = 20;
			}
			
			overLappCheck(bl, pl);
		}
		player.setFalling(fall);
	}
	
	public void overLappCheck(Rectangle block, Rectangle playerRectangle) {
		if(block.overlaps(playerRectangle)) {	
			if(block.y + 105 >= playerRectangle.y && block.y + 95 <= playerRectangle.y) { //Add block.getYMax() so you can have multiple sized blocks
				fall = false;
				player.setY((int) (block.y + 103));
				if(player.getJump() > 15) {
					player.setJump(0);
					player.setJumping(false);
				}
				player.setWaitTime(player.getWaitTime() == 0 ? 5 : player.getWaitTime());
			} else if(playerRectangle.y + 64 >= block.y) {
				if(fall == false) {
					player.setDead(true);
				} else {
					//player.setY((int) (block.y - 65));
					player.setJump(20);
				}
			} else {
				if(block.x - playerRectangle.x < 0) {
					player.setMaxSpeedLeft((int) 0);
					player.setX((int) (block.x + 103));
				} else {				
					player.setMaxSpeedRight((int) 0);
					player.setX((int) (block.x - 65));
				}
			}
		}
	}

	public void spawnBlock() {
		Rectangle block = new Rectangle();
		int rand = MathUtils.random(0, 480 - 100) - MathUtils.random(200);
		rand = rand > 0 ? rand : -rand;  // Trying to get it truly random, though I think just leaving it before doing this would be just as random idk...
		block.x = rand;
		block.y = 1000;
		block.width = 102;
		block.height = 108;
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
