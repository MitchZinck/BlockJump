package com.mzinck.blockjump;
 
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mzinck.blockjump.androidcontroller.AndroidRequestHandler;
 
public class BlockJump extends Game {
 
	SpriteBatch batch;
	BitmapFont font;
	AndroidRequestHandler arh;
	
	public BlockJump(AndroidRequestHandler arh) {
		this.arh = arh;
	}	

	public void create() {
		GameState.state = GameState.PLAY_MENU;
		batch = new SpriteBatch();
		font = new BitmapFont();
		this.setScreen(new SplashScreen(this, arh));
	}
 
	public void render() {
		super.render();
	}
 
	public void dispose() {
		batch.dispose();
		font.dispose();
	}
 
}