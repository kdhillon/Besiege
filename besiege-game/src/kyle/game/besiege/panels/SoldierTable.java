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
import kyle.game.besiege.battle.*;
import kyle.game.besiege.party.*;

/**
 * Represents a UI element that contains a list of soldiers. Usually contains a playerPartyPanel, but may
 * contain standalone units, for hire or upgrading. Basic display is a list of soldiers organized by unit type.
 * 
 * Usually a scrollpane. 
 *
 */
public class SoldierTable extends Table {
	private static final float PAD = 10;
	private static final float MINI_PAD = 5;
	private static final float NEG = -5;
	private static final float DESC_HEIGHT = 300;
	private static final float DEFAULT_INDENT = 15;
	private static final int r = 3;
	private static final String tablePatch = "grey-d9";
	private static final String LOCATION_EMPTY_TEXT = "No troops garrisoned!";
    private static Color SOLDIER_NAME_COLOR = Color.LIGHT_GRAY;
	private static Color SELECTED_COLOR = Color.YELLOW;

	private Table soldierTable;
    private ScrollPane soldierPane;

    private static final String EXPAND = " v";
    private static final String COLLAPSE = " ^";


    public Soldier selected;
    public Soldier nextToSelect; // everytime you select a soldier, figure out which one you should select next if the soldier is hired.

    private StrictArray<SoldierLabel> soldierLabels;

    public boolean hirePanel;
    public boolean selectable;

    protected final Party party;
    private final StrictArray<StrictArray<Soldier>> consolidatedWounded;
    private final StrictArray<StrictArray<Soldier>> consolidatedKilled;
    private int woundedCount;
    private int killedCount;

	private final LabelStyle ls = new LabelStyle();
    private final LabelStyle lsG = new LabelStyle();
    private final LabelStyle lsBig = new LabelStyle();

    private final Label garrisonC;
	protected final Label noTroopsC;
	private final Label nullC;
	private final Label prisonersC;
	private final Label emptyC;

    private boolean allowSubpartyCollapse = true;
    private boolean preventSoldierExpand = false;

    private boolean startAllCollapsed;

    // This is useful for displaying the current status of battlesubparties
    private BattleStage battleStage;

    public SoldierTable(Party party) {
        this(party, false, null, null, null);
    }

    public SoldierTable(Party party, boolean startAllCollapsed, BattleStage battle) {
        this(party, startAllCollapsed, battle, null, null);
        if (battle == null) throw new AssertionError();
    }

	public SoldierTable(Party party, boolean startAllCollapsed, BattleStage battle, StrictArray<StrictArray<Soldier>> wounded, StrictArray<StrictArray<Soldier>> killed) {
		ls.font = Assets.pixel16;
		lsG.font = Assets.pixel16;
		lsBig.font = Assets.pixel18;

		this.battleStage = battle;

		this.party = party;
		this.consolidatedWounded = wounded;
        this.consolidatedKilled = killed;
        if (consolidatedWounded != null) {
            for (StrictArray<Soldier> arr : consolidatedWounded) {
                woundedCount += arr.size;
            }
        }
        if (consolidatedKilled != null) {
            for (StrictArray<Soldier> arr : consolidatedKilled) {
                killedCount += arr.size;
            }
        }

        this.startAllCollapsed = startAllCollapsed;

		garrisonC = new Label("Garrison", ls);
		noTroopsC = new Label(LOCATION_EMPTY_TEXT,ls);
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

		soldierLabels = new StrictArray<>();
		
		this.add(soldierPane).top().width(SidePanel.WIDTH - PAD*2).expandY().fillY();
		this.selectable = true;
	}

	public void setAllowSubpartyCollapse(boolean allowSubpartyCollapse) {
	    this.allowSubpartyCollapse = allowSubpartyCollapse;
    }

    public void setPreventSoldierExpand(boolean preventSoldierExpand) {
	    this.preventSoldierExpand = preventSoldierExpand;
    }

	// Only updates the colors of the soldierlabels (for selecting)
	public void updateColors() {
	    for (SoldierLabel soldierLabel : soldierLabels) {
	        if (soldierLabel.soldier == selected) soldierLabel.setColor(SELECTED_COLOR);
	        else if (soldierLabel.soldier.isGeneral()) {
	            soldierLabel.setColor(soldierLabel.soldier.unitType.cultureType.colorDark);
            } else {
                soldierLabel.setColor(SOLDIER_NAME_COLOR);
            }
        }
    }

	// This does a full refresh.
	public void update() {
//	    if (playerPartyPanel == null || playerPartyPanel.getTotalSize() == 0) throw new AssertionError();
		soldierTable.clear(); // clearing the table is a problem right now. it hides the scroll bar and prevents click-drag scrolling
		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
		soldierLabels.clear();

//		garrisonC.setAlignment(Align.center);
//		soldierTable.padLeft(MINI_PAD).padRight(MINI_PAD);
//		soldierTable.add(garrisonC).colspan(2);
		soldierTable.row();
		if (party == null) {
		    if (consolidatedKilled != null || consolidatedWounded != null) {
                updateForPostBattle(consolidatedWounded, consolidatedKilled, ls);
            } else {
                noTroopsC.setAlignment(Align.center);
                soldierTable.add(nullC).colspan(2).center().width(SidePanel.WIDTH - PAD * 2);

                soldierTable.row();
            }
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

	private BattleSubParty getBspForSubparty(Subparty s) {
        if (battleStage != null) {
            BattleSubParty bsp = null;
            for (BattleSubParty b : battleStage.getDefending().subparties) {
                if (b.subparty == s) {
                    bsp = b;
                    break;
                }
            }
            for (BattleSubParty b : battleStage.getAttacking().subparties) {
                if (b.subparty == s) {
                    bsp = b;
                    break;
                }
            }
            return bsp;
        } return null;
//        /throw new AssertionError();
//        return null;
    }

    private void updateForPostBattle(StrictArray<StrictArray<Soldier>> wounded, StrictArray<StrictArray<Soldier>> killed, LabelStyle style) {
        Label woundedLabel = new Label("Wounded", style);
        Label woundedCount= new Label(this.woundedCount + "", style);
        soldierTable.add(woundedLabel).left().padLeft(0);
        soldierTable.add(woundedCount).right();
        soldierTable.row();
        updateTableWithTypes(wounded, style);

        Label killedLabel = new Label("Killed", style);
        Label killedCount = new Label(this.killedCount + "", style);
        soldierTable.add(killedLabel).left().padLeft(0);
        soldierTable.add(killedCount).right();
        soldierTable.row();
        updateTableWithTypes(killed, style);
        System.out.println("updating for post battle");
    }

	// Currently the vertical size is too big, maybe because height isn't properly set?
	private void updateWithParty(Party party, LabelStyle style, LabelStyle wounded) {
//		System.out.println("starting panelparty update: " + party.getName() + " with " + party.subparties.size + " subparties");
		for (final Subparty s : party.subparties) {
		    BattleSubParty bsp = getBspForSubparty(s);
//		    if (bsp == null) throw new AssertionError();

            final SoldierLabel general;
//            if (organizeByType) {
            if (allowSubpartyCollapse && !hirePanel) {
                updateTableWithTypesNew(s, style, bsp);
//                updateTableWithTypesNew(s.getGeneral(), s.getConsolWounded(), wounded);
            } else {

                // NOTE this stuff will only happen for panelhire
                if (s.getGeneral() != null) {
//				System.out.println(s.getGeneral().getName());
                    general = new SoldierLabel(s.getGeneral().getOfficialName(), style, s.getGeneral());
                    general.addListener(new ClickListener() {
                        public boolean touchDown(InputEvent event, float x,
                                                 float y, int pointer, int button) {
                            System.out.println("Dragging: " + general.getName());
                            if (selectable) {
                                if (selected == s.getGeneral()) {
                                    // Deselect happens in touchup
                                } else {
                                    select(s.getGeneral());
                                }
                            } else {
                                switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
                            }
                            return true;
                        }

                        public void touchUp(InputEvent event, float x, float y,
                                            int pointer, int button) {
                            System.out.println("Releasing: " + general.getName());
                            // Deselect happens in touchup
                            if (selectable) {
                                if (selected == s.getGeneral()) {
                                    deselect();
                                }
                            }

                        }
                    });
                    soldierTable.add(general).left().expandX();
                    general.setColor(s.getGeneral().unitType.cultureType.colorDark);

                    Label generalCount = new Label(s.getHealthySize() + "", style);
                    soldierTable.add(generalCount).left();
                    generalCount.setColor(s.getGeneral().unitType.cultureType.colorDark);
                    soldierTable.row();

                    soldierLabels.add(general);
                } else {
//				general = new SoldierLabel("No general!", style, s.getGeneral());
                }

                updateTableWithTypes(s.getConsolHealthy(), style);
                updateTableWithTypes(s.getConsolWounded(), wounded);
            }
        }
	}

    // This is a label of a general that includes a table of all subtypes below it
    public class SubpartyLabel extends Label {
	    Subparty subparty;
	    StrictArray<StrictArray<Soldier>> soldierLists;
        Table expand;
        public boolean expanded = false;
        private BattleSubParty bsp;

        public SubpartyLabel(Subparty subparty, LabelStyle ls, final BattleSubParty bsp) {
            super("", ls);
            final General general = subparty.getGeneral();
            if (general != null) {
                setText(general.getRank() + " " + general.getLastName());
            } else {
                // This happens when the general was wounded or killed recently.
                // TODO -- do we want to always have a general for a subparty?
                // I think yes. Even if the general is wounded, the subparty is still technically under the general.
                // If he dies, force the player to find a replacement or disband the subparty after the battle.
                // For now, automatically assign.
                throw new AssertionError();
            }
            this.bsp = bsp;
            this.subparty = subparty;
            this.setColor(general.unitType.cultureType.colorDark);
            this.soldierLists = subparty.getConsolHealthy();
            expand = new Table();
            this.addListener(new ClickListener() {
                public boolean touchDown(InputEvent event, float x,
                                         float y, int pointer, int button) {
                    System.out.println("Dragging general: " + general.getName());
                    if (battleStage != null) {
                        battleStage.selectUnit(bsp.general);
                    }
                    else if (allowSubpartyCollapse) {
                        if (expanded) {
                            clearExpand();
                        } else {
                            createExpand();
                            expanded = true;
                        }
                    } else {
                        switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
                    }
                    return true;
                }
                public void touchUp(InputEvent event, float x, float y,
                                    int pointer, int button) {
                    System.out.println("Releasing general: " + general.getName());
                }
            });
        }
        void createExpand() {
            expanded = true;
            for (final StrictArray<Soldier> type : soldierLists) {
                Table typeTable = new Table();
                float indent = 0;
                Label count = new Label(type.size + EXPAND, getStyle());
                TypeLabel name = new TypeLabel(type.first().getTypeName(), getStyle(), count, bsp);
                name.type = type;
                name.setColor(type.first().unitType.cultureType.colorLite);
                typeTable.add(name).left().padLeft(indent).expandX().padBottom(1*PanelUnit.NEG);

                count.setColor(type.first().unitType.cultureType.colorLite);
                typeTable.add(count).right().padBottom(1*PanelUnit.NEG);
                count.addListener(name.getListeners().first());
                expand.add(typeTable).left().expandX().fillX();
                expand.row();
//                expand.debug();
                float indentSub = DEFAULT_INDENT;

                expand.add(name.expand).expandX().left().padLeft(indent + indentSub).colspan(2);
                expand.row();
                expand.padBottom(-PanelUnit.NEG);

                if (hirePanel) {
                    name.clearExpand();
                    name.createExpand();
                    name.expanded = true;
                }
            }

            // Also add Shaman if necessary
            if (subparty.shaman != null) {
                final SoldierLabel shamanLabel = new SoldierLabel(subparty.shaman.unitType.name, getStyle(), subparty.shaman);
                expand.add(shamanLabel).left().expandX().padBottom(1*PanelUnit.NEG).colspan(2);
                shamanLabel.setColor(subparty.shaman.getCulture().colorLite);
                if (subparty.shaman == selected) {
                    shamanLabel.setColor(SELECTED_COLOR);
                }
                soldierLabels.add(shamanLabel);

                shamanLabel.addListener(new ClickListener() {
                    public boolean touchDown(InputEvent event, float x,
                                             float y, int pointer, int button) {
                        System.out.println("Dragging shaman: " + subparty.shaman.getName());
                        if (selectable) {
                            if (selected == subparty.shaman) {
                                // Deselect happens in touchup
                            } else
                                select(subparty.shaman);
                        } else {
                            if (bsp == null) {
                                switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
                            } else {
                                battleStage.selectUnit(bsp.shaman);
                            }
                        }
                        return true;
                    }
                    public void touchUp(InputEvent event, float x, float y,
                                        int pointer, int button) {
                        System.out.println("Releasing shaman: " + subparty.shaman.getName());
                        if (selectable) {
                            if (selected == subparty.shaman) {
                                deselect();
                            }
                        }
                    }
                });
                expand.row();
            }
        }
        void clearExpand() {
            expanded = false;
            expand.clear();
        }
    }

    public class TypeLabel extends Label {
        StrictArray<Soldier> type;
        Table expand;
        public boolean expanded = false;
        private BattleSubParty bsp;

        public TypeLabel(String name, LabelStyle ls, final Label count, BattleSubParty bsp) {
            super(name, ls);
            expand = new Table();
            this.bsp = bsp;
            this.addListener(new ClickListener() {
                public boolean touchDown(InputEvent event, float x,
                                         float y, int pointer, int button) {
                    return true;
                }
                public void touchUp(InputEvent event, float x, float y,
                                    int pointer, int button) {
                    if (preventSoldierExpand) return;
                    if (expanded) {
                        count.setText(type.size + EXPAND);
                        clearExpand();
                        expanded = false;
                    }
                    else {
                        count.setText(type.size + COLLAPSE);
                        createExpand();
                        expanded = true;
                    }
                }
            });
        }
        void createExpand() {
            for (final Soldier s : type) {
                SoldierLabel soldierName = new SoldierLabel(s.getName(), this.getStyle(), s);
                soldierName.setColor(SOLDIER_NAME_COLOR);
                if (s == selected) {
                    soldierName.setColor(SELECTED_COLOR);
                }
                expand.add(soldierName).left().padBottom(PanelUnit.NEG).expandX();

                soldierName.addListener(new ClickListener() {
                    public boolean touchDown(InputEvent event, float x,
                                             float y, int pointer, int button) {
                        System.out.println("Dragging: " + s.getName());
                        if (selectable) {
                            if (selected == s) {
                                // deselect happens in touch up
                            } else
                                select(s);
                        } else {
                            if (bsp == null) {
                                switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
                            } else {
                                System.out.println("Selecting unit");
                                battleStage.selectUnit(bsp.getUnit(((SoldierLabel) event.getListenerActor()).soldier));
                            }
                        }
                        return true;
                    }
                    public void touchUp(InputEvent event, float x, float y,
                                        int pointer, int button) {
                        System.out.println("Releasing: " + s.getName());
                        if (selectable) {
                            if (selected == s) {
                                deselect();
                            }
                        }
                    }
                });
                expand.row();

                soldierLabels.add(soldierName);
            }
        }
        void clearExpand() {
            expand.clear();
        }
    }

    private static void switchToPanel(Soldier s) {
        MapScreen.sidePanelReference.setActiveUnit(s);
    }

    public void updateTableWithTypesNew(final Subparty s, LabelStyle style, final BattleSubParty bsp) {
        // Add title of the party:
        if (s.getRank() == 0) {
            Label label = new Label(s.getPartyName(), ls);
            label.setAlignment(Align.center);
            label.setWrap(true);
            soldierTable.add(label).expandX().fillX();
            soldierTable.row();
        }

        final SubpartyLabel label = new SubpartyLabel(s, style, bsp);
        Table generalTable = new Table();
        generalTable.add(label).left().expandX();
//        soldierTable.add(label).left().expandX();
        String toAdd = label.expanded ? COLLAPSE : EXPAND;
        final Label generalCount = new Label(s.getHealthySize() + toAdd, style);
        generalTable.add(generalCount).right();
        soldierTable.add(generalTable).left().expandX().fillX();
        generalCount.setColor(s.getGeneral().unitType.cultureType.colorDark);
        generalCount.addListener(new ClickListener() {
            public boolean touchDown(InputEvent event, float x,
                                     float y, int pointer, int button) {
                return true;
            }
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                // Force table to be expanded the whole time.
                if (allowSubpartyCollapse) {
                    if (label.expanded) {
                        label.clearExpand();
                        generalCount.setText(s.getHealthySize() + EXPAND);
                        label.expanded = false;
                    } else {
                        label.createExpand();
                        label.expanded = true;
                        generalCount.setText(s.getHealthySize() + COLLAPSE);
                    }
                } else {
                    if (bsp == null) {
                        switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
                    } else {
                        battleStage.selectUnit(bsp.general);
                    }
                }
            }
        });
        soldierTable.row();

        if (bsp != null) {
            float indent = 10;
            Table statusTable = new Table();
            final Label stance = new Label(bsp.stance.toString(), style);
            statusTable.add(stance).left().expandX();
            stance.addListener(new ClickListener() {
                public boolean touchDown(InputEvent event, float x,
                                         float y, int pointer, int button) {
                    return true;
                }
                public void touchUp(InputEvent event, float x, float y,
                                    int pointer, int button) {
                    //Cycle through stances.
                    bsp.toggleStance();
                    stance.setText(bsp.stance.toString());
                }
            });
            LabelStyle colored = new LabelStyle(style);
            colored.fontColor = bsp.moraleColor;
            Label morale = new Label(bsp.getCurrentMoraleString(), colored);
            statusTable.add(morale).right().padLeft(indent);
            soldierTable.add(statusTable).left().expandX().padLeft(indent).fillX();
            soldierTable.row();
//            Label formation = new Label(bsp.formation.toString(), style);
//            soldierTable.add(formation).left().expandX().colspan(2).padLeft(indent);
//            soldierTable.row();
        }

        soldierTable.row();
        float indent = DEFAULT_INDENT;
        if (hirePanel) indent = 0;

        soldierTable.add(label.expand).expandX().left().padLeft(indent).colspan(2).fillX();
        soldierTable.row();

        if (!startAllCollapsed)
            label.createExpand();
    }


    // only used for panel hire, TODO deprecate.
	public void updateTableWithTypes(StrictArray<StrictArray<Soldier>> types, LabelStyle style) {
		//		table.debug();
		for (StrictArray<Soldier> type : types) {
            float indent = DEFAULT_INDENT;
            if (hirePanel) indent = 0;

            Label count = new Label(type.size + "", style);
            count.setColor(type.first().unitType.cultureType.colorLite);
            TypeLabel name = new TypeLabel(type.first().getTypeName(), style, count, null);
			name.type = type;
			name.setColor(type.first().unitType.cultureType.colorLite);
            soldierTable.add(name).left().padLeft(indent);
            soldierTable.add(count).right();
            soldierTable.row();

			float indentSub = DEFAULT_INDENT;

			soldierTable.add(name.expand).expandX().left().padLeft(indent + indentSub).colspan(2);
            soldierTable.row();
			name.expand.padBottom(-PanelUnit.NEG);

			if (hirePanel) {
			    name.clearExpand();
                name.createExpand();
                name.expanded = true;
            }

//			if (selected != null) {
//			    for (int i = 0; i < type.size; i++) {
//			        if (type.get(i) == selected) {
//                        name.createExpand();
//                        name.expanded = true;
//                    }
//			    }
//            }
		}
	}

//    public void updateTableWithSubparty(Subparty subparty, LabelStyle style) {
//        //		table.debug();
//        // TODO don't add General to this list (they're already listed above
//        // the other units)
//        SubpartyLabel name = new SubpartyLabel(subparty.general.getName(), style);
//        name.types = subparty.getTypeListHealthy();
//        name.setColor(subparty.getGeneral().unitType.cultureType.colorLite);
//        soldierTable.add(name).left();
//        Label count = new Label(subparty.getHealthySize() + "", style);
//        soldierTable.add(count).right();
//        soldierTable.row();
//
//        float indent = 15;
//
//        soldierTable.add(name.expand).expandX().left().padLeft(indent);
//        soldierTable.row();
//        name.expand.padBottom(-PanelUnit.NEG);
//
//        if (hirePanel) {
//            name.clearExpand();
//            name.createExpand();
//            name.expanded = true;
//        }
//
//    }


    public void select(Soldier soldier) {
        System.out.println("selecting: " + soldier.getName());
        this.selected = soldier;
        this.nextToSelect = getSoldierAfterSelectedOrBeforeIfNoneAfter();
        updateColors();
    }

    public void deselect() {
        this.selected = null;
        this.nextToSelect = null;
        updateColors();
    }

//	public void select(Soldier soldier, Label label) {
//	    System.out.println("selecting: " + soldier.getName());
//	    this.selected = soldier;
//	    this.nextToSelect = getSoldierAfterSelectedOrBeforeIfNoneAfter();
//	    label.setColor(SELECTED_COLOR);
////        update();
//    }

    // For selecting a soldier after one has been hired.
    private Soldier getSoldierAfterSelectedOrBeforeIfNoneAfter() {
        // Select next available soldier
        Soldier prevSoldier = null;
        boolean soldierJustFound = false;

        Soldier toSelect = null;

        for (final Subparty s : party.subparties) {
            StrictArray<StrictArray<Soldier>> lists = s.getConsolHealthy();
            for (int j = 0; j < lists.size; j++) {
                StrictArray<Soldier> soldiers = lists.get(j);
                for (int i = 0; i < soldiers.size; i++) {
                    // This gets the next soldier after the selected one has been found
                    if (soldierJustFound) {
                        toSelect = soldiers.get(i);
                        break;
                    }
                    if (soldiers.get(i) == selected) {
                        soldierJustFound = true;
                    }
                    else
                        prevSoldier = soldiers.get(i);
                }
                if (toSelect != null) break;
            }
        }

        if (toSelect != null) return toSelect;
        if (prevSoldier != null) return prevSoldier;
        return null;
    }

    // handle the fact that a selected soldier was just removed.
    public void notifySelectedSoldierRemoved() {
	    if (selected == null) {
	        throw new AssertionError();
        }

        if (nextToSelect != null) {
            select(nextToSelect);
        }
        else {
            selected = null;
        }
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
