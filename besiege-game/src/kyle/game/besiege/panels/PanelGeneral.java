/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.party.General;
import kyle.game.besiege.party.Soldier;

public class PanelGeneral extends PanelUnit { 	
	private Label fameS;
	private Label courageS;
	private Label loyaltyS;
	private Label prepS;
	private Label infAtkS;
	private Label infDefS;
	private Label rngCmdS;
	
	// figure out a way to make this visible outside of battle view...
	// show the general but no temp effects like HP?
	public PanelGeneral(SidePanel panel, Unit unit, Soldier s) {
		super(panel, unit, s);
		
		General general = (General) soldier;
		title.setText(general.getRank());
		
		Label fameSC = new Label("Fame:",ls);
		Label courageSC = new Label("Courage:", ls);
		Label loyaltySC = new Label("Loyalty:", ls);
		Label prepSC = new Label("Prep:", ls);
		Label infAtkSC = new Label("Inf Atk:", ls);
		Label infDefSC = new Label("Inf Def:", ls);		
		Label rngCmdSC = new Label("Rng Cmd:", ls);		
		
		fameS = new Label("" + general.getFame(), ls);
		loyaltyS = new Label("" + general.loyalty, ls);
		loyaltyS.setColor(General.getColor(general.loyalty));
		courageS = new Label("" + general.courage, ls);
		courageS.setColor(General.getColor(general.courage));
		prepS = new Label("" + general.preparation, ls);
		prepS.setColor(General.getColor(general.preparation));
		infAtkS = new Label("" + general.infantryAttack, ls);
		infAtkS.setColor(General.getColor(general.infantryAttack));
		infDefS = new Label("" + general.infantryDefense, ls);
		infDefS.setColor(General.getColor(general.infantryDefense));
		rngCmdS = new Label("" + general.rangedCommand, ls);
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
		generalStats.add(infAtkSC).padLeft(PAD);
		generalStats.add(infAtkS);
		generalStats.add(infDefSC).padLeft(MINI_PAD);
		generalStats.add(infDefS);
		generalStats.row();
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
	}
}
