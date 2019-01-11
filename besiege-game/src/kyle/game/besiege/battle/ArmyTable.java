package kyle.game.besiege.battle;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import kyle.game.besiege.Random;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.UnitLoader;
import kyle.game.besiege.title.SelectOption;

import static kyle.game.besiege.battle.QuickBattleTable.SM_PAD;
import static kyle.game.besiege.title.MainMenuScreen.styleButtons;

public class ArmyTable extends Table {
    Label title;
    QuickBattleTable.TextWithDropdown<SelectOption<CultureType>> cultureType;
    QuickBattleTable.TextWithDropdown<SelectOption<PartyType.Type>> partyType;

    public ArmyTable(String titleString) {
        this.title = new Label(titleString, styleButtons);
        this.add(title);
        this.defaults().padTop(SM_PAD);

        this.row();

        initializeCultureOptions();

        initializePartyTypeOptions();
    }

    private void initializeCultureOptions() {
        StrictArray<SelectOption<CultureType>> cultureOptions = new StrictArray<>();

        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.values().toArray(), "Random"));
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.get("Tundra"), "Tundra"));
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.get("Desert"), "Desert"));
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.get("Jungle"), "Jungle"));
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.get("Plains"), "Plains"));
        cultureOptions.add(new SelectOption(UnitLoader.cultureTypes.get("Forest"), "Forest"));

        // Add stuff to the table
        cultureType = new QuickBattleTable.TextWithDropdown("Culture", cultureOptions.toArray());

        this.add(cultureType).expandX().fillX();
        this.row();
    }

    private void initializePartyTypeOptions() {
        StrictArray<SelectOption<PartyType.Type>> partyTypeOptions = new StrictArray<>();

        StrictArray<PartyType.Type> toAddLater = new StrictArray<>();

        // Prepare the list of valid options
        for (PartyType.Type type : PartyType.Type.values()) {
            // Don't add hire parties
            if (type.toString().contains("HIRE")) continue;

            toAddLater.add(type);
        }

        // Actually add stuff to list
        partyTypeOptions.add(new SelectOption<>(toAddLater.toArray(), "Random"));

        for (PartyType.Type type : toAddLater) {
            if (type == null) continue;
            partyTypeOptions.add(new SelectOption<>(type, formatForList(type.toString())));
        }

        // Add stuff to the table
        partyType = new QuickBattleTable.TextWithDropdown<>("Party Type", partyTypeOptions.toArray());

        this.add(partyType).expandX().fillX();
        this.row();
    }

    // TODO make this more specific, so it can be a straight up party?
    BattleOptions.PartyOptions getPartyOptions() {
        BattleOptions.PartyOptions options = new BattleOptions.PartyOptions();

        options.partyType = PartyType.generatePartyType(partyType.getSelected().getObject(), cultureType.getSelected().getObject());
        return options;
    }

    public static String formatForList(String input) {
        input = input.replace("_", " ");
        char[] chars = input.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);
    }
}
