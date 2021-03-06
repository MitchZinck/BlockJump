package com.mzinck.blockjump;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.mzinck.blockjump.mobilecontroller.AdRequestHandler;

public class SplashScreen implements Screen {

    private final BlockJump    game;
    private OrthographicCamera camera;
    private Texture            logo;
    private AdRequestHandler   arh;

    public SplashScreen(final BlockJump game, AdRequestHandler arh) {
        this.game = game;
        this.arh = arh;
        logo = new Texture(Gdx.files.internal("player.png"));
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_HEIGHT,
                Constants.SCREEN_WIDTH);
    }

    @Override
    public void render(float delta) {
        // Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        // Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //
        // camera.update();
        // game.batch.setProjectionMatrix(camera.combined);
        //
        // game.batch.begin();
        // game.batch.draw(logo, (Constants.SCREEN_HEIGHT / 2) - logo.getWidth()
        // / 2, (Constants.SCREEN_WIDTH / 2) - logo.getHeight() / 2);
        // game.batch.end();

        // if (Gdx.input.isTouched()) {
        game.setScreen(new GameScreen(game, arh));
        dispose();
        // }
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
        logo.dispose();
    }
}