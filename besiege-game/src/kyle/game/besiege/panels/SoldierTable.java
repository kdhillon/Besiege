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
import kyle.game.besiege.party.*;

/**
 * Represents a UI element that contains a list of soldiers. Usually contains a playerPartyPanel, but may
 * contain standalone units, for hire or upgrading. Basic display is a list of soldiers organized by unit type.
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
	private static final String LOCATION_EMPTY_TEXT = "No troops garrisoned!";
    private static Color SOLDIER_NAME_COLOR = Color.LIGHT_GRAY;
	private static Color SELECTED_COLOR = Color.YELLOW;

	private Table soldierTable;
    private ScrollPane soldierPane;

    public Soldier selected;
    public Soldier nextToSelect; // everytime you select a soldier, figure out which one you should select next if the soldier is hired.

    private StrictArray<SoldierLabel> soldierLabels;

    public boolean hirePanel;
    public boolean selectable;

    protected final Party party;

	private final LabelStyle ls = new LabelStyle();
    private final LabelStyle lsG = new LabelStyle();
	private final Label garrisonC;
	protected final Label noTroopsC;
	private final Label nullC;
	private final Label prisonersC;
	private final Label emptyC;

    private boolean allowSubpartyCollapse = true;
    private boolean preventSoldierExpand = false;

    private boolean startAllCollapsed;

    public SoldierTable(Party party) {
        this(party, false);
    }

	public SoldierTable(Party party, boolean startAllCollapsed) {
		ls.font = Assets.pixel16;
		lsG.font = Assets.pixel16;

		this.party = party;
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
		this.selectable = false;
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
		System.out.println("starting panelparty update: " + party.getName() + " with " + party.subparties.size + " subparties");
		for (final Subparty s : party.subparties) {
            SoldierLabel general;
//            if (organizeByType) {
            if (allowSubpartyCollapse && !hirePanel) {
                updateTableWithTypesNew(s, style);
//                updateTableWithTypesNew(s.getGeneral(), s.getConsolWounded(), wounded);
            } else {
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
                                if (selected == s.general) {
                                    deselect();
                                } else {
                                    select(s.general);
                                }
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

                    soldierLabels.add(general);
                } else {
//				general = new SoldierLabel("No general!", style, s.general);
                }


                updateTableWithTypes(s.getConsolHealthy(), style);
                updateTableWithTypes(s.getConsolWounded(), wounded);
            }
//            else {
//                updateTableWithSubparty(s, style);
//            }
        }
	}

//	public class SubpartyLabel extends Label {
//	    LinkedHashMap<UnitType, Integer> types;
//        Table expand;
//        public boolean expanded = false;
//
//        public SubpartyLabel(String name, LabelStyle ls) {
//            super(name, ls);
//            expand = new Table();
//            this.addListener(new ClickListener() {
//                public boolean touchDown(InputEvent event, float x,
//                                         float y, int pointer, int button) {
//                    return true;
//                }
//
//                public void touchUp(InputEvent event, float x, float y,
//                                    int pointer, int button) {
//                    // Force table to be expanded the whole time.
//                    if (hirePanel) return;
//                    if (expanded) {
//                        clearExpand();
//                        expanded = false;
//                    } else {
//                        createExpand();
//                        expanded = true;
//                    }
//                }
//            });
//        }
//        public void createExpand() {
//            for (final Map.Entry<UnitType, Integer> entry : types.entrySet()) {
//                Label typeName = new Label((entry.getKey()).name, this.getStyle());
//                typeName.setColor(Color.GRAY);
//                expand.add(typeName).left().padBottom(PanelUnit.NEG).expandX();
//                Label typeCount = new Label((entry.getValue()) + "", this.getStyle());
//                typeCount.setColor(Color.GRAY);
//                expand.add(typeCount).left().padBottom(PanelUnit.NEG).expandX();
//                expand.row();
//            }
//        }
//        public void clearExpand() {
//            expand.clear();
//        }
//    }

    // This is a label of a general that includes a table of all subtypes below it
    public class SubpartyLabel extends Label {
	    Subparty subparty;
	    StrictArray<StrictArray<Soldier>> soldierLists;
        Table expand;
        public boolean expanded = false;

        public SubpartyLabel(Subparty subparty, LabelStyle ls) {
            super(subparty.general.getRank() + " " + subparty.general.getLastName(), ls);
            General general = subparty.getGeneral();
            StrictArray<StrictArray<Soldier>> types = subparty.getConsolHealthy();

            this.subparty = subparty;
            this.setColor(general.unitType.cultureType.colorDark);
            this.soldierLists = subparty.getConsolHealthy();
            expand = new Table();
            this.addListener(new ClickListener() {
                public boolean touchDown(InputEvent event, float x,
                                         float y, int pointer, int button) {
                    return true;
                }
                public void touchUp(InputEvent event, float x, float y,
                                    int pointer, int button) {
                    // Force table to be expanded the whole time.
                    if (allowSubpartyCollapse) {
                        if (expanded) {
                            clearExpand();
                        } else {
                            createExpand();
                            expanded = true;
                        }
                    } else {
                        switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
                    }
                }
            });
        }
        void createExpand() {
            expanded = true;
            for (final StrictArray<Soldier> type : soldierLists) {
                float indent = 0;
                TypeLabel name = new TypeLabel(type.first().getTypeName(), getStyle());
                name.type = type;
                name.setColor(type.first().unitType.cultureType.colorLite);
                expand.add(name).left().padLeft(indent).expandX().padBottom(1*PanelUnit.NEG);
                Label count = new Label(type.size + "", getStyle());
                count.setColor(type.first().unitType.cultureType.colorLite);
                expand.add(count).right().expandX().padBottom(1*PanelUnit.NEG);
                expand.row();
//                expand.debug();
                float indentSub = 15;

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
                SoldierLabel shamanLabel = new SoldierLabel(subparty.shaman.unitType.name, getStyle(), subparty.shaman);
                expand.add(shamanLabel).left().expandX().padBottom(1*PanelUnit.NEG).colspan(2);
                shamanLabel.setColor(SOLDIER_NAME_COLOR);
                if (subparty.shaman == selected) {
                    shamanLabel.setColor(SELECTED_COLOR);
                }
                soldierLabels.add(shamanLabel);


                shamanLabel.addListener(new ClickListener() {
                    public boolean touchDown(InputEvent event, float x,
                                             float y, int pointer, int button) {
                        return true;
                    }
                    public void touchUp(InputEvent event, float x, float y,
                                        int pointer, int button) {
                        if (selectable) {
                            if (selected == subparty.shaman)
                                deselect();
                            else
                                select(subparty.shaman);
                        } else {
                            switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
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
                    if (preventSoldierExpand) return;

                    // Force table to be expanded the whole time.
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
                        return true;
                    }
                    public void touchUp(InputEvent event, float x, float y,
                                        int pointer, int button) {
                        if (selectable) {
                            if (selected == s)
                                deselect();
                            else
                                select(s);
                        } else {
                            switchToPanel(((SoldierLabel) event.getListenerActor()).soldier);
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

    public void updateTableWithTypesNew(Subparty s, LabelStyle style) {
        SubpartyLabel label = new SubpartyLabel(s, style);
        soldierTable.add(label).left().expandX();
        Label generalCount = new Label(s.getHealthySize() + "", style);
        soldierTable.add(generalCount).right();
        generalCount.setColor(s.general.unitType.cultureType.colorDark);
        soldierTable.row();

        soldierTable.row();
        int indent = 15;
        if (hirePanel) indent = 0;

        soldierTable.add(label.expand).expandX().left().padLeft(indent).colspan(2).fillX();
        soldierTable.row();

        if (!startAllCollapsed)
            label.createExpand();
    }

	public void updateTableWithTypes(StrictArray<StrictArray<Soldier>> types, LabelStyle style) {
		//		table.debug();
        // TODO don't add General to this list (they're already listed above the other units)
		for (StrictArray<Soldier> type : types) {
            float indent = 15;
            if (hirePanel) indent = 0;

            TypeLabel name = new TypeLabel(type.first().getTypeName(), style);
			name.type = type;
			name.setColor(type.first().unitType.cultureType.colorLite);
            soldierTable.add(name).left().padLeft(indent).expandX();
			Label count = new Label(type.size + "", style);
            count.setColor(type.first().unitType.cultureType.colorLite);
            soldierTable.add(count).right();
            soldierTable.row();

			float indentSub = 15;

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
