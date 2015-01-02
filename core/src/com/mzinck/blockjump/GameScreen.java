package com.mzinck.blockjump;
 
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.TimeUtils;
import com.mzinck.blockjump.blocks.Block;
import com.mzinck.blockjump.blocks.BlockLogic;
import com.mzinck.blockjump.lava.Lava;
 
public class GameScreen implements Screen {
	
	private final BlockJump game;
	private Player player;
	private Lava lava;
	private BlockLogic blockLogic;
	private Music rainMusic;
	private Texture background = new Texture(Gdx.files.internal("background.png")); //Make texture regions
	private Texture sun = new Texture(Gdx.files.internal("sun.png"));
	private Texture backgroundScroll = new Texture(Gdx.files.internal("backgroundscroll.png"));
	private Texture buttonTexture = new Texture(Gdx.files.internal("buttons.png"));
	private OrthographicCamera camera;
	private int textHeight = Constants.SCREEN_HEIGHT;
	private int currentScroll = 3700, nextScroll = 4700;
	private boolean debug = false;
	private Stage pauseStage, pauseMenuStage, playMenuStage;
	private Table pauseTable, pauseMenuTable, playMenuTable;
	private Button resumeButton, playButton, highScoreButton, pauseButton;
	private GameState gameState;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
    
    private TextureRegion[] buttons = new TextureRegion[3];
	
	public static int cameraFallSpeed = 20;
 
	public GameScreen(final BlockJump game) {
		gameState = GameState.PLAY_MENU;
		this.game = game;
		
		player = new Player();	
		player.setJumping(true);
		player.setPlayMenu(true);
		Preferences prefs = Gdx.app.getPreferences("Blockjump");
		player.setHighScore(prefs.getInteger("highscore"));
		prefs.flush();
		player.setBlocks(blockLogic); 
		
		blockLogic = new BlockLogic(player);
		lava = new Lava();
		// load the drop sound effect and the rain background "music"
		//rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		//rainMusic.setLooping(true);
 
		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT); 
		
		if(debug == false) {
			game.font = new BitmapFont(Gdx.files.internal("data/font.fnt"),
			         Gdx.files.internal("data/font.png"), false);
			game.font.setScale(2F);
		}
		
		buttons[0] = new TextureRegion(buttonTexture, 0, 0, 0.333F, 0.5F); //Play
		buttons[1] = new TextureRegion(buttonTexture, 0.333F, 0, 0.666F, 0.5F); //Resume
		buttons[2] = new TextureRegion(buttonTexture, 0.666F, 0, 1F, 0.5F); //Highscore	    
	    createPlayMenu();
	    createPauseButton();
	    createPauseMenu();
	    Gdx.input.setInputProcessor(playMenuStage);
	    
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
			camera.position.y += player.getJumpSpeed();
			textHeight += player.getJumpSpeed();
		} else if(player.getY() < (camera.position.y - (camera.viewportHeight * .5f)) + Constants.BASE_HEIGHT) {
			camera.position.y -= player.getFallSpeed();
			textHeight -= cameraFallSpeed;
		}
		
		camera.update();		
 
		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		game.batch.setProjectionMatrix(camera.combined);
		
		game.batch.begin();
		
		if(player.getY() < 300) { 
			game.batch.draw(background, 0, 0);
		} else if(player.getY() > currentScroll - 1000) {
			game.batch.draw(backgroundScroll, 0, currentScroll - 2000);
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
		game.batch.draw(sun, 10, textHeight - 100);
		
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
										+ player.getY() + " Highscore: " + Integer.toString(player.getCurrentScore()) + "/" + Integer.toString(player.getHighScore()), 0, textHeight);
		} else {
			game.font.draw(game.batch, Integer.toString(player.getCurrentScore()), 0, textHeight);
		}
		
		game.batch.end();
		
		shapeRenderer.setProjectionMatrix(camera.combined);
		
	    shapeRenderer.begin(ShapeType.Filled);
	    shapeRenderer.setColor(255, 69, 0, 0);
	    shapeRenderer.rect(0, lava.getHeight() - 800, Constants.SCREEN_WIDTH, 800);
	    
	    shapeRenderer.end();
		
		switch(gameState) {
		
			case RUNNING: 
				if(TimeUtils.millis() - blockLogic.getLastDropTime() > 500) {
					blockLogic.spawnBlock();
				}
				player.update();
				blockLogic.update(false);
				lava.update(player);			
			    pauseStage.act(Gdx.graphics.getDeltaTime());
			    pauseStage.draw();
				break;
				
			case PLAY_MENU:
				player.setJumping(true);
				player.update();
			    playMenuStage.act(Gdx.graphics.getDeltaTime());
			    playMenuStage.draw();
				break;
				
			case PAUSED:
				pauseMenuStage.act(Gdx.graphics.getDeltaTime());
			    pauseMenuStage.draw();
	
			    pauseMenuTable.drawDebug(shapeRenderer);
				break;
		
		}
		
		if(player.getWaitTime() > 0) {
			player.setWaitTime(player.getWaitTime() - 1);
		}

		if(player.isDead()) {			
			if(player.getCurrentScore() > player.getHighScore()) {
				Preferences prefs = Gdx.app.getPreferences("Blockjump");
				prefs.putInteger("highscore", player.getCurrentScore());
				prefs.flush();
			}
			dispose();
			game.setScreen(new GameScreen(game));
		}
	    //pauseTable.drawDebug(shapeRenderer); // This is optional, but enables debug lines for tables.	
	}
	
	/**
	 * Creates a new {@link Stage} for the pause screen.
	 */
	public void createPauseButton() {
	    pauseStage = new Stage();
	    pauseTable = new Table();
	    pauseTable.setFillParent(true);
	    pauseStage.addActor(pauseTable);

		TextButtonStyle style = new TextButtonStyle();
		Texture text = new Texture(Gdx.files.internal("pause.png"));
		style.up = new TextureRegionDrawable(new TextureRegion(text, 0, 0, 1F, 1F));
		style.down = new TextureRegionDrawable(new TextureRegion(text, 0, 0, 1F, 1F));
		style.font = new BitmapFont();

		pauseButton = new Button(style);
		pauseTable.add(pauseButton);
		pauseTable.top().right();
		
		pauseButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(gameState == GameState.RUNNING) {
					pause();		
				} else {
					resume();
				}
			}
		});
	}
	
	public void createPauseMenu() {
		pauseMenuStage = new Stage();

	    pauseMenuTable = new Table();
	    pauseMenuTable.setFillParent(true);
	    pauseMenuStage.addActor(pauseMenuTable);
	    
		TextButtonStyle style = new TextButtonStyle();
		style.up = new TextureRegionDrawable(buttons[1]); //1080 x 300
		style.down = new TextureRegionDrawable(buttons[1]);
		style.font = new BitmapFont();

		resumeButton = new Button(style);
		pauseMenuTable.add(resumeButton);
		
		resumeButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				resume();
			}
		});
	}
	
	public void createPlayMenu() {
		playMenuStage = new Stage();

		playMenuTable = new Table();
		playMenuTable.setFillParent(true);
		playMenuStage.addActor(playMenuTable);
	    
		TextButtonStyle style = new TextButtonStyle();
		style.up = new TextureRegionDrawable(buttons[0]); 
		style.down = new TextureRegionDrawable(buttons[0]);
		style.font = new BitmapFont();

		playButton = new Button(style);
		playMenuTable.add(playButton).padRight(100);
		
		TextButtonStyle styleHS = new TextButtonStyle();
		styleHS.up = new TextureRegionDrawable(buttons[2]);
		styleHS.down = new TextureRegionDrawable(buttons[2]);
		styleHS.font = new BitmapFont();
		
		highScoreButton = new Button(styleHS);
		playMenuTable.add(highScoreButton).padLeft(100);
		
		playButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				player.setJumping(false);
				player.setPlayMenu(false);
				player.setJump(0);
				resume();
			}
		});
		
		highScoreButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
			}
		});
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
		gameState = GameState.PAUSED;
	    Gdx.input.setInputProcessor(pauseMenuStage);
	}
 
	@Override
	public void resume() {
		gameState = GameState.RUNNING;
	    Gdx.input.setInputProcessor(pauseStage);
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
		pauseStage.dispose();
		shapeRenderer.dispose();
	}
}