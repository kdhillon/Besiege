/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege.army;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;

import kyle.game.besiege.Character;
import kyle.game.besiege.Destination;
import kyle.game.besiege.Faction;
import kyle.game.besiege.Kingdom;
import kyle.game.besiege.Map;
import kyle.game.besiege.battle.BattleStage;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.PartyType;

public class ArmyPlayer extends Army {
	private final String textureRegion = "Player";
		
	// debugging
	transient private final ShapeRenderer sr;
	public float losSquared;
//	private Destination target;

	public ArmyPlayer() {
		this.sr = new ShapeRenderer();
	}
	
	public ArmyPlayer(Kingdom kingdom, Faction faction,
			int posX, int posY, int troopCount) {
//		super(kingdom, character.name, Faction.PLAYER_FACTION, posX, posY, PartyType.PATROL);
//		super(kingdom, character.name, Faction.BANDITS_FACTION, posX, posY, PartyType.RAIDING_PARTY);
		//super(kingdom, character.name, Faction.factions.get(3), posX, posY, PartyType.PATROL);
		super(kingdom, "", faction, posX, posY, PartyType.Type.TEST, true);
//		Location loc = this.detectNearbyFriendlyCity();
		
		setTextureRegion(textureRegion);
//		initializeBox();
//		System.out.println("player origin = " + this.getOriginX() + " y: " + this.getOriginY());
//		System.out.println("player width = " + this.getWidth() + " height: " + this.getHeight());

//		target = null;
		setStopped(true);
		setWaiting(false);
		
		// debugging
		sr = new ShapeRenderer();
		this.calcMaxPartySize();
		
		// debugging
		this.getParty().distributeExp(100000);
		this.getParty().wealth = 100000;
		
		this.getParty().getGeneral().setName(getCharacter().name);
	}

	@Override
	public void act(float delta) {	
		if (this.lastPathCalc > 0) this.lastPathCalc--;
		this.hiding = false;

		if (isStopped() && !isWaiting()) {
//			System.out.println("is stopped and isn't waiting");
			setPaused(true);
		}
		if (isWaiting()) {
			setStopped(true);
			this.hiding = true;
			//this.wait(delta);
			//System.out.println("setting stopped because waiting");
		}
		else if (this.getBattle() != null && isStopped())
			setStopped(false);
//		if (!this.isWaiting() && this.isGarrisoned() && this.getTarget() != null) { // eject
//			System.out.println("ejecting player");
//			getGarrisonedIn().playerIn = false;
//			getGarrisonedIn().eject(this);
//			panel.setDefault();
//		}
		if (this.isInSiege() && this.getTarget() != null) { //remove from siege?
			if (getSiege().location.playerWaiting) {
				this.setTarget(null);
				getSiege().location.stopWait(); 
			}
			else {
//				System.out.println("armyPlayer.getSiege().remove");
				getSiege().remove(this);
			}
//			System.out.println(getTarget().getName());
		}
		if (!getKingdom().isPaused() && !isInBattle())
			getKingdom().getMapScreen().getSidePanel().setStay(false);
			
		this.speedFactor = (float) (this.ORIGINAL_SPEED_FACTOR * getCharacter().getAttributeFactor("Marching"));
		setLOS(calcLOS());
//		getKingdom().getMapScreen().getFog().updateFog((int) this.getCenterX(), (int) this.getCenterY(), (int) this.getLineOfSight());

		setSpeed(calcSpeed());   // update speed
		setMorale(calcMorale()); // update morale
		setScale(calcScale());

		if (forceWait) { // forces player to wait
//			System.out.println("forcing wait fix next");
			wait(delta);
			if (!isWaiting()) 
				forceWait = false;
//			System.out.println(forceWait);
		}
		else {
//			if (!isInSiege()) {
				if (hasTarget() && !isStopped() && !isInBattle()) {
					if (targetLost())
					{
						// only display if target is still alive!
						if (getTarget() != null && getTarget().getType() == DestType.ARMY && getKingdom().getArmies().contains((Army) getTarget(), true))
							BottomPanel.log("Target lost!", "yellow");

						setTarget(null);
						getKingdom().setPaused(true);
						getKingdom().getMapScreen().getSidePanel().setDefault();
					}
					else {
						path.travel();
//						getKingdom().getMapScreen().getFog().updateFog((int) this.getCenterX(), (int) this.getCenterY(), (int) this.getLineOfSight());
					}
				}
//			}
//			else if (isInSiege()) {
//			}
		}
		getParty().act(delta);
		momentumDecay();
		
		
//		System.out.println("stopped: " + this.isStopped());
//		System.out.println("waiting: " + this.isWaiting());
		//System.out.println("");
//		setMorale(100);
//		this.faction.goRogue();
		//getParty().distributeExp(40);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.draw(getTextureRegion(), getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), 1, 1, getRotation());
		
		if (Map.debug){
			batch.end();
			sr.begin(ShapeType.Line);
			sr.setProjectionMatrix(batch.getProjectionMatrix());
			sr.setColor(1,0,0,1);
			
			Destination prev = this.path.finalGoal; // do in reverse order (Stack!)
			for (Destination curr : path.dStack) {
				if (prev != null && curr != null && prev != curr) {
					sr.line((float) prev.getX(), (float)(prev.getY()), (float) curr.getX(), (float) (curr.getY()));
				}
				else continue;
				prev = curr;
			}
			
			if (path.nextGoal != null)
				sr.line((float) this.getCenterX(), (float)(this.getCenterY()), (float) path.nextGoal.getCenterX(), (float) (path.nextGoal.getCenterY()));
			if (!path.dStack.isEmpty() && path.nextGoal != null)
				sr.line((float) this.path.dStack.peek().getCenterX(), (float)(this.path.dStack.peek().getCenterY()), (float) path.nextGoal.getCenterX(), (float) (path.nextGoal.getCenterY()));
			
			sr.end();
			batch.begin();
		}
		//if (mousedOver()) drawInfo(batch, parentAlpha);
	}
	
	@Override
	public void enemyLocationCollision(Location targetLocation) {
		setPaused(true);
		this.setTarget(null);
		targetLocation.hostilePlayerTouched = true;
		getKingdom().getMapScreen().getSidePanel().setActiveLocation(targetLocation);
		getKingdom().getMapScreen().getSidePanel().setStay(true);
	}
	
	@Override
	public void friendlyLocationCollision(Location targetLocation) {
		this.garrisonIn(targetLocation);
		targetLocation.playerIn = true;
		this.setTarget(null);
		getKingdom().getMapScreen().getSidePanel().setActiveLocation(targetLocation);
	}
	
	@Override
	public boolean detectPointCollision() {
		if (distToCenter(getTarget()) < 1) {
			setStopped(true);
			setPaused(true);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean detectBattleCollision() {
		// present options to user (join, leave, etc)
		return super.detectBattleCollision();
//		return false;
	}
	
	@Override
	public void enemyArmyCollision(Army targetArmy) {
		setPaused(true);
		if (!targetArmy.isInBattle() && !this.isInBattle() && targetArmy.getTarget() != this) {
			targetArmy.playerTouched = true;
			getKingdom().getMapScreen().getSidePanel().setActiveArmy(targetArmy);
			getKingdom().getMapScreen().getSidePanel().setStay(true); // when an army is reached, force a user decision
		}
		else if (targetArmy.isInBattle() && !this.isInBattle()){
			// join battle?
			targetArmy.getBattle().add(this);
			this.setBattle(targetArmy.getBattle());
		}
	}
	
	@Override
	public void friendlyArmyCollision(Army targetArmy) {
		//follow
		if (this.getSpeed() >= targetArmy.getSpeed()) {
			this.setSpeed(targetArmy.getSpeed() - 5);
		}
	}

	@Override
	public boolean targetLost() {
		if (hasTarget() && getTarget().getType() == Destination.DestType.ARMY) {
			Army targetArmy = (Army) getTarget();
			if (!targetArmy.hasParent() || targetArmy.isGarrisoned()) {
				return true;
			}
			if (targetArmy.isInBattle()){
					this.joinBattle(targetArmy.getBattle());
				//if (targetArmy.getBattle().shouldJoin(this) == 0) // Player should get to choose what battles to join!
					return true;
			}
			if (getKingdom().getMapScreen().losOn) {
				if (!targetArmy.withinLOSRange())
					return true;
			}
		}
		return false;
	}
	
	public void attack(Army army) {
		createBattleWith(army, null);
//		BottomPanel.log("Attacking " + getTarget().getName() + "!", "blue5");
	}
	
	// create a battle involving the player
	public void createPlayerBattleWith(Array<Party> allies, Array<Party> enemies, boolean defending, Location siegeOf) {
		System.out.println("switching to battle view");
		// first create battle stage with appropriate stuff
		BattleStage bs = new BattleStage(this.getKingdom().getMapScreen(), allies, enemies, defending, siegeOf);
		this.getKingdom().getMapScreen().switchToBattleView(bs);
	}
	
	public void train() {
		if (!getKingdom().getMapScreen().SIMULATE)
			this.party.distributeExp(getKingdom().getMapScreen().getCharacter().trainingExp);
	}
	
	@Override
	public float calcLOS() {
		if (getCharacter() == null) return 0;
		float los = ((float) (super.calcLOS()*getCharacter().getAttributeFactor("Spotting")));
		this.losSquared = los*los;
		return los;
	}

	@Override 
	public boolean setTarget(Destination target) {
//		if (target != null)
//			System.out.println("setting target " + target.getName());
		boolean toReturn = super.setTarget(target);
		setStopped(false);
		return toReturn;
	}
	
	@Override
	public void newTarget(Destination target) {
//		System.out.println("player can't get new target");
	}
//	@Override
//	public Destination getTarget() {
//		return target;
//	}
	@Override
	public void destroy() {
		BottomPanel.log("Player lost battle!", "red");
		super.destroy();
		getKingdom().removeArmy(this);
		//TODO player death menu
	}
	@Override
	public void nextTarget() {
//		System.out.println("next target");
		setTarget(null);
		setPaused(true);
	}

	public void setPaused(boolean paused) {
		this.getKingdom().setPaused(paused);
	}

	public String getPartyInfo() {
		return getParty().getHealthySize() + "/" + getParty().getTotalSize();
	}
	public Character getCharacter() {
		if (getKingdom() == null) return null;
		return getKingdom().getMapScreen().getCharacter();
	}
	
	@Override
	public String getName() {
		return getCharacter().name + "'s Party";
	}
	
	public void calcMaxPartySize() {
//		getParty().maxSize = (int) (character.famereknown * REKNOWN_PC_FACTOR + BASE_PC);
		getParty().maxSize = 10000;
	}
	
	public String getTitle() {
		return getKingdom().getMapScreen().getCharacter().getTitle();
	}
}
