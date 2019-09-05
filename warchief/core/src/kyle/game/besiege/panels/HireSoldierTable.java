package kyle.game.besiege.panels;

import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

public class HireSoldierTable extends SoldierTable {
    private static final String HIRE_EMPTY_TEXT = "No volunteers available!";
    private PanelHire panelHire;

    public HireSoldierTable(Party party, PanelHire panelHire) {
        super(panelHire, party);
        hirePanel = true;
        selectable = true;
        this.panelHire = panelHire;

        noTroopsC.setText(HIRE_EMPTY_TEXT);
        if (party.squads.size > 0 && party.squads.get(0)
                .getConsolHealthy().size > 0 && party.squads.get(0)
                .getConsolHealthy().get(0).size > 0)
            this.selectFirstSoldier();
    }

    @Override
    public void select(Soldier soldier) {
        super.select(soldier);
        panelHire.notifySelect(soldier);
    }

    private void selectFirstSoldier() {
        select(party.squads.get(0).getConsolHealthy().get(0).get(0));
    }
}
