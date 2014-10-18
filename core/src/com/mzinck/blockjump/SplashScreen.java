package com.mzinck.blockjump;
 
import java.awt.Color;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
 
public class SplashScreen implements Screen {
 
	private final BlockJump game; 
	private OrthographicCamera camera;
	private Texture logo;
 
	public SplashScreen(final BlockJump game) {
		this.game = game;		
		logo = new Texture(Gdx.files.internal("logo.png"));		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480); 
	}
 
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
 
		game.batch.begin();
		game.batch.draw(logo, 400 - logo.getWidth() / 2, 240 - logo.getHeight() / 2);
		game.batch.end();
 
		if (Gdx.input.isTouched()) {
			game.setScreen(new GameScreen(game));
			dispose();
		}
	}
 
	@Override
	public void resize(int width, int height) {
		
	}
 
	@Override
	public void show() {
		
	}
 
	@Override
	public void hide() {
		
	}
 
	@Override
	public void pause() {
		
	}
 
	@Override
	public void resume() {
		
	}
 
	@Override
	public void dispose() {
		
	}
}