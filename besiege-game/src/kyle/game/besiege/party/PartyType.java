/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.party;
import static kyle.game.besiege.party.Weapon.*;
import com.badlogic.gdx.math.MathUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;


public enum PartyType { // todo add ability for max party size
	// troop types, troop min counts, troop max counts, minWealth, maxWealth
	FARMER(new Weapon[]{PITCHFORK, MILITARY_FORK}, new int[]{2, 1}, new int[]{5, 2}, 5, 10),
	PATROL(new Weapon[]{MILITARY_FORK, SPEAR, HATCHET, CLUB, CAVALRY_SPEAR, CAVALRY_AXE}, new int[]{0, 3, 3, 0, 0, 0}, new int[]{3, 6, 5, 3, 2, 2}, 25, 35),
	MERCHANT(new Weapon[]{CAVALRY_SPEAR, CAVALRY_AXE, CAVALRY_PICK}, new int[]{5, 3, 3}, new int[]{6, 4, 4}, 35, 55),
	MILITIA(new Weapon[]{PITCHFORK, MILITARY_FORK, SPEAR, HATCHET, CLUB}, new int[]{15, 10, 0, 0, 0}, new int[]{20, 15, 5, 5, 5}, 0, 0),
	RAIDING_PARTY(new Weapon[]{HATCHET, BATTLE_AXE, WAR_HAMMER, CAVALRY_SPEAR, CAVALRY_AXE, CAVALRY_PICK}, new int[]{0, 0, 0, 5, 5, 5}, new int[]{4, 4, 4, 8, 8, 8}, 35, 50),
	
	BANDIT(new Weapon[]{MILITARY_FORK, HATCHET, CLUB, SPEAR, MACE, SHORTBOW, RECURVE, LONGBOW}, new int[]{0, 2, 2, 0, 0, 0, 0, 0}, new int[]{5, 4, 4, 4, 4, 5, 3, 3}, 5, 25), // btw 4 and 32
	
	NOBLE_TEST(new Weapon[]{HATCHET}, new int[]{40}, new int[]{60}, 25, 25),
	
	// min 40, max 82
	NOBLE_DEFAULT_1(new Weapon[]{PIKE, HALBERD, LONGSWORD, LANCE, ARMING_SWORD, CAVALRY_SPEAR, CAVALRY_AXE, CAVALRY_PICK, MACE, SHORTSWORD, LONGBOW, CROSSBOW, RECURVE}, 
			new int[]{5, 5, 5, 4, 4, 5, 5, 5, 5, 5, 2, 0, 0}, new int[] {8, 8, 8, 5, 5, 8, 8, 8, 12, 12, 15, 5, 5}, 100, 300), // best
			// min 40, max 82w
//	NOBLE_DEFAULT_2(new Weapon[]{PIKE, HALBERD, LONGSWORD, LANCE, ARMING_SWORD, CAVALRY_SPEAR, CAVALRY_AXE, CAVALRY_PICK, MACE, SHORTSWORD}, 
//			new int[]{5, 5, 5, 4, 4, 5, 5, 5, 5, 5}, new int[] {8, 8, 8, 5, 5, 8, 8, 8, 12, 12}, 0, 0),

	CITY_HIRE_1(new Weapon[]{PIKE, HALBERD, LONGSWORD, LANCE, ARMING_SWORD}, new int[]{0, 0, 0, 0, 0}, new int[] {2, 2, 2, 1, 1}, 0, 0), // best
	CITY_HIRE_2(new Weapon[]{CAVALRY_SPEAR, CAVALRY_AXE, CAVALRY_PICK, MACE, SHORTSWORD}, new int[]{0, 0, 0, 0, 0}, new int[] {2, 2, 2, 2, 2}, 0, 0),
	CITY_HIRE_3(new Weapon[]{MILITARY_FORK, SPEAR, HATCHET, CLUB}, new int[]{0, 0, 0, 0}, new int[] {2, 3, 3, 3}, 0, 0), // worst
	
//	CASTLE_HIRE_1(new Weapon[]{PIKE, HALBERD, LONGSWORD, LANCE, ARMING_SWORD}, new int[]{0, 0, 0, 0, 0}, new int[] {2, 2, 2, 1, 1}, 0, 0), // best
//	CASTLE_HIRE_2(new Weapon[]{CAVALRY_SPEAR, CAVALRY_AXE, CAVALRY_PICK, MACE, SHORTSWORD}, new int[]{0, 0, 0, 0, 0}, new int[] {2, 2, 2, 2, 2}, 0, 0),
//	CASTLE_HIRE_3(new Weapon[]{MILITARY_FORK, SPEAR, HATCHET, CLUB}, new int[]{0, 0, 0, 0}, new int[] {2, 3, 3, 3}, 0, 0), // worst
	
	VILLAGE_HIRE_1(new Weapon[]{PITCHFORK, MILITARY_FORK}, new int[]{0, 0}, new int[]{6, 4}, 0, 0),
	VILLAGE_HIRE_2(new Weapon[]{PITCHFORK, MILITARY_FORK}, new int[]{0, 0}, new int[]{6, 0}, 0, 0),
	VILLAGE_HIRE_3(new Weapon[]{PITCHFORK, MILITARY_FORK}, new int[]{0, 0}, new int[]{4, 0}, 0, 0),
	
	CITY_GARR_1(new Weapon[]{SPEAR, PIKE, HALBERD, LONGSWORD}, new int[]{10, 10, 10, 10}, new int[]{20, 20, 20, 20}, 0, 0),
//	CITY_GARR_1(new Weapon[]{SPEAR, PIKE, HALBERD, LONGSWORD}, new int[]{1, 1, 1, 1}, new int[]{20, 20, 20, 20}, 0, 0),
	
	MACE_TEST2(new Weapon[]{LONGBOW, CAVALRY_SPEAR, SHORTSWORD}, new int[]{0, 18, 8}, new int[]{0, 0, 0}, 0, 0),
	MACE_TEST(new Weapon[]{ADV_LONGBOW, CAVALRY_SPEAR, GLAIVE}, new int[]{20, 0, 30}, new int[]{0, 0, 0}, 0, 0);
//	VILLAGE_HIRE_1(new Weapon[]{}, new int[]{}, new int[]{}, 0, 0);

	//RANGED_TEST(new Weapon[]{SHORTBOW, CROSSBOW, RECURVE, LONGBOW}, new int[]{20, 0, 00, 0}, new int[]{20, 0, 00, 00}, 0, 0);
//	RANGED_TEST(new Weapon[]{ADV_CROSSBOW, ADV_RECURVE, ADV_LONGBOW}, new int[]{0, 00, 20}, new int[]{0, 00, 20}, 0, 0);

	
	private final Weapon[] troopTypes;
	private final int[] minCount;
	private final int[] maxCount;
	
	public final int minWealth; // wealth
	public final int maxWealth; // this is also cost
	//private final int maxTroopLevel;
	//private final int minTotal;
	
	private PartyType(Weapon[] troopTypes, int[] minCount, int[] maxCount, int minWealth, int maxWealth) {
		this.troopTypes = troopTypes;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.minWealth = minWealth;
		this.maxWealth = maxWealth;
	}
	
	
	public Party generate() {
		Party party = new Party();
		for (int i = 0; i < troopTypes.length; i++) {
			int randomCount = MathUtils.random(minCount[i], Math.max(minCount[i], maxCount[i]));
			for (int j = 0; j < randomCount; j++)
				party.addSoldier(new Soldier(troopTypes[i], party));
		}
		int randomWealth = MathUtils.random(minWealth, maxWealth);
		party.wealth = randomWealth;
		return party;
	}
	
	
	
//	// create minimal party to test if soldier is clogging memory
//	public Party generate() {
//		Party party = new Party();
//		party.addSoldier(new Soldier(Weapon.BATTLE_AXE, party));
//		int randomWealth = MathUtils.random(minWealth, maxWealth);
//		party.wealth = randomWealth;
//		return party;
//	}
	
	
	public int getMinSize() {
		int total = 0;
		for (int count : minCount) total += count;
		return total;
	}
	public int getMaxSize() {
		int total = 0;
		for (int count : maxCount) total += count;
		return total;
	}
	public int getRandomSize() {
		return MathUtils.random(getMinSize(), getMaxSize());
	}
	public Weapon randomSoldierType() {
		// weighted based on soldier frequencies (in max):
		int index = 0;
		Weapon currentWeapon = troopTypes[0];
		double randomDouble = Math.random() * getMaxSize();
		while (randomDouble > 0) {
			currentWeapon = troopTypes[index];
			randomDouble -= maxCount[index];
			index++;
		}
		return currentWeapon;
	}

}
