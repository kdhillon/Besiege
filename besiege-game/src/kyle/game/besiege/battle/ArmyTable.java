package kyle.game.besiege.battle;

import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.UnitLoader;

public class ArmyTable {
    // TODO change this to an actual table
    CultureType cultureType;
    PartyType.Type partyTypeType;
    BattleOptions.PartyOptions options = new BattleOptions.PartyOptions();

    // Army options:
    //      Culture (Tundra, Desert, Plains, Forest, Jungle)
    //      PartyType (Noble, Scout, Bandits, SingleUnit)
    //          TODO if custom:
    //          allow to select single unit from list.

    public void setCultureType(String typeString) {
        this.cultureType = UnitLoader.cultureTypes.get(typeString);
        if (cultureType == null) throw new AssertionError();
    }

    public void setPartyTypeType(PartyType.Type type) {
        this.partyTypeType = type;
    }

    public void setPartyCount(int partyCount) {
        options.partyCount = partyCount;
    }

    // TODO make this more specific, so it can be a straight up party?
    public BattleOptions.PartyOptions getPartyOptions() {
        options.partyType = PartyType.generatePartyType(partyTypeType, cultureType);
        return options;
    }
}
