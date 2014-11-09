package com.mzinck.blockjump;
 
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.mzinck.blockjump.blocks.Blocks;
 
public class GameScreen implements Screen {
	
	private final BlockJump game;
	private Blocks blocks;
	private Texture blockImage;
	private Texture bucketImage;
	private Music rainMusic;
	private OrthographicCamera camera;
	private Player player;
	private boolean debug = true;
 
	public GameScreen(final BlockJump game) {
		this.game = game;
		
		player = new Player();
		blocks = new Blocks(player);
		player.setBlocks(blocks);
 
		// load the images for the droplet and the bucket, 64x64 pixels each
		blockImage = new Texture(Gdx.files.internal("block.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
 
		// load the drop sound effect and the rain background "music"
		//rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		//rainMusic.setLooping(true);
 
		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 480, 800); 

	    blocks.spawnBlock();
	}
 
	@Override
	public void render(float delta) {
		// clear the screen with a dark blue color. The
		// arguments to glClearColor are the red, green
		// blue and alpha component in the range [0,1]
		// of the color to be used to clear the screen.
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
		// tell the camera to update its matrices.
		camera.update();
 
		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		game.batch.setProjectionMatrix(camera.combined);
 
		// begin a new batch and draw the bucket and
		// all drops
		game.batch.begin();
		for(Rectangle blz: blocks.getBlocksMoving()) {
	         game.batch.draw(blockImage, blz.x, blz.y);
	    }
		for(Rectangle blz : blocks.getBlocksStationary()) {
			game.batch.draw(blockImage, blz.x, blz.y);
		}
		game.batch.draw(bucketImage, player.getX(), player.getY());
		if(64 + player.getX() > 480) {
			game.batch.draw(bucketImage, player.getX() - 480, player.getY());
		} else if(player.getX() < 0) {
			game.batch.draw(bucketImage, player.getX() + 480, player.getY());
		}		
		if (debug == true) {
			game.font.draw(game.batch,Integer.toString(Gdx.graphics.getFramesPerSecond())
										+ " FPS : " + Float.toString(Math.round(Gdx.input.getAccelerometerX() * 100) / 100)
										+ "X Tilt\n X: " + player.getX() + "\n Y: "
										+ player.getY() + " Blah: ", 0, 800);
		}
		game.batch.end();
		
		if(TimeUtils.millis() - blocks.getLastDropTime() > 2500) {
			blocks.spawnBlock();
		}
		
		player.update();
		blocks.update();
		
		if(player.getWaitTime() > 0) {
			player.setWaitTime(player.getWaitTime() - 1);
		}

		if(player.isDead()) {
			game.setScreen(new SplashScreen(game));
			dispose();
		}
	}
	
	
 
	@Override
	public void resize(int width, int height) {
	}
 
	@Override
	public void show() {
		// start the playback of the background music
		// when the screen is shown
		//rainMusic.play();
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
		blockImage.dispose();
		bucketImage.dispose();
	}
}