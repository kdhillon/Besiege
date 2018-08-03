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
import com.badlogic.gdx.scenes.scene2d.Group;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Point;
import kyle.game.besiege.Siege;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.army.Farmer;
import kyle.game.besiege.army.Merchant;
import kyle.game.besiege.army.Patrol;
import kyle.game.besiege.battle.BattleActor;
import kyle.game.besiege.battle.Fire;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.PanelLocation;
import kyle.game.besiege.party.*;
import kyle.game.besiege.voronoi.Biomes;
import kyle.game.besiege.voronoi.Center;
import kyle.game.besiege.voronoi.Corner;

public class Location extends Group implements Destination {
	private final float SCALE = .06f;
	public static final float MIN_ZOOM = 0.5f;
	public static final float MAX_ZOOM = 2;
	private final int offset = 30;
	private final int HIRE_REFRESH = 600; // seconds it takes for soldiers to refresh in city
	private final float garrisonBudget = 0.20f; // this percent of wealth can be used to buy/upgrade garrison
	private final float GARRISON_DISCOUNT = .25f; // it's cheap to replenish your garrison 

	// TODO ^ change this to a variable. later make city wealth affect quality of soldiers.
	private final int CLOSE_LOC_DISTANCE = 1000; // distance away locations are considered "close"
	private static final Color clear_white = new Color(1f, 1f, 1f, .6f);
	private static final Color black = new Color(0f, 0f, 0f, 0.4f);

	// for font rotation
	private Matrix4 mx4Font = new Matrix4();
	
	public transient ObjectLabel label;

	protected int DAILY_WEALTH_INCREASE_BASE;
	protected double DAILY_POP_INCREASE_BASE;
	public int POP_MIN;
	public int POP_MAX;

	transient private TextureRegion region;
	public String textureName;

	public enum LocationType {CITY, CASTLE, VILLAGE, RUIN};
	public LocationType type;

	// relative prevalence of biomes surrounding this location
	public float[] biomeDistribution;

	// Main unit class based on surrounding biomes
	public CultureType cultureType;

	//	protected StrictArray<Location> closestFriendlyLocations;
	//	protected StrictArray<Location> closestEnemyLocations;
	
	protected static final int MAX_PATROLS = 5;
	protected StrictArray<Patrol> patrols;

	public StrictArray<City> closestFriendlyCities;
	public StrictArray<Castle> closestFriendlyCastles;
	public StrictArray<Village> closestFriendlyVillages;

	public StrictArray<City> closestEnemyCities;
	public StrictArray<Castle> closestEnemyCastles;
	public StrictArray<Village> closestEnemyVillages;

	private boolean mouseOver;

	private Kingdom kingdom;
	private String name;
	private int index;
	private Faction faction;

	public boolean ruin;

	protected double population;

	private double wealthFactor = 1;
	
	private StrictArray<Army> garrisonedArmies;
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
	private int center = -1; // one of these will be null
	private int corner = -1;

	private boolean discovered; // Has this location been discovered by the player?
	
	// TODO make wealth belong to location, not garrison. doesn't make sense when changing factions.
	
	public PartyType.Type pType;
	private int wealth;
	
	// do we need to update panel
	public boolean needsUpdate = true;
	public transient PanelLocation panel;
	
	public Fire fire;
	
	public Location(){
		this.region = Assets.atlas.findRegion(textureName);
	}

	// constructor for Ruins (location with no faction, garrison, etc)
	public Location(Kingdom kingdom, String name, int index, float posX, float posY, Center center, Corner corner) {
		this.faction = null;
		this.ruin = true;
		
		basicConstruct(kingdom, name, index, posX, posY, center, corner);
	}

	public void basicConstruct(Kingdom kingdom, String name, int index, float posX, float posY, Center center, Corner corner) {
		this.kingdom = kingdom;
//		this.name = name;
		this.index = index;

		if (center != null) {
		    setCenter(center);
        } else if (corner != null) {
            setCorner(corner);
        } else throw new AssertionError();

		setPosition(posX, posY);

		garrisonedArmies = new StrictArray<Army>();

		playerIn = false;
		hostilePlayerTouched = false;

		this.setRotation(0);
		this.setScale(1);
		
		spawnPoint = new Point(this.getCenterX(), this.getCenterY());
	}
	
	public Location(Kingdom kingdom, String name, int index, Faction faction, float posX, float posY, PartyType.Type pType, Center center, Corner corner) {
		this.faction = faction;
        this.pType = pType;

		basicConstruct(kingdom, name, index, posX, posY, center, corner);
        createGarrison();


        //		this.garrison.setParty(garrison);
		//		this.garrison(this.garrison);

		autoManage = true;

		patrols = new StrictArray<Patrol>();

		//		closestFriendlyLocations = new StrictArray<Location>();
		//		closestEnemyLocations = new StrictArray<Location>();

		closestEnemyCities = new StrictArray<City>(); 
		closestEnemyCastles = new StrictArray<Castle>();
		closestEnemyVillages = new StrictArray<Village>();

		closestFriendlyCities = new StrictArray<City>(); 
		closestFriendlyCastles = new StrictArray<Castle>();
		closestFriendlyVillages = new StrictArray<Village>();

		timeSinceFreshHire = 0;
		nextHire = new Party(); //empty
	}

	// This has to be called after setCorner/setCenter is called
	public void createGarrison() {
		String name = this.getName() + " Garrison";
//		if (getFaction() != null) name = this.getName() + " Garrison " + getFaction().name;
		this.garrison = new Army(getKingdom(), name, getFaction(), getCenterX(), getCenterY(), pType, this);
		this.garrison.isGarrison = true;
		this.garrison.passive = true;
//		manageGarrison();
		this.needsUpdate = true;
	}

	private CultureType calculateCultureType() {
	    if (getCenter() != null) {
	        if (getCenter().cultureType == null) {
                System.out.println("no CultureType for center " + getCenter().biome.name());
            } else return getCenter().cultureType;
        }
        Corner corner = getCorner();
        if (corner != null) {
	        for (Center center : corner.touches) {
                if (center.cultureType != null) {
                    return center.cultureType;
                } else {
                    System.out.println("no CultureType for corner " + center.biome.name());
                }
            }
        }
        throw new AssertionError();
    }

	private CultureType calculateCultureType(float[] biomeDistribution) {
	    Object[] unitClasses = UnitLoader.cultureTypes.values().toArray();

        float[] classDistribution = new float[unitClasses.length];

        boolean found = false;
        for (int i = 0; i < biomeDistribution.length; i++) {
            if (biomeDistribution[i] <= 0) {
                continue;
            }
            found = true;

            CultureType classForBiome = UnitLoader.biomeCultures.get(Biomes.values()[i]);
            System.out.println(Biomes.values()[i].name() + ", distribution: " + biomeDistribution[i]);
            if (classForBiome == null) {
//                continue; // this is for classes like ocean, etc.
                throw new AssertionError();
            }

            // Now find the corresponding unit class for this biome, and update distribution
            for (int j = 0; j < unitClasses.length; j++) {
                if ((unitClasses[j]) == classForBiome) {
                    System.out.println(Biomes.values()[i].name() + " uses class " + classForBiome.name + ", distribution: " + biomeDistribution[i]);
                    classDistribution[j] += biomeDistribution[i];
                }
            }
        }
        if (!found) throw new AssertionError();

        // Now that we've calculated the class distribution for all unit classes, just take the max.
        int maxIndex = -1;
        float maxValue = 0;
        for (int i = 0; i < classDistribution.length; i++) {
            if (classDistribution[i] > maxValue) {
                maxIndex = i;
                maxValue = classDistribution[i];
            }
        }

	    return (CultureType) unitClasses[maxIndex];
    }

	public void initializeBiomeDistributions() {
		Biomes[] biomes = Biomes.values();
		biomeDistribution = new float[biomes.length];

		if (this.corner != -1) {
			Corner c = this.getCorner();
			// just do adjacent corners
			for (Center neighbor : c.touches) {
				if (!neighbor.water) {
				    if (neighbor.getBiomeIndex() == -1) throw new AssertionError();
					int biomeIndex = neighbor.getBiomeIndex();
					biomeDistribution[biomeIndex]++;
				}
			}
		}
		else if (this.center != -1) {
			Center c = this.getCenter();
			if (this.getCenter().water) throw new AssertionError();

			int thisIndex = c.getBiomeIndex();
			biomeDistribution[thisIndex] += 15; // arbitrary
            if (c.getBiomeIndex() == -1) throw new AssertionError();

//			for (Center neighbor : c.neighbors) {
//				if (!neighbor.water) {
//					int biomeIndex = neighbor.getBiomeIndex();
//					biomeDistribution[biomeIndex]++;
//				}
//			}
		}

		// normalize biome distribution
		float total = 0;
		for (int i = 0; i < biomeDistribution.length; i++) {
			total += biomeDistribution[i];
		}
		for (int i = 0; i < biomeDistribution.length; i++) {
			biomeDistribution[i] /= total;
			if (biomeDistribution[i] > 0) {
			    System.out.println("Biome exists: " + Biomes.values()[i].name() + " " + biomeDistribution[i]);
            }
			//			if (biomeDistribution[i] != 0) 
			//				System.out.println(this.name + ": " + biomes[i].toString() + " (" + biomeDistribution[i] +")");
		}

		System.out.println("total biome value " + total);

		cultureType = calculateCultureType();

//		cultureType = calculateCultureType(biomeDistribution);
		if (cultureType == null) throw new AssertionError();

        // Update name of city
        if (this instanceof City) {
            this.name = cultureType.nameGenerator.generateCity();
        }
        if (this instanceof Castle) {
            this.name = cultureType.nameGenerator.generateCastle();
        }
        if (this instanceof Village) {
            this.name = cultureType.nameGenerator.generateVillage();
        }
        if (this instanceof Ruin) {
            this.name = cultureType.nameGenerator.generateRuins();
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
	protected void setCorner(Corner corner) {
		this.corner = corner.index;
		this.initializeBiomeDistributions();

		// problem is that boime distribution hasn't been initialized yet
		updateToHire();
		updateToHire();
		//		this.initializeGarrison();
	}
	protected void setCenter(Center center) {
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
			if (this.garrison.getKingdom() != null) {
				this.garrison.act(delta);
			}
			
			if (this.garrison == null || this.garrison.getFaction() != this.getFaction()) {
				System.out.println("garrison of " + this.getName() + " is null");
				createGarrison();
			}
			
			if (!this.garrison.isInBattle()) {
				manageGarrison();
			}
			

			if (autoManage) {
				autoManage();
			}
			if (timeSinceFreshHire >= HIRE_REFRESH) {
				timeSinceFreshHire = 0;
				if (biomeDistribution != null)
				    updateToHire();
			}
			else timeSinceFreshHire += delta;

			if (!kingdom.isPaused()) {
				hostilePlayerTouched = false; // only can be selected when game is paused;
			}
		}
		fireAct(delta);
	}
	
	public void fireAct(float delta) {
		if (fire == null) return;
		super.act(delta);	
		fire.setPosition(this.getWidth()/2, this.getHeight()/2);
	}
	
	// make sure garrison is at least size it should be
	public void manageGarrison() {
		while (shouldIncreaseGarrison()) {
			if (!increaseGarrison()) break;
		}
	}
	
	// sometimes this decreases the size of the playerPartyPanel?
	public boolean increaseGarrison() {
		System.out.println("increasing garrison of " + this.getName() + " from " + this.garrison.party.getAtk());
		Soldier rand = new Soldier(this.garrison.getPartyType().randomSoldierType(), this.garrison.party);
		if (this.garrison.party.addSoldier(rand, true)) {
			if (!this.isCastle())
				this.loseWealth((int) (rand.getBuyCost() * GARRISON_DISCOUNT));
			System.out.println(" to " + this.garrison.party.getAtk());
			return true;
		}
		else return false;
	}
	
	// do this for villages and cities, but not for castles
	public boolean shouldIncreaseGarrison() {
		return this.garrison.party.getAtk() < this.getWealth() * garrisonBudget;
//		return this.getWealth() > 100;
	}

	public void autoManage() {
		//contains actions in extensions
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
//		if (!shouldDraw()) return;
		// Don't draw if covered by fog (even partially)
		if (inFog()) return;
		
		setRotation(kingdom.getMapScreen().rotation);
		float scale = 4f;
		scale *= this.getSizeFactor();
		scale *= getAdjustedZoom(getKingdom());

        batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), (int) (10 * scale), (int) (10 * scale), getRotation());
		// for drawing candles
		super.draw(batch, parentAlpha);
	}

	// TODO make scale limits apply only to zoom level
	public float getSizeFactor() {
	    float scale = 2f + ((float) (this.population - this.POP_MIN) / (this.POP_MAX - this.POP_MIN));
	    if (this.population == 0) scale = 2.5f;
	    scale = scale / 2;
        if (cultureType.name.equals("Desert")) return 1.5f*scale;
        if (cultureType.name.equals("Tundra")) return 1.5f*scale;
        return scale;
//		if (this.isCastle()) {
//		    if (cultureType.name.equals("Forest"))
//		        return 3f;
//            if (cultureType.name.equals("Plains"))
//                return 3f;
////		    return 3.0f;/
//		    return 0.75f;
//        }
//		else if (this.ruin) return 0.75f;
//
//		float MAX_SCALE = 3f;
//		float MIN_SCALE = 2f;
//
//		float scale = (float) (this.population - this.POP_MIN) / (this.POP_MAX - this.POP_MIN);
//
////		float scale = (float) population / ((this.POP_MIN + this.POP_MAX)/ 2);
////		if (scale > 1.5f) return 1.5f;
////		if (scale < 0.5) return 0.5f;
//
//		scale = scale * (MAX_SCALE - MIN_SCALE) + MIN_SCALE;
//
//		if (this.isVillage()) {
//		    if (cultureType.name.equals("Plains")) {
//                return scale * 4;
//            }
//            if (cultureType.name.equals("Forest")) {
//                return scale * 4;
//            }
//            if (cultureType.name.equals("Tundra")) {
//                return scale * 1000;
//            }
//		    return 0.5f * scale;
//        }
//        if (cultureType.name.equals("Forest")) {
//            return scale * 4;
//        }
//        if (cultureType.name.equals("Plains")) {
//            return scale * 4;
//        }
//		return scale;
		
//		if (this.isCastle()) {
//			return 1;
//		}
//		else if (this.ruin) return 1;
//		else if (this.isVillage()) { 
//			float scale = (float) (population / this.POP_MIN);
//			if (scale > 2) return 2;
//			if (scale < 0.5) return 0.5f;
//			return scale;
//		}
//		else {
//			float scale = (float) (population / 10000);
//			if (scale > 2) return 2;
//			if (scale < 0.5) return 0.5f;
//			return scale;
//		}
	}
	
	public static float getAdjustedZoom(Kingdom kingdom) {
	    float zoom = kingdom.getZoom();
		if (zoom < MIN_ZOOM) zoom = MIN_ZOOM;
		if (zoom > MAX_ZOOM) zoom = MAX_ZOOM;
		return zoom;
	}

	public void setDiscovered() {
        discovered = true;
    }

    public boolean isDiscovered() {
	    return discovered;
    }

//	private boolean inFog() {
//	    return !discovered;
////		if (kingdom.getMapScreen().fogOn) {
////			// Easy to optimize this -- just have a "discovered" boolean for all locations.
////			if (getCenter() != null) {
////				if (!getCenter().discovered) {
////					return true;
////				}
////			} else if (getCorner() != null) {
////				for (Center center : getCorner().touches) {
////					if (!center.water && !center.discovered) {
////						return true;
////					}
////				}
////			}
////		}
////		return false;
//	}

	// lets be smart about this
	// how exactly do we want to have cities etc disappear as you zoom out?
	
	// first off lets do city scale/size. are sizes appropriate? Max size should be smaller
	public void drawCrest(SpriteBatch batch) {
		if (this.getFaction() == null) return;
		if (inFog()) return;
		
		float scale = 1.7f;
//        scale *= this.getSizeFactor();
        scale *= getAdjustedZoom(getKingdom());

//		if ((this.type == LocationType.VILLAGE))
//			size_factor = .5f * size_factor;
//		if ((this.type == LocationType.CASTLE))
//			size_factor = .8f * size_factor;

		Color temp = batch.getColor();

		if (shouldDraw()) {
			// draw crest			
			batch.setColor(clear_white);

			mx4Font.idt();
			mx4Font.rotate(new Vector3(0, 0, 1), getKingdom().getMapScreen().getRotation());
			mx4Font.trn(getCenterX(), getCenterY(), 0);
			Matrix4 tempMatrix = batch.getTransformMatrix();
			batch.setTransformMatrix(mx4Font);
			
//			batch.draw(this.getFaction().crest, -15*zoom, 5 + 5*zoom, 30*zoom, 45*zoom);
			faction.crest.setPosition(-15*scale , 15*scale);
			faction.crest.setSize(30*scale, 30*scale);
//			batch.draw(this.getFaction().crest, -15*zoom, 5 + 5*zoom, 30*zoom, 45*zoom);
			faction.crest.draw(batch, clear_white.a);
			
			batch.setColor(temp);

			batch.setTransformMatrix(tempMatrix);
		}
	}

	public boolean inFog() {
        if (!discovered && kingdom.getMapScreen().fogOn) return true;
        return false;
    }
	
	public boolean shouldDraw() {
		float zoom = getKingdom().getZoom();
		if (zoom > 6 * getSizeFactor() || zoom > MAX_ZOOM) return false;
		return true;
	}

//	public boolean shouldDrawText(){
//	    float zoom = getKingdom().getZoom();
//	    if (zoom > MAX_ZOOM) return false;
//	    return true;
//    }

	public void drawText(SpriteBatch batch) {
		if (inFog()) return;
		
		float size_factor = getSizeFactor();
//
//		if ((this.type == LocationType.VILLAGE))
//			size_factor = .5f * size_factor;
//		if ((this.type == LocationType.CASTLE || this.type == LocationType.RUIN))
//			size_factor = .8f * size_factor;

		float zoom =  getAdjustedZoom(kingdom);
		zoom *= size_factor; 

		if (shouldDraw()) {
			BitmapFont font;			
			font = Assets.pixel20forCities;

			String toDraw = getName();

			mx4Font.idt();
			mx4Font.rotate(new Vector3(0, 0, 1), getKingdom().getMapScreen().getRotation());
			mx4Font.trn(getX(), getY(), 0);
			Matrix4 tempMatrix = batch.getTransformMatrix();
			batch.setTransformMatrix(mx4Font);

			// TODO draw with stroke, or with dark background
			
			// draw background
			int fontHeight = 17;
			float fontWidthScale = 5f * toDraw.length() + 5f;
			Color c = batch.getColor();
			batch.setColor(black);
			float yPos = -16*zoom;
			batch.draw(Assets.white, -(int) (4.3*toDraw.length())*zoom - zoom * fontWidthScale / 10, yPos - fontHeight*zoom,  (9*toDraw.length() + 10)*zoom, zoom*fontHeight);
			batch.setColor(c);
			
//			font.setColor(black);
//			float bgScale = 1.05f;
//			font.setScale(zoom * bgScale);	
//			font.draw(batch, toDraw, -(int) (4.3*toDraw.length())*zoom * bgScale, -8*zoom * bgScale);
						
			// draw text
			font.setColor(clear_white);
			font.setScale(zoom);
			font.draw(batch, toDraw, -(int) (4.3*toDraw.length())*zoom, yPos);
			
			
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
		StrictArray<City> newCloseEnemyCities = new StrictArray<City>();
		StrictArray<City> newCloseFriendlyCities = new StrictArray<City>();

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
		closestEnemyCities = new StrictArray<City>(newCloseEnemyCities);
		closestFriendlyCities = new StrictArray<City>(newCloseFriendlyCities);

		StrictArray<Castle> newCloseEnemyCastles = new StrictArray<Castle>();
		StrictArray<Castle> newCloseFriendlyCastles = new StrictArray<Castle>();

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
		closestEnemyCastles = new StrictArray<Castle>(newCloseEnemyCastles);
		closestFriendlyCastles = new StrictArray<Castle>(newCloseFriendlyCastles);

		StrictArray<Village> newCloseEnemyVillages = new StrictArray<Village>();
		StrictArray<Village> newCloseFriendlyVillages = new StrictArray<Village>();

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
		closestEnemyVillages = new StrictArray<Village>(newCloseEnemyVillages);
		closestFriendlyVillages = new StrictArray<Village>(newCloseFriendlyVillages);
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

	public StrictArray<Patrol> getPatrols() {
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
	public void siegeAttack(StrictArray<Army> attackers) {
		needsUpdate = true;

		//		Army garrisonArmy = new Army(getKingdom(), this.getName() + " Garrison", getFaction(), getCenterX(), getCenterY(), null);
		//		garrisonArmy.setParty(garrison);
		//		if (this.location. == null) {
		//			System.out.println("FUCK no besieging");
		//			return;
		//		}

		if (playerWaiting) {
			// create array of defenders
			StrictArray<Army> defendersArmies = this.getGarrisonedAndGarrison();
			StrictArray<Party> defenders = new StrictArray<Party>();
			for (Army a : defendersArmies) 
				defenders.add(a.party);
				
			StrictArray<Party> attackerParties = new StrictArray<Party>();
			for (Army a : attackers) {
				attackerParties.add(a.party);
			}
			kingdom.getPlayer().createPlayerBattleWith(defenders, attackerParties, true, this);
		}
		else {
			attackers.first().createBattleWith(garrison, this);
			BattleActor battleActor = garrison.getBattleActor();
			if (battleActor != null) {
				battleActor.setSiegeLocation(this);

				//			System.out.println("siegeOf = " + this.getName());
				battleActor.setPosition(this.getX()-this.getWidth()/2, this.getY()-this.getHeight()/2);
				battleActor.getBattle().setDefensiveAdvantage(this.getDefenseFactor());
			}
			for (Army a : attackers) {
				if (a.getParty().player) ;
				// bring up option to attack, pause/stay etc
				if (a != attackers.first()) {
					a.joinBattle(battleActor);
				}
			}
			for (Army a : garrisonedArmies) {
				//			System.out.println("adding " + a.getName() + " to siege battle");
				if (a.passive) continue; // don't add passive armies to defenders
				a.joinBattle(battleActor);
			}
		}
	}
	
	public void beginSiege(Army army) {
		needsUpdate = true;

		siege = new Siege(this, army.getFaction());
		siege.add(army);
		kingdom.addActor(siege);
		
		this.addFire();
	}
	public void joinSiege(Army army) {
		needsUpdate = true;

		// htis is never being called
		System.out.println(army.getName() + " JOINING SIEGE");
		siege.add(army);
		if (garrison.getBattle() != null) {
			if (garrison.getBattle().shouldJoinAttackers(army)) {
				garrison.getBattle().addToAttackers(army);
			} else if (garrison.getBattle().shouldJoinDefenders(army)) {
				garrison.getBattle().addToDefenders(army);
			}
		}
	}

	public void endSiege() {
		needsUpdate = true;

		kingdom.removeActor(siege);
		siege = null;
		playerBesieging = false;
		
		this.removeFire();
	}
	public boolean underSiege() {
		return siege != null;
	}
	public Siege getSiege() {
		return siege;
	}
	public void addFire() {
		this.fire = new Fire(this.getWidth() * 20, this.getHeight() * 20, kingdom.getMapScreen(), this);
		this.addActor(fire);
	}
	public void removeFire() {
		this.removeActor(fire);
		this.fire = null;
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
		needsUpdate = true;
		garrisonedArmies.add(army);
		army.setVisible(false);
		army.setPosition(this.spawnPoint.getX()-army.getOriginX(), spawnPoint.getY()-army.getOriginY());

		// attmepting this
		//kingdom.removeArmy(army);
	}
	
	// ejecting armies was duplicating them previously.
	public void eject(Army army) {
		needsUpdate = true;
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
		if (toHire.getHealthy().contains(s, true) && !party.isFull()) {
			if (party.wealth - s.getHireCost() >= party.minWealth) {
				party.wealth -= s.getHireCost();
				toHire.removeSoldier(s);
				party.addSoldier(s, false);
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
		needsUpdate = true;
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
			if (this.faction != null)
				this.faction.villages.removeValue((Village) this, true);
			if (newFaction != null)
			    newFaction.villages.add((Village) this);
			//			((Village) this).farmers = new StrictArray<Farmer>(((Village) this).farmers);
			// TODO undo this
		}

		this.faction = newFaction;
		if (this.garrison != null) {
			if (this.type == LocationType.VILLAGE) {
                this.garrison.setFaction(newFaction);
            }
			else createGarrison();
//			this.garrison.setFaction(newFaction);
		}
		

		if (this.type != LocationType.VILLAGE) {
			// swap captured and garrison
			StrictArray<Soldier> oldCaptured = new StrictArray<Soldier>(garrison.getParty().getPrisoners());
			garrison.getParty().clearPrisoners();

			for (Soldier newPrisoner : garrison.getParty().getWounded()) 
				this.garrison.getParty().givePrisoner(newPrisoner, this.garrison.getParty());
			for (Soldier newPrisoner : garrison.getParty().getHealthy())
				this.garrison.getParty().givePrisoner(newPrisoner, this.garrison.getParty());

			for (Soldier newSoldier : oldCaptured) 
				this.garrison.getParty().addPrisoner(newSoldier);

			kingdom.updateFactionCityInfo();
		}
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
//	public void setParty(Party playerPartyPanel) {
//		this.garrison.setParty(playerPartyPanel);
//	}
	public double getPop() {
		return population;
	}
	public void addWealth(int wealth) {
//		this.getParty().wealth += wealth;
		this.wealth += wealth;
	}
	public void loseWealth(int wealth) {
//		this.getParty().wealth -= wealth;
		this.wealth -= wealth;
	}

	public double distTo(Location location) {
		return Math.sqrt((this.getCenterY() - location.getCenterY())*(this.getCenterY() - location.getCenterY()) + 
				(this.getCenterX() - location.getCenterX())*(this.getCenterX() - location.getCenterX()));
	}

	public double distTo(double x, double y) {
		return Math.sqrt((this.getCenterY() - y)*(this.getCenterY() - y) + 
				(this.getCenterX() - x)*(this.getCenterX() - x));
	}

	public void setWealth(int wealth) {
		this.wealth = wealth;
	}
	
	public int getWealth() {
//		return this.getParty().wealth;
		return this.wealth;
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
				kingdom.getMapScreen().getSidePanel().returnToPrevious(false);
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
		this.initializeBox();
	}

	public StrictArray<Army> getGarrisoned() {
		return garrisonedArmies;
	}

	// Doesn't return passive armies
	public StrictArray<Army> getGarrisonedAndGarrison() {
		StrictArray<Army> garrisoned = new StrictArray<Army>(getGarrisoned());
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
		if (faction == null) {
		    if (this.ruin) return "Abandoned";
		    else return "Independent";
        }
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
