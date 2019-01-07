/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.esotericsoftware.tablelayout.Cell;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Crest;
import kyle.game.besiege.CrestDraw;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.party.Party;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PanelLocation extends Panel {
	private final float MINI_PAD = 5;
	private final float DESC_HEIGHT = 300;
	private SidePanel panel;
	public Location location;
	
	private TopTable topTable;

	private LabelStyle ls;
	private LabelStyle lsMed;
	private LabelStyle lsG;
	
	private boolean playerIn;
	private PanelHire panelHire;
	private boolean playerTouched;
	private boolean playerWaiting;
	private boolean playerBesieging;

	SoldierTable garrisonSoldierTable;

	private HashMap<Party, SoldierTable> garrisonedTables = new HashMap<>();

	public PanelLocation(SidePanel panel, Location location) {
		this.panel = panel;
		this.location = location;
		
		this.addParentPanel(panel);
		this.playerTouched = false;
		
		LabelStyle lsBig = new LabelStyle();
		lsBig.font = Assets.pixel22;
		
		lsMed = new LabelStyle();
		lsMed.font = Assets.pixel18;
		
		ls = new LabelStyle();
		ls.font = Assets.pixel16;
		
		lsG = new LabelStyle();
		lsG.font = Assets.pixel16;
		lsG.fontColor = Color.GRAY;
		
		topTable = new TopTable();
		topTable.updateTitle(location.getName(), new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				centerCamera();
			}
		});
		topTable.addSubtitle("locationtype", location.getTypeStr(),  null);
		topTable.addSubtitle("factionname", location.getFactionName(), new InputListener() {
			public boolean touchDown(InputEvent event, float x,
									 float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
								int pointer, int button) {
				setActiveFaction();
			}
		});
		
		topTable.addBigLabel("Garrison", "Garrison:");
		topTable.addSmallLabel("Pop", "Pop:");
		topTable.addSmallLabel("Wealth", "Wealth:");
		topTable.row();

		//stats.debug();
		topTable.padLeft(MINI_PAD);
		this.addTopTable(topTable);

		garrisonSoldierTable = new SoldierTable(this, location.getParty());
		addSoldierTable(garrisonSoldierTable);
		updateSoldierTables();
		playerIn = false;
//		this.hireMode = false;

		location.needsUpdate = true;
		System.out.println("just created new panellocation");
		
		this.setButton(4, "Back"); 
	}
	
	@Override
	public void act(float delta) {
        // hostile player touches
        if (location.hostilePlayerTouched && !playerTouched && !location.isRuin()) {
            if (location.underSiege()) {
                if (location.isVillage())
                    setButton(1, "Continue Raid");
                else setButton(1, "Resume Siege");
            } else if (!location.underSiege()) {
                if (location.isVillage()) {
					if (!((Village) location).raided())
						setButton(1, "Raid");
				}
                else setButton(1, "Besiege");
            }
            setButton(2, "Withdraw");
			setButton(3, null);
			setButton(4, null);
            playerTouched = true;
        }
        // hostile player leaves

        else if (!location.hostilePlayerTouched && playerTouched && !location.playerBesieging) {
            setButton(1, null);
            setButton(2, null);
			setButton(3, null);
			setButton(4, "Back");
			sidePanel.setHardStay(false);
			sidePanel.setDefault(false);
            // Player just left, reset the buttons
            playerTouched = false;
        } else if (location.playerBesieging && !playerBesieging) {
            // turn on siegeOrRaid panel
            setButton(1, "Charge!");
            setButton(2, "Wait");
			setButton(3, null);
			setButton(4, "Withdraw");
            playerBesieging = true;
//			System.out.println("siegeOrRaid panel on");
        } else if (!location.playerBesieging && playerBesieging) {
            // turn off siegeOrRaid panel
            setButton(1, null);
            setButton(2, null);
			setButton(3, null);
            setButton(4, "Back");
            playerBesieging = false;
//			System.out.println("siegeOrRaid panel off");
        }
        // friendly player touches
        else if (location.playerIn && !playerIn) {
			setButton(1, "Rest");
            if (location.toHire != null ) // && location.toHire.getHealthySize() > 0
                setButton(2, "Hire");
            playerIn = true;
			if (this.panelHire == null && !this.location.isRuin())
				this.panelHire = new PanelHire(panel, location);
			if (location.getKingdom().getPlayer().getPanelCaptives() != null) {
//				this.panelHire = new PanelHire(panel, location);
				setButton(3, "Captives");
			}
        }
        // friendly player leaves
        else if (!location.playerIn && playerIn) {
        	System.out.println("friendly player leavin, setting buttons to null");
            setButton(1, null);
            setButton(2, null);
			setButton(3, null);
			playerIn = false;
        }
        // if hostile player is waiting at a siegeOrRaid
        else if (location.playerBesieging && playerBesieging) {
            // start wait
            if (location.playerWaiting && playerWaiting) {
                setButton(1, null);
                setButton(2, null);
				setButton(3, null);
				setButton(4, "Stop");
                playerWaiting = false;
//				System.out.println("setting to null");
            }
            // stop wait
            else if (!location.playerWaiting && !playerWaiting) {
                setButton(1, "Charge!");
                setButton(2, "Wait");
				setButton(3, null);
				setButton(4, "Withdraw");
                playerWaiting = true;
//				System.out.println("setting to rest");
            }
        }
        // if friendly player is inside
        else if (location.playerIn && playerIn) {
//			System.out.println("player inside location");

			//start Wait
            if (location.playerWaiting && playerWaiting) {
                setButton(1, null);
				setButton(4, "Stop");
                playerWaiting = false;
            }
            //stop Wait
            else if (!location.playerWaiting && !playerWaiting) {
                setButton(1, "Rest");
				setButton(4, "Back");
                playerWaiting = true;
            }
        }
//		else if (location.underSiege() && location.getKingdom().getPlayer().isInSiege() && location.getKingdom().getPlayer().getSiege().location == location) {

        if (!location.ruin) {
            String garrStr = getParty().getHealthySize() + "";

            int totalGarr = 0;
            for (Army a : location.getGarrisoned()) {
                // don't show passive armies in garrison
                if (a.passive) continue;
                //			garrStr += "+" + a.getParty().getHealthySize();
                totalGarr += a.getParty().getHealthySize();
            }
            //		if (totalGarr > 0) garrStr += "+" + totalGarr;
            if (totalGarr > 0)
                garrStr = totalGarr + location.garrison.getHealthySize() + " (" + garrStr + "+" + totalGarr + ")";
            topTable.update("Garrison", garrStr);

            topTable.update("Pop", (int) location.getPop() + "");
            topTable.update("Wealth", "" + location.getWealth());
        } else {
            String garrStr = "";

            int totalGarr = 0;
            for (Army a : location.getGarrisoned()) {
                //			garrStr += "+" + a.getParty().getHealthySize();
                // don't show passive armies in garrison
                if (a.passive) continue;
                totalGarr += a.getParty().getHealthySize();
            }
            //		if (totalGarr > 0) garrStr += "+" + totalGarr;
            if (totalGarr > 0) garrStr = totalGarr + " (" + garrStr + "+" + totalGarr + ")";
            topTable.update("Garrison", garrStr);

            topTable.update("Pop", (int) location.getPop() + "");
            topTable.update("Wealth", "0");
        }

        if (location.underSiege())
            topTable.update("factionname", "Under Siege!", null);
        else {
            if (location.type != Location.LocationType.RUIN && location.getKingdom().getPlayer().isAtWar(location))
                topTable.update("factionname", location.getFactionName() + " (at war)", null);
            else topTable.update("factionname", location.getFactionName(), null);
        }

        // Update all parties.
        if (location.needsUpdate) {
            garrisonedTables.get(location.getParty()).update();
//            if (Math.random() < 0.3f)
			topTable.update("locationtype", location.getTypeStr(),  null);
			location.needsUpdate = false;
		}

		if (garrisonedTables.size() != location.getGarrisonedAndGarrison().size) {
        	System.out.println("updating soldier tables for location: " + location.getName());
        	updateSoldierTables();
		}

		Set<Party> parties = garrisonedTables.keySet();
		for (Party p : parties) {
			if (p == null) continue;
			if (p.updated) {
				garrisonedTables.get(p).update();
				p.updated = false;
			}
		}

		super.act(delta);
    }

    // TODO This should rn keep around sts for parties that have left the garrison.
    private void updateSoldierTables() {
		System.out.println("updating soldier tables");
		Collection<SoldierTable> c = garrisonedTables.values();
		c.remove(garrisonSoldierTable);
		this.clearSoldierTables(c);

		garrisonedTables.clear();
		garrisonedTables.put(location.getParty(), garrisonSoldierTable);

		for (Army a : location.getGarrisoned()) {
			if (a.passive) continue;
			SoldierTable st = new SoldierTable(this, a.getParty());
			addSoldierTable(st);
//			topTable.add(st).colspan(4).top().padTop(MINI_PAD).expandY();
//			topTable.row();
			garrisonedTables.put(a.getParty(), st);
		}
	}
	
	public Party getParty() {
		return this.location.getParty();
	}
	
	public void setActiveFaction() {
		panel.setActiveFaction(location.getFaction());
	}
	private void centerCamera() {
		Camera camera = panel.getKingdom().getMapScreen().getCamera();
//		camera.translate(new Vector2(location.getCenterX()-camera.position.x, location.getCenterY()-camera.position.y));
		camera.translate(new Vector3(location.getCenterX()-camera.position.x, location.getCenterY()-camera.position.y, 0));
	}
	
	@Override
	public void resize() { // problem with getting scroll bar to appear...
//		Set<Party> set = new HashSet<>(garrisonedTables.keySet());
//		// p may be null (for ruins)
//		for (Party p : set) {
//			SoldierTable soldierTable = garrisonedTables.get(p);
//			garrisonedTables.remove(p);
//
//			Cell cell = topTable.getCell(soldierTable);
////			cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
//			soldierTable = new SoldierTable(this, p);
////			soldierTable.setHeight(panel.getHeight() - DESC_HEIGHT);
//			cell.setWidget(soldierTable);
//			garrisonedTables.put(p, soldierTable);
//			soldierTable.update();
//			// may be unnecessary
////			p.updated = true;
//		}
		location.needsUpdate = true;
		super.resize();
	}
	
	@Override
	public void button1() {
		if (this.getButton(1).isVisible()) {
			if (playerIn) {
				location.startWait();
			}
			else if (playerBesieging && !location.playerWaiting) {
				BottomPanel.log("charge!");
				
				// TODO temp fix
				if (location.getSiege() == null) {
					playerBesieging = false;
					return; 
				}
				location.getSiege().attack();
				System.out.println("ATTACKING ");
			}
			else { // besiege/raid
				panel.setHardStay(false);
				if (!location.underSiege()) {
					if (location.isVillage()) {
						panel.getKingdom().getPlayer().raid((Village) location);
					}
					else {
						BottomPanel.log("Besieging " + location.getName());
						panel.getKingdom().getPlayer().besiege(location);
					}
				}
				else {
//					if (location.isVillage()) {
//						panel.getKingdom().getPlayer().raid((Village) location);
//					}
//					else {
						BottomPanel.log("Resuming siegeOrRaid of" + location.getName());
						location.getSiege().add(panel.getKingdom().getPlayer());
//					}
				}
			}
		}
	}
	@Override
	public void button2() {
		if (this.getButton(2).isVisible()) {
			if (playerIn) {
				panel.setActive(this.panelHire);
			}
			else if (playerBesieging) {
				BottomPanel.log("waiting");
				location.startWait();
			}
			else {
				panel.setHardStay(false);
				panel.setDefault(true);		
			}
		}
	}
	@Override
	public void button3() {
		if (this.getButton(3).isVisible()) {
			if (playerIn) {
				location.getKingdom().getPlayer().getPanelCaptives().updateSoldierTable();
				panel.setActive(location.getKingdom().getPlayer().getPanelCaptives());
			} else throw new AssertionError();
		}
	}
	@Override
	public void button4() {
		if (location.playerWaiting) {
			location.stopWait();
			sidePanel.setHardStay(true);
		}
		else if (playerBesieging) {
			location.getKingdom().getPlayer().leaveSiege();
			BottomPanel.log("Withdraw!");
		}
		else {
			panel.setDefault(true);
		}
	}
	
	@Override
	public Crest getCrest() {
		if (location.isRuin()) return null;
		if (location.getFaction() == null) return CrestDraw.defaultCrest;
		return location.getFaction().crest;
	}
}
