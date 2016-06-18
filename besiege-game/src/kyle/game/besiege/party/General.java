package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.Color;

import kyle.game.besiege.battle.BattleSubParty;

public class General extends Soldier {
	private final static float BONUS_MAX = 2;
	private final static float BONUS_MIN = -1;

	private final static float RANGE_MAX = 5;
	private final static float RANGE_MIN = -2;
	
	// fill this in later
	public String epithet; 	// "great", "hammer", etc 
	
	public General rival;
	
	public int fame;
	
	public int courage;  // increases morale in battle, but also mutiny chance
	public int preparation; // increases troop health in battle
	public int loyalty; 	// affects chance of desertion, mutiny
	
	public int morality;
	
	public int infantryAttack;
	public int infantryDefense;
	
	public int rangedCommand;

	// for Kryo
	public General() {
	}
	
	public General(UnitType unitType, Party party) {
		super(unitType, party);

		this.isImportant = true;
		
		this.subparty = party.root;
		
		generateRandomInitStats();
	}
	
	// for promoting a soldier to a general
	public General(Soldier s) {
		super(s); // copy constructor
		this.isImportant = true;
		generateRandomInitStats();
	}
	
	
	
	// all stats are between 0 and 100
	// they start out between 20 and 80, and can change later.
	public void generateRandomInitStats() {
		courage = getRandomInit();
		preparation = getRandomInit();
		loyalty = getRandomInit();
		
		morality = getRandomInit();
		
		infantryAttack = getRandomInit();
		infantryDefense = getRandomInit();
		rangedCommand = getRandomInit();
	}
	
	private int getRandomInit() {
		return (int) (Math.random() * 60 + 20);
	}
	
	public float getBonusGeneralAtk() {
		return (BONUS_MAX - BONUS_MIN) * infantryAttack/100.0f + BONUS_MIN;
	}
	
	public float getBonusGeneralDef() {
		return (BONUS_MAX - BONUS_MIN) * infantryDefense/100.0f + BONUS_MIN;		
	}
	
	public float getBonusRangedAtk() {
		return (BONUS_MAX - BONUS_MIN) * rangedCommand/100.0f + BONUS_MIN;
	}
	
	public float getBonusGeneralRange() {
		return (RANGE_MAX - RANGE_MIN) * infantryAttack/100.0f + RANGE_MIN;
	}
	
	public float getHPBonus() {		
		return (BONUS_MAX - BONUS_MIN) * preparation/100.0f + BONUS_MIN;
	}
	
	public float getMoraleBonus() {
		return courage/100.0f * (1-BattleSubParty.BASE_MORALE - (1-BattleSubParty.MAX_MORALE));
	}
	
	public static Color getColor(int value) {
		if (value < 50) {
			// interpolate between white and red
			return new Color(1, value*1f/50, value/50, 1);
		}
		else {
			// interpolate between white and green
			return new Color((100f - value)/50, 1, (100f-value)/50, 1);			
		}
		// 51 should be close to 1, 
	}
	
	@Override
	public boolean isGeneral() {
		return true;
	}
	
	public String getRank() {
		if (this.subparty.getRank() == 0) {
			return "General";
		}
		if (this.subparty.getRank() == 1) {
			return "Lietenant";
		}
		if (this.subparty.getRank() == 2) {
			return "Platoon Leader";
		}
		return "None";
	}
	
	public void setName(String name) {
		System.out.println("Setting name: " + name);
		if (name != null && name.length() > 0)
			this.name = name;
	}
}
