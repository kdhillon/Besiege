package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import kyle.game.besiege.army.Noble;
import kyle.game.besiege.battle.BattleSubParty;

public class General extends Soldier {
	private final static float BONUS_MAX = 2;
	private final static float BONUS_MIN = -1;

	private final static float RANGE_MAX = 5;
	private final static float RANGE_MIN = -2;
	
	// change for testing
	private static final int BASE_PC = 1; // base playerPartyPanel count
	private static final int BASE_SUBPARTY = 5; // base playerPartyPanel count

//		private static final int BASE_PC = 35; // base playerPartyPanel count
	private static final float FAME_PC_FACTOR = 1f;
	
	private static final int GENERAL_THRESHOLD = 100;
	
	// fill this in later
	public String epithet; 	// "great", "hammer", etc 
	
	public General rival;
		
	public int loyalty; 	// affects chance of desertion, mutiny
	public int morality;
	
	private int fame;
	private int absoluteMaxSize; // hard max, cannot exceed this.
	
	public int courage;  // increases morale in battle, but also mutiny chance
	public int preparation; // increases troop health in battle

	public int infantryAttack;
	public int infantryDefense;
	
	public int rangedCommand;

	// for Kryo
	public General() {
	}

	public General(UnitType unitType, Party party, PartyType pt) {
		super(unitType, party);

		this.isImportant = true;

//		System.out.println("pt.getMaxSize " + pt.getMaxSize());
		
		this.absoluteMaxSize = pt.getMaxSize();
		
		this.increaseFame(getRandomFameFor(pt));
		
		// TODO set fame, use that to set stats.
		// or update stats later.
		
		generateRandomInitStats();
	}
	
	// for promoting a soldier to a general
    // MAKE SURE TO COPY STUFF FROM ABOVE CONSTRUCTOR
	public General(Soldier s) {
		super(s); // copy constructor
		this.isImportant = true;
	
		if (s.party != null) {
            this.increaseFame(getRandomFameFor(s.party.pt));
            this.absoluteMaxSize = s.party.pt.getMaxSize();
        }

        generateRandomInitStats();
	}
	
	// use a reverse calculation to figure out how much fame to give this guy.
	public int getRandomFameFor(PartyType pt) {
		// count = fame * c + min
		int minFame = (int) ((pt.getMinSize() - BASE_PC) / FAME_PC_FACTOR);
		int maxFame = (int) ((pt.getMaxSize() - BASE_PC) / FAME_PC_FACTOR);
//		System.out.println("Fame: " + minFame + ", " + maxFame);
		return MathUtils.random(minFame, maxFame);
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
	
	public float getBonusAccuracy() {
		return (BONUS_MAX - BONUS_MIN) * rangedCommand/100.0f + BONUS_MIN;
	}
	
	public float getBonusGeneralRange() {
		return (RANGE_MAX - RANGE_MIN) * rangedCommand/100.0f + RANGE_MIN;
	}
	
	public float getHPBonus() {		
		return (BONUS_MAX - BONUS_MIN) * preparation/100.0f + BONUS_MIN;
	}
	
	public float getMoraleBonus() {
		return courage/100.0f * (1-BattleSubParty.BASE_MORALE - (1-BattleSubParty.MAX_MORALE));
	}
	
	public void increaseFame(int fame) {
		this.fame += fame;
		if (Math.random() * 100 < fame) {
			increaseRandomStat();
		}
		if (this.party.army != null && this.party.army.isNoble()) {
		    // Increase the Noble's status.
//			((Noble) party.army).updateRank();
		}
	}
	
	public void increaseRandomStat() {
		double MAX_VAL = 0.5;
		double rand = Math.random() * MAX_VAL;
		int value = (int) (Math.random() * 5 + 1);
		if (rand < 0.1) {
			courage += value;
		}
		else if (rand < 0.2) {
			preparation += value;			
		}
		else if (rand < 0.3) {
			infantryAttack += value;
		}
		else if (rand < 0.4) {
			infantryDefense += value;
		}
		else if (rand < MAX_VAL) {
			rangedCommand += value;
		}
//		else if (rand < 0.6) {
//			courage += value;
//		}
//		else if (rand < 0.2) {
//			courage += value;
//		}
	}
	
	public int getFame() {
		return fame;
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
	
	public int getMaxSize() {
	    return absoluteMaxSize;
//		return Math.min(absoluteMaxSize, getTroopsForFame(fame));
	}
	
	public int getMaxSubPartySize() {
		return Math.min(Subparty.HARD_MAX, getBodyguardForFame(fame));
	}
	
	public int getTroopsForFame(int fame) {
		return (int) (getFame() * FAME_PC_FACTOR + BASE_PC);
	}
	
	public int getBodyguardForFame(int fame) {
		return (int) (getFame() * FAME_PC_FACTOR + BASE_SUBPARTY);
	}
	
	public String getRank() {
		if (this.subparty.getRank() == 0) {
			if (this.party.army != null && this.party.army.isNoble()) return ((Noble)this.party.army).title;
			
//			if (this.fame > GENERAL_THRESHOLD)
//				return "General";
					
			if (this.party.sub.size > 1) {
				if (this.party.army != null && this.party.army.isBandit())
					return "Warlord";
				return "General";
			}
			
			return "Commander";
		}
		if (this.subparty.getRank() == 1) {
			return "Captain";
		}
		
		// not active yet
		if (this.subparty.getRank() == 2) {
			return "Platoon Leader";
		}
		return "None";
	}
	
	public void setName(String name) {
//		System.out.println("Setting name: " + name);
		if (name != null && name.length() > 0)
			this.name = name;
	}
}
