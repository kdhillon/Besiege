package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import kyle.game.besiege.Assets;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.SoldierLabel;
import kyle.game.besiege.party.Subparty;

/**
 * Represents a UI element that contains a list of soldiers. May be single columned or multi columned (battle). Usually contains a playerPartyPanel, but may
 * contain standalone units, for hire or upgrading.
 * 
 * Usually a scrollpane. 
 *
 */
public class SoldierTable extends Table {
	private final float PAD = 10;
	private final float MINI_PAD = 5;
	private final float NEG = -5;
	private final float DESC_HEIGHT = 300;
	private final int r = 3; 
	private final String tablePatch = "grey-d9";
	
	private Table soldierTable;
    private ScrollPane soldierPane;

    private Soldier selected;
    public boolean selectable;

    private final Party party;

	private final LabelStyle ls = new LabelStyle();
    private final LabelStyle lsG = new LabelStyle();
	private final Label garrisonC;
	private final Label noTroopsC;
	private final Label nullC;
	private final Label prisonersC;
	private final Label emptyC;

	public SoldierTable(Party party) {
		ls.font = Assets.pixel16;
		lsG.font = Assets.pixel16;

		this.party = party;

		garrisonC = new Label("Garrison", ls);
		noTroopsC = new Label("No troops garrisoned!",ls);
		nullC = new Label("Garrison is null!",ls);
		prisonersC = new Label("Captured", ls);
		emptyC = new Label("", ls);
		
		soldierTable = new Table();
		soldierTable.defaults().padTop(NEG);
		soldierTable.top();
		soldierTable.setBackground(new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion(tablePatch), r,r,r,r)));
//		this.debug();
		soldierPane = new ScrollPane(soldierTable);
		soldierPane.setScrollbarsOnTop(true);
		soldierPane.setFadeScrollBars(false);
		
		this.add(soldierPane).top().width(SidePanel.WIDTH - PAD*2).expandY().fillY();
	}
	
	public void update() {
//	    if (playerPartyPanel == null || playerPartyPanel.getTotalSize() == 0) throw new AssertionError();
		soldierTable.clear(); // clearing the table is a problem right now. it hides the scroll bar and prevents click-drag scrolling
		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);

//		garrisonC.setAlignment(Align.center);
//		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
//		soldierTable.add(garrisonC).colspan(2);
		soldierTable.row();
		if (party == null) {
			noTroopsC.setAlignment(Align.center);
			soldierTable.add(nullC).colspan(2).center().width(SidePanel.WIDTH - PAD*2);
			soldierTable.row();
		}
		else if (party.getTotalSize() == 0) {
			noTroopsC.setAlignment(Align.center);
			soldierTable.add(noTroopsC).colspan(2).center().width(SidePanel.WIDTH - PAD*2);
			soldierTable.row();
		}
		else {
			updateWithParty(party, ls, lsG);
        }
	}

	// Currently the vertical size is too big, maybe because height isn't properly set?
	private void updateWithParty(Party party, LabelStyle style, LabelStyle wounded) {
		System.out.println("starting panelparty update: " + party.getName());
		for (final Subparty s : party.sub) {
			SoldierLabel general;
			if (s.general != null) {
//				System.out.println(s.general.getName());
				general = new SoldierLabel(s.general.getRank() + " " + s.general.getLastName(), style, s.general);
                general.addListener(new ClickListener() {
                    public boolean touchDown(InputEvent event, float x,
                                             float y, int pointer, int button) {
                        return true;
                    }
                    public void touchUp(InputEvent event, float x, float y,
                                        int pointer, int button) {
                        if (selectable) {
                            select(s.general);
                        } else {
                            switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
                        }
                    }
                });
                soldierTable.add(general).left().expandX();
                general.setColor(s.general.unitType.cultureType.colorDark);

                Label generalCount = new Label(s.getHealthySize() + "", style);
                soldierTable.add(generalCount).right();
                generalCount.setColor(s.general.unitType.cultureType.colorDark);
                soldierTable.row();
			}
			else {
//				general = new SoldierLabel("No general!", style, s.general);
			}

			updateTableWithTypes(s.getConsolHealthy(), style);
			updateTableWithTypes(s.getConsolWounded(), wounded);
		}
	}

    public class TypeLabel extends Label {
        StrictArray<Soldier> type;
        Table expand;
        boolean expanded = false;

        public TypeLabel(String name, LabelStyle ls) {
            super(name, ls);
            expand = new Table();
            this.addListener(new ClickListener() {
                public boolean touchDown(InputEvent event, float x,
                                         float y, int pointer, int button) {
                    return true;
                }
                public void touchUp(InputEvent event, float x, float y,
                                    int pointer, int button) {
                    if (expanded) {
                        clearExpand();
                        expanded = false;
                    }
                    else {
                        createExpand();
                        expanded = true;
                    }
                }
            });
        }
        public void createExpand() {
            for (final Soldier s : type) {
                SoldierLabel soldierName = new SoldierLabel(s.getName(), this.getStyle(), s);
                soldierName.setColor(Color.GRAY);
                expand.add(soldierName).left().padBottom(PanelUnit.NEG).expandX();

                soldierName.addListener(new ClickListener() {
                    public boolean touchDown(InputEvent event, float x,
                                             float y, int pointer, int button) {
                        return true;
                    }
                    public void touchUp(InputEvent event, float x, float y,
                                        int pointer, int button) {
                        if (selectable) {
                            select(s);
                        } else {
                            switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
                        }
                        switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
                    }
                });
                expand.row();
            }
        }
        public void clearExpand() {
            expand.clear();
        }
    }

    private static void switchToPanel(Soldier s) {
        MapScreen.sidePanelReference.setActiveUnit(s);
    }

	public void updateTableWithTypes(StrictArray<StrictArray<Soldier>> types, LabelStyle style) {
		//		table.debug();
        // TODO don't add General to this list (they're already listed above the other units)
		for (StrictArray<Soldier> type : types) {
			TypeLabel name = new TypeLabel(type.first().getTypeName(), style);
			name.type = type;
			name.setColor(type.first().unitType.cultureType.colorLite);
            soldierTable.add(name).left();
			Label count = new Label(type.size + "", style);
            soldierTable.add(count).right();
            soldierTable.row();

			float indent = 15;

			soldierTable.add(name.expand).expandX().left().padLeft(indent);
            soldierTable.row();
			name.expand.padBottom(-PanelUnit.NEG);
		}
	}

	public void select(Soldier soldier) {
	    this.selected = soldier;
	    update();
    }
	
	public void updateWithPrisoners(Party party) {
        if (party != null && party.getPrisoners().size > 0)
            prisonersC.setText("Captured");
        else prisonersC.setText("");
        prisonersC.setAlignment(0, 0);
        soldierTable.add(prisonersC).colspan(2).width(SidePanel.WIDTH - PAD * 2).expandX().fillX().padTop(0);
        soldierTable.row();
        updateTableWithTypes(party.getConsolPrisoners(), ls);
    }
}
