package com.mzinck.blockjump.blocks;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Block {
	
	private Texture blockCurrent, blockSleep, blockAwake;
	private Rectangle blockRectangle;
	
	public Block(Texture blockSleep, Texture blockAwake, Rectangle blockRectangle) {
		this.blockSleep = blockSleep;
		this.blockAwake = blockAwake;
		this.blockRectangle = blockRectangle;
	}

	public Texture getBlockSleep() {
		return blockSleep;
	}

	public void setBlockSleep(Texture blockSleep) {
		this.blockSleep = blockSleep;
	}

	public Texture getBlockAwake() {
		return blockAwake;
	}

	public void setBlockAwake(Texture blockAwake) {
		this.blockAwake = blockAwake;
	}

	public Rectangle getBlockRectangle() {
		return blockRectangle;
	}

	public void setBlockRectangle(Rectangle blockRect) {
		this.blockRectangle = blockRect;
	}

	public Texture getBlockCurrent() {
		return blockCurrent;
	}

	public void setBlockCurrent(Texture blockCurrent) {
		this.blockCurrent = blockCurrent;
	}
			
}
