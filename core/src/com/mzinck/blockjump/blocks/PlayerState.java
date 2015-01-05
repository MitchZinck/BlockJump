package com.mzinck.blockjump.blocks;

public enum PlayerState {
	
	OVERLAPS_BOTTOM(0),
	OVERLAPS_LEFT(1),
	OVERLAPS_RIGHT(2),
	OVERLAPS_TOP(3);
	
	private int slot;
	
	private PlayerState(int slot) {
		this.slot = slot;
	}
	
	public int getSlot() {
		return slot;
	}
	
}
