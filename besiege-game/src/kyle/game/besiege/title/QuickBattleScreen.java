package kyle.game.besiege.title;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import kyle.game.besiege.Assets;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.battle.QuickBattleTable;
import kyle.game.besiege.battle.Simulation;

public class QuickBattleScreen implements Screen {

    private BesiegeMain main;
    private Stage stage;
    private MainMenuScreen mainMenuScreen;
    private MenuBackground menuBackground;

    private Table mainTable;

    private QuickBattleTable quickBattleTable;

    private Label start;
    private Label back;

    public QuickBattleScreen(BesiegeMain main, MainMenuScreen mainMenuScreen, MenuBackground menuBackground) {
        this.main = main;
        this.mainMenuScreen = mainMenuScreen;
        this.menuBackground = menuBackground;
        stage = new Stage();
        stage.addActor(menuBackground);

        mainTable = new Table();
        stage.addActor(mainTable);
        mainTable.setFillParent(true);

        // TODO add options table with fixed height.
        quickBattleTable = new QuickBattleTable();

        mainTable.add(quickBattleTable).padBottom(40).colspan(2);
        mainTable.row();

        mainTable.debug();

        // TODO add back button, standardize
//        backButton = new Button();

        start = new Label("S T A R T", MainMenuScreen.styleButtons);
        start.addListener(new ClickListener() {
            //			public boolean touchDown(InputEvent event, float x,
//					float y, int pointer, int button) {
//				return true;
//			}
            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                startBattle();
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                setStartOn();
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                setStartOff();
            }
        });
        mainTable.add(start).padRight(40);

        back = new Label("B A C K", MainMenuScreen.styleButtons);
        back.addListener(new ClickListener() {
            //			public boolean touchDown(InputEvent event, float x,
//					float y, int pointer, int button) {
//				return true;
//			}
            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                returnToMain();
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                setBackOn();
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                setBackOff();
            }
        });

        mainTable.add(back);
        mainTable.row();

    }

    private void setStartOn() {
        start.setColor(Color.LIGHT_GRAY);
    }

    private void setStartOff() {
        start.setColor(Color.WHITE);
    }

    private void setBackOn() {
        back.setColor(Color.LIGHT_GRAY);
    }

    private void setBackOff() {
        back.setColor(Color.WHITE);
    }

    public void startBattle() {
        // Launch a new battle, with specified options.
        Simulation simulation = new Simulation(quickBattleTable.getCurrentOptions());
        main.setScreen(simulation.getMapScreen()); // eventually make mainMenu
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        Gdx.gl.glClearColor(.1f,.1f,.1f,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
        Table.drawDebug(stage);
    }

    @Override
    public void resize(int width, int height) {
        menuBackground.resize(width, height);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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

    }

    public void returnToMain() {
        main.setScreen(mainMenuScreen);
        mainMenuScreen.returnFromOtherPanel();
    }
}
