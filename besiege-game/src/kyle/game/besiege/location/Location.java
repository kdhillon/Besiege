/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.location;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Point;
import kyle.game.besiege.Siege;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.Farmer;
import kyle.game.besiege.army.Merchant;
import kyle.game.besiege.army.Patrol;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.voronoi.Biomes;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

public class Location extends Actor implements Destination {
	private final float SCALE = .06f;
	private final int offset = 30;
	private final int HIRE_REFRESH = 600; // seconds it takes for soldiers to refresh in city
	// TODO ^ change this to a variable. later make city wealth affect quality of soldiers.
	private final int CLOSE_LOC_DISTANCE = 1000; // distance away locations are considered "close"
	private static final Color clear_white = new Color(1f, 1f, 1f, .6f);

	// for font rotation
	private Matrix4 mx4Font = new Matrix4();
	
	public transient ObjectLabel label;

	protected int DAILY_WEALTH_INCREASE_BASE;
	protected double DAILY_POP_INCREASE_BASE;
	protected int POP_MIN;
	protected int POP_MAX;

	transient private TextureRegion region;
	public String textureName;

	public enum LocationType {CITY, CASTLE, VILLAGE, RUIN};
	public LocationType type;

	// relative prevalence of biomes surrounding this location
	public float[] biomeDistribution;

	//	protected Array<Location> closestFriendlyLocations;
	//	protected Array<Location> closestEnemyLocations;
	protected Array<Patrol> patrols;

	public Array<City> closestFriendlyCities;
	public Array<Castle> closestFriendlyCastles;
	public Array<Village> closestFriendlyVillages;

	public Array<City> closestEnemyCities;
	public Array<Castle> closestEnemyCastles;
	public Array<Village> closestEnemyVillages;

	private boolean mouseOver;

	private Kingdom kingdom;
	private String name;
	private int index;
	private Faction faction;

	public boolean ruin;

	protected double population;

	private double wealthFactor = 1;
	
	private Array<Army> garrisonedArmies;
	public transient Party toHire;
	transient protected Party nextHire; // prevents player from loading and quitting to get ideal choice of hire
	public Army garrison;
	
	// have a variable for delayed merchant arrivals?

	private float timeSinceFreshHire;

	public Siege siege;

//	private float spawnX; // where should units spawn? must be inside
//	private float spawnY;

	private boolean autoManage;
	public boolean playerIn; //is player garrisoned inside (special menu)
	public boolean hostilePlayerTouched;
	public boolean playerWaiting; // is player waiting inside?
	public boolean playerBesieging;

	private Point spawnPoint; // point where armies and farmers should spawn if on water
	public int center = -1; // one of these will be null
	public int corner = -1;

	public Location(){
		this.region = Assets.atlas.findRegion(textureName);
	}


	// constructor for Ruins (location with no faction, garrison, etc)
	public Location(Kingdom kingdom, String name, int index, float posX, float posY) {
		this.faction = null;
		this.ruin = true;
		
		basicConstruct(kingdom, name, index, posX, posY);
	}

	public void basicConstruct(Kingdom kingdom, String name, int index, float posX, float posY) {
		this.kingdom = kingdom;
		this.name = name;
		this.index = index;
	
		setPosition(posX, posY);

		garrisonedArmies = new Array<Army>();

		playerIn = false;
		hostilePlayerTouched = false;

		this.setRotation(0);
		this.setScale(1);
		
		spawnPoint = new Point(this.getCenterX(), this.getCenterY());
		
		setTextureRegion("Castle"); // default location textureRegion
		initializeBox();
	}
	
	public Location(Kingdom kingdom, String name, int index, Faction faction, float posX, float posY, PartyType.Type pType) {
		this.faction = faction;

		basicConstruct(kingdom, name, index, posX, posY);		

		this.garrison = new Army(getKingdom(), this.getName() + " Garrison", getFaction(), getCenterX(), getCenterY(), pType);
		this.garrison.isGarrison = true;
		//		this.garrison.setParty(garrison);
		//		this.garrison(this.garrison);

		autoManage = true;

		patrols = new Array<Patrol>();

		//		closestFriendlyLocations = new Array<Location>();
		//		closestEnemyLocations = new Array<Location>();

		closestEnemyCities = new Array<City>(); 
		closestEnemyCastles = new Array<Castle>();
		closestEnemyVillages = new Array<Village>();

		closestFriendlyCities = new Array<City>(); 
		closestFriendlyCastles = new Array<Castle>();
		closestFriendlyVillages = new Array<Village>();

		timeSinceFreshHire = 0;
		nextHire = new Party(); //empty
	}

	public void initializeBiomeDistributions() {
		Biomes[] biomes = Biomes.values();
		biomeDistribution = new float[biomes.length];

		if (this.corner != -1) {
			Corner c = this.getCorner();
			// just do adjacent corners
			for (Center neighbor : c.touches) {
				if (!neighbor.water) {
					int biomeIndex = neighbor.getBiomeIndex();
					biomeDistribution[biomeIndex]++;
				}
			}
		}
		else if (this.center != -1) {
			Center c = this.getCenter();
			int thisIndex = c.getBiomeIndex();
			biomeDistribution[thisIndex] += 3; // arbitrary

			for (Center neighbor : c.neighbors) {
				if (!neighbor.water) {
					int biomeIndex = neighbor.getBiomeIndex();
					biomeDistribution[biomeIndex]++;
				}
			}
		}

		// normalize biome distribution
		float total = 0;
		for (int i = 0; i < biomeDistribution.length; i++) {
			total += biomeDistribution[i];
		}
		for (int i = 0; i < biomeDistribution.length; i++) {
			biomeDistribution[i] /= total;
			//			if (biomeDistribution[i] != 0) 
			//				System.out.println(this.name + ": " + biomes[i].toString() + " (" + biomeDistribution[i] +")");
		}
	}

	//	public void initializeGarrison() {
	//		Type type = null;
	//		switch(this.type) {
	//		case CASTLE:
	//			type = Type.CASTLE_GARRISON;
	//			break;
	//		case CITY:
	//			type = Type.CITY_GARRISON;
	//			break;
	//		case VILLAGE:
	//			type = Type.VILLAGE_GARRISON;
	//			break;
	//		}
	//		Party gParty = PartyType.getDefault(type, this).generate();
	//		this.garrison.setParty(gParty);
	//	}

	public void initializeBox() {
		this.setWidth(region.getRegionWidth()*SCALE);
		this.setHeight(region.getRegionHeight()*SCALE);
		this.setOrigin(region.getRegionWidth()*SCALE/2, region.getRegionHeight()*SCALE/2);
	}
	public void setCorner(Corner corner) {
		this.corner = corner.index;
		this.initializeBiomeDistributions();

		// problem is that boime distribution hasn't been initialized yet
		updateToHire();
		updateToHire();
		//		this.initializeGarrison();
	}
	public void setCenter(Center center) {
		this.center = center.index;
		this.initializeBiomeDistributions();

		// problem is that boime distribution hasn't been initialized yet
		updateToHire();
		updateToHire();
		//		this.initializeGarrison();
	}
	public Center getCenter() {
		if (this.center == -1) return null;
		return kingdom.getMap().getCenter(center);
	}
	public Corner getCorner() {
		if (this.corner == -1) return null;
		return kingdom.getMap().getCorner(corner);
	}

	/** Initialize containers for armies created at this site 
	 * @param army Army to be created */
	public void setContainerForArmy(Army army) {
		if (this.getCenter() != null) {
			army.containingCenter = center;
			getCenter().armies.add(army);
		}
		else if (getCorner() != null) {
			//			assert(army != null);
			//			assert(army.containing != null);
			//			assert(corner.touches != null);
			//			assert(corner.touches.get(0) != null);
			//			assert(false);
			if (getCorner() == null) System.out.println("containing is null!");
			army.containingCenter = getCorner().touches.get(0).index;
			getCorner().touches.get(0).armies.add(army);
		}
		else 
			System.out.println(this.getName() + " no corner or center!");
	}

	@Override
	public void act(float delta) {
		if (ruin) {
			// nothing here
		}
		else {
			if (this.garrison.getKingdom() != null)
				this.garrison.act(delta);

			if (autoManage) {
				autoManage();
			}
			if (timeSinceFreshHire >= HIRE_REFRESH) {
				timeSinceFreshHire = 0;
				updateToHire();
			}
			else timeSinceFreshHire += delta;

			if (!kingdom.isPaused()) {
				hostilePlayerTouched = false; // only can be selected when game is paused;
			}
		}
		super.act(delta);
	}

	public void autoManage() {
		//contains actions in extensions
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		setRotation(kingdom.getMapScreen().rotation);
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}

	public void drawCrest(SpriteBatch batch) {
		float size_factor = 1.4f;

		if ((this.type == LocationType.VILLAGE))
			size_factor = .5f * size_factor;
		if ((this.type == LocationType.CASTLE))
			size_factor = .8f * size_factor;

		Color temp = batch.getColor();
		float zoom = getKingdom().getZoom();
		zoom *= size_factor; 

		// TODO do some vector calculations to make this rotate	
		// TODO create a bunch more fonts for smoother scrolling!
		// TODO do this in Kingdom at the end of everything
		// don't draw village names at a certain point.
		if (!(this.type == LocationType.VILLAGE && zoom > 4) && !(this.type == LocationType.CASTLE && zoom > 9)) {			
			BitmapFont font;

			if (zoom > 7) {
				font = Assets.pixel150;
				zoom = 7;
			}
			else if (zoom > 5) {
				font = Assets.pixel100;
				zoom = 5;
			}
			else if (zoom > 4) {
				font = Assets.pixel80;
				zoom = 4;
			}
			else if (zoom > 3) {
				font = Assets.pixel64;
				zoom = 3;
			}
			// add some fonts here for smoothness
			else if (zoom > 2.5) {
				font = Assets.pixel50;
				zoom = 2.5f;
			}
			else if (zoom > 2) {
				font = Assets.pixel40;
				zoom = 2f;
			}
			else if (zoom > 1.5) {
				font = Assets.pixel30;
				zoom = 1.5f;
			}
			else if (zoom > 1.2) {
				font = Assets.pixel24;
				zoom = 1.2f;
			}
			else if (zoom > 1) {
				font = Assets.pixel20;
				zoom = 1f;
			}
			else if (zoom > .75) {
				font = Assets.pixel15;
				zoom = .75f;
			}
			else {
				font = Assets.pixel12;
				zoom = .6f;
			}

			// draw crest			
			batch.setColor(clear_white);

			mx4Font.idt();
			mx4Font.rotate(new Vector3(0, 0, 1), getKingdom().getMapScreen().getRotation());
			mx4Font.trn(getCenterX(), getCenterY(), 0);
			Matrix4 tempMatrix = batch.getTransformMatrix();
			batch.setTransformMatrix(mx4Font);

			batch.draw(this.getFaction().crest, -15*zoom, 5 + 5*zoom, 30*zoom, 45*zoom);
			batch.setColor(temp);

			batch.setTransformMatrix(tempMatrix);
			//			// draw text
			//			font.setColor(clear_white);
			//			String toDraw = getName();
			//			font.draw(batch, toDraw, getX() - (int) (4.3*toDraw.length())*zoom, getY()-5-5*zoom);
		}
	}

	public void drawText(SpriteBatch batch) {
		float size_factor = 1.4f;

		if ((this.type == LocationType.VILLAGE))
			size_factor = .5f * size_factor;
		if ((this.type == LocationType.CASTLE || this.type == LocationType.RUIN))
			size_factor = .8f * size_factor;

		Color temp = batch.getColor();
		float zoom =  getKingdom().getZoom();
		zoom *= size_factor; 


		// TODO do some vector calculations to make this rotate	
		// TODO create a bunch more fonts for smoother scrolling!
		// TODO do this in Kingdom at the end of everything
		// don't draw village names at a certain point.
		if (!(this.type == LocationType.VILLAGE && zoom > 4) && !((this.type == LocationType.CASTLE || this.type == LocationType.RUIN) && zoom > 9)) {			
			BitmapFont font;			

//			if (zoom > 7) {
//				font = Assets.pixel150;
//				zoom = 7;
//			}
//			else if (zoom > 5) {
//				font = Assets.pixel100;
//				zoom = 5;
//			}
//			else if (zoom > 4) {
//				font = Assets.pixel80;
//				zoom = 4;
//			}
//			else if (zoom > 3) {
//				font = Assets.pixel64;
//				zoom = 3;
//			}
//			// add some fonts here for smoothness
//			else if (zoom > 2.5) {
//				font = Assets.pixel50;
//				zoom = 2.5f;
//			}
//			else if (zoom > 2) {
//				font = Assets.pixel40;
//				zoom = 2f;
//			}
//			else if (zoom > 1.5) {
				font = Assets.pixel30;
				zoom = 1.5f;
//			}
//			else if (zoom > 1.2) {
//				font = Assets.pixel24;
//				zoom = 1.2f;
//			}
//			else if (zoom > 1) {
//				font = Assets.pixel20;
//				zoom = 1f;
//			}
//			else if (zoom > .75) {
//				font = Assets.pixel15;
//				zoom = .75f;
//			}
//			else {
//				font = Assets.pixel12;
//				zoom = .6f;
//			}
			String toDraw = getName();

			mx4Font.idt();
			mx4Font.rotate(new Vector3(0, 0, 1), getKingdom().getMapScreen().getRotation());
			mx4Font.trn(getX(), getY(), 0);
			Matrix4 tempMatrix = batch.getTransformMatrix();
			batch.setTransformMatrix(mx4Font);

			//			// draw crest
			//			batch.setColor(clear_white);
			//			batch.draw(this.getFaction().crest, getCenterX() - 14*zoom, getCenterY() + 13, 30*zoom, 45*zoom);
			//			batch.setColor(temp);

			// draw text
			font.setColor(clear_white);
			font.draw(batch, toDraw, -(int) (4.3*toDraw.length())*zoom, -8*zoom);

			batch.setTransformMatrix(tempMatrix);
		}
	}

	//	public void drawInfo(SpriteBatch batch, float parentAlpha) {
	//		Kingdom.arial.setColor(Kingdom.factionColors.get(getFaction()));
	//		Kingdom.arial.draw(batch, getName() + " (" + garrison.wealth + ")", getX(), getY());	
	//		
	//		float offset = 0;
	//		for (Army army : getGarrisoned()) {
	//			offset -= getOffset()*getKingdom().getZoom();
	//			Kingdom.arial.draw(batch, army.getName() + ": " + army.getTroopCount(), getX(), getY() + offset);
	//		}	
	//	}

	/** initializes closeLocation arrays with close locations
	 */
	public void findCloseLocations() {
		closestEnemyCities.clear();
		closestEnemyCastles.clear();
		closestEnemyVillages.clear();

		for (City that : getKingdom().getCities()) {
			if (that != this && Kingdom.distBetween(this, that) < CLOSE_LOC_DISTANCE) {
				if (!this.faction.atWar(that.getFaction())) 
					closestFriendlyCities.add(that);
				else closestEnemyCities.add(that);
			}
		}
		for (Castle castle : getKingdom().castles) {
			if (castle != this && Kingdom.distBetween(this, castle) < CLOSE_LOC_DISTANCE) {
				if (!this.faction.atWar(castle.getFaction())) 
					closestFriendlyCastles.add(castle);
				else closestEnemyCastles.add(castle);
			}
		}
		// when is village faction info initialized?
		//		for (Village village : getKingdom().villages) {
		//			if (village != this && Kingdom.distBetween(this, village) < CLOSE_LOC_DISTANCE) {
		//				if (!Faction.isAtWar(getFaction(), village.getFaction()))
		//					closestFriendlyVillages.add(village);
		//				else closestEnemyVillages.add(village);
		//			}
		//		}
	}

	/** Update closeLocation arrays (doesn't look for new cities)
	 * 
	 */
	public void updateCloseLocations() {
		Array<City> newCloseEnemyCities = new Array<City>();
		Array<City> newCloseFriendlyCities = new Array<City>();

		// cities
		for (City c : closestEnemyCities) {
			if (this.getFaction().atWar(c.getFaction())) {
				if (!newCloseEnemyCities.contains(c, true))
					newCloseEnemyCities.add(c);
			}
			else if (!newCloseFriendlyCities.contains(c, true))
				newCloseFriendlyCities.add(c);
		}
		for (City c : closestFriendlyCities) {
			if (this.getFaction().atWar(c.getFaction())) {
				if (!newCloseEnemyCities.contains(c, true))
					newCloseEnemyCities.add(c);
			}
			else if (!newCloseFriendlyCities.contains(c, true))
				newCloseFriendlyCities.add(c);
		}
		closestEnemyCities = new Array<City>(newCloseEnemyCities);
		closestFriendlyCities = new Array<City>(newCloseFriendlyCities);

		Array<Castle> newCloseEnemyCastles = new Array<Castle>();
		Array<Castle> newCloseFriendlyCastles = new Array<Castle>();

		// castles
		for (Castle c : closestEnemyCastles) {
			if (this.getFaction().atWar(c.getFaction())) {
				if (!newCloseEnemyCastles.contains(c, true))
					newCloseEnemyCastles.add(c);
			}
			else if (!newCloseFriendlyCastles.contains(c, true))
				newCloseFriendlyCastles.add(c);
		}
		for (Castle c : closestFriendlyCastles) {
			if (this.getFaction().atWar(c.getFaction())) {
				if (!newCloseEnemyCastles.contains(c, true))
					newCloseEnemyCastles.add(c);
			}
			else if (!newCloseFriendlyCastles.contains(c, true))
				newCloseFriendlyCastles.add(c);
		}
		closestEnemyCastles = new Array<Castle>(newCloseEnemyCastles);
		closestFriendlyCastles = new Array<Castle>(newCloseFriendlyCastles);

		Array<Village> newCloseEnemyVillages = new Array<Village>();
		Array<Village> newCloseFriendlyVillages = new Array<Village>();

		// villages
		for (Village v : closestEnemyVillages) {
			if (this.getFaction().atWar(v.getFaction())) {
				if (!newCloseEnemyVillages.contains(v, true))
					newCloseEnemyVillages.add(v);
			}
			else if (!newCloseFriendlyVillages.contains(v, true))
				newCloseFriendlyVillages.add(v);
		}
		for (Village v : closestFriendlyVillages) {
			if (this.getFaction().atWar(v.getFaction())) {
				if (!newCloseEnemyVillages.contains(v, true))
					newCloseEnemyVillages.add(v);
			}
			else if (!newCloseFriendlyVillages.contains(v, true))
				newCloseFriendlyVillages.add(v);
		}
		closestEnemyVillages = new Array<Village>(newCloseEnemyVillages);
		closestFriendlyVillages = new Array<Village>(newCloseFriendlyVillages);
	}

	public Location getCloseEnemyCity() {
		return closestEnemyCities.random();
	}
	public Location getCloseEnemyCastles() {
		return closestEnemyCastles.random();
	}
	public Location getCloseEnemyVillage() {
		return closestEnemyVillages.random();
	}

	public Array<Patrol> getPatrols() {
		return patrols;
	}
	public void removePatrol(Patrol patrol) {
		patrols.removeValue(patrol, true);
	}

	public void dailyWealthIncrease() {
		this.addWealth((int) (DAILY_WEALTH_INCREASE_BASE * wealthFactor * population/POP_MAX));
	}

	public void dailyPopIncrease() {
		this.population += (DAILY_POP_INCREASE_BASE);
		if (this.population > POP_MAX) this.population = POP_MAX;
	}



	// TODO fix for battlestage
	public void siegeAttack(Array<Army> attackers) {
		//		Army garrisonArmy = new Army(getKingdom(), this.getName() + " Garrison", getFaction(), getCenterX(), getCenterY(), null);
		//		garrisonArmy.setParty(garrison);
		//		if (this.location. == null) {
		//			System.out.println("FUCK no besieging");
		//			return;
		//		}

		if (playerWaiting) {
			// create array of defenders
			Array<Army> defendersArmies = this.getGarrisonedAndGarrison();
			Array<Party> defenders = new Array<Party>();
			for (Army a : defendersArmies) 
				defenders.add(a.party);
				
			Array<Party> attackerParties = new Array<Party>();
			for (Army a : attackers) {
				attackerParties.add(a.party);
			}
			kingdom.getPlayer().createPlayerBattleWith(defenders, attackerParties, true, this);
		}
		else {
			attackers.first().createBattleWith(garrison, this);
			Battle b = garrison.getBattle();
			b.siegeOf = this;

//			System.out.println("siegeOf = " + this.getName());
			b.setPosition(this.getX()-this.getWidth()/2, this.getY()-this.getHeight()/2);
			b.dAdvantage = this.getDefenseFactor();
			for (Army a : attackers) {
				if (a.getParty().player) ;
				// bring up option to attack, pause/stay etc
				if (a != attackers.first()) {
					a.joinBattle(b);
				}
			}
			for (Army a : garrisonedArmies) {
				//			System.out.println("adding " + a.getName() + " to siege battle");
				if (a.passive) continue; // don't add passive armies to defenders
				a.joinBattle(b);
			}
		}
	}
	public void beginSiege(Army army) {
		siege = new Siege(this, army.getFaction());
		siege.add(army);
		kingdom.addActor(siege);
	}
	public void joinSiege(Army army) {
		// htis is never being called
		System.out.println(army.getName() + " JOINING SIEGE");
		siege.add(army);
		if (garrison.getBattle() != null) garrison.getBattle().add(army);
	}

	public void endSiege() {
		kingdom.removeActor(siege);
		siege = null;
	}
	public boolean underSiege() {
		return siege != null;
	}
	public Siege getSiege() {
		return siege;
	}
	public void updateToHire() {
		//		if (this.toHire.size == 0) toHire.add(new Soldier(Weapon.PITCHFORK, null));
		// contained in extensions
	}
//	public void garrison(Soldier soldier) {
//		garrison.getParty().addSoldier(soldier);
//	}
	public void garrison(Army army) {
		// don't garrison a city garrison
		if (army.isGarrison) return;
		if (army.shouldRepair()) {
			repair(army);
		}
		garrisonedArmies.add(army);
		army.setVisible(false);
		army.setPosition(this.spawnPoint.getX()-army.getOriginX(), spawnPoint.getY()-army.getOriginY());

		// attmepting this
		//kingdom.removeArmy(army);
	}
	
	// ejecting armies was duplicating them previously.
	public void eject(Army army) {
		garrisonedArmies.removeValue(army, true);
		army.setGarrisonedIn(null);
		army.setVisible(true);
		kingdom.addArmy(army);
		if (army == getKingdom().getPlayer()) {
			System.out.println("ejecting player");
			if (playerWaiting)
				stopWait();
		}
	}
	public void repair(Army army) {
		PartyType pt = army.getPartyType();
		army.getParty().repair(pt);
	}
	public void startWait() {
		System.out.println("location.startWait()");
		playerWaiting = true;
		getKingdom().getPlayer().setWaiting(true);
		//		getKingdom().getMapScreen().shouldFastForward = true;
		getKingdom().getMapScreen().shouldLetRun = true;
		getKingdom().setPaused(false);
	}

	public void stopWait() {
		//		System.out.println("location.stopWait()");
		playerWaiting = false;
		getKingdom().getPlayer().setWaiting(false);
		//		getKingdom().getMapScreen().shouldFastForward = false;
		getKingdom().getMapScreen().shouldLetRun = false;
		getKingdom().setPaused(true);
	}
	public boolean hire(Party party, Soldier s) { // returns true if hired successfully, false if not (not enough money?)
		if (toHire.getHealthy().contains(s, true)) {
			if (party.wealth - s.getHireCost() >= party.minWealth) {
				party.wealth -= s.getHireCost();
				toHire.removeSoldier(s);
				party.addSoldier(s);
				s.party = party;
				return true;
			}
		}
		return false;
	}
	public Party getToHire() {
		return toHire;
	}
	public void setToHire(Party toHire) {
		this.toHire = toHire;
	}
	
	public Point getSpawnPoint() {
		return spawnPoint;
	}

	@Override
	public String getName() {
		return name;
	}
	@Override
	public Faction getFaction() {
		return faction;
	}
	//	private void setFaction(Faction faction) {
	//		this.faction = faction;
	//	}
	public void changeFaction(Faction newFaction) {
		if (this.ruin) return;
		if (this.type == LocationType.CITY) {
			this.faction.cities.removeValue((City) this, true);
			newFaction.cities.add((City) this);

			this.faction.allocateNoblesFrom((City) this);
			newFaction.allocateNoblesFor((City) this);

			for (Army army : kingdom.getArmies()) {
				if (army.type == Army.ArmyType.MERCHANT) {
					Merchant merchant = ((Merchant) army);
					if (merchant.goal == this) merchant.returnHome();
				}
			}

			for (Patrol patrol : this.patrols)
				patrol.setFaction(newFaction);
			

			BottomPanel.log(newFaction.name + " has taken " + this.getName() + " from " + this.getFactionName());
		}
		else if (this.type == LocationType.CASTLE) {
			for (Patrol patrol : this.patrols)
				patrol.setFaction(newFaction);
			this.faction.castles.removeValue((Castle) this, true);
			newFaction.castles.add((Castle) this);
			BottomPanel.log(newFaction.name + " has taken " + this.getName() + " from " + this.getFactionName());
		}
		else if (this.type == LocationType.VILLAGE) {
			for (Farmer f : ((Village) this).farmers) {
				f.setFaction(newFaction);
			}
			//			((Village) this).farmers = new Array<Farmer>(((Village) this).farmers);
			// TODO undo this
		}

		if (this.type != LocationType.VILLAGE) {
			// swap captured and garrison
			Array<Soldier> oldCaptured = new Array<Soldier>(garrison.getParty().getPrisoners());
			garrison.getParty().clearPrisoners();

			for (Soldier newPrisoner : garrison.getParty().getWounded()) 
				this.garrison.getParty().givePrisoner(newPrisoner, this.garrison.getParty());
			for (Soldier newPrisoner : garrison.getParty().getHealthy())
				this.garrison.getParty().givePrisoner(newPrisoner, this.garrison.getParty());

			for (Soldier newSoldier : oldCaptured) 
				this.garrison.getParty().addPrisoner(newSoldier);

			kingdom.updateFactionCityInfo();
		}
		this.faction = newFaction;
//		if (playerWaiting) {
//			kingdom.getPlayer().garrisonIn(null);
//			this.playerWaiting = false;
//		}
//		
//		System.out.println(playerWaiting + " " + playerBesieging);
//		if (playerBesieging) {
//			kingdom.getPlayer().garrisonIn(this);
//			this.playerBesieging = false;
//		}
		// update friendly arrays, and everything (if this isn't a village)
	}
	@Override 
	public DestType getType() {
		return Destination.DestType.LOCATION;
	}
	public int getIndex() {
		return index;
	}
	public Party getParty() {
		if (garrison == null) return null;
		return garrison.getParty();
	}
//	public void setParty(Party party) {
//		this.garrison.setParty(party);
//	}
	public double getPop() {
		return population;
	}
	public void addWealth(int wealth) {
		this.getParty().wealth += wealth;
	}
	public void loseWealth(int wealth) {
		this.getParty().wealth -= wealth;
	}

	public double distTo(Location location) {
		return Math.sqrt((this.getCenterY() - location.getCenterY())*(this.getCenterY() - location.getCenterY()) + 
				(this.getCenterX() - location.getCenterX())*(this.getCenterX() - location.getCenterX()));
	}

	public double distTo(double x, double y) {
		return Math.sqrt((this.getCenterY() - y)*(this.getCenterY() - y) + 
				(this.getCenterX() - x)*(this.getCenterX() - x));
	}

	public int getWealth() {
		return this.getParty().wealth;
	}
	public void changeWealth(int delta) {
		if (this.getParty().wealth + delta > 0) this.getParty().wealth += delta;
	}
	//	public int getWealth() {
	//		return wealth;
	//	}
	//	public void setWealth(int wealth) {
	//		this.wealth = wealth;
	//	}
	//	@Override
	//	public double distToCenter(Destination d) {
	//		return Math.sqrt((d.getX()-getCenterX())*(d.getX()-getCenterX())+(d.getY()-getCenterY())*(d.getY()-getCenterY()));
	//	}
	//	@Override
	//	public double distTo(Destination d) {
	//		return Math.sqrt((d.getX()-getX())*(d.getX()-getX())+(d.getY()-getY())*(d.getY()-getY()));
	//	}

	@Override
	public void setMouseOver(boolean mouseOver) {
		if (this.mouseOver) {
			if (!mouseOver) {
				kingdom.getMapScreen().getSidePanel().returnToPrevious();
				this.mouseOver = false;
			}
		}
		else if (mouseOver) {
			if (!this.mouseOver) {
				System.out.println("MASODFHASIDj");
				kingdom.getMapScreen().getSidePanel().setActiveLocation(this);
				this.mouseOver = true;
			}
		}
	}

	public float getCenterX() {
		return this.getX() + this.getOriginX();
	}
	public float getCenterY() {
		return this.getY() + this.getOriginY();
	}

	public Kingdom getKingdom() {
		return kingdom;
	}

	public void setTextureRegion(String textureRegion) {
		this.textureName = textureRegion;
		region = Assets.atlas.findRegion(textureRegion);
	}

	public Array<Army> getGarrisoned() {
		return garrisonedArmies;
	}

	// Doesn't return passive armies
	public Array<Army> getGarrisonedAndGarrison() {
		Array<Army> garrisoned = new Array<Army>(getGarrisoned());
		for (Army a : garrisoned) {
			if (a.passive) garrisoned.removeValue(a, true);
		}
		//		Army garrisonArmy = new Army(getKingdom(), "Garrison", getFaction(), getCenterX(), getCenterY(), null);
		//		garrisonArmy.setParty(garrison);
		garrisoned.add(garrison);
		return garrisoned;
	}

	public int getOffset() {
		return offset;
	}
	public boolean isVillage() {
		return this.type == LocationType.VILLAGE;
	}
	public boolean isCastle() {
		return this.type == LocationType.CASTLE;
	}
	public String getFactionName() {
		if (faction == null) return "Abandoned";
		return faction.name;
	}
	public String getTypeStr() {
		if (this.isVillage())
			return "Village";
		else if (this.isCastle())
			return "Castle";
		else if (type == LocationType.CITY)
			return "City";
		else if (type == LocationType.RUIN)
			return "Ruins";
		return "No Type";
	}
	public float getDefenseFactor() {
		return 1.5f; //TODO
	}
	// prepare this for saving (separate from map)
	public void nullify() {
		this.kingdom = null;
		this.remove();
	}
	public void restore(Kingdom kingdom) {
		this.kingdom = kingdom;
		kingdom.addActor(this);
	}
	public void restoreTexture() {
		this.setTextureRegion(textureName);
	}
}
