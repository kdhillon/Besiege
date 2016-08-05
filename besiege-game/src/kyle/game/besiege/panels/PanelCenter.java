/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import kyle.game.besiege.Assets;
import kyle.game.besiege.RandomCrest;
import kyle.game.besiege.voronoi.Center;

public class PanelCenter extends Panel {
	private final float PAD = 5;
	private final float NEG = -5;
	private SidePanel panel;
	public Center center;
	private Table text;
	private Label regionC;
	private Label biome;
	private Label faction;
	private Label height;
	private Label wealth;
	private Label title;
	
	public PanelCenter(SidePanel panel, Center center) {
		this.panel = panel;
		this.center = center;
		this.addParentPanel(panel);
				
		// Create text
		text = new Table();
//		text.debug();
		text.defaults().padTop(NEG);
		
		LabelStyle ls30 = new LabelStyle();
		ls30.font = Assets.pixel24;
		LabelStyle ls24 = new LabelStyle();
		ls24.font = Assets.pixel18;
		
		Label factionC = new Label("Faction:", ls24);
//		Label biomeC = new Label("Biome:", ls24); 
		Label heightC = new Label("Elvtn:", ls24);
		Label wealthC = new Label("Resources:", ls24);

		regionC = new Label("Region", ls24);
		regionC.setAlignment(0,0);

//		biome = new Label(center.biome.toString() ,ls24);
		faction = new Label("", ls24);
		title = new Label(center.biome.toString(), ls30);
		title.setAlignment(0,0);
		height = new Label(""+center.getAvgElevation(), ls24);
		wealth = new Label(""+(int)(100*center.wealth), ls24);
		
		text.add(title).colspan(2).width(SidePanel.WIDTH-PAD*2).fillX().expandX().center();
		title.setWrap(true);
		text.row();
//		text.add(regionC).colspan(2).padBottom(PAD).width(SidePanel.WIDTH-PAD*2).fillX().expandX().center();
//		text.row();
//		text.add(biomeC).left();
		text.add(biome).left().fillX().expandX().padLeft(PAD);
		text.row();
		text.add(factionC).left();
		text.add(faction).left().fillX().expandX().padLeft(PAD);
		text.row();
		text.add(heightC).left();
		text.add(height).left().fillX().expandX().padLeft(PAD);
		text.row();
		text.add(wealthC).left();
		text.add(wealth).left().fillX().expandX().padLeft(PAD);
//		text.row();
//		text.add(fameC).left();
//		text.add(fame).right();
//		text.row();
//		text.add(maxTroopsC).left();
//		text.add(maxTroops).right();
		
		this.addTopTable(text);
		
//		this.setButton(1, "Attributes");
//		this.setButton(2, "Holdings");
		this.setButton(4, "Back");
	}
	
	@Override
	public void act(float delta) {
//		biome.setText(center.biome.toString());
		if (center.faction != null)
			faction.setText(center.faction.name);
		super.act(delta);
	}

	@Override
	public void button4() {
		panel.setDefault(true);
	}
	
	@Override
	public RandomCrest getCrest() {
		if (center.faction != null)
			return center.faction.randomCrest;
		else return null;
	}
}
