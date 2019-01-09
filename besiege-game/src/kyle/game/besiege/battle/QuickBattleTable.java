package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import kyle.game.besiege.Assets;
import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.title.MainMenuScreen;

public class QuickBattleTable extends Table {
    private final static BitmapFont fontForEverything = Assets.pixel16;

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
    private TextWithDropdown<BattleMap.MapType> mapType;
    private TextWithDropdown<Location.LocationType> locationType;
    private TextWithDropdown<BattleOptions.WeatherEffect> weatherEffect;

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

        mapOptions.defaults().padTop(3);

        Label mapTitle = new Label("Map Options", MainMenuScreen.styleButtons);
        mapOptions.add(mapTitle).center().expandX();
        mapOptions.row();

        // Add stuff to the table
        mapType = new TextWithDropdown("Map", BattleMap.MapType.values());
        mapOptions.add(mapType).expandX().fillX();
        mapOptions.row();

        locationType = new TextWithDropdown("Siege Type", Location.LocationType.values());
        mapOptions.add(locationType).expandX().fillX();
        mapOptions.row();

        weatherEffect = new TextWithDropdown("Weather", BattleOptions.WeatherEffect.values());
        mapOptions.add(weatherEffect).fillX();
        mapOptions.row();

        this.add(mapOptions).left().expandX().padRight(100);
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

        battleOptions.mapType = mapType.getSelected();
        battleOptions.siegeType = locationType.getSelected();
        battleOptions.weatherEffect = weatherEffect.getSelected();

        // TODO this should be specified by the user in some dropdowns.
//        alliesTable.setCultureType("Plains");
//        enemiesTable.setCultureType("Forest");
//        alliesTable.setPartyCount(1);
//        enemiesTable.setPartyCount(1);
//        alliesTable.setPartyTypeType(PartyType.Type.NOBLE);
//        enemiesTable.setPartyTypeType(PartyType.Type.TEST_ALL);

//		pt1.forceUnitType(type1.units.get("Spearman (Vet)4"));
//		pt1.forceUnitType(type1.units.get("Archer3"));
//		pt2.forceUnitType(type1.units.get("Archer3"));
        battleOptions.allyOptions = alliesTable.getPartyOptions();
        battleOptions.enemyOptions = enemiesTable.getPartyOptions();

        return battleOptions;
    }
}
