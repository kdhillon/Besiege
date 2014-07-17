/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Character;
import kyle.game.besiege.SidePanel;
import kyle.game.besiege.army.ArmyPlayer;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

public class PanelCharacter extends Panel {
	private final float PAD = 5;
	private final float NEG = -5;
	private SidePanel panel;
	private Character character;
	private ArmyPlayer player;
	private Table text;
	private Label name;
	private Label level;
	private Label exp;
	private Label nextLevel;
	private Label faction;
	private Label title;
	private Label fame;
	
	public PanelCharacter(SidePanel panel) {
		this.panel = panel;
		this.player = panel.getPlayer();
		this.addParentPanel(panel);
		
		this.character = player.getCharacter();
		
		// Create text
		text = new Table();
		//text.debug();
		text.defaults().padTop(NEG);
		
		LabelStyle ls30 = new LabelStyle();
		ls30.font = Assets.pixel30;
		LabelStyle ls24 = new LabelStyle();
		ls24.font = Assets.pixel24;
		
		Label charC = new Label("Character", ls30);
		charC.setAlignment(0,0);
		Label nameC = new Label("Name:", ls24);
		Label levelC = new Label("Level:",ls24);
		Label expC = new Label("EXP:",ls24);
		Label nextLevelC = new Label("Next:", ls24);
		Label factionC = new Label("Faction:", ls24);
		Label titleC = new Label("Title:", ls24); 
		Label fameC = new Label("Fame:", ls24);

		name = new Label("", ls24);
		level = new Label("", ls24);
		exp = new Label("", ls24);
		nextLevel = new Label("" ,ls24);
		faction = new Label("", ls24);
		title = new Label("", ls24);
		fame = new Label("", ls24);
		
		text.add(charC).colspan(2).padBottom(PAD).width(SidePanel.WIDTH-PAD*2).fillX().expandX();
		text.row();
		text.add(nameC).left();
		text.add(name).right();
		text.row();
		text.add(levelC).left();
		text.add(level).right();
		text.row();
		text.add(expC).left();
		text.add(exp).right();
		text.row();
		text.add(nextLevelC).left();
		text.add(nextLevel).right();
		text.row();
		text.add(factionC).left();
		text.add(faction).right();
		text.row();
		text.add(titleC).left();
		text.add(title).right();
		text.row();
		text.add(fameC).left();
		text.add(fame).right();

		this.addTopTable(text);
		
		this.setButton(1, "Attributes");
		this.setButton(2, "Holdings");
		this.setButton(4, "Back");
	}
	
	@Override
	public void act(float delta) {
		level.setText(""+player.getLevel());
		name.setText(player.getName());
		exp.setText("" + character.exp);
		nextLevel.setText(""+ character.nextLevel);
		faction.setText(player.getFactionName());
		title.setText(character.title);
		fame.setText(character.fame+"");
		super.act(delta);
	}
	
	@Override
	public void button1() {
		panel.setActive(panel.attributes);
	}
	@Override
	public void button2() {
		BottomPanel.log("Holdings");
	}
	@Override
	public void button3() {
		
	}
	@Override
	public void button4() {
		panel.setDefault();
	}
}
