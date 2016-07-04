package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
// stage for battles, contains all information regarding battle.

import kyle.game.besiege.BesiegeMain;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.MapScreen;
import kyle.game.besiege.Point;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.battle.Unit.Orientation;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.PanelBattle;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Subparty;
import kyle.game.besiege.voronoi.Biomes;

public class BattleStage extends Group {
	public Biomes biome;
	public Battle battle;
	private PanelBattle pb;
	//	public float scale = 1f;
	public float MIN_SIZE = 40;
	public float SIZE_FACTOR = .3f; // how much does the size of the parties
	public float targetDarkness;
	public float currentDarkness;
	public boolean raining;
	// affect the size of battlefield?

	public static boolean drawCrests = true;

	private final float MOUSE_DISTANCE = 15; // distance destination must be

	private final double CHARGE_THRESHOLD = .5;

	public static int SIDE_PAD = 5;
	public static int BOTTOM_PAD = 5;
	public static int PLACE_HEIGHT = 15;

	public static double RETREAT_TIME_BASE = 10; // have to wait 5 secs before can retreat
	static final float RAIN_SLOW = .8f;
	static final float SNOW_SLOW = .7f;


	public int MIN_PLACE_X;
	public int MAX_PLACE_X;

	public int MIN_PLACE_Y;
	public int MAX_PLACE_Y;

	// from mouse to register

	public BattleParty allies;
	public BattleParty enemies;
	//	public Array<Unit> allies;
	//	public Array<Unit> enemies;

	public Array<SiegeUnit> siegeUnitsArray;

	public boolean placementPhase;

	BPoint centerOffset;
	private BPoint placementPoint;
	private BPoint originalPoint;

	//	Party player;
	//	Party enemy;

	//	public boolean siegeAttack;
	//	public boolean siegeDefense;
	public boolean siege;

	public boolean closed[][]; // open or closed?
	public double slow[][]; // 0 is normal speed, 1 is very slow.
	public Unit[][] units; // units on map
	public SiegeUnit[][] siegeUnits;
	public Array<Unit> retreated; // units that have retreated
	public float heights[][]; // ground floor levels


	// two sizes: one for battle map, one for map to draw
	// this is battle map, map to draw is twice as big
	public int size_x = 128;
	public int size_y = 128;

	BPoint prevMouse;

	private MapScreen mapScreen;
	private Kingdom kingdom;
	//	private Party player;
	public BattleMap battlemap;

	public boolean playerDefending = false;

	//	public Stance allies.stance;
	//	public Stance enemies.stance;

	private boolean mouseOver; // is mouse over Battle screen?
	//	private boolean paused;
	public boolean isOver; // is the battle over?
	public double retreatTimerPlayer;
	public double retreatTimerEnemy;

	public int unit_width = 16;
	public int unit_height = 16;

	public Unit selectedUnit;
	public Unit currentPanel;

	private boolean leftClicked;
	private boolean rightClicked;
	private Point mouse;
	
	
	public boolean dragging;

	//	public Formation playerFormationChoice;
	//	public Formation enemyFormationChoice;
//	public Formation currentFormation; // I need this for some reason for moving units.
//	private Array<Formation> availableFormations;

//	private int currentFormationWidth;
//	private int currentFormationHeight;

	// take in battle object containing arrays of armies and stuff
	public BattleStage(MapScreen mapScreen, Array<Party> allyArray, Array<Party> enemyArray, boolean playerDefending, Location siegeOf) {
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

		this.playerDefending = playerDefending;
		//		this.isPlayer()Defending = false;


		boolean forceSiege = false;
		//		boolean forceSiege = true;

		if ((siegeOf != null && !siegeOf.isVillage()) || forceSiege) {
			//			siegeDefense = playerDefending;
			//			siegeAttack = !siegeDefense;
			//			//siegeAttack = true;
			siege = true;
		}

		// force siege off for now
		siege = false;

		if (playerDefending) {
			this.battle = new Battle(mapScreen.getKingdom(), enemies.first(), allies.first());
		}
		else  {
			this.battle = new Battle(mapScreen.getKingdom(), allies.first(), enemies.first());
		}
		
		this.biome = allies.first().army.getContaining().biome;

		this.raining = getMapScreen().getKingdom().raining;

		this.currentDarkness = kingdom.currentDarkness;
		this.targetDarkness = kingdom.targetDarkness; // TODO change depending on biome
		
		init();
		
		allies.updatePolygon();

		allies.first().army.setBattle(this.battle);
		enemies.first().army.setBattle(this.battle);

		for (Party p : enemies.getPartiesCopy()) {
			if (p != enemies.first())
				this.battle.add(p.army);
			p.army.setBattle(this.battle);
		}
		for (Party p : allies.getPartiesCopy()) {
			if (p != allies.first())
				this.battle.add(p.army);
			p.army.setBattle(this.battle);
		}

		if (battle.siegeOf == null) this.battle.siegeOf = siegeOf;
		else System.out.println("YES SIEGE");

		addUnits();
	}


	// constructor for simulations
	public BattleStage(MapScreen mapScreen, PartyType p1, PartyType p2) {
		this.mapScreen = mapScreen;

		Party allyParty1 = p1.generate();
		allyParty1.player = true;
		Party enemyParty1 = p2.generate();
		
		this.allies = new BattleParty(this, 0);
		this.allies.player = true;
		this.enemies = new BattleParty(this, 1);
		this.enemies.player = false;
		
		this.allies.addParty(allyParty1);
		this.enemies.addParty(enemyParty1);

		// must modify battle so it can support a null kingdom
		// attacker, defender
		if (playerDefending)
			this.battle = new Battle(null, enemyParty1, allyParty1);
		else 
			this.battle = new Battle(null, allyParty1, enemyParty1);
		

		int rand = (int) (Math.random() * Biomes.values().length);
		this.biome = Biomes.values()[rand];
		System.out.println("biome: " + this.biome.toString());

		this.raining = false;

		this.currentDarkness = 1;
		this.targetDarkness = 1;	
		
		init();

		this.enemies.player = false;

		
		addUnits();
	}


	public void init() {
		mouse = new Point(0, 0);

		//		this.isPlayer() = player;
		//		this.enemy = enemy

		if (playerDefending) {
			allies.setStance(Stance.DEFENSIVE);
			enemies.setStance(Stance.AGGRESSIVE);
		}
		else {
			allies.setStance( Stance.AGGRESSIVE);
			if (Math.random() < .5) 
				enemies.setStance( Stance.DEFENSIVE);
			else enemies.setStance( Stance.AGGRESSIVE);
			if (siege) {
				enemies.setStance( Stance.DEFENSIVE);
			}
		}

		int size = allies.getHealthySize() + enemies.getHealthySize();
		size *= SIZE_FACTOR;
		size += MIN_SIZE;
		this.size_x = size;
		this.size_y = size; // square for now

		// round to nearest number divisible by 8, for drawing purposes
		this.size_x += (BattleMap.SIZE - this.size_x % BattleMap.SIZE);
		this.size_y += (BattleMap.SIZE - this.size_y % BattleMap.SIZE);

		closed = new boolean[size_y][size_x];
		units = new Unit[size_y][size_x];
		siegeUnits = new SiegeUnit[size_y][size_x];
		siegeUnitsArray = new Array<SiegeUnit>();
		retreated = new Array<Unit>();
		slow = new double[size_y][size_x];
		heights = new float[size_y][size_x];

		battle.calcBalancePlayer();

		// try this
		pb = new PanelBattle(mapScreen.getSidePanel(), battle);
		pb.battleStage = this;

		this.battlemap = new BattleMap(this);
		this.addActor(battlemap);

		//		if (availableFormations.size == 0) System.out.println("no formations available");

		// set up default formations
		if (playerDefending) {
			enemies.setGlobalFormation(Formation.DEFENSIVE_LINE);
			allies.setGlobalFormation(Formation.DEFENSIVE_LINE);
		}
		else {
			enemies.setGlobalFormation(Formation.DEFENSIVE_LINE);
			allies.setGlobalFormation(Formation.SQUARE);
		}

		if (siege && playerDefending) allies.setGlobalFormation(Formation.WALL_LINE);
		else if (siege && !playerDefending) enemies.setGlobalFormation(Formation.WALL_LINE);


		this.placementPhase = true;

		//		this.paused = true;

		MIN_PLACE_X = SIDE_PAD;
		MAX_PLACE_X = size_x - SIDE_PAD;

		MIN_PLACE_Y = BOTTOM_PAD;
		MAX_PLACE_Y = PLACE_HEIGHT + BOTTOM_PAD;

		// set up orignal base points
		originalPoint =  new BPoint(size_x/2, BOTTOM_PAD + PLACE_HEIGHT/2);
		placementPoint = originalPoint;

		this.retreatTimerPlayer = RETREAT_TIME_BASE / allies.first().getAvgSpd() * 2;
		this.retreatTimerEnemy = RETREAT_TIME_BASE / enemies.first().getAvgSpd() * 2;

	}


	public void centerCamera() {
		// translate to center of screen?
		mapScreen.battleCamera.translate((this.size_x)*this.unit_width/2 - mapScreen.getCamera().position.x, (this.size_y*.4f)*this.unit_height - mapScreen.getCamera().position.y);
		//		mapScreen.getCamera().translate(6, 0);

		//mapScreen.getCamera().translate(new Vector2((this.size_x)*this.unit_width/2, (this.size_y)*this.unit_height/2));
	}

	public void centerCameraOnPlayer() {
		centerCameraOnPoint(this.placementPoint);
	}

	public void centerCameraOnPoint(BPoint point) {
		// translate to center of screen?
		mapScreen.battleCamera.translate(this.unit_width*point.pos_x - mapScreen.getCamera().position.x, this.unit_height*point.pos_y - mapScreen.getCamera().position.y);
		//		mapScreen.getCamera().translate(6, 0);

		//mapScreen.getCamera().translate(new Vector2((this.size_x)*this.unit_width/2, (this.size_y)*this.unit_height/2));
	}

	public void rain() {
		//		System.out.println("raining");
		this.targetDarkness = kingdom.RAIN_FLOAT;
		if (Math.random() < 1/kingdom.THUNDER_CHANCE) thunder();
	}

	private void thunder() {
		//		this.currentDarkness = (float)((Math.random()/2+.5)*this.LIGHTNING_FLOAT);
		this.currentDarkness = kingdom.LIGHTNING_FLOAT;
	}

	public void updateColor(SpriteBatch batch) {
		//		System.out.println("target darkness: " + this.targetDarkness);
		if (this.currentDarkness != this.targetDarkness) adjustDarkness();
		batch.setColor(this.currentDarkness, this.currentDarkness, this.currentDarkness, 1f);
	}

	private void adjustDarkness() {
		if (this.kingdom == null) return;
		if (this.raining) {
			if (this.targetDarkness - this.currentDarkness > kingdom.LIGHT_ADJUST_SPEED) this.currentDarkness += kingdom.LIGHT_ADJUST_SPEED/2;
			else if (this.currentDarkness - this.targetDarkness > kingdom.LIGHT_ADJUST_SPEED) this.currentDarkness -= kingdom.LIGHT_ADJUST_SPEED/2;
		}
		else {
			if (this.targetDarkness - this.currentDarkness > kingdom.LIGHT_ADJUST_SPEED) this.currentDarkness += kingdom.LIGHT_ADJUST_SPEED;
			else if (this.currentDarkness - this.targetDarkness > kingdom.LIGHT_ADJUST_SPEED) this.currentDarkness -= kingdom.LIGHT_ADJUST_SPEED;
		}
	}

	// for now, put them randomly on the field
	public void addUnits() {
		// because I was getting nested iterator error...
		for (int i = 0; i < allies.subparties.size; i++) {
			addSubparty(allies.subparties.get(i));
		}
		for (int i = 0; i < enemies.subparties.size; i++) {
			addSubparty(enemies.subparties.get(i));
		}

		if (siege) {
			if (playerDefending) {
				addSiegeUnits(enemies);
			}
			else {
				addSiegeUnits(allies);
			}
		}
	}
	
	// add on wall if there is a wall
	// online alg, start in middle and move left/right
	public void addSubparty(BattleSubParty bsp) {
		Formation choice;
		choice = bsp.formation;
//		if (party.isPlayer()) {
//			this.currentFormation = party.formation;
//		}

		int REINFORCEMENT_DIST = 5;
		Stance partyStance;
		partyStance = bsp.stance;
		if (bsp.isPlayer()) {
			REINFORCEMENT_DIST = -REINFORCEMENT_DIST;
		}

//		Array<Soldier> infantry = bsp.getHealthyInfantry();
//		Array<Soldier> cavalry = bsp.getHealthyCavalry();
//		Array<Soldier> archers = bsp.getHealthyArchers();
		Soldier.SoldierType[][] formation = Formation.getFormation(bsp, choice, size_x, size_y);
		if (!bsp.isPlayer()) formation = flipVertical(formation);

		int region_height = formation.length;
		int region_width = formation[0].length;

//		if (party.isPlayer()) {
//			currentFormationHeight = region_height;
//			currentFormationWidth = region_width;
//		}

		int base_x = placementPoint.pos_x - region_width;
		int base_y = placementPoint.pos_y - region_height;
		
		int team = 0;
		if (!bsp.isPlayer()) {
			base_x = size_x/2 - region_width/2;
			base_y = (int) (size_y * .7f) - region_height/2;
			team = 1;

			if (siege && battlemap.wallBottom > 0) {
				base_y = battlemap.wallBottom + 1;
			}
		}

		if (bsp.currentPosX != 0 && bsp.currentPosY != 0) {
			base_x = bsp.currentPosX;
			base_y = bsp.currentPosY;
		}
		
		boolean canPlaceHere = false;
		int tries = 0;
		
		// simplify this to only use bounding boxes.
		// if won't fit, gently nudge over to the right.
		while (!canPlaceHere) {
			tries++;
			if (tries > 10000) {
//				throw new java.lang.AssertionError();
				break;
			}
			canPlaceHere = true;
			
			for (int i = 0; i < region_height; i++) {
				for (int j = 0; j < region_width; j++) {
					if (formation[i][j] == null) continue;
					if (i + base_y < 0 || j + base_x >= size_x - 1) continue;
					// change to handle rocks, trees, beach... 
					if (units[i+base_y][j+base_x] != null || (!canPlaceUnitPlacement(j+base_x, i+base_y, team) && !canPlaceUnitPlacement(j+base_x, i+base_y+REINFORCEMENT_DIST, team))) {
						canPlaceHere = false;
						
						base_x += (int) (Math.random() * 10);
						if (base_x >= this.MAX_PLACE_X - region_width) {
							base_x = (int) (Math.random() * 20);
							base_y += (int) (Math.random() * 5);
						}
						
						if (bsp.isPlayer() && base_y >= this.MAX_PLACE_Y - region_height) {
							base_y = this.MIN_PLACE_Y;
						}
						else if (!bsp.isPlayer() && base_y >= size_y - this.MIN_PLACE_Y - region_height) {
							base_y = (int) (size_y - this.MAX_PLACE_Y);
						}
						break;
					}
				}
				if (!canPlaceHere) break;
			}
		}

		for (int i = 0; i < region_height; i++) {
			for (int j = 0; j < region_width; j++) {
				if (formation[i][j] != null) {
					Unit toAdd;
					if (formation[i][j] == Soldier.SoldierType.INFANTRY) toAdd = bsp.infantry.pop();
					else if (formation[i][j] == Soldier.SoldierType.ARCHER) toAdd = bsp.archers.pop();
					else if (formation[i][j] == Soldier.SoldierType.CAVALRY) toAdd = bsp.cavalry.pop();
					else {
						toAdd = bsp.general;
					}
					
					if (canPlaceUnit(base_x + j, base_y + i)) {
//						Unit unit = new Unit(this, base_x + j, base_y + i, team, toAdd, bsp);
						toAdd.setStance( partyStance);
						if (bsp.isPlayer() && siege || toAdd.onWall()) toAdd.dismount();
						addUnitToField(toAdd, base_x + j, base_y + i);
					}
					else if (canPlaceUnit(base_x + j, base_y + i + REINFORCEMENT_DIST)) {
//						Unit unit = new Unit(this, base_x + j, base_y + i + REINFORCEMENT_DIST, team, toAdd, bsp);
						toAdd.setStance( partyStance);
						if (bsp .isPlayer() && siege || toAdd.onWall()) toAdd.dismount();
						addUnitToField(toAdd, base_x + j, base_y + i + REINFORCEMENT_DIST);
					}
				}
			}
		}
//		System.out.println("Base x: " + base_x + " Base y: " + base_y);
		
		bsp.currentPosX = base_x;
		bsp.currentPosY = base_y;
		bsp.currentRegHeight = region_height;
		bsp.currentRegWidth = region_width;
		
		this.allies.updateHiddenAll();
		this.enemies.updateHiddenAll();
	}
	

	public void addUnitToField(Unit unit, int x, int y) {
		units[y][x] = unit;
		unit.pos_x = x;
		unit.pos_y = y;
		unit.original_x = x;
		unit.original_y = y;
		addActor(unit);
	}
	
	public void removeSubParty(BattleSubParty s) {
		s.clearAllUnitsFromStage();
		for (Unit u : s.units) {
			removeActor(u);
			
			// for some weird ass reason this doesn't work to clear the units[] array...
//			System.out.println("u.pos_x: " + u.pos_x + " u.pos_y: " + u.pos_y);
			if (units[u.pos_y][u.pos_x] == null) System.out.println("unit is null");
			units[u.pos_y][u.pos_x] = null;
		}
		
		// so we still have to do this full clear shit until the above bug is fixed
		for (int i = 0; i < units.length; i++) {
			for (int j = 0; j < units[0].length; j++) {
				if (units[i][j] != null && units[i][j].bsp == s) units[i][j] = null;
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
	//		Soldier.SoldierType[][] formation = Formation.getFormation(allies, choice, this.size_x, this.size_y);
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
	//					if (formation[i][j] == Soldier.SoldierType.INFANTRY && infantry.size > 0) toMove = infantry.pop();
	//					else if (formation[i][j] == Soldier.SoldierType.ARCHER && archers.size > 0) toMove = archers.pop();
	//					else if (cavalry.size > 0) toMove = cavalry.pop();
	//
	//					if (toMove == null) continue;
	//
	//					if (canPlaceUnitIgnoreUnits(base_x + j, base_y + i)) {
	//						toMove.setStance( partyStance;
	//						this.moveUnitTo(toMove, new BPoint(base_x + j, base_y+i));
	//						// move the unit
	//					}
	//					else if (canPlaceUnitIgnoreUnits(base_x + j, base_y + i + REINFORCEMENT_DIST)) {
	//						toMove.setStance( partyStance;
	//						this.moveUnitTo(toMove, new BPoint(base_x + j, base_y+i + REINFORCEMENT_DIST));						
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
	//			if (u.soldier.getType() == Soldier.SoldierType.INFANTRY) toReturn.add(u);
	//		return toReturn;
	//	}
	//	public Array<Unit> getPlayerArchers() {
	//		Array<Unit> toReturn = new Array<Unit>();
	//		for (Unit u : getAllies()) 
	//			if (u.soldier.getType() == Soldier.SoldierType.ARCHER) toReturn.add(u);
	//		return toReturn;
	//	}
	//	public Array<Unit> getPlayerCavalry() {
	//		Array<Unit> toReturn = new Array<Unit>();
	//		for (Unit u : getAllies()) 
	//			if (u.soldier.getType() == Soldier.SoldierType.CAVALRY) toReturn.add(u);
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
//		//		if (this.allies.setStance(= Stance.AGGRESSIVE) return "Aggressive";
//		//		else if (this.allies.setStance(= Stance.DEFENSIVE) return "Defensive";
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
					// generate random spot in siege zone 
					int x = (int) (Math.random() * size_x);
					int y = siegeZoneBottom + (int) (Math.random() * siegeZoneSize);
					point = new BPoint(x, y);
				}
				while (!SiegeUnit.canPlace(this, point.pos_x, point.pos_y));

				SiegeUnit catapult = new SiegeUnit(this, SiegeUnit.SiegeType.CATAPULT, point.pos_x, point.pos_y, Orientation.UP);
				addSiegeUnit(catapult);
			}
		}
		else {
			int siegeCount = 5;
			int siegeZoneTop = 0;
			int siegeZoneSize = 20; // 20 from the top

			for (int i = 0; i < siegeCount; i++) {

				BPoint point; 
				do {
					//					System.out.println("finding spot");
					// generate random spot in siege zone 
					int x = (int) (Math.random() * size_x);
					int y = this.size_y - 1 -siegeZoneTop - (int) (Math.random() * siegeZoneSize);
					point = new BPoint(x, y);
				}
				while (!SiegeUnit.canPlace(this, point.pos_x, point.pos_y));

				SiegeUnit catapult = new SiegeUnit(this, SiegeUnit.SiegeType.CATAPULT, point.pos_x, point.pos_y, Orientation.DOWN);
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

	public void retreatAll(boolean player) {	
		if (player && retreatTimerPlayer < 0) {
			allies.retreatAll();
		} 
		else if (!player && retreatTimerEnemy < 0){
			enemies.retreatAll();
		} 
	}

	public void chargeAll(boolean player) {
		if (player) {
			for (BattleSubParty p : allies.subparties)
				p.charge();
		}
		else {
			for (BattleSubParty p : enemies.subparties)
				p.charge();
		}
	}

	private static Soldier.SoldierType[][] flipVertical(Soldier.SoldierType[][] formation) {
		Soldier.SoldierType[][] flipped = new Soldier.SoldierType[formation.length][formation[0].length];

		for (int i = 0; i < formation.length; i++) {
			for (int j = 0; j < formation[0].length; j++) {
				flipped[i][j] = formation[formation.length-i-1][j];
			}
		}
		return flipped;
	}

	@Override
	public void act(float delta) {
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

			if ((playerDefending && battle.balanceA < Battle.RETREAT_THRESHOLD/2) || (!playerDefending && battle.balanceD < Battle.RETREAT_THRESHOLD/2)) {
				retreatAll(false);
			}
			if ((playerDefending && battle.balanceA > CHARGE_THRESHOLD) || (!playerDefending && battle.balanceD > CHARGE_THRESHOLD)) {
				chargeAll(false);
			}

			if (this.kingdom != null) {
				if (allies.noUnits()) {
					victory(enemies.first().army, allies.first().army);
				} else if (enemies.noUnits()) {
					victory(allies.first().army, enemies.first().army);
				}
			}
			else {
				if (allies.noUnits()) {
					BottomPanel.log("Defeat", "green");
					this.placementPhase = true;
				}
				else if (enemies.noUnits()) {
					BottomPanel.log("Victory", "green");
					this.placementPhase = true;
				}
			}
		}
		else if (placementPhase) {
			if (dragging && selectedUnit != null) selectedUnit.bsp.updateFormationLocation();
		}
		if (leftClicked)
			leftClicked = false;
		if (rightClicked)
			rightClicked = false;
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
		else {
			currentPanel = null;
			mapScreen.getSidePanel().setActive(pb);
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

		int x_int = (int) (x/this.unit_width);
		int y_int = (int) (y/this.unit_width);

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
					centerOffset = new BPoint(this.placementPoint.pos_x - u.getPoint().pos_x, this.placementPoint.pos_y - u.getPoint().pos_y); 

					dragging = true;
					u.bsp.updateFormationLocation();
				}
			}
			else {
				dragging = false;
				centerOffset = new BPoint(0, 0);
			}
		}
	}

	private void rightClick(Point mouse) {
		if (dragging) {
			dragging = false;
			centerOffset = new BPoint(0,0);
			return;
		}


		Unit u = getUnitAt(mouse);

		if (u != null) {
			selectedUnit = u;
			setPanelTo(u);
			mouseOver(mouse);
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

	public void victory(Army winner, Army loser) {
		System.out.println("Battle over!");
		if (winner != kingdom.getPlayer() && loser != kingdom.getPlayer()) System.out.println("Player not involved in victory!!!");

		this.isOver = true;

		boolean didAtkWin;
		if (winner.getParty().player) {
			battle.logDefeat(loser);
			kingdom.getMapScreen().getSidePanel().setStay(false);
			kingdom.getMapScreen().getSidePanel().setActiveArmy(winner);
			if (!playerDefending) didAtkWin = true;
			else didAtkWin = false;
		} else {
			if (playerDefending) didAtkWin = true;
			else didAtkWin = false;
		}


		this.battle.didAtkWin = didAtkWin;

		if (winner.getParty().player) {
			kingdom.getMapScreen().getSidePanel().setStay(false);
		}

		// log(army.getName() + " has won a battle", "cyan");

		// heal retreated soldiers
		for (Unit u : retreated) 
			u.soldier.subparty.healNoMessage(u.soldier);

		boolean loserDestroyed = false;

		// figure out if totally destroyed or if units retreated
		if ((loser.getParty().getHealthySize() <= Battle.DESTROY_THRESHOLD && !loser.getParty().player) || loser.getParty().getHealthySize() <= 0) {
			battle.increaseSpoilsForKill(loser);
			loserDestroyed = true;
			loser.destroy();
		} else battle.increaseSpoilsForRetreat(loser);


		for (Party p : allies.parties) {
			if (!((p.getHealthySize() <= Battle.DESTROY_THRESHOLD && !p.player) || p.getHealthySize() <= 0))
				p.army.setVisible(true);
			p.army.endBattle();
			p.army.setStopped(false);
			p.army.setTarget(null);
			for (Subparty s : p.sub) {
				s.handleBattleEnded();
			}
		}
		for (Party p : enemies.parties) {
			if (!((p.getHealthySize() <= Battle.DESTROY_THRESHOLD && !p.player) || p.getHealthySize() <= 0))
				p.army.setVisible(true);
			p.army.endBattle();
			p.army.setStopped(false);
			p.army.setTarget(null);
			for (Subparty s : p.sub) {
				s.handleBattleEnded();
			}
		}

		loser.waitFor(0);
		winner.forceWait(Battle.WAIT);

		if (battle.siegeOf != null && !battle.siegeOf.isVillage()) {
			System.out.println("managing siege");
			battle.handleSiegeVictory();
		}

		battle.distributeRewards(winner, 1, didAtkWin);
		battle.destroy();

		this.pb = null;

		// free up some memory
		this.battlemap = null;

		// see if player died
		if (loserDestroyed && didAtkWin == playerDefending) mapScreen.playerDeath();

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
					if (inMap(new BPoint(mousePoint.pos_x + i, mousePoint.pos_y + j))) {
						Unit adj = units[mousePoint.pos_y + j][mousePoint.pos_x + i];	
						if (adj == null) continue;
						if (adj.prev_x == mousePoint.pos_x && adj.prev_y == mousePoint.pos_y) return adj;
						if (adj != null && (i == 0 || j == 0)) u = adj;
					}
				}
			}
			return u;
		}

		else return null;
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
		// batch.setColor(Color.WHITE);
		// batch.setColor(kingdom.currentDarkness, kingdom.currentDarkness,
		// kingdom.currentDarkness, 1f);
		super.draw(batch, parentAlpha);

		battlemap.drawTrees(batch); // will probably be above arrows for now

		// System.out.println("bs drawing");
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
		if (this.battlemap.isSnowing()) {
			return SNOW_SLOW;
		}
		if (this.battlemap.isRaining()) {
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
		if (pos_x < 0 || pos_y < 0 || pos_x >= size_x || pos_y >= size_x) return false;
		if (closed[pos_y][pos_x]) return false;
		if (units[pos_y][pos_x] != null) return false;
		return true;
	}
	
	public boolean canPlaceUnitPlacement(int pos_x, int pos_y, int team) {
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
		if (pos_x < 0 || pos_y < 0 || pos_x >= size_x || pos_y >= size_x) return false;
		if (closed[pos_y][pos_x]) return false;
		return true;
	}

	// return true if in wall, false if otherwise
	public boolean insideWall(int pos_x, int pos_y) {
		return battlemap.insideWalls(pos_x, pos_y);
	}

	//	public Array<Unit> getAllies() {
	//		return this.allies.units;
	//	}
	//	public Array<Unit> getEnemies() {
	//		return this.enemies.units;
	//	}
}
