/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

public class MainMenuScreen implements Screen {
	private final int PAD = 50;
	private final int PAD2 = 25;
	private final int WIDTH = 500;
	private final int SEPARATE = 50;
	private final int MAX_NAME = 15;
	
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
//	private ButtonStyle bs;
	
	public MainMenuScreen(BesiegeMain main) {
		this.main = main;
		
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
		
		labelNew.addListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				clickNew();
			}
		});
		labelLoad.addListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				clickLoad();
			}
		});
		topTable.add(labelNew).right().padRight(SEPARATE);
		topTable.add(labelLoad).left().padLeft(SEPARATE);
		
		Label placeholder = new Label(" ", styleButtons);
		topTable.row();
		topTable.add(placeholder).colspan(2);
		
		stage.addActor(topTable);
	}

	@Override
	public void render(float delta) {
		stage.act(delta);
		Gdx.gl.glClearColor(0,0,0,0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Table.drawDebug(stage);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		BesiegeMain.HEIGHT = height;
		BesiegeMain.WIDTH = width;
		topTable.setWidth(width);
		topTable.setHeight(height);
		
//		topTable.getCells().get(1).padLeft(width-WIDTH).padRight(SEPARATE);
//		topTable.getCells().get(2).padRight(width-WIDTH).padLeft(SEPARATE);
		stage.setViewport(BesiegeMain.WIDTH, BesiegeMain.HEIGHT, false);
	}
	
	private void clickNew() {
		topTable.clear();
		topTable.add(labelTitle).colspan(2).padBottom(PAD);
		topTable.row();
		Label prompt = new Label("Who are you?", styleButtons);
		topTable.add(prompt).expandX().center().colspan(2);
		topTable.row();
		
		TextFieldStyle tfs = new TextFieldStyle();
		tfs.fontColor = Color.WHITE;
		tfs.font = Assets.pixel64;
		TextField tf = new TextField("", tfs);
		TextField tf2 = new TextField("", tfs);
		tf2.setVisible(false);
		topTable.add(tf).expandX().center().colspan(2).width(300);
		tf.setMaxLength(MAX_NAME);
		topTable.row();
		topTable.add(tf2).colspan(2).height(0); // needed to autoselect
		tf.next(true);
		tf.setTextFieldFilter(new TextFieldFilter() {
			public boolean acceptChar(TextField textField, char key) {
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
	
	private void clickLoad() {
	}
	
	private void enterName(String text) {
		System.out.println("User entered: " + text); 
		main.setPlayerName(text);
		main.setScreen(main.mapScreen);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
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
