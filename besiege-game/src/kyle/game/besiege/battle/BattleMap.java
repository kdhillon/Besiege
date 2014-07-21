package kyle.game.besiege.battle;

import kyle.game.besiege.battle.Unit.Orientation;
import kyle.game.besiege.voronoi.Biomes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;


public class BattleMap extends Actor {
	boolean raining;
	private TextureRegion white;
	private static final float WALL_SLOW = .5f;
	private static final float LADDER_SLOW = .75f;

	private static final int TREE_X_OFFSET = 1;
	private static final int TREE_Y_OFFSET = 1;
	private static final int TREE_WIDTH = 3;
	private static final int TREE_HEIGHT = 3;
	public static final float CASTLE_WALL_HEIGHT_DEFAULT = 1f;

	private enum MapType {
		FOREST, BEACH, GRASSLAND, DESERT, ALPINE, MEADOW, CRAG, RIVER
	}
	private MapType maptype;

	private BattleStage stage;
	public static final int SIZE = 4;
	private int total_height;
	private int total_width;

	private enum GroundType {
		GRASS, DIRT, SAND, DARKGRASS, MUD, WATER, LIGHTGRASS, SNOW, ROCK, DARKROCK, LIGHTSAND, LIGHTSNOW, FLOWERS, FLOWERS2
	}

	public enum Object {
		TREE(.5f), STUMP(.1f), SMALL_WALL_V(.07f), SMALL_WALL_H(.07f), CASTLE_WALL(.058f), CASTLE_WALL_FLOOR(0f), LADDER(0); 
		float height;
		Orientation orientation; // for ladders
		private Object(float height) {
			this.orientation = Orientation.UP;
			this.height = height;
		}
	}
	
	
	public Array<Point> cover; // points with protection
	private GroundType[][] ground;
	public Object[][] objects;

	private TextureRegion grass, flowers, flowers2, dirt, sand, darkgrass, mud, water, tree, stump, lightgrass, rock, darkrock, snow, lightsnow, lightsand, wallV, wallH, castleWall, castleWallFloor, ladder;


	public BattleMap(BattleStage mainmap) {
		this.stage = mainmap;

		//		this.maptype = randomMapType();
		this.maptype = getMapTypeForBiome(mainmap.biome);
//		this.maptype = MapType.BEACH;

		this.total_height = mainmap.size_y/SIZE;
		this.total_width = mainmap.size_x/SIZE;

		ground = new GroundType[total_height][total_width];
		objects = new Object[mainmap.size_y][mainmap.size_x];
		cover = new Array<Point>();
		
		grass = 	new TextureRegion(new Texture(Gdx.files.internal("ground/grass.png"))); 
		dirt =		new TextureRegion(new Texture(Gdx.files.internal("ground/dirt.png"))); 
		sand = 		new TextureRegion(new Texture(Gdx.files.internal("ground/sand.png"))); 
		darkgrass = new TextureRegion(new Texture(Gdx.files.internal("ground/darkgrass.png"))); 
		mud = 		new TextureRegion(new Texture(Gdx.files.internal("ground/mud.png"))); 
		water = 	new TextureRegion(new Texture(Gdx.files.internal("ground/water.png")));
		lightgrass= new TextureRegion(new Texture(Gdx.files.internal("ground/lightgrass.png")));
		flowers = 	new TextureRegion(new Texture(Gdx.files.internal("ground/flowers.png")));
		flowers2 = 	new TextureRegion(new Texture(Gdx.files.internal("ground/flowers2.png")));
		rock = 		new TextureRegion(new Texture(Gdx.files.internal("ground/rock.png")));
		darkrock = 	new TextureRegion(new Texture(Gdx.files.internal("ground/darkrock.png")));
		snow = 		new TextureRegion(new Texture(Gdx.files.internal("ground/snow.png")));
		lightsnow = new TextureRegion(new Texture(Gdx.files.internal("ground/lightsnow.png")));
		lightsand = new TextureRegion(new Texture(Gdx.files.internal("ground/sandlight.png")));
		
		
		tree = 		new TextureRegion(new Texture(Gdx.files.internal("objects/tree_3_3 alt.png")));
		stump = 	new TextureRegion(new Texture(Gdx.files.internal("objects/stump.png")));
		wallV = 	new TextureRegion(new Texture(Gdx.files.internal("objects/stone_fence_v.png")));
		wallH = 	new TextureRegion(new Texture(Gdx.files.internal("objects/stone_fence.png")));

		castleWall = 		new TextureRegion(new Texture(Gdx.files.internal("objects/castle_wall.png")));
		castleWallFloor = 	new TextureRegion(new Texture(Gdx.files.internal("objects/castle_wall_floor.png")));
		ladder = 			new TextureRegion(new Texture(Gdx.files.internal("objects/ladder.png")));

		white = new TextureRegion(new Texture("whitepixel.png"));

	
	
		// generate random map
		if (maptype == MapType.FOREST) {
			for (int i = 0; i < ground.length; i++) {
				for (int j = 0; j < ground[0].length; j++) {
					double random = Math.random();
					if (random < 0.33) ground[i][j] = GroundType.GRASS;
					else if (random < .80) ground[i][j] = GroundType.DARKGRASS;
					else ground[i][j] = GroundType.DIRT;
				}
			}
			// add walls
			
			if (stage.siegeDefense)
				addWall((int)(stage.size_y*.2f));
			
			addWalls(5);
			addTrees(.05*Math.random() + .03);
		}
		if (maptype == MapType.GRASSLAND) {
			for (int i = 0; i < ground.length; i++) {
				for (int j = 0; j < ground[0].length; j++) {
					double random = Math.random();
					if (random < .5) ground[i][j] = GroundType.LIGHTGRASS;
					else if (random < 0.94) ground[i][j] = GroundType.GRASS;
					else ground[i][j] = GroundType.DIRT;
				}
			}
			
			if (stage.siegeDefense)
				addWall((int)(stage.size_y*.2f));
			
			addTrees(.001);
			addWalls(1);
		}
		if (maptype == MapType.MEADOW) {
			for (int i = 0; i < ground.length; i++) {
				for (int j = 0; j < ground[0].length; j++) {
					double random = Math.random();
					if (random < .7) ground[i][j] = GroundType.GRASS;
					else if (random < 0.90) ground[i][j] = GroundType.LIGHTGRASS;
					else if (random < .95) ground[i][j] = GroundType.FLOWERS2;
					else if (random < 1) ground[i][j] = GroundType.FLOWERS;
					else ground[i][j] = GroundType.DIRT;
				}
			}
			
			if (stage.siegeDefense)
				addWall((int)(stage.size_y*.2f));
			
			addTrees(.00);
			addWalls(15);
		}
		if (maptype == MapType.BEACH) {
			double slope = Math.random()*3+3;
			double slope2 = Math.random()*1;
			double thresh = Math.random()*40/SIZE+60/SIZE;

			// determine if ocean on right or left of line?
			for (int i = 0; i < ground.length; i++) {
				for (int j = 0; j < ground[0].length; j++) {
					ground[i][j] = GroundType.SAND;
					if (Math.random() < .01) ground[i][j] = GroundType.MUD;
					double leftSide = slope*i + slope2*j;

					if (leftSide < thresh || (leftSide - thresh < 4 && Math.random() < .5)) {
						ground[i][j] = GroundType.WATER;
						// set as closed
						closeGround(j, i);
					} 
					else if (leftSide > thresh + 100/SIZE * Math.random() + 150/SIZE) ground[i][j] = GroundType.LIGHTGRASS;
				} 
			}
			
			if (stage.siegeDefense)
				addWall((int)(stage.size_y*.2f));
		}
		if (maptype == MapType.DESERT) {
			for (int i = 0; i < ground.length; i++) {
				for (int j = 0; j < ground[0].length; j++) {
					double random = Math.random();
					if (random < .6) ground[i][j] = GroundType.SAND;
					else if (random < 1) ground[i][j] = GroundType.LIGHTSAND;
					else if (random < 0.99) ground[i][j] = GroundType.DIRT;
					else ground[i][j] = GroundType.MUD;
				}
			}
			if (stage.siegeDefense)
				addWall((int)(stage.size_y*.2f));
		}
		if (maptype == MapType.ALPINE) {
			for (int i = 0; i < ground.length; i++) {
				for (int j = 0; j < ground[0].length; j++) {
					double random = Math.random();
					if (random < .7) ground[i][j] = GroundType.LIGHTSNOW;
					else if (random < 1) ground[i][j] = GroundType.SNOW;
					else if (random < 0.99) ground[i][j] = GroundType.DIRT;
					else ground[i][j] = GroundType.MUD;
				}
			}
			if (stage.siegeDefense)
				addWall((int)(stage.size_y*.2f));
		}
		if (maptype == MapType.CRAG) {
			for (int i = 0; i < ground.length; i++) {
				for (int j = 0; j < ground[0].length; j++) {
					double random = Math.random();
					if (random < .7) ground[i][j] = GroundType.DARKROCK;
					else if (random < .9) ground[i][j] = GroundType.MUD;
					else if (random < 1) ground[i][j] = GroundType.ROCK;
				}
			}
			
			if (stage.siegeDefense)
				addWall((int)(stage.size_y*.2f));
			
			addStumps(.01);
		}
		
		// remove cover on top of objects
		for (Point p : cover) {
			if (objects[p.pos_y][p.pos_x] != null || stage.closed[p.pos_y][p.pos_x]) {
//				System.out.println("removing value from cover");
//				cover.removeValue(p, true);
				// doesn't work for some reason

			}
		}
	}
	
	public MapType getMapTypeForBiome(Biomes biome) {
		switch(biome) {
			case BEACH : 			return MapType.BEACH;
			case SNOW : 			return MapType.ALPINE;
			case TUNDRA : 			return MapType.ALPINE;
			case BARE : 			return MapType.DESERT;
			case SCORCHED :			return MapType.CRAG;
			case TAIGA :			return MapType.FOREST;
			case SHURBLAND : 		return MapType.MEADOW;
			case TEMPERATE_DESERT : return MapType.DESERT;
			case TEMPERATE_RAIN_FOREST : 		return MapType.FOREST;
			case TEMPERATE_DECIDUOUS_FOREST : 	return MapType.FOREST;
			case GRASSLAND : 					return MapType.BEACH;
			case SUBTROPICAL_DESERT : 			return MapType.DESERT;
			case SHRUBLAND : 					return MapType.MEADOW;
			case ICE : 							return MapType.ALPINE;
			case MARSH : 						return MapType.FOREST;
			case TROPICAL_RAIN_FOREST : 		return MapType.FOREST;
			case TROPICAL_SEASONAL_FOREST : 	return MapType.FOREST;
			case COAST : 						return MapType.BEACH;
			case LAKESHORE: 					return MapType.BEACH;
			default : 							return MapType.GRASSLAND;
		}
	}
	
	private void addWall(int y_position) {
		
		// add ladders
		if (addObject(stage.size_x/2, y_position+1, Object.LADDER)) {
			stage.slow[y_position+1][stage.size_x/2] = LADDER_SLOW;
			stage.closed[y_position+1][stage.size_x/2] = false;
		}
		
		for (int i = 0; i < stage.size_x; i++) {
			if (addObject(i, y_position, Object.CASTLE_WALL_FLOOR)) {
				stage.heights[y_position][i] = CASTLE_WALL_HEIGHT_DEFAULT; // close random middle row
				Point coverPoint = new Point(i, y_position);
				coverPoint.orientation = Orientation.UP;
				if (inMap(coverPoint)) cover.add(coverPoint);
			}
		}

		for (int i = 0; i < stage.size_x; i++) {
			if (addObject(i, y_position-1, Object.CASTLE_WALL_FLOOR))
				stage.heights[y_position-1][i] = CASTLE_WALL_HEIGHT_DEFAULT; // close random middle row
		}
		for (int i = 0; i < stage.size_x; i++) {
			if (addObject(i, y_position+1, Object.CASTLE_WALL)) {
				stage.heights[y_position+1][i] = CASTLE_WALL_HEIGHT_DEFAULT; // close random middle row
				stage.closed[y_position+1][i] = true;
			}
		}
		
		// add ladders
		if (addObject(stage.size_x/2, y_position-2, Object.LADDER))
			stage.slow[y_position-2][stage.size_x/2] = LADDER_SLOW;
		
	
	}

	private void addTrees(double probability) {
		for (int i = 0; i < stage.size_x; i++) {
			for (int j = 0; j < stage.size_y; j++) {
				if (Math.random() < probability && objects[j][i] == null) {
					objects[j][i] = Object.TREE;
					stage.closed[j][i] = true;
					//					mainmap.closed[i][j] = true;
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
					Point cover_right = new Point(i+1, j);
					cover_right.orientation = Orientation.LEFT;
					if (inMap(cover_right) && objects[cover_right.pos_y][cover_right.pos_x] == null) cover.add(cover_right);
					
					Point cover_left = new Point(i-1, j);
					cover_left.orientation = Orientation.RIGHT;
					if (inMap(cover_left) && objects[cover_left.pos_y][cover_left.pos_x] == null) cover.add(cover_left);

					Point cover_up = new Point(i, j+1);
					if (inMap(cover_up) && objects[cover_up.pos_y][cover_up.pos_x] == null) cover.add(cover_up);
					cover_up.orientation = Orientation.DOWN;

					Point cover_down = new Point(i, j-1);
					if (inMap(cover_down) && objects[cover_down.pos_y][cover_down.pos_x] == null) cover.add(cover_down);
					cover_down.orientation = Orientation.UP;
				}	
			}
		}
	}

	private void addWalls(int maxWalls) {
		int number_walls = (int)(Math.random()*maxWalls + .5);
		for (int count = 0; count < number_walls; count++) {
			int wall_length = (int) (10*Math.random()+5);
			int wall_start_x = (int) ((stage.size_x-wall_length)*Math.random());
			int wall_start_y =  (int) ((stage.size_y-wall_length)*Math.random());

			boolean vertical = true;
			if (Math.random() < .5) vertical = false;

			for (int i = 0; i < wall_length; i++) {
				if (Math.random() < .9) {
					if (vertical && objects[wall_start_y+i][wall_start_x] == null) {
						objects[wall_start_y+i][wall_start_x] = Object.SMALL_WALL_V;
						stage.slow[wall_start_y+i][wall_start_x] = WALL_SLOW;
						
						// add cover 
						Point cover_right = new Point(wall_start_x+1, wall_start_y+i);
						cover_right.orientation = Orientation.LEFT;
						if (inMap(cover_right) && objects[cover_right.pos_y][cover_right.pos_x] == null) cover.add(cover_right);
						
						Point cover_left = new Point(wall_start_x-1, wall_start_y+i);
						cover_left.orientation = Orientation.RIGHT;
						if (inMap(cover_left) && objects[cover_left.pos_y][cover_left.pos_x] == null) cover.add(cover_left);
					}
					else if (!vertical && objects[wall_start_y][wall_start_x+i] == null) {
						objects[wall_start_y][wall_start_x+i] = Object.SMALL_WALL_H;
						stage.slow[wall_start_y][wall_start_x+i] = WALL_SLOW;
						
						// add cover 
						Point cover_up = new Point(wall_start_x+i, wall_start_y+1);
						if (inMap(cover_up) && objects[cover_up.pos_y][cover_up.pos_x] == null) cover.add(cover_up);
						cover_up.orientation = Orientation.DOWN;

						Point cover_down = new Point(wall_start_x+i, wall_start_y-1);
						if (inMap(cover_down) && objects[cover_down.pos_y][cover_down.pos_x] == null) cover.add(cover_down);
						cover_down.orientation = Orientation.UP;
					}
				}
			}
		}
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		TextureRegion texture;

		this.toBack();
		
//		System.out.println(ground.length);
//		System.out.println(stage.size_x);

		// draw base layer textures
		for (int i = 0; i < ground[0].length; i++) {
			for (int j = 0; j < ground.length; j++) {
				texture = getTexture(ground[j][i]);

				batch.draw(texture, j*stage.unit_width*stage.scale*SIZE, i*stage.unit_height*stage.scale*SIZE, stage.unit_width*stage.scale*SIZE, stage.unit_height*stage.scale*SIZE);
			}
		}

		boolean blend = true;
		if (blend) {
			// then draw blend textures
			for (int i = 0; i < ground[0].length; i++) {
				for (int j = 0; j < ground.length; j++) {
					texture = getTexture(ground[j][i]);

					int half_width = (int) (stage.unit_width*stage.scale*SIZE/2);
					int half_height = (int) (stage.unit_height*stage.scale*SIZE/2);


					Color c = batch.getColor();
					Color mycolor = new Color(1, 1, 1, .3f);
					batch.setColor(mycolor);
					// draw four extra squares at corners with alpha
					batch.draw(texture, j*stage.unit_width*stage.scale*SIZE + half_width, i*stage.unit_height*stage.scale*SIZE + half_height, stage.unit_width*stage.scale*SIZE, stage.unit_height*stage.scale*SIZE);
					batch.draw(texture, j*stage.unit_width*stage.scale*SIZE + half_width, i*stage.unit_height*stage.scale*SIZE - half_height, stage.unit_width*stage.scale*SIZE, stage.unit_height*stage.scale*SIZE);
					batch.draw(texture, j*stage.unit_width*stage.scale*SIZE - half_width, i*stage.unit_height*stage.scale*SIZE + half_height, stage.unit_width*stage.scale*SIZE, stage.unit_height*stage.scale*SIZE);
					batch.draw(texture, j*stage.unit_width*stage.scale*SIZE - half_width, i*stage.unit_height*stage.scale*SIZE - half_height, stage.unit_width*stage.scale*SIZE, stage.unit_height*stage.scale*SIZE);

//					if (j != ground.length - 1 && i != ground[0].length - 1) batch.draw(texture, j*stage.unit_width*stage.scale*SIZE + half_width, i*stage.unit_height*stage.scale*SIZE + half_height, stage.unit_width*stage.scale*SIZE, stage.unit_height*stage.scale*SIZE);
//					if (j != ground.length - 1 && i != 0) batch.draw(texture, j*stage.unit_width*stage.scale*SIZE + half_width, i*stage.unit_height*stage.scale*SIZE - half_height, stage.unit_width*stage.scale*SIZE, stage.unit_height*stage.scale*SIZE);
//					if (j != 0 && i != ground[0].length - 1) batch.draw(texture, j*stage.unit_width*stage.scale*SIZE - half_width, i*stage.unit_height*stage.scale*SIZE + half_height, stage.unit_width*stage.scale*SIZE, stage.unit_height*stage.scale*SIZE);
//					if (j != 0 && i != 0) batch.draw(texture, j*stage.unit_width*stage.scale*SIZE - half_width, i*stage.unit_height*stage.scale*SIZE - half_height, stage.unit_width*stage.scale*SIZE, stage.unit_height*stage.scale*SIZE);


					batch.setColor(c);
				}
			}
		}

		// draw obstacles
		for (int i = 0; i < stage.size_y; i++) {
			for (int j = 0; j < stage.size_x; j++) {
				texture = null;
				// Don't draw trees here
				if (objects[i][j] == Object.SMALL_WALL_V) 
					texture = wallV;
				else if (objects[i][j] == Object.SMALL_WALL_H) 
					texture = wallH;
				else if (objects[i][j] == Object.STUMP) 
					texture = stump;
				else if (objects[i][j] == Object.CASTLE_WALL) 
					texture = castleWall;
				else if (objects[i][j] == Object.CASTLE_WALL_FLOOR)
					texture = castleWallFloor;
				else if (objects[i][j] == Object.LADDER)
					texture = ladder;
				
				if (texture != null) batch.draw(texture, (j*stage.unit_width*stage.scale), (i*stage.unit_height*stage.scale), texture.getRegionWidth()*stage.unit_width/8*stage.scale, texture.getRegionHeight()*stage.unit_height/8*stage.scale);
			}
		}
		
		// draw second round of obstacles
		for (int i = 0; i < stage.size_y; i++) {
			for (int j = 0; j < stage.size_x; j++) {
				texture = null;
				// Don't draw trees here
				if (objects[i][j] == Object.LADDER)
					texture = ladder;
				
				if (texture != null) batch.draw(texture, (j*stage.unit_width*stage.scale), (i*stage.unit_height*stage.scale), texture.getRegionWidth()*stage.unit_width/8*stage.scale, texture.getRegionHeight()*stage.unit_height/8*stage.scale);
			}
		}
		

		boolean drawAll = false;
		if (stage.selectedUnit != null) drawAll = true;
		
		// draw range of selected unit
		if (!drawAll && stage.currentPanel != null) {
			Unit drawRange = stage.currentPanel;
			drawRange(drawRange, batch);
		}
		else if (drawAll && stage.currentPanel != null) {
			if (stage.currentPanel.team == 0) {
				for (Unit drawRange : stage.allies) 
					drawRange(drawRange, batch);
			}
			else if (stage.currentPanel.team == 1) {
				for (Unit drawRange : stage.enemies)
					drawRange(drawRange, batch);
			}
		}
		
		
		// draw cover
		boolean drawCover = false;
//		boolean drawCover = true;
		if (drawCover) {

			Color c = batch.getColor();
			Color mycolor = new Color(1, 1, 0, .5f);
			batch.setColor(mycolor);
			
			for (Point p : cover)
				batch.draw(white, (p.pos_x*stage.unit_width*stage.scale), (p.pos_y*stage.unit_height*stage.scale), stage.unit_width*stage.scale, stage.unit_height*stage.scale);
			
			batch.setColor(c);
		}
		
		// draw closed
		boolean drawClosed = false;
//		boolean drawClosed = true;
		if (drawClosed) {

			Color c = batch.getColor();
			Color mycolor = new Color(1, 0, 0, .5f);
			batch.setColor(mycolor);

			for (int i = 0; i < stage.closed.length; i++) {
				for (int j = 0; j < stage.closed[0].length; j++) {
					if (stage.closed[i][j])
						batch.draw(white, (j*stage.unit_width*stage.scale), (i*stage.unit_height*stage.scale), stage.unit_width*stage.scale, stage.unit_height*stage.scale);
				}
			}
			batch.setColor(c);
		}

	}

	// used to draw trees after units have been drawn
	public void drawTrees(SpriteBatch batch) {
		TextureRegion texture;
		for (int i = 0; i < stage.size_y; i++) {
			for (int j = 0; j < stage.size_x; j++) {
				texture = null;
				if (objects[i][j] == Object.TREE) 
					texture = tree;
				if (texture != null) batch.draw(texture, ((j-TREE_X_OFFSET)*stage.unit_width*stage.scale), ((i-TREE_Y_OFFSET)*stage.unit_height*stage.scale), TREE_WIDTH*stage.unit_width*stage.scale, TREE_HEIGHT*stage.unit_height*stage.scale);
			}
		}
	}
	
	

	private void drawRange(Unit drawRange, SpriteBatch batch) {
		if (drawRange.bowOut() && !drawRange.retreating) {
			Color c = batch.getColor();
			Color mycolor = new Color(1, 0, 0, .15f);
			
			float max_alpha = .3f;
			float base_alpha = .1f;
			
			batch.setColor(mycolor);

			int center_x = drawRange.pos_x;
			int center_y = drawRange.pos_y;

			int range = (int) drawRange.getCurrentRange();
			for (int i = -range; i < range; i++) {
				for (int j = -range; j < range; j++) {
					if (i == 0 && j == 0) continue;
					if (i*i + j*j < range*range && center_x+i >= 0 && center_y+j >= 0 && center_x+i < stage.size_x && center_y+j < stage.size_y) {
						// calculate distance as fraction of range
						float alpha_factor = (float)(Math.sqrt(i*i+j*j)/range);
						mycolor.a = (1-alpha_factor) * max_alpha + base_alpha;
						batch.setColor(mycolor);
						
						if (drawRange.orientation == Orientation.UP)
							if (Math.abs(i) < Math.abs(j) && j > 0) 
								batch.draw(white, (center_x+i)*stage.unit_width*stage.scale, (center_y+j)*stage.unit_height*stage.scale , stage.unit_width*stage.scale, stage.unit_height*stage.scale);
						if (drawRange.orientation == Orientation.DOWN) 
							if (Math.abs(i) < Math.abs(j) && j < 0) 
								batch.draw(white, (center_x+i)*stage.unit_width*stage.scale, (center_y+j)*stage.unit_height*stage.scale , stage.unit_width*stage.scale, stage.unit_height*stage.scale);
						if (drawRange.orientation == Orientation.LEFT)
							if (Math.abs(i) > Math.abs(j) && i < 0) 
								batch.draw(white, (center_x+i)*stage.unit_width*stage.scale, (center_y+j)*stage.unit_height*stage.scale , stage.unit_width*stage.scale, stage.unit_height*stage.scale);
						if (drawRange.orientation == Orientation.RIGHT)
							if (Math.abs(i) > Math.abs(j) && i > 0) 
								batch.draw(white, (center_x+i)*stage.unit_width*stage.scale, (center_y+j)*stage.unit_height*stage.scale , stage.unit_width*stage.scale, stage.unit_height*stage.scale);
					}
				}
			}
			batch.setColor(c);
		}
	}

	private boolean addObject(int pos_x, int pos_y, Object object) {
		if (objects[pos_y][pos_x] == null && !stage.closed[pos_y][pos_x]) {
			objects[pos_y][pos_x] = object;
			return true;
		}
		return false;
	}
	
	private TextureRegion getTexture(GroundType ground) {
		TextureRegion texture;
		if (ground == GroundType.GRASS) texture = grass;
		else if (ground == GroundType.DARKGRASS) texture = darkgrass;
		else if (ground == GroundType.LIGHTGRASS) texture = lightgrass;
		else if (ground == GroundType.SAND) texture = sand;
		else if (ground == GroundType.WATER) texture = water;
		else if (ground == GroundType.MUD) texture = mud;
		else if (ground == GroundType.ROCK) texture = rock;
		else if (ground == GroundType.DARKROCK) texture = darkrock;
		else if (ground == GroundType.SNOW) texture = snow;
		else if (ground == GroundType.LIGHTSAND) texture = lightsand;
		else if (ground == GroundType.LIGHTSNOW) texture = lightsnow;
		else if (ground == GroundType.FLOWERS) texture = flowers;
		else if (ground == GroundType.FLOWERS2) texture = flowers2;
		else texture = dirt;

		return texture;
	}

	private MapType randomMapType() {
		int count = MapType.values().length;
		int index = (int) (Math.random() * count);
		return MapType.values()[index];
	}

	// close a spot of ground SIZE by SIZE
	private void closeGround(int y, int x) {
		for (int k = 0; k < SIZE; k++) {
			for (int l = 0; l < SIZE; l++) {
				stage.closed[y*SIZE + k][x*SIZE + l] = true;
			}
		}
	}

	@Override
	public void act(float delta) {

	}
	
	public boolean inMap(Point p) {
		if (p == null) return false;
		return p.pos_x < stage.size_x &&
				p.pos_y < stage.size_y && 
				p.pos_x >= 0 && 
				p.pos_y >= 0;
	}
}
