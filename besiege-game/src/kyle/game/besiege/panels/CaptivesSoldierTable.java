package kyle.game.besiege.panels;

import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

public class CaptivesSoldierTable extends SoldierTable {
    private static final String HIRE_EMPTY_TEXT = "No captives remaining!";
    private PanelCaptives panelCaptives;

    public CaptivesSoldierTable(Party party, PanelCaptives panelCaptives) {
        super(party);
        captivesPanel = true;
        selectable = true;
        this.panelCaptives = panelCaptives;

        if (party.getPrisoners().size > 0)
            this.selectFirstSoldier();
    }

    @Override
    public void select(Soldier soldier) {
        super.select(soldier);
        panelCaptives.notifySelect(soldier);
        noTroopsC.setText(HIRE_EMPTY_TEXT);
    }

    private void selectFirstSoldier() {
        select(party.getPrisoners().get(0));
    }
}
