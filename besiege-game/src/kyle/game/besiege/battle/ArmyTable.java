package kyle.game.besiege.battle;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.UnitLoader;

import static kyle.game.besiege.title.MainMenuScreen.styleButtons;

public class ArmyTable extends Table {
    Label title;
    QuickBattleTable.TextWithDropdown<CultureType> cultureType;
    QuickBattleTable.TextWithDropdown<PartyType.Type> partyType;

    private static StrictArray<SelectOption> cultureOptions = new StrictArray<>();
    private static StrictArray<SelectOption> typeOptions = new StrictArray<>();

    public class SelectOption {
        Object object;
        String string;

        public SelectOption(BattleMap.Object object, String string) {
            this.object = object;
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    public ArmyTable(String titleString) {
        initializeCultureOptions();

        this.title = new Label(titleString, styleButtons);
        this.add(title);
    }

    private void initializeCultureOptions() {

    }

    // Army options:
    //      Culture (Tundra, Desert, Plains, Forest, Jungle)
    //      PartyType (Noble, Scout, Bandits, SingleUnit)
    //          TODO if custom:
    //          allow to select single unit from list.

    public void setCultureType(String typeString) {
        this.cultureType = UnitLoader.cultureTypes.get(typeString);
        if (cultureType == null) throw new AssertionError();
    }

    // TODO make this more specific, so it can be a straight up party?
    public BattleOptions.PartyOptions getPartyOptions() {
        BattleOptions.PartyOptions options = new BattleOptions.PartyOptions();

        options.partyType = PartyType.generatePartyType(partyType.getSelected(), cultureType.getSelected());
        return options;
    }
}
