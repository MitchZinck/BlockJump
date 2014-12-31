package com.mzinck.blockjump;
 
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.TimeUtils;
import com.mzinck.blockjump.blocks.Block;
import com.mzinck.blockjump.blocks.BlockLogic;
import com.mzinck.blockjump.lava.Lava;
 
public class GameScreen implements Screen {
	
	private final BlockJump game;
	private Lava lava;
	private BlockLogic blockLogic;
	private Music rainMusic;
	private Texture background = new Texture(Gdx.files.internal("background.png"));
	private Texture sun = new Texture(Gdx.files.internal("sun.png"));
	private Texture backgroundScroll = new Texture(Gdx.files.internal("backgroundscroll.png"));
	private OrthographicCamera camera;
	private int textHeight = Constants.SCREEN_HEIGHT;
	private Player player;
	private int currentScroll = 3700, nextScroll = 4700;
	private boolean debug = true;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	public static int cameraFallSpeed = 20;
 
	public GameScreen(final BlockJump game) {
		this.game = game;
		
		player = new Player();
		blockLogic = new BlockLogic(player);
		lava = new Lava();
		player.setBlocks(blockLogic); 
		// load the drop sound effect and the rain background "music"
		//rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		//rainMusic.setLooping(true);
 
		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT); 

	    blockLogic.spawnBlock();
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
		if(player.getY() > camera.position.y + (camera.viewportHeight * .10f)) {
			camera.position.y += 20;
			textHeight += 20;
		} else if(player.getY() < (camera.position.y - (camera.viewportHeight * .5f)) + Constants.BASE_HEIGHT) {
			camera.position.y -= 20;
			textHeight -= cameraFallSpeed;
		}
		
		camera.update();		
 
		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		game.batch.setProjectionMatrix(camera.combined);
		
		game.batch.begin();
		
		if(player.getY() < 300) { 
			game.batch.draw(background, 0, 0);
		} else if(player.getY() > currentScroll) {
			game.batch.draw(backgroundScroll, 0, currentScroll);
			if(nextScroll - 1000 < player.getY()) {
				game.batch.draw(backgroundScroll, 0, nextScroll);
			}
			if(player.getY() > nextScroll) {
				currentScroll = nextScroll;
				nextScroll = nextScroll + 2000;
			}
			
		} else {
			game.batch.draw(background, 0, 0);
			game.batch.draw(backgroundScroll, 0, 1700);
		}
		game.batch.draw(sun, 600, textHeight - 100);
		
		for(Block blz: blockLogic.getBlocksMoving()) {
	        game.batch.draw(blz.getBlockCurrent(), blz.getBlockRectangle().x, blz.getBlockRectangle().y, blz.getBlockRectangle().getWidth(), blz.getBlockRectangle().getHeight());
	    }
		for(Block blz : blockLogic.getBlocksStationary()) {
	        game.batch.draw(blz.getBlockCurrent(), blz.getBlockRectangle().x, blz.getBlockRectangle().y, blz.getBlockRectangle().getWidth(), blz.getBlockRectangle().getHeight());
		}
		
		game.batch.draw(player.getCurrentTexture(), player.getX(), player.getY());
		if(64 + player.getX() > Constants.SCREEN_WIDTH) {
			player.setCheckBoth(true);
			game.batch.draw(player.getCurrentTexture(), player.getX() - Constants.SCREEN_WIDTH, player.getY());
		} else if(player.getX() < 0) {
			player.setCheckBoth(true);
			game.batch.draw(player.getCurrentTexture(), player.getX() + Constants.SCREEN_WIDTH, player.getY());
		} else {
			player.setCheckBoth(false);
		}
		if (debug == true) {
			game.font.draw(game.batch,Integer.toString(Gdx.graphics.getFramesPerSecond())
										+ " FPS : " + Float.toString(Math.round(Gdx.input.getAccelerometerX() * 100) / 100)
										+ "X Tilt\n X: " + player.getX() + "\n Y: "
										+ player.getY() + " Highscore: " + player.getCurrentScore() + "/" + player.getHighScore() + "Blocks: " + blockLogic.blah, 0, textHeight);
		} else {
			/* TO -DO */
		}
		
		game.batch.end();
		
		shapeRenderer.setProjectionMatrix(camera.combined);
		
	    shapeRenderer.begin(ShapeType.Filled);
	    shapeRenderer.setColor(0, 1, 0, 1);
	    shapeRenderer.rect(0, lava.getHeight() - 800, Constants.SCREEN_WIDTH, 800);
	    
	    shapeRenderer.end();
	 	
		if(TimeUtils.millis() - blockLogic.getLastDropTime() > 1000) {
			blockLogic.spawnBlock();
		}
		
		player.update();
		blockLogic.update(false);
		lava.update(player);
		
		
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
		for(Block blz : blockLogic.getBlocksMoving()) {
			blz.getBlockCurrent().dispose();
			blz.getBlockAwake().dispose();
			blz.getBlockSleep().dispose();
		}
		
		for(Block blz : blockLogic.getBlocksMoving()) {
			blz.getBlockCurrent().dispose();
			blz.getBlockAwake().dispose();
			blz.getBlockSleep().dispose();
		}

		player.getCurrentTexture().dispose();
		background.dispose();
		sun.dispose();
	}
}