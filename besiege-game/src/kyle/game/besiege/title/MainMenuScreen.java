/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.title;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.Assets;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.Random;
import kyle.game.besiege.battle.BPoint;
import kyle.game.besiege.battle.BattleMap;
import kyle.game.besiege.battle.Simulation;

import static kyle.game.besiege.battle.BattleMap.RAINDROP_COLOR;
import static kyle.game.besiege.battle.BattleMap.SNOW_COLOR;

//import com.esotericsoftware.kryo.io.Input;

// TODO separate MainMenuScreen from background.
public class MainMenuScreen implements Screen {
//	private static final String TITLE = "CHIEFTAIN";
	private static final String TITLE_1 = "C H I E F T A I N";
	private static final String TITLE = "W A R C H I E F";

	private static TextureRegion region = new TextureRegion(new Texture("whitepixel.png"));
	private TextureRegion tree;

	private final int PAD_BELOW_TITLE = 100;
	private final int PAD_ABOVE_TITLE = -20;
	private final int WIDTH = 500;
	private final int SEPARATE = 120;
	private final int MAX_NAME = 15;
	private final int FIXED_SIZE_BOTTOM = 100;
	
	private static final float WIND_SPEED_INTENSITY = 0.2f;

	private static final float DARKNESS = 0.5f;
	
	enum BackgroundType{Forest, Tundra, Beach, Jungle};
	BackgroundType backgroundType;
	
	enum ScreenState{Title, InputName};
	
	ScreenState state;
	
	private BesiegeMain main;
	private Stage stage;
	private Table mainTable;
	private Table lowerTable;
//	private Button buttonNew;
//	private Button buttonLoad;
	private Label labelTitle;
	private Label labelNew;
	private Label labelLoad;
	private Label quickBattle;
	
	private LabelStyle styleTitle;
	private LabelStyle styleButtons;
	
	TextField tf;
	
	private Array<Candle> candles;
	
	private float wind_speed;
	
	private float count;
//	private ButtonStyle bs;
	
	private int candleX;
	private int candleY;

	private class TitleBackground extends Group {
		// Rain/snow
		private int currentRainIndex;
		private BPoint[] raindrops;
		private Color rainColor;
		private final float SECONDS_PER_DROP = 1f;
		private int raindropsPerFrame; // this needs to be based on raindrop_count
		private int raindrop_count = Random.getRandomInRange(50, 100);
		private float rainDrawOffsetX;
		private float rainDrawOffsetY;
		private TextureRegion white = new TextureRegion(new Texture("whitepixel.png"));
		private float X_SNOW_SPEED = Random.getRandomInRange(1.f, 2.5f);
		private float Y_SNOW_SPEED = Random.getRandomInRange(1.5f, 2f);
		private boolean X_RIGHT = Random.coinflip();

		// colors of rectangles, top to bottom
        private final Color[] sunset = {
//                new Color(68 / 256f, 0 / 256f, 76 / 256f, 1),
                new Color(45 / 256f, 0 / 256f, 50 / 256f, 1),
                new Color(60 / 256f, 0 / 256f, 30 / 256f, 1),
                new Color(80 / 256f, 0 / 256f, 0 / 256f, 1),
//                new Color(199 / 256f, 1 / 256f, 26 / 256f, 1),
                // this color is replaced according to background type.
                Color.BLACK,
        };

        private final Color[] night = {
				// Dark blue
				new Color(20 / 256f, 17 / 256f, 42 / 256f, 1), 
				new Color(42 / 256f, 14 / 256f, 50 / 256f, 1),
				new Color(60 / 256f, 30 / 256f, 50 / 256f, 1), 

				// this color is replaced according to background type.
				Color.BLACK,
			};

        private final Color[][] colorsets = {
//                sunset,
                night
        };

        private Color[] colors;
		
		private class Star {
			private float x, y, size;
			public Star(double x, double y, double size) {
				this.x = (float) x;
				this.y = (float) y;
				this.size = (float) size;
			}
		}
		private Star[] stars = new Star[40];
		private Star[] starsFewer = new Star[30];

		public TitleBackground(BackgroundType backgroundType) {
//		    colors = getColorSet(backgroundType).clone();
            colors = getRandomColorSet();
		    randomizeBrightness(colors);

			colors[colors.length - 1] = getGroundColor(backgroundType);
			for (int i = 0; i < stars.length; i++) {
				stars[i] = new Star(Math.random(), Math.random() * 1 / colors.length , 1.5 + Math.random());
			}
			for (int i = 0; i < starsFewer.length; i++) {
				starsFewer[i] = new Star(Math.random(), Math.random() / colors.length, 0.75 + Math.random() / 2);
			}

			// initialize rain
			raindrops = new BPoint[raindrop_count];
			for (int i = 0; i < raindrop_count; i++) {
				raindrops[i] = new BPoint(0, 0);
			}

			float delta = 1.0f / 60;
			raindropsPerFrame = (int) (raindrop_count * delta / SECONDS_PER_DROP);
			if (raindropsPerFrame < 1) raindropsPerFrame = 1;
			System.out.println("raindrops per frame: " + raindropsPerFrame);

			if (isRaining()) {
				this.rainColor = RAINDROP_COLOR;
				this.rainColor.mul(DARKNESS*1f);
			}
			else {
				this.rainColor = SNOW_COLOR;
			}
		}

		private void randomizeBrightness(Color[] colors) {
		    double effect = Random.getRandomInRange(1.25, 1.75);
		    for (Color color : colors) {
		        color.r *= effect;
                color.g *= effect;
                color.b *= effect;
            }
        }

		private Color[] getColorSet(BackgroundType backgroundType) {
		    switch (backgroundType) {
                case Beach:
                    return sunset;
                case Forest:
                    return sunset;
                case Tundra:
                    return sunset;
                default: return night;
            }
        }

		private Color[] getRandomColorSet() {
		    return (Color[]) Random.getRandomValue(colorsets);
        }
		
		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			Color prev = batch.getColor();
			int stripeHeight = BesiegeMain.HEIGHT / colors.length;
			for (int i = 0; i < colors.length; i++) {
				Color color = colors[i];
				batch.setColor(color);
				batch.draw(region, 0,  BesiegeMain.HEIGHT - (i + 1) * stripeHeight, BesiegeMain.WIDTH, stripeHeight);
			}
			batch.setColor(prev);
			
			for (Star star : stars) {
				batch.setColor(1, 1, 1, 0.6f);
                if (Math.random() < 0.3) batch.setColor(1, 1, 1, 0.5f);
                batch.draw(region, star.x * BesiegeMain.WIDTH, BesiegeMain.HEIGHT - star.y  * BesiegeMain.HEIGHT, star.size, star.size);
			}
			for (Star star : starsFewer) {
			    // Add flickering effect
				batch.setColor(1, 1, 1, 0.8f);
				if (Math.random() < 0.3) batch.setColor(1, 1, 1, 0.5f);
                batch.draw(region, star.x * BesiegeMain.WIDTH, BesiegeMain.HEIGHT - star.y  * BesiegeMain.HEIGHT - BesiegeMain.HEIGHT *1/colors.length, star.size, star.size);
			}
			// draw trees
			int treeHeight = (int) (stripeHeight * (1 + 1.2f/16));
			int treeWidth = treeHeight;
			for (int i = 0; i * treeWidth < BesiegeMain.WIDTH; i++) {
				batch.setColor(1, 1, 1, 1f);
				batch.draw(tree, i * treeWidth, BesiegeMain.HEIGHT - stripeHeight * 3 - stripeHeight * 1.1f /16, treeWidth, treeHeight);
			}


			// draw rain/snow
			boolean drawSnow = true;
			if (drawSnow) {
				if (isRaining() || isSnowing()) {

					// Only add new drops while the list is not full. otherwise
					// recycle drops
					if (currentRainIndex < raindrops.length) {
						for (int i = 0; i < raindropsPerFrame; i++) {
							raindrops[currentRainIndex].pos_x = (int) (Math.random() * BesiegeMain.WIDTH);

							raindrops[currentRainIndex].pos_y = (int) (Math.random() * BesiegeMain.HEIGHT);
							// increment current rain index proportionally to the number of drops, otherwise the speed of drops
							// will be too low.
							currentRainIndex++;
						}
					}

					Color c = batch.getColor();
					Color mycolor = rainColor;

					// we can figure out how much to fade drop by calculating distance between its index and currentrainindex,
					// then divide by array size to get between 0 and 1 yay

					// eg if index = 20 and currentIndex = 10, diff is (20-10)/40 = 1/4
					// eg if index = 20 and currentIndex = 25, diff is 40 + (20 - 25) =

					// This is nice because it makes the raindrops look "softer"
					float alpha_minus = .2f;

					if (isSnowing()) {
						mycolor = SNOW_COLOR;

						rainDrawOffsetX += X_SNOW_SPEED;
						rainDrawOffsetY += Y_SNOW_SPEED;

						alpha_minus = 0.0f; // makes snow last longer

						//					if (rainDrawOffsetX >= this.total_width) rainDrawOffsetX = 0;
					}

					for (int i = 0; i < currentRainIndex; i++) {
						BPoint p = raindrops[i];
						double indexDiff = i - currentRainIndex;
						if (indexDiff < 0) indexDiff += raindrops.length;

						// Add a bit of variation based on the index
						float drawAtY = (p.pos_y + rainDrawOffsetY) % (BesiegeMain.HEIGHT);
						// IN this case, we make color based on height.
						mycolor.a = 1 - (drawAtY / BesiegeMain.HEIGHT);

						float drawAtX = (p.pos_x + rainDrawOffsetX + (mycolor.a * 200 * (i - raindrops.length/2f)/raindrops.length)) % (BesiegeMain.WIDTH);

						// This makes some drops disappear early for an interesting effect
						mycolor.a *= (i * 1.f / raindrops.length);
						if (mycolor.a < 0) mycolor.a = 0;

						batch.setColor(mycolor);

						if (X_RIGHT)
							batch.draw(white, (drawAtX), BesiegeMain.HEIGHT - (drawAtY), 5, 5);
						else
							batch.draw(white, (BesiegeMain.WIDTH - drawAtX), BesiegeMain.HEIGHT - (drawAtY), 5, 5);
					}

					batch.setColor(c);
				}
			}
		}
	}


	public boolean isSnowing() {
//		return true;
		return backgroundType == BackgroundType.Tundra;
	}

	public boolean isRaining() {
		return false;
//		return !this.isSnowing() && stage.raining;
	}


	private Color getGroundColor(BackgroundType type) {
        switch (type) {
            case Forest:
                return new Color(30 / 256f, 45 / 256f, 30 / 256f, 1);
            case Jungle:
                return new Color(20 / 256f, 40 / 256f, 35 / 256f, 1);
            case Tundra:
                return new Color(150 / 256f, 150 / 256f, 150 / 256f, 1);
            case Beach:
                return new Color(160 / 256f, 150 / 256f, 120 / 256f, 1);
            default:
                return Color.BLACK;
        }
    }
	
	private TextureRegion getTreeRegion(BackgroundType type) {
        switch (type) {
            case Forest:
                return new TextureRegion(new Texture("blacktree5.png"));
            case Tundra:
                return new TextureRegion(new Texture("blacktree5.png"));
            case Beach:
                return new TextureRegion(new Texture("palmtree.png"));
            case Jungle:
                return new TextureRegion(new Texture("blacktree3.png"));
            default:
                return new TextureRegion(new Texture("null"));
        }
	}
	
	public MainMenuScreen(BesiegeMain main) {
		this.main = main;
		
		state = ScreenState.Title;
		
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		stage.addListener(new InputListener());
		
		backgroundType = randomBackgroundType();
//		backgroundType = BackgroundType.Tundra;
		
		tree = getTreeRegion(backgroundType);
		
		TitleBackground bg = new TitleBackground(backgroundType);
		stage.addActor(bg);

		candleX = 0; // will be initialized in Resize
		candleY = 0; 
		
		wind_speed = (float) (Math.random() - .5) * WIND_SPEED_INTENSITY;
		
		Candle candle = new Candle(candleX, candleY, 2, 1, 3);
		candles = new Array<Candle>();
		candles.add(candle);
		stage.addActor(candle);
		
		mainTable = new Table();
		lowerTable = new Table();
//		topTable.debug();
		mainTable.defaults().expandX().center();
		mainTable.setX(0);
		mainTable.setY(0);
		mainTable.setWidth(WIDTH);
		mainTable.setHeight(BesiegeMain.HEIGHT);
		
		lowerTable.defaults().expandX().center();
		lowerTable.setX(0);
		lowerTable.setY(0);
		lowerTable.setWidth(WIDTH);
		lowerTable.setHeight(BesiegeMain.HEIGHT / 2);
		
		styleTitle = new LabelStyle();
		styleTitle.font = Assets.pixel150;
		styleButtons = new LabelStyle();
		styleButtons.font = Assets.pixel40;
//		bs = new ButtonStyle();

		float titleFade = 3.5f;
		float titleDelay = 1.5f;
		labelTitle = new Label(TITLE, styleTitle);
//		labelTitle.addAction(Actions.fadeIn(3));
		labelTitle.addAction(Actions.sequence(Actions.alpha(0), Actions.delay(titleDelay), Actions.fadeIn(titleFade)));

		float buttonFade = 2f;
		float buttonDelay = 3f;
		labelNew = new Label("N E W", styleButtons);
		labelLoad = new Label("L O A D", styleButtons);
		labelNew.addAction(Actions.sequence(Actions.alpha(0), Actions.delay(buttonDelay), Actions.fadeIn(buttonFade)));
		labelLoad.addAction(Actions.sequence(Actions.alpha(0),  Actions.delay(buttonDelay), Actions.fadeIn(buttonFade)));

		quickBattle = new Label("Q U I C K   B A T T L E", styleButtons);
		quickBattle.addAction(Actions.sequence(Actions.alpha(0), Actions.delay(buttonDelay), Actions.fadeIn(buttonFade)));

		mainTable.add(labelTitle).padBottom(PAD_BELOW_TITLE).padTop(PAD_ABOVE_TITLE);
		mainTable.row();
		mainTable.add(lowerTable).height(FIXED_SIZE_BOTTOM);
		
		labelNew.addListener(new ClickListener() {
//			public boolean touchDown(InputEvent event, float x,
//					float y, int pointer, int button) {
//				return true;
//			}
			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				clickNew();
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				setNewOn();
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				setNewOff();
			}
		});
		labelLoad.addListener(new ClickListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				clickLoad();
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				setLoadOn();
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				setLoadOff();
			}
		});
		quickBattle.addListener(new ClickListener() {
			public boolean touchDown(InputEvent event, float x,
									 float y, int pointer, int button) { return true; }
			public void touchUp(InputEvent event, float x, float y,
								int pointer, int button) {
				clickBattleLaunch();
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				setQuickBattleOn();
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				setQuickBattleOff();
			}
		});

		lowerTable.add(labelNew).right().padRight(SEPARATE);
		lowerTable.add(labelLoad).left().padLeft(SEPARATE);
		lowerTable.row();
		lowerTable.add(quickBattle).left().colspan(2).expandX().padTop(25);

		Label placeholder = new Label(" ", styleButtons);
		lowerTable.row();
		lowerTable.add(placeholder).colspan(2);
		
		stage.addActor(mainTable);
		
		
		count = 0f;
		this.checkForFile();
	}
	
	private BackgroundType randomBackgroundType() {
		return (BackgroundType) Random.getRandomValue(BackgroundType.values());
	}
	
	private void setNewOn() {
		labelNew.setColor(Color.LIGHT_GRAY);
	}
	
	private void setNewOff() {
		labelNew.setColor(Color.WHITE);
	}
	
	private void setLoadOn() {
		labelLoad.setColor(Color.LIGHT_GRAY);
	}
	
	private void setLoadOff() {
		labelLoad.setColor(Color.WHITE);
	}

	private void setQuickBattleOn() {
		quickBattle.setColor(Color.LIGHT_GRAY);
	}

	private void setQuickBattleOff() {
		quickBattle.setColor(Color.WHITE);
	}
	
	@Override
	public void render(float delta) {
		updateCandles();
		
		stage.act(delta);
		Gdx.gl.glClearColor(.1f,.1f,.1f,0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Table.drawDebug(stage);
		stage.draw();

		mainTable.act(delta);

		// check for a file every second
		if (count > 1) {
			checkForFile();
			count = 0;
		}
		count += delta;
		
		if (Gdx.input.isKeyPressed(Keys.ENTER) && 
				state == ScreenState.Title)
			this.clickNew();
	}
	
	private void checkForFile() {
		com.esotericsoftware.kryo.io.Input input = null;
		try {
			FileHandle file = Gdx.files.local(MapScreen.getSaveFileName());
			input = new com.esotericsoftware.kryo.io.Input(file.read());
			input.close();
			this.labelLoad.setVisible(true);
		}
		catch (Exception e) {
			System.out.println("no save file");
			this.labelLoad.setVisible(false);
		}
	}
	

	private void updateCandles() {
		if (Math.random() < 0.001f) {
			wind_speed = (float) (Math.random() - .5) * WIND_SPEED_INTENSITY;
		}
//		System.out.println(wind_speed);

		
		Candle.wind_x = wind_speed;
		
		// update text lighting
//		if (Math.random() < .15)
//			updateTextLighting();

		for (Candle candle : candles) {
			candle.generateParticles();
			candle.updatePositions();
		}
	}

	@Override
	public void resize(int width, int height) {
		BesiegeMain.HEIGHT = height;
		BesiegeMain.WIDTH = width;
		mainTable.setWidth(width);
		mainTable.setHeight(height);
		lowerTable.setWidth(width);
		lowerTable.setHeight(height/2);	
		// If it's "BESIEGE"
//		this.candleX = (int) (width/2) - 7;
//		this.candleY = (int) (-height/2) - 120;
		this.candleX = (int) (width/2);
		this.candleY = (int) (-height * 1/5) + 40;
		this.candles.first().move(candleX, candleY);
		
//		topTable.getCells().get(1).padLeft(width-WIDTH).padRight(SEPARATE);
//		topTable.getCells().get(2).padRight(width-WIDTH).padLeft(SEPARATE);
		stage.setViewport(BesiegeMain.WIDTH, BesiegeMain.HEIGHT, false);
	}
	
	private void clickNew() {
		this.state = ScreenState.InputName;
		
		lowerTable.clear();
		Label prompt = new Label("Who are you?", styleButtons);
		lowerTable.add(prompt).expandX();
		lowerTable.row();
		
		TextFieldStyle tfs = new TextFieldStyle();
		tfs.fontColor = Color.WHITE;
		tfs.font = Assets.pixel64;
		tf = new TextField("", tfs);
		
		// need a second textfield so that it's automatically selected?
		TextField tf2 = new TextField("", tfs);
		tf2.setVisible(false);
		
		lowerTable.add(tf).center().colspan(2).align(Align.center);
		tf.setMaxLength(MAX_NAME);
		lowerTable.row();
		lowerTable.add(tf2).colspan(2).height(0); // needed to autoselect
		tf.next(true);
		tf.setTextFieldFilter(new TextFieldFilter() {
			public boolean acceptChar(TextField textField, char key) {
				expand();
//				System.out.println("Hello");
				return true;
			}
		});
		tf.addListener(new InputListener() {
			 public boolean keyDown (InputEvent event, int keyCode) {
                 if (keyCode == Input.Keys.ENTER) enterName(((TextField)event.getTarget()).getText());
                 return true;
			 }
		});
		tf2.next(false);
	}
	
	private void expand() {
		this.tf.setWidth(50000);
	}

	// TODO more complex battle simulator UI:
	// 	 add it to the main menu. player can choose either quick battle or campaign. more expensive?
	// 			requires adding a complex menu to main screen. Instead, make the menu (table) a separate class, replace the "Warchief" title with that table.
	private void clickBattleLaunch() {
		// Launch a new battle, with specified options.
		Simulation simulation = new Simulation();
		main.setScreen(simulation.getMapScreen()); // eventually make mainMenu
	}

	private void clickLoad() {
		// check if null
		System.out.println("clicked load");
		main.loadMapScreen();
		main.setScreen(main.mapScreen);
	}
	
	private void enterName(String text) {
		System.out.println("User entered: " + text); 
		
		if (text.equals("")) text = "Defacto";
		
		main.createMapScreen(text);
//		main.setPlayerName(text);
		main.setScreen(main.mapScreen);
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
