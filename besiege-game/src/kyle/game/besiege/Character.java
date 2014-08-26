/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import kyle.game.besiege.battle.Formation;
import kyle.game.besiege.panels.BottomPanel;

import com.badlogic.gdx.utils.Array;

public class Character {
	private static final int STARTING_PTS = 5;
	private final int POINTS_PER_LEVEL = 1;
	public String name;
	public int exp;
	public int level;
	public int nextLevel;
	public String title;
	public int fame;
	public int maxTroops;
	public Array<Formation> availableFormations;
	
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
		this.level = 0;
		this.exp = 0;
		this.nextLevel = 100; // change
		this.title = "None";
		this.fame = 0;
		this.addAvailablePoints(5);
		this.availableFormations = new Array<Formation>();
		this.availableFormations.add(Formation.LINE);
		this.availableFormations.add(Formation.DEFENSIVE_LINE);
		this.availableFormations.add(Formation.FLANKING);
		this.availableFormations.add(Formation.VEE);
		
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
	
	public void addExp(int exp) {
		this.exp += exp;
		if (exp >= nextLevel)
			levelUp();
		BottomPanel.log(exp + " exp gained!", "yellow");
	}
	
	public void levelUp() {
		level++;
		this.addAvailablePoints(POINTS_PER_LEVEL);
		BottomPanel.log("Level Up!", "green");
	}
	public String getTitle() {
		return title;
	}
	public void addAvailablePoints(int additional) {
		availablePoints += additional;
//		BottomPanel.log(availablePoints + " points available to spend", "green");
	}
	
}
