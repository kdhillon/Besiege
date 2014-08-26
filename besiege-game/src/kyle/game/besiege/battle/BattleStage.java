package kyle.game.besiege.battle;

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
import kyle.game.besiege.battle.Formation;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.PanelBattle;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.voronoi.Biomes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public class BattleStage extends Group {
	public Biomes biome;
	public Battle battle;
	private PanelBattle pb;
	public float scale = 1f;
	public float MIN_SIZE = 70;
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

	public static double RETREAT_TIME = 10; // have to wait 5 secs before can retreat
	static final float RAIN_SLOW = .8f;
	static final float SNOW_SLOW = .7f;


	public int MIN_PLACE_X;
	public int MAX_PLACE_X;

	public int MIN_PLACE_Y;
	public int MAX_PLACE_Y;

	// from mouse to register
	public Array<Unit> allies;
	public Array<Unit> enemies;

	public Array<SiegeUnit> siegeUnitsArray;

	public boolean placementPhase;

	private BPoint centerOffset;
	private BPoint placementPoint;
	private BPoint originalPoint;

	Party player;
	Party enemy;

	public boolean siegeAttack;
	public boolean siegeDefense;

	public boolean closed[][]; // open or closed?
	public double slow[][]; // 0 is normal speed, 1 is very slow.
	public Unit[][] units; // units on map
	public SiegeUnit[][] siegeUnits;
	public Array<Unit> retreated; // units that have retreated
	public float heights[][]; // ground floor levels

	public int size_x = 128;
	public int size_y = 128;

	private MapScreen mapScreen;
	private Kingdom kingdom;
	// private Party player;
	public BattleMap battlemap;

	public boolean playerDefending = false;

	public Stance playerStance;
	public Stance enemyStance;

	private boolean mouseOver; // is mouse over Battle screen?
//	private boolean paused;
	public boolean isOver; // is the battle over?
	public double retreatTimer;

	public int unit_width = 16;
	public int unit_height = 16;

	public Unit selectedUnit;
	public Unit currentPanel;

	private boolean leftClicked;
	private boolean rightClicked;
	private Point mouse;
	public boolean dragging;

	public Formation playerFormationChoice;
	public Formation enemyFormationChoice;
	public Formation currentFormation; // I need this for some reason for moving units.
	private Array<Formation> availableFormations;

	private int currentFormationWidth;
	private int currentFormationHeight;

	// take in battle object containing arrays of armies and stuff
	public BattleStage(MapScreen mapScreen, Party player, Party enemy, boolean playerDefending, Location siegeOf) {
		this.mapScreen = mapScreen;

		mouse = new Point(0, 0);

		this.player = player;
		this.enemy = enemy;

		this.kingdom = mapScreen.getKingdom();

		this.playerDefending = playerDefending;
		//		this.playerDefending = false;

		if (playerDefending) {
			playerStance = Stance.DEFENSIVE;
			enemyStance = Stance.AGGRESSIVE;
		}
		else {
			playerStance = Stance.AGGRESSIVE;
			if (Math.random() < .5) 
				enemyStance = Stance.DEFENSIVE;
			else enemyStance = Stance.AGGRESSIVE;
		}

		boolean forceSiege = false;
//		boolean forceSiege = true;

		if ((siegeOf != null && !siegeOf.isVillage()) || forceSiege) {
			siegeDefense = playerDefending;
			siegeAttack = !siegeDefense;
			//siegeAttack = true;
		}

		if (player == null)
			createFakeBattle();

		this.player.army.updatePolygon();
		this.biome = this.player.army.getContaining().biome;

		this.raining = getMapScreen().getKingdom().raining;
		this.currentDarkness = kingdom.currentDarkness;
		this.targetDarkness = kingdom.targetDarkness; // TODO change depending on biome
		
		// int size = Math.max(this.player.getHealthySize(),
		// this.enemy.getHealthySize());
		int size = this.player.getHealthySize() + this.enemy.getHealthySize();
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

		if (playerDefending)
			this.battle = new Battle(mapScreen.getKingdom(), this.enemy.army, this.player.army);
		else 
			this.battle = new Battle(mapScreen.getKingdom(), this.player.army, this.enemy.army);
		
		if (battle.siegeOf == null) this.battle.siegeOf = siegeOf;

		else System.out.println("YES SIEGE");
		
		battle.calcBalancePlayer();
		this.player.army.setBattle(this.battle);
		this.enemy.army.setBattle(this.battle);

		// try this
		pb = new PanelBattle(mapScreen.getSidePanel(), battle);
		pb.battleStage = this;

		allies = new Array<Unit>();
		enemies = new Array<Unit>();

		this.battlemap = new BattleMap(this);
		this.addActor(battlemap);

		this.availableFormations = mapScreen.getCharacter().availableFormations;
		
		// set up default formations
		if (playerDefending) {
			enemyFormationChoice = Formation.FLANKING;
			playerFormationChoice = Formation.DEFENSIVE_LINE;
		}
		else {
			enemyFormationChoice = Formation.DEFENSIVE_LINE;
			playerFormationChoice = Formation.FLANKING;
		}
		if (siegeDefense) playerFormationChoice = Formation.WALL_LINE;
		else if (siegeAttack) enemyFormationChoice = Formation.WALL_LINE;

		this.placementPhase = true;

//		this.paused = true;

		MIN_PLACE_X = SIDE_PAD;
		MAX_PLACE_X = size_x - SIDE_PAD;

		MIN_PLACE_Y = BOTTOM_PAD;
		MAX_PLACE_Y = PLACE_HEIGHT + BOTTOM_PAD;

		// set up orignal base points
		originalPoint =  new BPoint(size_x/2, BOTTOM_PAD + PLACE_HEIGHT/2);
		placementPoint = originalPoint;

		this.retreatTimer = RETREAT_TIME;
		
		addUnits();
	}


	public void createFakeBattle() {
		// player = PartyType.MERCHANT.generate();
		player = kingdom.getPlayer().getParty();
		do {
			enemy = kingdom.getArmies().random().getParty();
		} while (enemy == player);
	}

	public void centerCamera() {
		// translate to center of screen?
		mapScreen.getCamera().translate((this.size_x*this.scale)*this.unit_width/2 - mapScreen.getCamera().position.x, (this.scale*this.size_y*.4f)*this.unit_height - mapScreen.getCamera().position.y);
		//		mapScreen.getCamera().translate(6, 0);

		//mapScreen.getCamera().translate(new Vector2((this.size_x)*this.unit_width/2, (this.size_y)*this.unit_height/2));
	}

	public void centerCameraOnPlayer() {
		centerCameraOnPoint(this.placementPoint);
	}

	public void centerCameraOnPoint(BPoint point) {
		// translate to center of screen?
		mapScreen.getCamera().translate(this.unit_width*this.scale*point.pos_x - mapScreen.getCamera().position.x, this.scale*this.unit_height*point.pos_y - mapScreen.getCamera().position.y);
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
		addParty(player);
		addParty(enemy);
		if (siegeDefense)
			addSiegeUnits(enemy);
		else if (siegeAttack)
			addSiegeUnits(player);
	}

	// add on wall if there is a wall
	private void addParty(Party party) {
		Formation choice;
		if (party == player) {
			choice = playerFormationChoice;
			this.currentFormation = playerFormationChoice;
		}
		else choice = enemyFormationChoice;

		int REINFORCEMENT_DIST = 5;

		Stance partyStance;
		if (party == player) {
			partyStance = playerStance;
			REINFORCEMENT_DIST = -REINFORCEMENT_DIST;
		}
		else partyStance = enemyStance;


		Array<Soldier> infantry = party.getHealthyInfantry();
		Array<Soldier> cavalry = party.getHealthyCavalry();
		Array<Soldier> archers = party.getHealthyArchers();
		Soldier.SoldierType[][] formation = Formation.getFormation(party, choice, size_x, size_y);
		if (party != player) formation = flipVertical(formation);


		int region_height = formation.length;
		int region_width = formation[0].length;

		if (party == player) {
			currentFormationHeight = region_height;
			currentFormationWidth = region_width;
		}

		int base_x = placementPoint.pos_x - region_width/2;
		int base_y = placementPoint.pos_y - region_height/2;

		int team = 0;
		if (party != player) {
			base_x = size_x/2 - region_width/2;
			base_y = (int) (size_y * .7f) - region_height/2;
			team = 1;
			
			
			if (siegeAttack && battlemap.wallBottom > 0) {
				base_y = battlemap.wallBottom + 1;
			}
		}

		for (int i = 0; i < region_height; i++) {
			for (int j = 0; j < region_width; j++) {
				if (formation[i][j] != null) {
					Soldier toAdd;
					if (formation[i][j] == Soldier.SoldierType.INFANTRY) toAdd = infantry.pop();
					else if (formation[i][j] == Soldier.SoldierType.ARCHER) toAdd = archers.pop();
					else toAdd = cavalry.pop();

					if (canPlaceUnit(base_x + j, base_y + i)) {
						Unit unit = new Unit(this, base_x + j, base_y + i, team, toAdd, party);
						unit.stance = partyStance;
						if (party == player && siegeDefense || unit.onWall()) unit.dismount();
						addUnit(unit);
					}
					else if (canPlaceUnit(base_x + j, base_y + i + REINFORCEMENT_DIST)) {
						Unit unit = new Unit(this, base_x + j, base_y + i + REINFORCEMENT_DIST, team, toAdd, party);
						unit.stance = partyStance;
						if (party == player && siegeDefense || unit.onWall()) unit.dismount();
						addUnit(unit);
					}
				}
			}
		}
	}

	private void updatePlayerParty() {
		Formation choice;
		choice = playerFormationChoice;
		this.currentFormation = playerFormationChoice;

		int REINFORCEMENT_DIST = 1;

		Stance partyStance;
		partyStance = playerStance;
		REINFORCEMENT_DIST = -REINFORCEMENT_DIST;

		Array<Unit> infantry = getPlayerInfantry();
		Array<Unit> cavalry = getPlayerCavalry();
		Array<Unit> archers = getPlayerArchers();

		Soldier.SoldierType[][] formation = Formation.getFormation(player, choice, this.size_x, this.size_y);

		int region_height = formation.length;
		int region_width = formation[0].length;

		currentFormationHeight = region_height;
		currentFormationWidth = region_width;

		int base_x = placementPoint.pos_x - region_width/2;
		int base_y = placementPoint.pos_y - region_height/2;

		for (int i = 0; i < region_height; i++) {
			for (int j = 0; j < region_width; j++) {
				if (formation[i][j] != null) {
					Unit toMove = null;
					if (formation[i][j] == Soldier.SoldierType.INFANTRY && infantry.size > 0) toMove = infantry.pop();
					else if (formation[i][j] == Soldier.SoldierType.ARCHER && archers.size > 0) toMove = archers.pop();
					else if (cavalry.size > 0) toMove = cavalry.pop();
					
					if (toMove == null) continue;

					if (canPlaceUnitIgnoreUnits(base_x + j, base_y + i)) {
						toMove.stance = partyStance;
						this.moveUnitTo(toMove, new BPoint(base_x + j, base_y+i));
						// move the unit
					}
					else if (canPlaceUnitIgnoreUnits(base_x + j, base_y + i + REINFORCEMENT_DIST)) {
						toMove.stance = partyStance;
						this.moveUnitTo(toMove, new BPoint(base_x + j, base_y+i + REINFORCEMENT_DIST));						
						// move the unit
					}
				}
			}
		}
		for (Unit unit : allies) {
			units[unit.pos_y][unit.pos_x] = unit;
		}
	}
	
	public Array<Unit> getPlayerInfantry() {
		Array<Unit> toReturn = new Array<Unit>();
		for (Unit u : allies) 
			if (u.soldier.getType() == Soldier.SoldierType.INFANTRY) toReturn.add(u);
		return toReturn;
	}
	public Array<Unit> getPlayerArchers() {
		Array<Unit> toReturn = new Array<Unit>();
		for (Unit u : allies) 
			if (u.soldier.getType() == Soldier.SoldierType.ARCHER) toReturn.add(u);
		return toReturn;
	}
	public Array<Unit> getPlayerCavalry() {
		Array<Unit> toReturn = new Array<Unit>();
		for (Unit u : allies) 
			if (u.soldier.getType() == Soldier.SoldierType.CAVALRY) toReturn.add(u);
		return toReturn;
	}

	public Formation getNextFormation() {
		int index = availableFormations.indexOf(this.playerFormationChoice, true);
		index++;
		if (index >= availableFormations.size) index = 0;
		return availableFormations.get(index);
	}
	public void toNextFormation() {
		changePlayerFormation(getNextFormation());
	}
	
	public void changePlayerFormation(Formation formation) {
		this.playerFormationChoice = formation;
		this.updateFormationLocation();
	}
	
	public String getPlayerStanceString() {
		if (this.playerStance == Stance.AGGRESSIVE) return "Aggressive";
		else if (this.playerStance == Stance.DEFENSIVE) return "Defensive";
		else return null;
	}

	private void addSiegeUnits(Party party) {
		if (party == player) {
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

	private boolean inMap(BPoint p) {
		return p.pos_x < size_x &&
				p.pos_y < size_y && 
				p.pos_x >= 0 && 
				p.pos_y >= 0;
	}

	public void retreatAll(boolean player) {
		if (retreatTimer >= 0) return; // can't retreat unless proper time
		
		if (player) {
			for (Unit unit : allies) {
				unit.retreating = true;
			}
		} 
		else{
			for (Unit unit : enemies) {
				unit.retreating = true;
			}
		} 
	}

	public void chargeAll(boolean player) {
		setStanceForParty(player, Stance.AGGRESSIVE);
	}
	
	public void setStanceForParty(boolean player, Stance stance) {
		if (player) {
			playerStance = stance;
			for (Unit unit : allies) 
				unit.stance = stance;
		} 
		else{
			enemyStance = stance;
			for (Unit unit : enemies) 
				unit.stance = stance;
		} 
	}
	
	public void togglePlayerStance() {
		if (playerStance == Stance.AGGRESSIVE) setStanceForParty(true, Stance.DEFENSIVE);
		else if (playerStance == Stance.DEFENSIVE) setStanceForParty(true, Stance.AGGRESSIVE);	
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
			this.retreatTimer -= delta;
			if ((playerDefending && battle.balanceA < Battle.RETREAT_THRESHOLD/2) || (!playerDefending && battle.balanceD < Battle.RETREAT_THRESHOLD/2)) {
				retreatAll(false);
			}
			if ((playerDefending && battle.balanceA > CHARGE_THRESHOLD) || (!playerDefending && battle.balanceD > CHARGE_THRESHOLD)) {
				chargeAll(false);
			}
			
			if (allies.size == 0) {
				victory(enemy.army, player.army);
			} else if (enemies.size == 0) {
				victory(player.army, enemy.army);
			}
		}
		else if (placementPhase) {
			if (dragging) updateFormationLocation();
		}
		if (leftClicked)
			leftClicked = false;
		if (rightClicked)
			rightClicked = false;
	}

	public void damageWallAt(int pos_x, int pos_y, int damage) {
		battlemap.damageWallAt(pos_x, pos_y, damage);
	}
	
	private void mouseOver(Point mouse) {
		Unit u = getUnitAt(mouse);
		// if (d.getType() != 0)
		if (u != null)
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

	private BPoint mouseToPoint() {
		float x = this.mouse.getX();
		float y = this.mouse.getY();

		int x_int = (int) (x/scale/this.unit_width);
		int y_int = (int) (y/scale/this.unit_width);

		return new BPoint(x_int, y_int);
	}


	// put formation in proper place
	private void updateFormationLocation() {
		// calculate 
		//		if (this.originalPlacePoint.pos_x == this.mou)
		BPoint mousePoint = mouseToPoint();
		//		if (!inMap(mousePoint)) return;

		if (!dragging) {
			// just pretend that it hasn't been touched
			centerOffset = new BPoint(0, 0);
			mousePoint = placementPoint;
			System.out.println("null center");
		}

		if (this.currentFormation == this.playerFormationChoice && mousePoint.pos_x == this.placementPoint.pos_x - centerOffset.pos_x && mousePoint.pos_y == this.placementPoint.pos_y - centerOffset.pos_y) return;
		mousePoint = centerInPlacementRegion(mousePoint);

		this.updatePlayerParty();

//		this.removeParty(player);
		this.placementPoint = new BPoint(mousePoint.pos_x + centerOffset.pos_x, mousePoint.pos_y + centerOffset.pos_y);
//		this.addParty(player);
	}

	private BPoint centerInPlacementRegion(BPoint mousePoint) {
		BPoint center = new BPoint(mousePoint.pos_x + centerOffset.pos_x, mousePoint.pos_y + centerOffset.pos_y);

		if (center.pos_x - currentFormationWidth/2 < MIN_PLACE_X)
			mousePoint.pos_x = MIN_PLACE_X - centerOffset.pos_x + currentFormationWidth/2;

		if (center.pos_y - currentFormationHeight/2 < MIN_PLACE_Y)
			mousePoint.pos_y = MIN_PLACE_Y - centerOffset.pos_y + currentFormationHeight/2;

		if (center.pos_x + currentFormationWidth/2 >= MAX_PLACE_X)
			mousePoint.pos_x = MAX_PLACE_X - 1 - centerOffset.pos_x - currentFormationWidth/2;

		if (center.pos_y + currentFormationHeight/2 >= MAX_PLACE_Y) 
			mousePoint.pos_y = MAX_PLACE_Y - 1 - centerOffset.pos_y - currentFormationHeight/2;

//		System.out.println(center.pos_x + " " + center.pos_y);

		// set position

		return mousePoint;
	}

	private void moveUnitTo(Unit unit, BPoint newPoint) {
		this.units[unit.pos_y][unit.pos_x] = null;
//		this.units[newPoint.pos_y][newPoint.pos_x] = unit;
		unit.pos_x = newPoint.pos_x;
		unit.pos_y = newPoint.pos_y;
		unit.original_x = newPoint.pos_x;
		unit.original_y = newPoint.pos_y;
	}
	
	private void removeParty(Party party) {
		if (party == player) {
			for (int i = 0; i < size_x; i++) {
				for (int j = 0; j < size_y; j++) {
					if (units[j][i] != null && units[j][i].team == 0)
						removeUnit(units[j][i]);
				}
			}
			//				removeUnit(unit);

		}
	}

	private void leftClick(Point mouse) {
		if (this.placementPhase) {
			if (!dragging) {
				Unit u = getUnitAt(mouse);
				if (u != null && u.team == 0) {

					// just calculate distance from center
					centerOffset = new BPoint(this.placementPoint.pos_x - u.getPoint().pos_x, this.placementPoint.pos_y - u.getPoint().pos_y); 

					dragging = true;
					//					updateFormationLocation();
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
		else if (pointer == 4)
			writeUnit();
	}

	public void victory(Army winner, Army loser) {
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
		
		winner.endBattle();
		winner.setStopped(false);
		winner.forceWait(Battle.WAIT);
		if (winner.getParty().player) {
			kingdom.getMapScreen().getSidePanel().setStay(false);
		}

		// log(army.getName() + " has won a battle", "cyan");

		// heal retreated soldiers
		for (Unit u : retreated) 
			u.soldier.party.healNoMessage(u.soldier);

		boolean loserDestroyed = false;

		// figure out if totally destroyed or if units retreated
		if ((loser.getParty().getHealthySize() <= Battle.DESTROY_THRESHOLD && !loser.getParty().player) || loser.getParty().getHealthySize() <= 0) {
			battle.increaseSpoilsForKill(loser);
			loserDestroyed = true;
			loser.destroy();
		} else battle.increaseSpoilsForRetreat(loser);


		if (!winner.isGarrisoned())
			winner.setVisible(true);
		if (!loser.isGarrisoned() && !loserDestroyed) 
			loser.setVisible(true);
		// army.nextTarget(); //

		//		if (winner.getParty().player) {
		//			// army.setStopped(true);
		loser.setStopped(false);
		winner.setStopped(false);
		
		loser.waitFor(0);
		winner.forceWait(Battle.WAIT);
		
		winner.endBattle();
		loser.endBattle();
		
		winner.setTarget(null);
		loser.setTarget(null);

		if (battle.siegeOf != null && !battle.siegeOf.isVillage()) {
			System.out.println("managing siege");
			battle.manageSiege();
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

	public void writeUnit() {
		float x = mouse.getCenterX();
		float y = mouse.getCenterY();
		// mapScreen.out.println(x + " " + y);
	}

	private Unit getUnitAt(Point mouse) {
		BPoint mousePoint = this.mouseToPoint();
		if (inMap(mousePoint)) {
			Unit u = units[mousePoint.pos_y][mousePoint.pos_x];	
			if (u != null) return u;
			
			// search all adjacent points
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					if (inMap(new BPoint(mousePoint.pos_x + i, mousePoint.pos_y + j))) {
						Unit adj = units[mousePoint.pos_x + i][mousePoint.pos_y + j];	
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

	public void addUnit(Unit unit) {
		if (unit.team == 0)
			this.allies.add(unit);
		else if (unit.team == 1)
			this.enemies.add(unit);
		this.addActor(unit);
		//		unit.addActor(unit.weaponDraw);
	}

	public void removeUnit(Unit remove) {
		//		remove.removeActor(remove.weaponDraw);
		if (remove.team == 0)
			allies.removeValue(remove, true);
		else
			enemies.removeValue(remove, true);
		if (remove.inMap())
			if (this.units[remove.pos_y][remove.pos_x] == remove) this.units[remove.pos_y][remove.pos_x] = null;
		this.removeActor(remove);
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
		return getMapScreen().getCamera().zoom;
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
	public boolean canPlaceUnitIgnoreUnits(int pos_x, int pos_y) {
		if (pos_x < 0 || pos_y < 0 || pos_x >= size_x || pos_y >= size_x) return false;
		if (closed[pos_y][pos_x]) return false;
		return true;
	}
	
	// return true if in wall, false if otherwise
	public boolean insideWall(int pos_x, int pos_y) {
		return battlemap.insideWalls(pos_x, pos_y);
	}
}
