/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Character;
import kyle.game.besiege.voronoi.Center;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class PanelCenter extends Panel {
	private final float PAD = 5;
	private final float NEG = -5;
	private SidePanel panel;
	private Center center;
	private Table text;
	private Label regionC;
	private Label biome;
	private Label faction;
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
		ls30.font = Assets.pixel30;
		LabelStyle ls24 = new LabelStyle();
		ls24.font = Assets.pixel20;
		
		Label factionC = new Label("Faction:", ls24);
		Label biomeC = new Label("Biome:", ls24); 

		regionC = new Label("Region", ls24);
		regionC.setAlignment(0,0);

		biome = new Label("" ,ls24);
		faction = new Label("", ls24);
		title = new Label(center.getName(), ls30);
		title.setAlignment(0,0);
		
		text.add(title).colspan(2).width(SidePanel.WIDTH-PAD*2).fillX().expandX().center();
		text.row();
		text.add(regionC).colspan(2).padBottom(PAD).width(SidePanel.WIDTH-PAD*2).fillX().expandX().center();
		text.row();
		text.add(biomeC).left();
		biome.setWrap(true);
		text.add(biome).left().fillX().expandX().padLeft(PAD);
		text.row();
		text.add(factionC).left();
		text.add(faction).left().fillX().expandX().padLeft(PAD);
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
		biome.setText(center.biome.toString());
		if (center.faction != null)
			faction.setText(center.faction.name);
		super.act(delta);
	}

	@Override
	public void button4() {
		panel.setDefault();
	}
	
	@Override
	public TextureRegion getCrest() {
		if (center.faction != null)
			return center.faction.crest;
		else return null;
	}
}
