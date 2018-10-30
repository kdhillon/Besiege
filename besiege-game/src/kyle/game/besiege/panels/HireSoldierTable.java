package kyle.game.besiege.panels;

import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

public class HireSoldierTable extends SoldierTable {
    private static final String HIRE_EMPTY_TEXT = "No volunteers available!";
    private PanelHire panelHire;

    public HireSoldierTable(Party party, PanelHire panelHire) {
        super(party);
        hirePanel = true;
        selectable = true;
        this.panelHire = panelHire;

        if (party.subparties.size > 0 && party.subparties.get(0)
                .getConsolHealthy().size > 0 && party.subparties.get(0)
                .getConsolHealthy().get(0).size > 0)
            this.selectFirstSoldier();
    }

    @Override
    public void select(Soldier soldier) {
        super.select(soldier);
        panelHire.notifySelect(soldier);
        noTroopsC.setText(HIRE_EMPTY_TEXT);
    }

    private void selectFirstSoldier() {
        select(party.subparties.get(0).getConsolHealthy().get(0).get(0));
    }
}
