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
	public float scale = 1.5f;
	public float MIN_SIZE = 75;
	public float SIZE_FACTOR = .2f; // how much does the size of the parties
									// affect the size of battlefield?
	
	public static boolean drawCrests = true;

	private final float MOUSE_DISTANCE = 15; // distance destination must be
												// from mouse to register

	public Array<Unit> allies;
	public Array<Unit> enemies;

	Party player;
	Party enemy;

	public boolean siegeAttack;
	public boolean siegeDefense;
	
	public boolean closed[][]; // open or closed?
	public double slow[][]; // 0 is normal speed, 1 is very slow.
	public Unit[][] map; // units on map
	public float heights[][]; // ground floor levels

	public int size_x = 128;
	public int size_y = 128;

	private MapScreen mapScreen;
	private Kingdom kingdom;
	// private Party player;
	public BattleMap battlemap;
	
	private boolean playerDefending = false;

	private boolean mouseOver; // is mouse over Battle screen?
	private boolean paused;
	public boolean isOver; // is the battle over?

	public int unit_width = 16;
	public int unit_height = 16;

	public Unit selectedUnit;
	public Unit currentPanel;

	private boolean leftClicked;
	private boolean rightClicked;
	private Point mouse;
	
	private Formation playerFormationChoice;
	private Formation enemyFormationChoice;
	
	private enum Formation {
		LINE, DEFENSIVE_LINE, WEDGE, VEE, FLANKING, SPREAD_LINE
	}

	// take in battle object containing arrays of armies and stuff
	public BattleStage(MapScreen mapScreen, Party player, Party enemy, boolean playerDefending, Location siegeOf) {
		this.mapScreen = mapScreen;
		

		mouse = new Point(0, 0);

		this.player = player;
		this.enemy = enemy;

		this.kingdom = mapScreen.getKingdom();
		
		this.playerDefending = playerDefending;
//		this.playerDefending = false;
		
		boolean forceSiege = true;
		if (siegeOf != null || forceSiege) {
			siegeDefense = playerDefending;
		}
		
		if (player == null)
			createFakeBattle();


		this.player.army.updatePolygon();
		this.biome = this.player.army.containing.biome;
		

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
		map = new Unit[size_y][size_x];
		slow = new double[size_y][size_x];
		heights = new float[size_y][size_x];

		this.battle = new Battle(mapScreen.getKingdom(), this.player.army,
				this.enemy.army);
		battle.calcBalancePlayer();
		

		// try this
		pb = new PanelBattle(mapScreen.getSidePanel(), battle);
		pb.battleStage = this;

		allies = new Array<Unit>();
		enemies = new Array<Unit>();

		this.battlemap = new BattleMap(this);
		this.addActor(battlemap);
		
		if (playerDefending) {
			enemyFormationChoice = Formation.FLANKING;
			playerFormationChoice = Formation.DEFENSIVE_LINE;
		}
		else {
			enemyFormationChoice = Formation.DEFENSIVE_LINE;
			playerFormationChoice = Formation.FLANKING;
		}
		if (siegeDefense) playerFormationChoice = Formation.LINE;

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

		mapScreen.getCamera().translate(new Vector2((this.size_x-13)*this.unit_width/2, (this.size_y-10)*this.unit_height/2));
	}

	// for now, put them randomly on the field
	public void addUnits() {
		
		Array<Soldier> infantry = player.getHealthyInfantry();
		Array<Soldier> cavalry = player.getHealthyCavalry();
		Array<Soldier> archers = player.getHealthyArchers();
		Soldier.SoldierType[][] formation = getFormation(player, playerFormationChoice);
		
		int region_height = formation.length;
		int region_width = formation[0].length;
		
		int player_base_x = size_x/2 - region_width/2;
		int player_base_y = (int)(size_y*.2f);
		
		for (int i = 0; i < region_height; i++) {
			for (int j = 0; j < region_width; j++) {
				if (formation[i][j] != null) {
					Soldier toAdd;
					if (formation[i][j] == Soldier.SoldierType.INFANTRY) toAdd = infantry.pop();
					else if (formation[i][j] == Soldier.SoldierType.ARCHER) toAdd = archers.pop();
					else toAdd = cavalry.pop();
					Unit unit = new Unit(this, player_base_x + j, player_base_y + i, 0, toAdd, player);
					if (playerDefending) unit.stance = Unit.Stance.DEFENSIVE;
					if (siegeDefense) unit.dismount();
					addUnit(unit);
				}
			}
		}

		// next, do same thing but rotate array around 180 degrees
		infantry = enemy.getHealthyInfantry();
		cavalry = enemy.getHealthyCavalry();
		archers = enemy.getHealthyArchers();
		formation = getFormation(enemy, enemyFormationChoice);
		
		formation = flipVertical(formation);
		
		region_height = formation.length;
		region_width = formation[0].length;
		
		int enemy_base_x = size_x/2 - region_width/2;
		int enemy_base_y = size_y - formation.length;
		
		for (int i = 0; i < region_height; i++) {
			for (int j = 0; j < region_width; j++) {
				if (formation[i][j] != null) {
					Soldier toAdd;
					if (formation[i][j] == Soldier.SoldierType.INFANTRY) toAdd = infantry.pop();
					else if (formation[i][j] == Soldier.SoldierType.ARCHER) toAdd = archers.pop();
					else toAdd = cavalry.pop();
					Unit unit = new Unit(this, enemy_base_x + j, enemy_base_y + i, 1, toAdd, enemy);
					if (!playerDefending) unit.stance = Unit.Stance.DEFENSIVE;
					if (siegeDefense) unit.dismount();
					addUnit(unit);
				}
			}
		}
	}
	
	public void retreatAll(boolean player) {
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
	
	public Soldier.SoldierType[][] getFormation(Party party, Formation formationChoice) {
		Soldier.SoldierType[][] formation;
		
		Array<Soldier> infantry = party.getHealthyInfantry();
		Array<Soldier> cavalry = party.getHealthyCavalry();
		Array<Soldier> archers = party.getHealthyArchers();

		int iCount = infantry.size;
		int aCount = archers.size;
		int cCount = cavalry.size;
		
		// figure out what formation to do
		// Line:   CCCC IIII AAAAA IIII CCCC
		if (formationChoice == Formation.LINE) {
			int formation_width = iCount + aCount + cCount;
			int formation_height = 1;
			
			formation = new Soldier.SoldierType[formation_height][formation_width];
			
			int cavalry_left = (int) (cCount/2.0);
			int cavalry_right = cCount - cavalry_left;
			int infantry_right = (int) (iCount/2.0);
			int infantry_left = iCount - infantry_right;
			
			for (int i = 0; i < cavalry_left; i++) {
				formation[0][i] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < infantry_left; i++) {
				formation[0][i + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < aCount; i++) {
				formation[0][i + infantry_left + cavalry_left] = Soldier.SoldierType.ARCHER;
			}
			for (int i = 0; i < infantry_right; i++) {
				formation[0][i + aCount + infantry_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[0][i + infantry_right + aCount + infantry_left + cavalry_left] = Soldier.SoldierType.CAVALRY;
			}
		} 
		// Spread Line:   C C C C I I I I A A A A A I I I I C C C C
		else if (formationChoice == Formation.SPREAD_LINE) {
			int formation_width = (iCount*2-1 + aCount*2-1 + cCount*2-1);
			int formation_height = 1;
			
			formation = new Soldier.SoldierType[formation_height][formation_width];
			
			int cavalry_left = (int) (cCount/2.0)*2;
			int cavalry_right = cCount*2 - cavalry_left;
			int infantry_right = (int) (iCount/2.0)*2;
			int infantry_left = iCount*2 - infantry_right;
			
			boolean skip = false;
			
			for (int i = 0; i < cavalry_left; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[0][i] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < infantry_left; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[0][i + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < aCount; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[0][i + infantry_left + cavalry_left] = Soldier.SoldierType.ARCHER;
			}
			for (int i = 0; i < infantry_right; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[0][i + aCount + infantry_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				skip = !skip;
				if (skip == true) continue;
				formation[0][i + infantry_right + aCount + infantry_left + cavalry_left] = Soldier.SoldierType.CAVALRY;
			}
		} 
		//    Defensive Line
		// CCCCC IIIII CCCCC
		//
		//    AAAAAAAAAAA
		else if (formationChoice == Formation.DEFENSIVE_LINE) {
			
			int top_row = iCount + cCount;
			int bottom_row = aCount;
			
			int bottom_start_left = Math.max(0, top_row - bottom_row)/2;
			int top_start_left = Math.max(0, bottom_row - top_row)/2;
			
			int formation_width = Math.max(top_row, bottom_row);
			int formation_height = 2;
			
			formation = new Soldier.SoldierType[formation_height][formation_width];
			
			
			int cavalry_left = (int) (cCount/2.0);
			int cavalry_right = cCount - cavalry_left;
//			int infantry_right = (int) (iCount/2.0);
//			int infantry_left = iCount - infantry_right;
			
			for (int i = 0; i < cavalry_left; i++) {
				formation[formation_height-1][i + top_start_left] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < infantry.size; i++) {
				formation[formation_height-1][i + top_start_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[formation_height-1][i + top_start_left + infantry.size + cavalry_left] = Soldier.SoldierType.CAVALRY;
			}
			
			
			for (int i = 0; i < archers.size; i++) {
				formation[0][i + bottom_start_left] = Soldier.SoldierType.ARCHER;
			}
		} 
		//   Vee Formation
		// CC            CC
		//   CC	       CC
		//     II    II 
		//       IIII
		//    AAAAAAAAAAA
		// 
		else if (formationChoice == Formation.VEE) {
			
			int top_row = iCount + cCount;
			int bottom_row = aCount;
			
			int archer_separation = 2;
			
			int bottom_start_left = Math.max(0, top_row - bottom_row)/2;
			int top_start_left = Math.max(0, bottom_row - top_row)/2;
			
			int formation_width = Math.max(top_row, bottom_row);
			int formation_height = top_row / 4 + archer_separation;
			
			formation = new Soldier.SoldierType[formation_height][formation_width];
			
			
			int cavalry_left = (int) (cCount/2.0);
			int cavalry_right = cCount - cavalry_left;
			
			int current_height = formation_height - 1;
			int height_change = -1; // either 1 or -1 
			boolean next_row = true;
			
			for (int i = 0; i < cavalry_left; i++) {
				formation[current_height][i + top_start_left] = Soldier.SoldierType.CAVALRY;
				next_row = !next_row;
				if (next_row) {
					current_height += height_change;
					if (current_height < archer_separation) height_change = -height_change;
				}
			}
			for (int i = 0; i < infantry.size; i++) {
				formation[current_height][i + top_start_left + cavalry_left] = Soldier.SoldierType.INFANTRY;
				next_row = !next_row;
				if (next_row) {
					current_height += height_change;
					if (current_height < archer_separation) height_change = -height_change;
				}
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[current_height][i + top_start_left + infantry.size + cavalry_left] = Soldier.SoldierType.CAVALRY;
				next_row = !next_row;
				if (next_row) {
					current_height += height_change;
					if (current_height < archer_separation) height_change = -height_change;
				}
			}
			
			for (int i = 0; i < archers.size; i++) {
				formation[0][i + bottom_start_left] = Soldier.SoldierType.ARCHER;
			}
		} 
		// Flanking formation
		//  CCCCC            IIIIIII              CCCCC
		//                   AAAAAAA
		else if (formationChoice == Formation.FLANKING) {
			int cavalry_separation = size_x/2 - 2*cCount;
			
			int top_row = iCount + cCount + 2*cavalry_separation;
			int bottom_row = aCount;
			
			int bottom_start_left = Math.max(0, top_row - bottom_row)/2;
			int top_start_left = Math.max(0, bottom_row - top_row)/2;
			
			int formation_width = Math.max(top_row, bottom_row);
			int formation_height = 3;
			
			formation = new Soldier.SoldierType[formation_height][formation_width];
			
			
			int cavalry_left = (int) (cCount/2.0);
			int cavalry_right = cCount - cavalry_left;
//			int infantry_right = (int) (iCount/2.0);
//			int infantry_left = iCount - infantry_right;
			
			for (int i = 0; i < cavalry_left; i++) {
				formation[formation_height-1][i + top_start_left] = Soldier.SoldierType.CAVALRY;
			}
			for (int i = 0; i < infantry.size; i++) {
				formation[formation_height-1][i + top_start_left + cavalry_separation + cavalry_left] = Soldier.SoldierType.INFANTRY;
			}
			for (int i = 0; i < cavalry_right; i++) {
				formation[formation_height-1][i + top_start_left + infantry.size + cavalry_left + cavalry_separation*2] = Soldier.SoldierType.CAVALRY;
			}
			
			
			for (int i = 0; i < archers.size; i++) {
				formation[0][i + bottom_start_left] = Soldier.SoldierType.ARCHER;
			}
		} 
		
		
		else formation = null;
		return formation;
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

		super.act(delta);
		if (allies.size == 0) {
			enemyVictory();
		} else if (enemies.size == 0) {
			playerVictory();
		}
		if (leftClicked)
			leftClicked = false;
		if (rightClicked)
			rightClicked = false;
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

	private void leftClick(Point mouse) {
		Unit u = getUnitAt(mouse);
	}

	private void rightClick(Point mouse) {
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
		if (pointer == 0)
			leftClicked = true;
		else if (pointer == 1)
			rightClicked = true;
		else if (pointer == 4)
			writeUnit();
	}

	public void playerVictory() {
		this.isOver = true;
		
//		System.out.println("Player wins!");
//		if (this.mapScreen != null)
//			victory(kingdom.getPlayer());
	}

	public void enemyVictory() {
		this.isOver = true;
		// destroy player TODO
		// System.out.println("Enemy wins!");
	}

	public void victory(Army army) {
		army.endBattle();
		army.setStopped(false);
		army.forceWait(Battle.WAIT);
		if (army.getParty().player) {
			kingdom.getMapScreen().getSidePanel().setStay(false);
			kingdom.getMapScreen().getSidePanel().setDefault();
		}

		// log(army.getName() + " has won a battle", "cyan");
		if (!army.isGarrisoned())
			army.setVisible(true);
		// army.nextTarget(); //

		if (army.getParty().player) {
			// army.setStopped(true);
			army.setTarget(null);
		}

		double contribution = 1;

		/*
		 * int reward = (int) (contribution*spoils); int expReward; int
		 * moraleReward; if (victor == aArmies) { expReward = (int)
		 * (contribution*expA); moraleReward = (int)
		 * (initBalanceA*baseMoraleReward); } else { expReward = (int)
		 * (contribution*expD); moraleReward = (int)
		 * (initBalanceD*baseMoraleReward); } expReward *= EXP_FACTOR; // just
		 * to beef it up log(victor.get(i).getName() + " receives " +
		 * moraleReward + " morale, " + reward + " gold and " + expReward +
		 * " experience!", "green"); victor.get(i).getParty().wealth += reward;
		 * victor.get(i).getParty().distributeExp(expReward);
		 * victor.get(i).setMomentum(victor.get(i).getMomentum()+moraleReward);
		 */

		// just make sure no "retreating" soldiers
		for (Unit u : this.allies) {
			if (u.soldier.retreated)
				u.soldier.party.heal(u.soldier);
			u.soldier.retreated = false;
		}
		for (Unit u : this.enemies) {
			if (u.soldier.retreated)
				u.soldier.party.heal(u.soldier);
			u.soldier.retreated = false;
		}

		mapScreen.switchToKingdomView();
	}

	public void writeUnit() {
		float x = mouse.getCenterX();
		float y = mouse.getCenterY();
		// mapScreen.out.println(x + " " + y);
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
	
	public boolean ladderAt(int pos_x, int pos_y) {
		return battlemap.ladderAt(pos_x, pos_y);
	}
	
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
	}

	public void removeUnit(Unit remove) {
		if (remove.team == 0)
			allies.removeValue(remove, true);
		else
			enemies.removeValue(remove, true);
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
}
