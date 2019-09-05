package kyle.game.besiege.title;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import kyle.game.besiege.WarchiefGame;
import kyle.game.besiege.battle.QuickBattleTable;
import kyle.game.besiege.battle.Simulation;
import kyle.game.besiege.party.ClickListenerWithHover;

public class QuickBattleScreen implements Screen {

    private WarchiefGame main;
    private Stage stage;
    private MainMenuScreen mainMenuScreen;
    private MenuBackground menuBackground;

    private Table mainTable;

    private QuickBattleTable quickBattleTable;

    private Label start;
    private Label back;

    public QuickBattleScreen(WarchiefGame main, MainMenuScreen mainMenuScreen, MenuBackground menuBackground) {
        this.main = main;
        this.mainMenuScreen = mainMenuScreen;
        this.menuBackground = menuBackground;
        stage = new Stage();
        stage.addActor(menuBackground);

        mainTable = new Table();
        stage.addActor(mainTable);
        mainTable.setFillParent(true);

        quickBattleTable = new QuickBattleTable();

        mainTable.add(quickBattleTable).padBottom(60).colspan(2);
        mainTable.row();

        start = new Label("S T A R T", MainMenuScreen.styleButtons);
        start.addListener(new ClickListenerWithHover(start) {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                startBattle();
            }
        });
        mainTable.add(start).padRight(60);

        back = new Label("B A C K", MainMenuScreen.styleButtons);
        back.addListener(new ClickListenerWithHover(back) {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                returnToMain();
            }
        });

        mainTable.add(back);
        mainTable.row();

    }

    public void startBattle() {
        System.out.println("starting bqttle");
        // Launch a new battle, with specified options.
        Simulation simulation = new Simulation(quickBattleTable.getCurrentOptions(), mainMenuScreen);
        main.setScreen(simulation.getMapScreen()); // eventually make mainMenu
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        Gdx.gl.glClearColor(.1f,.1f,.1f,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
//        Table.drawDebug(stage);
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

//		topTable.getCells().get(1).padLeft(width-WIDTH).padRight(SEPARATE);
//		topTable.getCells().get(2).padRight(width-WIDTH).padLeft(SEPARATE);
//        stage.setViewport(WarchiefGame.WIDTH, WarchiefGame.HEIGHT, false);
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

    public void returnToThis() {
        main.setScreen(this);
        mainMenuScreen.returnFromOtherPanel(true);
    }

    public void returnToMain() {
        main.setScreen(mainMenuScreen);
        mainMenuScreen.returnFromOtherPanel(false);
    }
}
