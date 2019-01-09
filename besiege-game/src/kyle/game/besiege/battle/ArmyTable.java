package kyle.game.besiege.battle;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import kyle.game.besiege.Random;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.UnitLoader;

import static kyle.game.besiege.title.MainMenuScreen.styleButtons;

public class ArmyTable extends Table {
    Label title;
    QuickBattleTable.TextWithDropdown<SelectOption> cultureType;
    QuickBattleTable.TextWithDropdown<SelectOption> partyType;

    private StrictArray<SelectOption> cultureOptions = new StrictArray<>();
    private StrictArray<SelectOption> partyTypeOptions = new StrictArray<>();

    public class SelectOption {
        Object[] objectsToGetRandomFrom;
        Object object;
        String string;

        public SelectOption(Object object, String string) {
            if (object == null) throw new AssertionError(string + " is null");
            this.object = object;
            this.string = string;
        }

        public SelectOption(Object[] objects, String string) {
            this.objectsToGetRandomFrom = objects;
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }

        public Object getObject() {
            if (object != null) return object;
            return Random.getRandomValue(objectsToGetRandomFrom);
        }
    }

    public ArmyTable(String titleString) {
        this.title = new Label(titleString, styleButtons);
        this.add(title);
        this.row();

        initializeCultureOptions();

        // Add stuff to the table
        cultureType = new QuickBattleTable.TextWithDropdown("Culture", cultureOptions.toArray());

        this.add(cultureType).expandX().fillX();
        this.row();

        initializePartyTypeOptions();

        // Add stuff to the table
        partyType = new QuickBattleTable.TextWithDropdown("PartyType", partyTypeOptions.toArray());

        this.add(partyType).expandX().fillX();
        this.row();
    }

    private void initializeCultureOptions() {
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.values().toArray(), "Random"));
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.get("Tundra"), "Tundra"));
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.get("Desert"), "Desert"));
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.get("Jungle"), "Jungle"));
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.get("Plains"), "Plains"));
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.get("Forest"), "Forest"));
    }

    private void initializePartyTypeOptions() {
        for (PartyType.Type type : PartyType.Type.values()) {
            partyTypeOptions.add(new SelectOption(type, type.toString()));
        }
        // TODO add some other options.
        //
    }

    // TODO make this more specific, so it can be a straight up party?
    public BattleOptions.PartyOptions getPartyOptions() {
        BattleOptions.PartyOptions options = new BattleOptions.PartyOptions();

        options.partyType = PartyType.generatePartyType((PartyType.Type)(partyType.getSelected().getObject()), (CultureType) cultureType.getSelected().getObject());
        return options;
    }
}
