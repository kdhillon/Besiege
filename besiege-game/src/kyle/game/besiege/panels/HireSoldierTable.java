package kyle.game.besiege.panels;

import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

public class HireSoldierTable extends SoldierTable {
    Soldier selected;

    public HireSoldierTable(Party party) {
        super(party);
        selectable = true;
    }
}
