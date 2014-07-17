package kyle.game.besiege.battle;

import kyle.game.besiege.battle.Unit.Orientation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;


public class BattleMap extends Actor {
	boolean raining;
	private TextureRegion white;

	private enum MapType {
		FOREST, BEACH, GRASSLAND, DESERT, ALPINE, MEADOW
	}
	private MapType maptype;

	private BattleStage stage;
	private final int SIZE = 4;
	private int total_height;
	private int total_width;

	private enum GroundType {
		GRASS, DIRT, SAND, DARKGRASS, MUD, WATER, LIGHTGRASS, SNOW, ROCK, LIGHTSAND, LIGHTSNOW, FLOWERS, FLOWERS2
	}
	
	private enum Object {
		TREE, WALL_V, WALL_H
	}

	private GroundType[][] ground;
	private Object[][] object;

	private TextureRegion grass, flowers, flowers2, dirt, sand, darkgrass, mud, water, tree, lightgrass, rock, snow, lightsnow, lightsand, wallV, wallH;


	public BattleMap(BattleStage mainmap) {
		this.stage = mainmap;
		
		this.maptype = randomMapType();
//		this.maptype = MapType.MEADOW;


		this.total_height = mainmap.size_y/SIZE;
		this.total_width = mainmap.size_x/SIZE;

		ground = new GroundType[total_height][total_width];
		object = new Object[mainmap.size_y][mainmap.size_x];

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
		snow = 		new TextureRegion(new Texture(Gdx.files.internal("ground/snow.png")));
		lightsnow = new TextureRegion(new Texture(Gdx.files.internal("ground/lightsnow.png")));
		lightsand = new TextureRegion(new Texture(Gdx.files.internal("ground/sandlight.png")));
		tree = 		new TextureRegion(new Texture(Gdx.files.internal("ground/tree.png")));
		wallV = 	new TextureRegion(new Texture(Gdx.files.internal("stone_fence_v.png")));
		wallH = 	new TextureRegion(new Texture(Gdx.files.internal("stone_fence.png")));
		// generate random map
		white = new TextureRegion(new Texture("whitepixel.png"));

		
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
			addWalls(5);
			addTrees(.05);
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
			addTrees(.00);
			addWalls(5);
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
		}
	}
	
	private void addTrees(double probability) {
		for (int i = 0; i < stage.size_y; i++) {
			for (int j = 0; j < stage.size_x; j++) {
				if (Math.random() < probability) {
					object[j][i] = Object.TREE;
					stage.closed[j][i] = true;
//					mainmap.closed[i][j] = true;
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
					if (vertical) {
						object[wall_start_y+i][wall_start_x] = Object.WALL_V;
						stage.slow[wall_start_y+i][wall_start_x] = .5;
					}
					else {
						object[wall_start_y][wall_start_x+i] = Object.WALL_H;
						stage.slow[wall_start_y][wall_start_x+i] = .5;
					}
				}
			}
		}
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		TextureRegion texture;
		
		this.toBack();

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


				batch.setColor(c);
			}
		}
		}
		
		// draw obstacles
		for (int i = 0; i < stage.size_y; i++) {
			for (int j = 0; j < stage.size_x; j++) {
				texture = null;
				if (object[i][j] == Object.TREE) 
					texture = tree;
				else if (object[i][j] == Object.WALL_V) 
					texture = wallV;
				else if (object[i][j] == Object.WALL_H) 
					texture = wallH;
				
				if (texture != null) batch.draw(texture, (j*stage.unit_width*stage.scale), (i*stage.unit_height*stage.scale), stage.unit_width*stage.scale, stage.unit_height*stage.scale);
			}
		}
		
		// draw range of selected unit
		if (stage.currentPanel != null && stage.currentPanel.isRanged()) {

			Color c = batch.getColor();
			Color mycolor = new Color(1, 0, 0, .3f);
			batch.setColor(mycolor);
			
			int center_x = stage.currentPanel.pos_x;
			int center_y = stage.currentPanel.pos_y;
			
			int range = stage.currentPanel.rangedWeapon.range;
			for (int i = -range; i < range; i++) {
				for (int j = -range; j < range; j++) {
					if (i == 0 && j == 0) continue;
					if (i*i + j*j < range*range) {
						if (stage.currentPanel.orientation == Orientation.UP) {
							if (Math.abs(i) < Math.abs(j) && j > 0) {
								batch.draw(white, (center_x+i)*stage.unit_width*stage.scale, (center_y+j)*stage.unit_height*stage.scale , stage.unit_width*stage.scale, stage.unit_height*stage.scale);
							}
						}
						if (stage.currentPanel.orientation == Orientation.DOWN) {
							if (Math.abs(i) < Math.abs(j) && j < 0) {
								batch.draw(white, (center_x+i)*stage.unit_width*stage.scale, (center_y+j)*stage.unit_height*stage.scale , stage.unit_width*stage.scale, stage.unit_height*stage.scale);
							}
						}
						if (stage.currentPanel.orientation == Orientation.LEFT) {
							if (Math.abs(i) > Math.abs(j) && i < 0) {
								batch.draw(white, (center_x+i)*stage.unit_width*stage.scale, (center_y+j)*stage.unit_height*stage.scale , stage.unit_width*stage.scale, stage.unit_height*stage.scale);
							}
						}
						if (stage.currentPanel.orientation == Orientation.RIGHT) {
							if (Math.abs(i) > Math.abs(j) && i > 0) {
								batch.draw(white, (center_x+i)*stage.unit_width*stage.scale, (center_y+j)*stage.unit_height*stage.scale , stage.unit_width*stage.scale, stage.unit_height*stage.scale);
							}
						}
					}
				}
			}
			
			
			batch.setColor(c);
		}
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
}
