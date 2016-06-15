package kyle.game.besiege.party;

public class General extends Soldier {
	private final static float BONUS_MAX = 2;
	private final static float BONUS_MIN = -1;

	private final static float RANGE_MAX = 5;
	private final static float RANGE_MIN = -2;

	public String rank;
	
	// fill this in later
	public String epithet; 	// "great", "hammer", etc 
	
	public General rival;
	
	public int fame;
	
	public int courage;  // increases morale in battle, but also mutiny chance
	public int preparation; // increases troop health in battle
	public int loyalty; 	// affects chance of desertion, mutiny
	
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
		
		rank = "Commander";
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
		
		infantryAttack = getRandomInit();
		infantryDefense = getRandomInit();
		rangedCommand = getRandomInit();
	}
	
	private int getRandomInit() {
		return (int) (Math.random() * 60 + 20);
	}
	
	public float getBonusAtk() {
		return (BONUS_MAX - BONUS_MIN) * infantryAttack/100.0f + BONUS_MIN;
	}
	
	public float getBonusDef() {
		return (BONUS_MAX - BONUS_MIN) * infantryDefense/100.0f + BONUS_MIN;		
	}
	
	public float getBonusRangedAtk() {
		return (BONUS_MAX - BONUS_MIN) * rangedCommand/100.0f + BONUS_MIN;
	}
	
	public float getBonusRange() {
		return (RANGE_MAX - RANGE_MIN) * infantryAttack/100.0f + RANGE_MIN;
	}
	
	public float getHPBonus() {
		return (BONUS_MAX - BONUS_MIN) * preparation/100.0f + BONUS_MIN;
	}
	
	public float getMoraleBonus() {
		return (BONUS_MAX - BONUS_MIN) * preparation/100.0f + BONUS_MIN;
	}
	
	@Override
	public boolean isGeneral() {
		return true;
	}
	
	public String getRank() {
		return rank;
	}
}
