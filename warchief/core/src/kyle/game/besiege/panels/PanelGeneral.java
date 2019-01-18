/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.party.General;
import kyle.game.besiege.party.Soldier;

public class PanelGeneral extends PanelUnit { 	
	private Label fameS;
	private Label courageS;
	private Label loyaltyS;
	private Label prepS;
	private Label infCmdS;
	private Label rngCmdS;
	
	// figure out a way to make this visible outside of battle view...
	// show the general but no temp effects like HP?
	public PanelGeneral(SidePanel panel, Unit unit, Soldier s) {
		super(panel, unit, s);
		
		General general = (General) soldier;

		// TODO set general rank using new table format
//		title.setText(general.getRank());
		topTable.updateTitle(general.getRank(), null);
	}

	public static Table getGeneralStats(General general, Label.LabelStyle ls) {
		Table generalStats = new Table();

		Label fameSC = new Label("Fame:",ls);
		Label courageSC = new Label("Courage:", ls);
		Label loyaltySC = new Label("Loyalty:", ls);
		Label prepSC = new Label("Prep:", ls);
		Label infCmdSC = new Label("Inf Cmd:", ls);
		Label rngCmdSC = new Label("Rng Cmd:", ls);

		Label fameS = new Label("" + general.getFame(), ls);
		Label loyaltyS = new Label("" + general.loyalty, ls);
		loyaltyS.setColor(General.getColor(general.loyalty));
		Label courageS = new Label("" + general.courage, ls);
		courageS.setColor(General.getColor(general.courage));
		Label prepS = new Label("" + general.preparation, ls);
		prepS.setColor(General.getColor(general.preparation));
		Label infCmdS = new Label("" + general.infantryCommand, ls);
		infCmdS.setColor(General.getColor(general.infantryCommand));
		Label rngCmdS = new Label("" + general.rangedCommand, ls);
		rngCmdS.setColor(General.getColor(general.rangedCommand));

		generalStats.defaults().padTop(NEG).left();
		generalStats.add(fameS).colspan(4).expandX();
		generalStats.row();
		generalStats.add(fameSC).padLeft(PAD);
		generalStats.add(fameS);
		generalStats.add(loyaltySC).padLeft(MINI_PAD);
		generalStats.add(loyaltyS);
		generalStats.row();
		generalStats.add(courageSC).padLeft(PAD);
		generalStats.add(courageS);
		generalStats.add(prepSC).padLeft(MINI_PAD);
		generalStats.add(prepS);
		generalStats.row();
		generalStats.add(infCmdSC).padLeft(PAD);
		generalStats.add(infCmdS);
		generalStats.add(rngCmdSC).padLeft(MINI_PAD);
		generalStats.add(rngCmdS);
		generalStats.row();

		return generalStats;
	}

	@Override
	public void act(float delta) {
		super.act(delta);
	}
}
