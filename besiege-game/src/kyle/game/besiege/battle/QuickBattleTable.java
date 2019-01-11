package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import kyle.game.besiege.Assets;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.title.MainMenuScreen;
import kyle.game.besiege.title.SelectOption;

import static kyle.game.besiege.battle.ArmyTable.formatForList;

public class QuickBattleTable extends Table {
    final static int SM_PAD = 3;

    final static int DISTANCE_BETWEEN_BOXES = 200;

    private final static BitmapFont fontForEverything = Assets.pixel18;

    public static Label.LabelStyle ls = new Label.LabelStyle(fontForEverything, Color.WHITE);

    // Options:
    //      Map:
    //          Biome (Random, ...)
    //          SiegeSetting (None, City, Fort, Village, Ruin, Random)
    //          Weather (None, Rain, Snow)
    //          Time of day (Day, Night)
    //
    //      Armies:
    //          Allies:
    //              (see army table)
    //          Enemies:
    //              (see army table)

    private Table mapOptions;
    private TextWithDropdown<SelectOption<BattleMap.MapType>> mapType;
    private TextWithDropdown<SelectOption<Location.LocationType>> locationType;
    private TextWithDropdown<SelectOption<BattleOptions.WeatherEffect>> weatherEffect;
    private TextWithDropdown<SelectOption<BattleOptions.TimeOfDay>> timeOfDay;

    private Table armyTables;

    private ArmyTable alliesTable;
    private ArmyTable enemiesTable;

    public static class TextWithDropdown<T> extends Table {
        private Label label;
        private SelectBox options;
        private ScrollPane.ScrollPaneStyle spStyle;
        private T[] items;

        public TextWithDropdown(String labelText, T[] items) {
            this.items = items;

            spStyle = new ScrollPane.ScrollPaneStyle();

            spStyle.background = Assets.ninepatchBackgroundGray;

            List.ListStyle listStyle = new List.ListStyle();
            listStyle.selection = Assets.ninepatchBackgroundLightGray;
            listStyle.font = fontForEverything;

            this.label = new Label(labelText + ":  ", ls);
            for (T t : items) {
                System.out.println(t.toString());
            }

            this.options = new SelectBox(items, new SelectBox.SelectBoxStyle(fontForEverything, Color.WHITE, Assets.ninepatchBackgroundDarkGray, spStyle, listStyle));
            this.add(this.label).left().expandX();
            this.add(options).right();
        }

        public T getSelected() {
            return items[options.getSelectionIndex()];
        }
    }

    public QuickBattleTable() {
        addMapOptions();
        addArmyTables();
    }

    private void addMapOptions() {
        mapOptions = new Table();
//        mapOptions.debug();
        mapOptions.setWidth(BesiegeMain.WIDTH/2);

        mapOptions.defaults().padTop(SM_PAD);

        Label mapTitle = new Label("Map Options", MainMenuScreen.styleButtons);
        mapOptions.add(mapTitle).center().expandX();
        mapOptions.row();

        initializeMapTypes();

        initializeWeatherTypes();

        initializeTimeOfDay();

        initializeLocationTypes();

        this.add(mapOptions).left().expandX().padRight(DISTANCE_BETWEEN_BOXES);
    }

    private void initializeWeatherTypes() {
        StrictArray<SelectOption<BattleOptions.WeatherEffect>> options = new StrictArray();

        // TODO add "Auto" effect, that automatically selects a weather effect accordingly.
        for (BattleOptions.WeatherEffect weatherEffect : BattleOptions.WeatherEffect.values()) {
            String name = formatForList(weatherEffect.toString());
            if (weatherEffect == BattleOptions.WeatherEffect.NONE) name = "Clear";
            options.add(new SelectOption<>(weatherEffect, name));
        }

        weatherEffect = new TextWithDropdown<>("Weather", options.toArray());
        mapOptions.add(weatherEffect).fillX();
        mapOptions.row();
    }

    private void initializeTimeOfDay() {
        StrictArray<SelectOption<BattleOptions.TimeOfDay>> options = new StrictArray();

        for (BattleOptions.TimeOfDay timeOfDay : BattleOptions.TimeOfDay.values()) {
            options.add(new SelectOption<>(timeOfDay, formatForList(timeOfDay.toString())));
        }

        timeOfDay = new TextWithDropdown<>("Time of Day", options.toArray());
        mapOptions.add(timeOfDay).fillX();
        mapOptions.row();
    }

    private void initializeLocationTypes() {
        StrictArray<SelectOption<Location.LocationType>> options = new StrictArray();

        options.add(new SelectOption<Location.LocationType>("No Siege"));
        options.add(new SelectOption<>(Location.LocationType.values(), "Random"));

        for (Location.LocationType type : Location.LocationType.values()) {
            options.add(new SelectOption<>(type, formatForList(type.toString())));
        }

        locationType = new TextWithDropdown<>("Siege Type", options.toArray());
        mapOptions.add(locationType).expandX().fillX();
        mapOptions.row();
    }

    private void initializeMapTypes() {
        StrictArray<SelectOption> options = new StrictArray<>();

        // TODO add some other options.
        options.add(new SelectOption<>( BattleMap.MapType.values(), "Random"));

        for (BattleMap.MapType type : BattleMap.MapType.values()) {
            options.add(new SelectOption<>(type, formatForList(type.toString())));
        }

        // Add stuff to the table
        mapType = new TextWithDropdown("Map", options.toArray());
        mapOptions.add(mapType).expandX().fillX();
        mapOptions.row();
    }


    private void addArmyTables() {
        armyTables = new Table();
        alliesTable = new ArmyTable("Allied Party");
        armyTables.add(alliesTable).padBottom(30);
        armyTables.row();

        enemiesTable = new ArmyTable("Enemy Party");
        armyTables.add(enemiesTable);

        this.add(armyTables).right();
    }

    public BattleOptions getCurrentOptions() {
        // Create options for all current settings
        BattleOptions battleOptions = new BattleOptions();

        // TODO add checkbox for this
        battleOptions.alliesDefending = true;

        battleOptions.mapType = mapType.getSelected().getObject();
        battleOptions.siegeType = locationType.getSelected().getObject();
        battleOptions.weatherEffect = weatherEffect.getSelected().getObject();

        battleOptions.allyOptions = alliesTable.getPartyOptions();
        battleOptions.enemyOptions = enemiesTable.getPartyOptions();

        return battleOptions;
    }
}
