/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.title;


import kyle.game.besiege.Assets;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.MapScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

//import com.esotericsoftware.kryo.io.Input;

public class MainMenuScreen implements Screen {
	private final int PAD = 50;
	private final int PAD2 = 25;
	private final int WIDTH = 500;
	private final int SEPARATE = 50;
	private final int MAX_NAME = 15;
	
	enum ScreenState{Title, InputName};
	
	ScreenState state;
	
	private BesiegeMain main;
	private Stage stage;
	private Table topTable;
//	private Button buttonNew;
//	private Button buttonLoad;
	private Label labelTitle;
	private Label labelNew;
	private Label labelLoad;
	
	private LabelStyle styleTitle;
	private LabelStyle styleButtons;
	
	TextField tf;
	
	private Array<Candle> candles;
	
	private float wind_velocity;
	
	private float count;
//	private ButtonStyle bs;
	
	private int candleX;
	private int candleY;
	
	public MainMenuScreen(BesiegeMain main) {
		this.main = main;
		
		state = ScreenState.Title;
		
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		stage.addListener(new InputListener());
		
		topTable = new Table(); 
//		topTable.debug();
		topTable.defaults().expandX().center();
		topTable.setX(0);
		topTable.setY(0);
		topTable.setWidth(WIDTH);
		topTable.setHeight(BesiegeMain.HEIGHT);
		
		styleTitle = new LabelStyle();
		styleTitle.font = Assets.pixel128;
		styleButtons = new LabelStyle();
		styleButtons.font = Assets.pixel64;
//		bs = new ButtonStyle();
		
		labelTitle = new Label("BESIEGE", styleTitle);
		labelNew = new Label("new", styleButtons);
		labelLoad = new Label("load", styleButtons);
		
		topTable.add(labelTitle).colspan(2).padBottom(PAD);
		topTable.row();
		
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
		topTable.add(labelNew).right().padRight(SEPARATE);
		topTable.add(labelLoad).left().padLeft(SEPARATE);
		
		Label placeholder = new Label(" ", styleButtons);
		topTable.row();
		topTable.add(placeholder).colspan(2);
		
		stage.addActor(topTable);
		
		candleX = 0; // will be initialized in Resize
		candleY = 0; 
		
		Candle candle = new Candle(candleX, candleY, 2, 1);
		candles = new Array<Candle>();
		candles.add(candle);
		stage.addActor(candle);
		
		count = 0f;
		this.checkForFile();
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
	
	@Override
	public void render(float delta) {
		updateCandles();
		
		stage.act(delta);
		Gdx.gl.glClearColor(.1f,.1f,.1f,0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Table.drawDebug(stage);
		stage.draw();

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
//		if (Math.random() < .1) 
		wind_velocity += (Math.random() - .5f)/400;
		
		Candle.wind_x += wind_velocity/10;
//		System.out.println(Candle.wind_x);

		if (wind_velocity > 2) wind_velocity = 1;
		if (wind_velocity < -2) wind_velocity = -1;
		
		if (Candle.wind_x > .5) { Candle.wind_x = .5f;		}
		if (Candle.wind_x < -.5) { Candle.wind_x = -.5f; }
	
//		Candle.wind_x += 1*candles.first().current_flicker;

		// update text lighting
//		if (Math.random() < .15)
//			updateTextLighting();

		for (Candle candle : candles) {
			candle.generateParticles();
			candle.updateFlicker();
			candle.updatePositions();
		}
	}

	@Override
	public void resize(int width, int height) {
		BesiegeMain.HEIGHT = height;
		BesiegeMain.WIDTH = width;
		topTable.setWidth(width);
		topTable.setHeight(height);
				
		this.candleX = (int) (width/2) - 7;
		this.candleY = (int) (-height/2) - 120;
		this.candles.first().move(candleX, candleY);
		
//		topTable.getCells().get(1).padLeft(width-WIDTH).padRight(SEPARATE);
//		topTable.getCells().get(2).padRight(width-WIDTH).padLeft(SEPARATE);
		stage.setViewport(BesiegeMain.WIDTH, BesiegeMain.HEIGHT, false);
	}
	
	private void clickNew() {
		this.state = ScreenState.InputName;
		
		topTable.clear();
		topTable.add(labelTitle).colspan(2).padBottom(PAD);
		topTable.row();
		Label prompt = new Label("Who are you?", styleButtons);
		topTable.add(prompt).expandX().center().colspan(2);
		topTable.row();
		
		TextFieldStyle tfs = new TextFieldStyle();
		tfs.fontColor = Color.WHITE;
		tfs.font = Assets.pixel64;
		tf = new TextField("", tfs);
		
		// need a second textfield so that it's automatically selected?
		TextField tf2 = new TextField("", tfs);
		tf2.setVisible(false);
		
		topTable.add(tf).center().colspan(2).align(Align.center);
		tf.setMaxLength(MAX_NAME);
		topTable.row();
		topTable.add(tf2).colspan(2).height(0); // needed to autoselect
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
	
	private void clickLoad() {
		// check if null
		
		
		System.out.println("clicked load");
		main.loadMapScreen();
		main.setScreen(main.mapScreen);
	}
	
	private void enterName(String text) {
		System.out.println("User entered: " + text); 
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
