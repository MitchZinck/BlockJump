package com.mzinck.blockjump;
 
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mzinck.blockjump.blocks.Block;
import com.mzinck.blockjump.blocks.BlockLogic;
import com.mzinck.blockjump.lava.Lava;
import com.mzinck.blockjump.mobilecontroller.AdRequestHandler;
import com.mzinck.blockjump.objects.Player;
import com.mzinck.blockjump.objects.User;
 
public class GameScreen implements Screen {
	
	private final BlockJump game;
	private AdRequestHandler androidHandler;
	private Player player;
	private Lava lava;
	private BlockLogic blockLogic;
	private Music rainMusic;	
	private OrthographicCamera camera;
	
	private Texture spriteSheetTexture = new Texture(Gdx.files.internal("spritesheet.png"));
	private Texture background = new Texture(Gdx.files.internal("background.png")); //Make texture regions
	private Texture backgroundScroll = new Texture(Gdx.files.internal("backgroundscroll.png"));
	private Texture buttonTexture = new Texture(Gdx.files.internal("buttons.png"));
	
	private String result;
	private int textHeight = Constants.SCREEN_HEIGHT - 150;
	private int currentScroll = 3700, nextScroll = 4700;
	private int backgroundX;
	private long lastTimeBg;
	private boolean debug = false;
	private boolean madeTop10 = false;
	public static int cameraFallSpeed = 20;
	
	private Stage pauseStage, pauseMenuStage, playMenuStage, highScoreStage, userDetailStage, twitterStage;
	private Table pauseTable, pauseMenuTable, playMenuTable, highScoreTable, userDetailTable, twitterTable;
	private Button okButton, pauseButton, backButton, highScoreButton, playButton, userDetailsButton, resumeButton, noThanks, twitterButton, rateButton;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	private ArrayList<User> globalList = new ArrayList<User>();
	private SocketChannel socket;
	
    private TextureRegion[] buttons = new TextureRegion[6];
    private TextureRegion[] spriteSheet = new TextureRegion[6];
	private TextureRegion sun = new TextureRegion(spriteSheetTexture, 0.666F, 0, 1F, 0.62352941177F);
	
	private String email, name;
	private TextField emailField, nameField;
	
	private InputMultiplexer im;
	private Viewport fit;
	
	/**
	 * TODO - New Buttons, fix game glitches and smooth it out, add highscores
	 * @param game
	 * @throws ParseException 
	 */ 
 
	public GameScreen(final BlockJump game, AdRequestHandler androidHandler) {				
		this.game = game;		
		this.androidHandler = androidHandler;
		this.androidHandler.hideAds();
		
		spriteSheet[0] = new TextureRegion(spriteSheetTexture, 0, 0, 100, 106); //BlockAsleep
		spriteSheet[1] = new TextureRegion(spriteSheetTexture, 100, 0, 100, 106); //BlockAwake

		player = new Player(new TextureRegion(spriteSheetTexture, 0.42666666666F, 0.62352941177F, 0.63999999999F, 1F), 
				 new TextureRegion(spriteSheetTexture, 0, 0.62352941177F, 0.2133333333333F, 1F), 
				 new TextureRegion(spriteSheetTexture, 0.2133333333333F, 0.62352941177F, 0.42666666666F, 1F));

		if(GameState.state == GameState.PLAY_MENU) {
			player.setJumping(true);
			player.setPlayMenu(true);
		}
		
		player.setBlocks(blockLogic);
		
		lava = new Lava();
		
		blockLogic = new BlockLogic(player, lava, spriteSheet);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT); 		
		fit = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
		
		if(debug == false) {
			game.font = new BitmapFont(Gdx.files.internal("data/font.fnt"),
			         Gdx.files.internal("data/font.png"), false);
			game.font.setScale(1F);
		}
		
		buttons[0] = new TextureRegion(buttonTexture, 0, 0, 0.166666666667F, 1F); //Play
		buttons[1] = new TextureRegion(buttonTexture, 0.166666666667F, 0, 0.333333333334F, 1F); //Highscore	    
		buttons[2] = new TextureRegion(buttonTexture, 0.333333333334F, 0, 0.500000000001F, 1F); //back button
		buttons[3] = new TextureRegion(buttonTexture, 0.500000000001F, 0, 0.666666666668F, 1F); // okbutton
		buttons[4] = new TextureRegion(buttonTexture, 0.666666666668F, 0, 0.833333333335F, 1F); // detailsbutton
		buttons[5] = new TextureRegion(buttonTexture, 0.833333333335F, 0, 1F, 1F);  // twitter button
				
	    createPlayMenu();
	    createPauseButton();
	    createPauseMenu();
	    createTwitterButton();
	    
		im = new InputMultiplexer();
	    
	    Preferences prefs = Gdx.app.getPreferences("Blockjump");
		if (!prefs.contains("name")) {
			player.setHighScore(0);
			im.addProcessor(playMenuStage);
			im.addProcessor(twitterStage);
			Gdx.input.setInputProcessor(im);
			prefs.flush();
		} else {
			byte[] b = Base64.decodeBase64(prefs.getString("a8dsk4kdgashcas").getBytes());
			String highscore = null;
			try {
				highscore = new String(b, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			player.setHighScore(Long.parseLong(highscore));
			prefs.flush();
			im.addProcessor(playMenuStage);
			im.addProcessor(twitterStage);
			Gdx.input.setInputProcessor(im);
		    getUserDetails();
		}
	    
	    blockLogic.spawnBlock();
	    
	    backgroundX = Constants.SCREEN_WIDTH;
	    lastTimeBg = TimeUtils.nanoTime();
	    
	    if(GameState.state == GameState.RUNNING) {
	    	im.clear();
	    	im.addProcessor(pauseStage);
	    	Gdx.input.setInputProcessor(im);
	    }
	}
 
	@Override
	public void render(float delta) {	
		// clear the screen with a dark blue color. The
		// arguments to glClearColor are the red, green
		// blue and alpha component in the range [0,1]
		// of the color to be used to clear the screen.
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
		// tell the camera to update its matrices.		
		camera.update();
		renderGame();	
		renderShapes();
		game.batch.setProjectionMatrix(camera.combined);		
		game.batch.begin();			
		
		float fontWidth;
		if(GameState.state == GameState.DEAD) {
			fontWidth = game.font.getBounds("Endscore:" + Long.toString(player.getCurrentScore())).width;
		    game.font.draw(game.batch, "Endscore:" + Long.toString(player.getCurrentScore()), camera.viewportWidth / 2F - fontWidth / 2F, (float) (camera.viewportHeight * 0.75));
		    fontWidth = game.font.getBounds("Hiscore:" + Long.toString(player.getHighScore())).width;
		    game.font.draw(game.batch, "Hiscore:" + Long.toString(player.getHighScore()), camera.viewportWidth / 2F - fontWidth / 2F, (float) (camera.viewportHeight * 0.70));
		} else if(GameState.state == GameState.PLAY_MENU || GameState.state == GameState.DEAD) {
			fontWidth = game.font.getBounds("Hiscore:" + Long.toString(player.getHighScore())).width;
		    game.font.draw(game.batch, "Hiscore:" + Long.toString(player.getHighScore()), camera.viewportWidth / 2F - fontWidth / 2F, (float) (camera.viewportHeight * 0.70));
		}
		
		game.batch.end();	
		
		if(GameState.state != GameState.RUNNING && GameState.state != GameState.USER_DETAILS) {
			twitterStage.act(Gdx.graphics.getDeltaTime());
		    twitterStage.draw();
		}
		
		switch(GameState.state) {
		
			case RUNNING:		
				if(player.getY() > camera.position.y + (camera.viewportHeight * .10f)) {
					camera.position.y += player.getJumpSpeed();
					textHeight += player.getJumpSpeed();
				} else if(player.getY() < (camera.position.y - (camera.viewportHeight * .5f)) + Constants.BASE_HEIGHT) {
					camera.position.y -= cameraFallSpeed;
					textHeight -= cameraFallSpeed;
				}
				
				if(TimeUtils.millis() - blockLogic.getLastDropTime() > 500) {
					blockLogic.spawnBlock();
				}
				
				player.update();
				if(64 + player.getX() > Constants.SCREEN_WIDTH) {
					player.setCheckBoth(true);
				} else if(player.getX() < 0) {
					player.setCheckBoth(true);
				} else {
					player.setCheckBoth(false);
				}
				blockLogic.update(false);
				lava.update(player);
				
			    pauseStage.act(Gdx.graphics.getDeltaTime());
			    pauseStage.draw();

				if(player.isDead()) {			
					if(player.getCurrentScore() > player.getHighScore()) {
						Preferences prefs = Gdx.app.getPreferences("Blockjump");
						byte[] base64 = null;
						try {
							base64 = Base64.encodeBase64(Long.toString(player.getCurrentScore()).getBytes("UTF-8"));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						prefs.putString("a8dsk4kdgashcas", new String(base64));
						prefs.putBoolean("highscore_submitted", false);
						prefs.flush();
						player.setHighScore(player.getCurrentScore());
					}
					player.setCurrentTexture(player.getDeadTexture());
					androidHandler.showAds();
					GameState.state = GameState.DEAD_SETUP;
				}
				break;
				
			case PLAY_MENU:
				if(!player.isDead()) {
					player.setJumping(true);
					player.update();
				}
			    playMenuStage.act(Gdx.graphics.getDeltaTime());
			    playMenuStage.draw();
				break;
				
			case DEAD_SETUP:
				if(camera.position.y > Constants.SCREEN_HEIGHT / 2) {
					camera.position.y -= camera.position.y / 100;
					textHeight -= camera.position.y / 100;
				} else {
					camera.position.y = Constants.SCREEN_HEIGHT / 2;
					if(madeTop10 == true) {
						createUserInput();
						GameState.state = GameState.USER_DETAILS;
						im.clear();
						im.addProcessor(userDetailStage);
						im.addProcessor(twitterStage);
						Gdx.input.setInputProcessor(im);
						madeTop10 = false;
					} else {
						GameState.state = GameState.DEAD;
						resize(Gdx.app.getGraphics().getWidth(), Gdx.app.getGraphics().getHeight());
						im.clear();
						im.addProcessor(playMenuStage);
						im.addProcessor(twitterStage);
						Gdx.input.setInputProcessor(im);
					}
				}
				break;
				
			case DEAD:
				playMenuStage.act(Gdx.graphics.getDeltaTime());
			    playMenuStage.draw();
				break;
				
			case PAUSED:	
				pauseMenuStage.act(Gdx.graphics.getDeltaTime());
			    pauseMenuStage.draw();
				break;
				
			case HIGHSCORES:
				highScoreStage.act(Gdx.graphics.getDeltaTime());
				highScoreStage.draw();
				break;
				
			case USER_DETAILS:
				userDetailStage.act(Gdx.graphics.getDeltaTime());
				userDetailStage.draw();
				break;
		
		}
		
		if(player.getWaitTime() > 0) {
			player.setWaitTime(player.getWaitTime() - 1);
		}
	    //pauseTable.drawDebug(shapeRenderer); // This is optional, but enables debug lines for tables.	
	}
	
	public void renderBackground() {
		int currentY = (int) (GameState.state == GameState.RUNNING ? player.getY() : camera.position.y);  
		if(currentY < 300) { 
			game.batch.draw(background, 0, 0);
			game.batch.draw(backgroundScroll, backgroundX - Constants.SCREEN_WIDTH, 400);
			game.batch.draw(backgroundScroll, backgroundX, 400);
		} else if(currentY > currentScroll - 1000) {
			game.batch.draw(backgroundScroll, backgroundX - Constants.SCREEN_WIDTH, currentScroll - 2000);
			game.batch.draw(backgroundScroll, backgroundX, currentScroll - 2000);
			
			game.batch.draw(backgroundScroll, backgroundX - Constants.SCREEN_WIDTH, currentScroll);
			game.batch.draw(backgroundScroll, backgroundX, currentScroll);
			if(nextScroll - 1000 < currentY) {
				game.batch.draw(backgroundScroll, backgroundX - Constants.SCREEN_WIDTH, nextScroll);
				game.batch.draw(backgroundScroll, backgroundX, nextScroll);
			}
			if(currentY > nextScroll) {
				currentScroll = nextScroll;
				nextScroll = nextScroll + 2000;
			}			
		} else {
			game.batch.draw(background, 0, 0);
			game.batch.draw(backgroundScroll, backgroundX - Constants.SCREEN_WIDTH, 400);
			game.batch.draw(backgroundScroll, backgroundX, 400);
			
			game.batch.draw(backgroundScroll, backgroundX - Constants.SCREEN_WIDTH, 1700);
			game.batch.draw(backgroundScroll, backgroundX, 1700);
		}		
		if(TimeUtils.nanoTime() - lastTimeBg > 16666666.66){
			backgroundX -= 1;
			lastTimeBg = TimeUtils.nanoTime();
		}
		
		if(backgroundX <= 0){
			backgroundX = Constants.SCREEN_WIDTH;
		}
	}
	
	public void renderGame() {			 
		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		game.batch.setProjectionMatrix(camera.combined);		
		game.batch.begin();		
		renderBackground();
		game.batch.draw(sun, 10, textHeight + 20);
		
		for(Block blz: blockLogic.getBlocksMoving()) {
	        game.batch.draw(blz.getBlockCurrent(), blz.getBlockRectangle().x, blz.getBlockRectangle().y, blz.getBlockRectangle().getWidth(), blz.getBlockRectangle().getHeight());
	    }
		for(Block blz : blockLogic.getBlocksStationary()) {
	        game.batch.draw(blz.getBlockCurrent(), blz.getBlockRectangle().x, blz.getBlockRectangle().y, blz.getBlockRectangle().getWidth(), blz.getBlockRectangle().getHeight());
		}
		if(GameState.state == GameState.DEAD_SETUP || GameState.state == GameState.DEAD) {
			for(Block blz : blockLogic.getBlocksUnderLava()) {
		        game.batch.draw(blz.getBlockCurrent(), blz.getBlockRectangle().x, blz.getBlockRectangle().y, blz.getBlockRectangle().getWidth(), blz.getBlockRectangle().getHeight());
			}
		}
		
		game.batch.draw(player.getCurrentTexture(), player.getX(), player.getY());
		if(64 + player.getX() > Constants.SCREEN_WIDTH) {
			game.batch.draw(player.getCurrentTexture(), player.getX() - Constants.SCREEN_WIDTH, player.getY());
		} else if(player.getX() < 0) {
			game.batch.draw(player.getCurrentTexture(), player.getX() + Constants.SCREEN_WIDTH, player.getY());
		}
		
		if (debug == true) {
			game.font.draw(game.batch, Integer.toString(Gdx.graphics.getFramesPerSecond())
										+ " FPS : " + Float.toString(Math.round(Gdx.input.getAccelerometerX() * 100) / 100)
										+ "X Tilt\n X: " + player.getX() + "\n Y: "
										+ player.getY() + " Highscore: " + Long.toString(player.getCurrentScore()) + "/" + Long.toString(player.getHighScore()), 0, textHeight);
		} else if(GameState.state == GameState.RUNNING) {
			game.font.draw(game.batch, Long.toString(player.getCurrentScore()), 0, textHeight);			
		}
		
		game.batch.end();	
	}
	
	public void renderShapes() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.setProjectionMatrix(camera.combined);
		
	    shapeRenderer.begin(ShapeType.Filled);
	    shapeRenderer.setColor(255, 0, 0, 0.9F);
	    float height = GameState.state == GameState.RUNNING ? 800 : player.getY() + 500;
	    shapeRenderer.rect(0, lava.getHeight() - height, Constants.SCREEN_WIDTH, height);
	    
	    shapeRenderer.end();
	}
	
	/**
	 * Creates a new {@link Stage} for the pause screen.
	 */
	public void createPauseButton() {
	    pauseStage = new Stage();
	    //pauseStage.setViewport(fit);
	    
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
				if(GameState.state == GameState.RUNNING) {
					pauseCall();		
				} else {
					resumeCall();
				}
			}
		});
	}
	
	public void createTwitterButton() {
	    twitterStage = new Stage();
	    twitterTable = new Table();
	    twitterTable.setFillParent(true);
	    twitterStage.addActor(twitterTable);

		TextButtonStyle style = new TextButtonStyle();
		style.up = new TextureRegionDrawable(buttons[3]);
		style.down = new TextureRegionDrawable(buttons[3]);
		style.font = new BitmapFont();
		
		TextButtonStyle style1 = new TextButtonStyle();
		style1.up = new TextureRegionDrawable(buttons[5]);
		style1.down = new TextureRegionDrawable(buttons[5]);
		style1.font = new BitmapFont();

		twitterButton = new Button(style1);
		rateButton = new Button(style);
		
		twitterTable.add(rateButton).padRight(20);
		twitterTable.add(twitterButton).padLeft(20);
		twitterTable.bottom().padBottom(211);
		
		twitterButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Gdx.net.openURI("https://twitter.com/BlockyJump");
			}
		});
		
		rateButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Gdx.net.openURI("https://play.google.com/store/apps/details?id=com.mzinck.blockjump.android");
			}
		});
	}
	
	public void sendHighScore() {	
		String toSend = "2";
		toSend += name + "'" + email + "'" + player.getHighScore();
		ByteBuffer b = ByteBuffer.wrap(toSend.getBytes());		
		
		try {
			socket = SocketChannel.open();
			socket.configureBlocking(false);
			socket.connect(new InetSocketAddress("24.222.27.154", 49593));
			long time = System.currentTimeMillis();
			while(!socket.finishConnect()) {
				if(System.currentTimeMillis() - time > 300) {
					return;
				}
			}
			socket.write(b);
			Preferences prefs = Gdx.app.getPreferences("Blockjump");
			prefs.putBoolean("highscore_submitted", true);
			prefs.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void grabHighScores() {
		Json json = new Json();
		String jsonString = result;
		
		if(jsonString == null || jsonString.equals("")) {
			return;
		}
		
		ArrayList<JsonValue> list = json.fromJson(ArrayList.class, jsonString);
		globalList.clear();
		for(JsonValue l : list) {
			globalList.add(json.readValue(User.class, l));
		}
		
		Preferences prefs = Gdx.app.getPreferences("Blockjump");	
		if(prefs.getBoolean("highscore_submitted") == false) {
			if(globalList.size() < 10) {
				if(prefs.contains("name")) {
					prefs.flush();
					sendHighScore();
				} else {
					madeTop10 = true;
					prefs.flush();
				}	
			} else {
				User user = globalList.get(globalList.size() - 1);
				if(user.getScore() < player.getHighScore()) {
					prefs = Gdx.app.getPreferences("Blockjump");
					if(prefs.contains("name")) {
						prefs.flush();
						sendHighScore();
					} else {
						madeTop10 = true;
						prefs.flush();
					}								
				}
			}
		}
	}
	
	public void getUserDetails() {
		Preferences prefs = Gdx.app.getPreferences("Blockjump");
		name = prefs.getString("name");
		email = prefs.getString("email");
		prefs.flush();
	}
	
	public void getJsonList() {
		HttpRequest httpRequest = new HttpRequest(HttpMethods.GET);
		httpRequest.setUrl("http://24.222.27.154/highscores.json");
		
		Gdx.net.sendHttpRequest(httpRequest, new HttpResponseListener() {

			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				result = httpResponse.getResultAsString();
				Gdx.app.postRunnable(new Runnable() {

					@Override
					public void run() {
						grabHighScores();
						createHighScores();
					}
					
				});
			}

			@Override
			public void failed(Throwable t) {
				Gdx.app.postRunnable(new Runnable() {

					@Override
					public void run() {
						createHighScores();
						if(highScoreStage != null) {
							Dialog dialog = new Dialog("Error", new Skin(Gdx.files.internal("data/uiskin.json")));
							dialog.text("Connection Error");
							dialog.button("OK");
							dialog.show(highScoreStage);
						}
					}
					
				});
				result = "";
			}

			@Override
			public void cancelled() {
				
			}
			
		});
		
//		long time = System.currentTimeMillis();
//		while(result == null) {
//			if(System.currentTimeMillis() - time > 5000) {
//				break;
//			}
//		}
//		
//		return result;
	}
	
	public void createUserInput() {
		userDetailStage = new Stage();
		userDetailStage.setViewport(fit);
		
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		skin.getFont("default-font").setScale(0.6F);
		userDetailTable = new Table(skin);
		userDetailTable.setFillParent(true);
		
		nameField = new TextField("Username", skin);
		nameField.setMaxLength(6);
		emailField = new TextField("Email", skin);
		emailField.setMaxLength(30);
		
		if(madeTop10 == false) {
			userDetailTable.row();
			userDetailTable.add("-- HighScores Information --").colspan(2);
			userDetailTable.row();
			userDetailTable.add("Submit for bi-weekly prizes!").colspan(2);
			userDetailTable.row();
			userDetailTable.add("Click the twitter button for more info!").colspan(2).padBottom(20);
		} else {
			userDetailTable.row();
			userDetailTable.add("You made a top 10 highscore!").colspan(2);
			userDetailTable.row();
			userDetailTable.add("Please input details for prizes!").colspan(2);
			userDetailTable.row();
			userDetailTable.add("Note: Email is for contact only.").colspan(2).padBottom(20);
		}
		
		userDetailTable.row();
		userDetailTable.add(nameField).colspan(1).width(200);
		userDetailTable.add(emailField).colspan(1).width(200);		
        
        TextButtonStyle style = new TextButtonStyle();
		style.up = new TextureRegionDrawable(buttons[3]); 
		style.down = new TextureRegionDrawable(buttons[3]);
		style.font = new BitmapFont();        
		
		okButton = new Button(style);
		
		TextButtonStyle style1 = new TextButtonStyle();
		style1.up = new TextureRegionDrawable(buttons[2]); 
		style1.down = new TextureRegionDrawable(buttons[2]);
		style1.font = new BitmapFont();    
		noThanks = new Button(style1);
		
		userDetailTable.row();
		userDetailTable.add(noThanks).colspan(1).padTop(30);
		userDetailTable.add(okButton).colspan(1).padTop(30);
		
		userDetailStage.addActor(userDetailTable);
		
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Preferences prefs = Gdx.app.getPreferences("Blockjump");
				name = nameField.getText().equals("Username") ? "No Name" : nameField.getText();
				email = emailField.getText().equals("Email") ? "No Email" : emailField.getText();
				prefs.putString("name", name);
				prefs.putString("email", email);
				prefs.flush();
				GameState.state = GameState.PLAY_MENU;
				im.clear();
				im.addProcessor(playMenuStage);
				im.addProcessor(twitterStage);
				Gdx.input.setInputProcessor(im);
			}
		});
		
		noThanks.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Preferences prefs = Gdx.app.getPreferences("Blockjump");
				if(!prefs.contains("name")) {
					email = "No Email";
					name = "No Name";
					prefs.putString("name", "No Name");
					prefs.putString("email", "No Email");
					prefs.flush();
				}
				GameState.state = GameState.PLAY_MENU;
				im.clear();
				im.addProcessor(playMenuStage);
				im.addProcessor(twitterStage);
				Gdx.input.setInputProcessor(im);
			}
		});
	}
	
	public void createHighScores() {     		
		if(globalList.isEmpty()) {
			grabHighScores();
		}
		
		if(name != null) {
			Preferences prefs = Gdx.app.getPreferences("Blockjump");
			if(prefs.getBoolean("highscore_submitted") == false) {
				sendHighScore();
			}
			prefs.flush();
		}
		
		highScoreTable = new Table();
//			
//		Pixmap pm = new Pixmap(1,1, Format.RGB565);
//		pm.setColor(Color.GRAY);
//		pm.fill();
		
		//highScoreTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pm))));	
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		skin.getFont("default-font").setScale(0.6F);
        highScoreTable.setSkin(skin);
        highScoreTable.setFillParent(true);
        highScoreTable.add(name).colspan(3);
        highScoreTable.row();
        highScoreTable.add(email).colspan(3);
        highScoreTable.row();
        
        highScoreTable.add("TOP 10 HIGHSCORES").colspan(3);
        highScoreTable.row();
        
        highScoreTable.add("Name").colspan(1);
        highScoreTable.add("|").colspan(1);
        highScoreTable.add("Score").colspan(1);
        
        highScoreTable.row();
        highScoreTable.add("---------------").colspan(1);
        highScoreTable.add("---------------").colspan(1);
        highScoreTable.add("---------------").colspan(1);
        
        if(globalList.isEmpty()) {
        	highScoreTable.row();
        	highScoreTable.add("Loading...").colspan(3);
        } else {
        
	        int counter = 0;
	        for(User user : globalList) {
	        	counter++;
	        	highScoreTable.row();
	        	highScoreTable.add("(" + counter + ") " + user.getName());
	        	highScoreTable.add("|");
	        	highScoreTable.add(Long.toString(user.getScore()));
	        }
        }

        TextButtonStyle style = new TextButtonStyle();
		style.up = new TextureRegionDrawable(buttons[2]); 
		style.down = new TextureRegionDrawable(buttons[2]);
		style.font = new BitmapFont();
		
        backButton = new Button(style);   
        
        TextButtonStyle style1 = new TextButtonStyle();
		style1.up = new TextureRegionDrawable(buttons[4]); 
		style1.down = new TextureRegionDrawable(buttons[4]);
		style1.font = new BitmapFont();
        userDetailsButton = new Button(style1);
        
        highScoreTable.row();
        highScoreTable.add(backButton).colspan(1).padTop(20);
        highScoreTable.add().colspan(1).pad(20);
        highScoreTable.add(userDetailsButton).colspan(1).padTop(20);
		
		highScoreStage = new Stage();
		highScoreStage.setViewport(fit);
		highScoreStage.addActor(highScoreTable);
        
		im.clear();
		im.addProcessor(highScoreStage);
		im.addProcessor(twitterStage);
		Gdx.input.setInputProcessor(im);
		GameState.state = GameState.HIGHSCORES;
		
		backButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GameState.state = GameState.PLAY_MENU;
				im.clear();
				im.addProcessor(playMenuStage);
				im.addProcessor(twitterStage);
				Gdx.input.setInputProcessor(im);
			}
		});		

		userDetailsButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				createUserInput();
				GameState.state = GameState.USER_DETAILS;
				im.clear();
				im.addProcessor(userDetailStage);
				im.addProcessor(twitterStage);
				Gdx.input.setInputProcessor(im);
			}
		});
	}
	
	public void createPauseMenu() {
		pauseMenuStage = new Stage();
		//pauseMenuStage.setViewport(fit);

	    pauseMenuTable = new Table();
	    pauseMenuTable.setFillParent(true);
	    pauseMenuStage.addActor(pauseMenuTable);
	    
		TextButtonStyle style = new TextButtonStyle();
		style.up = new TextureRegionDrawable(buttons[0]); //1080 x 300
		style.down = new TextureRegionDrawable(buttons[0]);
		style.font = new BitmapFont();

		resumeButton = new Button(style);
		pauseMenuTable.add(resumeButton);
		
		resumeButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				resumeCall();
			}
		});
	}
	
	public void createPlayMenu() {
		playMenuStage = new Stage();
		playMenuStage.setViewport(fit);

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
		styleHS.up = new TextureRegionDrawable(buttons[1]);
		styleHS.down = new TextureRegionDrawable(buttons[1]);
		styleHS.font = new BitmapFont();
		
		highScoreButton = new Button(styleHS);
		playMenuTable.add(highScoreButton).padLeft(100);
		
		playButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(GameState.state == GameState.PLAY_MENU && !player.isDead()) {
					player.setJumping(false);
					player.setPlayMenu(false);
					player.setJump(0);
					resumeCall();
				} else {
					dispose();
					GameState.state = GameState.RUNNING;
					game.setScreen(new GameScreen(game, androidHandler));
				}
			}
		});
		
		highScoreButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {				
			    getJsonList();
			}
		});
	}	
 
	@Override
	public void resize(int width, int height) {

		switch(GameState.state) {
//		
//			case RUNNING:
//				playMenuStage.getViewport().update(width, height, true);
//				break;
//				
//			case PAUSED:	
//				pauseMenuStage.getViewport().update(width, height, true);
//				break;
				
			case HIGHSCORES:
				highScoreStage.getViewport().update(width, height, true);
				break;
				
			case USER_DETAILS:
				userDetailStage.getViewport().update(width, height, true);
				break;
				
			case PLAY_MENU:
				playMenuStage.getViewport().update(width, height, true);
				break;
				
			case DEAD:
				playMenuStage.getViewport().update(width, height, true);
				break;
				
		}
		
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
	
	public void pauseCall() {
		androidHandler.showAds();
		GameState.state = GameState.PAUSED;
		im.clear();
		im.addProcessor(pauseMenuStage);
		im.addProcessor(twitterStage);
		Gdx.input.setInputProcessor(im);
	}
 
	@Override
	public void pause() {
		if(GameState.state == GameState.RUNNING) {
			androidHandler.showAds();
			GameState.state = GameState.PAUSED;
			im.clear();
			im.addProcessor(pauseMenuStage);
			im.addProcessor(twitterStage);
			Gdx.input.setInputProcessor(im);
		}
	}
	
	public void resumeCall() {
		player.setPlayMenu(false);
		androidHandler.hideAds();
		GameState.state = GameState.RUNNING;
		im.clear();
		im.addProcessor(pauseStage);
		im.addProcessor(twitterStage);
		Gdx.input.setInputProcessor(im);
	}
 
	@Override
	public void resume() {

	}
 
	@Override
	public void dispose() {
		background.dispose();
		pauseStage.dispose();
		shapeRenderer.dispose();
	}
}