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
import com.badlogic.gdx.utils.Align;
import kyle.game.besiege.Assets;
import kyle.game.besiege.SoundPlayer;
import kyle.game.besiege.WarchiefGame;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.party.ClickListenerWithHover;
import kyle.game.besiege.party.UnitLoader;

// TODO separate MainMenuScreen from background.
public class MainMenuScreen implements Screen {
//	private static final String TITLE = "CHIEFTAIN";
	private static final String TITLE_1 = "C H I E F T A I N";
	private static final String TITLE = "W A R C H I E F";

	private final int PAD_BELOW_TITLE = 100;
	private final int PAD_ABOVE_TITLE = -20;
	private final int WIDTH = 500;
	private final int SEPARATE = 260;
	private final int MAX_NAME = 15;
	private final int FIXED_SIZE_BOTTOM = 100;

	enum ScreenState{Title, InputName};
	
	ScreenState state;
	
	private WarchiefGame main;
	private Stage stage;
	private Table mainTable;
	private Table lowerTable;
//	private Button buttonNew;
//	private Button buttonLoad;
	private Label labelTitle;
	private Label labelNew;
	private Label labelLoad;
	private Label quickBattle;
	private Label about;

	private QuickBattleScreen qbs;
	
	private LabelStyle styleTitle;
	public static LabelStyle styleButtons = new LabelStyle(Assets.pixel40, Color.WHITE);

	TextField tf;
	
	private float count;

	private MenuBackground menuBackground;

//	private ButtonStyle bs;

	public MainMenuScreen(WarchiefGame main) {
		this.main = main;
		
		state = ScreenState.Title;
		
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		stage.addListener(new InputListener());

		menuBackground = new MenuBackground();
		stage.addActor(menuBackground);

		mainTable = new Table();
		lowerTable = new Table();
//		topTable.debug();
		mainTable.defaults().expandX().center();
		mainTable.setX(0);
		mainTable.setY(0);
		mainTable.setWidth(WIDTH);
		mainTable.setHeight(WarchiefGame.HEIGHT);
		
		lowerTable.defaults().expandX().center();
		lowerTable.setX(0);
		lowerTable.setY(0);
		lowerTable.setWidth(WIDTH);
		lowerTable.setHeight(WarchiefGame.HEIGHT / 2);
		
		styleTitle = new LabelStyle();
		styleTitle.font = Assets.pixel150;
//		bs = new ButtonStyle();

		float titleFade = 3.5f;
		float titleDelay = 1.5f;
		labelTitle = new Label(TITLE, styleTitle);
//		labelTitle.addAction(Actions.fadeIn(3));
		labelTitle.addAction(Actions.sequence(Actions.alpha(0), Actions.delay(titleDelay), Actions.fadeIn(titleFade)));

		float buttonFade = 2f;
		float buttonDelay = 3f;
		labelNew = new Label("C A M P A I G N", styleButtons);
		labelLoad = new Label("L O A D", styleButtons);
		labelNew.addAction(Actions.sequence(Actions.alpha(0), Actions.delay(buttonDelay), Actions.fadeIn(buttonFade)));
		labelLoad.addAction(Actions.sequence(Actions.alpha(0),  Actions.delay(buttonDelay), Actions.fadeIn(buttonFade)));

		quickBattle = new Label("Q U I C K   B A T T L E", styleButtons);
		quickBattle.addAction(Actions.sequence(Actions.alpha(0), Actions.delay(buttonDelay), Actions.fadeIn(buttonFade)));

		about = new Label("A B O U T", styleButtons);
		about.addAction(Actions.sequence(Actions.alpha(0), Actions.delay(buttonDelay), Actions.fadeIn(buttonFade)));

		mainTable.add(labelTitle).padBottom(PAD_BELOW_TITLE).padTop(PAD_ABOVE_TITLE);
		mainTable.row();
		mainTable.add(lowerTable).height(FIXED_SIZE_BOTTOM);
//		mainTable.debug();

		labelNew.addListener(new ClickListenerWithHover(labelNew) {
			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				clickNew();
			}
		});
		labelLoad.addListener(new ClickListenerWithHover(labelLoad) {
			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				clickLoad();
			}
		});
		quickBattle.addListener(new ClickListenerWithHover(quickBattle) {
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				clickQuickBattle();
			}
		});
		about.addListener(new ClickListenerWithHover(about) {
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				clickAbout();
			}
		});
//		lowerTable.debug();

		lowerTable.add(labelNew).right().padRight(SEPARATE);
		lowerTable.add(labelLoad).left().padLeft(SEPARATE);
		lowerTable.row();
		lowerTable.add(quickBattle).left().colspan(2).expandX().padTop(25);
		lowerTable.row();
		// TODO add about screen later
//		lowerTable.add(about).left().colspan(2).expandX().padTop(25);

		stage.addActor(mainTable);
		SoundPlayer.startMusic(UnitLoader.cultureTypes.get("Forest"));

		count = 0f;
		this.checkForFile();
	}

	private void setNewOn() {
		if (labelNew.getActions().size == 0) {
			labelNew.setColor(Color.LIGHT_GRAY);
		}
	}

	public void returnFromOtherPanel(boolean tint) {
		stage.clear();
		stage.addActor(menuBackground);
		stage.addActor(mainTable);
		menuBackground.drawWithTint(tint);
	}
	
	@Override
	public void render(float delta) {
		stage.act(delta);
		Gdx.gl.glClearColor(.1f,.1f,.1f,0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
		SoundPlayer.updateSounds(delta);

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

//		Table.drawDebug(stage);
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

	@Override
	public void resize(int width, int height) {
		WarchiefGame.HEIGHT = height;
		WarchiefGame.WIDTH = width;
		mainTable.setWidth(width);
		mainTable.setHeight(height);
//		lowerTable.setWidth(width);
//		lowerTable.setHeight(height/2);

		menuBackground.resize(width, height);
		// TODO fix resizing here by using a viewport
//		topTable.getCells().get(1).padLeft(width-WIDTH).padRight(SEPARATE);
//		topTable.getCells().get(2).padRight(width-WIDTH).padLeft(SEPARATE);
//		stage
		// TODO do we need to update this manually still?
//		stage.set(WarchiefGame.WIDTH, WarchiefGame.HEIGHT, false);
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

	public void clickAbout() {
		AboutScreen as = new AboutScreen(main, this, menuBackground);
		main.setScreen(as);
		menuBackground.drawWithTint(true);
	}

	public void clickQuickBattle() {
		if (qbs != null) {
			qbs.returnToThis();
		} else {
			qbs = new QuickBattleScreen(main, this, menuBackground);
			main.setScreen(qbs);
		}
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

	public void addActor(MenuBackground menuBackground) {
		stage.addActor(menuBackground);
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