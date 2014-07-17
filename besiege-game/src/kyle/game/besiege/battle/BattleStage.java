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
import kyle.game.besiege.panels.PanelBattle;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.Soldier;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public class BattleStage extends Group {
	private Battle battle;
	private PanelBattle pb;
	public float scale = 1.5f;

	public static boolean drawCrests = true;

	private final float MOUSE_DISTANCE = 15; // distance destination must be from mouse to register

	public Array<Unit> allies;
	public Array<Unit> enemies;

	Party player;
	Party enemy;

	public boolean closed[][]; // open or closed? 
	public double slow[][]; // 0 is normal speed, 1 is very slow.
	public Unit[][] map; // units on map

	public int size_x = 128;
	public int size_y = 128;

	private MapScreen mapScreen;
	private Kingdom kingdom;
	//		private Party player;
	private BattleMap battlemap;

	private boolean mouseOver; // is mouse over Battle screen?
	private boolean paused;

	public int unit_width = 16;
	public int unit_height = 16;

	public Unit selectedUnit;
	public Unit currentPanel;

	private boolean leftClicked;
	private boolean rightClicked;
	private Point mouse;

	// take in battle object containing arrays of armies and stuff
	public BattleStage(MapScreen mapScreen, Party player, Party enemy) {
		this.mapScreen = mapScreen;
		
		mouse = new Point(0,0);
		
		this.player = player;
		this.enemy = enemy;

		this.kingdom = mapScreen.getKingdom();
		
		if (player == null)
			createFakeBattle();
		
		closed = new boolean[size_y][size_x];
		map  = new Unit[size_y][size_x];
		slow = new double[size_y][size_x];
		
		//int size = Math.max(this.player.getHealthySize(), this.enemy.getHealthySize());
		int size = this.player.getHealthySize() + this.enemy.getHealthySize();
		size *= 2;
		this.size_x = size;
		this.size_y = size;
		
		this.battle = new Battle(mapScreen.getKingdom(), this.player.army, this.enemy.army);
		
		// try this
		pb = new PanelBattle(mapScreen.getSidePanel(), battle);

		allies = new Array<Unit>();
		enemies = new Array<Unit>();

		
		this.battlemap = new BattleMap(this);
		this.addActor(battlemap);
		
		addUnits();
	}

	public void createFakeBattle() {
//		player = PartyType.MERCHANT.generate();
		player = kingdom.getPlayer().getParty();
		enemy = kingdom.getArmies().random().getParty();
	}

	// for now, put them randomly on the field
	public void addUnits() {
		for (Soldier s : player.getHealthy()) {
			int team = 0;

			int pos_x = (int) (Math.random()*size_x);
			int pos_y = 0;

			while (map[pos_y][pos_x] != null || closed[pos_y][pos_x]) {
				pos_x = (int) (Math.random()*size_x);
				pos_y = (int) (Math.random()*size_y);
			}

			Unit unit = new Unit(this, pos_x, pos_y, team, s, player);
			addUnit(unit);
		}
		for (Soldier s : enemy.getHealthy()) {
			int team = 1;

			int pos_x = (int) (Math.random()*size_x);
			int pos_y = size_y-1;

			while (map[pos_y][pos_x] != null || closed[pos_y][pos_x]) {
				pos_x = (int) (Math.random()*size_x);
				pos_y = (int) (Math.random()*size_y);
			}

			Unit unit = new Unit(this, pos_x, pos_y, team, s, enemy);
			addUnit(unit);
//			System.out.println("Adding enemy unit");
		}
	}

	@Override
	public void act(float delta) {
		if (mouseOver) {
			if (leftClicked) leftClick(mouse);
			else if (rightClicked) rightClick(mouse);
			else if (BesiegeMain.appType != 1) mouseOver(mouse);
		}

		super.act(delta);
		if (allies.size == 0) {
			enemyVictory();
		} 
		else if (enemies.size == 0) {
			playerVictory();
		}
		if (leftClicked) leftClicked = false;
		if (rightClicked) rightClicked = false;
	}

	private void mouseOver(Point mouse) {
		Unit u = getUnitAt(mouse);
		//		if (d.getType() != 0)
		if (u != null) this.setPanelTo(u);
		else if (selectedUnit != null) this.setPanelTo(selectedUnit);
		else {
			currentPanel = null;
			mapScreen.getSidePanel().setActive(pb);
		}
		//			d.setMouseOver(true);
	}

	private void setPanelTo(Unit newPanel) {
		//		if (currentPanel == null) System.out.println("currentPanel is null");
		// makes sure not to set the same panel a lot, and makes sure not to return to previous for every single point
		
		if (newPanel != currentPanel) {
			mapScreen.getSidePanel().setActiveUnit(newPanel);
			this.currentPanel = newPanel;
		}
//		if (newPanel == null)
			
	}

	private void leftClick(Point mouse) {
		Unit u = getUnitAt(mouse);
	}

	private void rightClick(Point mouse) {
		Unit u = getUnitAt(mouse);	
		
		if (u != null) {
			selectedUnit = u;
			setPanelTo(u);
			mouseOver(mouse);
			System.out.println("unit at mouse is " + u.soldier.name);
		}
		else {
			selectedUnit = null;
			currentPanel = null;
		}
	}

	public void click(int pointer) {
		if (pointer == 0)
			leftClicked = true;
		else if (pointer == 1) 
			rightClicked = true;
		else if (pointer == 4)
			writeUnit();
	}
	
	public void playerVictory() {
		System.out.println("Player wins!");
		if (this.mapScreen != null)
			victory(kingdom.getPlayer());
	}
	
	public void enemyVictory() {
		// destroy player TODO
		//System.out.println("Enemy wins!");
	}
	
	public void victory(Army army) {
		army.endBattle();
		army.setStopped(false);
		army.forceWait(Battle.WAIT);
		if (army.getParty().player) {
			kingdom.getMapScreen().getSidePanel().setStay(false);
			kingdom.getMapScreen().getSidePanel().setDefault();
		}
	
		//	log(army.getName() + " has won a battle", "cyan");
		if (!army.isGarrisoned()) army.setVisible(true);
		//army.nextTarget(); // 
		
		if (army.getParty().player) {
//			army.setStopped(true);
			army.setTarget(null);
		}
		
		double contribution = 1;
		
		/* int reward = (int) (contribution*spoils);
		int expReward;
		int moraleReward;
		if (victor == aArmies) {
			expReward = (int) (contribution*expA);
			moraleReward = (int) (initBalanceA*baseMoraleReward);
		}
		else {
			expReward = (int) (contribution*expD);
			moraleReward = (int) (initBalanceD*baseMoraleReward);
		}
		expReward *= EXP_FACTOR; // just to beef it up
		log(victor.get(i).getName() + " receives " + moraleReward + " morale, " + reward + " gold and " + expReward + " experience!", "green");
		victor.get(i).getParty().wealth += reward;
		victor.get(i).getParty().distributeExp(expReward);
		victor.get(i).setMomentum(victor.get(i).getMomentum()+moraleReward);
		
		*/
		
		
		// just make sure no "retreating" soldiers
		for (Unit u : this.allies) {
			if (u.soldier.retreated) u.soldier.party.heal(u.soldier);
			u.soldier.retreated = false;
		}
		for (Unit u : this.enemies) {
			if (u.soldier.retreated) u.soldier.party.heal(u.soldier);
			u.soldier.retreated = false;
		}
		
		mapScreen.switchToKingdomView();
	}

	public void writeUnit() {
		float x = mouse.getCenterX();
		float y = mouse.getCenterY();
		//		mapScreen.out.println(x + " " + y);
	}

	private Unit getUnitAt(Point mouse) {
		Unit close = null;
		for (Unit unit : enemies) {
			if (mouseDistTo(unit) < MOUSE_DISTANCE) {
				close = unit;
			}
		}
		for (Unit unit : allies) {
			if (mouseDistTo(unit) < MOUSE_DISTANCE) {
				close = unit;
			}
		}
		return close;
	}
	
	private double mouseDistTo(Unit unit) {
		float dx = mouse.getX() - unit.getCenterX();
		float dy = mouse.getY() - unit.getCenterY();
		return Math.sqrt(dx*dx + dy*dy);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		//batch.setColor(Color.WHITE);
		//			batch.setColor(kingdom.currentDarkness, kingdom.currentDarkness, kingdom.currentDarkness, 1f);
		super.draw(batch, parentAlpha);

//		System.out.println("bs drawing");
		if (drawCrests) {

		}
	}

	public void addUnit(Unit unit) {
		if (unit.team == 0)
			this.allies.add(unit);
		else if (unit.team == 1)
			this.enemies.add(unit);
		this.addActor(unit);
	}

	public void removeUnit(Unit remove) {
		if (remove.team == 0)
			allies.removeValue(remove, true);
		else enemies.removeValue(remove, true);
		this.removeActor(remove);
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

	public float getZoom() {
		return getMapScreen().getCamera().zoom;
	}
	public void setMouseOver(boolean b) {
		mouseOver = b;
	}
	public boolean isPaused() {
		return paused;
	}
	public static double distBetween(Unit d1, Unit d2) {
		// TODO optimize by computing getCenter only once per
		return Math.sqrt((d1.getCenterX()-d2.getCenterX())*(d1.getCenterX()-d2.getCenterX())+(d1.getCenterY()-d2.getCenterY())*(d1.getCenterY()-d2.getCenterY()));
	}
	// should be slightly faster than above
	public static double sqDistBetween(Destination d1, Destination d2) {
		return (d1.getCenterX()-d2.getCenterX())*(d1.getCenterX()-d2.getCenterX())+(d1.getCenterY()-d2.getCenterY())*(d1.getCenterY()-d2.getCenterY());
	}


}
