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

import kyle.game.besiege.Assets;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.MapScreen;

// TODO separate MainMenuScreen from background.
public class MainMenuScreen implements Screen {
//	private static final String TITLE = "CHIEFTAIN";
	private static final String TITLE_1 = "C H I E F T A I N";
	private static final String TITLE = "W A R C H I E F";

	private final int PAD_BELOW_TITLE = 100;
	private final int PAD_ABOVE_TITLE = -20;
	private final int WIDTH = 500;
	private final int SEPARATE = 120;
	private final int MAX_NAME = 15;
	private final int FIXED_SIZE_BOTTOM = 100;

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
	public static LabelStyle styleButtons = new LabelStyle(Assets.pixel40, Color.WHITE);

	TextField tf;
	
	private float count;

	private MenuBackground menuBackground;

//	private ButtonStyle bs;

	public MainMenuScreen(BesiegeMain main) {
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
		mainTable.setHeight(BesiegeMain.HEIGHT);
		
		lowerTable.defaults().expandX().center();
		lowerTable.setX(0);
		lowerTable.setY(0);
		lowerTable.setWidth(WIDTH);
		lowerTable.setHeight(BesiegeMain.HEIGHT / 2);
		
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

		mainTable.add(labelTitle).padBottom(PAD_BELOW_TITLE).padTop(PAD_ABOVE_TITLE);
		mainTable.row();
		mainTable.add(lowerTable).height(FIXED_SIZE_BOTTOM);
//		mainTable.debug();

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

//		lowerTable.debug();

		lowerTable.add(labelNew).right().padRight(SEPARATE);
		lowerTable.add(labelLoad).left().padLeft(SEPARATE);
		lowerTable.row();
		lowerTable.add(quickBattle).left().colspan(2).expandX().padTop(25);

		stage.addActor(mainTable);
		
		count = 0f;
		this.checkForFile();
	}

	private void setNewOn() {
		if (labelNew.getActions().size == 0) {
			labelNew.setColor(Color.LIGHT_GRAY);
		}
	}

	public void returnFromOtherPanel() {
		stage.clear();
		stage.addActor(menuBackground);
		stage.addActor(mainTable);
		menuBackground.drawWithTint(false);
	}

	private void setNewOff() {
		if (labelNew.getActions().size == 0) {
			labelNew.setColor(Color.WHITE);
		}
	}

	private void setLoadOn() {
		if (labelLoad.getActions().size == 0) {
			labelLoad.setColor(Color.LIGHT_GRAY);
		}
	}
	
	private void setLoadOff() {
		if (labelLoad.getActions().size == 0) {
			labelLoad.setColor(Color.WHITE);
		}
	}

	private void setQuickBattleOn() {
		if (quickBattle.getActions().size == 0) {
			quickBattle.setColor(Color.LIGHT_GRAY);
		}
	}

	private void setQuickBattleOff() {
		if (quickBattle.getActions().size == 0) {
			quickBattle.setColor(Color.WHITE);
		}
	}
	
	@Override
	public void render(float delta) {
		stage.act(delta);
		Gdx.gl.glClearColor(.1f,.1f,.1f,0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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

		Table.drawDebug(stage);
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
		BesiegeMain.HEIGHT = height;
		BesiegeMain.WIDTH = width;
		mainTable.setWidth(width);
		mainTable.setHeight(height);
//		lowerTable.setWidth(width);
//		lowerTable.setHeight(height/2);

		menuBackground.resize(width, height);
		
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
		QuickBattleScreen qbs = new QuickBattleScreen(main, this, menuBackground);
		main.setScreen(qbs);
		menuBackground.drawWithTint(true);
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
