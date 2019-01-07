package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import kyle.game.besiege.Assets;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.location.Location;

public class QuickBattleTable extends Table {
    public static Label.LabelStyle ls = new Label.LabelStyle( Assets.pixel16, Color.WHITE);

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


    public class TextWithDropdown<T> extends Table {
        private Label label;
        private SelectBox options;

        public TextWithDropdown(String label, T[] items) {
            this.label = new Label(label + ":", ls);
            this.options = new SelectBox(items, new SelectBox.SelectBoxStyle(Assets.pixel16, Color.WHITE, Assets.ninepatchBackground, new ScrollPane.ScrollPaneStyle(), new List.ListStyle()));
            this.add(label);
            this.add(options);
        }
    }

    public QuickBattleTable() {
        // Add stuff to the table
        TextWithDropdown mapType = new TextWithDropdown("Map", BattleMap.MapType.values());
        this.add(mapType);
    }

    public BattleOptions getCurrentOptions() {
        // Create options for all current settings
        BattleOptions battleOptions = new BattleOptions();

        // TODO these should be set by user in dropdowns
        battleOptions.alliesDefending = true;
        battleOptions.mapType = BattleMap.getRandomMapType();
        battleOptions.mapType = BattleMap.MapType.SNOW;
        battleOptions.siegeType = Location.LocationType.VILLAGE;

        return battleOptions;
    }
}
