package kyle.game.besiege.party;

import kyle.game.besiege.Faction;
import kyle.game.besiege.location.Location;

public class ImportantPerson extends Soldier {
	private static final int[] REKNOWN_RANK = {0,    50,      100,     150,    200, 	  250,		 300, 301};
	private static final String[] RANKS = {"Baron", "Earl", "Count", "Duke", "Prince", "Archduke", "King"};

	int rank;
	int nextRank;
	
	// fill this in later
	public String epithet; 	// "great", "hammer", etc 
	public String title; 	// "earl of x", "bandit warlord"
	public int fame;

	public Location home;

	public String fullName;

	public Faction faction;

	public Party party; // the party they command

	// rules: one IP per party (for now, but think about having multiple later on, with a rank)

	public ImportantPerson rival;

	public ImportantPerson(UnitType unitType, Party party, String title, Location home) {
		super(unitType, party);
		this.isImportant = true;
		updateTitle(title);
		updateHome(home);

		System.out.println("Created general: " + this.getName() + ", " + this.unitType.name);
	}

	// for Kryo
	public ImportantPerson() {

	}

	public String getTitle() {
		if (this.home != null) {
			return this.title + " of " + this.home.getName();			
		}
		return this.title;
	}

	public void updateFullName() {
		fullName = this.getName() + ", " + getTitle();
	}

	public void updateHome(Location home) {
		this.home = home;
		updateFullName();
	}

	public void updateTitle(String title) {
		this.title = title;
		updateFullName();
	}

	public String getFullName() {
		return fullName; 
	}

	public void giveReknown(int reknown) {
		this.fame += reknown;
		if (fame <= RANKS.length - 1) {
			if (this.fame >= nextRank) {
				increaseRank();
			}
		}
//		calcMaxPartySize();
	}

	private void increaseRank() {
		this.rank++;
		updateTitle(RANKS[rank]);
		nextRank = REKNOWN_RANK[rank+1];
	}

	public static String getTitleForFame(int fame) {
		for (int i = 1; i < REKNOWN_RANK.length; i++) {
			if (REKNOWN_RANK[i] > fame) return RANKS[i-1];
		}
		return "God";
	}

}
