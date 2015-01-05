package com.mzinck.blockjump.lava;

import com.mzinck.blockjump.Player;

public class Lava {
	
	private float height = -800;
	
	public Lava() {
		
	}
	
	public void update(Player player) {
		height += 1;
		if(player.getY() <= height) {
			player.kill();
		}
	}
	
	public float getHeight() {
		return height;
	}
}
