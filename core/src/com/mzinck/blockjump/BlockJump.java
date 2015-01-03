package com.mzinck.blockjump;
 
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
public class BlockJump extends Game {
 
	SpriteBatch batch;
	BitmapFont font;

	public void create() {
		GameState.state = GameState.PLAY_MENU;
		batch = new SpriteBatch();
		font = new BitmapFont();
		this.setScreen(new SplashScreen(this));
	}
 
	public void render() {
		super.render();
	}
 
	public void dispose() {
		batch.dispose();
		font.dispose();
	}
 
}