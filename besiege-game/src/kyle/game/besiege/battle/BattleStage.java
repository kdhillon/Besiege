package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.*;

/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or
 * distribute.
 ******************************************************************************/
// stage for battles, contains all information regarding battle.

import kyle.game.besiege.army.Army;
import kyle.game.besiege.battle.Unit.Orientation;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.PanelBattle2;
import kyle.game.besiege.panels.PanelPostBattle;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Subparty;
import kyle.game.besiege.voronoi.Biomes;
import kyle.game.besiege.voronoi.VoronoiGraph;

import static kyle.game.besiege.Kingdom.RAIN_FLOAT;

public class BattleStage extends Group implements Battle {
    public Biomes biome;
    //	public OldBattle battle;
    private PanelBattle2 pb;
    private PanelPostBattle postBattle;

    private static boolean FORCE_RAIN = false;
    public static final double RETREAT_THRESHOLD = 0.3; // if balance less
    // than this, army will retreat (btw 0 and 1, but obviously below 0.5)
    public static final int DEPRECATED_THRESHOLD = 2; // this field is now in
    // victory manager

    private static final float SPEED = 0.01f;

    //	public float scale = 1f;
    public float MIN_SIZE = 40;
    public float SIZE_FACTOR = .3f; // how much does the size of the parties
    public Color biomeColor;
    public Color currentColor;
    public float currentDarkness;
    public float targetDarkness;
    public boolean raining;

    // affect the size of battlefield?

    public static boolean drawCrests = true;

    private final float MOUSE_DISTANCE = 15; // distance destination must be

    private final double CHARGE_THRESHOLD = .5;

    public static int SIDE_PAD = 5;
    public static int BOTTOM_PAD = 5;

    // TODO this should scale with party size.
    public static int PLACE_HEIGHT = 20;

    public static double RETREAT_TIME_BASE = 10; // have to wait 5 secs
    // before can retreat
    static final float RAIN_SLOW = .8f;
    static final float SNOW_SLOW = .7f;

    public int MIN_PLACE_X; // This is modified by Battlemap (if there's
    // water on the left side, for example)
    public int MAX_PLACE_X;

    public int MIN_PLACE_Y_1;
    public int MAX_PLACE_Y_1;

    public int MIN_PLACE_Y_2;
    public int MAX_PLACE_Y_2;

    public BPoint placementCenter1;
    public BPoint placementCenter2;

    // if true, defenders cannot change their start location, and attackers
    // can place themselves anywhere in either placement area.
    boolean ambush = true;

    // from mouse to register

    public BattleParty allies;
    public BattleParty enemies;

    // These are for handling victories
    public StrictArray<Party> retreatedAttackers = null;
    public StrictArray<Party> retreatedDefenders = null;

    public Array<SiegeUnit> siegeUnitsArray;

    public boolean placementPhase;

    BPoint centerOffset;
    private BPoint placementPoint;
    private BPoint originalPoint;

    public boolean siegeOrRaid;
    private boolean hasWall;
    private boolean village;
    private boolean ruins;

    public boolean closed[][]; // open or closed?
    public double slow[][]; // 0 is normal speed, 1 is very slow.
    public Unit[][] units; // units on map
    public SiegeUnit[][] siegeUnits;
    public Array<Unit> retreated; // units that have retreated
    public float heights[][]; // ground floor levels

    // two sizes: one for battle map, one for map to draw
    // this is battle map, map to draw is twice as big
    public int size_x;
    public int size_y;

    BPoint prevMouse;

    private MapScreen mapScreen;
    private Kingdom kingdom;
    //	private Party player;
    public BattleMap battlemap;

    private boolean snowing;

    public boolean alliesDefending;

    //	public Stance allies.stance;
    //	public Stance enemies.stance;

    private boolean mouseOver; // is mouse over Battle screen?
    //	private boolean paused;
    public boolean isOver; // is the battle over?
    public boolean didAtkWin;
    public double retreatTimerPlayer;
    public double retreatTimerEnemy;

    public int unit_width = 16;
    public int unit_height = 16;

    public Unit selectedUnit;
    public Unit currentPanel;

    private boolean leftClicked;
    private boolean rightClicked;
    private Point mouse;

    private VictoryManager victoryManager;

    public boolean dragging;

    private Label victoryText;

    //	public Formation playerFormationChoice;
    //	public Formation enemyFormationChoice;
//	public Formation currentFormation; // I need this for some reason for
// moving units.
//	private Array<Formation> availableFormations;

//	private int currentFormationWidth;
//	private int currentFormationHeight;

    // take in battle object containing arrays of armies and stuff
    public BattleStage(MapScreen mapScreen, Array<Party> allyArray,
                       Array<Party> enemyArray, boolean alliesDefending, Siege siege) {
        this.mapScreen = mapScreen;

        this.allies = new BattleParty(this, 0);
        this.allies.player = true;
        this.enemies = new BattleParty(this, 1);
        this.enemies.player = false;

        if (allyArray != null) {
            for (Party p : allyArray)
                this.allies.addParty(p);
        }
        if (enemyArray != null) {
            for (Party p : enemyArray)
                this.enemies.addParty(p);
        }

        this.kingdom = mapScreen.getKingdom();

        this.victoryManager = new VictoryManager(kingdom, this, siege,
                getBalanceDefenders());
        this.victoryManager.addInitTroopCount(getTotalBattleSize());

        BottomPanel.log("Starting battle, probability of victory: " + (int)
                (100 * getBalanceAllies()) + "%", "white");

        this.alliesDefending = alliesDefending;

        //		this.isPlayer()Defending = false;

        boolean forceSiege = false;

        if (siege != null || forceSiege) {
            //			siegeDefense = alliesDefending;
            //			siegeAttack = !siegeDefense;
            //			//siegeAttack = true;
            siegeOrRaid = true;
            System.out.println("creating siege or raid battle");

            // TODO add different type of wall according to the type of
            // location we're at
            if (siege.location.isCastle() || siege.location.isCity()) {
                hasWall = true;
            } else if (siege.location.isRuin()) {
                ruins = true;
            } else if (siege.location.isVillage()) {
                village = true;
            }
        }

        this.biome = allies.first().army.getContaining().biome;

        this.currentColor = VoronoiGraph.getColor(allies.first().army
                .getContaining());
        biomeColor = currentColor;
        this.targetDarkness = kingdom.currentDarkness;
        currentDarkness = kingdom.currentDarkness;

        init();

        allies.updatePolygon();

//		allies.first().army.setBattle(this.battle);
//		enemies.first().army.setBattle(this.battle);

//		for (Party p : enemies.getPartiesCopy()) {
//			if (p != enemies.first()) {
//				if (alliesDefending) {
//					this.battle.addToAttackers(p.army);
//				} else {
//					this.battle.addToDefenders(p.army);
//				}
//			}
////			p.army.setBattle(this.battle);
//		}
//		for (Party p : allies.getPartiesCopy()) {
//			if (p != allies.first()) {
//				if (!alliesDefending) {
//					this.battle.addToAttackers(p.army);
//				} else {
//					this.battle.addToDefenders(p.army);
//				}			}
////			p.army.setBattle(this.battle);
//		}

//		if (battle.getSiegeLocation() == null) this.battle.setSiegeLocation
// (siegeOf);
//		else System.out.println("YES SIEGE");

        addUnits();
    }

    // constructor for simulations
    public BattleStage(MapScreen mapScreen, PartyType p1, PartyType p2, int
            partiesEach) {
        this.mapScreen = mapScreen;

        Party allyParty1 = p1.generate();
        allyParty1.player = true;
        Party enemyParty1 = p2.generate();
        allyParty1.setName("Your Party");
        enemyParty1.setName("Evil Party");

        this.allies = new BattleParty(this, 0);
        this.allies.player = true;
        this.enemies = new BattleParty(this, 1);
        this.enemies.player = false;

        alliesDefending = true;

        this.allies.addParty(allyParty1);
        this.enemies.addParty(enemyParty1);

        for (int i = 1; i < partiesEach; i++) {
            Party allyParty2 = p1.generate();
            Party enemyParty2 = p2.generate();
            allyParty2.setName("Friendly_" + (i + 1));
            enemyParty2.setName("Evil_" + (i + 1));
            this.allies.addParty(allyParty2);
            this.enemies.addParty(enemyParty2);
        }

        this.victoryManager = new VictoryManager(kingdom, this, null,
                getBalanceDefenders());
        this.victoryManager.addInitTroopCount(getTotalBattleSize());

        BottomPanel.log("Starting battle, probability of victory: " + (int)
                (100 * getBalanceAllies()) + "%", "white");

        // for testing
        this.siegeOrRaid = true;
        this.hasWall = false;

//		// must modify battle so it can support a null kingdom
//		// attacker, defender
//		if (alliesDefending)
//			this.battle = new OldBattle(null, enemyParty1, allyParty1);
//		else 
//			this.battle = new OldBattle(null, allyParty1, enemyParty1);

        int rand = (int) (Math.random() * Biomes.values().length);
        this.biome = Biomes.values()[rand];
        System.out.println("biome: " + this.biome.toString());

        this.currentColor = new Color(Color.WHITE);
        currentDarkness = Kingdom.NIGHT_FLOAT;
        biomeColor = currentColor;
        biomeColor = new Color(1, 0.9f, 0.7f, 1); // orange

        this.targetDarkness = 1;

        boolean FORCE_RUIN = true;
        if (Math.random() < 0.3f || FORCE_RUIN) {
            ruins = true;
        }

        init();

        this.enemies.player = false;

        addUnits();
    }

    public void setSnowingOrRaining() {
        System.out.println("setting snow or rain");
        if (this.biome == Biomes.SNOW && Math.random() < .75) {
            startSnow();
        } else if (getMapScreen().getKingdom() != null) {
            if (getMapScreen().getKingdom().raining) {
                startRain();
            }
        } else {
            if (Math.random() < 0.2f || FORCE_RAIN) {
                startRain();
            }
        }
    }

    boolean isRaining() {
        return raining;
    }

    boolean isSnowing() {
        return snowing;
    }

    public void init() {
        mouse = new Point(0, 0);

        //		this.isPlayer() = player;
        //		this.enemy = enemy

        if (alliesDefending) {
            allies.setStance(Stance.DEFENSIVE);
            enemies.setStance(Stance.AGGRESSIVE);
        } else {
            allies.setStance(Stance.AGGRESSIVE);
            if (Math.random() < .5)
                enemies.setStance(Stance.DEFENSIVE);
            else enemies.setStance(Stance.AGGRESSIVE);
            if (siegeOrRaid) {
                enemies.setStance(Stance.DEFENSIVE);
            }
        }

        int size = allies.getHealthySize() + enemies.getHealthySize();
        size *= SIZE_FACTOR;
        size += MIN_SIZE;
        this.size_x = size;
        this.size_y = size; // square for now

        // round to nearest number divisible by 8, for drawing purposes
        this.size_x += (BattleMap.BLOCK_SIZE - this.size_x % BattleMap
                .BLOCK_SIZE);
        this.size_y += (BattleMap.BLOCK_SIZE - this.size_y % BattleMap
                .BLOCK_SIZE);

        closed = new boolean[size_y][size_x];
        units = new Unit[size_y][size_x];
        siegeUnits = new SiegeUnit[size_y][size_x];
        siegeUnitsArray = new Array<SiegeUnit>();
        retreated = new Array<Unit>();
        slow = new double[size_y][size_x];
        heights = new float[size_y][size_x];

        // try this
        pb = new PanelBattle2(mapScreen.getSidePanel(), this);

        setSnowingOrRaining();

        this.battlemap = new BattleMap(this);
        this.addActor(battlemap);

        //		if (availableFormations.size == 0) System.out.println("no
        // formations available");

        // set up default formations
        if (alliesDefending) {
            enemies.setGlobalFormation(Formation.DEFENSIVE_LINE);
            allies.setGlobalFormation(Formation.DEFENSIVE_LINE);
        } else {
            // Should randomize this
            enemies.setGlobalFormation(enemies.subparties.first()
                    .availableFormations.random());
//			enemies.setGlobalFormation(Formation.DEFENSIVE_LINE);
//			allies.setGlobalFormation(Formation.SQUARE);
        }

        if (siegeOrRaid && alliesDefending)
            allies.setGlobalFormation(Formation.WALL_LINE);
        else if (siegeOrRaid && !alliesDefending)
            enemies.setGlobalFormation(Formation.WALL_LINE);


        this.placementPhase = true;

        //		this.paused = true;

        MIN_PLACE_X = SIDE_PAD;
        MAX_PLACE_X = size_x - SIDE_PAD;

        MIN_PLACE_Y_1 = BOTTOM_PAD;
        MAX_PLACE_Y_1 = PLACE_HEIGHT + BOTTOM_PAD;

        MIN_PLACE_Y_2 = size_y - this.MAX_PLACE_Y_1;
        MAX_PLACE_Y_2 = size_y - this.MIN_PLACE_Y_1;

       updatePlacementPoints();

        this.retreatTimerPlayer = RETREAT_TIME_BASE;// / allies.first()
        // .getAvgSpd() * 2;
        this.retreatTimerEnemy = RETREAT_TIME_BASE;// / enemies.first()
        // .getAvgSpd() * 2;

    }

    void updatePlacementPoints() {
        if (battlemap.getMaxX() < MAX_PLACE_X) {
            MAX_PLACE_X = battlemap.getMaxX();
            System.out.println("updating max x to: " + MAX_PLACE_X);
        }
        if (battlemap.getMinX() > MIN_PLACE_X) {
            MIN_PLACE_X = battlemap.getMinX();
            System.out.println("updating min x to: " + MIN_PLACE_X);
        }

        placementCenter1 = new BPoint((int) ((MAX_PLACE_X - MIN_PLACE_X) * 0.5f + MIN_PLACE_X), (int) ((MAX_PLACE_Y_1 - MIN_PLACE_Y_1) * 0.5f) + MIN_PLACE_Y_1);
        placementCenter2 = new BPoint((int) ((MAX_PLACE_X - MIN_PLACE_X) * 0.5f + MIN_PLACE_X), (int) ((MAX_PLACE_Y_2 - MIN_PLACE_Y_2) * 0.5f) + MIN_PLACE_Y_2);
        System.out.println("updating placement center 1 to: " + placementCenter1.toString());

        // set up orignal base points
        originalPoint = new BPoint(placementCenter1.pos_x, BOTTOM_PAD + PLACE_HEIGHT / 2);
        placementPoint = originalPoint;
    }

    public void centerCamera() {
        // translate to center of screen?
        mapScreen.battleCamera.translate((this.size_x) * this.unit_width / 2
                - mapScreen.getCamera().position.x, (this.size_y * .4f) * this
                .unit_height - mapScreen.getCamera().position.y);
        //		mapScreen.getCamera().translate(6, 0);

        //mapScreen.getCamera().translate(new Vector2((this.size_x)*this
        // .unit_width/2, (this.size_y)*this.unit_height/2));
    }

    // This is sort of being used right now
    // If this is a siegeOrRaid or a raid, don't allow defenders to retreat.
    // Note that village raids count as a siegeOrRaid.
//	public boolean canDefendersRetreat() {
//	    return !siegeOrRaid;
//    }

    private double getBalanceAllies() {
        double balanceAllies = allies.getLevelSum();
        double balanceEnemies = enemies.getLevelSum();

        double total = balanceAllies + balanceEnemies;
        balanceAllies = balanceAllies / total; // balanceA + balanceD = 1

        return balanceAllies;
    }

    @Override
    public double getBalanceDefenders() {
        double balanceAllies = getBalanceAllies();
        double balanceEnemies = 1 - balanceAllies;

        if (alliesDefending) {
            return balanceAllies;
        } else {
            return balanceEnemies;
        }
    }

    public void centerCameraOnPlayer() {
        centerCameraOnPoint(this.placementPoint);
    }

    public void centerCameraOnPoint(BPoint point) {
        // translate to center of screen?
        mapScreen.battleCamera.translate(this.unit_width * point.pos_x -
                mapScreen.getCamera().position.x, this.unit_height * point
                .pos_y - mapScreen.getCamera().position.y);
        //		mapScreen.getCamera().translate(6, 0);

        //mapScreen.getCamera().translate(new Vector2((this.size_x)*this
        // .unit_width/2, (this.size_y)*this.unit_height/2));
    }

    public void rain() {
        //		System.out.println("raining");
//		this.currentDarkness = Kingdom.RAIN_FLOAT;
        this.targetDarkness = RAIN_FLOAT;
        if (Math.random() < Kingdom.THUNDER_CHANCE) thunder();
    }

    public void startSnow() {
        if (snowing || raining) throw new AssertionError();
        snowing = true;

        // TODO add snow sound effect
    }

    public void startRain() {
        if (raining || snowing) throw new AssertionError();

        raining = true;
        SoundPlayer.startRain();
    }

    public void stopRain() {
        if (raining == false) return;
        raining = false;
        SoundPlayer.stopRain();
    }

    private void thunder() {
        //		this.currentDarkness = (float)((Math.random()/2+.5)*this
        // .LIGHTNING_FLOAT);
        this.currentDarkness = Kingdom.LIGHTNING_FLOAT;
        SoundPlayer.playThunder();
    }

    public void updateColor(SpriteBatch batch) {
        //		System.out.println("target darkness: " + this.targetDarkness);
        if (this.currentDarkness != this.targetDarkness) adjustDarkness();

        // This is a cool effect, but can be a bit obnoxious if it's a strong
        // color.
        // need to decide whether to add biome color to this.
//		this.currentColor.r = this.currentDarkness * biomeColor.r;
//		this.currentColor.g = this.currentDarkness * biomeColor.g;
//		this.currentColor.b = this.currentDarkness * biomeColor.b;

        this.currentColor.r = this.currentDarkness;
        this.currentColor.g = this.currentDarkness;
        this.currentColor.b = this.currentDarkness;
        this.currentColor.a = 1;

//		System.out.println(currentColor.r + " " + currentColor.g + " " +
// currentColor.b + " " + currentColor.a);

        batch.setColor(this.currentColor);
    }

    private void adjustDarkness() {
        if (this.raining) {
            if (this.targetDarkness - this.currentDarkness > Kingdom
                    .LIGHT_ADJUST_SPEED)
                this.currentDarkness += Kingdom.LIGHT_ADJUST_SPEED / 2;
            else if (this.currentDarkness - this.targetDarkness > Kingdom
                    .LIGHT_ADJUST_SPEED)
                this.currentDarkness -= Kingdom.LIGHT_ADJUST_SPEED / 2;
        } else {
            if (this.targetDarkness - this.currentDarkness > Kingdom
                    .LIGHT_ADJUST_SPEED)
                this.currentDarkness += Kingdom.LIGHT_ADJUST_SPEED;
            else if (this.currentDarkness - this.targetDarkness > Kingdom
                    .LIGHT_ADJUST_SPEED)
                this.currentDarkness -= Kingdom.LIGHT_ADJUST_SPEED;
        }
    }

    // for now, put them randomly on the field
    public void addUnits() {
        addAllSubparties(allies.subparties);
        addAllSubparties(enemies.subparties);

        if (siegeOrRaid) {
            if (alliesDefending) {
//				addSiegeUnits(enemies);
            } else {
//				addSiegeUnits(allies);
            }
        }
    }

    // Start with a few hard coded values
    // for example, deploy in rows of 3.
    // Place the root (generals bodyguard) in the back middle
    public void addAllSubparties(StrictArray<BattleSubParty> bspList) {
        // These keep track of how far left/right to place units.
        // They correspond to the outer edges of the bsps that have been
        // placed so far
        int spacesToLeftOfCenter = 0;
        int spacesToRightOfCenter = 0;

        for (int i = 0; i < bspList.size; i++) {
            BattleSubParty bsp = bspList.get(i);
            Formation formationType = bsp.formation;

            Soldier.SoldierType[][] formation = formationType.getFormation(bsp);
            if (!bsp.isPlayer()) formation = flipVertical(formation);

            int region_width = formation[0].length;
            int region_height = formation.length;

            int BSP_PER_ROW = 3;
            // Assume the following for now:
            //   9 7 6 8 10
            //   4 2 1 3 5
            // So start in bottom middle
            int horizontal_position = i % BSP_PER_ROW;
            int vertical_position = i / BSP_PER_ROW;    // 0 is bottom, 1 is
            // first row, etc.

            // Let's put everything 20 places apart for simplicity (max 20
            // units in subparty)
            int FIXED_SPACING_X = 20;

            // We need to put everything above a certain distance from the
            // bottom (to account for bsp height)
            int FIXED_SPACING_Y = 6;

            // Now place this where it belongs:
            int base_x;
            int base_y;

            if (bsp.currentPosX != 0 && bsp.currentPosY != 0) {
                base_x = bsp.currentPosX;
                base_y = bsp.currentPosY;
            } else { // not already on field
                FIXED_SPACING_X = region_width + 1;

                System.out.println("using placement center: " + placementCenter1.pos_x);
                if (horizontal_position == 0) {
                    spacesToRightOfCenter = FIXED_SPACING_X;
                    spacesToLeftOfCenter = 0;
                    base_x = placementCenter1.pos_x;
                }
                // If even, subtract width so this party is far enough away
                else if (horizontal_position % 2 == 0) {
                    spacesToLeftOfCenter += FIXED_SPACING_X;
                    base_x = placementCenter1.pos_x - spacesToLeftOfCenter;
                }
                // If odd, add this width for next party.
                else {
                    base_x = placementCenter1.pos_x + spacesToRightOfCenter;
                    spacesToRightOfCenter += FIXED_SPACING_X;
                }

                // Calculate vertical position:
                // TODO this may need some adjustment. The rest of this
                // method works as intended.
                if (bsp.isPlayer()) {
                    base_y = (1 + vertical_position) * FIXED_SPACING_Y +
                            MIN_PLACE_Y_1 - region_height;
                } else {
                    // Vertical position should be inverse to height.
                    base_y = (BSP_PER_ROW - 1 - vertical_position) *
                            FIXED_SPACING_Y + MIN_PLACE_Y_2 - region_height;
                }
            }

            // We push everything left at the end, because everything is
            // offset towards the right
            int FIXED_OFFSET_LEFT = 10;
            base_x -= FIXED_OFFSET_LEFT;

            // Adjust even sized parties even more to move them towards the
            // middle
//            if (bspList.size == 2) {
//                base_x -= FIXED_OFFSET_LEFT;
//            }

            System.out.println("Adding at x: " + base_x);


            bsp.currentPosX = base_x;
            bsp.currentPosY = base_y;
            bsp.currentRegHeight = region_height;
            bsp.currentRegWidth = region_width;
            bsp.tryPlaceSubParty();

            this.allies.updateHiddenAll();
            this.enemies.updateHiddenAll();
        }
    }

    // Should the battlemap have a wall?
    public boolean hasWall() {
        return hasWall;
    }

    public boolean isVillage() {
        return village;
    }

    public boolean isRuins() {
        return ruins;
    }

    // TODO put units on wall if a siege
    // returns true if success, false if couldn't place a unit.
    public boolean addUnitsFromSubparty(BattleSubParty bsp, int base_x, int
            base_y) {
        if (base_x< 0 || base_x > size_x) return false;

        Formation formationType = bsp.formation;
        Stance partyStance = bsp.stance;

        Soldier.SoldierType[][] formation = formationType.getFormation(bsp);
        if (!bsp.isPlayer()) formation = flipVertical(formation);

        int region_height = formation.length;
        int region_width = formation[0].length;

        // This determines where the individual units will be placed
        boolean generalAdded = false;
        for (int i = 0; i < region_height; i++) {
            for (int j = 0; j < region_width; j++) {
                if (formation[i][j] != null) {
                    Unit toAdd = null;
                    boolean addGeneral = false;
                    if (formation[i][j] == Soldier.SoldierType.INFANTRY &&
                            bsp.infantrySizeWithoutGeneral() > 0)
                        toAdd = bsp.getInfantry().pop();
                    else if (formation[i][j] == Soldier.SoldierType.ARCHER &&
                            bsp.archersSizeWithoutGeneral() > 0)
                        toAdd = bsp.getArchers().pop();
                    else if (formation[i][j] == Soldier.SoldierType.CAVALRY
                            && bsp.cavalrySizeWithoutGeneral() > 0)
                        toAdd = bsp.getCavalry().pop();
                    else if (formation[i][j] == Soldier.SoldierType.GENERAL) {
                        if (bsp.general == null) throw new AssertionError();
                        if (bsp.team == 1) {
                            System.out.println("Adding enemy general in bs");
                        }
                        generalAdded = true;
                        addGeneral = true;
                        toAdd = bsp.general;
                    } else if (formation[i][j] == Soldier.SoldierType.SHAMAN) {
                        if (bsp.shaman == null) continue;
                        generalAdded = true;
                        addGeneral = true;
                        toAdd = bsp.shaman;
                    }
                    if (toAdd == null) continue;

                    // Don't add general yet.
                    if (toAdd.isGeneral() && !addGeneral) continue;

                    if (canPlaceUnit(base_x + j, base_y + i)) {
//						Unit unit = new Unit(this, base_x + j, base_y + i,
// team, toAdd, bsp);
                        toAdd.setStance(partyStance);
                        if (bsp.isPlayer() && siegeOrRaid || toAdd.onWall())
                            toAdd.dismount();
                        addUnitToField(toAdd, base_x + j, base_y + i);
                    } else {
                        boolean unitPlaced = false;
                        if (bsp.isPlayer()) {
                            // Try placing behind party
                            for (int k = base_y + i; k >= this.MIN_PLACE_Y_1;
                                 k--) {
                                if (canPlaceUnit(base_x + j, k)) {
                                    toAdd.setStance(partyStance);
                                    if (bsp.isPlayer() && siegeOrRaid ||
                                            toAdd.onWall())
                                        toAdd.dismount();
                                    addUnitToField(toAdd, base_x + j, k);
                                    unitPlaced = true;
                                    break;
                                }
                            }
                            // Now try placing in front of party
                            if (!unitPlaced) {
                                for (int k = base_y + i; k <= this
                                        .MAX_PLACE_Y_1; k++) {
                                    if (canPlaceUnit(base_x + j, k)) {
                                        toAdd.setStance(partyStance);
                                        if (bsp.isPlayer() && siegeOrRaid ||
                                                toAdd.onWall())
                                            toAdd.dismount();
                                        addUnitToField(toAdd, base_x + j, k);
                                        unitPlaced = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            for (int k = base_y + i; k <= this.MAX_PLACE_Y_2;
                                 k++) {
                                if (canPlaceUnit(base_x + j, k)) {
                                    toAdd.setStance(partyStance);
                                    if (bsp.isPlayer() && siegeOrRaid ||
                                            toAdd.onWall())
                                        toAdd.dismount();
                                    addUnitToField(toAdd, base_x + j, k);
                                    unitPlaced = true;
                                    break;
                                }
                            }
                            // Now try placing in front of party
                            if (!unitPlaced) {
                                for (int k = base_y + i; k >= this
                                        .MIN_PLACE_Y_2; k--) {
                                    if (canPlaceUnit(base_x + j, k)) {
                                        toAdd.setStance(partyStance);
                                        if (bsp.isPlayer() && siegeOrRaid ||
                                                toAdd.onWall())
                                            toAdd.dismount();
                                        addUnitToField(toAdd, base_x + j, k);
                                        unitPlaced = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!unitPlaced) {
                            System.out.println("NO VALID LOCATIONS, UNIT NOT " +
                                    "PLACED: " + base_x);
                            return false;
                        }
                    }
                }
            }
        }

        if (!generalAdded && bsp.subparty.getGeneral() != null) {
            System.out.println("formation: " + formationType.toString() + " " +
                    "has no spot for general");
            throw new AssertionError();
        }

        bsp.currentPosX = base_x;
        bsp.currentPosY = base_y;
        bsp.currentRegHeight = region_height;
        bsp.currentRegWidth = region_width;

        System.out.println("bsp placed at: " + base_x);
        return true;
    }

    private int getTotalBattleSize() {
        return allies.getHealthySize() + enemies.getHealthySize();
    }

//	// add on wall if there is a wall
//	// online alg, start in middle and move
//    // left/right
//	public void addSubparty(BattleSubParty bsp, int index) {
//		Formation choice;
//		choice = bsp.formation;
////		if (playerPartyPanel.isPlayer()) {
////			this.currentFormation = playerPartyPanel.formation;
////		}
//
//		int REINFORCEMENT_DIST = 5;
//		REINFORCEMENT_DIST = 0;
//		Stance partyStance;
//		partyStance = bsp.stance;
//		if (bsp.isPlayer()) {
//			REINFORCEMENT_DIST = -REINFORCEMENT_DIST;
//		}
//
////		Array<Soldier> infantry = bsp.getHealthyInfantry();
////		Array<Soldier> cavalry = bsp.getHealthyCavalry();
////		Array<Soldier> archers = bsp.getHealthyArchers();
//		Soldier.SoldierType[][] formation = choice.getFormation(bsp);
//		if (!bsp.isPlayer()) formation = flipVertical(formation);
//
//		int region_height = formation.length;
//		int region_width = formation[0].length;
//
////		if (playerPartyPanel.isPlayer()) {
////			currentFormationHeight = region_height;
////			currentFormationWidth = region_width;
////		}
//
//		int base_x = placementPoint.pos_x - region_width;
//        int base_y = placementPoint.pos_y - region_height;
//
//		int team = 0;
//		if (!bsp.isPlayer()) {
//			base_x = size_x/2 - region_width/2;
//			base_y = (MAX_PLACE_Y_2 - MIN_PLACE_Y_2)/2 + MIN_PLACE_Y_2;
//			team = 1;
//
//			if (siegeOrRaid && battlemap.wallBottom > 0) {
//				base_y = battlemap.wallBottom + 1;
//			}
//		}
//
//		boolean alreadyOnField = false;
//		if (bsp.currentPosX != 0 && bsp.currentPosY != 0) {
//			base_x = bsp.currentPosX;
//			base_y = bsp.currentPosY;
//			alreadyOnField = true;
//		}
//
//		boolean canPlaceHere = false;
//		int tries = 0;
//
//		// simplify this to only use bounding boxes.
//		// if won't fit, gently nudge over to the right.
//		while (!canPlaceHere) {
//			tries++;
//			if (tries > 100) {
////				throw new java.lang.AssertionError();
//				break;
//			}
//			canPlaceHere = true;
//
//			// This determines where the subparty rectangle will be placed
//            // This is currently biased towards the center of the map?
//            // Idk but it's pretty messed up.
//            // Could be the part below.
//            // Let's try with 20 soldiers
//            // All parties are being placed at the same location.
//
//            // Basically, before we were simply nudging the party over to
// the right when it wouldn't fit.
//            // Now, we have an algo that ensures that it'll fit no matter
// where you place it.
//            // Good solution:
//            //   pre-calculate the width of all subparties
//            //   place "root" subparty at the middle of the map
//            //   place other parties around it, at a small distance away
// from the center one and others.
//
//            // Count even/odd. Even goes on left, odd on right. Let's try
// that.
//
//            // Don't do this if the user is controlling!
//            if (!alreadyOnField) {
//                int totalSquaresLeft = 0;
//                int totalSquaresRight = 0;
//                boolean odd = (index % 2 != 0);
//                odd = false;
//                int totalSubCount = 0;
//
//                int firstPartyLocation = 0;
//                int firstPartySize = 0;
//
//                // Do some simple math here. don't be dumb.
//
//                // Calculate where to place it this party.
//
//                // Try a simpler way, place all parties at once?
//
//                for (BattleSubParty other : bsp.parent.subparties) {
//                    if (other.currentPosX != 0 || other.currentPosY != 0) {
//                        totalSubCount++;
//                        // Add first party as a special count added later
//                        if (totalSubCount == 1) {
//                            firstPartyLocation = region_width / 2;
//                            firstPartySize = other.formation.getFormation
// (other)[0].length;
//                        }
//                        else if (odd) totalSquaresRight += other.formation
// .getFormation(other)[0].length;
//                        else totalSquaresLeft += other.formation
// .getFormation(other)[0].length;
//                        odd = !odd;
//                        System.out.println("totalsquaresleft: " +
// totalSquaresLeft);
//                        System.out.println("totalsquaresright: " +
// totalSquaresRight);
//                    }
//                }
//                // This is true if the party is the center party.
////                if (totalSubCount == 0) {
////                    // do nothing. place in the middle
////                    System.out.println("totalsubcount is 0");
////                }
//                // If odd is true, it means we should place this on the
// right side of troops
//                int center_x = base_x + firstPartyLocation;
//                if (odd) {
//                    // Adjust y position as necessary
//                    if (center_x + totalSquaresRight + firstPartySize/2 +
// formation[0].length > region_width) {
//
//                    }
//                    base_x = center_x + totalSquaresRight + firstPartySize/2;
//                    // Just need a way to handle placing parties that
// extend outside of the placeable region
//                    // should just move behind/in front.
////
//                    System.out.println("placing: odd (right) " + base_x);
//                } else {
//                    base_x = center_x - (int) (firstPartySize*1.5f) -
// totalSquaresLeft;
//                    System.out.println("placing: even (left) " + base_x);
//                }
//            }
//
//            int start_x = -1;
//            int start_y = -1;
//            if (!bsp.isPlayer()) {
//                start_x = region_width;
//                start_y = region_height;
//            }
//
//            for (int i = start_y; i < region_height && i >= 0; ) {
//                if (bsp.isPlayer()) i++;
//                else i--;
//                for (int j = start_x; j < region_width && j >= 0;) {
//                    if (bsp.isPlayer()) j++;
//                    else j--;
//
//                    if (formation[i][j] == null) continue;
//                    if (i + base_y < 0 || j + base_x >= size_x - 1) continue;
//                    // change to handle rocks, trees, beach...
//                    if (units[i + base_y][j + base_x] != null ||
// (!canPlaceUnitPlacement(j + base_x, i + base_y, team) &&
// !canPlaceUnitPlacement(j + base_x, i + base_y + REINFORCEMENT_DIST, team))) {
//                        canPlaceHere = false;
//
//                        base_x += (int) (Math.random() * 10);
//                        if (base_x >= this.MAX_PLACE_X - region_width) {
//                            base_x = (int) (Math.random() * 20);
//                            base_y += (int) (Math.random() * 5);
//                        }
//
//                        if (bsp.isPlayer() && base_y >= this.MAX_PLACE_Y_1
// - region_height) {
//                            base_y = this.MIN_PLACE_Y_1;
//                        } else if (!bsp.isPlayer() && base_y >= this
// .MIN_PLACE_Y_2 - region_height) {
//                            base_y = (int) (this.MAX_PLACE_Y_2);
////							System.out.println("base y: " + base_y);
//                        }
//                        break;
//                    }
//                }
//                if (!canPlaceHere) {
////					System.out.println("cant place yo");
//                    break;
//                }
//            }
//		}
//
//
//		// This determines where the individual units will be placed
//		for (int i = 0; i < region_height; i++) {
//			for (int j = 0; j < region_width; j++) {
//				if (formation[i][j] != null) {
//					Unit toAdd;
//					if (formation[i][j] == Soldier.SoldierType.INFANTRY && bsp
// .infantry.size > 0) toAdd = bsp.infantry.pop();
//					else if (formation[i][j] == Soldier.SoldierType.ARCHER &&
// bsp.archers.size > 0) toAdd = bsp.archers.pop();
//					else if (formation[i][j] == Soldier.SoldierType.CAVALRY &&
// bsp.cavalry.size > 0) toAdd = bsp.cavalry.pop();
//					else {
//						toAdd = bsp.general;
//					}
//
//					if (canPlaceUnit(base_x + j, base_y + i)) {
////						Unit unit = new Unit(this, base_x + j, base_y + i,
// team, toAdd, bsp);
//						toAdd.setStance( partyStance);
//						if (bsp.isPlayer() && siegeOrRaid || toAdd.onWall())
// toAdd.dismount();
//						addUnitToField(toAdd, base_x + j, base_y + i);
//					}
//					else {
//					    boolean unitPlaced = false;
//                        if (bsp.isPlayer()) {
//                            // Try placing behind party
//                            System.out.println("k: " + (base_y + i -
// REINFORCEMENT_DIST) + " MAX: " + this.MAX_PLACE_Y_1);
//                            for (int k = base_y + i - REINFORCEMENT_DIST; k
// >= this.MIN_PLACE_Y_1; k--) {
//                                if (canPlaceUnit(base_x + j, k)) {
//                                    toAdd.setStance(partyStance);
//                                    if (bsp.isPlayer() && siegeOrRaid ||
// toAdd.onWall()) toAdd.dismount();
//                                    addUnitToField(toAdd, base_x + j, k);
//                                    unitPlaced = true;
//                                    break;
//                                }
//                            }
//                            // Now try placing in front of party
//                            if (!unitPlaced) {
//                                for (int k = base_y + i +
// REINFORCEMENT_DIST; k <= this.MAX_PLACE_Y_1; k++) {
//                                    if (canPlaceUnit(base_x + j, k)) {
//                                        toAdd.setStance(partyStance);
//                                        if (bsp.isPlayer() && siegeOrRaid
// || toAdd.onWall()) toAdd.dismount();
//                                        addUnitToField(toAdd, base_x + j, k);
//                                        unitPlaced = true;
//                                        break;
//                                    }
//                                }
//                            }
//                        } else {
//                            System.out.println("k: " + (base_y + i +
// REINFORCEMENT_DIST) + " MAX: " + this.MAX_PLACE_Y_2);
//                            for (int k = base_y + i + REINFORCEMENT_DIST; k
// <= this.MAX_PLACE_Y_2; k++) {
//                                if (canPlaceUnit(base_x + j, k)) {
//                                    toAdd.setStance( partyStance);
//                                    if (bsp.isPlayer() && siegeOrRaid ||
// toAdd.onWall()) toAdd.dismount();
//                                    addUnitToField(toAdd, base_x + j, k);
//                                    unitPlaced = true;
//                                    break;
//                                }
//                            }
//                            // Now try placing in front of party
//                            if (!unitPlaced) {
//                                for (int k = base_y + i -
// REINFORCEMENT_DIST; k >= this.MIN_PLACE_Y_2; k--) {
//                                    if (canPlaceUnit(base_x + j, k)) {
//                                        toAdd.setStance(partyStance);
//                                        if (bsp.isPlayer() && siegeOrRaid
// || toAdd.onWall()) toAdd.dismount();
//                                        addUnitToField(toAdd, base_x + j, k);
//                                        unitPlaced = true;
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                        if (!unitPlaced) {
//                            System.out.println("NO VALID LOCATIONS, UNIT
// NOT PLACED");
//                        }
//                    }
//				}
//			}
//		}
////		System.out.println("Base x: " + base_x + " Base y: " + base_y);
//
//		bsp.currentPosX = base_x;
//		bsp.currentPosY = base_y;
//		bsp.currentRegHeight = region_height;
//		bsp.currentRegWidth = region_width;
//
//		this.allies.updateHiddenAll();
//		this.enemies.updateHiddenAll();
//	}


    public void addUnitToField(Unit unit, int x, int y) {
        units[y][x] = unit;
        unit.pos_x = x;
        unit.pos_y = y;
        unit.original_x = x;
        unit.original_y = y;
        addActor(unit);
        unit.updateCover();
    }

    public void removeSubParty(BattleSubParty s) {
        s.clearAllUnitsFromStage();
        for (Unit u : s.units) {
            removeActor(u);

            // for some weird ass reason this doesn't work to clear the
            // units[] array...
//			System.out.println("u.pos_x: " + u.pos_x + " u.pos_y: " + u.pos_y);
            if (u.inMap() && units[u.pos_y][u.pos_x] == null)
                System.out.println("unit is null");
            if (u.inMap()) units[u.pos_y][u.pos_x] = null;
        }

        // so we still have to do this full clear shit until the above bug is
        // fixed
        for (int i = 0; i < units.length; i++) {
            for (int j = 0; j < units[0].length; j++) {
                if (units[i][j] != null && units[i][j].bsp == s)
                    units[i][j] = null;
            }
        }
    }

    //	private void updatePlayerParty() {
    //		Formation choice;
    //		choice = allies.formation;
    //		this.currentFormation = allies.formation;
    //
    //		int REINFORCEMENT_DIST = 1;
    //
    //		Stance partyStance;
    //		partyStance = allies.stance;
    //		REINFORCEMENT_DIST = -REINFORCEMENT_DIST;
    //
    //		Array<Unit> infantry = getPlayerInfantry();
    //		Array<Unit> cavalry = getPlayerCavalry();
    //		Array<Unit> archers = getPlayerArchers();
    //
    //		Soldier.SoldierType[][] formation = Formation.getFormation(allies,
    // choice, this.size_x, this.size_y);
    //
    //		int region_height = formation.length;
    //		int region_width = formation[0].length;
    //
    //		currentFormationHeight = region_height;
    //		currentFormationWidth = region_width;
    //
    //		int base_x = placementPoint.pos_x - region_width/2;
    //		int base_y = placementPoint.pos_y - region_height/2;
    //
    //		for (int i = 0; i < region_height; i++) {
    //			for (int j = 0; j < region_width; j++) {
    //				if (formation[i][j] != null) {
    //					Unit toMove = null;
    //					if (formation[i][j] == Soldier.SoldierType.INFANTRY &&
    // infantry.size > 0) toMove = infantry.pop();
    //					else if (formation[i][j] == Soldier.SoldierType.ARCHER
    // && archers.size > 0) toMove = archers.pop();
    //					else if (cavalry.size > 0) toMove = cavalry.pop();
    //
    //					if (toMove == null) continue;
    //
    //					if (canPlaceUnitIgnoreUnits(base_x + j, base_y + i)) {
    //						toMove.setStance( partyStance;
    //						this.moveUnitTo(toMove, new BPoint(base_x + j,
    // base_y+i));
    //						// move the unit
    //					}
    //					else if (canPlaceUnitIgnoreUnits(base_x + j, base_y +
    // i + REINFORCEMENT_DIST)) {
    //						toMove.setStance( partyStance;
    //						this.moveUnitTo(toMove, new BPoint(base_x + j,
    // base_y+i + REINFORCEMENT_DIST));
    //						// move the unit
    //					}
    //				}
    //			}
    //		}
    //		for (Unit unit : getAllies()) {
    //			units[unit.pos_y][unit.pos_x] = unit;
    //		}
    //	}

    //	public Array<Unit> getPlayerInfantry() {
    //		Array<Unit> toReturn = new Array<Unit>();
    //		for (Unit u : getAllies())
    //			if (u.soldier.getType() == Soldier.SoldierType.INFANTRY)
    // toReturn.add(u);
    //		return toReturn;
    //	}
    //	public Array<Unit> getPlayerArchers() {
    //		Array<Unit> toReturn = new Array<Unit>();
    //		for (Unit u : getAllies())
    //			if (u.soldier.getType() == Soldier.SoldierType.ARCHER)
    // toReturn.add(u);
    //		return toReturn;
    //	}
    //	public Array<Unit> getPlayerCavalry() {
    //		Array<Unit> toReturn = new Array<Unit>();
    //		for (Unit u : getAllies())
    //			if (u.soldier.getType() == Soldier.SoldierType.CAVALRY)
    // toReturn.add(u);
    //		return toReturn;
    //	}


    //	public void toNextFormation() {
    //		changePlayerFormation(getNextFormation());
    //	}

    // TODO change these to handle individual squads.
//	public void changePlayerFormation(Formation formation) {
//		//		this.allies.formation = formation;
//		//		this.updateFormationLocation();
//	}

//	public String getPlayerStanceString() {
//		//		if (this.allies.setStance(= Stance.AGGRESSIVE) return
// "Aggressive";
//		//		else if (this.allies.setStance(= Stance.DEFENSIVE) return
// "Defensive";
//		//		else if (this.allies.setStance(= Stance.INLINE) return "Line";
//		return "Unspecified";
//		//		else return null;
//	}

    private void addSiegeUnits(BattleParty party) {
        if (party.player) {
            int siegeCount = 3;
            int siegeZoneBottom = 0;
            int siegeZoneSize = 20; // 20 from the top

            for (int i = 0; i < siegeCount; i++) {

                BPoint point;
                do {
                    // generate random spot in siegeOrRaid zone
                    int x = (int) (Math.random() * size_x);
                    int y = siegeZoneBottom + (int) (Math.random() *
                            siegeZoneSize);
                    point = new BPoint(x, y);
                }
                while (!SiegeUnit.canPlace(this, point.pos_x, point.pos_y));

                SiegeUnit catapult = new SiegeUnit(this, SiegeUnit.SiegeType
                        .CATAPULT, point.pos_x, point.pos_y, Orientation.UP);
                addSiegeUnit(catapult);
            }
        } else {
            int siegeCount = 5;
            int siegeZoneTop = 0;
            int siegeZoneSize = 20; // 20 from the top

            for (int i = 0; i < siegeCount; i++) {

                BPoint point;
                do {
                    //					System.out.println("finding spot");
                    // generate random spot in siegeOrRaid zone
                    int x = (int) (Math.random() * size_x);
                    int y = this.size_y - 1 - siegeZoneTop - (int) (Math
                            .random() * siegeZoneSize);
                    point = new BPoint(x, y);
                }
                while (!SiegeUnit.canPlace(this, point.pos_x, point.pos_y));

                SiegeUnit catapult = new SiegeUnit(this, SiegeUnit.SiegeType
                        .CATAPULT, point.pos_x, point.pos_y, Orientation.DOWN);
                addSiegeUnit(catapult);
            }
        }
    }

    public boolean inMap(BPoint p) {
        return p.pos_x < size_x &&
                p.pos_y < size_y &&
                p.pos_x >= 0 &&
                p.pos_y >= 0;
    }

    public boolean inMap(int x, int y) {
        return x < size_x &&
                y < size_y &&
                x >= 0 &&
                y >= 0;
    }

    public void tryToRetreatAll(boolean player) {
        if (player && retreatTimerPlayer < 0) {
            allies.tryToRetreatAll();
        } else if (!player && retreatTimerEnemy < 0) {
            enemies.tryToRetreatAll();
        }
    }

    public void chargeAll(boolean player) {
        if (player) {
            for (BattleSubParty p : allies.subparties)
                p.charge();
        } else {
            for (BattleSubParty p : enemies.subparties)
                p.charge();
        }
    }

    private static Soldier.SoldierType[][] flipVertical(Soldier
                                                                .SoldierType[][] formation) {
        Soldier.SoldierType[][] flipped = new Soldier.SoldierType[formation
                .length][formation[0].length];

        for (int i = 0; i < formation.length; i++) {
            for (int j = 0; j < formation[0].length; j++) {
                flipped[i][j] = formation[formation.length - i - 1][j];
            }
        }
        return flipped;
    }

    @Override
    public void act(float delta) {
        SoundPlayer.updateSounds(delta);

        // TODO better way to do this?
        delta = SPEED;

        // try to slow things down
        if (mapScreen.slowDown) {
            delta = 0.005f;
        }

        battlemap.actSpecial(delta);
        if (mouseOver) {
            if (leftClicked)
                leftClick(mouse);
            else if (rightClicked)
                rightClick(mouse);
            else if (BesiegeMain.appType != 1)
                mouseOver(mouse);
        }
        if (!placementPhase) {
            super.act(delta);
            this.retreatTimerPlayer -= delta;
            this.retreatTimerEnemy -= delta;

            if ((alliesDefending && (1 - getBalanceDefenders()) <
                    RETREAT_THRESHOLD / 2) || (!alliesDefending &&
                    getBalanceDefenders() < RETREAT_THRESHOLD / 2)) {
                tryToRetreatAll(false);
            }
            if ((alliesDefending && (1 - getBalanceDefenders()) >
                    CHARGE_THRESHOLD) || (!alliesDefending &&
                    getBalanceDefenders() > CHARGE_THRESHOLD)) {
                chargeAll(false);
            }

//			if (this.kingdom != null) {
//				if (allies.noUnits()) {
//				    // TODO make this work with multiple armies
//					victory(enemies.first(), allies.first());
//				} else if (enemies.noUnits()) {
//
//					victory(allies.first(), enemies.first());
//				}
//			}
            // Note this only happens if it was a simulation.
            // TODO make this happen no matter what -- and add a button to
            // return to main screen.
            // TODO add a "Battle Summary Panel" to the right hand side
            if (!isOver()) {
                if (allies.noUnits() && !placementPhase) {
                    victory(enemies.first(), allies.first());
//				/	BottomPanel.log("Defeat", "green");
//					if (kingdom != null)
//						victoryManager.handleVictory(getAttackingParties(),
// getDefendingParties(), getAttackingPartiesRetreated(),
// getDefendingPartiesRetreated(), false);
                    displayVictoryText("Defeat");
//                    this.isOver = true;
//                    this.placementPhase = true;
////					mapScreen.switchToKingdomView();
//					postBattle = new PanelPostBattle(mapScreen.getSidePanel(),
// this);
//					mapScreen.getSidePanel().setActive(postBattle);
                } else if (enemies.noUnits() && !placementPhase) {
                    victory(allies.first(), enemies.first());
//					if (kingdom != null)
//	                    victoryManager.handleVictory(getAttackingParties(),
// getDefendingParties(), getAttackingPartiesRetreated(),
// getDefendingPartiesRetreated(), true);
                    // display "Victory" text.
                    displayVictoryText("Victory");
//                    this.isOver = true;
////					BottomPanel.log("Victory", "green");
//					this.placementPhase = true;
////					mapScreen.switchToKingdomView();
//					postBattle = new PanelPostBattle(mapScreen.getSidePanel(),
// this);
//					mapScreen.getSidePanel().setActive(postBattle);
                }
            }
        } else if (placementPhase) {
            if (dragging && selectedUnit != null)
                selectedUnit.bsp.updateFormationLocation();
        }
        if (leftClicked)
            leftClicked = false;
        if (rightClicked)
            rightClicked = false;

        if (raining) rain();
    }

    public void damageWallAt(int pos_x, int pos_y, float damage) {
        battlemap.damageWallAt(pos_x, pos_y, damage);
    }

    private void mouseOver(Point mouse) {
        Unit u = getUnitAt(mouse);
        // if (d.getType() != 0)
        if (u != null && (u.team == 0 || !u.isHidden()))
            this.setPanelTo(u);
        else if (selectedUnit != null)
            this.setPanelTo(selectedUnit);
        else if (!isOver) {
            // Reset panel to battlestage
            currentPanel = null;
            mapScreen.getSidePanel().setActive(pb);
        } else {
            if (postBattle == null) throw new AssertionError();
            mapScreen.getSidePanel().setActive(postBattle);
        }
        // d.setMouseOver(true);
    }

    private void setPanelTo(Unit newPanel) {
        // if (currentPanel == null) System.out.println("currentPanel is null");
        // makes sure not to set the same panel a lot, and makes sure not to
        // return to previous for every single point

        if (newPanel != currentPanel) {
            mapScreen.getSidePanel().setActiveUnit(newPanel);
            this.currentPanel = newPanel;
        }
        // if (newPanel == null)

    }

    // TODO Make memory efficient
    BPoint mouseToPoint() {
        float x = this.mouse.getX();
        float y = this.mouse.getY();

        int x_int = (int) (x / this.unit_width);
        int y_int = (int) (y / this.unit_width);

        return new BPoint(x_int, y_int);
    }


//	// put formation in proper place


    private void moveUnitTo(Unit unit, BPoint newPoint) {
        this.units[unit.pos_y][unit.pos_x] = null;
        //		this.units[newPoint.pos_y][newPoint.pos_x] = unit;
        unit.pos_x = newPoint.pos_x;
        unit.pos_y = newPoint.pos_y;
        unit.original_x = newPoint.pos_x;
        unit.original_y = newPoint.pos_y;
    }

    private void removeParty(Party party) {
        if (party.player) {
            for (int i = 0; i < size_x; i++) {
                for (int j = 0; j < size_y; j++) {
                    if (units[j][i] != null && units[j][i].team == 0)
                        allies.removeUnit(units[j][i], false);
                }
            }
        }
    }

    private void leftClick(Point mouse) {
        if (this.placementPhase) {
            if (!dragging) {
                Unit u = getUnitAt(mouse);
                selectedUnit = u;
                if (u != null && u.team == 0) {

                    // just calculate distance from center
                    centerOffset = new BPoint(this.placementPoint.pos_x - u
                            .getPoint().pos_x, this.placementPoint.pos_y - u
                            .getPoint().pos_y);

                    dragging = true;
                    u.bsp.updateFormationLocation();
                }
            } else {
                dragging = false;
                centerOffset = new BPoint(0, 0);
            }
        }
    }

    public void unselectUnit() {
        selectedUnit = null;
    }

    public void selectUnit(Unit u) {
        if (u == null) throw new AssertionError();
        selectedUnit = u;
        setPanelTo(u);
        mouseOver(mouse);
    }

    private void rightClick(Point mouse) {
        if (dragging) {
            dragging = false;
            centerOffset = new BPoint(0, 0);
            return;
        }


        Unit u = getUnitAt(mouse);

        if (u != null) {
            selectUnit(u);
            // System.out.println("unit at mouse is " + u.soldier.name);
        } else {
            selectedUnit = null;
            currentPanel = null;
        }
    }

    public void click(int pointer) {
        //		if (pointer == 0)
        //			leftClicked = true;
        //		else if (pointer == 1)
        //			rightClicked = true;
        //		else if (pointer == 4)
        //			writeUnit();
        // try switching
        if (pointer == 1)
            leftClicked = true;
        else if (pointer == 0)
            rightClicked = true;
        //		else if (pointer == 4)
        //			writeUnit();
    }

    //	public void addUnit(Unit unit) {
    //		if (unit.team == 0) allies.addUnit(unit);
    //		if (unit.team == 1) enemies.addUnit(unit);
    //	}


    // Note this is only called if not a simulation
    // Not anymore! we're consolidating.
    public void victory(Party winner, Party loser) {
        System.out.println("Battle over!");
        Army winnerArmy = winner.army; // May be null
        Army loserArmy = loser.army;

        if (kingdom != null && winnerArmy != kingdom.getPlayer() && loserArmy
                != kingdom.getPlayer())
            System.out.println("Player not involved in victory!!!");

        this.isOver = true;

        if (winner.player) {
//			battle.logDefeat(loser);
            if (kingdom != null) {
                kingdom.getMapScreen().getSidePanel().setHardStay(false);
                if (winnerArmy != null)
                    kingdom.getMapScreen().getSidePanel().setActiveArmy
                            (winnerArmy);
            }
            if (!alliesDefending) didAtkWin = true;
            else didAtkWin = false;
        } else {
            if (alliesDefending) didAtkWin = true;
            else didAtkWin = false;
        }

        // heal retreated soldiers
        for (Unit u : retreated)
            u.soldier.subparty.healNoMessage(u.soldier);

        victoryManager.handleVictory(getAttackingParties(),
                getDefendingParties(), getAttackingPartiesRetreated(),
                getDefendingPartiesRetreated(), didAtkWin);

        if (kingdom != null && winner.player) {
            kingdom.getMapScreen().getSidePanel().setHardStay(false);
        }

        // log(army.getName() + " has won a battle", "cyan");

        boolean loserDestroyed = false;

//		// figure out if totally destroyed or if units retreated
//		if ((loser.getParty().getHealthySize() <= OldBattle
// .DEPRECATED_THRESHOLD && !loser.getParty().player) || loser.getParty()
// .getHealthySize() <= 0) {
//			battle.increaseSpoilsForKill(loser);
//			loserDestroyed = true;
//			loser.destroy();
//		} else battle.increaseSpoilsForRetreat(loser);


        for (Party p : allies.parties) {
            if (p.army != null) {
                p.army.endBattle();
            }
            for (Subparty s : p.subparties) {
                s.handleBattleEnded();
            }
        }
        for (Party p : enemies.parties) {
            if (p.army != null) {
//                if (!((p.getHealthySize() <= DEPRECATED_THRESHOLD && !p
// .player) || p.getHealthySize() <= 0))
//                    p.army.setVisible(true);
                p.army.endBattle();
//                p.army.setStopped(false);
//                p.army.setTarget(null);
            }
            for (Subparty s : p.subparties) {
                s.handleBattleEnded();
            }
        }

//		if (loserArmy != null)
//		    loserArmy.waitFor(0);
        if (loserArmy != null) {
            loserArmy.forceWait = false;
//		    loserArmy.endBattle();
        }
        if (winnerArmy != null) {
            winnerArmy.forceWait(winnerArmy.getForceWait());
        }
//		if (battle.getSiegeLocation() != null && !battle.getSiegeLocation()
// .isVillage()) {
//			System.out.println("managing siegeOrRaid");
//			battle.handleSiegeVictory();
//		}
//		if (didAtkWin && battle.getSiegeLocation() != null && battle
// .getSiegeLocation().isVillage())
//			((Village) battle.getSiegeLocation()).handleRaidVictory(winner);

//		battle.distributeRewards(winner, 1, didAtkWin);
//		battle.destroy();

        this.pb = null;

        // see if player died
        if (loserDestroyed && didAtkWin == alliesDefending)
            mapScreen.playerDeath();

//		mapScreen.switchToKingdomView();
        postBattle = new PanelPostBattle(mapScreen.getSidePanel(), this, this);
        mapScreen.getSidePanel().setActive(postBattle);
    }

    public void exitBattle() {
        // free up some memory
        this.battlemap = null;
        this.postBattle = null;
        clearVictoryText();

        mapScreen.switchToKingdomView();
    }

    //	public void writeUnit() {
    //		float x = mouse.getCenterX();
    //		float y = mouse.getCenterY();
    //		// mapScreen.out.println(x + " " + y);
    //	}

    private Unit getUnitAt(Point mouse) {
        BPoint mousePoint = this.mouseToPoint();
        if (inMap(mousePoint)) {
            Unit u = units[mousePoint.pos_y][mousePoint.pos_x];
            if (u != null) return u;

            // search all adjacent points
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (inMap(new BPoint(mousePoint.pos_x + i, mousePoint
                            .pos_y + j))) {
                        Unit adj = units[mousePoint.pos_y + j][mousePoint
                                .pos_x + i];
                        if (adj == null) continue;
                        if (adj.prev_x == mousePoint.pos_x && adj.prev_y ==
                                mousePoint.pos_y)
                            return adj;
                        if (adj != null && (i == 0 || j == 0)) u = adj;
                    }
                }
            }
            return u;
        } else return null;
    }

    public boolean ladderAt(int pos_x, int pos_y) {
        return battlemap.ladderAt(pos_x, pos_y);
    }

    public boolean entranceAt(int pos_x, int pos_y) {
        return battlemap.entranceAt(pos_x, pos_y);
    }

    //	public boolean siegeUnitAdjacent(int pos_x, int pos_y) {
    //
    //		if (siegeUnits[pos_y][pos_x] != null) return false;
    //	}

    public boolean wallAt(int pos_x, int pos_y) {
        return heights[pos_y][pos_x] > 0;
    }

    private double mouseDistTo(Unit unit) {
        float dx = mouse.getX() - unit.getCenterX();
        float dy = mouse.getY() - unit.getCenterY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        updateColor(batch);
        super.draw(batch, parentAlpha);

        // gotta update again in case it's still set to some bad value.
        updateColor(batch);
        battlemap.drawTrees(batch); // will probably be above arrows for now

        if (drawCrests) {

        }

    }

    public void addSiegeUnit(SiegeUnit unit) {
        this.siegeUnitsArray.add(unit);
        this.addActor(unit);
    }

    //	public void setPaused(boolean paused) {
    //		this.paused = paused;
    //	}

    public float getStageSlow() {
        if (this.isSnowing()) {
            return SNOW_SLOW;
        }
        if (this.isRaining()) {
            return RAIN_SLOW;
        }
        return 1;
    }

    public float getMouseX() {
        return mouse.getX();
    }

    public float getMouseY() {
        return mouse.getY();
    }

    public void setMouse(Vector2 mousePos) {
        mouse.setPos(mousePos.x, mousePos.y);
    }

    public MapScreen getMapScreen() {
        return mapScreen;
    }

    public float getZoom() {
        return getMapScreen().battleCamera.zoom;
    }

    public void setMouseOver(boolean b) {
        mouseOver = b;
    }

    //	public boolean isPaused() {
    //		return paused;
    //	}

    public static double distBetween(Unit d1, Unit d2) {
        // TODO optimize by computing getCenter only once per
        return Math.sqrt((d1.getCenterX() - d2.getCenterX())
                * (d1.getCenterX() - d2.getCenterX())
                + (d1.getCenterY() - d2.getCenterY())
                * (d1.getCenterY() - d2.getCenterY()));
    }

    // should be slightly faster than above
    public static double sqDistBetween(Destination d1, Destination d2) {
        return (d1.getCenterX() - d2.getCenterX())
                * (d1.getCenterX() - d2.getCenterX())
                + (d1.getCenterY() - d2.getCenterY())
                * (d1.getCenterY() - d2.getCenterY());
    }

    public boolean canPlaceUnit(int pos_x, int pos_y) {
        if (pos_x < 0 || pos_y < 0 || pos_x >= size_x || pos_y >= size_y) {
//            System.out.println("outside of size");
            return false;
        }
        if (closed[pos_y][pos_x]) {
//            System.out.println("area closed");
            return false;
        }
        if (units[pos_y][pos_x] != null) {
//            System.out.println("already occupied");
            return false;
        }
        return true;
    }

    public boolean canPlaceUnitPlacement(int pos_x, int pos_y, int team) {
//		System.out.println("pos_x:" + pos_x + " pos_y: " + pos_y);
        if (!canPlaceUnit(pos_x, pos_y)) return false;
//		if (pos_x < MIN_PLACE_X) return false;
//		if (pos_x > MAX_PLACE_X) return false;
//		if (team == 0) {
//			if (pos_y <= MIN_PLACE_Y) return false;
//			if (pos_y >= MAX_PLACE_Y) return false;
//		}
//		else {
//			if (pos_y <= size_y - MAX_PLACE_Y) return false;
//			if (pos_y >= size_y - MIN_PLACE_Y) return false;
//		}
        return true;
    }

    public boolean canPlaceUnitIgnoreUnits(int pos_x, int pos_y) {
        if (pos_x < 0 || pos_y < 0 || pos_x >= size_x || pos_y >= size_x)
            return false;
        if (closed[pos_y][pos_x]) return false;
        return true;
    }

    // return true if in wall, false if otherwise
    public boolean insideWall(int pos_x, int pos_y) {
        return battlemap.insideWalls(pos_x, pos_y);
    }

    public void displayVictoryText(String text) {
        LabelStyle ls = new LabelStyle();
        ls.font = Assets.pixel100;
        victoryText = new Label(text, ls);
        victoryText.addAction(Actions.fadeIn(2000, Interpolation.sine));
        victoryText.setFillParent(true);
        victoryText.setX(BesiegeMain.WIDTH * 0.4f);
        victoryText.setTouchable(Touchable.disabled);
        mapScreen.getUIStage().addActor(victoryText);
    }

    public void clearVictoryText() {
        if (victoryText != null)
            victoryText.remove();
        victoryText = null;
    }

    @Override
    public boolean addToAttackers(Army army) {
        // No-op
        return false;
    }

    @Override
    public boolean addToDefenders(Army army) {
        // No-op
        return false;
    }

    @Override
    public StrictArray<Party> getAttackingParties() {
        return getAttacking().parties;
    }

    @Override
    public StrictArray<Party> getDefendingParties() {
        return getDefending().parties;
    }

    @Override
    public StrictArray<Party> getAttackingPartiesRetreated() {
        if (!getAttacking().noUnits() && !getDefending().noUnits())
            throw new AssertionError();

        if (retreatedAttackers != null) return retreatedAttackers;

        // For now, just return a copy of all the defeated parties.
        // Main problem is that this ignores parties that have fled the
        // battle on the winning side.
        // Eventually need to fix this.
        if (getAttacking().noUnits()) {
            System.out.println("retreated attacker size1: " + getAttacking()
                    .parties.size);
            retreatedAttackers = new StrictArray<>(getAttacking().parties);
            getAttacking().parties.clear();
            System.out.println("retreated attacker size: " +
                    retreatedAttackers.size);

            return retreatedAttackers;
        } else {
            System.out.println("returning empty attackers");
            return new StrictArray<>();
        }
    }

    @Override
    public StrictArray<Party> getDefendingPartiesRetreated() {
        if (!getAttacking().noUnits() && !getDefending().noUnits())
            throw new AssertionError();

        if (retreatedDefenders != null) return retreatedDefenders;

        // For now, just return a copy of all the defeated parties.
        // Main problem is that this ignores parties that have fled the battle
        // on the winning side.
        // Eventually need to fix this.
        if (getDefending().noUnits()) {
            System.out.println("retreated defenders size1: " + getDefending()
                    .parties.size);
            retreatedDefenders = new StrictArray<>(getDefending()
                    .parties);
            getDefending().parties.clear();
            System.out.println("retreated defender size: " +
                    retreatedDefenders.size);
            return retreatedDefenders;
        } else {
            System.out.println("returning empty defenders");
            return new StrictArray<>();
        }
    }

    @Override
    public boolean shouldJoinAttackers(Army army) {
        // no-op
        return false;
    }

    @Override
    public boolean shouldJoinDefenders(Army army) {
        // no-op
        return false;
    }

    @Override
    public void setDefensiveAdvantage(double advantage) {
        // TODO
    }

    @Override
    public Faction getAttackingFactionOrNull() {
        return getAttacking().parties.first().getFaction();
    }

    @Override
    public Faction getDefendingFactionOrNull() {
        return getDefending().parties.first().getFaction();
    }

    public BattleParty getAttacking() {
        if (alliesDefending) {
            return enemies;
        } else {
            return allies;
        }
    }

    public BattleParty getDefending() {
        if (!alliesDefending) {
            return enemies;
        } else {
            return allies;
        }
    }

    @Override
    public float getAttackingAtk() {
        // TODO Auto-generated method stub
        return getAttacking().getAtk();
    }

    @Override
    public float getDefendingAtk() {
        return getDefending().getAtk();
    }

    @Override
    public void simulate(float delta) {
        // no-op?
    }

    @Override
    public void forceRetreat(Army army) {
        // no-op
    }

    @Override
    public void forceRetreatAllAttackers() {
        // no-op
    }

    @Override
    public void casualty(Soldier soldier, boolean wasAttacker) {
        // We already set killedOrWoundedBy previously if present.
        boolean killed = soldier.casualty(wasAttacker, soldier.killedOrWoundedBy, !alliesDefending, alliesDefending);
        victoryManager.handleCasualty(soldier, wasAttacker, killed);
    }

    @Override
    public boolean playerAttacking() {
        return !alliesDefending;
    }

    @Override
    public boolean playerDefending() {
        return alliesDefending;
    }

    @Override
    public boolean isOver() {
        return isOver;
    }

    @Override
    public boolean didAttackersWin() {
        return didAtkWin;
    }

    @Override
    public VictoryManager getVictoryManager() {
        return victoryManager;
    }
}
