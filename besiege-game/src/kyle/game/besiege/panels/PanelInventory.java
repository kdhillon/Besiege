/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Inventory;
import kyle.game.besiege.army.Noble;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.ObjectLabel;
import kyle.game.besiege.party.ArmorType;
import kyle.game.besiege.party.RangedWeaponType;
import kyle.game.besiege.party.WeaponType;

// Displays basic info about the faction including relations with all others.
/* Should contain:
 * 	Total faction wealth (sum of all cities)
 * 	List of Cities? (Should list city's villages on locationPanel then)
 * 		Clicking on cities should redirect to City Panel.
 *  Later: list of warlords (barons or whatever)
 * 	List of relations with other factions (or maybe do this on a separate panel?)
 *  	Mousing over relations should give detailed info (base of 0, military actions (-/+), nearby cities (-), trade (+)) 
 * 		
 * 
 * 
 */
public class PanelInventory extends Panel {
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 350;
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private SidePanel panel;

	private Table text;
	private Label title;
	
	private Table weapons;
	private Table rangedWeapons;
	private Table armors;
	private Label weaponsC;
	private Label rangedC;
	private Label armorC;
	private Label emptyWeapons;
	private Label emptyRanged;
	private Label emptyArmor;
	private ScrollPane weaponsPane;
	private ScrollPane rangedWeaponsPane;
	private ScrollPane armorsPane;

	private LabelStyle ls;
	private LabelStyle lsG;	// wounded
	private LabelStyle lsY; // upgrade

	public boolean playerTouched;

	public PanelInventory(SidePanel panel) {
		this.panel = panel;
		this.addParentPanel(panel);

		LabelStyle lsBig = new LabelStyle();
		lsBig.font = Assets.pixel24;

		ls = new LabelStyle();
		ls.font = Assets.pixel16;

		lsG = new LabelStyle();
		lsG.font = Assets.pixel16;
		lsG.fontColor = Color.GRAY;

		lsY = new LabelStyle();
		lsY.font = Assets.pixel16;
		lsY.fontColor = Color.YELLOW;

		title = new Label("", lsBig);
		title.setAlignment(0,0);
		title.setWrap(true);
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
		title.setText("Inventory");

		weaponsC = new Label("Weapons:", ls);
		rangedC = new Label("Ranged Weapons:", ls);
		armorC = new Label("Armor:", ls);
		emptyWeapons = new Label("None",ls);
		emptyRanged = new Label("None", ls);
		emptyArmor = new Label("None", ls);
		// Create text
		text = new Table();
//		text.debug();
		text.defaults().padTop(NEG).left();

		text.add(title).fillX().expandX().padBottom(MINI_PAD);
		text.row();
		text.add().width((SidePanel.WIDTH-PAD*2));
		text.row();
		text.add().padTop(MINI_PAD);
		text.row();

//		cities = new Table();
//		//		cities.debug();
//		//cities.defaults().padTop(NEG);
//		cities.top();
//		cities.defaults().left().padTop(NEG).expandX();
//
//		castles = new Table();
//		//		castles.debug();
//		castles.top();
//		castles.defaults().left().padTop(NEG).expandX();
		
		weapons = new Table();
		//		locations.debug();
		weapons.top();
		weapons.defaults().padTop(NEG).left();
		weapons.setBackground(Assets.ninepatchBackgroundDarkGray);
		weapons.add().width((SidePanel.WIDTH-PAD*2));
		weapons.row();
//		weapons.add(cities).width((SidePanel.WIDTH-PAD*2)/2);
//		weapons.add(castles).width((SidePanel.WIDTH-PAD*2)/2);
		weaponsPane = new ScrollPane(weapons);
		weaponsPane.setScrollbarsOnTop(true);
		weaponsPane.setFadeScrollBars(false);

		text.add(weaponsPane).expandY().fillY();
		text.row();
		text.add().padTop(PAD);
		text.row();

		rangedWeapons = new Table();
		rangedWeapons.top();
		rangedWeapons.defaults().padTop(NEG).left();
		rangedWeaponsPane = new ScrollPane(rangedWeapons);
		rangedWeaponsPane.setScrollbarsOnTop(true);
		rangedWeaponsPane.setFadeScrollBars(false);
		rangedWeapons.setBackground(Assets.ninepatchBackgroundDarkGray);

		text.add(rangedWeaponsPane).expandY().fillY();
		text.row();
		text.add().padTop(PAD);
		text.row();

		armors = new Table();
		armors.top();
		armors.defaults().padTop(NEG).left();
		armorsPane = new ScrollPane(armors);
		armorsPane.setScrollbarsOnTop(true);
		armorsPane.setFadeScrollBars(false);
		armors.setBackground(Assets.ninepatchBackgroundDarkGray);

		text.add(armorsPane).expandY().fillY();		
		
//		relations = new Table();
//		relations.top();
//		relations.defaults().padTop(NEG).left();
//		relationsPane = new ScrollPane(relations);
//		relationsPane.setScrollbarsOnTop(true);
//		relationsPane.setFadeScrollBars(false);
//		relations.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));		
//
//		text.add(relationsPane).colspan(2);
//		text.row();
		
		text.row();

		this.addTopTable(text);

		this.setButton(4, "Back");
	}
	
	public Inventory getInventory() {
		return panel.character.inventory;
	}

	@Override
	public void act(float delta) {
		updateWeapons();
		updateRanged();
		updateArmors();
		super.act(delta);
	}
	
	public void updateWeapons() {
		weapons.clear(); 
		weapons.padLeft(MINI_PAD).padRight(MINI_PAD);
		weapons.add().colspan(2).width((SidePanel.WIDTH-PAD*2));
		weapons.row();
		weapons.add(weaponsC).colspan(2).center();
		weapons.row();
		if (getInventory().weapons.size() > 0) {
			for (WeaponType r : getInventory().weapons.keySet()) {
				int count = getInventory().weapons.get(r);
				ObjectLabel name = new ObjectLabel(r.name, ls, r);
				weapons.add(name);
				ObjectLabel countLabel = new ObjectLabel(count +"", ls, r);
				weapons.add(countLabel).right();
				weapons.row();
			}
		}
		else {
			weapons.add(emptyWeapons).center().colspan(2);	
		}	
	}
	
	public void updateRanged() {
		rangedWeapons.clear(); 
		rangedWeapons.padLeft(MINI_PAD).padRight(MINI_PAD);
		rangedWeapons.add().colspan(2).width((SidePanel.WIDTH-PAD*2));
		rangedWeapons.row();
		rangedWeapons.add(rangedC).colspan(2).center();
		rangedWeapons.row();
		if (getInventory().rangedWeapons.size() > 0) {
			for (RangedWeaponType r : getInventory().rangedWeapons.keySet()) {
				int count = getInventory().rangedWeapons.get(r);

				ObjectLabel name = new ObjectLabel(r.name, ls, r);
				rangedWeapons.add(name);
				ObjectLabel countLabel = new ObjectLabel(count +"", ls, r);
				rangedWeapons.add(countLabel).right();
				rangedWeapons.row();
			}
		}
		else {
			rangedWeapons.add(emptyRanged).center().colspan(2);	
		}	
	}
	
	public void updateArmors() {
		armors.clear(); 
		armors.padLeft(MINI_PAD).padRight(MINI_PAD);
		armors.add().colspan(2).width((SidePanel.WIDTH-PAD*2));
		armors.row();
		armors.add(armorC).colspan(2).center();
		armors.row();
		if (getInventory().armors.size() > 0) {
			for (ArmorType r : getInventory().armors.keySet()) {
				int count = getInventory().armors.get(r);
				ObjectLabel name = new ObjectLabel(r.name, ls, r);
				armors.add(name);
				ObjectLabel countLabel = new ObjectLabel(count +"", ls, r);
				armors.add(countLabel).right();
				armors.row();
			}
		}
		else {
			armors.add(emptyArmor).center().colspan(2);	
		}	
	}

//	public void updateNobles() {
//		nobles.clear(); 
//		nobles.padLeft(MINI_PAD).padRight(MINI_PAD);
//		nobles.add().colspan(2).width((SidePanel.WIDTH-PAD*2));
//		nobles.row();
//		nobles.add(noblesC).colspan(2).center();
//		nobles.row();
//		for (Noble noble : faction.nobles) {
//			ObjectLabel name = new ObjectLabel(noble.getName(), ls, noble);
//			name.addListener(new InputListener() {
//				public boolean touchDown(InputEvent event, float x,
//						float y, int pointer, int button) {
//					return true;
//				}
//				public void touchUp(InputEvent event, float x, float y,
//						int pointer, int button) {
//					setActiveNoble((Noble) ((ObjectLabel) event.getTarget()).object);
//				}
//			});
//			nobles.add(name);
//			ObjectLabel troopCount = new ObjectLabel(noble.getTroopCount() +"", ls, noble);
//			nobles.add(troopCount).right();
//			nobles.row();
//		}
//	}

	public void setActiveLocation(Location location) {
		panel.setActiveLocation(location);
	}
	public void setActiveNoble(Noble noble) {
		panel.setActiveArmy(noble);
	}
	public void setActiveFaction(Faction faction) {
		panel.setActiveFaction(faction);
	}

//	@Override
//	public void resize() { // problem with getting scroll bar to appear...
//		Cell cell = text.getCell(locationPane);
//		cell.height((panel.getHeight() - DESC_HEIGHT)/3).setWidget(null);
//		//		locationPane = new ScrollPane(locations);
//		locationPane.setHeight((panel.getHeight() - DESC_HEIGHT)/3);
//		//		locationPane.setScrollingDisabled(true, false);
//		//		locationPane.setFadeScrollBars(false); 
//		//		locationPane.setScrollbarsOnTop(true);
//		cell.setWidget(locationPane);
//
//		cell = text.getCell(noblesPane);
//		cell.height((panel.getHeight() - DESC_HEIGHT)/3).setWidget(null);
//		//		relationsPane = new ScrollPane(relations);
//		noblesPane.setHeight((panel.getHeight() - DESC_HEIGHT)/3);
//		//		relationsPane.setScrollingDisabled(true, false);
//		//		relationsPane.setFadeScrollBars(false);
//		//		relationsPane.setScrollbarsOnTop(true);
//		cell.setWidget(noblesPane);
//
//		cell = text.getCell(relationsPane);
//		cell.height((panel.getHeight() - DESC_HEIGHT)/3).setWidget(null);
//		//		relationsPane = new ScrollPane(relations);
//		relationsPane.setHeight((panel.getHeight() - DESC_HEIGHT)/3);
//		//		relationsPane.setScrollingDisabled(true, false);
//		//		relationsPane.setFadeScrollBars(false);
//		//		relationsPane.setScrollbarsOnTop(true);
//		cell.setWidget(relationsPane);
//		super.resize();
//	}

	@Override
	public void button1() {

	}
	@Override
	public void button2() {

	}
	@Override
	public void button3() {

	}


//	@Override
//	public RandomCrest getCrest() {
//		return null; // TODO try to draw current weapon
//	}
}

