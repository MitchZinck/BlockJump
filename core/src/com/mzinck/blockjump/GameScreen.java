package com.mzinck.blockjump;
 
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.mzinck.blockjump.androidcontroller.AndroidRequestHandler;
import com.mzinck.blockjump.blocks.Block;
import com.mzinck.blockjump.blocks.BlockLogic;
import com.mzinck.blockjump.lava.Lava;
import com.mzinck.blockjump.objects.Player;
import com.mzinck.blockjump.objects.User;
 
public class GameScreen implements Screen {
	
	private final BlockJump game;
	private AndroidRequestHandler androidHandler;
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
	public static int cameraFallSpeed = 20;
	
	private Stage pauseStage, pauseMenuStage, playMenuStage, highScoreStage, userDetailStage;
	private Table pauseTable, pauseMenuTable, playMenuTable, highScoreTable, userDetailTable;
	private Button okButton, pauseButton, backButton, highScoreButton, playButton, userDetailsButton, resumeButton;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	private ArrayList<User> globalList = new ArrayList<User>();
	private SocketChannel socket;
	
    private TextureRegion[] buttons = new TextureRegion[4];
    private TextureRegion[] spriteSheet = new TextureRegion[6];
	private TextureRegion sun = new TextureRegion(spriteSheetTexture, 0.666F, 0, 1F, 0.62352941177F);
	
	private String email, name;
	private TextField emailField, nameField;
	
	/**
	 * TODO - New Buttons, fix game glitches and smooth it out, add highscores
	 * @param game
	 * @throws ParseException 
	 */ 
 
	public GameScreen(final BlockJump game, AndroidRequestHandler androidHandler) {				
		this.game = game;		
		this.androidHandler = androidHandler;
		this.androidHandler.hideAds();
		
		spriteSheet[0] = new TextureRegion(spriteSheetTexture, 0, 0, 0.333F, 0.62352941177F); //BlockAsleep
		spriteSheet[1] = new TextureRegion(spriteSheetTexture, 0.333F, 0, 0.666F, 0.62352941177F); //BlockAwake
		
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
		
		if(debug == false) {
			game.font = new BitmapFont(Gdx.files.internal("data/font.fnt"),
			         Gdx.files.internal("data/font.png"), false);
			game.font.setScale(1.5F);
		}
		
		buttons[0] = new TextureRegion(buttonTexture, 0, 0, 0.333F, 0.5F); //Play
		buttons[1] = new TextureRegion(buttonTexture, 0.333F, 0, 0.666F, 0.5F); //Resume
		buttons[2] = new TextureRegion(buttonTexture, 0.666F, 0, 1F, 0.5F); //Highscore	    
		buttons[3] = new TextureRegion(buttonTexture, 0.666F, 0.5F, 1F, 1F);
				
	    createPlayMenu();
	    createPauseButton();
	    createPauseMenu();
	    createUserInput();
	    
	    Preferences prefs = Gdx.app.getPreferences("Blockjump");
		if (!prefs.contains("name")) {
			GameState.state = GameState.USER_DETAILS;
			player.setHighScore(0);
			Gdx.input.setInputProcessor(userDetailStage);
			prefs.flush();
		} else {
			player.setHighScore(prefs.getLong("highscore"));
			prefs.flush();
		    Gdx.input.setInputProcessor(playMenuStage);
		    getUserDetails();
		}
	    
	    blockLogic.spawnBlock();
	    
	    backgroundX = Constants.SCREEN_WIDTH;
	    lastTimeBg = TimeUtils.nanoTime();
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
		renderGame();		
		
		switch(GameState.state) {
		
			case RUNNING:
				renderShapes();		
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
				blockLogic.update(false);
				lava.update(player);
				
			    pauseStage.act(Gdx.graphics.getDeltaTime());
			    pauseStage.draw();

				if(player.isDead()) {			
					if(player.getCurrentScore() > player.getHighScore()) {
						Preferences prefs = Gdx.app.getPreferences("Blockjump");
						prefs.putLong("highscore", player.getCurrentScore());
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
				player.setJumping(true);
				player.update();
			    playMenuStage.act(Gdx.graphics.getDeltaTime());
			    playMenuStage.draw();
				break;
				
			case DEAD_SETUP:
				if(camera.position.y > Constants.SCREEN_HEIGHT / 2) {
					camera.position.y -= camera.position.y / 100;
					textHeight -= camera.position.y / 100;
				} else {
					camera.position.y = Constants.SCREEN_HEIGHT / 2;
					GameState.state = GameState.DEAD;
					Gdx.input.setInputProcessor(playMenuStage);
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
		if(player.getY() < 300) { 
			game.batch.draw(background, 0, 0);
			game.batch.draw(backgroundScroll, backgroundX - Constants.SCREEN_WIDTH, 400);
			game.batch.draw(backgroundScroll, backgroundX, 400);
		} else if(player.getY() > currentScroll - 1000) {
			game.batch.draw(backgroundScroll, backgroundX - Constants.SCREEN_WIDTH, currentScroll - 2000);
			game.batch.draw(backgroundScroll, backgroundX, currentScroll - 2000);
			
			game.batch.draw(backgroundScroll, backgroundX - Constants.SCREEN_WIDTH, currentScroll);
			game.batch.draw(backgroundScroll, backgroundX, currentScroll);
			if(nextScroll - 1000 < player.getY()) {
				game.batch.draw(backgroundScroll, backgroundX - Constants.SCREEN_WIDTH, nextScroll);
				game.batch.draw(backgroundScroll, backgroundX, nextScroll);
			}
			if(player.getY() > nextScroll) {
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
			player.setCheckBoth(true);
			game.batch.draw(player.getCurrentTexture(), player.getX() - Constants.SCREEN_WIDTH, player.getY());
		} else if(player.getX() < 0) {
			player.setCheckBoth(true);
			game.batch.draw(player.getCurrentTexture(), player.getX() + Constants.SCREEN_WIDTH, player.getY());
		} else {
			player.setCheckBoth(false);
		}
		
		if (debug == true) {
			game.font.draw(game.batch, Integer.toString(Gdx.graphics.getFramesPerSecond())
										+ " FPS : " + Float.toString(Math.round(Gdx.input.getAccelerometerX() * 100) / 100)
										+ "X Tilt\n X: " + player.getX() + "\n Y: "
										+ player.getY() + " Highscore: " + Long.toString(player.getCurrentScore()) + "/" + Long.toString(player.getHighScore()), 0, textHeight);
		} else {
			game.font.draw(game.batch, Long.toString(player.getCurrentScore()), 0, textHeight);
		}
		
		if(GameState.state == GameState.DEAD) {
		    game.font.draw(game.batch, "Endscore:" + Long.toString(player.getCurrentScore()), Constants.SCREEN_WIDTH / 8, 1000);
		    game.font.draw(game.batch, "Hiscore:" + Long.toString(player.getHighScore()), Constants.SCREEN_WIDTH / 8, 900);
		}
		
		game.batch.end();	
	}
	
	public void renderShapes() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		
	    shapeRenderer.begin(ShapeType.Filled);
	    shapeRenderer.setColor(255, 69, 0, 0);
	    shapeRenderer.rect(0, lava.getHeight() - 800, Constants.SCREEN_WIDTH, 800);
	    
	    shapeRenderer.end();
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
				if(GameState.state == GameState.RUNNING) {
					pause();		
				} else {
					resume();
				}
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
			while(!socket.finishConnect()) {
				
			}
			socket.write(b);
			Preferences prefs = Gdx.app.getPreferences("BlockJump");
			prefs.putBoolean("highscore_submitted", true);
			prefs.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void grabHighScores() {
		Json json = new Json();
		String jsonString = getJsonList();
		if(jsonString.equals("")) {
			return;
		} 
		ArrayList<JsonValue> list = json.fromJson(ArrayList.class, jsonString);

		for(JsonValue l : list) {
			globalList.add(json.readValue(User.class, l));
		}
		
		sendHighScore();
	}
	
	public void getUserDetails() {
		Preferences prefs = Gdx.app.getPreferences("Blockjump");
		name = prefs.getString("name");
		email = prefs.getString("email");
		prefs.flush();
	}
	
	public String getJsonList() {
		HttpRequest httpRequest = new HttpRequest(HttpMethods.POST);
		httpRequest.setUrl("24.222.27.154/highscores.json");
		
		Gdx.net.sendHttpRequest(httpRequest, new HttpResponseListener() {

			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				result = httpResponse.getResultAsString();
			}

			@Override
			public void failed(Throwable t) {
				Dialog dialog = new Dialog("Error", new Skin(Gdx.files.internal("data/uiskin.json")));
				dialog.text("You need a network\n connection to access\n or submit highscores!");
				dialog.button("OK");
				dialog.show(highScoreStage);
				result = "";
			}

			@Override
			public void cancelled() {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		return result;
	}
	
	public void createUserInput() {
		userDetailStage = new Stage();
		userDetailTable = new Table(new Skin(Gdx.files.internal("data/uiskin.json")));
		userDetailTable.setFillParent(true);
		
		nameField = new TextField("Username", new Skin(Gdx.files.internal("data/uiskin.json")));
		nameField.setMaxLength(12);
		emailField = new TextField("Email", new Skin(Gdx.files.internal("data/uiskin.json")));
		emailField.setMaxLength(30);
		
		userDetailTable.row();
		userDetailTable.add("-- HighScores Information --").colspan(2);
		userDetailTable.row();
		userDetailTable.add("Submit for bi-weekly prizes!").colspan(2);
		userDetailTable.row();
		userDetailTable.add("Click the twitter button for more info!").colspan(2).padBottom(20);
		
		userDetailTable.row();
		userDetailTable.add(nameField).colspan(1).width(400);
		userDetailTable.add(emailField).colspan(1).width(400);		
        
        TextButtonStyle style = new TextButtonStyle();
		style.up = new TextureRegionDrawable(buttons[3]); 
		style.down = new TextureRegionDrawable(buttons[3]);
		style.font = new BitmapFont();        
		
		okButton = new Button(style);
		Button noThanks = new Button(style);
		
		userDetailTable.row();
		userDetailTable.add(noThanks).colspan(1).padTop(30);
		userDetailTable.add(okButton).colspan(1).padTop(30);
		
		userDetailStage.addActor(userDetailTable);
		
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Preferences prefs = Gdx.app.getPreferences("Blockjump");
				name = nameField.getText();
				email = emailField.getText();
				prefs.putString("name", nameField.getText());
				prefs.putString("email", emailField.getText());
				prefs.flush();
				GameState.state = GameState.PLAY_MENU;
				Gdx.input.setInputProcessor(playMenuStage);
			}
		});
		
		noThanks.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Preferences prefs = Gdx.app.getPreferences("Blockjump");
				email = "No Email";
				name = "No Name";
				prefs.putString("name", "No Name");
				prefs.putString("email", "No Email");
				prefs.flush();
				GameState.state = GameState.PLAY_MENU;
				Gdx.input.setInputProcessor(playMenuStage);
			}
		});
	}
	
	public void createHighScores() {
		Preferences prefs = Gdx.app.getPreferences("Blockjump");
		if(prefs.getBoolean("highscore_submitted") == false) {
			sendHighScore();
		}
		prefs.flush();
		
		Pixmap pm = new Pixmap(1,1, Format.RGB565);
		pm.setColor(Color.GRAY);
		pm.fill();
		
		highScoreTable = new Table();
		//highScoreTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pm))));		
        highScoreTable.setSkin(new Skin(Gdx.files.internal("data/uiskin.json")));
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
        	highScoreTable.add("Turn on");
        	highScoreTable.add("your");
        	highScoreTable.add("connection!");
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
		style.up = new TextureRegionDrawable(buttons[3]); 
		style.down = new TextureRegionDrawable(buttons[3]);
		style.font = new BitmapFont();
        
		highScoreStage = new Stage();
		highScoreStage.addActor(highScoreTable);
		
        backButton = new Button(style);   
        userDetailsButton = new Button(style);
        
        highScoreTable.row();
        highScoreTable.add(backButton).colspan(3).padTop(20);
        highScoreTable.row();
        highScoreTable.add(userDetailsButton).colspan(3).padTop(20);
        
		backButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GameState.state = GameState.PLAY_MENU;
				Gdx.input.setInputProcessor(playMenuStage);
			}
		});		

		userDetailsButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GameState.state = GameState.USER_DETAILS;
				Gdx.input.setInputProcessor(userDetailStage);
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
				if(GameState.state == GameState.PLAY_MENU) {
					player.setJumping(false);
					player.setPlayMenu(false);
					player.setJump(0);
					resume();
				} else if(GameState.state == GameState.DEAD) {
					dispose();
					GameState.state = GameState.RUNNING;
					Gdx.input.setInputProcessor(pauseStage);
					game.setScreen(new GameScreen(game, androidHandler));
				} 
			}
		});
		
		highScoreButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
			    createHighScores();
				GameState.state = GameState.HIGHSCORES;
				Gdx.input.setInputProcessor(highScoreStage);
				grabHighScores();
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
		androidHandler.showAds();
		GameState.state = GameState.PAUSED;
	    Gdx.input.setInputProcessor(pauseMenuStage);
	}
 
	@Override
	public void resume() {
		player.setPlayMenu(false);
		androidHandler.hideAds();
		GameState.state = GameState.RUNNING;
	    Gdx.input.setInputProcessor(pauseStage);
	}
 
	@Override
	public void dispose() {
		background.dispose();
		pauseStage.dispose();
		shapeRenderer.dispose();
	}
}