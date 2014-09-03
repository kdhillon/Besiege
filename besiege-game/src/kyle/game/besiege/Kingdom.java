/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
// group of actors, contains bg, all cities and all armies
package kyle.game.besiege;


import java.util.Scanner;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.ArmyPlayer;
import kyle.game.besiege.army.Bandit;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.location.Castle;
import kyle.game.besiege.location.City;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.panels.Panel;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public class Kingdom extends Group {
	public static final double DECAY = .1;
	public static final float HOUR_TIME = 2.5f;
	public static final int CITY_START_WEALTH = 500;
	public static final int VILLAGE_START_WEALTH = 10;
	public static final int CASTLE_START_WEALTH = 100;
	public static final int BANDIT_FREQ = 1000;
	public static final int MAX_BANDITS = 5;
	public static boolean drawCrests = true;
	public static boolean drawArmyCrests = true;

	public final float LIGHT_ADJUST_SPEED = .005f; //adjust this every frame when changing daylight
	public final float NIGHT_FLOAT = .5f;
	public final float RAIN_FLOAT = .4f;
	public final float LIGHTNING_FLOAT = 1f;
	private final float MOUSE_DISTANCE = 10; // distance destination must be from mouse to register
	private final int DAWN = 7;
	private final int DUSK = 21;
	private final double RAIN_CHANCE = 5000; // higher is less likely
	public final double THUNDER_CHANCE = 800;

	public float clock;
	private int timeOfDay; // 24 hour day is 60 seconds, each hour is 2.5 seconds
	private int day;
	public boolean night; // is nighttime?
	public float currentDarkness; // can be used for LOS
	public float targetDarkness; // for fading
	public boolean raining;
	

	public Map map;
	transient private MapScreen mapScreen;
	public Array<Faction> factions;
	private int factionCount;
	public Array<Array<Integer>> factionRelations; // should break into multiple arrays
	
	private Array<Army> armies;
	public Array<City> cities;
	public Array<Castle> castles;
	public Array<Village> villages;
	private Array<Battle> battles;
	private ArmyPlayer player;

	public int banditCount;

	public Destination currentPanel;

	private boolean mouseOver; // is mouse over Kingdom screen?
	private boolean paused;

	private boolean leftClicked;
	private boolean rightClicked;
	private Point mouse;

	// for loading kingdom
	public Kingdom() {
		
	}
	
	public Kingdom(MapScreen mapScreen) {
		map = new Map(true);

		clock = 0; // set initial clock
		timeOfDay = 0;
		day = 0;
		raining = true;
//		raining = false;

		this.time(0);
		
		//		currentDarkness = NIGHT_FLOAT;
		currentDarkness = 0; // fade in
		if (this.night)
			this.targetDarkness = NIGHT_FLOAT;
		else this.targetDarkness = 1f;
		this.mapScreen = mapScreen;

		addActor(map);

		armies = new Array<Army>();
		cities = new Array<City>();
		castles = new Array<Castle>();
		villages = new Array<Village>();
		battles = new Array<Battle>();

		initializeFactions(this);

//		assignFactionCenters();
		
		initializeCities();
		initializeVillages();
		initializeCastles();
		
		
		initializeFactionCityInfo();

		for (int i = 0; i < cities.size; i++)
			cities.get(i).findCloseLocations();
		
		mouse = new Point(0,0);
		banditCount = 0;
		currentPanel = new Point(0,0);
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

			manageBandits();
			factionAct(delta);
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
	
	// just check adjacent centers!
	public void updateArmyPolygon(Army army) {
		if (army.getContaining() != null) {

			// first check if it's left it's previous polygon
			// if not, return, if so, remove from previous container
			if (army.getContaining().polygon != null && army.getContaining().polygon.contains(army.getCenterX(), army.getCenterY())) {
				if (!army.getContaining().armies.contains(army, true)) army.getContaining().armies.add(army);
				return;
			}

			army.getContaining().armies.removeValue(army, true);

			for (Center adjacent : army.getContaining().neighbors) {
				// checks if in connected (all connected have polygon)
				if (adjacent.polygon != null) {
					Polygon p = adjacent.polygon;
					if (p.contains(army.getCenterX(), army.getCenterY())) {
						adjacent.armies.add(army);
						army.containingCenter = adjacent.index;
						return;
					}
				}
			}
			army.containingCenter = -1;
			//			System.out.println("nothing adjacent found for " + army.getName());
		}
		// container is null (slow during initialization stages - plan is to initialize armies with accurate center)
		for (Center center : map.connected) {
			Polygon p = center.polygon;
			if (p.contains(army.getCenterX(), army.getCenterY())) {
				center.armies.add(army);
				army.containingCenter = center.index;
				//						System.out.println("completely differnt polygon");
				return;
			}
		}
	}

	// update time
	public void time(float delta) {
		clock += delta;
		timeOfDay = (int) ((clock - day*60) / HOUR_TIME);
		if (timeOfDay == 24) {
			dailyRoutine();
			day++;
		}
		if (timeOfDay >= DAWN && timeOfDay <= DUSK)
			night = false;
		if (timeOfDay <= DAWN || timeOfDay >= DUSK)
			night = true;
		
		if (Math.random() < 1/RAIN_CHANCE) raining = true;
		if (raining && Math.random() < .0005) raining = false;
	}
	

	public void rain() {
//		System.out.println("raining");
		this.targetDarkness = this.RAIN_FLOAT;
		if (Math.random() < 1/this.THUNDER_CHANCE) thunder();
	}
	
	private void thunder() {
//		this.currentDarkness = (float)((Math.random()/2+.5)*this.LIGHTNING_FLOAT);
		this.currentDarkness = this.LIGHTNING_FLOAT;
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
		System.out.println("day is done");
		
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
	}

	private void mouseOver(Point mouse) {
		Destination d = getDestAt(mouse);
		//		if (d.getType() != 0)
		this.setPanelTo(d);
		//			d.setMouseOver(true);
	}

	private void setPanelTo(Destination newPanel) {
		//		if (currentPanel == null) System.out.println("currentPanel is null");
		// makes sure not to set the same panel a lot, and makes sure not to return to previous for every single point
		if (newPanel != currentPanel && (newPanel.getType() != Destination.DestType.POINT || currentPanel.getType() != Destination.DestType.POINT)) {
			getMapScreen().getSidePanel().setActiveDestination(newPanel);
			currentPanel = newPanel;
		}
	}

	private void leftClick(Point mouse) {
		
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
				getMapScreen().getSidePanel().setActiveArmy(player);
			}
			//if (player.getTarget() != null) System.out.println("target = " + player.getTarget().getName());
		}
	}
	private void rightClick(Point mouse) {		
		Destination d = getDestAt(mouse);
		if (d.getType() == Destination.DestType.LOCATION) {
			Location location = (Location) d;
			getMapScreen().getSidePanel().setActiveLocation(location);
		}
		if (d.getType() == Destination.DestType.ARMY) { // army
			Army destinationArmy = (Army) d;
			getMapScreen().getSidePanel().setActiveArmy(destinationArmy);
		}
		if (d.getType() == Destination.DestType.BATTLE) { //battle
			Battle battle = (Battle) d;
			getMapScreen().getSidePanel().setActiveBattle(battle);
		}
	}

	public void click(int pointer) {
		this.armies.iterator();
		
//		if (pointer == 0)
//			leftClicked = true;
//		else if (pointer == 1) 
//			rightClicked = true;
//		else if (pointer == 4)
//			writeCity();
		// try switching them
		if (pointer == 1)
			leftClicked = true;
		else if (pointer == 0) 
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
			if (Kingdom.distBetween(city, mouse) <= MOUSE_DISTANCE)
				dest = city;
		}
		for (Village village : villages) {
			if (Kingdom.distBetween(village, mouse) <= MOUSE_DISTANCE)
				dest = village;
		}
		for (Castle castle : castles) {
			if (Kingdom.distBetween(castle, mouse) <= MOUSE_DISTANCE)
				dest = castle;
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
		for (Battle battle : battles) {
			if (Kingdom.distBetween(battle, mouse) <= MOUSE_DISTANCE) {
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
	}
	

	public void initializeFactions(Kingdom kingdom) {
		factions = new Array<Faction>();

		factionRelations = new Array<Array<Integer>>();
		//		factionMilitaryAction = new Array<Array<Integer>>();

		// add player faction (index 0) 
		
		Faction.BANDITS_FACTION = new Faction(this, "Bandits", "crestBandits", Color.BLACK);
		Faction.PLAYER_FACTION = new Faction(this,"Rogue", "crestBlank", Color.WHITE);
		
		createFaction(Faction.PLAYER_FACTION);
		// add bandits faction (index 1)
		createFaction(Faction.BANDITS_FACTION);	

		createFaction("Geinever", "crestWhiteLion", Color.DARK_GRAY);
		createFaction("Weyvel", "crestGreenTree", Faction.OLIVE);
		createFaction("Rolade", "crestOrangeCross", Faction.BROWN);
		createFaction("Myrnfar", "crestYellowStar", Faction.TAN);
		createFaction("Corson", "crestRedCross", Faction.RED);
		createFaction("Selven", "crestGreenStripe", Faction.GREEN);
		createFaction("Halmera", "crestBlueRose", Faction.BLUE);

		createFaction("Fernel", "crestRedAxe", Color.LIGHT_GRAY);
	//	createFaction("Draekal", "crestBlank", Color.BLACK);

		for (Faction f : factions) {
			f.kingdom = kingdom;
		}

		factions.get(2).declareWar(factions.get(3));

		//		factionRelations = new int[factionCount][factionCount];
		for (int i = 0; i < factionCount; i++) {
			for (int j = 0; j < factionCount; j++) {
				//				factionRelations[i][j] = -30;
				factionRelations.get(i).set(j, -30);
				factionRelations.get(j).set(i, -30);
			}
		}
		for (int i = 0; i < factionCount; i++) {
			//			factionRelations[i][i] = 100;
			factionRelations.get(i).set(i, 100);
		}
	}
	
	public void factionAct(float delta) {
		factions.shrink();
		for (int i = 0; i < factions.size; i++)
			factions.get(i).act(delta);
	}
	public void createFaction(String name, String textureRegion, Color color) {
		Faction faction = new Faction(this, name, textureRegion, color);
		factions.add(faction);
		faction.index = factions.indexOf(faction, true);
		for (int i = 0; i < factions.size; i++) {
			//			factionRelations[faction.index][i] = 0; // resets faction relations
			//			factionRelations[i][faction.index] = 0;
			if (factionRelations.size <= faction.index)
				factionRelations.add(new Array<Integer>());

			if (factionRelations.get(i).size <= faction.index)
				factionRelations.get(i).add(0);
			else factionRelations.get(i).set(faction.index, 0);

			if (factionRelations.get(faction.index).size <= i)
				factionRelations.get(faction.index).add(0);
			else factionRelations.get(faction.index).set(i, 0);
		}
		if (faction.index >= 1) {
			faction.declareWar(Faction.BANDITS_FACTION);
		}
	}
	public void createFaction(Faction faction) {
		factions.add(faction);
		faction.index = factions.indexOf(faction, true);
		for (int i = 0; i < factions.size; i++) {
			//			factionRelations[faction.index][i] = 0; // resets faction relations
			//			factionRelations[i][faction.index] = 0;
			if (factionRelations.size <= faction.index)
				factionRelations.add(new Array<Integer>());

			if (factionRelations.get(i).size <= faction.index)
				factionRelations.get(i).add(0);
			else factionRelations.get(i).set(faction.index, 0);

			if (factionRelations.get(faction.index).size <= i)
				factionRelations.get(faction.index).add(0);
			else factionRelations.get(faction.index).set(i, 0);
		}
		if (faction.index >= 1) {
			faction.declareWar(Faction.BANDITS_FACTION);
		}
	}
	
	
	/** Initialize each faction's list of hostile cities, faction control map 
	 *  of centers, and village control. 
	 */
	public void initializeFactionCityInfo() {
		System.out.println("initializing faction city info");
		for (Faction f : factions) { 
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
		System.out.println("updating faction city info");
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
		for (Center c : getMap().connected) calcInfluence(c);
		getMap().calcBorderEdges();
		for (Faction f : factions) {
			f.territory.clear();
			Array<Array<Center>> aaCenters = MapUtils.calcConnectedCenters(f.centers);				
			for (Array<Center> centers : aaCenters) {
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
	 * @param center
	 */
	private void calcInfluence(Center center) {
		Point centerPoint = new Point(center.loc.x, Map.HEIGHT - center.loc.y);
		Faction bestFaction = null;
		double bestScore = 0;

		// go through factions and calc influence, saving it if it's the highest
		for (Faction faction : factions) {
			double score = 0; // score is inverse of city distance
			for (City city : faction.cities) {
				double dist = Kingdom.distBetween(city, centerPoint);
				score += 1/dist;
			}
			for (Castle castle : faction.castles) {
				double dist = Kingdom.distBetween(castle, centerPoint);
				score += 1/dist;
			}
			if (score > bestScore) {
				bestScore = score;
				bestFaction = faction;
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

	public int getRelations(Faction faction1, Faction faction2) {
		if (faction1 == null || faction2 == null) return 0;
		return factionRelations.get(faction1.index).get(faction2.index);
	}
	
	/** return whether or not these two factions are at war. 
	 * note that the same faction can't be at war with itself.
	 * @param faction1
	 * @param faction2
	 * @return
	 */
	public boolean isAtWar(Faction faction1, Faction faction2) {
		if (faction1 == faction2) return false;
		return (getRelations(faction1, faction2) < Faction.WAR_THRESHOLD);
	}
	public void setAtWar(Faction faction1, Faction faction2) {
		//		factionRelations[faction1][faction2] = WAR_THRESHOLD-1;
		//		factionRelations[faction2][faction1] = WAR_THRESHOLD-1;
		factionRelations.get(faction1.index).set(faction2.index, Faction.INIT_WAR_RELATION);
		factionRelations.get(faction2.index).set(faction1.index, Faction.INIT_WAR_RELATION);
	}
	public void setNeutral(Faction faction1, Faction faction2) {
		factionRelations.get(faction1.index).set(faction2.index, 0);
		factionRelations.get(faction2.index).set(faction1.index, 0);

		//		factionRelations[faction1][faction2] = 0;
		//		factionRelations[faction2][faction1] = 0;
	}

	public void makePeace(Faction faction1, Faction faction2) {
		//		BottomPanel.log(faction1.name + " and " + faction2.name + " have signed a peace agreement!", "magenta");
		setNeutral(faction1, faction2);
	}
	public void declareWar(Faction faction1, Faction faction2) {
		//		BottomPanel.log(faction1.name + " and " + faction2.name + " have declared war!", "magenta");
		setAtWar(faction1, faction2);
	}

	public void changeRelation(Faction faction1, Faction faction2, int delta) {
		int initialRelation = factionRelations.get(faction1.index).get(faction2.index);
		int newRelation;
		if (initialRelation + delta >= Faction.MAX_RELATION) newRelation = Faction.MAX_RELATION;
		else if (initialRelation + delta <= Faction.MIN_RELATION) newRelation = Faction.MIN_RELATION;
		else newRelation = initialRelation + delta;
		if (initialRelation >= Faction.WAR_THRESHOLD && newRelation < Faction.WAR_THRESHOLD) ;
		//			BottomPanel.log(faction1.name + " and " + faction2.name + " have declared war!", "magenta");
		else if (initialRelation < Faction.WAR_THRESHOLD && newRelation >= Faction.WAR_THRESHOLD) 
			makePeace(faction1, faction2);
		factionRelations.get(faction1.index).set(faction2.index, newRelation);
	}

	//	public static void calcAllRelations() {
	//		for (Faction f : factions) { 
	//			for (int i = 0; i < factions.size; i++) {
	//				int base = 0;
	////				base += factionMilitaryAction.get(f.index).get(i); // Military actions
	//				base += f.getCloseCityEffect(factions.get(i));	   // Borders
	//				base += factions.get(i).getCloseCityEffect(f);	   // (Borders is 2-way)
	//				factionRelations.get(i).set(f.index, base); 	   // can make more efficient
	//				factionRelations.get(f.index).set(i, base);
	//			}
	//		}
	//	}

	public Faction get(int index) {
		return factions.get(index);
	}
	
	
	
	public void addCity(City newCity) {
		cities.add(newCity);
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
		Array<String> cityArray = Assets.cityArray;
		//		Scanner scanner = Assets.cityList;
		
//		int currentFaction = 2; // no bandits or player 
//		int factionRepeats = 0;
		
//		int maxRepeats = (int) (Math.random()*3) + 1; // max 2, min 0
		int maxRepeats = 4;
		
		//		while (scanner.hasNextLine() && (map.cityCorners.size > 0 && map.cityCenters.size > 0)) {
		while (cityArray.size > 0 && (map.cityCorners.size > 0 && map.cityCenters.size > 0)) {
			//			System.out.println("adding city");
			City city = null;
			int x, y;
			boolean useCorner = true; // later allow to use center
			if (Math.random() > .5)
				useCorner = false; // later allow to use center
			
			Corner corner = null;
			Center center = null;
			
			// make with corner
			if (useCorner) {
				corner = map.cityCorners.random();
				x = (int) corner.getLoc().x;
				y = (int) corner.getLoc().y;
				map.cityCorners.removeValue(corner, true);
				map.availableCorners.removeValue(corner, true);

				// remove neighboring corners 2 levels deep!
				for (Corner adj : corner.adjacent) {
					if (adj != null) {
						map.cityCorners.removeValue(adj, true);
						map.availableCorners.removeValue(adj, true);
						for (int i = 0; i < adj.adjacent.size(); i++) {
							// two levels deep
							if (adj.adjacent.get(i) != null) 
								map.cityCorners.removeValue(adj.adjacent.get(i), true);
						}
					}
				}
				// remove neighboring centers
				for (Center adj : corner.touches) {
					if (adj != null) {
						map.cityCenters.removeValue(adj, true);
						map.availableCenters.removeValue(adj, true);
					}
				}
			}
			// make with center
			else {
				center = map.cityCenters.random();
				x = (int) center.loc.x;
				y = (int) center.loc.y;
				map.cityCenters.removeValue(center, true);
				map.availableCenters.removeValue(center, true);

				for (Corner adj : center.corners) {
					if (adj != null) {
						map.cityCorners.removeValue(adj, true);
						//						map.availableLocationSites.removeValue(adj.loc, false);
						for (int i = 0; i < adj.adjacent.size(); i++) {
							// two levels deep
							if (adj.adjacent.get(i) != null)  {
								map.cityCorners.removeValue(adj.adjacent.get(i), true);
								//								map.availableLocationSites.removeValue(adj.adjacent.get(i).loc, false);
							}
						}
					}
				}
				for (Center adjCenter : center.neighbors) {
					if (adjCenter != null) {
						map.cityCenters.removeValue(adjCenter, true);
						//						map.availableLocationSites.removeValue(adjCenter.loc, false);
					}
				}
				int index = -1;
			}
			
			// calculate the closest faction center
			double closestDistance = 99999999;
			Faction closestFaction = null;
			for (Faction f : factions) {
				if (f == Faction.PLAYER_FACTION || f == Faction.BANDITS_FACTION) continue;
				if (f == null) continue;
				
				if (!f.hasNewCenter()) {
					f.faction_center_x = x;
					f.faction_center_y = Map.HEIGHT-y;
					closestFaction = f;
					break;
				}
				
				double distance = Kingdom.distBetween(new Point(f.faction_center_x, f.faction_center_y), new Point(x, Map.HEIGHT-y));
				if (distance < closestDistance) {
					closestFaction = f;
					closestDistance = distance;
				}	
			}
			
			if (closestFaction == null) System.out.println("NULL CLOSEST FACTION");
			
			city = new City(this, cityArray.pop(), -1, closestFaction, x, Map.HEIGHT-y, CITY_START_WEALTH);

			if (center != null)
				city.setCenter(center);
			if (corner != null)
				city.setCorner(corner);

			addCity(city);
		}

		System.out.println("Number cities: " + cities.size);
	}

	// villages only spawn at centers (for influence purposes)
	public void initializeVillages() {
		//		Scanner scanner = Assets.villageList;
		Array<String> villageArray = Assets.villageArray;

		//		while (scanner.hasNextLine() && map.availableCenters.size > 0) {
		while (villageArray.size > 0 && map.availableCenters.size > 0) {
			Center center = map.availableCenters.random();
			map.availableCenters.removeValue(center, true);

			//			Village village = new Village(this, scanner.next(), -1, null, (float) center.loc.x, (float) (Map.HEIGHT-center.loc.y), VILLAGE_START_WEALTH);			
			Village village = new Village(this, villageArray.pop(), -1, null, (float) center.loc.x, (float) (Map.HEIGHT-center.loc.y), VILLAGE_START_WEALTH);			
			villages.add(village);
			village.center = center.index;
			addActor(village);
		}
		System.out.println("Number villages: " + villages.size);
	}
	
	public void initializeCastles() {
		Array<String> castleArray = Assets.castleArray;

		int currentFaction = 2; // no bandits or player 
		int factionRepeats = 0;
		int maxRepeats = (int) (Math.random()*3) + 1; // max 2, min 0
		
		//		while (scanner.hasNextLine() && map.availableCenters.size > 0) {
		while (castleArray.size > 0 && map.availableCorners.size > 0) {
			
			Corner corner;
			do {
				//System.out.println(map.availableCorners.size);
				corner = map.availableCorners.random();
				map.availableCorners.removeValue(corner, true);
			} while (corner == null && map.availableCorners.size > 0);
			
			if (corner == null) {
				System.out.println("no corners left");
				break;
			}
			
			float x = (float) corner.getLoc().x;
			float y = (float) (Map.HEIGHT-corner.getLoc().y);
			
			
			double closestDistance = Double.MAX_VALUE;
			Faction closestFaction = null;
			for (Faction f : factions) {
				if (f == Faction.PLAYER_FACTION || f == Faction.BANDITS_FACTION) continue;
				if (f == null) continue;
				double distance = Kingdom.distBetween(new Point(f.faction_center_x, f.faction_center_y), new Point(x, y));
				if (distance < closestDistance) {
					closestFaction = f;
					closestDistance = distance;
				}	
			}
			
			if (closestFaction == null) System.out.println("NULL CLOSEST FACTION");
			
			Castle castle = new Castle(this, castleArray.pop(), -1, closestFaction, x, y, CASTLE_START_WEALTH);			
			castles.add(castle);
			castle.corner = corner.index;
			
			addActor(castle);
		}
		
		System.out.println("Number castles: " + villages.size);
	}

	public void addArmy(Army add) {
		armies.add(add);
		addActor(add);
		//add.postAdd();
	}
	public void addPlayer() {
		Faction faction = factions.get(2);
		Center center = map.reference;
		
		int pos_x = (int) map.reference.loc.x;
		int pos_y = (int) (Map.HEIGHT-map.reference.loc.y);
		
		if (faction.centers.size > 0) {
			center = faction.centers.random();
			pos_x = (int) center.loc.x;
			pos_y = (int) (Map.HEIGHT-center.loc.y);
			System.out.println("player created in faction center");
		} else System.out.println("no centers!");
		
		player = new ArmyPlayer(this, faction, pos_x, pos_y, 6);
		player.getParty().player = true;
		player.containingCenter = center.index;
		addArmy(player);
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
	
	public Array<Location> getAllLocationsCopy() {
		Array<Location> locations = new Array<Location>();
		locations.addAll(cities);
		locations.addAll(castles);
		locations.addAll(villages);
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

	public Array<Army> getArmies() {
		return armies;
	}
	public void setArmies(Array<Army> armies) {
		this.armies = armies;
	}
	public void addBattle(Battle battle) {
		battles.add(battle);
	}
	public void removeBattle(Battle battle) {
		battles.removeValue(battle, true);
	}
	public Array<City> getCities() {
		return cities;
	}
	public ArmyPlayer getPlayer() {
		return player;
	}

	public void setPaused(boolean paused) {
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
	public void nullifyForSave() {
		this.mapScreen = null;
		this.map = null;
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
		return getMapScreen().getCamera().zoom;
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
	public static double distBetween(Destination d1, Destination d2) {
		// TODO optimize by computing getCenter only once per
		return Math.sqrt((d1.getCenterX()-d2.getCenterX())*(d1.getCenterX()-d2.getCenterX())+(d1.getCenterY()-d2.getCenterY())*(d1.getCenterY()-d2.getCenterY()));
	}
	// should be slightly faster than above
	public static double sqDistBetween(Destination d1, Destination d2) {
		return (d1.getCenterX()-d2.getCenterX())*(d1.getCenterX()-d2.getCenterX())+(d1.getCenterY()-d2.getCenterY())*(d1.getCenterY()-d2.getCenterY());
	}
}
