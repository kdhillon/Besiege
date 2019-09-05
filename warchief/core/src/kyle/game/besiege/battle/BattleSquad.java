package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.MapScreen;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.Unit.Stance;
import kyle.game.besiege.party.General;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.Soldier.SoldierType;
import kyle.game.besiege.party.Squad;


// make BattleSquad - for controlling individual parties.
public class BattleSquad {
	public static float BASE_MORALE = 0.4f;
	public static float MAX_MORALE = 0.9f;
	
	public Squad squad;
	BattleParty parent;
	BattleStage stage;

	public Array<Formation> availableFormations;

	// for placement
	private StrictArray<Unit> infantry, cavalry, archers;
	public Unit general;

	public Unit shaman;
	
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

	public float battleMoraleThreshold; // between 0 and 1. IF troop ratio drops below (1-currentMorale), will retreat.
	
	public int currentPosX;
	public int currentPosY;
	public int currentRegWidth;
	public int currentRegHeight;
	
	int team;

	boolean revealed;
	
	TextureRegion white;
	Color c;
	
	boolean charging;

	public BattleSquad(BattleParty parent, Squad squad, int team) {
		this.squad = squad;
		this.parent = parent;
		this.units = new StrictArray<Unit>();
		this.stage = parent.stage;
		this.team = team;

		this.availableFormations = MapScreen.characterReference.availableFormations;
		if (this.availableFormations == null) {
		    this.availableFormations = new Array<Formation>();
		    this.availableFormations.add(Formation.DEFENSIVE_LINE);
		    this.availableFormations.add(Formation.SQUARE);
		    this.availableFormations.add(Formation.SCRAMBLE);
        }
        // this isn't ideal, should be changed.
        this.formation = availableFormations.random();

		startingCount = squad.getHealthySize() + 1;
		currentCount = startingCount;

		if (squad.getGeneral() == null) {
		    System.out.println("Squad of " + parent.parties.first().getName() + " is null");
        }
		else battleMoraleThreshold = BASE_MORALE + squad.getGeneral().getMoraleBonus();
		
		archers = new StrictArray<Unit>();
		cavalry = new StrictArray<Unit>();
		infantry = new StrictArray<Unit>();
		retreatingUnits = new StrictArray<Unit>();
		
		white = new TextureRegion(new Texture("whitepixel.png"));
		c = new Color();


		if (squad.getGeneral() == null) {
		    System.out.println("General is null");
		    generalOut = true;
        }
		
		createAllUnits();
		updateCurrentMorale();
	}
	
	public void createAllUnits() {
	    int count = 0;
		for (Soldier s : squad.healthy) {
		    if (s.isGeneral()) continue;

			Unit unit = new Unit(stage, team, s, this);
			this.units.add(unit);
			this.parent.units.add(unit);
			count++;
			unit.setStance(stance);
		}
		addUnitsToArrays();

		if (squad.getGeneral() == null) {
		    System.out.println("A party that isn't supposed to fight (hire party?) is fighting...");
		    throw new AssertionError();
        }
		this.general = new Unit(stage, team, squad.getGeneral(), this);
        if (team == 1) System.out.println("Enemy general being added");

		this.units.add(general);
		this.parent.units.add(general); // is this necessary?

		general.setStance(stance);

		if (squad.shaman != null) {
			this.shaman = new Unit(stage, team, squad.shaman, this);
			this.units.add(shaman);
			this.parent.units.add(shaman);
			shaman.setStance(stance);
		}
		
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
        System.out.println("removing unit. units: " + units.size);
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
	    if (!parent.canRetreat()) return;

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
			currentMoraleString = "Steady";
		}
		else if (currentMorale < 1) {
			currentMoraleString = "Eager";
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
		if (getHealthySize() == 0) return "Defeated";
		return currentMoraleString;
	}

	public String getStanceString() {
		if (getHealthySize() == 0) return "";
		if (retreating) return "Retreating";
		return stance.toString();
	}

	public int getHealthySize() {
		return squad.getHealthySize();
	}

	public StrictArray<Soldier> getHealthyInfantry() {
		return squad.getHealthyInfantry();
	}
	public double getHealthyLevelSum() {
		return squad.getHealthyLevelSum();
	}
	//
    public boolean isRoot() {
	    return squad.getRank() == 0;
    }

	public StrictArray<Soldier> getHealthyArchers() {
		//		StrictArray<Soldier> healthyArchers = new StrictArray<Soldier>();
		//		for (Party p : parties) {
		//			healthyArchers.addAll(p.root.getHealthyArchers());
		//		}
		//		return healthyArchers;
		return squad.getHealthyArchers();
	}

	public StrictArray<Soldier> getHealthyCavalry() {
		//		StrictArray<Soldier> healthyCavalry = new StrictArray<Soldier>();
		//		for (Party p : parties) {
		//			healthyCavalry.addAll(p.root.getHealthyCavalry());
		//		}
		//		return healthyCavalry;
		return squad.getHealthyCavalry();
	}
	
	public void drawPlacement(Batch batch) {
		c.set(batch.getColor());
		batch.setColor(1, 1, 1, 0.5f);
		batch.draw(white, stage.unit_width * currentPosX, stage.unit_height * currentPosY, stage.unit_width * currentRegWidth, stage.unit_height * currentRegHeight);
		batch.setColor(c);
	}

	public void calcMinSpeed() {
		float min = Float.MAX_VALUE;
		for (Unit unit : units) {
			if (unit.isRetreating()) continue;
			if (unit.spd < min) min = unit.spd;
		}
		this.minSpeed = min;
	}

	public void updateHiddenAll() {
		for (int i = 0; i < units.size; i++) {
			Unit unit = units.get(i);
			unit.updateHidden();
		}
		if (stance != Stance.DEFENSIVE) revealAll();
		//		System.out.println("updating hidden");
	}

	public void revealAll() {
		for (Unit unit : units) {
			unit.reveal();
		}
		revealed = true;
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

	public void tryPlaceSquad() {
		int MAX_INDEX = 300;
		int x = currentPosX;
		int y = currentPosY;

		// TODO does this work for big parties?
		if (stage.hasWall()) {
			if (stage.playerDefending()) {
				if (this.isPlayer()) y = stage.battlemap.wallTop - 2;
			} else {
				if (!this.isPlayer()) y = stage.battlemap.wallBottom + 2;
			}
		}

		int index = 0;
		while (!stage.addUnitsFromSquad(this, x, y) && index < MAX_INDEX) {
//			throw new AssertionError();
//			System.out.println("Moving right: " + index);
			index++;
			if (index % 2 == 0) {
				x = currentPosX + -index / 2 - 1;
			} else {
				x = currentPosX + index / 2 + 1;
			}
			System.out.println("x is " + x);
			stage.removeSquad(this);
		}
		if (index == MAX_INDEX) throw new AssertionError("couldn't place bsp, not enough tries");
	}

	// TODO for efficiency, don't remove it fully, just remove it from the stage and change positions. Keep the guys alive in units array here.
	// create three arrays of UNITS, inf, cav, archers, and keep them alive for redistribution.
	public void updateFormation() {
		stage.removeSquad(this);

		tryPlaceSquad();
//		stage.add(this, this.parent.subparties.indexOf(this, true));
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

	public int infantrySizeWithoutGeneral() {
	    int size = infantry.size;
	    if (infantry.contains(general, true)) size--;
	    return size;
    }

    public int archersSizeWithoutGeneral() {
        int size = archers.size;
        if (archers.contains(general, true)) size--;
        return size;
    }

    public int cavalrySizeWithoutGeneral() {
        int size = cavalry.size;
        if (cavalry.contains(general, true)) size--;
        return size;
    }

    // Need to check if can retreat first
	public void retreat() {
		this.retreating = true;
		for (Unit s : units) {
			s.startRetreating();
		}
	}
	
	public void setStance(Stance s) {
		this.stance = s;
		for (Unit u : units) {
			u.setStance(s);
		}
		general.setStance(s);
		this.parent.setUpdated();
	}

	public Unit getUnit(Soldier soldier) {
		System.out.println("lookin for " + soldier.getName());

		for (Unit s : units) {
			System.out.println(s.getName());
			if (soldier == s.soldier) return s;
		}
		return null;
	}

    public StrictArray<Unit> getInfantry() {
        return infantry;
    }

    public StrictArray<Unit> getArchers() {
        return archers;
    }

    public StrictArray<Unit> getCavalry() {
        return cavalry;
    }

    public boolean isRevealed() {
		return revealed;
	}
}
