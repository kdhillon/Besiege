package kyle.game.besiege.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.Assets;
import kyle.game.besiege.Random;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.Unit.Orientation;
import kyle.game.besiege.party.UnitDraw;
import kyle.game.besiege.voronoi.Biomes;

import static kyle.game.besiege.battle.BattleMap.GroundType.*;


public class BattleMap extends Group {
	private TextureRegion white;
	private static final float SIZE_FACTOR = 1f; //  change this to increase the drawn area around the battlefield. Cannot exceed 2
	private static final float WALL_SLOW = .5f;
	private static final float LADDER_SLOW = .75f;
    private static final double RAIN_INTENSITY = 2f;
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, .13f);
	public static final Color RAINDROP_COLOR = new Color(0, 0, .8f, 1f); // ALPHA is replaced later of course.
	public static final Color SNOW_COLOR = new Color(.7f, .7f, .8f, 1f);
	private static final Color CLEAR_WHITE = new Color(1, 1, 1, .5f);
	private static final Color PLACEMENT_COLOR = new Color(0, 1, 0, .2f);
	private static final Color COVER_COLOR = new Color(1, 1, 0, .5f);
	private static final Color CLOSED_COLOR = new Color(1, 0, 0, .5f);
	private static final Color RANGE_COLOR = new Color(1, 0, 0, .15f);
	private static final Color LOS_COLOR = new Color(1, 1, 0, .15f);
	private static final Color HIDE_COLOR = new Color(0, 0, 1, .15f);

	private static final int TREE_X_OFFSET = 1;
	private static final int TREE_Y_OFFSET = 1;
	private static final int TREE_WIDTH = 3;
	private static final int TREE_HEIGHT = 3;
	public static final float CASTLE_WALL_HEIGHT_DEFAULT = .5f;

	// This is the background color
	public Color bgColor = new Color();

	public enum MapType {
		FOREST, BEACH, GRASSLAND, SWAMP, DESERT, SNOW, TUNDRA, PRARIE, CRAG, JUNGLE, MOUNTAINS
	}
	private MapType maptype;
	public static MapType getRandomMapType() {
		return (MapType) Random.getRandomValue(MapType.values());
	}

	private BattleStage stage;

	public static final int BLOCK_SIZE = 4;
	private int total_size_x;
	private float edge_size_percent;// percent of drawn bg that is off map

	public Array<Ladder> ladders;
	public Array<BPoint> entrances;

	private int currentRainIndex;
	private BPoint[] raindrops;
	private Color rainColor;
	private final float SECONDS_PER_DROP = 1f;
	private int raindropsPerFrame; // this needs to be based on raindrop_count
	private int raindrop_count;
    private boolean snowing; // used for rare cases where mountaintops aren't snowing
	private Color groundcolor = new Color();


	private class Wall {
		int pos_x;
		int pos_y;
		int hp;
		boolean damaged;
		Orientation orientation;
		int size; // original thickness of wall
	}

	public Array<Wall> walls;

	//	private Array<Object> walls;
	
	public float sunRotation;
	public float sunStretch;

	public int wallTop; 
	public int wallLeft;
	public int wallRight;
	public int wallBottom;

	private boolean wallDamaged;

	// Also have a stealth bonus.
	public enum GroundType {
		GRASS(1.2),
		DARKGRASS(1.5),
		DIRT(1.2),
		SAND(1),
		LIGHTSAND(1),
		MUD(1.2),
		WATER(1),
		LIGHTGRASS(1),
		SNOW(1),
		LIGHTSNOW(1),
		ROCK(1.2),
		DARKROCK(1.5),
		FLOWERS(1),
		FLOWERS2(1),
		SWAMP(1.5),
		SWAMP2(1.8);

		public double stealthBonus;

		GroundType(double stealthBonus) {
			this.stealthBonus = stealthBonus;
		}
	}

	private GroundType getGroundAt(int x, int y) {
		return ground[x / BLOCK_SIZE][y / BLOCK_SIZE];
	}

	public GroundType getGroundAt(Unit unit) {
		if (unit == null) return null;
		if (!unit.inMap()) return null;
		return getGroundAt(unit.pos_x, unit.pos_y);
	}

	public enum Object { //CASTLE_WALL(.058f)
		TREE(.5f), TREE_ON_FIRE(.5f),
		DARK_TREE(.5f), DARK_TREE_ON_FIRE(.5f),
		SNOW_TREE(.5f), SNOW_TREE_ON_FIRE(.5f),
		PALM(.5f), PALM_ON_FIRE(.5f),
		PALM_DARK(.5f), PALM_DARK_ON_FIRE(.5f),
		STUMP(.1f), SMALL_WALL_V(.099f), SMALL_WALL_H(.099f), CASTLE_WALL(.06f, 20), CASTLE_WALL_FLOOR(0f, 20), COTTAGE_LOW(.1f), COTTAGE_MID(.12f), COTTAGE_HIGH(.14f), FIRE_SMALL(0.0f);
		float height;
		Orientation orientation; // for ladders
		int hp; // for walls
		private Object(float height) {
			this.orientation = Orientation.UP;
			this.height = height;
			this.hp = 1;
		}
		private Object(float height, int hp) {
			this.orientation = Orientation.UP;
			this.height = height;
			this.hp = hp;
		}
	}

	public class Ladder {
		int pos_x, pos_y;
		Orientation orientation;
	}

	public Array<BPoint> cover; // points with protection
	private GroundType[][] ground;
	private StrictArray<GroundType> groundTypes = new StrictArray<>();

	// try this, more memory intensive but less gpu intensive
	private TextureRegion[][] groundTexture;

	public Object[][] objects;
	public float obscurity_factor; // This makes it harder to see units on certain types of maps.

	private float rainDrawOffsetX;
	private float rainDrawOffsetY;

	private StrictArray<FireContainer> fc;

	//	private Pixmap grass, flowers, flowers2, dirt, sand, swamp, swamp2, darkgrass, mud, water, lightgrass, rock, darkrock, snow, lightsnow, lightsand;
	private TextureRegion wallV, wallH, castleWall, castleWallFloor, ladder, tree, snowDarkTree, darkTree, stump, palm, palmDark, treeShadow, palmShadow;

	private int min_x;
	private int max_x;

	public BattleMap(BattleStage mainmap, MapType mapType) {
		this.stage = mainmap;

		//		this.maptype = randomMapType();
		if (mapType == null) {
			this.maptype = getMapTypeForBiome(mainmap.biome);
		} else {
			this.maptype = mapType;
		}

		// total height is twice as big as normal size, for a massive map
		this.total_size_x = (int) (mainmap.size_x * SIZE_FACTOR);
//		this.total_size_y = (int) (mainmap.size_y * SIZE_FACTOR);

		this.edge_size_percent = (SIZE_FACTOR - 1) / SIZE_FACTOR / 2;

		ground = new GroundType[mainmap.size_x/ BLOCK_SIZE][mainmap.size_y/ BLOCK_SIZE];
		objects = new Object[mainmap.size_y][mainmap.size_x];
		ladders = new Array<>();
		entrances = new Array<BPoint>();
		cover = new Array<BPoint>();
		walls = new Array<Wall>();

		tree = 		Assets.map.findRegion("tree2");
		darkTree = 	Assets.map.findRegion("tree dark");
		snowDarkTree = 	Assets.map.findRegion("tree snow dark");
		palm =   	Assets.map.findRegion("palm");
        palmShadow = Assets.map.findRegion("palmShadow");
		palmDark =   	Assets.map.findRegion("palm red");

		treeShadow = Assets.map.findRegion("treeShadow");
        stump = 	Assets.map.findRegion("stump");
		wallV = 	Assets.map.findRegion("stone fence v");
		wallH = 	Assets.map.findRegion("stone fence");

		castleWall = 		Assets.map.findRegion("castle wall wood");
		castleWallFloor =  Assets.map.findRegion("castle wall floor wood");
		ladder = 			Assets.map.findRegion("ladder");

		white = new TextureRegion(new Texture("whitepixel.png"));

		this.sunStretch = (float) Math.random() * 1 + 0.5f;
		this.sunRotation = (float) Math.random() * 360;

		fc = new StrictArray<FireContainer>();

		min_x = 0;
		max_x = stage.size_x;

		if (stage.isRaining() || stage.isSnowing()) {
		    raindrop_count = 100 + (int) (Math.random() * 400);
		    // 100 - 500 is good

			raindrops = new BPoint[raindrop_count];
			for (int i = 0; i < raindrop_count; i++) {
				raindrops[i] = new BPoint(0, 0);
			}

            float delta = 1.0f / 60;
            raindropsPerFrame = (int) (raindrop_count * delta / SECONDS_PER_DROP);
            if (raindropsPerFrame < 1) raindropsPerFrame = 1;
            System.out.println("raindrops per frame: " + raindropsPerFrame);

			if (stage.isRaining()) {
				this.rainColor = RAINDROP_COLOR;
//				this.rainColor.mul(stage.targetDarkness *1f);
			}
			else {
				this.rainColor = SNOW_COLOR;
			}
			//			System.out.println("snowing!!!");
		}

		// default values 
		wallTop = Integer.MAX_VALUE;
		wallLeft = Integer.MIN_VALUE;
		wallRight = Integer.MAX_VALUE;
		wallBottom = Integer.MIN_VALUE;

		//		wallTop = 60;
		//		wallLeft = 10;
		//		wallBottom = 10;
		//		wallRight = 60;

		if (stage.hasWall() && !stage.alliesDefending)
			wallBottom = stage.size_y * 2/ 3;

		// create castle
		//		if (stage.siegeDefense)
		//			wallTop = (int) (stage.size_y*.2f);
		//		if (stage.siegeAttack)
		//			wallBottom = (int) (stage.size_y*.8f);

		obscurity_factor = 1;

		// generate random map
		if (maptype == MapType.FOREST) {
			setGroundTypesWithProbabilities(new GroundType[]{GRASS, DARKGRASS, DIRT}, new double[]{0.33, 0.80});

			addAppropriateLocationFeatures();

			addFences(5);
			addTrees(.03*Math.random() + .01, new Object[]{Object.DARK_TREE, Object.TREE});
			obscurity_factor = 1.5f;
			bgColor = new Color(20/256f, 70/256f, 20/256f, 1);
		}
		if (maptype == MapType.GRASSLAND) {
			setGroundTypesWithProbabilities(new GroundType[]{LIGHTGRASS, GRASS, DIRT}, new double[]{0.5, 0.94});
			addAppropriateLocationFeatures();

			addTrees(.001, Object.TREE);
			addFences(1);
			bgColor = new Color(91f/256, 164/256f, 63/256f, 1);
		}
		if (maptype == MapType.PRARIE) {
			setGroundTypesWithProbabilities(new GroundType[]{GRASS, LIGHTGRASS, FLOWERS2, FLOWERS}, new double[]{0.7, 0.9, 0.95});
			addAppropriateLocationFeatures();

			addTrees(.01, Object.TREE);
			addFences(15);
			bgColor = new Color(91f/256, 164/256f, 63/256f, 1);
		}
		if (maptype == MapType.BEACH) {
			// this will have to be tweaked for the new map size
			double slope = Math.random()*3+3;
			double slope2 = Math.random()*1;
			double thresh = Random.getRandomInRange(BLOCK_SIZE * 1f, BLOCK_SIZE * 5f);

			int maxWaterX = 0;
			boolean left = Random.coinflip();
			for (int i = 0; i < ground.length; i++) {
				for (int j = 0; j < ground[0].length; j++) {
					int i1 = i;
					int j1 = j;
					if (!left) {
						i1 = ground.length - i - 1;
					}

					ground[i1][j1] = GroundType.SAND;
					if (Math.random() < .01) setGround(i1, j1, GroundType.MUD);
					double leftSide = slope*i + slope2*j;

					if (leftSide < thresh || (leftSide - thresh < 4 && Math.random() < .5)) {
						setGround(i1, j1, WATER);
						if (i > maxWaterX)
							maxWaterX = i;
						// set as closed
						closeGround(j1, i1);
					}
					else if (leftSide > thresh + Random.getRandomInRange(100/ BLOCK_SIZE, 150/ BLOCK_SIZE)) setGround(i1, j1, GroundType.LIGHTGRASS);
				} 
			}
			if (left) {
				min_x = (maxWaterX + 1) * BLOCK_SIZE;
			} else {
				max_x = (ground.length - maxWaterX ) * BLOCK_SIZE;
			}

			addAppropriateLocationFeatures();

			addTrees(.005, Object.PALM);

            bgColor = new Color(143f/256, 202/256f, 85/256f, 1);
		}
		if (maptype == MapType.DESERT) {
			setGroundTypesWithProbabilities(new GroundType[]{SAND, LIGHTSAND, DIRT, MUD}, new double[]{0.6, 0.98, 0.99});

			this.addFences(20);
			addAppropriateLocationFeatures();

			addTrees(Math.random() * 0.005, Object.PALM);

			bgColor = new Color(204/256f, 188/256f, 74/256f, 1);
		}
		if (maptype == MapType.MOUNTAINS) {
			setGroundTypesWithProbabilities(new GroundType[]{ROCK, SNOW, DARKROCK}, new double[]{0.6, 0.95});

			addAppropriateLocationFeatures();
			addStumps(.01);

			bgColor = new Color(150/256f, 150/256f, 150/256f, 1);
		}
		if (maptype == MapType.JUNGLE) {
			setGroundTypesWithProbabilities(new GroundType[]{SWAMP2, DARKGRASS, MUD}, new double[]{0.1, 0.8});

//			this.addFences(20);
			addAppropriateLocationFeatures();

			addTrees(0.03, new Object[]{Object.PALM_DARK, Object.PALM, Object.TREE, Object.DARK_TREE});

			bgColor = new Color(30/256f, 80/256f, 20/256f, 1);
		}
		if (maptype == MapType.SNOW) {
			setGroundTypesWithProbabilities(new GroundType[]{LIGHTSNOW, SNOW, DIRT, MUD}, new double[]{0.7, 1, 0.99});
			addAppropriateLocationFeatures();

			bgColor = new Color(0.95f, 0.95f, 0.95f, 1);
		}
		if (maptype == MapType.TUNDRA) {
			setGroundTypesWithProbabilities(new GroundType[]{LIGHTSNOW, SNOW, SWAMP, SWAMP2}, new double[]{0.3, 0.7, 0.9});
			addAppropriateLocationFeatures();

//			addTrees(.03*Math.random() + .01, new Object[]{Object.DARK_TREE, Object.TREE});
			addTrees(Random.getRandomInRange(0.005, 0.05), new Object[]{Object.DARK_TREE, Object.SNOW_TREE});
			addStumps(.01);


			bgColor = new Color(0.95f, 0.95f, 0.95f, 1);
		}
		if (maptype == MapType.CRAG) {
			setGroundTypesWithProbabilities(new GroundType[]{DARKROCK, MUD, ROCK}, new double[]{0.7, 0.9});
			stage.targetDarkness = .5f;

			addAppropriateLocationFeatures();

			addFire(.001);
			addStumps(.01);
			bgColor = new Color(58/256f, 47/256f, 45/256f, 1);
		}
		if (maptype == MapType.SWAMP) {
			setGroundTypesWithProbabilities(new GroundType[]{SWAMP, SWAMP2, DIRT}, new double[]{0.5, 0.95});
			addAppropriateLocationFeatures();

			bgColor = new Color(65/256f, 138/256f, 92/256f, 1);
		}

		// Manipulate background color based on time of day:
		if (stage.options != null)
			bgColor = blend(bgColor, stage.options.timeOfDay.tint);

		// remove cover on top of objects
		for (BPoint p : cover) {
			if (objects[p.pos_y][p.pos_x] != null || stage.closed[p.pos_y][p.pos_x]) {
				//				System.out.println("removing value from cover");
				//				cover.removeValue(p, true);
				// doesn't work for some reason

			}
		}

		if (stage.isSnowing()) obscurity_factor *= 1.5f;
		if (stage.isRaining()) obscurity_factor *= 1.2f;

		rainDrawOffsetX = 0;
		rainDrawOffsetY = 0;

		initializeGround();
	}

	private Color blend(Color c1, Color c0) {
		c0 = new Color(c0);
		c1 = new Color(c1);

		// r0 over 1 = (1 - a0)·r1 + a0·r0
		Color c3 = new Color();
		c3.r = (1 - c0.a) * c1.r + c0.a * c0.r;
		c3.g = (1 - c0.a) * c1.g + c0.a * c0.r;
		c3.b = (1 - c0.a) * c1.b + c0.a * c0.r;

		c3.a = 1;
		return c3;
	}


	// Example: {GRASS, DARKGRASS, DIRT}, {0.33, 0.83}
	// That means theres's a 33% chance of grass, 0.5 chance for darkgrass, and a 0.17 chance of dirt.
	private void setGroundTypesWithProbabilities(GroundType[] types, double[] probs) {
		for (int i = 0; i < ground.length; i++) {
			for (int j = 0; j < ground[0].length; j++) {
				double random = Math.random();

				for (int k = 0; k < types.length; k++) {
					// Use default if no ground set yet.
					if (k >= probs.length || random < probs[k]) {
						setGround(i, j, types[k]);
						break;
					}
				}
			}
		}
	}

	private void setGround(int i, int j, GroundType groundType) {
		ground[i][j] = groundType;
		if (!groundTypes.contains(groundType, true)) groundTypes.add(groundType);
	}

	// Takes into account water, etc.
	public int getMinX() {
		return min_x;
	}

	public int getMaxX() {
		return max_x;
	}

	public void initializeGround() {
		//		Texture[][] baseTextures = new Texture[ground.length][ground[0].length];
		groundTexture = new TextureRegion[ground.length][ground[0].length];

		//		// create base texture, first test this
		//		for (int i = 0; i < groundTexture.length; i++) {
		//			for (int j = 0; j < groundTexture[0].length; j++) {
		////				Pixmap base = this.getTexture(ground[i][j]);
		////				
		////				Pixmap mask = this.getTexture(ground[0][0]);
		////
		////				Color c;
		////								
		////				for (int x = 0; x < base.getWidth(); x++) {
		////					for (int y = 0; y < base.getHeight(); y++) {
		////						int maskColor = mask.getPixel(x, y);
		////						System.out.println(maskColor);
		////						c = new Color(maskColor);
		////						c = new Color(c.r, c.g, c.b, 0.5f);
		////						base.setColor(c);						
		////						base.drawPixel(x, y);
		////					}
		////				}	
		////				
		////				groundTexture[i][j] = new TextureRegion(new Texture(base));
		//				groundTexture[i][j] = new TextureRegion(new Texture(this.getTexture(ground[i][j])));
		//			}
		//		}

		//		int half_width = (int) (getDrawWidth()/2);
		//		int half_height = (int) (getDrawHeight()/2);

		// apply pixmap layers

		// TODO this blending and the need to store so many textures might be the most expensive part of battle map. can test by having a small battle on a huge map.
		// then add blend textures (shift down and right 1)
		for (int i = 0; i < ground[0].length; i++) {
			for (int j = 0; j < ground.length; j++) {
				Pixmap current = getTexture(ground[j][i]);
				//					current.

				// DON'T mess with this, the algo is really good now.
				boolean blend = true;
				if (blend) {
					// now for each of the 8 (9) adjacent textures, blend them with appropriate corners of this guy

					// Option 1: draw one ground type at a time. e.g., draw the most popular ground type first, then accents on top of that. or vice versa for smoothing.
//					for (GroundType currentType : groundTypes) {
					// TODO randomize this a little bit
					int deltaX = 1;
					int deltaY = 1;

					int xStart = -1;
					int xEnd = 2;

					int yStart = -1;
					int yEnd = 2;

					// This doesn't work as intended because order does matter for two adjacent squares I think.
					// Not high priority.
//					if (Random.coinflip()) {
//						deltaX = -1;
//						xStart = 1;
//						xEnd = -2;
//					}
//					if (Random.coinflip()) {
//						deltaY = -1;
//						yStart = 1;
//						yEnd = -2;
//					}

					for (int x = xStart; x != xEnd; x += deltaX) {
						for (int y = yStart; y != yEnd; y += deltaY) {
							if (x + j < 0 || x + j >= ground.length || y + i <
									0 || y + i >= ground[0].length)
								continue;
//								if (ground[j + x][i + y] != currentType)
// continue;

							Pixmap mask = this.getTexture(ground[j + x][i +
									y]);

							Color c;

							float MAX_ALPHA = 0.6f;

							// Current blending algo --
							// for every square
							//    go through all 8 adjacent squares
							// 		go through every pixel in this guy
							// 		  blend this pixel's color with a random
							// pixel from the adjacent square
							// 	      blending is based on distance away from
							// that square.
							//
							// 	problems: every pixel in this light square
							// might be darkened by a dark square to the right, but a square to the left of this one won't be
							// affected by the dark one (because it's nonadjacent). so the border of this one and
							//  the neighbor won't be blended. simple fix: only let pixels that are actually
							// close to the dark square be affected (so the far edge won't see any darkening).
							// complex fix: have multiple passes for blending (using already blended textures for blending)

							// How to implement simple fix:
							// 	 the alpha calculation should be 0 for pixels that are width pixels away from the opposite
							// side, and 1 for pixels that are adjacent. alpha calculation should be based on distance
							// to the center of the adjacent square? or, based on distance to center of this square?
							for (int x_pix = 0; x_pix < current.getWidth(); x_pix++) {
								for (int y_pix = 0; y_pix < current.getHeight(); y_pix++) {
									// add some randomness for which pixel in
									// adjacent square we're talking about.
									int random_x = (int) (Math.random() * current.getWidth());
									int random_y = (int) (Math.random() * current.getHeight());

									int maskColor = mask.getPixel(random_x, random_y);
									c = new Color(maskColor);

									// position is relative to current boxs
									// bottom left corner (x_pix = 0, y_pix
									// = 0)
									float adjacentCenterX = x * current.getWidth() + current.getWidth() / 2;
									float adjacentCenterY = -y * current.getHeight() + current.getHeight() / 2;

									boolean useRandomInsteadOfCenter = false;
									if (useRandomInsteadOfCenter) {
										adjacentCenterX = x * current.getWidth() + random_x;
										adjacentCenterY = -y * current.getHeight() + random_y;
									}

									// First, calculate distance to center of adjacent square.
									float distanceToCenterOfAdjacent = (float)
											Math.sqrt((x_pix - adjacentCenterX) * (x_pix - adjacentCenterX)
													+ (y_pix - adjacentCenterY) * (y_pix - adjacentCenterY));

									// Distance to center of adjacent is proportional to the affect on alpha
									// Picture a square, and then draw a circle that's just small enough to fit
									// the square. That circle has radius = hypotenuse of the square.
									// Everything within that distance will have a high alpha impact. beyond that
									// will have diminishing alpha impact.
									float hypotenuse = (float) Math.sqrt((current.getWidth() / 2) * (current.getWidth
											() / 2) + (current.getHeight() / 2) * (current.getHeight() / 2));

									// Now that we have the minimum impact distance (the hypotenuse), we need the
									// maximum impact distance. Let's go as far as possible without
									// affecting non-adjacent squares. The distance from the center of this
									// square will simply be 1.5 * square width.
									float maxImpactDistance = 1.5f * current.getWidth();

									// Now, to calculate the distance impact.
									// Might be greater than one, but should never be less than 0.
									// if distance < hypotenuse, impact = 1;
									// if distance > maxImpactDistance, impact
									// = 0;


									//           |0       1|
									//   ----- -- --- -----
									//  o     |  )   o     |

									float distanceImpactInverted = (distanceToCenterOfAdjacent - hypotenuse) /
											(maxImpactDistance - hypotenuse);
									float distanceImpact = 1 - distanceImpactInverted;


									if (distanceImpact > 1) distanceImpact = 1;
									if (distanceImpact < 0) distanceImpact = 0;
									c.a = distanceImpact * MAX_ALPHA;

									current.setColor(c);
									current.drawPixel(x_pix, y_pix);
								}
							}
						}
					}
//					}
				}
				groundTexture[j][i] = new TextureRegion(new Texture(current));
			}
		}


		//		// convert to textureRegions
		//		for (int i = 0; i < groundTexture.length; i++) {
		//			for (int j = 0; j < groundTexture[0].length; j++) {
		//				groundTexture[i][j] = new TextureRegion(baseTextures[i][j]);
		//			}
		//		}
	}

	public static MapType getMapTypeForBiome(Biomes biome) {
		switch(biome) {
		case BEACH : 			            return MapType.BEACH;
		case SNOW : 		               	return MapType.SNOW;
		case TUNDRA : 			            return MapType.TUNDRA;
		case MOUNTAINS: 			        return MapType.MOUNTAINS;
		case SCORCHED :			            return MapType.CRAG;
		case TAIGA :			            return MapType.TUNDRA;
		case PLATEAU :                      return MapType.GRASSLAND;
		case SWAMP : 			            return MapType.SWAMP;
		case TEMPERATE_DECIDUOUS_FOREST : 	return MapType.FOREST;
		case GRASSLAND : 					return MapType.PRARIE;
		case SUBTROPICAL_DESERT : 			return MapType.DESERT;
		case SHRUBLAND: 					return MapType.GRASSLAND;
		case ICE : 							return MapType.SNOW;
		case MARSH : 						return MapType.SWAMP;
		case TROPICAL_RAIN_FOREST : 		return MapType.JUNGLE;
		case TROPICAL_SEASONAL_FOREST : 	return MapType.JUNGLE;
		case LAKESHORE: 					return MapType.BEACH;
		default : 							return MapType.GRASSLAND;
		}
	}

	private void addWall() {
		// figure out what kind of shape you want... 
		// different types of sieges: ladder, catapult, or already broken
		double percent_broken = .1;

		System.out.println("adding wall");

		if (wallTop != Integer.MAX_VALUE) {
			for (int i = Math.max(0, wallLeft); i < Math.min(total_size_x/SIZE_FACTOR, wallRight); i++) {
				if (Math.random() > percent_broken) {
					boolean bool = false;
					if (i % 10 == 7 || i % 10 == 5) bool = true;

					this.addSingleWall(i, wallTop, 3, Orientation.UP, bool);
				}
				else {
					BPoint entrance = new BPoint(i, wallTop);
					entrances.add(entrance);
				}
			} 
		}
		if (wallBottom != Integer.MIN_VALUE) {
			for (int i = Math.max(0, wallLeft); i < Math.min(total_size_x/SIZE_FACTOR, wallRight); i++) {
				if (Math.random() > percent_broken) {
					boolean bool = false;
					if (i % 10 == 7 || i % 10 == 5) bool = true;

					this.addSingleWall(i, wallBottom, 3, Orientation.DOWN, bool);
				}
				else {
					BPoint entrance = new BPoint(i, wallBottom);
					entrances.add(entrance);
				}
			} 

		}
		if (wallRight != Integer.MAX_VALUE) {
			for (int i = Math.max(0, wallBottom); i < Math.min(total_size_x/SIZE_FACTOR, wallTop); i++) {
				if (Math.random() > percent_broken) {
					boolean bool = false;
					if (i % 10 == 7 || i % 10 == 5) bool = true;

					this.addSingleWall(wallRight, i, 3, Orientation.RIGHT, bool);
				}
				else {
					BPoint entrance = new BPoint(wallRight, i);
					entrances.add(entrance);
				}
			} 
		}
		if (wallLeft != Integer.MIN_VALUE) {
			for (int i = Math.max(0, wallBottom); i < Math.min(total_size_x/SIZE_FACTOR, wallTop); i++) {
				if (Math.random() > percent_broken) {
					boolean bool = false;
					if (i % 10 == 7 || i % 10 == 5) bool = true;

					this.addSingleWall(wallLeft, i, 3, Orientation.LEFT, bool);
				}
				else {
					BPoint entrance = new BPoint(wallLeft, i);
					entrances.add(entrance);
				}
			} 
		}
	}

	private void addAppropriateLocationFeatures() {
		if (stage.hasWall()) {
			addWall();
		}
		else if (stage.isVillage()) {
			// TODO have addVillage();
			addRuins();
		} else if (stage.isRuins()) {
			addRuins();
		}
	}

	private void addLadder(int pos_x, int pos_y, Orientation orientation) {
		Ladder l = new Ladder();
		l.pos_x = pos_x;
		l.pos_y = pos_y;
		l.orientation = orientation;
		this.ladders.add(l);
		stage.slow[l.pos_y][l.pos_x] = LADDER_SLOW;
	}

	public void removeLadderAt(int pos_x, int pos_y) {
		for (Ladder l : ladders) {
			if (l.pos_x == pos_x && l.pos_y == pos_y) ladders.removeValue(l, true);
		}
		stage.slow[pos_y][pos_x] = 0;
	}

	public boolean ladderAt(int pos_x, int pos_y) {
		return getLadderAt(pos_x, pos_y) != null;
	}
	public Ladder getLadderAt(int pos_x, int pos_y) {
		for (Ladder l : ladders) {
			if (l.pos_x == pos_x && l.pos_y == pos_y) return l;
		}
		return null;
	}

	public boolean entranceAt(int pos_x, int pos_y) {
		for (BPoint l : entrances) {
			if (l.pos_x == pos_x && l.pos_y == pos_y) return true;
		}
		return false;
	}	

	private void addCottage() { 
		int MIN_SIZE = 5;
		int MAX_SIZE = 10;

		int size_x, size_y;
		size_x = size_y = (int) (Math.random() * (MAX_SIZE - MIN_SIZE) + MIN_SIZE);

		// find clear region 



	}

	//	private void addTower(int y_position) {
	//		
	//		// add ladders
	//		if (addObject(stage.size_x/2, y_position+1, Object.LADDER)) {
	//			stage.slow[y_position+1][stage.size_x/2] = LADDER_SLOW;
	//			stage.closed[y_position+1][stage.size_x/2] = false;
	//		}
	//		
	//		for (int i = 0; i < stage.size_x; i++) {
	//			if (addObject(i, y_position, Object.CASTLE_WALL_FLOOR)) {
	//				stage.heights[y_position][i] = CASTLE_WALL_HEIGHT_DEFAULT; // close random middle row
	//				Point coverPoint = new Point(i, y_position);
	//				coverPoint.orientation = Orientation.UP;
	//				if (inMap(coverPoint)) cover.add(coverPoint);
	//			}
	//		}
	//
	//		for (int i = 0; i < stage.size_x; i++) {
	//			if (addObject(i, y_position-1, Object.CASTLE_WALL_FLOOR))
	//				stage.heights[y_position-1][i] = CASTLE_WALL_HEIGHT_DEFAULT; // close random middle row
	//		}
	//		for (int i = 0; i < stage.size_x; i++) {
	//			if (addObject(i, y_position+1, Object.CASTLE_WALL)) {
	//				stage.heights[y_position+1][i] = CASTLE_WALL_HEIGHT_DEFAULT; // close random middle row
	//				stage.closed[y_position+1][i] = true;
	//			}
	//		}
	//		
	//		// add ladders
	//		if (addObject(stage.size_x/2-1, y_position-2, Object.LADDER))
	//			stage.slow[y_position-2][stage.size_x/2-1] = LADDER_SLOW;
	//		// add ladders
	//		if (addObject(stage.size_x/2+1, y_position-2, Object.LADDER))
	//				stage.slow[y_position-2][stage.size_x/2+1] = LADDER_SLOW;
	//	
	//	}


    // Returns true if any of the adjacent squares is obstructed
    private boolean adjacentObstructed(int x, int y) {
//        System.out.println("checking: " + x + " " + y);
        boolean obstructed = false;
        for (int i = x - 1; i <= x+1; i++) {
            for (int j = y - 1; j <= y+1; j++) {
                if (stage.inMap(i, j) && stage.closed[j][i]) { //!stage.inMap(i, j) ||
//                    System.out.println("checking: " + x + " " + y + ", obstructed by: " + i + " " + j);
                    obstructed = true;
                }
            }
        }
        return obstructed;
    }

	private void addSnowTrees(double probability) {
		for (int i = 0; i < stage.size_x; i++) {
			for (int j = 0; j < stage.size_y; j++) {
				GroundType g = getGroundAt(i, j);
				double prob = probability * getTreeProb(g);
				if (Math.random() < prob && objects[j][i] == null && !insideWalls(i, j) && stage.canPlaceUnit(i, j) && !adjacentObstructed(i, j)) {
					objects[j][i] = Object.SNOW_TREE;
					stage.closed[j][i] = true;
					//					mainmap.closed[i][j] = true;

				}
			}
		}
	}

	private void addTrees(double probability, Object[] array) {
		for (int i = 0; i < stage.size_x; i++) {
			for (int j = 0; j < stage.size_y; j++) {
				GroundType g = getGroundAt(i, j);
				double prob = probability * getTreeProb(g);
				if (this.maptype == MapType.BEACH) {
					if (Math.random() < probability && objects[j][i] == null && !insideWalls(i, j) && ground[i / BLOCK_SIZE][j / BLOCK_SIZE] == GroundType.LIGHTGRASS && !adjacentObstructed(i, j)) {
						objects[j][i] = (Object) Random.getRandomValue(array);
						stage.closed[j][i] = true;
					}
				} else if (Math.random() < prob && objects[j][i] == null && !insideWalls(i, j) && stage.canPlaceUnit(i, j) && !adjacentObstructed(i, j)) {
					objects[j][i] = (Object) Random.getRandomValue(array);
					stage.closed[j][i] = true;
					//					mainmap.closed[i][j] = true;

				}
			}
		}
	}

	private void addTrees(double probability, Object treeType) {
		Object[] array = new Object[1];
		array[0] = treeType;
		addTrees(probability, array);
    }

    private float getTreeProb(GroundType ground) {
		switch(ground) {
			case ROCK: return 0.1f;
			case DARKROCK: return 0.0f;
			case SAND: return 0.2f;
			case SNOW: return 0.5f;
			case DIRT: return 0.5f;
			case GRASS: return 1.5f;
			case DARKGRASS: return 3.5f;
			case MUD: return 0.5f;
			case WATER: return 0;
			default: return 1;
		}
	}

//    private void addPalms(double probability) {
//        for (int i = 0; i < stage.size_x; i++) {
//            for (int j = 0; j < stage.size_y; j++) {
//                // Only add palms on the grassy part of the map (not the sand)
//				if (this.maptype == MapType.BEACH) {
//					if (Math.random() < probability && objects[j][i] == null && !insideWalls(i, j) && ground[i / BLOCK_SIZE][j / BLOCK_SIZE] == GroundType.LIGHTGRASS && !adjacentObstructed(i, j)) {
//						objects[j][i] = Object.PALM;
//						stage.closed[j][i] = true;
//					}
//				}
//				else {
//					if (Math.random() < probability && objects[j][i] == null && !insideWalls(i, j) && !adjacentObstructed(i, j)) {
//						objects[j][i] = Object.PALM;
//						stage.closed[j][i] = true;
//					}
//				}
//            }
//        }
//    }

    private boolean fireAt(int posX, int posY) {
        Object o  = objects[posY][posX];
        if (o == Object.TREE_ON_FIRE || o == Object.PALM_ON_FIRE || o == Object.PALM_DARK_ON_FIRE || o == Object.FIRE_SMALL || o == Object.DARK_TREE_ON_FIRE ||  o == Object.SNOW_TREE_ON_FIRE) return true;
        return false;
    }

    public void createFireAt(int posX, int posY) {
        if (fireAt(posX, posY)) return;

        // Do this check so we can draw both tree and fire at the same time ;)
        // problem is it still allows multiple fires to be created on the same tree.
        // can solve with Object.TREE_ON_FIRE
        boolean shouldGrow = true;
        if (objects[posY][posX] == Object.TREE)
            objects[posY][posX] = Object.TREE_ON_FIRE;
		else if (objects[posY][posX] == Object.DARK_TREE)
			objects[posY][posX] = Object.DARK_TREE_ON_FIRE;
		else if (objects[posY][posX] == Object.SNOW_TREE)
			objects[posY][posX] = Object.SNOW_TREE_ON_FIRE;
        else if (objects[posY][posX] == Object.PALM)
            objects[posY][posX] = Object.PALM_ON_FIRE;
		else if (objects[posY][posX] == Object.PALM_DARK)
			objects[posY][posX] = Object.PALM_DARK_ON_FIRE;
        else {
            objects[posY][posX] = Object.FIRE_SMALL;
            shouldGrow = false;
        }
        stage.closed[posY][posX] = true;

        FireContainer fireContainer = new FireContainer();
        Fire fire = new Fire(800, 1000, stage.getMapScreen(), null, shouldGrow, false);
        fireContainer.addFire(fire);
        float y = posY * stage.unit_height + stage.unit_height * 0.5f;
        if (objects[posY][posX] == Object.TREE_ON_FIRE || objects[posY][posX] == Object.DARK_TREE_ON_FIRE || objects[posY][posX] == Object.SNOW_TREE_ON_FIRE) y = posY * stage.unit_height + stage.unit_height * 0.5f; // note we move it a bit down (for aesthetics)
        if (objects[posY][posX] == Object.PALM_ON_FIRE || objects[posY][posX] == Object.PALM_DARK_ON_FIRE) y = posY * stage.unit_height + stage.unit_height * 0.5f; // note we move it a bit down (for aesthetics)
        fireContainer.setPosition(posX * stage.unit_width + stage.unit_width * 0.5f, y); // we shift it a bit to the left to account for size.
        fc.add(fireContainer);
        //					fire.setPosition(0, 0);
        //			System.out.println("adding fire: " + j + " " + i);

        this.addActor(fireContainer);
	}

	private void addFire(double probability) {
		for (int i = 0; i < stage.size_x; i++) {
			for (int j = 0; j < stage.size_y; j++) {
				if (Math.random() < probability && objects[j][i] == null) {
					createFireAt(i, j);
				}	
			}
		}
	}

	private void addStumps(double probability) {
		for (int i = 0; i < stage.size_x; i++) {
			for (int j = 0; j < stage.size_y; j++) {
				if (Math.random() < probability && objects[j][i] == null) {
					objects[j][i] = Object.STUMP;
					stage.closed[j][i] = true;

					// add cover 
					BPoint cover_right = new BPoint(i+1, j);
					cover_right.orientation = Orientation.LEFT;
					if (inMap(cover_right) && objects[cover_right.pos_y][cover_right.pos_x] == null) cover.add(cover_right);

					BPoint cover_left = new BPoint(i-1, j);
					cover_left.orientation = Orientation.RIGHT;
					if (inMap(cover_left) && objects[cover_left.pos_y][cover_left.pos_x] == null) cover.add(cover_left);

					BPoint cover_up = new BPoint(i, j+1);
					if (inMap(cover_up) && objects[cover_up.pos_y][cover_up.pos_x] == null) cover.add(cover_up);
					cover_up.orientation = Orientation.DOWN;

					BPoint cover_down = new BPoint(i, j-1);
					if (inMap(cover_down) && objects[cover_down.pos_y][cover_down.pos_x] == null) cover.add(cover_down);
					cover_down.orientation = Orientation.UP;
				}	
			}
		}
	}


	// first draw wall at position, then floor, then
	private void addSingleWall(int pos_x, int pos_y, int width, Orientation orientation, boolean withLadder) {
		if (!inMap(new BPoint(pos_x, pos_y))) return;
		if (addWall(pos_x, pos_y, Object.CASTLE_WALL, orientation, width)) {
			stage.heights[pos_y][pos_x] = CASTLE_WALL_HEIGHT_DEFAULT; // close random middle row
			stage.closed[pos_y][pos_x] = true;
		}

		int vertFactor = 0;
		int horFactor = 0;

		if (orientation == Orientation.UP) vertFactor = -1;
		else if (orientation == Orientation.DOWN) vertFactor = 1;
		else if (orientation == Orientation.RIGHT) horFactor = -1;
		else if (orientation == Orientation.LEFT) horFactor = 1;

		// add floor behind wall
		for (int i = 1; i < width; i++) {
			if (addWall(pos_x + horFactor*i, pos_y + vertFactor*i, Object.CASTLE_WALL_FLOOR, orientation, width)) {
				stage.heights[pos_y + vertFactor*i][pos_x + horFactor*i] = CASTLE_WALL_HEIGHT_DEFAULT; 
			}
			if (i == 1) {
				BPoint coverPoint = new BPoint(pos_x + horFactor, pos_y + vertFactor);
				coverPoint.orientation = orientation;
				if (inMap(coverPoint)) cover.add(coverPoint);	
			}
		}
		if (withLadder) { 
			this.addLadder(pos_x+horFactor*width, pos_y+vertFactor*width, orientation);
		}
	}

	public void damageWallAt(int pos_x, int pos_y, float damage) {
		for (Wall wall : walls) {
			if (wall.pos_x == pos_x && wall.pos_y == pos_y) {
				wall.hp -= damage;
				wall.damaged = true;
				this.wallDamaged = true;
				boolean floor = false;
				if (wall.hp <= 0) {
					if (this.objects[pos_y][pos_x] == Object.CASTLE_WALL_FLOOR) floor = true;
					this.objects[pos_y][pos_x] = null;
					// delete entire wall?
					stage.heights[pos_y][pos_x] = 0;
					stage.closed[pos_y][pos_x] = false;

					//delete ladder;
					if (wall.orientation == Orientation.DOWN || wall.orientation == Orientation.UP) {
						if (ladderAt(pos_x, pos_y+1)) removeLadderAt(pos_x, pos_y+1);
						if (ladderAt(pos_x, pos_y-1)) removeLadderAt(pos_x, pos_y-1);
					}
					else {
						if (ladderAt(pos_x+1, pos_y)) removeLadderAt(pos_x+1, pos_y);
						if (ladderAt(pos_x-1, pos_y)) removeLadderAt(pos_x-1, pos_y);
					}

					// injure unit
					if (stage.units[pos_y][pos_x] != null) {
					    if (!stage.units[pos_y][pos_x].outOfBattle) {
                            stage.units[pos_y][pos_x].isDying = true;
                            stage.units[pos_y][pos_x].kill();
                        }
					}

					checkForNewEntrance(wall);
				}
			}
		}
	}

	private void checkForNewEntrance(Wall wall) {
		// find the appropriate point to add entrance at
		int entrance_x;
		int entrance_y;
		if (wall.orientation == Orientation.DOWN) {
			entrance_x = wall.pos_x;
			entrance_y = wallBottom;
			// make sure no walls left
			for (int i = 0; i < wall.size; i++)
				if (objects[entrance_y+i][entrance_x] != null) return;
		}
		else if (wall.orientation == Orientation.UP) {
			entrance_x = wall.pos_x;
			entrance_y = wallTop;
			// make sure no walls left
			for (int i = 0; i < wall.size; i++)
				if (objects[entrance_y-i][entrance_x] != null) return;
		}
		else if (wall.orientation == Orientation.LEFT) {
			entrance_x = wallLeft;
			entrance_y = wall.pos_y;
			// make sure no walls left
			for (int i = 0; i < wall.size; i++)
				if (objects[entrance_y][entrance_x+i] != null) return;
		}
		else {
			entrance_x = wallRight;
			entrance_y = wall.pos_y;
			// make sure no walls left
			for (int i = 0; i < wall.size; i++)
				if (objects[entrance_y][entrance_x-i] != null) return;
		}

		// add entrance
		BPoint entrance = new BPoint(entrance_x, entrance_y);
		this.entrances.add(entrance);
	}

	// We add Ruins to the side of the map where the defenders are
	private void addRuins() {
		int min_house_size = 5;
		int max_house_size = 10;

		int limit_min = 5;
		int limit_max = 10;

		int start_x = Random.getRandomInRange(limit_min, limit_max);
		int end_x = Random.getRandomInRange(stage.size_x - max_house_size - limit_max,  stage.size_x - max_house_size - limit_min);

//		int start_y = stage.MIN_PLACE_Y_1 + Random.getRandomInRange(-5, 5);
		int gap = 10;
		int start_y = 0;
		int end_y = stage.size_y / 2 - max_house_size;

		if (stage.playerAttacking()) {
			start_y = stage.size_y / 2;
			end_y = stage.size_y - max_house_size - 1;
		}

//		int end_y = Random.getRandomInRange(stage.size_y - max_house_size - limit_max,  stage.size_y - max_house_size - limit_min);

		for (int i = start_x; i < end_x; i++) {
			for (int j = start_y; j < end_y; j++) {
				if (Math.random() < 0.02) {
					int size = Random.getRandomInRange(min_house_size, max_house_size);
					addRuinSquare(i, j, size);
					j += size;
					i += Random.getRandomInRange(min_house_size, max_house_size);
				}
			}
		}

		// Also add some walls
		addFences(10);
	}

	private void addRuinSquare(int bottom_left_x, int bottom_left_y, int size) {
		addFence(bottom_left_x, bottom_left_y, size, true);
		addFence(bottom_left_x, bottom_left_y, size, false);

		addFence(bottom_left_x + size, bottom_left_y, size, true);
		addFence(bottom_left_x , bottom_left_y + size, size, false);

//		if (maptype == MapType.SNOW) return; // Don't do it for snow maps (snow covered the ground).

		// Make floor brown
		GroundType toUse = GroundType.DARKROCK;

		// Iterate through the center of the square and set all appropriate ground colors
		int gap = BLOCK_SIZE / 2;
		gap = 0;
		for (int i = bottom_left_x + gap; i < bottom_left_x + size - gap; i += BLOCK_SIZE) {
			for (int j = bottom_left_y + gap; j < bottom_left_y + size - gap; j += BLOCK_SIZE) {
				int x = (i + BLOCK_SIZE / 2) / BLOCK_SIZE;
				int y = (j+ BLOCK_SIZE / 2) / BLOCK_SIZE;
				if (ground[x][y] != GroundType.WATER) {
					setGround(x, y, toUse);
				}
			}
		}

//		ground[(bottom_left_x + size / 2) / BLOCK_SIZE][(bottom_left_y + size / 2)/BLOCK_SIZE] = toUse;
//		if (size >= BLOCK_SIZE * 2) {
//			ground[(bottom_left_x + size / 2) / BLOCK_SIZE + 1][(bottom_left_y + size / 2)/BLOCK_SIZE] = toUse;
//			ground[(bottom_left_x + size / 2) / BLOCK_SIZE][(bottom_left_y + size / 2)/BLOCK_SIZE + 1] = toUse;
//			ground[(bottom_left_x + size / 2) / BLOCK_SIZE + 1][(bottom_left_y + size / 2)/BLOCK_SIZE + 1] = toUse;
//
//			ground[(bottom_left_x + size / 2) / BLOCK_SIZE - 1][(bottom_left_y + size / 2)/BLOCK_SIZE] = toUse;
//			ground[(bottom_left_x + size / 2) / BLOCK_SIZE][(bottom_left_y + size / 2)/BLOCK_SIZE + 1] = toUse;
//			ground[(bottom_left_x + size / 2) / BLOCK_SIZE - 1][(bottom_left_y + size / 2)/BLOCK_SIZE - 1] = toUse;
//
//			ground[(bottom_left_x + size / 2) / BLOCK_SIZE + 1][(bottom_left_y + size / 2)/BLOCK_SIZE - 1] = toUse;
//			ground[(bottom_left_x + size / 2) / BLOCK_SIZE - 1][(bottom_left_y + size / 2)/BLOCK_SIZE + 1] = toUse;
//		}

	}

	private void addFence(int wall_start_x, int wall_start_y, int wall_length,
						  boolean vertical) {
		for (int i = 0; i < wall_length; i++) {
			if (Math.random() < .9) {
				if (vertical) {
					if (objects[wall_start_y + i][wall_start_x] == null &&
							!stage.closed[wall_start_y + i][wall_start_x]) {
						if (!stage.closed[wall_start_x][wall_start_y + i])
							objects[wall_start_y + i][wall_start_x] = Object
									.SMALL_WALL_V;
						stage.slow[wall_start_y + i][wall_start_x] = WALL_SLOW;

						// add cover
						BPoint cover_right = new BPoint(wall_start_x + 1,
								wall_start_y + i);
						cover_right.orientation = Orientation.LEFT;
						if (inMap(cover_right) && objects[cover_right
								.pos_y][cover_right.pos_x] == null)

							cover.add(cover_right);

						BPoint cover_left = new BPoint(wall_start_x - 1,
								wall_start_y + i);
						cover_left.orientation = Orientation.RIGHT;
						if (inMap(cover_left) && objects[cover_left
								.pos_y][cover_left.pos_x] == null)
							cover.add(cover_left);
					}
				} else if (objects[wall_start_y][wall_start_x +
						i] == null && !stage.closed[wall_start_y][wall_start_x
						+ i]) {
					objects[wall_start_y][wall_start_x + i] = Object
							.SMALL_WALL_H;
					stage.slow[wall_start_y][wall_start_x + i] = WALL_SLOW;

					// add cover
					BPoint cover_up = new BPoint(wall_start_x + i,
							wall_start_y + 1);
					if (inMap(cover_up) && objects[cover_up.pos_y][cover_up
							.pos_x] == null)
						cover.add(cover_up);
					cover_up.orientation = Orientation.DOWN;

					BPoint cover_down = new BPoint(wall_start_x + i,
							wall_start_y - 1);
					if (inMap(cover_down) && objects[cover_down
							.pos_y][cover_down.pos_x] == null)
						cover.add(cover_down);
					cover_down.orientation = Orientation.UP;
				}
			}
		}
	}

	// TODO use specifically seeded random generator for this and
	// for ruin generator, so that when you go back to the same ruin or village
	// It will have the same layout
	private void addFences(int maxWalls) {
		int number_walls = (int)(Math.random()*maxWalls + .5);
		for (int count = 0; count < number_walls; count++) {
			int wall_length = (int) (10*Math.random()+5);
			int wall_start_x = (int) ((stage.size_x-wall_length)*Math.random());
			int wall_start_y =  (int) ((stage.size_y-wall_length)*Math.random());

			boolean vertical = true;
			if (Math.random() < .5) vertical = false;

			addFence(wall_start_x, wall_start_y, wall_length, vertical);
		}
	}

	// keep in mind that the input is in LAND units, not map units (each has size BLOCK_SIZE)
	private float getDrawX(float input) {
		return (input - (SIZE_FACTOR - 1)*stage.size_x/ BLOCK_SIZE /2.0f) *stage.unit_width* BLOCK_SIZE;
	}

	private float getDrawY(float input) {
		return (input - (SIZE_FACTOR - 1)*stage.size_y/ BLOCK_SIZE /2.0f) *stage.unit_height* BLOCK_SIZE;
	}

	private float getDrawWidth() {
		return stage.unit_width* BLOCK_SIZE;
	}

	private float getDrawHeight() {
		return stage.unit_height* BLOCK_SIZE;
	}


	//	@Override
	public void actSpecial(float delta) {
		super.act(delta);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		TextureRegion texture;

		this.toBack();

		//		System.out.println(ground.length);
		//		System.out.println(stage.size_x);
		//		// draw base layer textures

			for (int i = 0; i < ground[0].length; i++) {
				for (int j = 0; j < ground.length; j++) {
					texture = groundTexture[j][i];

					boolean offMap = false;
					if (i < ground[0].length * this.edge_size_percent - 1 || i >= ground[0].length - ground[0].length * this.edge_size_percent)


						offMap = true;
					if (j < ground.length * this.edge_size_percent - 1 || j >= ground.length - ground.length * this.edge_size_percent)
						offMap = true;

					Color c = batch.getColor();
					groundcolor.set(c);
//
					if (offMap) {
						groundcolor.a = c.a * 0.6f;
						batch.setColor(groundcolor);
					}
					batch.draw(texture, getDrawX(j), getDrawY(i), getDrawWidth(), getDrawHeight());
					if (offMap) {
						batch.setColor(c);
					}
				}
			}


		for (FireContainer f : fc) {
			f.toFront();
			f.updateRotation(stage.getMapScreen().getBattleRotation());
		}

		// TODO: make this happen first
		// create an array of textures of size BLOCK_SIZE. For each one,
		// add base layer pixmap, then add blended version of neighbor textures as pixmaps.
		// save and draw


		// draw obstacles
		for (int i = 0; i < stage.size_y; i++) {
			for (int j = 0; j < stage.size_x; j++) {
				texture = null;
				boolean flashWhite = false;
				// Don't draw trees here
				if (objects[i][j] == Object.SMALL_WALL_V)
					texture = wallV;
				else if (objects[i][j] == Object.SMALL_WALL_H)
					texture = wallH;
				else if (objects[i][j] == Object.STUMP)
					texture = stump;
				else if (objects[i][j] == Object.CASTLE_WALL) {
					texture = castleWall;
					if (wallDamaged) {
						for (Wall wall : walls) {
							if (wall.pos_x == j && wall.pos_y == i && wall.damaged) {
								flashWhite = true;
								wallDamaged = false;
								wall.damaged = false;
							}
						}
					}
				} else if (objects[i][j] == Object.CASTLE_WALL_FLOOR) {
					texture = castleWallFloor;
					if (wallDamaged) {
						for (Wall wall : walls) {
							if (wall.pos_x == j && wall.pos_y == i && wall.damaged) {
								flashWhite = true;
								wallDamaged = false;
								wall.damaged = false;
							}
						}
					}
				}

				float stretch = this.sunStretch;
				float rotation = this.sunRotation;

				if (texture != null) {
					// TODO 
					drawShadow(batch, texture, (j * stage.unit_width), (i * stage.unit_height), texture.getRegionWidth() * stage.unit_width / 8, texture.getRegionHeight() * stage.unit_height / 8);
					batch.draw(texture, (j * stage.unit_width), (i * stage.unit_height), texture.getRegionWidth() * stage.unit_width / 8, texture.getRegionHeight() * stage.unit_height / 8);
				}
				if (flashWhite) {
					Color c = batch.getColor();
					groundcolor.set(CLEAR_WHITE);
					batch.setColor(groundcolor);
					batch.draw(white, (j * stage.unit_width), (i * stage.unit_height), stage.unit_width, stage.unit_height);
					batch.setColor(c);
				}
			}
		}

		// draw ladders
		for (Ladder l : ladders) {
			texture = ladder;
			float rotation = getOrientationRotation(l.orientation);
			//setKingdomRotation(kingdomRotation);
			//atch.draw(toDraw, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(),getScaleY(), getKingdomRotation());

			float x = l.pos_x * stage.unit_width;
			float y = l.pos_y * stage.unit_height;

			float width = texture.getRegionWidth() * stage.unit_width / 8;
			float height = texture.getRegionHeight() * stage.unit_height / 8;

			batch.draw(texture, x, y, width / 2, height / 4, width, height, 1, 1, rotation);
		}

		if (stage.selectedUnit != null && stage.placementPhase && !stage.isOver()) {
			stage.selectedUnit.bsp.drawPlacement(batch);
		}

		boolean drawPlacementArea = true;

		if (drawPlacementArea && stage.dragging && !stage.isOver() && stage.placementPhase) {
			Color c = batch.getColor();
			groundcolor.set(PLACEMENT_COLOR);
			batch.setColor(groundcolor);

			for (int i = stage.MIN_PLACE_X; i < stage.MAX_PLACE_X; i++) {
				for (int j = stage.MIN_PLACE_Y_1; j < stage.MAX_PLACE_Y_1; j++) {
					batch.draw(white, (i * stage.unit_width), (j * stage.unit_height), stage.unit_width, stage.unit_height);
				}
			}

			// for now, only draw enemy's placement area if we can actually place there
			// TODO make enemy's area visible for certain cases.
			if (stage.ambush) {
				for (int i = stage.MIN_PLACE_X; i < stage.MAX_PLACE_X; i++) {
					for (int j = stage.MIN_PLACE_Y_2; j < stage.MAX_PLACE_Y_2; j++) {
						batch.draw(white, (i * stage.unit_width), (j * stage.unit_height), stage.unit_width, stage.unit_height);


					}
				}
			}


			batch.setColor(c);
		}

		boolean drawAll = false;
		//		if (stage.selectedUnit != null) drawAll = true;

		// draw range of selected unit
		if (!drawAll && stage.currentPanel != null && !stage.dragging && !stage.isOver()) {
			Unit drawRange = stage.currentPanel;

			boolean drewRange = drawRange(drawRange, batch);

//			if (!drewRange)
		}

		// Draw Hide radius around friendly unit
		boolean drawHideRadius = true;
		if (drawHideRadius && stage.currentPanel != null && !stage.isOver()) {
			Unit unit = stage.currentPanel;
			if (unit.isHidden() && unit.bsp.stance == Unit.Stance.DEFENSIVE) {
				drawHideRadius(unit.bsp, batch);

				// Also draw LOS radius around enemies.
				for (BattleSubParty bsp : unit.enemyParty.subparties) {
					drawLOS(bsp, batch);
				}

			}
		}

		boolean debugDrawLosSelected = false;
		// Draw LOS radius around selected BSP
		if (debugDrawLosSelected && stage.currentPanel != null && !stage.isOver()) {
			Unit unit = stage.currentPanel;
			boolean allEnemiesHidden = true;
			for (BattleSubParty bsp : unit.enemyParty.subparties) {
				if (bsp.isRevealed()) allEnemiesHidden = false;
			}
			if (allEnemiesHidden)
				drawLOS(unit.bsp, batch);
		}

		boolean drawAllLOS = false;
		if (drawAllLOS) {
			for (BattleSubParty bsp : stage.enemies.subparties) {
				drawLOS(bsp, batch);
			}
		}
		//		else if (drawAll && stage.currentPanel != null) {
		//			if (stage.currentPanel.team == 0) {
		//				for (Unit drawRange : stage.getAllies()) 
		//					drawRange(drawRange, batch);
		//			}
		//			else if (stage.currentPanel.team == 1) {
		//				for (Unit drawRange : stage.getEnemies())
		//					drawRange(drawRange, batch);
		//			}
		//		}

		// draw cover
		boolean drawCover = false;
		//		boolean drawCover = false;
		if (drawCover) {
			Color c = batch.getColor();
			groundcolor.set(COVER_COLOR);
			batch.setColor(groundcolor);

			for (BPoint p : cover)
				batch.draw(white, (p.pos_x*stage.unit_width), (p.pos_y*stage.unit_height), stage.unit_width, stage.unit_height);
			batch.setColor(c);
		}

		// draw closed
		boolean drawClosed = false;
		//		boolean drawClosed = true;
		if (drawClosed) {
			Color c = batch.getColor();
			groundcolor.set(CLOSED_COLOR);
			batch.setColor(groundcolor);
			for (int i = 0; i < stage.closed.length; i++) {
				for (int j = 0; j < stage.closed[0].length; j++) {
					if (stage.closed[i][j])
						batch.draw(white, (j*stage.unit_width), (i*stage.unit_height), stage.unit_width, stage.unit_height);
				}
			}
			batch.setColor(c);
		}

		// draw ladders
		boolean drawLadders = false;
		//				boolean drawClosed = true;
		if (drawLadders) {
			Color c = batch.getColor();
			Color mycolor = new Color(0, 0, 1, .5f);
			batch.setColor(mycolor);
			for (Ladder l : ladders) {
				batch.draw(white, (l.pos_x*stage.unit_width), (l.pos_y*stage.unit_height), stage.unit_width, stage.unit_height);
			}
			batch.setColor(c);
		}
		// draw entrances
		boolean drawEntrances = false;
		if (drawEntrances) {
			Color c = batch.getColor();
			Color mycolor = new Color(1, 0, 0, .5f);
			batch.setColor(mycolor);
			for (BPoint l : entrances) {
				batch.draw(white, (l.pos_x*stage.unit_width), (l.pos_y*stage.unit_height), stage.unit_width, stage.unit_height);
			}
			batch.setColor(c);
		}

		// draw area inside walls
		boolean drawInsideWalls = false;
		//				boolean drawClosed = true;

		if (drawInsideWalls) {
			Color c = batch.getColor();
			Color mycolor = new Color(1, 0, 1, .2f);
			batch.setColor(mycolor);
			for (int i = 0; i < stage.size_y; i++) {
				for (int j = 0; j < stage.size_x; j++) {
					if (insideWalls(j, i))
						batch.draw(white, (j*stage.unit_width), (i*stage.unit_height), stage.unit_width, stage.unit_height);
				}
			}
			batch.setColor(c);
		}

		// draw rain
		boolean drawRain = true;
		if (drawRain) {
			if (stage.isRaining() || stage.isSnowing()) {

				for (int i = 0; i < raindropsPerFrame; i++) {
					raindrops[currentRainIndex].pos_x = (int) (Math.random()*stage.size_x);
					raindrops[currentRainIndex].pos_y = (int) (Math.random()*stage.size_y);
					// increment current rain index proportionally to the number of drops, otherwise the speed of drops
                    // will be too low.
					currentRainIndex++; 
					if (currentRainIndex >= raindrops.length) currentRainIndex = 0;
				}

				Color c = batch.getColor();
				Color mycolor = rainColor;

				// we can figure out how much to fade drop by calculating distance between its index and currentrainindex, 
				// then divide by array size to get between 0 and 1 yay

				// eg if index = 20 and currentIndex = 10, diff is (20-10)/40 = 1/4
				// eg if index = 20 and currentIndex = 25, diff is 40 + (20 - 25) = 

                // This is nice because it makes the raindrops look "softer"
				float MAX_ALPHA = .3f;

				if (stage.isSnowing()) {
					mycolor = SNOW_COLOR;

					float speed = 4;
					rainDrawOffsetX += speed;
					rainDrawOffsetY += speed;

					MAX_ALPHA = 1f; // makes snow last longer

					//					if (rainDrawOffsetX >= this.total_width) rainDrawOffsetX = 0;
				}


				for (int i = 0; i < raindrops.length; i++) {
					BPoint p = raindrops[i];
					double indexDiff = i - currentRainIndex;
					if (indexDiff < 0) indexDiff += raindrops.length;

					mycolor.a = (float) (Math.max(0, (indexDiff / raindrops.length) * MAX_ALPHA));
					batch.setColor(mycolor);

					float drawAtX = (p.pos_x*stage.unit_width + rainDrawOffsetX) % (this.stage.size_x*stage.unit_width);
					float drawAtY = (p.pos_y*stage.unit_height + rainDrawOffsetY) % (this.stage.size_y*stage.unit_height);

					batch.draw(white, (drawAtX), (drawAtY),stage.unit_width/2, stage.unit_height/2);
				}

				batch.setColor(c);
			}
		}

		//gray out unplayable area
		super.draw(batch, parentAlpha);
	}

//	public boolean isSnowing() {
//		return stage.isSnowing();
//	}
//
//	public boolean isRaining() {
//		return stage.raining;
//	}

	// used to draw trees after units have been drawn
	public void drawTrees(SpriteBatch batch) {
		TextureRegion texture;
		for (int i = 0; i < stage.size_y; i++) {
			for (int j = 0; j < stage.size_x; j++) {
				texture = null;
				if (objects[i][j] == Object.TREE || objects[i][j] == Object.TREE_ON_FIRE || objects[i][j] == Object.DARK_TREE || objects[i][j] == Object.DARK_TREE_ON_FIRE || objects[i][j] == Object.SNOW_TREE || objects[i][j] == Object.SNOW_TREE_ON_FIRE) {
					//					System.out.println("drawing trees");
                    // TODO add tree shadow
					texture = treeShadow;
				} else if (objects[i][j] == Object.PALM || objects[i][j] == Object.PALM_DARK|| objects[i][j] == Object.PALM_ON_FIRE || objects[i][j] == Object.PALM_DARK_ON_FIRE ) {
				    texture = palmShadow;
                }
				if (texture != null) drawShadow(batch, texture, ((j-TREE_X_OFFSET)*stage.unit_width), ((i-TREE_Y_OFFSET)*stage.unit_height), TREE_WIDTH*stage.unit_width, TREE_HEIGHT*stage.unit_height);
			}
		}
		
		// draw actual trees
		for (int i = 0; i < stage.size_y; i++) {
			for (int j = 0; j < stage.size_x; j++) {
				texture = null;
				if (objects[i][j] == Object.TREE || objects[i][j] == Object.TREE_ON_FIRE) {
					texture = tree;
				} else if (objects[i][j] == Object.DARK_TREE ||  objects[i][j] == Object.DARK_TREE_ON_FIRE) {
					texture = darkTree;
				} else if (objects[i][j] == Object.SNOW_TREE ||  objects[i][j] == Object.SNOW_TREE_ON_FIRE) {
					texture = snowDarkTree;
				}
				else if (objects[i][j] == Object.PALM ||  objects[i][j] == Object.PALM_ON_FIRE) {
                    texture = palm;
                }
				else if (objects[i][j] == Object.PALM_DARK ||  objects[i][j] == Object.PALM_DARK_ON_FIRE) {
					texture = palmDark;
				}
				if (texture != null) batch.draw(texture, ((j-TREE_X_OFFSET)*stage.unit_width), ((i-TREE_Y_OFFSET)*stage.unit_height), TREE_WIDTH*stage.unit_width, TREE_HEIGHT*stage.unit_height);
			}
		}

		for (FireContainer f : fc) {
			f.draw(batch, 1);
		}
	}

	public void drawShadow(SpriteBatch batch, TextureRegion texture, float x, float y, float width, float height) {
		boolean drawShadows = true;
		if (!drawShadows) {
			batch.draw(texture, x, y, width, height);
			return;
		}
		float scale = 1;
		if (texture == palmShadow) scale = 1.2f;

		Color o = batch.getColor();
		batch.setColor(SHADOW_COLOR);
		batch.draw(texture, x, y + height * 0.3f, width/2, height * 0.2f, width, height, scale, scale * sunStretch, sunRotation);
		batch.setColor(o);
	}

	private StrictArray<BPoint> getAllHidePoints(BattleSubParty bsp) {
		StrictArray<BPoint> allPoints = new StrictArray<>();
		for (Unit unit : bsp.units) {
			if (unit.team == 1) throw new AssertionError();
			if (!unit.inMap()) continue;

			StrictArray<BPoint> unitPoints = getRadiusPoints(unit, unit.getHideRadius());
			for (int i = 0; i < unitPoints.size; i++) {
				BPoint p = unitPoints.get(i);
				if (allPoints.contains(p, false)) {
//					System.out.println("encountered duplicate point in battlemap. good");
					continue; // Using .equals comparison here.
				}
				allPoints.add(p);
			}
		}
		return allPoints;
	}

	private StrictArray<BPoint> getAllLOSPoints(BattleSubParty bsp) {
		StrictArray<BPoint> allPoints = new StrictArray<>();
		for (Unit unit : bsp.units) {
			if (!unit.inMap()) continue;
			if (unit.team == 1 && unit.isHidden()) continue;

			StrictArray<BPoint> unitPoints = getRadiusPoints(unit, unit.getLineOfSight());
			for (int i = 0; i < unitPoints.size; i++) {
				BPoint p = unitPoints.get(i);
				if (allPoints.contains(p, false)) {
//					System.out.println("encountered duplicate point in battlemap. good");
					continue; // Using .equals comparison here.
				}
				allPoints.add(p);
			}
		}
		return allPoints;
	}

	private StrictArray<BPoint> getRadiusPoints(Unit unit, int range) {
		StrictArray<BPoint> points = new StrictArray<>();
		int center_x = unit.pos_x;
		int center_y = unit.pos_y;

		for (int i = -range + 1; i < range; i++) {
			for (int j = -range + 1; j < range; j++) {
				if (i == 0 && j == 0) continue;
				if (i*i + j*j <= range*range && center_x+i >= 0 && center_y+j >= 0 && center_x+i < stage.size_x && center_y+j < stage.size_y) {
					BPoint point = new BPoint(center_x + i, center_y + j);
					points.add(point);
				}
			}
		}
		return points;
	}

	private void drawRadius(Unit drawRange, SpriteBatch batch, boolean diminishing, Color color, int range, boolean quarter) {
			Color c = batch.getColor();
			groundcolor.set(color);

			float max_alpha = .3f;
			float base_alpha = .1f;

			batch.setColor(groundcolor);

			int center_x = drawRange.pos_x;
			int center_y = drawRange.pos_y;

			for (int i = -range + 1; i < range; i++) {
				for (int j = -range + 1; j < range; j++) {
					if (i == 0 && j == 0) continue;
					if (i*i + j*j <= range*range && center_x+i >= 0 && center_y+j >= 0 && center_x+i < stage.size_x && center_y+j < stage.size_y) {

						if (diminishing) {
							// calculate distance as fraction of range
							float alpha_factor = (float) (Math.sqrt(i * i + j * j) / range);

							groundcolor.a = (1 - alpha_factor) * max_alpha + base_alpha;
						}
						batch.setColor(groundcolor);

						if (quarter) {
							if (drawRange.orientation == Orientation.UP)
								if (Math.abs(i) < Math.abs(j) && j > 0)
									batch.draw(white, (center_x + i) * stage.unit_width, (center_y + j) * stage.unit_height, stage.unit_width, stage.unit_height);
							if (drawRange.orientation == Orientation.DOWN)
								if (Math.abs(i) < Math.abs(j) && j < 0)
									batch.draw(white, (center_x + i) * stage.unit_width, (center_y + j) * stage.unit_height, stage.unit_width, stage.unit_height);
							if (drawRange.orientation == Orientation.LEFT)
								if (Math.abs(i) > Math.abs(j) && i < 0)
									batch.draw(white, (center_x + i) * stage.unit_width, (center_y + j) * stage.unit_height, stage.unit_width, stage.unit_height);
							if (drawRange.orientation == Orientation.RIGHT)
								if (Math.abs(i) > Math.abs(j) && i > 0)
									batch.draw(white, (center_x + i) * stage.unit_width, (center_y + j) * stage.unit_height, stage.unit_width, stage.unit_height);
						} else {
							batch.draw(white, (center_x + i) * stage.unit_width, (center_y + j) * stage.unit_height, stage.unit_width, stage.unit_height);
						}
					}
				}
			}
			batch.setColor(c);
	}
	private void drawHideRadius(BattleSubParty bsp, SpriteBatch batch) {
		Color c = batch.getColor();

		StrictArray<BPoint> points = getAllHidePoints(bsp);

		batch.setColor(HIDE_COLOR);

		for (BPoint p : points) {
			batch.draw(white, p.pos_x * stage.unit_width, p.pos_y * stage.unit_height, stage.unit_width, stage.unit_height);
		}
		batch.setColor(c);
	}

	private void drawLOS(BattleSubParty bsp, SpriteBatch batch) {
		Color c = batch.getColor();

		StrictArray<BPoint> points = getAllLOSPoints(bsp);

		batch.setColor(LOS_COLOR);

		for (BPoint p : points) {
			batch.draw(white, p.pos_x * stage.unit_width, p.pos_y * stage.unit_height, stage.unit_width, stage.unit_height);
		}
		batch.setColor(c);
	}

	private boolean drawRange(Unit drawRange, SpriteBatch batch) {
		if (drawRange.rangedWeaponOut() && !drawRange.isRetreating()) {
			drawRadius(drawRange, batch, true, RANGE_COLOR, (int) drawRange.getCurrentRange(), true);

			Color c = batch.getColor();
			batch.setColor(Color.BLACK);

			// draw target
			if (drawRange.nearestTarget != null) {
				//				System.out.println("drawing nearest target");
				batch.draw(white, (drawRange.nearestTarget.getX()), (drawRange.nearestTarget.getY()), stage.unit_width, stage.unit_height);
			}
			batch.setColor(c);
			return true;
		}
		return false;
	}

	void drawLOS(Unit drawLos, SpriteBatch batch) {
		drawRadius(drawLos, batch, false, LOS_COLOR, drawLos.getLineOfSight(), false);
	}

	void drawHideRadius(Unit unit, SpriteBatch batch) {
		System.out.println("Drawing hide radius");
		drawRadius(unit, batch, false, HIDE_COLOR, unit.getHideRadius(), false);
	}

	private boolean addWall(int pos_x, int pos_y, Object object, Orientation orientation, int width) {
		if (objects[pos_y][pos_x] == null && !stage.closed[pos_y][pos_x]) {
			objects[pos_y][pos_x] = object;
			if (object == Object.CASTLE_WALL || object == Object.CASTLE_WALL_FLOOR) {
				Wall wall = new Wall();
				wall.pos_x = pos_x; 
				wall.pos_y = pos_y; 
				wall.hp = object.hp;
				wall.orientation = orientation;
				wall.size = width;
				walls.add(wall);
			}
			return true;
		}
		return false;
	}

	private boolean addObject(int pos_x, int pos_y, Object object) {
		return addWall(pos_x, pos_y, object, null, 0);
	}

	//	private Pixmap getTexture(GroundType ground) {
	//		Pixmap texture;
	//		if (ground == GroundType.GRASS) texture = grass;
	//		else if (ground == GroundType.DARKGRASS) texture = darkgrass;
	//		else if (ground == GroundType.LIGHTGRASS) texture = lightgrass;
	//		else if (ground == GroundType.SAND) texture = sand;
	//		else if (ground == GroundType.WATER) texture = water;
	//		else if (ground == GroundType.MUD) texture = mud;
	//		else if (ground == GroundType.ROCK) texture = rock;
	//		else if (ground == GroundType.DARKROCK) texture = darkrock;
	//		else if (ground == GroundType.SNOW) texture = snow;
	//		else if (ground == GroundType.LIGHTSAND) texture = lightsand;
	//		else if (ground == GroundType.LIGHTSNOW) texture = lightsnow;
	//		else if (ground == GroundType.FLOWERS) texture = flowers;
	//		else if (ground == GroundType.FLOWERS2) texture = flowers2;
	//		else if (ground == GroundType.SWAMP) texture = swamp;
	//		else if (ground == GroundType.SWAMP2) texture = swamp2;
	//		else texture = dirt;
	//
	//		return texture;
	//	}


	private Pixmap getTexture(GroundType ground) {
		switch (ground) {
		case GRASS: 	return new Pixmap(Gdx.files.internal("ground/grass.png")); 
		case DIRT: 		return new Pixmap(Gdx.files.internal("ground/dirt.png")); 
		case SAND:		return	new Pixmap(Gdx.files.internal("ground/sand.png")); 
		case DARKGRASS: return new Pixmap(Gdx.files.internal("ground/darkgrass.png")); 
		case MUD: 		return	new Pixmap(Gdx.files.internal("ground/mud.png")); 
		case WATER: 	return	new Pixmap(Gdx.files.internal("ground/water.png"));
		case LIGHTGRASS: return new Pixmap(Gdx.files.internal("ground/lightgrass.png"));
		case FLOWERS: 	return	new Pixmap(Gdx.files.internal("ground/flowers.png"));
		case FLOWERS2: 	return	new Pixmap(Gdx.files.internal("ground/flowers2.png"));
		case ROCK: 		return	new Pixmap(Gdx.files.internal("ground/rock.png"));
		case DARKROCK:	return	new Pixmap(Gdx.files.internal("ground/darkrock.png"));
		case SNOW: 		return new Pixmap(Gdx.files.internal("ground/snow.png"));
		case LIGHTSNOW: return new Pixmap(Gdx.files.internal("ground/lightsnow.png"));
		case LIGHTSAND: return new Pixmap(Gdx.files.internal("ground/sandlight.png"));
		case SWAMP: 	return new Pixmap(Gdx.files.internal("ground/swamp3.png"));
		case SWAMP2:	 return new Pixmap(Gdx.files.internal("ground/swamp2.png"));
		}
		return null;
	}

	private MapType randomMapType() {
		int count = MapType.values().length;
		int index = (int) (Math.random() * count);
		return MapType.values()[index];
	}

	// close a spot of ground BLOCK_SIZE by BLOCK_SIZE
	private void closeGround(int y, int x) {
		for (int k = 0; k < BLOCK_SIZE; k++) {
			for (int l = 0; l < BLOCK_SIZE; l++) {
				if (!inMap(new BPoint(x* BLOCK_SIZE + l, y* BLOCK_SIZE + k))) continue;
				stage.closed[y* BLOCK_SIZE + k][x* BLOCK_SIZE + l] = true;
			}
		}
	}

	private float getOrientationRotation(Orientation orientation) {
		if (orientation == Orientation.UP) return 0;
		if (orientation == Orientation.LEFT) return 90;
		if (orientation == Orientation.DOWN) return 180;
		else return 270;
	}

	public boolean insideWalls(int pos_x, int pos_y) {
	    if (!stage.hasWall()) return false;
		if (wallTop > stage.size_y && wallRight > stage.size_x && wallBottom < 0 && wallLeft < 0) return false;
		if (pos_y <= wallTop && pos_y >= wallBottom)
			if (pos_x <= wallRight && pos_x >= wallLeft) 
				return true;
		return false;
	}


	// trying it out with double the size
	public boolean inMap(BPoint p) {
		if (p == null) return false;
		return p.pos_x < stage.size_x &&
				p.pos_y < stage.size_y && 
				p.pos_x >= 0 && 
				p.pos_y >= 0;
	}
}
