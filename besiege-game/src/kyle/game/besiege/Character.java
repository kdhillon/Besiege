/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.battle.Formation;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.Soldier;
import kyle.game.besiege.party.UnitType;

public class Character {
	private static final int STARTING_PTS = 5;
	private final int POINTS_PER_LEVEL = 1;
	private final int BASE_TROOPS = 20;
	private final double FAME_FACTOR = .1;
	public String name;

	public Inventory inventory;

	public int rank;

	public int honor;
	public int fame;
	public int trainingExp;

	//	public int maxTroops;

	// Player has:
	// name
	// rank/title
	// 		gained by: fighting for a faction and maintaining high honor and fame
	//		lost by: leaving a faction, low honor
	//		allows: increases max holdings (additional tax)
	// honor (karma -- determines faction trust, allows hiring of better soldiers, prevents rebellions, can increase or decrease rank)
	// 		number between 0 and 100?
	// 		gained by:	fighting enemies stronger than you (winning or losing)
	//					releasing enemy nobles
	//					defeating bandits and defending villages from raids
	//					low tax rates
	//		lost by: 	fighting battles against much smaller non-noble forces
	// 					executing enemy nobles
	//					raiding villages
	// 					high tax rates
	// fame  (basically exp  -- allows a larger party and grants you )
	// 		gained by:	winning battles (bigger the enemy, the better)
	//		lost by: 	nothing
	// holdings (cities, castles, villages):
	// 		gained by:	besieging cities and castles, and having high enough rank
	// 		lost by: 	losing them to enemies
	//		allows: 	collecting taxes, training specialized soldiers,

	public Array<Formation> availableFormations;

	public Array<Location> holdings;

	public int availablePoints; // for attributes

	public static final Array<String> attributeNames = new Array<String>(new String[] {
			"Training",       // Increases exp gained by party by 10%
			"Bargaining",     // Decreases equipment upgrade cost by 5%
			"Commanding",     // Increases base party size by 5%
			"Recruiting",     // Decreases recruitment cost by 5%
			"Inspiring",      // Increases base party morale by 5%
			"Spotting",       // Increases line of sight by 5%
			"Marching",       // Increases party map movement speed by 2%
			"Tracking",       // Increases party speed when chasing enemies by 4%
			"Defending",      // Decreases chance to suffer casualties in battle by 4%
			"Attacking",      // Increases chance to inflict casualties in battle by 4% 
			"Retreating",     // Increases speed of retreating in battle by 10%"
			"Healing",		  // Increases speed that soldiers recover from injuries by 10%
			"Reviving"		  // Decreases chance soldiers will be fatally injured in battle by 5%
	});

	public static final String[] arrDescriptionsBase = {
			"Increases exp gained by party by ",
			"Decreases equipment upgrade cost by ",
			"Increases base party size by ",
			"Decreases recruitment cost by ",
			"Increases base party morale by ",
			"Increases party line of sight by ",
			"Increases party map movement speed by ",
			"Increases party speed when chasing enemies by ",
			"Decreases chance to suffer casualties in battle by ",
			"Increases chance to inflict casualties in battle by ",
			"Increases speed of retreating by ",
			"Increases speed at which soldiers recover from injuries by ",
			"Decreases chance soldiers will be fatally injured in battle by "
	};

	public static final int[] arrPercentages = {
			10, -5, 5, 5, 5, 5, 2, 4, 4, 4, 10, 10, 5
	};

	public Array<Integer> attributeValues;

	public Character(String name) {
		this.name = name;
		System.out.println("Character name: " + name);
		//		this.title = "None";
		this.fame = 0;
		this.addAvailablePoints(5);
		this.trainingExp = 10;
		//		this.trainingExp = 20000;

		if (Soldier.WEAPON_NEEDED)
			this.inventory = new Inventory();

		this.availableFormations = new Array<Formation>();
		this.availableFormations.add(Formation.LINE);
		this.availableFormations.add(Formation.DEFENSIVE_LINE);
		this.availableFormations.add(Formation.FLANKING);
		//		this.availableFormations.add(Formation.SCRAMBLE);
		this.availableFormations.add(Formation.VEE);
		//		this.availableFormations.add(Formation.SPREAD_LINE);
		this.availableFormations.add(Formation.SQUARE);

		initAttributes();
	}

	public Character() {

	}

	public void initAttributes() {	
		availablePoints = STARTING_PTS;
		attributeValues = new Array<Integer>();
		for (int i = 0; i < attributeNames.size; i++) 
			attributeValues.add(0);
	}

	public String getDescription(int i) {
		return arrDescriptionsBase[i] + (int) Math.abs(arrPercentages[i]) + "%";
	}

	public String getDescription(CharSequence name) {
		return getDescription(attributeNames.indexOf(name.toString(), false));
	}

	public int getAttributeLevel(String name) {
		return attributeValues.get(attributeNames.indexOf(name, false));
	}
	public double getAttributeFactor(String name) {
		int index = attributeNames.indexOf(name, false);
		return getAttributeFactor(index);
	}
	public double getAttributeFactor(int index) {
		int percent = arrPercentages[index];
		return Math.pow((percent/100.0) + 1, attributeValues.get(index));
	}
	public void increaseAttribute(String name) {
		int index = attributeNames.indexOf(name, false);
		increaseAttribute(index);
	}
	public void increaseAttribute(int index) {
		if (availablePoints > 0) {
			attributeValues.set(index, attributeValues.get(index) + 1);
			availablePoints--;
			BottomPanel.log(attributeNames.get(index) + " increased to " + attributeValues.get(index), "green");
			//			BottomPanel.log(attributeNames.get(index) + " increased to " + attributeValues.get(index) + " (" + String.format("%.2f", getAttributeFactor(index)) + ")", "green");
		}
		else BottomPanel.log("No points available!", "red");
	}

	public String getTitle() {
		return "scum";
	}

	public void addFame(int fame) {
		this.fame += fame;
	}
	public int getFame() {
		return fame;
	}
	public String getHonor() {
		return ""+honor;
	}
	public void addAvailablePoints(int additional) {
		availablePoints += additional;
		//		BottomPanel.log(availablePoints + " points available to spend", "green");
	}

	public int getMaxTroops() {
		return BASE_TROOPS + (int) (this.fame*FAME_FACTOR);
	}
	
	public boolean canEquip(UnitType type) {
		if (!Soldier.WEAPON_NEEDED) return true;
		return inventory.canEquip(type);
	}
}
