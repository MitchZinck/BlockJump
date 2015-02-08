package com.mzinck.blockjump;


public enum GameState {
	
	RUNNING,
	PAUSED,
	PLAY_MENU,
	DEAD_SETUP,
	DEAD,
	STOPPED,
	HIGHSCORES,
	USER_DETAILS;
	
	public static GameState state;
	
}
