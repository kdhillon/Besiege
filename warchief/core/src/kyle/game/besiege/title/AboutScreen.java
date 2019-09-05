package kyle.game.besiege.title;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import kyle.game.besiege.WarchiefGame;
import kyle.game.besiege.battle.QuickBattleTable;
import kyle.game.besiege.party.ClickListenerWithHover;

public class AboutScreen implements Screen {

    private static String aboutText = "Warchief is a strategy RPG based on the diverse, complex and technologically " +
            "" + "advanced native peoples who lived in North America before European arrival. First and foremost, Warchief " +
            "seeks to respect the memory of the millions of indigenous people who died and suffered in one of the" +
            " greatest " + "tragedies in the history of humankind. Before 1492, North America was home to thousands " +
            "of societies and" + " some of the largest cities and empires in the world. But in the following " +
            "centuries, waves of " + "horrifying European diseases ravaged the hemisphere, destroying families, " +
            "villages, and entire nations. " + "At the same time, groups of Europeans brutalized, enslaved, and " +
            "committed acts of genocide on the " + "indigenous people who survived. We will never know the full scale" +
            " of human suffering that was caused, " + "because countless societies, cultures, languages, and " +
            "histories were lost forever. \n" + "\n" + "Warchief is based on the vibrant and dynamic continent " +
            "before this tragedy. It blends historical " + "sources with myths, legends and belief systems. Now, I’m " +
            "neither a historian nor an expert in mythology." + " With Warchief, I wanted to try to reconstruct a " +
            "rough picture of what it would be like to lead a " + "warband, army or empire in this often overlooked " +
            "period of history.\n" + "\n" + "Unfortunately, there is" + " a frustrating lack of primary sources " +
            "describing exactly what life and war were like before the " + "introduction of European diseases, guns, " +
            "horses, and metallurgy to the continent. Historians combine archaeological evidence with first-hand " +
            "accounts by early Europeans to paint a rough picture. I used several such sources when researching for " +
            "this game. Where there were holes," + " I filled them with my imagination. Therefore, like all " +
            "historical works, this game will include biases " + "and inaccuracies. I apologize in advance for these." +
            " That said, I hope Warchief serves its purpose of " + "transporting the player to one of the most " +
            "beautiful, complex and fascinating places in world history. \n\n - Kyle Dhillon";

    private WarchiefGame main;
    private Stage stage;
    private MainMenuScreen mainMenuScreen;
    private MenuBackground menuBackground;

    private Table mainTable;

    private ScrollPane scrollPane;

    private Label back;

    public AboutScreen(WarchiefGame main, MainMenuScreen mainMenuScreen, MenuBackground menuBackground) {
        this.main = main;
        this.mainMenuScreen = mainMenuScreen;
        this.menuBackground = menuBackground;
        stage = new Stage();
        stage.addActor(menuBackground);

        mainTable = new Table();
        stage.addActor(mainTable);
        mainTable.setFillParent(true);

        Label paragraphs = new Label(aboutText, QuickBattleTable.ls);
        paragraphs.setWrap(true);
        scrollPane = new ScrollPane(paragraphs);

        mainTable.row();
        mainTable.add(scrollPane).padRight(60).width(WarchiefGame.WIDTH * 0.8f);
        mainTable.row();

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
