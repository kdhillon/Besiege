/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
//contains camera, controls and stage for main actor: Kingdom

package kyle.game.besiege;


import java.util.Date;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.battle.BattleStage;
import kyle.game.besiege.location.Castle;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.SidePanel;
import kyle.game.besiege.party.Subparty;

public class MapScreen implements Screen {
	private Kryo kryo;
	public boolean SIMULATE = false;

	private static final boolean VIEW_GENESIS = false; // without this, takes about 5 seconds to setAppropriateRunTarget.
	private static final boolean FORCERUN = false;
	private static final float SCROLL_SPEED = 2f;
	private static final float FAST_FORWARD_FACTOR = 3f;
	public static final float ZOOM_MAX = 10;
	public static final float ZOOM_MIN = .05f;
	private static final float ZOOM_RATE_Z = 500;
	private static final float CENTER_SPEED = 0.07f;
    private static final float MIN_CENTER_SPEED = 0.1f;

    //	private final Color background = new Color(0, 109.0f/255, 185.0f/255, 1);
	//	private final Color background = new Color(VoronoiGraph.OCEAN);
    private static final Color background = Color.BLACK;
//    private static final Color background = new Color(0x44447aff); // water
	//	private static final Color background = new Color(0x55aa44ff); // grass

	//	private final Color backgroundGrass = new Color(0x55aa44ff);
//	private static final Color backgroundGrass = new Color(0x000000ff);
	public static SidePanel sidePanelReference;
	public static Character characterReference;
	
	private float speedFactor;

	public Environment environment;

	// Switch kingdom camera from Orthographic to an angled one.
	public OrthographicCamera currentCamera;
	//	public Camera currentCamera;
	private OrthographicCamera kingdomCamera;
	//	public PerspectiveCamera kingdomPerspectiveCamera;
	public OrthographicCamera battleCamera;

	private OrthographicCamera minimap;
	public float rotation;

	private Stage currentStage;
	private Stage kingdomStage;
	private Stage battleStage;

	private Stage uiStage;
	private Kingdom kingdom;
	public BattleStage battle;

	private Fog fog;
	private MapControllerDesktop mapControllerDesktop;
	private MapControllerAndroid mapControllerAndroid;
	private Vector2 mousePos;

	private Character character;

	private SidePanel sidePanel;
	private boolean mouseOverPanel;
	
	private boolean generate;
	private boolean worldInitialized;
	private int worldInitCount = 0;

	public boolean fastForward;
	public boolean shouldFastForward;
	public boolean superFastForward;
	public boolean shouldSuperFastForward;
	public boolean crazyFastForward;
	public boolean slowDown;

	public boolean shouldLetRun; // for "waiting" and " ing", in location
	public boolean shouldCenter;

	private int keydown;
	public boolean fogOn;
	public boolean losOn;
	public boolean editOn; // "map editor" for placing cities
	private boolean fogToggle;
	private boolean losToggle;
	private boolean editToggle;
	private boolean nightToggle;

	private boolean wealthToggle;
	private boolean territoryToggle;
    private boolean cultureToggle;
    private boolean crestsToggle;
	private boolean armyCrestsToggle;
	private boolean debugToggle;

	private boolean toggleNextFormation;
	
	public static float wind;

	//	public PrintWriter out; // accessed by kingdom

	// doesn't necessarily require a kingdom either!
	public MapScreen(boolean generate, String name) {
		this.kryo = new Kryo();
		kryo.register(Array.ArrayIterable.class, new Serializer<Array.ArrayIterable>() {
			public void write (Kryo kryo, Output output, Array.ArrayIterable object) {

			}
			public Array.ArrayIterable read (Kryo kryo, Input input, Class<Array.ArrayIterable> type) {
				return null; // don't return an iterable
			}
		});
		// for some reason have to register Castle 
		kryo.register(Castle.class);
		kryo.register(Subparty.class);

		System.out.println("initializing mapscreen");
		
		character = new Character(name);
		setStaticCharacter(character);

		kingdomCamera = new OrthographicCamera(BesiegeMain.WIDTH, BesiegeMain.HEIGHT);

		// part of the problem is we can't pass the perspective matrix through the spritebatch, because it's meant for 
		// 2D rendering only.
		//		kingdomPerspectiveCamera = new PerspectiveCamera(60, BesiegeMain.WIDTH, BesiegeMain.HEIGHT);
		//		kingdomPerspectiveCamera.

		//		kingdomPerspectiveCamera.update();
		//		kingdomCamera.translate(0, 0, 500);
		//		kingdomCamera.rotate(-01, 1, 0, 0);
		//		kingdomCamera.normalizeUp();
		//		kingdomCamera.

		// I don't know why but have to set this to 2*Width
		battleCamera = new OrthographicCamera(2*BesiegeMain.WIDTH, BesiegeMain.HEIGHT);

		SpriteBatch kingdomBatch = new SpriteBatch(5460); // optimizes swapBuffers
		//kingdomStage = new Stage();
		kingdomStage = new Stage(0, 0, false, kingdomBatch);
		kingdomStage.setCamera(kingdomCamera);
		//		kingdomStage.setCamera(kingdomPerspectiveCamera);
		currentStage = kingdomStage;

		battleStage = new Stage(0, 0, false, kingdomBatch);
		battleStage.setCamera(battleCamera);

		//		this.currentCamera = kingdomPerspectiveCamera;
		this.currentCamera = kingdomCamera;
		
		// will this draw blank before kingdom loads?
//		kingdomStage.setViewport(BesiegeMain.WIDTH, BesiegeMain.HEIGHT, false);	
//		kingdomStage.act();
//		kingdomStage.draw();
	
		uiStage = new Stage();
		uiStage.addListener(new InputListener());

		sidePanel = new SidePanel(this);
		storeStaticSidePanel(sidePanel);
		
		// key to start initializing world;
		this.generate = generate;
		worldInitialized = false;
		//		environment = new Environment();
		//        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		//        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	// don't generate kingdom
	public MapScreen() {
		character = new Character("Default");
		setStaticCharacter(character);

		// I don't know why but have to set this to 2*Width
		battleCamera = new OrthographicCamera(2*BesiegeMain.WIDTH, BesiegeMain.HEIGHT);

		SpriteBatch kingdomBatch = new SpriteBatch(5460); // optimizes swapBuffers

		battleStage = new Stage(0, 0, false, kingdomBatch);
		battleStage.setCamera(battleCamera);

		this.currentCamera = battleCamera;

		uiStage = new Stage();
		uiStage.addListener(new InputListener());
		
		sidePanel = new SidePanel(this);

		storeStaticSidePanel(sidePanel);

		currentStage = battleStage;

		startLog();

		worldInitialized = true;
		
		center();

		mapControllerAndroid = new MapControllerAndroid(currentCamera, this);
		mapControllerDesktop = new MapControllerDesktop(currentCamera, this);

		//		kingdomStage.addActor(fog); // test to see if this is slowing things down
		mousePos = new Vector2(0,0);
		rotation = 0;
		speedFactor = 1;

		uiStage.addActor(sidePanel);
		mouseOverPanel = false;
		keydown = 0;

		startLog();
	}
	
	
	public void initializeWorldStep() {
//		System.out.println("initializeWorldStep");
		// just to make sure the kingdom is getting drawn
		if (VIEW_GENESIS && worldInitCount < 10) {
			worldInitCount++; return;
		}
		
		if (!VIEW_GENESIS || worldInitCount < 11) {
			if (generate) {
				if (kingdom == null) {
					kingdom = new Kingdom(this);
					kingdomStage.addActor(kingdom);
					sidePanel.setKingdom(kingdom);
				}
				if (VIEW_GENESIS) {
					if (!kingdom.initialized) {
						kingdom.initStep();
						center();
						return;
					}
				}
				else {
					while (!kingdom.initialized) {
						kingdom.initStep();
					}
					if (generate) kingdom.addPlayer();
					center();
				}
				if (kingdom.initialized && kingdom.getPlayer() == null) {
					if (generate)
						kingdom.addPlayer();
				}
				center();
			}
			else {
				this.load();
				if (kingdom == null) System.out.println("kingdom still null");
			}
			
			center();
			//		fog = new Fog(this);

			mapControllerAndroid = new MapControllerAndroid(currentCamera, this);
			mapControllerDesktop = new MapControllerDesktop(currentCamera, this);

			//		kingdomStage.addActor(fog); // test to see if this is slowing things down
			mousePos = new Vector2(0,0);
			rotation = 0;
			speedFactor = 1;

			uiStage.addActor(sidePanel);
			mouseOverPanel = false;
			keydown = 0;

			sidePanel.initializePanels();
		}
		if (VIEW_GENESIS && worldInitCount < 15) {
			center();
			worldInitCount++; return;
		}
		if (!VIEW_GENESIS || worldInitCount < 16) {
			center();
//			rotate((float) (360 * Math.random()));

			shouldCenter = true;
			shouldFastForward = false;
			shouldLetRun = false;

			fogOn = false;
			losOn = false;
			fogToggle = false;
			losToggle = true;

			toggleNextFormation = false;

			startLog();
		}
		if (VIEW_GENESIS && worldInitCount < 20) {
			worldInitCount++; return;
		}
		
		worldInitialized = true;
		kingdom.setPaused(true);
		center();
		if (!VIEW_GENESIS) {
			kingdomCamera.zoom = 0.5f;
			fogOn = true;
			
		}
	}
	
	public static void storeStaticSidePanel(SidePanel sp) {
		MapScreen.sidePanelReference = sp;
	}

	public static void setStaticCharacter(Character c) {
		MapScreen.characterReference = c;
	}
	
	private void startLog() {
		BottomPanel.log("Welcome to Besiege! This is the alpha release. Enjoy!", "green");
		BottomPanel.log("Controls: ", "orange");
		BottomPanel.log("Move: right-click       Pan camera: WASD       Rotate camera: Q,E       Zoom: mouse wheel       Wait: hold space", "orange");
		BottomPanel.log("View map info: g       View factions: t         8x Speed: hold f          Toggle Line of Sight: l", "orange");
		BottomPanel.log("Check out the source code at github.com/kdhillon/besiege", "yellow");
	}

	@Override
	public void resize(int width, int height) {
		BesiegeMain.HEIGHT = height;
		BesiegeMain.WIDTH = width;

		if (kingdomStage != null) kingdomStage.setViewport(BesiegeMain.WIDTH, BesiegeMain.HEIGHT, true);
		if (battleStage != null) battleStage.setViewport(BesiegeMain.WIDTH, BesiegeMain.HEIGHT, true);
        uiStage.setViewport(BesiegeMain.WIDTH, BesiegeMain.HEIGHT, true);
		//		kingdomPerspectiveCamera = new PerspectiveCamera(60, BesiegeMain.WIDTH, BesiegeMain.HEIGHT);
		//		kingdomPerspectiveCamera.position.set(2000, 3000, 2000);
		//		kingdomPerspectiveCamera.lookAt(2000, 0, 0);
		//		// it starts up
		//		kingdomPerspectiveCamera.up.mul(new Vector3(-1, -1, -1));
		////		kingdomPerspectiveCamera.normalizeUp();
		//		kingdomPerspectiveCamera.near = 1f;
		//		kingdomPerspectiveCamera.far = 20000f;
		//		kingdomPerspectiveCamera.update();

		if (this.currentCamera == null)
			this.currentCamera = kingdomCamera;

		//		BottomPanel.log("resizing to " + width + ", " + height, "white");
		//		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		//        Gdx.gl.glEnable(GL10.GL_BLEND);
		//	if (currentStage == kingdomStage)
		center();
	}

	@Override
	public void render(float delta) {			
		//		kingdomPerspectiveCamera.rotate(new Vector3(0,0.5f,0.5f), .1f);
		//		kingdomPerspectiveCamera.update();
		if ((kingdom != null && kingdom.getPlayer() != null) || battle != null)
			handleInput();
		Gdx.gl.glEnable(GL20.GL_BLEND);

		// have to really be careful if we want to do backface culling and draw vertices in order.
		//		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		//		Gdx.gl.glCullFace(GL20.GL_BACK);

		// keys for preventing slow display with SwapBuffers taking a 
		// long time: disable vsync. maybe a better solution somewhere.

		if (currentCamera == kingdomCamera && kingdom != null)
			//		if (currentCamera == kingdomPerspectiveCamera)
			Gdx.gl.glClearColor(background.r*kingdom.currentDarkness, background.g*kingdom.currentDarkness, 
					background.b*kingdom.currentDarkness, background.a);
		else if (battle != null) {
			Gdx.gl.glClearColor(battle.battlemap.bgColor.r*battle.currentDarkness, battle.battlemap.bgColor.g*battle.currentDarkness, 
					battle.battlemap.bgColor.b*battle.currentDarkness, battle.battlemap.bgColor.a);
		}
		else {
			Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);	
		}
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (crazyFastForward) crazyFastForward(delta);
		if (superFastForward) { // experimental 16x speed!
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			uiStage.act(delta);
		}
		if (fastForward) { // x8 speed (naive approach)!!
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			//			uiStage.act(delta);
			currentStage.act(delta);
			uiStage.act(delta);

			// doesn't work
			//			currentStage.act(8*delta);
			//			uiStage.act(8*delta);
		}
		else {
			currentStage.act(delta);
			uiStage.act(delta);
		}

		if (kingdom != null) 
			if (kingdom.raining) kingdom.rain();
		if (battle != null) battle.updateColor(currentStage.getSpriteBatch());
		else if (kingdom != null) kingdom.updateColor(currentStage.getSpriteBatch());

		updateWind(delta);
		
		currentStage.draw();
		uiStage.draw();

		// if (shouldCenter && !kingdom.isPaused()) center(); // maybe should be in kingdom
		if (shouldCenter) center(); // maybe should be in kingdom
		
		if (!worldInitialized) {
			initializeWorldStep();
		}
	}

	private void crazyFastForward(float delta) {
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		currentStage.act(delta);
		uiStage.act(delta);
	}
	
	public void updateWind(float delta) {
		float WIND_CHANGE = 10f;
		wind += (Math.random() * WIND_CHANGE - WIND_CHANGE /2) * delta ;
//		System.out.println("wind: " + wind);
		if (wind > 1) wind = 1;
		if (wind < -1) wind = -1;
	}

	public float getSpeed() {
		return SCROLL_SPEED * speedFactor * currentCamera.zoom;
	}
	
	public void moveUp() {
		currentCamera.translate(currentCamera.up.scl(getSpeed(), getSpeed(), 0));
		currentCamera.up.scl(1/(getSpeed()), 1/(getSpeed()), 0);
	}
	public void moveDown() {
		currentCamera.translate(currentCamera.up.scl(-getSpeed(), -getSpeed(), 0));
		currentCamera.up.scl(-1/(getSpeed()), -1/(getSpeed()), 0);
	}
	public void moveLeft() {
		currentCamera.translate(currentCamera.up.rotate(90, 0, 0, 1).scl(getSpeed(), getSpeed(),0));
		currentCamera.up.scl(1/(getSpeed()), 1/(getSpeed()), 0).rotate(-90, 0, 0, 1);
	}
	public void moveRight() {
		currentCamera.translate(currentCamera.up.rotate(-90, 0, 0, 1).scl(getSpeed(),getSpeed(),0));
		currentCamera.up.scl(1/(getSpeed()), 1/(getSpeed()), 0).rotate(90, 0, 0, 1);
	}

	//	public void moveUp() {
	//		currentCamera.translate(currentCamera.up.scale(SCROLL_SPEED*speedFactor*currentCamera.zoom, SCROLL_SPEED*speedFactor*currentCamera.zoom, 0));
	//		currentCamera.up.scale(1/(SCROLL_SPEED*speedFactor)/currentCamera.zoom, 1/(SCROLL_SPEED*speedFactor)/currentCamera.zoom, 0);
	//	}
	//	public void moveDown() {
	//		currentCamera.translate(currentCamera.up.scale(-SCROLL_SPEED*speedFactor*currentCamera.zoom, -SCROLL_SPEED*speedFactor*currentCamera.zoom, 0));
	//		currentCamera.up.scale(-1/(SCROLL_SPEED*speedFactor)/currentCamera.zoom, -1/(SCROLL_SPEED*speedFactor)/currentCamera.zoom, 0);
	//	}
	//	public void moveLeft() {
	//		currentCamera.translate(currentCamera.up.rotate(90, 0, 0, 1).scale(SCROLL_SPEED*speedFactor*currentCamera.zoom, SCROLL_SPEED*speedFactor*currentCamera.zoom,0));
	//		currentCamera.up.scale(1/(SCROLL_SPEED*speedFactor)/currentCamera.zoom, 1/(SCROLL_SPEED*speedFactor)/currentCamera.zoom, 0).rotate(-90, 0, 0, 1);
	//	}
	//	public void moveRight() {
	//		currentCamera.translate(currentCamera.up.rotate(-90, 0, 0, 1).scale(SCROLL_SPEED*speedFactor*currentCamera.zoom,SCROLL_SPEED*speedFactor*currentCamera.zoom,0));
	//		currentCamera.up.scale(1/(SCROLL_SPEED*speedFactor)/currentCamera.zoom, 1/(SCROLL_SPEED*speedFactor)/currentCamera.zoom, 0).rotate(90, 0, 0, 1);
	//	}
	public void rotate(float factor) {
		if (currentCamera == kingdomCamera) rotation += factor;
		currentCamera.rotate(factor, 0, 0, 1);
	}
	public void zoom(float factor) {
		//		if (this.currentCamera == kingdomPerspectiveCamera) {
		//			this.kingdomPerspectiveCamera.translate(0, 0, ZOOM_RATE_Z*factor);
		//		}
		//		else

		// adjust factor to be proportional to height
		float speed = 0.7f;
		if (this.currentCamera == kingdomCamera) {
			factor *= (kingdomCamera.zoom - ZOOM_MIN*0.5) * speed;
			if ((factor > 0) && (kingdomCamera.zoom < ZOOM_MAX) || (factor < 0) && (kingdomCamera.zoom > ZOOM_MIN))
				kingdomCamera.zoom += factor;
		}
		else if (this.currentCamera == battleCamera) {
			factor *= (battleCamera.zoom - ZOOM_MIN*0.5) * speed;
			if ((factor > 0) && (battleCamera.zoom < ZOOM_MAX) || (factor < 0) && (battleCamera.zoom > ZOOM_MIN))
				battleCamera.zoom += factor;
			SoundPlayer.setZoomBattle(battleCamera.zoom);
		}
	}

	public void letRun() {
		if (battle == null) {
			kingdom.setPaused(false);
			kingdom.getPlayer().setWaiting(true);	
		}
		else {
			//			battle.setPaused(false);
		}
	}
	public void endRun() {
		if (battle == null) {
			kingdom.getPlayer().setWaiting(false);
			kingdom.setPaused(true);
			System.out.println("end setAppropriateRunTarget");
		}
		//		else {
		//			battle.setPaused(true);
		//		}
	}
	public void click(int pointer) {
		if (battle != null) battle.click(pointer);
		else kingdom.click(pointer);
	}

	public void hardCenter() {
        currentCamera.translate(kingdom.getPlayer().getCenterX() - currentCamera.position.x, kingdom.getPlayer().getCenterY() - currentCamera.position.y);
    }

	public void center() {
		if (currentCamera == battleCamera) {
			if (battle != null) battle.centerCameraOnPlayer();
		}
		else if (currentCamera == kingdomCamera && kingdom != null && kingdom.getPlayer() != null && worldInitialized) {
            //		if (currentCamera == kingdomPerspectiveCamera)
			//			currentCamera.translate(new Vector2(kingdom.getPlayer().getCenterX()-currentCamera.position.x, kingdom.getPlayer().getCenterY()-currentCamera.position.y));
            float xSpeed = (kingdom.getPlayer().getCenterX() - currentCamera.position.x) * CENTER_SPEED;
            if (xSpeed < MIN_CENTER_SPEED && xSpeed > -MIN_CENTER_SPEED)
                 xSpeed = 0;

            float ySpeed = (kingdom.getPlayer().getCenterY() - currentCamera.position.y) * CENTER_SPEED;
            if (ySpeed < MIN_CENTER_SPEED && ySpeed > -MIN_CENTER_SPEED)
                ySpeed = 0;

                //                ySpeed = Math.max(ySpeed, MIN_CENTER_SPEED);
//            else if (ySpeed < -0.1) ySpeed = Math.min(ySpeed, -MIN_CENTER_SPEED);

            currentCamera.translate(new Vector3(xSpeed, ySpeed, 0));
		}
		else if (kingdom != null && kingdom.map != null) {
			currentCamera.translate(new Vector3(Map.WIDTH/2-currentCamera.position.x, Map.HEIGHT/2-currentCamera.position.y, 0));
			kingdomCamera.zoom = 10f;;		
		} else {
            System.out.println((currentCamera != null) + " " + (kingdom != null) + " " + worldInitialized);
            if (kingdom != null) {
                System.out.println(kingdom.getPlayer() != null);
            }
//            throw new AssertionError();
        }
		
	}
	public void handleInput() {
		if (mousePos == null) return;
		mousePos.x = Gdx.input.getX();
		mousePos.y = Gdx.input.getY();
//				BottomPanel.log("mouse at " + mousePos.x + ", " + mousePos.y);

		if (mousePos.x > BesiegeMain.WIDTH-SidePanel.WIDTH || mousePos.y > BesiegeMain.HEIGHT - BottomPanel.HEIGHT) {
			mouseOverPanel = true;
			//			System.out.println("mousing over stage");
			Gdx.input.setInputProcessor(uiStage);
		}
		else {
			InputMultiplexer inputMultiplexer = new InputMultiplexer();
			if (kingdomStage != null)
				inputMultiplexer.addProcessor(kingdomStage);

			if (Gdx.app.getType() == ApplicationType.Desktop)
				inputMultiplexer.addProcessor(mapControllerDesktop);
			else if (Gdx.app.getType() == ApplicationType.WebGL)
				inputMultiplexer.addProcessor(mapControllerDesktop);
			else if (Gdx.app.getType() == ApplicationType.Android && Gdx.input.getInputProcessor() == uiStage)
				inputMultiplexer.addProcessor(new GestureDetector(mapControllerAndroid));
			Gdx.input.setInputProcessor(inputMultiplexer);

			mouseOverPanel = false;
		}
		if (kingdom != null)
			kingdom.setMouseOver(!mouseOverPanel);
		if (currentStage == battleStage)
			battle.setMouseOver(!mouseOverPanel);

		currentStage.screenToStageCoordinates(mousePos);
		if (kingdom != null)
			kingdom.setMouse(mousePos);
		if (currentStage == battleStage)
			battle.setMouse(mousePos);

		if (Gdx.app.getType() == ApplicationType.Desktop || Gdx.app.getType() == ApplicationType.WebGL) {
			// this will update camera position on desktop!
			if (Gdx.input.isKeyPressed(Keys.W)) {
				moveUp();
				shouldCenter = false;
			}
			if (Gdx.input.isKeyPressed(Keys.S)) {
				moveDown();
				shouldCenter = false;
			}
			if (Gdx.input.isKeyPressed(Keys.D)) {
				moveRight();
				shouldCenter = false;
			}
			if (Gdx.input.isKeyPressed(Keys.A)) {
				moveLeft();
				shouldCenter = false;
			}
			
			if (Gdx.input.isKeyPressed(Keys.Q)) 
				rotate(.5f);
			if (Gdx.input.isKeyPressed(Keys.E)) 
				rotate(-.5f);

			if (Gdx.input.isKeyPressed(Keys.N))   
				zoom(.03f);
			if (Gdx.input.isKeyPressed(Keys.M))
				zoom(-.03f);

			if (FORCERUN) {
				shouldSuperFastForward = true;
				letRun();
			}
			else {
				if (SIMULATE || Gdx.input.isKeyPressed(Keys.SPACE) || (kingdom != null && kingdom.getPlayer().isWaiting() && kingdom.getPlayer().forceWait) || kingdom != null && kingdom.getPlayer().isInBattle() || (shouldLetRun && (kingdom != null && kingdom.getPlayer().isGarrisoned() || (kingdom != null && kingdom.getPlayer().isInSiege())))) {
					letRun();
					if (battle != null && Gdx.input.isKeyPressed(Keys.SPACE)) battle.placementPhase = false;
					//				System.out.println(kingdom.getPlayer().isWaiting());
				}
				else if (kingdom != null && !kingdom.getPlayer().forceWait && kingdom.getPlayer().isWaiting()) {
					//				System.out.println("ending setAppropriateRunTarget");
					endRun();
				}
			}
			if ((Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)|| Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) && speedFactor != FAST_FORWARD_FACTOR)
				speedFactor = FAST_FORWARD_FACTOR;
			else if (speedFactor != 1)
				speedFactor = 1;
			if (Gdx.input.isKeyPressed(Keys.C)) {
				shouldCenter = true;
			}

			//			if (!editOn) {
			//				if (Gdx.input.isButtonPressed(0) && !kingdom.getPlayer().isGarrisoned()) // allows holding down mouse.
			//					click(0);
			//			}
			if (Gdx.input.isKeyPressed(Keys.ENTER)) {
				click(1);
			}
			if (Gdx.input.justTouched()) {
				if (!editOn) {
					if (Gdx.input.isButtonPressed(0)) {
						click(0);
						//						System.out.println("clicked");
					}
					else if (Gdx.input.isButtonPressed(1)) {
						click(1);
					}
				}
				else {
					click(4); // arbitrary number to places cities
				}
			}

			if (Gdx.input.isKeyPressed(Keys.F) || shouldFastForward)
				fastForward = true;
			else if (fastForward)
				fastForward = false;

			if (Gdx.input.isKeyPressed(Keys.P) || SIMULATE || shouldSuperFastForward)
				superFastForward = true;
			else if (superFastForward)
				superFastForward = false;
			
			if (Gdx.input.isKeyPressed(Keys.H))
				slowDown = true;
			else if (slowDown)
				slowDown = false;

			if (SIMULATE)
				crazyFastForward = true;
			else if (crazyFastForward)
				crazyFastForward = false;

			if (Gdx.input.isKeyPressed(Keys.O))
				fogToggle = true;
			else if (fogToggle) {
				toggleFog();
				fogToggle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.L))
				losToggle = true;
			else if (losToggle) {
				toggleLOS();
				losToggle = false;
			}
			//			if (Gdx.input.isKeyPressed(Keys.E)
			//				editToggle = true;
			//			else if (editToggle) {
			////				save();
			////				toggleEdit();
			//				editToggle = false;
			//			}
			if (Gdx.input.isKeyPressed(Keys.B))
				nightToggle = true;
			else if (nightToggle) {
//				load();

				if (kingdom != null) kingdom.toggleNight();
				nightToggle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.T))
				territoryToggle = true;
			else if (territoryToggle) {
				Map.drawSpheres = !Map.drawSpheres;
				territoryToggle = false;
			}
            if (Gdx.input.isKeyPressed(Keys.V))
                cultureToggle = true;
            else if (cultureToggle) {
                Map.drawCultures = !Map.drawCultures;
                cultureToggle = false;
            }
			if (Gdx.input.isKeyPressed(Keys.R))
				wealthToggle = true;
			else if (wealthToggle) {
				Map.drawWealth = !Map.drawWealth;
				if (kingdom != null)
				    kingdom.map.updateAllCenterColors();
				wealthToggle = false;
			}
			
			if (Gdx.input.isKeyPressed(Keys.G))
				debugToggle = true;
			else if (debugToggle) {
				Map.debug = !Map.debug;
				debugToggle = false;
			}

			if (Gdx.input.isKeyPressed(Keys.Y))
				crestsToggle = true;
			else if (crestsToggle) {
				Kingdom.drawCrests = !Kingdom.drawCrests;
				crestsToggle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.U))
				armyCrestsToggle = true;
			else if (armyCrestsToggle) {
				Kingdom.drawArmyCrests = !Kingdom.drawArmyCrests;
				armyCrestsToggle = false;
			}

			if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
				sidePanel.setDefault(true);
				if (battle != null) battle.selectedUnit = null;
			}
			//			if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			//				Gdx.app.exit();
			//			}

			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				if (battle != null && battle.placementPhase) {
					toggleNextFormation = !toggleNextFormation;
					
					// TODO change back
//					if (toggleNextFormation) battle.toNextFormation();
				}
			}

			if (keydown != 0) {
				if (keydown == 1) {
					if (!Gdx.input.isKeyPressed(Keys.NUM_1)) {
						sidePanel.press(1);
						keydown = 0;
					}
				}
				else if (keydown == 2) {
					if (!Gdx.input.isKeyPressed(Keys.NUM_2)) {
						sidePanel.press(2);
						keydown = 0;
					}
				}
				else if (keydown == 3) {
					if (!Gdx.input.isKeyPressed(Keys.NUM_3)) {
						sidePanel.press(3);
						keydown = 0;
					}
				}
				else if (keydown == 4) {
					if (!Gdx.input.isKeyPressed(Keys.NUM_4) && !Gdx.input.isKeyPressed(Keys.BACKSPACE)) {
						sidePanel.press(4);
						keydown = 0;
					}
				}
			}
			else {
				//bootleg key-up
				if (Gdx.input.isKeyPressed(Keys.NUM_1)) {
					keydown = 1;
				}
				else if (Gdx.input.isKeyPressed(Keys.NUM_2)) {
					keydown = 2;
				}
				else if(Gdx.input.isKeyPressed(Keys.NUM_3)) {
					keydown = 3;
				}
				else if(Gdx.input.isKeyPressed(Keys.NUM_4) || Gdx.input.isKeyPressed(Keys.BACKSPACE)) {
					keydown = 4;
				}
				else keydown = 0;
			}
		}
	}

	// switch to battle view
	public void switchToBattleView(BattleStage bs) {
		this.battle = bs;
		this.battleStage.addActor(battle);
		this.battleCamera = new OrthographicCamera(BesiegeMain.WIDTH, BesiegeMain.HEIGHT);
		this.battleStage.setCamera(battleCamera);
		this.currentCamera = battleCamera;
		this.sidePanel.setActiveBattle(battle);
		this.shouldCenter = false;
		center();
		battleCamera.zoom = 1f;
		currentStage = battleStage;
		System.out.println("switching to battle view");
	}

	public void switchToKingdomView() {
		//this.battle = null;
		this.battleStage.clear();
		this.battle = null;
		currentStage = kingdomStage;
		this.currentCamera = kingdomCamera;
		//		this.currentCamera = kingdomPerspectiveCamera;

        hardCenter();

		this.sidePanel.clean();
		this.sidePanel.setDefault(true);
		kingdom.currentDarkness = kingdom.targetDarkness;

		System.out.println("switching to kingdom view");
		if (!kingdom.getPlayer().forceWait) kingdom.setPaused(true);
	}

	public void playerDeath() {
		System.out.println("player died");
	}

	@Override
	public void show() {
		//BottomPanel.log("gdx apptype: " + Gdx.app.getType(), "orange");

		if (Gdx.app.getType() == ApplicationType.Desktop) {
			//BottomPanel.log("setting controller to desktop");
			//			may not register, may be unnecessary
			Gdx.input.setInputProcessor(mapControllerDesktop);
		}
		else if (Gdx.app.getType() == ApplicationType.Android) {
			//BottomPanel.log("setting controller to android");
			Gdx.input.setInputProcessor(new GestureDetector(mapControllerAndroid));		
		}
		else if (Gdx.app.getType() == ApplicationType.WebGL){
			Gdx.input.setInputProcessor(mapControllerDesktop);
		}
		else System.out.println("invalid app type!");
	}

	public void toggleLOS() {
		this.losOn = !losOn;
	}
	public void toggleFog() {
		this.fogOn = !fogOn;
	}
	public void toggleEdit() {
		this.editOn = !editOn;
		//		if (editOn == true) {
		//			BottomPanel.log("Editor mode enabled", "magenta");
		//			out = new PrintWriter((Gdx.files.absolute("data/map.txt").writer(false))); // boolean append, must be absolute, goes to Desktop data directory
		//
		//			out.print("Hello "); 
		//			out.println("world");
		//		}
		//		else {
		//			BottomPanel.log("Editor mode disabled", "magenta");
		//			out.close(); 
		//		}
	}

	public void save() {	
		//		System.out.println("saving");
		//		this.sidePanel.beginSaving();
		//		BottomPanel.log("presaving");
		// Should not save MapScreen or SidePanel.
		// Should only save Kingdom.
		// Kryo errors read bottom to top (bottom is always Kingdom)
		// need to draw for one frame to let everyone know what's going o
		FileHandle file = Gdx.files.local("save.dat");
		Output output = new Output(file.write(false));

		//		Log.DEBUG();

		Date date = new Date();
		kryo.writeObjectOrNull(output, date, date.getClass());
		kingdom.setPaused(true);
		kingdom.remove();
		kryo.writeObjectOrNull(output, this.kingdom, this.kingdom.getClass());

		output.close();

		this.kingdomStage.addActor(kingdom);

		this.sidePanel.endSaving();
		BottomPanel.log("Game saved successfully!");
	}

	public void load() {
		if (kryo == null) return;

		Input input = null;
		try {
			FileHandle file = Gdx.files.local(getSaveFileName());
			input = new Input(file.read());
		}
		catch (Exception e) {
			System.out.println("no save file");
			return;
		}

		Date date = kryo.readObjectOrNull(input, Date.class);

		if (this.kingdom != null) this.kingdom.remove();

		Kingdom kingdom = kryo.readObjectOrNull(input, Kingdom.class);
		this.kingdom = kingdom;

		if (this.kingdom == null) System.out.println("loaded null kingdom");

		for (Army army : kingdom.getArmies()) army.restoreAnimation();

		Array<Location> locations = kingdom.getAllLocationsCopy();
		for (Location location : locations) {
			location.restoreTexture();
		}

		kingdom.getMap().initialize();
		this.kingdom.setMapScreen(this);
		this.kingdomStage.addActor(kingdom);
		// restore faction crests()
		kingdom.restoreFactionCrests();
		this.sidePanel.setActiveArmy(kingdom.getPlayer());
		this.sidePanel.setKingdom(kingdom);
		storeStaticSidePanel(sidePanel);

		this.center();
		System.out.println("Loaded save from " + date.toString());
	}

	public static String getSaveFileName() {
		return "save.dat";
	}

	@Override
	public void hide() {
		System.out.println("hiding");

		Gdx.input.setInputProcessor(null);	
		if (kingdom != null) kingdom.setPaused(true);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		// SAVE/Pause
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		// RESUME
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Stage getKingdomStage() {
		return kingdomStage;
	}

	public Stage getUIStage() {
		return uiStage;
	}

	public Camera getUICamera() {
		return uiStage.getCamera();
	}

	public float getRotation() {
		return rotation;
	}
	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	//	public OrthographicCamera getCamera() {
	//		return currentCamera;
	//		//		return kingdomCamer
	//	}
	public Camera getCamera() {
		return currentCamera;
		//		return kingdomCamer
	}
	public Kingdom getKingdom() {
		return kingdom;
	}
	public Fog getFog() {
		return fog;
	}
	public Character getCharacter() {
		return character;
	}
	public SidePanel getSidePanel() {
		return sidePanel;
	}
	public float getZoom() {
		//		if (this.currentCamera == kingdomPerspectiveCamera) {
		//			return this.currentCamera.position.z;
		//		}
		//		else 
		if (this.currentCamera == kingdomCamera) {
			return kingdomCamera.zoom;
		}
		else if (this.currentCamera == battleCamera) {
			return battleCamera.zoom;
		}
		return -9999999;
	}
}
