package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.MapScreen;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.party.General;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Soldier.SoldierType;
import kyle.game.besiege.party.Subparty;


// make BattleSubParty - for controlling individual parties.
public class BattleSubParty {
	public static float BASE_MORALE = 0.4f;
	public static float MAX_MORALE = 0.9f;
	
	Subparty subparty;
	BattleParty parent;
	BattleStage stage;

	public Array<Formation> availableFormations;

	// for placement
	public StrictArray<Unit> infantry, cavalry, archers;
	public Unit general;
	
	public StrictArray<Unit> units;
	
	public StrictArray<Unit> retreatingUnits;

	public int startingCount;
	public int currentCount;
	public int killCount; 
	private float currentMorale;
	private String currentMoraleString;
	public Color moraleColor;
	public boolean generalOut;
	
	public Stance stance;
	public Formation formation;
	public float minSpeed;
	public boolean retreating;
	public boolean retreated;
	
	public float battleMoraleThreshold; // between 0 and 1. IF troop ratio drops below (1-currentMorale), will retreat.
	
	public int currentPosX;
	public int currentPosY;
	public int currentRegWidth;
	public int currentRegHeight;
	
	int team;
	
	TextureRegion white;
	Color c;
	
	boolean charging;

	public BattleSubParty(BattleParty parent, Subparty subparty, int team) {
		this.subparty = subparty;
		this.parent = parent;
		this.units = new StrictArray<Unit>();
		this.stage = parent.stage;
		this.team = team;

		this.availableFormations = MapScreen.characterReference.availableFormations;
		
		startingCount = subparty.getHealthySize() + 1;
		currentCount = startingCount;
		
		battleMoraleThreshold = BASE_MORALE + subparty.general.getMoraleBonus();
		
		archers = new StrictArray<Unit>();
		cavalry = new StrictArray<Unit>();
		infantry = new StrictArray<Unit>();
		retreatingUnits = new StrictArray<Unit>();
		
		white = new TextureRegion(new Texture("whitepixel.png"));
		c = new Color();
		
		createAllUnits();
		updateCurrentMorale();
	}
	
	public void createAllUnits() {
		for (Soldier s : subparty.healthy) {
			Unit unit = new Unit(stage, team, s, this);
			this.units.add(unit);
			this.parent.units.add(unit);
			unit.setStance(stance);
		}
		addUnitsToArrays();

		this.general = new Unit(stage, team, subparty.general, this);
		this.units.add(general);
		this.parent.units.add(general);
		general.setStance(stance);
		
		this.calcMinSpeed();
	}
	
	public void addUnitsToArrays() {
		for (Unit unit : units) {
			if (unit.isGeneral()) continue;
			if (unit.soldier.getType() == SoldierType.ARCHER) archers.add(unit);
			if (unit.soldier.getType() == SoldierType.CAVALRY) cavalry.add(unit);
			if (unit.soldier.getType() == SoldierType.INFANTRY) infantry.add(unit);
		}
	}

	public void removeUnit(Unit remove, boolean dying) {
		units.removeValue(remove, true);
		parent.units.removeValue(remove, true);
		if (remove.inMap())
			if (stage.units[remove.pos_y][remove.pos_x] == remove) stage.units[remove.pos_y][remove.pos_x] = null;
		
		if (!dying) stage.removeActor(remove);
		calcMinSpeed();
	}
	
	public boolean noUnits() {
		return this.units.size == 0 && generalOut;
	}
	
	public void handleKilledEnemy() {
		this.killCount++;
		updateCurrentMorale();
	}
	
	// decide if should retreat
	public void handleUnitKilled(Unit unit) {
//		System.out.println("handle unit killed");
		if (unit.isGeneral()) {
			battleMoraleThreshold -= 0.2f;
			this.generalOut = true;
		}
		this.currentCount--;
		updateCurrentMorale();
		retreatIfNecessary();
	}
	
	public void handleUnitRetreating(Unit unit) {
		if (this.retreatingUnits.contains(unit, true)) return;
		retreatingUnits.add(unit);
		
		if (unit.isGeneral()) {
			battleMoraleThreshold -= 0.2f;
			this.generalOut = true;
		}
		this.currentCount--;
		updateCurrentMorale();
		retreatIfNecessary();		
	}
	
	// multiply by courage of general
	public void retreatIfNecessary() {
		if (currentMorale < 0) retreat();
	}
	
	public void updateCurrentMorale() {
		currentMorale = (currentCount * 1.0f + killCount) / startingCount - (1-battleMoraleThreshold);
		updateMoraleString();
		updateMoraleColor();
	}
	
	public void updateMoraleString() {
		if (currentMorale < 0) {
			currentMoraleString = "Defeated";
		}
		else if (currentMorale < 0.25) {
			currentMoraleString = "Terrified";			
		}
		else if (currentMorale < 0.5) {
			currentMoraleString = "Nervous";
		}
		else if (currentMorale < 0.75) {
			currentMoraleString = "Ready";
		}
		else if (currentMorale < 1) {
			currentMoraleString = "Excited";
		}
		else {
			currentMoraleString = "Inspired";
		}
	}
	
	public void updateMoraleColor() {
//		if (team == 0) System.out.println(currentMorale);
		this.moraleColor = General.getColor((int) (Math.min(99, Math.max(1, 100 * currentMorale))));
	}
	
	public String getCurrentMoraleString() {
		return currentMoraleString;
	}

	public int getHealthySize() {
		return subparty.getHealthySize();
	}

	public StrictArray<Soldier> getHealthyInfantry() {
		return subparty.getHealthyInfantry();
	}
	//
	public StrictArray<Soldier> getHealthyArchers() {
		//		StrictArray<Soldier> healthyArchers = new StrictArray<Soldier>();
		//		for (Party p : parties) {
		//			healthyArchers.addAll(p.root.getHealthyArchers());
		//		}
		//		return healthyArchers;
		return subparty.getHealthyArchers();
	}

	public StrictArray<Soldier> getHealthyCavalry() {
		//		StrictArray<Soldier> healthyCavalry = new StrictArray<Soldier>();
		//		for (Party p : parties) {
		//			healthyCavalry.addAll(p.root.getHealthyCavalry());
		//		}
		//		return healthyCavalry;
		return subparty.getHealthyCavalry();
	}
	
	public void drawPlacement(SpriteBatch batch) {
		c.set(batch.getColor());
		batch.setColor(1, 1, 1, 0.5f);
		batch.draw(white, stage.unit_width * currentPosX, stage.unit_height * currentPosY, stage.unit_width * currentRegWidth, stage.unit_height * currentRegHeight);
		batch.setColor(c);
	}

	public void calcMinSpeed() {
		float min = Float.MAX_VALUE;
		for (Unit unit : units) {
			if (unit.retreating) continue;
			if (unit.spd < min) min = unit.spd;
		}
		this.minSpeed = min;
	}

	public void updateHiddenAll() {
		for (Unit unit : units) unit.updateHidden();
		//		System.out.println("updating hidden");
	}

	public boolean isPlayer() {
		return parent.player;
	}
	
	public void updateFormationLocation() {
		// calculate 
		//		if (this.originalPlacePoint.pos_x == this.mou)
		BPoint mousePoint = stage.mouseToPoint();
		if (stage.prevMouse != null && mousePoint.pos_x == stage.prevMouse.pos_x && mousePoint.pos_y == stage.prevMouse.pos_y) {
			return;
		}
		//		if (!inMap(mousePoint)) return;

//		if (!stage.dragging) {
//			// just pretend that it hasn't been touched
////			centerOffset = new BPoint(0, 0);
//			mousePoint = placementPoint;
//			System.out.println("null center");
//		}

//		if (this.currentFormation == allies.formation && mousePoint.pos_x == this.placementPoint.pos_x - centerOffset.pos_x && mousePoint.pos_y == this.placementPoint.pos_y - centerOffset.pos_y) return;
//		mousePoint = centerInPlacementRegion(mousePoint);
		int newX = mousePoint.pos_x - currentRegWidth/2;
		int newY = mousePoint.pos_y - currentRegHeight/2;
		
		if (!canPlaceAt(newX, newY)) {
			System.out.println("can't place there");
			return;
		}
		
		stage.prevMouse = mousePoint;
		
		this.currentPosX = newX;
		this.currentPosY = newY;
//		System.out.println("currentPosX: " + currentPosX + " currentPosY: " + currentPosY);
		this.updateFormation();
//		System.out.println("Update formation location");


		//		this.removeParty(player);
//		this.placementPoint = new BPoint(mousePoint.pos_x + centerOffset.pos_x, mousePoint.pos_y + centerOffset.pos_y);
		//		this.addParty(player);
	}
	
	public boolean canPlaceAt(int x, int y) {
		boolean xWorks = true;
		boolean yWorks = true;
		boolean shouldUpdate = false;
		if (x + currentRegWidth > stage.MAX_PLACE_X) {
			if (currentPosX != stage.MAX_PLACE_X - currentRegWidth) {
				currentPosX = stage.MAX_PLACE_X - currentRegWidth;
				shouldUpdate = true;
			}
			xWorks = false;
		}
		if (x < stage.MIN_PLACE_X) {
			if (currentPosX != stage.MAX_PLACE_X) { 
				currentPosX = stage.MIN_PLACE_X;
				shouldUpdate = true;
			}
			xWorks = false;
		}
		if (xWorks) {
			currentPosX = x;
			shouldUpdate = true;
		}
			
		if (this.team == 0 && y + currentRegHeight > stage.MAX_PLACE_Y_1) {
			if (currentPosY != stage.MAX_PLACE_Y_1 - currentRegHeight) {
				currentPosY = stage.MAX_PLACE_Y_1 - currentRegHeight;
				shouldUpdate = true;
			}
			yWorks = false;
		}
		if (this.team == 1 && y + currentRegHeight > stage.MAX_PLACE_Y_2) {
			if (currentPosY != stage.MAX_PLACE_Y_2 - currentRegHeight) {
				currentPosY = stage.MAX_PLACE_Y_2 - currentRegHeight;
				shouldUpdate = true;
			}
			yWorks = false;
		}
		
		if (this.team == 0 && y < stage.MIN_PLACE_Y_1) {
			if (currentPosY != stage.MAX_PLACE_Y_1) { 
				currentPosY = stage.MIN_PLACE_Y_1;
				shouldUpdate = true;
			}
			yWorks = false;
		}
		if (this.team == 1 && y < stage.MIN_PLACE_Y_2) {
			if (currentPosY != stage.MAX_PLACE_Y_2) { 
				currentPosY = stage.MIN_PLACE_Y_2;
				shouldUpdate = true;
			}
			yWorks = false;
		}
		if (yWorks) {
			currentPosY = y;
			shouldUpdate = true;
		}
		

		if (shouldUpdate && (!xWorks || !yWorks)) updateFormation();
		return xWorks && yWorks;
	}

	private BPoint centerInPlacementRegion(BPoint mousePoint) {
		BPoint center = new BPoint(mousePoint.pos_x + stage.centerOffset.pos_x, mousePoint.pos_y + stage.centerOffset.pos_y);

//		if (center.pos_x - currentRegWidth/2 < stage.MIN_PLACE_X)
//			mousePoint.pos_x = stage.MIN_PLACE_X - stage.centerOffset.pos_x + currentRegWidth/2;
//
//		if (center.pos_y - currentRegHeight/2 < stage.MIN_PLACE_Y)
//			mousePoint.pos_y = stage.MIN_PLACE_Y - stage.centerOffset.pos_y + currentRegHeight/2;
//
//		if (center.pos_x + currentRegWidth/2 >= stage.MAX_PLACE_X)
//			mousePoint.pos_x = stage.MAX_PLACE_X - 1 - stage.centerOffset.pos_x - currentRegWidth/2;
//
//		if (center.pos_y + currentRegHeight/2 >= stage.MAX_PLACE_Y) 
//			mousePoint.pos_y = stage.MAX_PLACE_Y - 1 - stage.centerOffset.pos_y - currentRegHeight/2;

		//		System.out.println(center.pos_x + " " + center.pos_y);

		// set position

		return mousePoint;
	}

	public void toNextFormation() {
		this.formation = getNextFormation();
		System.out.println("To next formation");
		updateFormation();
	}

	public Formation getNextFormation() {
		int index = availableFormations.indexOf(this.formation, true);
		index++;
		if (index >= availableFormations.size) index = 0;
		return availableFormations.get(index);
	}

	
	// TODO for efficiency, don't remove it fully, just remove it from the stage and change positions. Keep the guys alive in units array here.
	// create three arrays of UNITS, inf, cav, archers, and keep them alive for redistribution.
	public void updateFormation() {
		stage.removeSubParty(this);
		stage.addSubparty(this);
	}
	
	public void clearAllUnitsFromStage() {
		addUnitsToArrays();
	}

	public void toggleStance() {
		if (charging) return; // don't allow toggling once charging.
		if (stance == Stance.AGGRESSIVE) this.setStance(Stance.DEFENSIVE);
		else if (stance == Stance.DEFENSIVE) this.setStance(Stance.INLINE);
		else if (stance == Stance.INLINE) this.setStance(Stance.AGGRESSIVE);
	}
	
	public void charge() {
		this.setStance(Stance.AGGRESSIVE);
		this.charging = true;
	}
	
	public void retreat() {
		this.retreating = true;
		for (Unit s : units) {
			s.retreating = true;
		}
	}
	
	public void setStance(Stance s) {
		this.stance = s;
		for (Unit u : units) {
			u.setStance(s);
		}
		general.setStance(s);
	}

}
