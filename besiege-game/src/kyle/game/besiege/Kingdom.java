/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
// group of actors, contains bg, all cities and all armies
package kyle.game.besiege;

import static kyle.game.besiege.Kingdom.getRandom;

import java.util.HashSet;
import java.util.Random;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.ArmyPlayer;
import kyle.game.besiege.army.Bandit;
import kyle.game.besiege.battle.BattleActor;
import kyle.game.besiege.battle.BattleSim;
import kyle.game.besiege.location.Castle;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Ruin;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

public class Kingdom extends Group {
    // Actual values
//	public static int cityCount = 60;
//	public static int castleCount = 0;
//	public static int ruinCount = 5;
//	public static int villageCount = 100;
//	int FACTION_COUNT = 25;

	// Fast values
    public static int cityCount = 15;
    public static int castleCount = 0;
    public static int ruinCount = 10;
    public static int villageCount = 20;
    int FACTION_COUNT = 15;

    public static final double DECAY = .1;
	public static final float HOUR_TIME = 2.5f;
	public static final int BANDIT_FREQ = 1000;
	public static final int MAX_BANDITS = 50;
	public static boolean drawCrests = true;
	public static boolean drawArmyCrests = true;

	public static final float LIGHT_ADJUST_SPEED = .005f; //adjust this every frame when changing daylight
	public static final float NIGHT_FLOAT = .6f;
	public static final float RAIN_FLOAT = .5f;
	public static final float LIGHTNING_FLOAT = 1f;
	private static final float MOUSE_DISTANCE = 10; // distance destination must be from mouse to register
	private static final float LOCATION_MOUSE_DISTANCE = 100; // distance destination must be from mouse to register
	private final int DAWN = 7;
	private final int DUSK = 21;
	private final double RAIN_CHANCE = 5000; // higher is less likely
//	public static final double THUNDER_CHANCE = 1.0/800;
    public static final double THUNDER_CHANCE = 1.0/1000;

    public float clock;
	private int timeOfDay; // 24 hour day is 60 seconds, each hour is 2.5 seconds
	private int day;
	public boolean night; // is nighttime?
	public float currentDarkness; // can be used for LOS
	public float targetDarkness; // for fading
	public boolean raining;

	public Map map;
	transient private MapScreen mapScreen;
	public StrictArray<Faction> factions;

//	private int factionCount;
//	public StrictArray<StrictArray<Integer>> factionRelations; // should break into multiple arrays
	
	private StrictArray<Army> armies;
	public StrictArray<City> cities;
	public StrictArray<Castle> castles;
	public StrictArray<Village> villages;
	public StrictArray<Ruin> ruins;
	
	private StrictArray<BattleActor> battles;
	private ArmyPlayer player;

	public int banditCount;

	public Destination currentPanel;

	private boolean mouseOver; // is mouse over Kingdom screen?
	private boolean paused;

	private boolean leftClicked;
	public boolean rightClicked;
	private Point mouse;

	public boolean initialized;
	public int initCount;
	
	public int citiesLeft;
	public int villagesLeft;
	public int castlesLeft;

	private Fog fog;

    StrictArray<StrictArray<Center>> connectedCenters;

    // for loading kingdom
	public Kingdom() {
		
	}
	
	public Kingdom(MapScreen mapScreen) {
		this.mapScreen = mapScreen;
		initCount = -1;
	}
	
	public void initStep() {
		if (initCount < 0) {
			map = new Map(this, true);

            clock = 12 * HOUR_TIME; // set initial time as noon
			night = false;
			day = 0;
//			startRain();
			//		raining = false;

			this.time(0);
			
			//		currentDarkness = NIGHT_FLOAT;
//			currentDarkness = 0; // fade in
			
			if (this.night)
				this.targetDarkness = NIGHT_FLOAT;
			else this.targetDarkness = 1f;
			
			// testing
			currentDarkness = targetDarkness;

			addActor(map);

			armies = new StrictArray<Army>();
			cities = new StrictArray<City>();
			castles = new StrictArray<Castle>();
			villages = new StrictArray<Village>();
			ruins = new StrictArray<Ruin>();
			battles = new StrictArray<BattleActor>();

			fog = new Fog(this);
			
			System.out.println("initializing factions");
			initializeFactions(this);
			
			initCount++;
			return;
		}
		if (initCount < 1) {
			
		}
		if (initCount < 2) {
			System.out.println("initializing cities");
			if (cities.size == 0) initializeCities();
//			while (scanner.hasNextLine() && (map.cityCorners.size > 0 && map.cityCenters.size > 0)) {
			if (citiesLeft > 0 && (map.cityCorners.size() > 0 && map.cityCenters.size() > 0)) {
				initCitiesStep();
				return;
			}

			System.out.println("Number cities: " + cities.size);
			initCount++;
			return;
		}
		if (initCount < 3) {
			System.out.println("initializing villages");
			if (villages.size == 0) initializeVillages();
			if (villagesLeft > 0 && map.availableCenters.size() > 0) {
				initVillagesStep();
				return;
			}
			System.out.println("Number villages: " + villages.size);

			initCount++;
			return;
		}
		if (initCount < 4) {
			System.out.println("initializing castles");
			if (castles.size == 0) initializeCastles();
			if (castlesLeft > 0 && map.availableCorners.size() > 0) {
				initCastlesStep();
				return;
			}
			System.out.println("Number castles: " + castles.size);
			
			initCount++;
			return;
		}
		if (initCount < 5) {
			System.out.println("initializing ruins");
			initializeRuins();
			initCount++;
			return;
		}
		if (initCount < 6) {
			System.out.println("initializing faction city info");
			initializeFactionCityInfo();
			initCount++;
			return;
		}

		for (int i = 0; i < cities.size; i++)
			cities.get(i).findCloseLocations();
		
		mouse = new Point(0,0);
		banditCount = 0;
		currentPanel = new Point(0,0);
		
		initialized = true;
	}


	@Override
	public void act(float delta) {
		if (mouseOver) {
			if (leftClicked) leftClick(mouse);
			else if (rightClicked) rightClick(mouse);
			else if (BesiegeMain.appType != 1) mouseOver(mouse);
		}

		if (!paused) {
			time(delta);
			super.act(delta);

			if (player != null)
				manageBandits();
			factionAct(delta);
		}
		else {
			for (Location c : cities) {
				c.fireAct(delta);
			}
			for (Location c : villages) {
				c.fireAct(delta);
			}
			for (Location c : ruins) {
				c.fireAct(delta);
			}
			for (Location c : castles) {
				c.fireAct(delta);
			}
		}
		if (leftClicked) leftClicked = false;
		if (rightClicked) rightClicked = false;
		
	}
	

	public void manageBandits() {
		if (banditCount <= MAX_BANDITS && cities.size != 0) {
			if (Math.random() < 1.0/BANDIT_FREQ) {
				City originCity = cities.random();
				//					if (originCity.getVillages().size == 0) 
				createBandit(originCity);
			}
		}
	}
	
	public boolean centerContainsDestination(Center center, Destination army) {
		// checks if in connected (all connected have polygon)
		if (center.polygon != null) {
			Polygon p = center.polygon;
			if (p.contains(army.getCenterX(), army.getCenterY())) {
				return true;
			}
		}
		return false;
	}
	
	// just check adjacent centers!
	public void updateArmyPolygon(Army army) {
		if (army.isGarrison) return;
		if (army.getContaining() != null) {

			// first check if it's left it's previous polygon
			// if not, return, if so, remove from previous container
			if (army.getContaining().polygon != null && army.getContaining().polygon.contains(army.getCenterX(), army.getCenterY())) {
				if (!army.getContaining().armies.contains(army, true)) army.getContaining().armies.add(army);
				return;
			}

			army.getContaining().armies.removeValue(army, true);

			for (Center adjacent : army.getContaining().neighbors) {
				if (centerContainsDestination(adjacent, army)) {
					updateContainingCenter(army, adjacent);
				}
			}
			army.containingCenter = -1;
			//			System.out.println("nothing adjacent found for " + army.getName());
		}
		
		// container is null (slow during initialization stages - plan is to initialize armies with accurate center)
		for (Center center : map.connected) {
			if (center.water) continue;
			Polygon p = center.polygon;
			if (p.contains(army.getCenterX(), army.getCenterY())) {
				updateContainingCenter(army, center);
				//						System.out.println("completely differnt polygon");
				return;
			}
		}
	}

	public void updateContainingCenter(Army army, Center center) {
		center.armies.add(army);
		army.containingCenter = center.index;
		if (army.isPlayer()) {
			System.out.println("ARMY PLAYER");
			fog.playerIn(center);
		}
	}
	
	// update time
	public void time(float delta) {
		clock += delta;
		timeOfDay = (int) ((clock - day*60) / HOUR_TIME);
		if (timeOfDay >= 24) {
			dailyRoutine();
			day++;
		}
		if (timeOfDay >= DAWN && timeOfDay <= DUSK)
			night = false;
		if (timeOfDay <= DAWN || timeOfDay >= DUSK)
			night = true;
		
		if (Math.random() < 1/RAIN_CHANCE) startRain();
		if (raining && Math.random() < .0005) stopRain();
	}
	
	public void startRain() {
		if (raining == true) return;
		raining = true;
		SoundPlayer.startRain();
	}
	
	public void stopRain() {
		if (raining == false) return;
		raining = false;
		SoundPlayer.stopRain();
	}
	
	public void rain() {
		this.targetDarkness = RAIN_FLOAT;
		if (Math.random() < THUNDER_CHANCE) thunder();
	}
	
	private void thunder() {
		this.currentDarkness = LIGHTNING_FLOAT;
        SoundPlayer.playThunder();
	}
	
	public void updateColor(SpriteBatch batch) {
//		System.out.println("target darkness: " + this.targetDarkness);
		if (this.currentDarkness != this.targetDarkness) adjustDarkness();
		
		batch.setColor(this.currentDarkness, this.currentDarkness, this.currentDarkness, 1f);
	}
	
	private void adjustDarkness() {
		if (this.raining) {
			if (this.targetDarkness - this.currentDarkness > this.LIGHT_ADJUST_SPEED) this.currentDarkness += this.LIGHT_ADJUST_SPEED/2;
			else if (this.currentDarkness - this.targetDarkness > this.LIGHT_ADJUST_SPEED) this.currentDarkness -= this.LIGHT_ADJUST_SPEED/2;
		}
		else {
			if (this.targetDarkness - this.currentDarkness > this.LIGHT_ADJUST_SPEED) this.currentDarkness += this.LIGHT_ADJUST_SPEED;
			else if (this.currentDarkness - this.targetDarkness > this.LIGHT_ADJUST_SPEED) this.currentDarkness -= this.LIGHT_ADJUST_SPEED;
		}
	}
	
	// Events that occur every day
	private void dailyRoutine() {
//		System.out.println("day is done");
		
		// testing 
		boolean printArmies = false;
		if (printArmies) {
			System.out.println("total armies: " + armies.size);
			int merchantCount = 0;
			int farmerCount = 0;
			int patrolCount = 0;
			int nobleCount = 0;
			int banditCount = 0;
			for (Army a : armies) {
				if (a.isMerchant()) merchantCount++;
				if (a.isFarmer()) farmerCount++;
				if (a.isPatrol()) patrolCount++;
				if (a.isNoble()) nobleCount++;
				if (a.getFaction() == Faction.BANDITS_FACTION) banditCount++;
			}
			System.out.println("merchants: " + merchantCount);
			System.out.println("farmers: " + farmerCount);
			System.out.println("patrols: " + patrolCount);
			System.out.println("nobles: " + nobleCount);
			System.out.println("bandits: " + banditCount);
			System.out.println();
		}

		// increase village wealth
		for (Village v : villages) {
			v.dailyWealthIncrease();
			v.dailyPopIncrease();
		}
		
		// increase city wealth
		for (City c : cities) {
			c.dailyWealthIncrease(); 
			c.dailyPopIncrease();
		}
		
		// assume fixed war effect for now
//		for (int i = 0; i < factions.size; i++) {
//			Faction f = factions.get(i);
//			f.updateWarEffects();
//		}
		
		player.train();
		
//		printArmyStats();
	}

	public void mouseOverCurrentPoint() {
		Destination d = new Point(mouse.getCenterX(), mouse.getCenterY());
//		if (d.getType() == Destination.DestType.POINT)
			this.setPanelTo(d);
//		else {
//			System.out.println("Not mousing over a point!!");
//		}

	}

	private void mouseOver(Point mouse) {
		Destination d = getDestAt(mouse);
		//		if (d.getType() != 0)
		// TODO replace this with actor inputlisteners
		if (d.getType() != Destination.DestType.LOCATION && d.getType() != Destination.DestType.POINT && d.getType() != Destination.DestType.ARMY)
			this.setPanelTo(d);
		//			d.setMouseOver(true);
	}

	public void setPanelTo(Destination newPanel) {
//				if (currentPanel == null) System.out.println("currentPanel is null");
		// makes sure not to set the same panel a lot, and makes sure not to return to previous for every single point
		if (newPanel != currentPanel && (newPanel.getType() != Destination.DestType.POINT || currentPanel.getType() != Destination.DestType.POINT)) {
			getMapScreen().getSidePanel().setActiveDestination(newPanel);
			currentPanel = newPanel;
			System.out.println("setting panel to " + newPanel.getName());
		}
	}

	private void rightClick(Point mouse) {
		Destination d = getDestAt(mouse);
//		System.out.println("left click");
		
//		if (d == player) System.out.println("selected player");

		if (player.isGarrisoned() && d == player.getGarrisonedIn()) {
			System.out.println("trying to go to city you're already in");
			return;
		}
		
		if (player.forceWait) {
			System.out.println("player forced wait");return;
		}
		
		if (d != player && !player.isInBattle()) {
			player.setWaiting(false);

			if (!player.setTarget(d)) {
				System.out.println("can't travel to dest");
				return; 
			}
			paused = false;
//			player.setForceWait(false);
//			getMapScreen().shouldCenter = true;
			if (player.isGarrisoned()) {
				if (d != player.getGarrisonedIn()) {
			
//				System.out.println("player getting new target and player garrisoned so ejecting");
//				System.out.println(player.getGarrisonedIn().getName());
					player.eject();
				//paused = true;
				}
			}
			
			
			if (d.getType() == Destination.DestType.POINT) {
				getMapScreen().getSidePanel().setDefault(false);
			}
			//if (player.getTarget() != null) System.out.println("target = " + player.getTarget().getName());
		}
	}
	
	private void leftClick(Point mouse) {		
		Destination d = getDestAt(mouse);
		System.out.println("leftclick()");
		
		mapScreen.getSidePanel().setHardStay(false);

		if (d.getType() == Destination.DestType.BATTLE) { //battle
			BattleActor battle = (BattleActor) d;
			getMapScreen().getSidePanel().setActiveBattle(battle.getBattle());
			mapScreen.getSidePanel().setHardStay(true);
		}
		if (d.getType() == Destination.DestType.ARMY) { // army
			Army destinationArmy = (Army) d;
			getMapScreen().getSidePanel().setActiveArmy(destinationArmy);
			mapScreen.getSidePanel().setHardStay(true);
		}
		if (d.getType() == Destination.DestType.LOCATION) {
			Location location = (Location) d;
			getMapScreen().getSidePanel().setActiveLocation(location);
			mapScreen.getSidePanel().setHardStay(true);
		}
				
		// check if a center 
		if (d.getType() == Destination.DestType.POINT) {
			Center containing = null;
			Point testPoint = (Point) d;
			for (Center center : map.connected) {
			    if (!center.discovered && mapScreen.fogOn) continue;
				Polygon p = center.polygon;
				if (p.contains(testPoint.getCenterX(), testPoint.getCenterY())) {
					containing = center;
					break;
				}
			}
			if (containing != null) {
				getMapScreen().getSidePanel().setActiveCenter(containing);
				mapScreen.getSidePanel().setHardStay(false);
			}
			else {
				// just for fun, do water as well
//				for (Center center : map.vg.centers) {
//					Polygon p = center.polygon;
//					if (p.contains(testPoint.getCenterX(), testPoint.getCenterY())) {
//						containing = center;
//						System.out.println("setting center to water");
//						break;
//					}
//				}
//				if (containing != null)
//					getMapScreen().getSidePanel().setActiveCenter(containing);
				
//				System.out.println("containing is null");
//				else 
				getMapScreen().getSidePanel().setDefault(true);
				
				// deselect 
			}
		}		
	}

	public void click(int pointer) {
		this.armies.iterator();
		
//		if (pointer == 0)
//			ed = true;
//		else if (pointer == 1) 
//			rightClicked = true;
//		else if (pointer == 4)
//			writeCity();
		// try switching them
		if (pointer == 0)
			leftClicked = true;
		else if (pointer == 1) 
			rightClicked = true;
		else if (pointer == 4)
			writeCity();
	}
	
	public void writeCity() {
		float x = mouse.getCenterX();
		float y = mouse.getCenterY();
		//		getMapScreen().out.println(x + " " + y);
	}

	private Destination getDestAt(Point mouse) {
		Destination dest = new Point(mouse.getCenterX(), mouse.getCenterY());
		for (City city : cities) {
		    if (getMapScreen().fogOn && !city.isDiscovered()) continue;
			if (Kingdom.distBetween(city, mouse) <= LOCATION_MOUSE_DISTANCE * Location.getAdjustedZoom(this))
				dest = city;
		}
		for (Village village : villages) {
            if (getMapScreen().fogOn && !village.isDiscovered()) continue;
            if (Kingdom.distBetween(village, mouse) <= LOCATION_MOUSE_DISTANCE)
				dest = village;
		}
		for (Castle castle : castles) {
            if (getMapScreen().fogOn && !castle.isDiscovered()) continue;
            if (Kingdom.distBetween(castle, mouse) <= LOCATION_MOUSE_DISTANCE) {
                dest = castle;
            }
		}
		for (Ruin ruin : ruins) {
            if (getMapScreen().fogOn && !ruin.isDiscovered()) continue;
            if (Kingdom.distBetween(ruin, mouse) <= LOCATION_MOUSE_DISTANCE)
				dest = ruin;
		}
		for (Army army : armies) {
			if (army.isVisible() && Kingdom.distBetween(army, mouse) <= MOUSE_DISTANCE)
				if (getMapScreen().losOn) {
					if (Kingdom.distBetween(army, player) <= player.getLineOfSight())
						dest = army;
				}
				else
					dest = army;
		}
		for (BattleActor battle : battles) {
			if (battle.isVisible() && Kingdom.distBetween(battle, mouse) <= MOUSE_DISTANCE) {
				dest = battle;
			}
		}

		return dest;
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		//batch.setColor(Color.WHITE);
		if (!raining) {
			if (night) targetDarkness = NIGHT_FLOAT;
			else targetDarkness = 1f;
		}
	
		this.updateColor(batch);

		map.draw(batch, parentAlpha);

		// leak is not here
		super.draw(batch, parentAlpha);

		if (drawCrests) {
			for (City c : cities)
				c.drawCrest(batch);
			for (Village v : villages)
				v.drawCrest(batch);
			for (Castle c : castles)
				c.drawCrest(batch);
		}
		if (drawArmyCrests) 
			for (Army a : armies)
				a.drawCrest(batch);
		for (City c : cities)
			c.drawText(batch);
		for (Village v : villages) 
			v.drawText(batch);
		for (Castle c : castles) 
			c.drawText(batch);
		for (Ruin r : ruins) 
			r.drawText(batch);
	}
	

	public void initializeFactions(Kingdom kingdom) {
		RandomCrestGenerator rcg = new RandomCrestGenerator();
		
		factions = new StrictArray<Faction>();

//		factionRelations = new StrictArray<StrictArray<Integer>>();
		//		factionMilitaryAction = new StrictArray<StrictArray<Integer>>();

		// add player faction (index 0) 
		
		Faction.BANDITS_FACTION = new Faction(this, "Bandit", Crest.BANDIT_CREST, Color.BLACK);
		Faction.ROGUE_FACTION = new Faction(this,"Rogue", Crest.ROGUE_CREST, Color.BLACK);
		
		addFaction(Faction.ROGUE_FACTION);
		// add bandits faction (index 1)
		addFaction(Faction.BANDITS_FACTION);	

		for (int i = 0; i < FACTION_COUNT; i++) {
			createFaction();
		}

		for (int i = 0; i < factions.size; i++) {
			Faction f = factions.get(i);
			f.kingdom = kingdom;
			f.initializeRelations();
		}
		Faction.BANDITS_FACTION.goRogue();
		Faction.ROGUE_FACTION.goRogue();
		Faction.initialized = true;
	}
	
	public void factionAct(float delta) {
		factions.shrink();
		for (int i = 0; i < factions.size; i++)
			factions.get(i).act(delta);
	}
	public void createFaction() {
		Faction faction = new Faction(this);
		factions.add(faction);
		faction.index = factions.indexOf(faction, true);
	}
	public void addFaction(Faction faction) {
		factions.add(faction);
		faction.index = factions.indexOf(faction, true);		
	}
	
	
	/** Initialize each faction's list of hostile cities, faction control map 
	 *  of centers, and village control. 
	 */
	public void initializeFactionCityInfo() {
//		System.out.println("initializing faction city info");
		for (Faction f : factions) { 
//			System.out.println("initializeing fci! " + f.name);
			f.initializeCloseLocations();
			f.centers.clear();
		}
		// update each faction's centers
		updateFactionCenters();
	}
	/** Update each faction's list of hostile cities, faction control map 
	 *  of centers, and village control. 
	 */
	public void updateFactionCityInfo() {
//		System.out.println("updating faction city info");
		for (Faction f : factions) { 
			f.updateCloseLocations();
			f.centers.clear();
		}
		updateFactionCenters();
	}
	
	/** updates faction centers and village factions
	 */
	private void updateFactionCenters() {
		// update each faction's centers
		for (Center c : getMap().connected)  {
			// remove later to include lakes
			boolean shouldIncludeLakes = true;
			if (!shouldIncludeLakes) if (c.water) continue;
			calcInfluence(c);
		}
		getMap().calcBorderEdges();
		for (Faction f : factions) {
			f.territory.clear();
			connectedCenters = MapUtils.calcConnectedCenters(f.centers);
			for (StrictArray<Center> centers : connectedCenters) {
				//				System.out.println("working");
				
				f.territory.add(MapUtils.centersToPolygon(centers));
			}
		}
		for (Village v : villages) {
			v.changeFaction(getInfluenceAt(v.getCenter()));
		}
	}

	/**
	 * figures out which faction has the most influence on this center,
	 * and adds it to that faction's "centers" array
	 * 
	 * THIS IS WHATS MAKING IT SO SLOW!!!
	 * @param center
	 */
	private void calcInfluence(Center center) {
		Point centerPoint = new Point(center.loc.x, Map.HEIGHT - center.loc.y);
		if (center.water) return;

		Faction bestFaction = null;
		double minDist = Double.POSITIVE_INFINITY;

		// Actually, have a minimum distance of influence.
        minDist = 500f;

        System.out.println("center: " + centerPoint.toString());

        // TODO even with fast mode, this is one of the slowest parts of the pipeline
        boolean FAST_MODE = true;

		// go through factions and calc influence, saving it if it's the highest
		for (Faction faction : factions) {
			double dist; // score is inverse of city distance (birds eye, but should be path-based)

			for (City city : faction.cities) {
			    if (FAST_MODE)
    				dist = distBetween(city, centerPoint);
			    else
                    dist = pathDistBetween(city, centerPoint);

                if (dist < minDist) {
					minDist = dist;
					bestFaction = faction;
				}
			}
			for (Castle castle : faction.castles) {
				// path dist between is pretty slow rn
				if (FAST_MODE)
    				dist = distBetween(castle, centerPoint);
                else
                    dist = pathDistBetween(castle, centerPoint);
                if (dist < minDist) {
					minDist = dist;
					bestFaction = faction;
				}
			}
		}

		if (bestFaction != null) {
			bestFaction.centers.add(center);
			center.faction = bestFaction;
		}
	}

	public Faction getInfluenceAt(Center center) {
		for (Faction f : factions) {
			if (f.centers.contains(center, true)) return f;
		}

		System.out.println("no one controls that center");
		return null;
	}
	
	
	public void restoreFactionCrests() {
		for (Faction faction : factions) {
			faction.restoreCrest();
		}
	}

//	public int getRelations(Faction faction1, Faction faction2) {
//		if (faction1 == null || faction2 == null) return 0;
//		return factionRelations.get(faction1.index).get(faction2.index);
//	}
	
	public int calcRelations(Faction faction1, Faction faction2) {
		if (faction1 == null || faction2 == null) return 0;
		// calculate relations between the two faction
		return faction1.calcRelations(faction2);
		
	}
	
	
//	public boolean isAtWar(Faction faction1, Faction faction2) {
//		if (faction1 == faction2) return false;
//		return (getRelations(faction1, faction2) < Faction.WAR_THRESHOLD);
//	}
//	public void setAtWar(Faction faction1, Faction faction2) {
//		//		factionRelations[faction1][faction2] = WAR_THRESHOLD-1;
//		//		factionRelations[faction2][faction1] = WAR_THRESHOLD-1;
//		factionRelations.get(faction1.index).set(faction2.index, Faction.INIT_WAR_RELATION);
//		factionRelations.get(faction2.index).set(faction1.index, Faction.INIT_WAR_RELATION);
//	}
//	public void setNeutral(Faction faction1, Faction faction2) {
//		factionRelations.get(faction1.index).set(faction2.index, 0);
//		factionRelations.get(faction2.index).set(faction1.index, 0);
//
//		//		factionRelations[faction1][faction2] = 0;
//		//		factionRelations[faction2][faction1] = 0;
//	}

//	public void makePeace(Faction faction1, Faction faction2) {
//		//		BottomPanel.log(faction1.name + " and " + faction2.name + " have signed a peace agreement!", "magenta");
//		
////		setNeutral(faction1, faction2);
//	}
//	public void declareWar(Faction faction1, Faction faction2) {
//		//		BottomPanel.log(faction1.name + " and " + faction2.name + " have declared war!", "magenta");
//		setAtWar(faction1, faction2);
//	}

//	public void changeRelation(Faction faction1, Faction faction2, int delta) {
////		int initialRelation = factionRelations.get(faction1.index).get(faction2.index);
////		int newRelation;
////		if (initialRelation + delta >= Faction.MAX_RELATION) newRelation = Faction.MAX_RELATION;
////		else if (initialRelation + delta <= Faction.MIN_RELATION) newRelation = Faction.MIN_RELATION;
////		else newRelation = initialRelation + delta;
////		if (initialRelation >= Faction.WAR_THRESHOLD && newRelation < Faction.WAR_THRESHOLD) ;
////		//			BottomPanel.log(faction1.name + " and " + faction2.name + " have declared war!", "magenta");
////		else if (initialRelation < Faction.WAR_THRESHOLD && newRelation >= Faction.WAR_THRESHOLD) 
////			makePeace(faction1, faction2);
////		factionRelations.get(faction1.index).set(faction2.index, newRelation);
//		
//	}

	public static void calcAllRelations() {
//		for (Faction f : factions) { 
//			for (int i = 0; i < factions.size; i++) {
//				int base = 0;
//				//				base += factionMilitaryAction.get(f.index).get(i); // Military actions
//				base += f.getCloseCityEffect(factions.get(i));	   // Borders
//				base += factions.get(i).getCloseCityEffect(f);	   // (Borders is 2-way)
//			}
//		}
	}

	public Faction get(int index) {
		return factions.get(index);
	}
	
	
	public void addCity(City newCity) {
		cities.add(newCity);
		
		// initialize nobles
		newCity.getFaction().createNobleAt(newCity);
		
		addActor(newCity);
	}

//	public void assignFactionCenters() {
//		for (Faction faction : Faction.factions) {
//			faction.faction_center_x = (int) (Math.random() * Map.WIDTH);
//			faction.faction_center_y = (int) (Math.random() * Map.HEIGHT);
//		}
//	}
	
	public void initializeCities() {	
//		StrictArray<String> cityArray = Assets.cityNames;
		//		Scanner scanner = Assets.cityList;
		
//		int currentFaction = 2; // no bandits or player 
//		int factionRepeats = 0;

//		int maxRepeats = (int) (Math.random()*3) + 1; // max 2, min 0
		int maxRepeats = 4;
		citiesLeft = cityCount;
	}
	
	public void initCitiesStep() {
//		System.out.println("adding city");
		City city = null;
		int x, y;
		boolean useCorner = true; // later allow to use center
		if (Math.random() > .5)
			useCorner = false; // later allow to use center
		
		Corner corner = null;
		Center center = null;
		
		// make with corner
		if (useCorner) {
			
			corner = Kingdom.getRandom(map.cityCorners);
			x = (int) corner.getLoc().x;
			y = (int) corner.getLoc().y;
			map.cityCorners.remove(corner);
			map.availableCorners.remove(corner);

			// remove neighboring corners 2 levels deep!
			for (Corner adj : corner.adjacent) {
				if (adj != null) {
					map.cityCorners.remove(adj);
					map.availableCorners.remove(adj);
					for (int i = 0; i < adj.adjacent.size(); i++) {
						// two levels deep
						if (adj.adjacent.get(i) != null) 
							map.cityCorners.remove(adj.adjacent.get(i));
					}
				}
			}
			// remove neighboring centers
			for (Center adj : corner.touches) {
				if (adj != null) {
					map.cityCenters.remove(adj);
					map.availableCenters.remove(adj);
				}
			}
		}
		// make with center
		else {
			center = Kingdom.getRandomCenter(map.cityCenters);
			x = (int) center.loc.x;
			y = (int) center.loc.y;
			map.cityCenters.remove(center);
			map.availableCenters.remove(center);

			for (Corner adj : center.corners) {
				if (adj != null) {
					map.cityCorners.remove(adj);
					//						map.availableLocationSites.remove(adj.loc, false);
					for (int i = 0; i < adj.adjacent.size(); i++) {
						// two levels deep
						if (adj.adjacent.get(i) != null)  {
							map.cityCorners.remove(adj.adjacent.get(i));
							//								map.availableLocationSites.remove(adj.adjacent.get(i).loc, false);
						}
					}
				}
			}
			for (Center adjCenter : center.neighbors) {
				if (adjCenter != null) {
					map.cityCenters.remove(adjCenter);
					//						map.availableLocationSites.remove(adjCenter.loc, false);
				}
			}
			int index = -1;
		}
		
		// calculate the closest faction center
		double closestDistance = 99999999;
		Faction closestFaction = null;
		for (Faction f : factions) {
			if (f == Faction.ROGUE_FACTION || f == Faction.BANDITS_FACTION) continue;
			if (f == null) continue;
			
			if (!f.hasNewCenter()) {
				f.faction_center_x = x;
				f.faction_center_y = Map.HEIGHT-y;
				closestFaction = f;
				break;
			}
			
			double distance = pathDistBetween(new Point(f.faction_center_x, f.faction_center_y), new Point(x, Map.HEIGHT-y));
			if (distance < closestDistance) {
				closestFaction = f;
				closestDistance = distance;
			}	
		}
		
		if (closestFaction == null) {
		    System.out.println("NULL CLOSEST FACTION -- This is because citycenters contains a center not connected to the center of the world map (even though we checked all of them). weird!");
		    return;
        }
		
		city = new City(this, null, -1, closestFaction, x, Map.HEIGHT-y, center, corner);

		addCity(city);
		citiesLeft--;
		System.out.println("citiesLeft: " + citiesLeft);
	}

	// villages only spawn at centers (for influence purposes)
	public void initializeVillages() {
		//		Scanner scanner = Assets.villageList;
		villagesLeft = villageCount;
		//		while (scanner.hasNextLine() && map.availableCenters.size > 0) {
		
	}
	
	public void initVillagesStep() {
		Center center = getRandomCenter(map.availableCenters);
		map.availableCenters.remove(center);

		//			Village village = new Village(this, scanner.next(), -1, null, (float) center.loc.x, (float) (Map.HEIGHT-center.loc.y), VILLAGE_START_WEALTH);			
		Village village = new Village(this,  null, -1, null, (float) center.loc.x, (float) (Map.HEIGHT-center.loc.y), center);
		villages.add(village);
//		village.center = center.index;
		addActor(village);
		villagesLeft--;
	}
	
	public void initializeCastles() {
		castlesLeft = castleCount;
		//		while (scanner.hasNextLine() && map.availableCenters.size > 0) {
		
	}

	public void initCastlesStep() {
		Corner corner;
		do {
			//System.out.println(map.availableCorners.size);
			corner = getRandom(map.availableCorners);
			map.availableCorners.remove(corner);
		} while (corner == null && map.availableCorners.size() > 0);
		
		if (corner == null) {
			System.out.println("no corners left");
			castlesLeft = 0;
			return;
		}
		
		float x = (float) corner.getLoc().x;
		float y = (float) (Map.HEIGHT-corner.getLoc().y);
		
		double closestDistance = Double.MAX_VALUE;
		Faction closestFaction = null;
		for (Faction f : factions) {
			if (f == Faction.ROGUE_FACTION || f == Faction.BANDITS_FACTION) continue;
			if (f == null) continue;
			double distance = this.pathDistBetween(new Point(f.faction_center_x, f.faction_center_y), new Point(x, y));
			if (distance < closestDistance) {
				closestFaction = f;
				closestDistance = distance;
			}	
		}
		
		if (closestFaction == null) {
		    System.out.println("NULL CLOSEST FACTION");
		    throw new AssertionError();
        }
		
		Castle castle = new Castle(this, null, -1, closestFaction, x, y, null, corner);
		castles.add(castle);

		addActor(castle);
		castlesLeft--;
	}
	
	public void initializeRuins() {		
		int ruinsLeft = ruinCount;
		while (ruinsLeft > 0 && map.availableCorners.size() > 0) {
			Corner corner;
			do {
				//System.out.println(map.availableCorners.size);
				corner = getRandom(map.availableCorners);
				map.availableCorners.remove(corner);
			} while (corner == null && map.availableCorners.size() > 0);
			
			if (corner == null) {
				System.out.println("no corners left");
				break;
			}
			
			float x = (float) corner.getLoc().x;
			float y = (float) (Map.HEIGHT-corner.getLoc().y);
			
			
			double closestDistance = Double.MAX_VALUE;
			Faction closestFaction = null;
			for (Faction f : factions) {
				if (f == Faction.ROGUE_FACTION || f == Faction.BANDITS_FACTION) continue;
				if (f == null) continue;
				double distance = Kingdom.distBetween(new Point(f.faction_center_x, f.faction_center_y), new Point(x, y));
				if (distance < closestDistance) {
					closestFaction = f;
					closestDistance = distance;
				}	
			}
			
			if (closestFaction == null) System.out.println("NULL CLOSEST FACTION");
			
			Ruin ruin = new Ruin(this, null, -1, x, y, null, corner);
			ruins.add(ruin);

			addActor(ruin);
			ruinsLeft--;
		}
		
		System.out.println("Number ruins: " + ruins.size);
	}
	
	// Was adding nobles more than once previously.
	public void addArmy(Army add) {
		if (!armies.contains(add, true)) {
			armies.add(add);
		}
		if (!this.getChildren().contains(add, true)) {
			addActor(add);
			// add the crestdraw for that army

			// the rotation on this guy needs to be adjusted in the same way
			// that a location is updated.
		}
		//add.postAdd();
	}
	public void addPlayer() {
		System.out.println("adding player");
		Faction faction = factions.get(5);
//		faction = ;
		Center center = map.reference;
		
		int pos_x = (int) center.loc.x;
		int pos_y = (int) (Map.HEIGHT-center.loc.y);
		
		if (faction.centers.size > 0) {
			center = faction.centers.random();
			while (center.water) {
				System.out.println("player center is water");
				center = faction.centers.random();
			}
			
			pos_x = (int) center.loc.x;
			pos_y = (int) (Map.HEIGHT-center.loc.y);
			System.out.println("player created in faction center");
		} else System.out.println("no centers!");
		
		player = new ArmyPlayer(this, faction, pos_x, pos_y, 6);
		player.containingCenter = center.index;
		
		addArmy(player);
		fog.playerIn(center);
		//		player.initializeBox(); // otherwise line of sight will be 0!
		//getMapScreen().center(); doesn't do anything bc of auto resize
		//player.getParty().wound(player.getParty().getHealthy().random());
		//		getMapScreen().getFog().updateFog();
	}
	public void createBandit(City originCity) {
		//get a good bandit location, out of player's LOS, away from other armies, etc.
		Bandit bandit = new Bandit(this, "Bandit", 0, 0);
		float posX, posY;
		//		float posX = 2048;
		//		float posY = 2048;
		Point p = new Point(0,0);
		int count = 0;
		do {
			count++;
			posX = (float) (originCity.getCenterX() + (Math.random()+0.5)*bandit.getLineOfSight());
			posY = (float) (originCity.getCenterY() + (Math.random()+0.5)*bandit.getLineOfSight());
			p.setPos(posX, posY); 
			//			System.out.println("creating bandit spot");
		} while ((map.isInWater(p) || Kingdom.distBetween(p, player) <= player.getLineOfSight()) && count < 10); // makes sure bandit is out of sight of player!
		if (count == 10) return;
		bandit.setDefaultTarget(cities.random());
		bandit.setPosition(posX, posY);
		//		System.out.println("new bandit created at " + origin.getName() + posX + "  " + posY);
		this.addArmy(bandit);
		banditCount++;
	}
	
	public StrictArray<Location> getAllLocationsCopy() {
		StrictArray<Location> locations = new StrictArray<Location>();
		locations.addAll(cities);
		locations.addAll(castles);
		locations.addAll(villages);
		locations.addAll(ruins);
		return locations;
	}
	
	public void removeArmy(Army remove) {
//		System.out.println("removing " + remove.getName());
//		if (remove.getParty().player) System.out.println("kingdom removing player");
		armies.removeValue(remove, true);
		if (remove.getContaining() != null)
			remove.getContaining().armies.removeValue(remove, true);
		
		remove.remove();
		this.removeActor(remove);
		armies.removeValue(remove, true);
		this.removeActor(remove);
	}

	public StrictArray<Army> getArmies() {
		return armies;
	}
	public void setArmies(StrictArray<Army> armies) {
		this.armies = armies;
	}
	public void addBattle(BattleActor battle) {
		if (!battles.contains(battle, true)) {
			System.out.println("Adding battle " + battle.getName());
			battles.add(battle);
			addActor(battle);
		}
	}
	public void removeBattle(BattleActor battle) {
		System.out.println("removing battle: " + battle.getName());
		battles.removeValue(battle, true);
		removeActor(battle);
	}
	public StrictArray<City> getCities() {
		return cities;
	}
	public ArmyPlayer getPlayer() {
		return player;
	}

	public void setPaused(boolean paused) {
//		System.out.println("setting paused: " + paused);
		this.paused = paused;
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
	public void setMapScreen(MapScreen mapScreen) {
		this.mapScreen = mapScreen;
	}

	public float clock() {
		return clock;
	}
	public void toggleNight() {
		night = !night;
	}
	//	public void setToNight(SpriteBatch b) {
	//		b.setColor(NIGHT_FLOAT, NIGHT_FLOAT, NIGHT_FLOAT, 1f);
	//	}
	//	public void setToDay(SpriteBatch b) {
	//		b.setColor(1f, 1f, 1f, 1f);
	//	}
	
	public int getTime() {
		return timeOfDay;
	}
	public int getTotalHour() {
		return (int) (clock/HOUR_TIME);
	}
	public int getDay() {
		return day;
	}
	public float getZoom() {
		return ((OrthographicCamera) getMapScreen().getCamera()).zoom;
//		return zoom;
//		return 1;
	}
	public void setMouseOver(boolean b) {
		mouseOver = b;
	}
	public boolean isPaused() {
		return paused;
	}
	public Map getMap() {
		return map;
	}
	
	public double pathDistBetween(Destination d1, Destination d2) {
//		System.out.println("path dist between");
		Path path = new Path(d1, this);
		path.calcPathTo(d2, false);
		double ret =  path.getRemainingDistance();
//		if (ret == 0) System.out.println("RET IS 0 KiNGDOM");
		return ret;
	}

	public static double distBetween(Destination d1, Destination d2) {
		// TODO optimize by computing getCenter only once per
		return Math.sqrt((d1.getCenterX()-d2.getCenterX())*(d1.getCenterX()-d2.getCenterX())+(d1.getCenterY()-d2.getCenterY())*(d1.getCenterY()-d2.getCenterY()));
	}
	// should be slightly faster than above
	public static double sqDistBetween(Destination d1, Destination d2) {
		return (d1.getCenterX()-d2.getCenterX())*(d1.getCenterX()-d2.getCenterX())+(d1.getCenterY()-d2.getCenterY())*(d1.getCenterY()-d2.getCenterY());
	}
	
	public static Corner getRandom(HashSet<Corner> myHashSet) {
		int size = myHashSet.size();
		int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
		int i = 0;
		for(Corner obj : myHashSet)
		{
		    if (i == item)
		        return obj;
		    i++;
		}
		return null;
	}

	public static Center getRandomCenter(HashSet<Center> myHashSet) {
		int size = myHashSet.size();
		int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
		int i = 0;
		for(Center obj : myHashSet)
		{
		    if (i == item)
		        return obj;
		    i++;
		}
		return null;
	}
	
//	public void printArmyStats() {
//		System.out.println("Total soldiers " + getTotalSoldiers());
//		Soldier champ = getSoldierWithMostKills();
////		if (champ.kills > 10) {
//			System.out.println("Most kills: " + champ.getTypeName() + " (" + champ.playerPartyPanel.getName() + ") with " + champ.kills +
//				" kills after " + champ.battlesWon + " wins, " + champ.battlesFled + " retreats out of " + champ.battlesSurvived + " total battles");
//			System.out.println();
////		}
//		Soldier champ2 = getSoldierWithMostCaptures();
//			System.out.println("Most times captured: " + champ2.getTypeName() + " (" + champ2.playerPartyPanel.getName() + ") with " + champ2.timesCaptured +
//				" times captured after " + champ2.battlesWon + " wins, " + champ2.battlesFled + " retreats out of " + champ2.battlesSurvived + " total battles");
//			System.out.println();
//	}
	
	
	private int getTotalSoldiers() {
		int count = 0; 
		for (Army a : armies) {
			count += a.party.getTotalSize();
		}
		return count;
	}
	
	private Soldier getSoldierWithMostKills() {
		Soldier champion = null;
		int max = -1; 
		for (Army a : armies) {
			for (Soldier s : a.party.getHealthy()) {
				if (s.kills > max) {
					max = s.kills;
					champion = s;
				}
			}
			for (Soldier s : a.party.getWounded()) {
				if (s.kills > max) {
					max = s.kills;
					champion = s;
				}
			}
		}
		return champion;
	}
	
	private Soldier getSoldierWithMostCaptures() {
		Soldier champion = null;
		int max = -1; 
		for (Army a : armies) {
			for (Soldier s : a.party.getHealthy()) {
				if (s.timesCaptured > max) {
					max = s.timesCaptured;
					champion = s;
				}
			}
			for (Soldier s : a.party.getWounded()) {
				if (s.timesCaptured > max) {
					max = s.timesCaptured;
					champion = s;
				}
			}
			for (Soldier s : a.party.getPrisoners()) {
				if (s.timesCaptured > max) {
					max = s.timesCaptured;
					champion = s;
				}
			}
		}
		return champion;
	}
}
