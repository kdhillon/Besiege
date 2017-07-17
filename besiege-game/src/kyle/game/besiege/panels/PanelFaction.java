/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.esotericsoftware.tablelayout.Cell;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.Faction;
import kyle.game.besiege.army.Noble;
import kyle.game.besiege.location.Castle;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.ObjectLabel;
import kyle.game.besiege.location.Village;

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
public class PanelFaction extends Panel {
	private Faction faction;

	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 350;
	private final int r = 3;
	private final String tablePatch = "grey-d9";
	private SidePanel panel;

	private Table text;
	private Label title;
	private Label wealth;
	private Table cities;
	private Label citiesC;
	private Table castles;
	private Label castlesC;
	private Table villages;
	private Label villagesC;
	private Table locations;
	private ScrollPane locationPane;
	private Table nobles;
	private Label noblesC;
	private Label emptyCities;
	private Label emptyCastles;
	private Label emptyVillages;
	private ScrollPane noblesPane;
	private Table relations;
	private Label relationsC;
	private ScrollPane relationsPane;

	//	private Table stats;
	//	private Label nameS;
	//	private Label levelS;
	//	private Label expS;
	//	private Label nextS;
	//	private Label atkS;
	//	private Label defS;
	//	private Label spdS;
	//	private Label weaponS;
	//	private Label equipmentS;

	private LabelStyle ls;
	private LabelStyle lsG;	// wounded
	private LabelStyle lsY; // upgrade

	public boolean playerTouched;

	public PanelFaction(SidePanel panel, Faction faction) {
		this.panel = panel;
		this.faction = faction;
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

		citiesC = new Label("Cities:", ls);
		castlesC = new Label("Castles:", ls);
		villagesC = new Label("Villages:", ls);
		noblesC = new Label("Nobles:", ls);
		emptyCastles = new Label("None",ls);
		emptyCities = new Label("None",ls);
		emptyVillages = new Label("None",ls);
		relationsC = new Label("Faction Relations:", ls);
		Label wealthC = new Label("Wealth:",ls); // maybe give this a mouseover for description.

		title = new Label("", lsBig);
		title.setAlignment(0,0);
		title.setWrap(true);
		title.setWidth(SidePanel.WIDTH-PAD*2-MINI_PAD*2);
		title.setText(faction.name);

		wealth = new Label("", ls);
		cities = new Table();

		// Create text
		text = new Table();
//		text.debug();
		text.defaults().padTop(NEG).left();

		//		title.addListener(new InputListener() {
		//			public boolean touchDown(InputEvent event, float x,
		//					float y, int pointer, int button) {
		//				return true;
		//			}
		//			public void touchUp(InputEvent event, float x, float y,
		//					int pointer, int button) {
		//				centerCamera();
		//			}
		//		});

		text.add(title).colspan(2).fillX().expandX().padBottom(MINI_PAD);
		text.row();
		text.add().width((SidePanel.WIDTH-PAD*2)/2);
		text.add().width((SidePanel.WIDTH-PAD*2)/2);
		text.row();
		text.add(wealthC).padLeft(MINI_PAD).center();
		text.add(wealth).center();
		text.row();
		text.add().colspan(2).padTop(MINI_PAD);
		text.row();

		cities = new Table();
		//		cities.debug();
		//cities.defaults().padTop(NEG);
		cities.top();
		cities.defaults().left().padTop(NEG).expandX();

		castles = new Table();
		//		castles.debug();
		castles.top();
		castles.defaults().left().padTop(NEG).expandX();

		villages = new Table();
		villages.top();
		villages.defaults().left().padTop(NEG).expandX();		
	
		locations = new Table();
		//		locations.debug();
		locations.top();
		locations.defaults().left().top();
		locations.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));
		locations.add().width((SidePanel.WIDTH-PAD*2)/2);
		locations.add().width((SidePanel.WIDTH-PAD*2)/2);
		locations.row();
		locations.add(cities).width((SidePanel.WIDTH-PAD*2));
		locations.row();
		locations.add(castles).width((SidePanel.WIDTH-PAD*2));
		locations.row();
		locations.add(villages).width((SidePanel.WIDTH-PAD*2));
		locationPane = new ScrollPane(locations);
		locationPane.setScrollbarsOnTop(true);
		locationPane.setFadeScrollBars(false);

		text.add(locationPane).colspan(2).top().padTop(0);
		text.row();
		text.add().colspan(2).padTop(PAD);
		text.row();

		nobles = new Table();
		//		nobles.debug();
		nobles.top();
		nobles.defaults().padTop(NEG).left();
		noblesPane = new ScrollPane(nobles);
		noblesPane.setScrollbarsOnTop(true);
		noblesPane.setFadeScrollBars(false);
		nobles.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));		
		nobles.add().width((SidePanel.WIDTH-PAD*2)/2);
		nobles.add().width((SidePanel.WIDTH-PAD*2)/2);
		nobles.row();

		text.add(noblesPane).colspan(2);
		text.row();
		text.add().colspan(2).padTop(PAD);
		text.row();

		relations = new Table();
		relations.top();
		relations.defaults().padTop(NEG).left();
		relationsPane = new ScrollPane(relations);
		relationsPane.setScrollbarsOnTop(true);
		relationsPane.setFadeScrollBars(false);
		relations.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));		

		text.add(relationsPane).colspan(2);
		text.row();

		this.addTopTable(text);

		this.setButton(4, "Back");
		updateRelations();
	}

	@Override
	public void act(float delta) {
		wealth.setText(faction.getTotalWealth() + "");
		
		// only need to update cities when a) this panel is opened b) cities change hands
		// TODO clean up memory leaks
		updateCities();
		updateCastles();
		updateVillages();
		updateNobles();
		updateRelations();
		super.act(delta);
	}

	public void updateCities() {
		cities.clear(); // clearing the table is a problem right now. it hides the scroll bar and prevents click-drag scrolling
		cities.padLeft(MINI_PAD).padRight(MINI_PAD);
		cities.defaults().left();
		cities.add(citiesC).center();
		cities.row();
		if (faction.cities.size != 0) {	
			for (City c : faction.cities) {
				if (c.label == null) {
					c.label = new ObjectLabel(c.getName() + " (" + c.getWealth() + ")", ls, c);
					c.label.addListener(new InputListener() {
						public boolean touchDown(InputEvent event, float x,
								float y, int pointer, int button) {
							return true;
						}
						public void touchUp(InputEvent event, float x, float y,
								int pointer, int button) {
							setActiveLocation((Location) ((ObjectLabel) event.getTarget()).object);
						}
					});
				}
				cities.add(c.label);
				cities.row();
			}
		}
		else {
			cities.add(emptyCities).top().left();	
		}	
	}
	public void updateCastles() {
		castles.clear(); // clearing the table is a problem right now. it hides the scroll bar and prevents click-drag scrolling
		castles.padLeft(MINI_PAD).padRight(MINI_PAD);
		castles.add(castlesC).center();
		castles.row();
		if (faction.castles.size > 0) {
			for (Castle c : faction.castles) {
				if (c.label == null) {
					c.label = new ObjectLabel(c.getName(), ls, c);
					c.label.addListener(new InputListener() {
						public boolean touchDown(InputEvent event, float x,
								float y, int pointer, int button) {
							return true;
						}
						public void touchUp(InputEvent event, float x, float y,
								int pointer, int button) {
							setActiveLocation((Location) ((ObjectLabel) event.getTarget()).object);
						}
					});
				}
				castles.add(c.label);
				castles.row();
			}
		}
		else {
			castles.add(emptyCastles).expandY().fillY().left();	
		}	
	}

	public void updateVillages() {
		villages.clear(); // clearing the table is a problem right now. it hides the scroll bar and prevents click-drag scrolling
		villages.padLeft(MINI_PAD).padRight(MINI_PAD);
		villages.add(villagesC).center();
		villages.row();
		if (faction.villages.size > 0) {
			for (Village c : faction.villages) {
				if (c.label == null) {
					c.label = new ObjectLabel(c.getName() + " (" + c.getWealth() +")", ls, c);
					c.label.addListener(new InputListener() {
						public boolean touchDown(InputEvent event, float x,
								float y, int pointer, int button) {
							return true;
						}
						public void touchUp(InputEvent event, float x, float y,
								int pointer, int button) {
							setActiveLocation((Location) ((ObjectLabel) event.getTarget()).object);
						}
					});
				}
				villages.add(c.label);
				villages.row();
			}
		}
		else {
			villages.add(emptyVillages).expandY().fillY().left();	
		}	
	}
	
	public void updateNobles() {
		nobles.clear(); 
		nobles.padLeft(MINI_PAD).padRight(MINI_PAD);
		nobles.add().colspan(2).width((SidePanel.WIDTH-PAD*2));
		nobles.row();
		nobles.add(noblesC).colspan(2).center();
		nobles.row();
		for (Noble noble : faction.nobles) {
			ObjectLabel name = new ObjectLabel(noble.getName(), ls, noble);
			name.addListener(new InputListener() {
				public boolean touchDown(InputEvent event, float x,
						float y, int pointer, int button) {
					return true;
				}
				public void touchUp(InputEvent event, float x, float y,
						int pointer, int button) {
					setActiveNoble((Noble) ((ObjectLabel) event.getTarget()).object);
				}
			});
			nobles.add(name);
			ObjectLabel troopCount = new ObjectLabel(noble.getTroopCount() +"", ls, noble);
			nobles.add(troopCount).right();
			nobles.row();
		}
	}

	public void updateRelations() {
		relations.clear(); 
		relations.padLeft(MINI_PAD).padRight(MINI_PAD);
		relations.add().colspan(2).width((SidePanel.WIDTH-PAD*2));
		relations.row();
		relations.add(relationsC).colspan(2).center();
		relations.row();

		// don't want to recreate these things every frame  yo
		for (Faction f : faction.kingdom.factions) {
			if ((f != faction) && f != Faction.BANDITS_FACTION && f != Faction.ROGUE_FACTION) {
				if (f.label == null) {
					f.label = new ObjectLabel(f.name, ls, f);
					f.label.addListener(new InputListener() {
						public boolean touchDown(InputEvent event, float x,
								float y, int pointer, int button) {
							return true;
						}
						public void touchUp(InputEvent event, float x, float y,
								int pointer, int button) {
							setActiveFaction((Faction) ((ObjectLabel) event.getTarget()).object);
						}
					});
				}
				
				relations.add(f.miniCrest).height(24).width(16).left().padRight(3).expandY();
				relations.add(f.label).left().expandX();

				// only intialize new when text is wrong or something like that
				if (f.label2 == null || !f.label2.getText().toString().equals(f.calcRelations(faction) +"")) {
					f.label2 = new ObjectLabel(f.calcRelations(faction) +"", ls, f);
					
					// Set the label color to be along a spectrum from red to green
					int n = f.calcRelations(faction);
					n = (-n + 50);
					System.out.println(n);
					int R = (int) ((255.0 * n) / 100.0);
					int	G = (int) ((255 * (100.0 - n)) / 100.0);
					int B = 0;
					System.out.println(R + ", " + G + ", " + B);
					Color labelColor = new Color(R/255f, G/255f, B/255f, 1);
					f.label2.setColor(labelColor);
				}
				relations.add(f.label2).right().padLeft(-25);
				relations.row();
			}
		}
	}

	//	public void setStats(Soldier s) {
	//		stats.setVisible(true);
	//		nameS.setText(s.name + "");
	//		levelS.setText(s.level + "");
	//		expS.setText(s.exp + "");
	//		nextS.setText(s.next + "");
	//		if (s.bonusAtk >= 0)
	//			atkS.setText(s.atk + " (" + s.baseAtk + "+" + s.bonusAtk + ")");
	//		else 
	//			atkS.setText(s.atk + " (" + s.baseAtk + s.bonusAtk + ")");
	//		if (s.bonusDef >= 0)
	//			defS.setText(s.def + " (" + s.baseDef + "+" + s.bonusDef + ")");
	//		else 
	//			defS.setText(s.def + " (" + s.baseDef + s.bonusDef + ")");
	//		if (s.bonusSpd >= 0)
	//			spdS.setText(s.spd + " (" + s.baseSpd + "+" + s.bonusSpd + ")");
	//		else 
	//			spdS.setText(s.spd + " (" + s.baseSpd + s.bonusSpd + ")");
	//		weaponS.setText(s.weapon.name);
	//		equipmentS.setText(s.equipmentList());
	//	}

	//	public void clearStats() {
	//		stats.setVisible(false);
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

	@Override
	public void resize() { // problem with getting scroll bar to appear...
		Cell cell = text.getCell(locationPane);
		cell.height((panel.getHeight() - DESC_HEIGHT)/3).setWidget(null);
		//		locationPane = new ScrollPane(locations);
		locationPane.setHeight((panel.getHeight() - DESC_HEIGHT)/3);
		//		locationPane.setScrollingDisabled(true, false);
		//		locationPane.setFadeScrollBars(false); 
		//		locationPane.setScrollbarsOnTop(true);
		cell.setWidget(locationPane);

		cell = text.getCell(noblesPane);
		cell.height((panel.getHeight() - DESC_HEIGHT)/3).setWidget(null);
		//		relationsPane = new ScrollPane(relations);
		noblesPane.setHeight((panel.getHeight() - DESC_HEIGHT)/3);
		//		relationsPane.setScrollingDisabled(true, false);
		//		relationsPane.setFadeScrollBars(false);
		//		relationsPane.setScrollbarsOnTop(true);
		cell.setWidget(noblesPane);

		cell = text.getCell(relationsPane);
		cell.height((panel.getHeight() - DESC_HEIGHT)/3).setWidget(null);
		//		relationsPane = new ScrollPane(relations);
		relationsPane.setHeight((panel.getHeight() - DESC_HEIGHT)/3);
		//		relationsPane.setScrollingDisabled(true, false);
		//		relationsPane.setFadeScrollBars(false);
		//		relationsPane.setScrollbarsOnTop(true);
		cell.setWidget(relationsPane);
		super.resize();
	}

	@Override
	public void button1() {

	}
	@Override
	public void button2() {

	}
	@Override
	public void button3() {

	}
	@Override
	public void button4() {
		panel.returnToPrevious();
	}

	@Override
	public Crest getCrest() {
		return faction.crest;
	}
}

