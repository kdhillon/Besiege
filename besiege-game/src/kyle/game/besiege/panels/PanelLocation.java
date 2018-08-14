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
import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.party.Party;

public class PanelLocation extends Panel {
	private final float MINI_PAD = 5;
	private final float DESC_HEIGHT = 300;
	private SidePanel panel;
	public Location location;
	
	private TopTable topTable;
	
	private SoldierTable soldierTable;
	
	private LabelStyle ls;
	private LabelStyle lsMed;
	private LabelStyle lsG;
	
	private boolean playerIn;
	private PanelHire panelHire;
	private boolean playerTouched;
	private boolean playerWaiting;
	private boolean playerBesieging;

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
		
		topTable = new TopTable(2);
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
		topTable.updateSubtitle(location.getFactionName(), new InputListener() {
			public boolean touchDown(InputEvent event, float x,
					float y, int pointer, int button) {
				return true;
			}
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				setActiveFaction();
			}
		});
		topTable.updateSubtitle2(location.getTypeStr(),  null);
		
		topTable.addBigLabel("Garrison", "Garrison:");
		topTable.addSmallLabel("Pop", "Pop:");
		topTable.addSmallLabel("Wealth", "Wealth:");

		soldierTable = new SoldierTable(location.getParty());
		topTable.add(soldierTable).colspan(4).top().padTop(0).expandY();

		topTable.row();
		//stats.debug();
		topTable.padLeft(MINI_PAD);
		this.addTopTable(topTable);

		playerIn = false;
//		this.hireMode = false;

        if (!location.ruin)
		    this.panelHire = new PanelHire(panel, location);
				
		location.needsUpdate = true;
		System.out.println("just created new panellocation");
		
		this.setButton(4, "Back"); 
	}
	
	@Override
	public void act(float delta) {
        // hostile player touches
        if (location.hostilePlayerTouched && !playerTouched) {
            if (location.underSiege()) {
                if (location.isVillage())
                    setButton(1, "Continue Raid");
                else setButton(1, "Resume Siege");
            } else if (!location.underSiege()) {
                if (location.isVillage())
                    setButton(1, "Raid");
                else setButton(1, "Besiege");
            }
            setButton(2, "Withdraw");
            setButton(4, null);
            playerTouched = true;
        }
        // hostile player leaves

        else if (!location.hostilePlayerTouched && playerTouched && !location.playerBesieging) {
            setButton(1, null);
            setButton(2, null);
            setButton(4, "Back");
            playerTouched = false;
        } else if (location.playerBesieging && !playerBesieging) {
            // turn on siegeOrRaid panel
            setButton(1, "Charge!");
            setButton(2, "Wait");
            setButton(4, "Withdraw");
            playerBesieging = true;
//			System.out.println("siegeOrRaid panel on");
        } else if (!location.playerBesieging && playerBesieging) {
            // turn off siegeOrRaid panel
            setButton(1, null);
            setButton(2, null);
            setButton(4, "Back");
            playerBesieging = false;
//			System.out.println("siegeOrRaid panel off");
        }
        // friendly player touches
        else if (location.playerIn && !playerIn) {
            if (location.toHire != null && location.toHire.getHealthySize() > 0)
                setButton(2, "Hire");
            playerIn = true;
        }
        // friendly player leaves
        else if (!location.playerIn && playerIn) {
            setButton(1, null);
            setButton(2, null);
            playerIn = false;
        }
        // if hostile player is waiting at a siegeOrRaid
        else if (location.playerBesieging && playerBesieging) {
            // start wait
            if (location.playerWaiting && playerWaiting) {
                setButton(1, null);
                setButton(2, null);
                setButton(4, "Stop");
                playerWaiting = false;
//				System.out.println("setting to null");
            }
            // stop wait
            else if (!location.playerWaiting && !playerWaiting) {
                setButton(1, "Charge!");
                setButton(2, "Wait");
                setButton(4, "Withdraw");
                playerWaiting = true;
//				System.out.println("setting to rest");
            }
        }
        // if friendly player is inside
        else if (location.playerIn && playerIn) {
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
                garrStr = totalGarr + location.garrison.getParty().getHealthySize() + " (" + garrStr + "+" + totalGarr + ")";
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
            topTable.updateSubtitle("Under Siege!", null);
        else {
            if (location.getKingdom().getPlayer().isAtWar(location))
                topTable.updateSubtitle(location.getFactionName() + " (at war)", null);
            else topTable.updateSubtitle(location.getFactionName(), null);
        }

        if (location.needsUpdate) {
            soldierTable.update();
//            if (Math.random() < 0.3f)
            location.needsUpdate = false;
        }
        super.act(delta);
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
		Cell cell = topTable.getCell(soldierTable);
		cell.height(panel.getHeight() - DESC_HEIGHT).setWidget(null);
		soldierTable = new SoldierTable(getParty());
		location.needsUpdate = true;
		soldierTable.setHeight(panel.getHeight() - DESC_HEIGHT);
		cell.setWidget(soldierTable);
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
		
	}
	@Override
	public void button4() {
		if (location.playerWaiting) {
			location.stopWait();
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
		if (location.getFaction() == null) return null;
		return location.getFaction().crest;
	}
}
