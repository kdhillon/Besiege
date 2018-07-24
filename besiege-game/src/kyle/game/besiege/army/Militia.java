///*******************************************************************************
// * Besiege
// * by Kyle Dhillon
// * Source Code available under a read-only license. Do not copy, modify, or distribute.
// ******************************************************************************/
//package kyle.game.besiege.army;
//
//import kyle.game.besiege.Faction;
//import kyle.game.besiege.Kingdom;
//import kyle.game.besiege.location.Village;
//import kyle.game.besiege.playerPartyPanel.PartyType;
//import kyle.game.besiege.playerPartyPanel.PartyType.Type;
//
//public class Militia extends Army { // Revamp this :)
//	private final String textureRegion = "Farmer";
//
//	private Village village;
//	
//	public Militia() {}
//	
//	public Militia(Kingdom kingdom, String name, Faction faction, float posX,
//			float posY) {
//		super(kingdom, name, faction, posX, posY, Type.MILITIA);
//		setTextureRegion(textureRegion);
//	}
//	
//	@Override
//	public void act(float delta) {
//		if (!isInBattle()) {
//			// return all wealth to village
//			village.getParty().wealth = village.getParty().wealth + this.getParty().wealth;
//			this.destroy();
//		}
//	}
//	
//	public void setVillage(Village village) {
//		this.village = village;
//	}
//
//}
