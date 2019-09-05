package kyle.game.besiege.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import kyle.game.besiege.Assets;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.BattleStage;
import kyle.game.besiege.battle.BattleSquad;
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
	private static final String LOCATION_EMPTY_TEXT = "No troops garrisoned!";
    private static Color SOLDIER_NAME_COLOR = Color.LIGHT_GRAY;
	private static Color SELECTED_COLOR = Color.YELLOW;

	Table soldierTable;
    private ScrollPane soldierPane;

    private static final String EXPAND = " v";
    private static final String COLLAPSE = " ^";

    public Soldier selected;
    public Soldier nextToSelect; // everytime you select a soldier, figure out which one you should select next if the soldier is hired.

    private StrictArray<SoldierLabel> soldierLabels;

    // TODO make squad tables panels.
    private Panel parent;
    public boolean hirePanel;
    public boolean captivesPanel; // similar to hire panel, but slightly different.
    public boolean squadSoldierTable;
    public boolean selectable;

    // Flip-flop logic
    private boolean justSelected;

    protected final Party party;

    // These are present for post-battle
    private final StrictArray<StrictArray<Soldier>> consolidatedWounded;
    private final StrictArray<StrictArray<Soldier>> consolidatedKilled;
    private int woundedCount;
    private int killedCount;

    // This is present only for squadsoldiertables
    private final Squad squad;

	private final LabelStyle ls = new LabelStyle();
    private final LabelStyle lsG = new LabelStyle();
    private final LabelStyle lsBig = new LabelStyle();

    private final Label garrisonC;
	protected final Label noTroopsC;
	private final Label nullC;
	private final Label prisonersC;
	private final Label emptyC;

    private boolean allowSquadCollapse = true;
    private boolean preventSoldierExpand = false;

    private boolean startAllCollapsed;

    // This is useful for displaying the current status of battlesquads
    private BattleStage battleStage;

    public SoldierTable(Panel parent, Party party) {
        this(parent, party, false, null, null, null, null);
    }

    public SoldierTable(Party party, boolean startAllCollapsed, BattleStage battle) {
        this(null, party, startAllCollapsed, battle, null, null, null);
//        if (battle == null) throw new AssertionError();
    }

	public SoldierTable(Panel parent, Party party, boolean startAllCollapsed, BattleStage battle, StrictArray<StrictArray<Soldier>> wounded, StrictArray<StrictArray<Soldier>> killed, Squad squad) {
		ls.font = Assets.pixel16;
		lsG.font = Assets.pixel16;
		lsBig.font = Assets.pixel18;

		this.battleStage = battle;
		this.parent = parent;

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
        this.squad = squad;
        if (squad != null) allowSquadCollapse = false;

        this.startAllCollapsed = startAllCollapsed;

		garrisonC = new Label("Garrison", ls);
		noTroopsC = new Label(LOCATION_EMPTY_TEXT,ls);
        nullC = new Label("Garrison is null!",ls);
		prisonersC = new Label("Captives", ls);
		emptyC = new Label("", ls);
		
		soldierTable = new Table();
		soldierTable.defaults().padTop(NEG);
		soldierTable.top();
		soldierTable.setBackground(Assets.ninepatchBackgroundDarkGray);
		soldierPane = new ScrollPane(soldierTable);
		soldierPane.setScrollbarsOnTop(true);
		soldierPane.setFadeScrollBars(false);
//		soldierTable.debug();

		soldierLabels = new StrictArray<SoldierLabel>();
		
		this.add(soldierPane).top().width(SidePanel.WIDTH - PAD*2).expandY().fillY();
		this.selectable = true;
	}

	public void setAllowSquadCollapse(boolean allowSquadCollapse) {
	    this.allowSquadCollapse = allowSquadCollapse;
    }

    public void setLockSoldierExpand(boolean preventSoldierExpand) {
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
//		System.out.println("updating soldierTable");
		if (party == null) {
		    if (squad != null) {
		        updateForSquad(squad, ls, lsG);
            }
		    else if (consolidatedKilled != null || consolidatedWounded != null) {
                updateForPostBattle(consolidatedWounded, consolidatedKilled, ls);
            } else {
                noTroopsC.setAlignment(Align.center);
                soldierTable.add(nullC).colspan(2).center().width(SidePanel.WIDTH - PAD * 2);

                soldierTable.row();
            }
//            System.out.println("party is null");
        }
		else if ((!captivesPanel && party.getTotalSize() == 0) || (captivesPanel && party.getPrisoners().size == 0)) {
			noTroopsC.setAlignment(Align.center);
			soldierTable.add(noTroopsC).colspan(2).center().width(SidePanel.WIDTH - PAD*2);
			soldierTable.row();
//            System.out.println("using notroopsC");
        }
		else {
//            System.out.println("updating for party");
            updateWithParty(party, ls, lsG);
        }
	}

	private BattleSquad getBspForSquad(Squad s) {
        if (battleStage != null) {
            BattleSquad bsp = null;
            for (BattleSquad b : battleStage.getDefending().squads) {
                if (b.squad == s) {
                    bsp = b;
                    break;
                }
            }
            for (BattleSquad b : battleStage.getAttacking().squads) {
                if (b.squad == s) {
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

    private void updateForSquad(Squad squad, LabelStyle style, LabelStyle woundedStyle) {
        SoldierLabel general = new SoldierLabel(squad.getGeneral().getOfficialName(), style, squad.getGeneral());
        general.addListener(createSoldierClickListener(squad.getGeneral(), null));
        soldierTable.add(general).center().expandX();
        general.setColor(squad.getGeneral().unitType.cultureType.colorDark);

        soldierTable.row();
        updateTableWithTypes(squad.getConsolHealthy(), style);

//        Label killedLabel = new Label("Killed", woundedStyle);
//        Label killedCount = new Label(this.killedCount + "", woundedStyle);
//        soldierTable.add(killedLabel).left().padLeft(0);
//        soldierTable.add(killedCount).right();
        soldierTable.row();
        updateTableWithTypes(squad.getConsolWounded(), woundedStyle);
        System.out.println("updating for squad");
    }

	// Currently the vertical size is too big, maybe because height isn't properly set?
	private void updateWithParty(Party party, LabelStyle style, LabelStyle wounded) {
        // Special case for captive panel.
        if (captivesPanel) {
            updateTableWithTypes(party.getConsolPrisoners(), style);
            return;
        }

//		System.out.println("starting panelparty update: " + party.getName() + " with " + party.squads.size + " squads");
		for (final Squad s : party.squads) {
		    BattleSquad bsp = getBspForSquad(s);
//		    if (bsp == null) throw new AssertionError();

            final SoldierLabel general;
//            if (organizeByType) {
            if (allowSquadCollapse && !hirePanel) {
                updateTableWithTypesNew(s, style, bsp);
//                updateTableWithTypesNew(s.getGeneral(), s.getConsolWounded(), wounded);
            } else {

                // NOTE this stuff will only happen for panelhire
                if (s.getGeneral() != null) {
//				System.out.println(s.getGeneral().getName());
                    general = new SoldierLabel(s.getGeneral().getOfficialName(), style, s.getGeneral());
                    general.addListener(createSoldierClickListener(s.getGeneral(), bsp));
                    soldierTable.add(general).left().expandX();
                    general.setColor(s.getGeneral().unitType.cultureType.colorDark);

                    Label generalCount = new Label(s.getHealthySize() + "", style);
                    soldierTable.add(generalCount).right();
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

        // Add prisoners.
        addPrisoners(party);
	}

	private void addPrisoners(Party party) {
        if (party.getPrisoners() == null || party.getPrisoners().size == 0) return;

        prisonersC.setAlignment(Align.center);
        soldierTable.add(prisonersC).colspan(2).center().width(SidePanel.WIDTH - PAD * 2);
        soldierTable.row();

        updateTableWithTypes(party.getConsolPrisoners(), lsG);
    }

    // This is a label of a general that includes a table of all subtypes below it
    public class SquadLabel extends Label {
	    Squad squad;
	    StrictArray<StrictArray<Soldier>> soldierLists;
        Table expand;
        public boolean expanded = false;
        private BattleSquad bsp;

        public SquadLabel(Squad squad, LabelStyle ls, final BattleSquad bsp) {
            super("", ls);
            final General general = squad.getGeneral();
            if (general != null) {
                setText(general.getRank() + " " + general.getLastName());
            } else {
                // This happens when the general was wounded or killed recently.
                // TODO -- do we want to always have a general for a squad?
                // I think yes. Even if the general is wounded, the squad is still technically under the general.
                // If he dies, force the player to find a replacement or disband the squad after the battle.
                // For now, automatically assign.
                throw new AssertionError();
            }
            this.bsp = bsp;
            this.squad = squad;
            this.setColor(general.unitType.cultureType.colorDark);
            this.soldierLists = squad.getConsolHealthy();
            expand = new Table();
            this.addListener(new ClickListener() {
                public boolean touchDown(InputEvent event, float x,
                                         float y, int pointer, int button) {
                    System.out.println("Dragging general: " + general.getName());
                    parent.notifyDragStart(general);
                    if (battleStage != null) {
                        battleStage.selectUnit(bsp.general);
                    }
                    else if (allowSquadCollapse) {
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
                    parent.notifyDragRelease(general);
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

                if (hirePanel || captivesPanel || squadSoldierTable) {
                    name.clearExpand();
                    name.createExpand();
                    name.expanded = true;
                }
            }

            // Also add Shaman if necessary
            if (squad.shaman != null) {
                final SoldierLabel shamanLabel = new SoldierLabel(squad.shaman.unitType.name, getStyle(), squad.shaman);
                expand.add(shamanLabel).left().expandX().padBottom(1*PanelUnit.NEG).colspan(2);
                shamanLabel.setColor(squad.shaman.getCulture().colorLite);
                if (squad.shaman == selected) {
                    shamanLabel.setColor(SELECTED_COLOR);
                }
                soldierLabels.add(shamanLabel);

                shamanLabel.addListener(createSoldierClickListener(squad.shaman, bsp));
                expand.row();
            }
        }
        void clearExpand() {
            expanded = false;
            expand.clear();
        }
    }

    private ClickListener createSoldierClickListener(final Soldier soldier, /**Optional*/ final BattleSquad bsp) {
        if (soldier == null) throw new AssertionError();
        return new ClickListener() {
            public boolean touchDown(InputEvent event, float x,
                                     float y, int pointer, int button) {
                System.out.println("Dragging soldier: " + soldier.getName());
                parent.notifyDragStart(soldier);
                if (selectable) {
                    if (selected == soldier) {
                        // Deselect happens in touchup
                    } else
                        select(soldier);
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
                System.out.println("Releasing soldier: " + soldier.getName());
                parent.notifyDragRelease(soldier);
                if (selectable && !justSelected) {
                    if (selected == soldier) {
                        deselect();
                    }
                }
                justSelected = false;
            }
        };
    }

    public class TypeLabel extends Label {
        StrictArray<Soldier> type;
        Table expand;
        public boolean expanded = false;
        private BattleSquad bsp;
        private Label count;

        public TypeLabel(String name, LabelStyle ls, final Label count, BattleSquad bsp) {
            super(name, ls);
            expand = new Table();
            this.bsp = bsp;
            this.count = count;
            this.addListener(new ClickListener() {
                public boolean touchDown(InputEvent event, float x,
                                         float y, int pointer, int button) {
                    return true;
                }
                public void touchUp(InputEvent event, float x, float y,
                                    int pointer, int button) {
                    if (preventSoldierExpand) return;
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
            count.addListener(this.getListeners().first());
        }

        void createExpand() {
            for (final Soldier s : type) {
                SoldierLabel soldierName = new SoldierLabel(s.getName(), this.getStyle(), s);
                soldierName.setColor(SOLDIER_NAME_COLOR);
                if (s == selected) {
                    soldierName.setColor(SELECTED_COLOR);
                }
                expand.add(soldierName).left().padBottom(PanelUnit.NEG).expandX();

                soldierName.addListener(createSoldierClickListener(s, bsp));
                expand.row();

                soldierLabels.add(soldierName);
            }
            String toUse = type.size + COLLAPSE;
            if (preventSoldierExpand) toUse = type.size + "";
            count.setText(toUse);

        }
        void clearExpand() {
            expand.clear();
            String toUse = type.size + EXPAND;
            if (preventSoldierExpand) toUse = type.size + "";
            count.setText(type.size + toUse);
        }
    }

    private static void switchToPanel(Soldier s) {
        MapScreen.sidePanelReference.setActiveUnit(s);
    }

    /**
     *
     * @param s Squad to update
     * @param style Style to use for this table
     * @param bsp Optional, only present if this is in a battle
     */
    public void updateTableWithTypesNew(final Squad s, LabelStyle style, final BattleSquad bsp) {
        // Add title of the party:
        if (s.getRank() == 0) {
            Label label = new Label(s.getPartyName(), ls);
            // This selects the panel of the party. Shouldn't do anything if the party is currently selected. But useful for a battle or a location.
            label.addListener(new ClickListener() {
                public boolean touchDown(InputEvent event, float x,
                                         float y, int pointer, int button) {
                    return true;
                }
                public void touchUp(InputEvent event, float x, float y,
                                    int pointer, int button) {
                    if (s.party.army != null)
                        SidePanel.sidePanel.setActiveArmy(s.party.army);
                }
            });
            label.setAlignment(Align.center);
            label.setWrap(true);
            soldierTable.add(label).expandX().fillX();
            soldierTable.row();
        }

        final SquadLabel label = new SquadLabel(s, style, bsp);
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
                if (allowSquadCollapse) {
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
            final Label stance = new Label(bsp.getStanceString(), style);
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
        if (hirePanel || captivesPanel || squadSoldierTable) indent = 0;

        soldierTable.add(label.expand).expandX().left().padLeft(indent).colspan(2).fillX();
        soldierTable.row();

        if (!startAllCollapsed)
            label.createExpand();
    }


    // only used for panel hire, and special panels (post battle, squad table)
	public void updateTableWithTypes(StrictArray<StrictArray<Soldier>> types, LabelStyle style) {
		//		table.debug();
		for (StrictArray<Soldier> type : types) {
            float indent = DEFAULT_INDENT;
            if (hirePanel || captivesPanel || squadSoldierTable) indent = 0;

            Label count = new Label(type.size + "", style);
            count.setColor(type.first().unitType.cultureType.colorLite);
            TypeLabel name = new TypeLabel(type.first().getTypeName(), style, count, null);
			name.type = type;
			name.setColor(type.first().unitType.cultureType.colorLite);
            soldierTable.add(name).left().padLeft(indent);
            soldierTable.add(count).right();
            soldierTable.row();

			float indentSub = DEFAULT_INDENT;
			if (hirePanel || captivesPanel || squadSoldierTable) indentSub = DEFAULT_INDENT/2;

			soldierTable.add(name.expand).expandX().left().padLeft(indent + indentSub).colspan(2);
            soldierTable.row();
			name.expand.padBottom(-PanelUnit.NEG);

			if (hirePanel || captivesPanel || squadSoldierTable) {
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

//    public void updateTableWithSquad(Squad squad, LabelStyle style) {
//        //		table.debug();
//        // TODO don't add General to this list (they're already listed above
//        // the other units)
//        SquadLabel name = new SquadLabel(squad.general.getName(), style);
//        name.types = squad.getTypeListHealthy();
//        name.setColor(squad.getGeneral().unitType.cultureType.colorLite);
//        soldierTable.add(name).left();
//        Label count = new Label(squad.getHealthySize() + "", style);
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
        justSelected = true;
        if (parent instanceof PanelParty) {
            ((PanelParty) parent).notifySelect(soldier);
        }
    }

    public void deselect() {
        this.selected = null;
        this.nextToSelect = null;
        System.out.println("Deselecting");
        updateColors();
        if (parent instanceof PanelParty) {
            ((PanelParty) parent).notifyDeselect();
        }
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
        if (captivesPanel) {
            return selectNextInList(party.getPrisoners());
        } else if (squad != null) {
            StrictArray<StrictArray<Soldier>> lists = squad.getConsolHealthy();
            Soldier next = selectNextInLists(lists);
            if (next == null) {
                next = selectNextInLists(squad.getConsolWounded());
            }
            return next;
        } else {
            for (final Squad s : party.squads) {
                Soldier toReturn = selectNextInLists(s.getConsolHealthy());
                if (toReturn != null) return toReturn;
            }
            return null;
        }
    }

    private Soldier selectNextInList(StrictArray<Soldier> list) {
        boolean soldierJustFound = false;
        Soldier toSelect = null;
        Soldier prevSoldier = null;
        for (int i = 0; i < party.getPrisoners().size; i++) {
            // This gets the next soldier after the selected one has been
            // found
            if (soldierJustFound) {
                toSelect = party.getPrisoners().get(i);
                break;
            }
            if (party.getPrisoners().get(i) == selected) {
                soldierJustFound = true;
            } else
                prevSoldier = party.getPrisoners().get(i);
        }
        if (toSelect != null) return toSelect;
        if (prevSoldier != null) return prevSoldier;
        return null;
    }

    private Soldier selectNextInLists(StrictArray<StrictArray<Soldier>> lists) {
        boolean soldierJustFound = false;
        Soldier toSelect = null;
        Soldier prevSoldier = null;
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
                } else
                    prevSoldier = soldiers.get(i);
            }
            if (toSelect != null) break;
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
	        deselect();
        }
        update();
    }

//	public void updateWithPrisoners(Party party) {
//        if (party != null && party.getPrisoners().size > 0)
//            prisonersC.setText("Captives");
//        else prisonersC.setText("");
//        prisonersC.setAlignment(0, 0);
//        soldierTable.add(prisonersC).colspan(2).width(SidePanel.WIDTH - PAD * 2).expandX().fillX().padTop(0);
//        soldierTable.row();
//        updateTableWithTypes(party.getConsolPrisoners(), ls);
//    }
}
