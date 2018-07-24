package kyle.game.besiege.party;
//package kyle.game.besiege.playerPartyPanel;
//
//import com.badlogic.gdx.utils.Array;
//import static kyle.game.besiege.playerPartyPanel.Weapon.*;
//
//public enum UnitType {
//	BANDITS(new Weapon[]{MILITARY_FORK, HATCHET, CLUB, SPEAR, MACE, SHORTBOW, RECURVE, LONGBOW}),
//	PEASANTS(new Weapon[]{PITCHFORK, MILITARY_FORK, HATCHET, CLUB, SHORTBOW}),
//	FARMERS(new Weapon[]{PITCHFORK, MILITARY_FORK, HATCHET}),
//	
//	POLEAXE_BAD(new Weapon[]{SPEAR}),
//	POLEAXE_MED(new Weapon[]{PIKE, HALBERD}),
//	POLEAXE_BEST(new Weapon[]{VOULGE, GUISARME}),
//	
//	MOUNTED_MED(new Weapon[]{CAVALRY_AXE, CAVALRY_SPEAR, CAVALRY_PICK}),
//	KNIGHT(new Weapon[]{LANCE, FLAIL, ARMING_SWORD}),
//
//	RANGED_BAD(new Weapon[]{SHORTBOW}),
//	RANGED_MED(new Weapon[]{SHORTBOW, CROSSBOW, LONGBOW, RECURVE}),
//	RANGED_GOOD(new Weapon[]{ADV_CROSSBOW, ADV_LONGBOW, ADV_RECURVE}),
//	
//	LIGHT_BAD(new Weapon[]{CLUB, HATCHET}),
//	LIGHT_MED(new Weapon[]{SHORTSWORD, MACE, WAR_HAMMER}),
//	LIGHT_GOOD(new Weapon[]{MORNINGSTAR, FALCHION}),
//	
//	HEAVY_MED(new Weapon[]{LONGSWORD, BATTLE_AXE}),
//	HEAVY_GOOD(new Weapon[]{GREATSWORD, MAUL, GLAIVE}),
//	
//	INFANTRY_BAD(new Weapon[]{CLUB, HATCHET, SPEAR}),
//	INFANTRY_MED(new Weapon[]{PIKE, HALBERD, SHORTSWORD, MACE, WAR_HAMMER, LONGSWORD, BATTLE_AXE}),
//	INFANTRY_GOOD(new Weapon[]{VOULGE, GUISARME, MORNINGSTAR, FALCHION, GREATSWORD, MAUL, GLAIVE});
//	
//	private Array<Weapon> types; 
//	
//	private UnitType(Weapon[] weapons) {
//		this.types = new Array<Weapon>(weapons);
//	}
//
//	public Weapon random() {
//		return types.random();
//	}
//}
