package kyle.game.besiege.panels;

import kyle.game.besiege.StrictArray;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Subparty;

public class HireSoldierTable extends SoldierTable {
    private static final String HIRE_EMPTY_TEXT = "No volunteers available!";
    private PanelHire panelHire;

    public HireSoldierTable(Party party, PanelHire panelHire) {
        super(party);
        hirePanel = true;
        this.panelHire = panelHire;

        this.selectFirstSoldier();
    }

    @Override
    public void select(Soldier soldier) {
        super.select(soldier);
        panelHire.notifySelect(soldier);
        noTroopsC.setText(HIRE_EMPTY_TEXT);
    }

    private void selectFirstSoldier() {
        select(party.sub.get(0).getConsolHealthy().get(0).get(0));
    }
}
