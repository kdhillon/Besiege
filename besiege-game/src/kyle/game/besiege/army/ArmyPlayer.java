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

import kyle.game.besiege.*;
import kyle.game.besiege.Character;
import kyle.game.besiege.battle.BattleStage;
import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.panels.BottomPanel;
import kyle.game.besiege.panels.SidePanel;
import kyle.game.besiege.party.Party;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.UnitDraw;

public class ArmyPlayer extends Army {
//	private final String TEXTURE_REGION = "Player";
		
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
		this.player = true;
		this.setScale(calcScale());
//		Location loc = this.detectNearbyFriendlyCity();
		
//		setTextureRegion(TEXTURE_REGION);
//		initializeBox();
//		System.out.println("player origin = " + this.getOriginX() + " y: " + this.getOriginY());
//		System.out.println("player width = " + this.getWidth() + " height: " + this.getHeight());

//		target = null;
		setStopped(true);
		setWaiting(false);
		
		// debugging
		sr = new ShapeRenderer();
		
		// debugging
//		this.getParty().distributeExp(100000);
		this.getParty().wealth = 500;
		
		this.getParty().getGeneral().setName(getCharacter().name);
		kingdom.updateArmyPolygon(this);
        updateVisibility();
        restoreAnimation();
	}

	@Override
	public void act(float delta) {	
		if (this.lastPathCalc > 0) this.lastPathCalc--;
		this.hiding = false;
        super.updateVisibility();
        if (!isWaiting())
            stateTime += delta;

        if (isStopped() && !isWaiting()) {
//			System.out.println("is stopped and isn't waiting");
//			System.out.println("is stopped");
			// STOPPED ISN"T WORKING
//			setPaused(true);
		}
		if (isWaiting()) {
//			System.out.println("is waiting");
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
		if (this.isInSiege() && this.getTarget() != null) { //remove from siegeOrRaid?
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
//		if (!getKingdom().isPaused() && !isInBattle())
//			getKingdom().getMapScreen().getSidePanel().setStay(false);
			
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
						System.out.println("target lost");
						// only display if target is still alive!
						if (getTarget() != null && getTarget().getType() == DestType.ARMY && getKingdom().getArmies().contains((Army) getTarget(), true))
							BottomPanel.log("Target lost!", "yellow");

						setTarget(null);
						getKingdom().setPaused(true);
						getKingdom().getMapScreen().getSidePanel().setDefault(false);
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
//		super.act(delta);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
          // Draw an animation of the player's current armor, and skin.
//		float animationTime = 0;
//		if (isArmyMoving()) {
//			animationTime = stateTime;
//		}
//        UnitDraw.drawUnit(this, batch, walkArmor, walkSkin, getGeneralArmorColor(), getGeneralSkinColor(), animationTime, getGeneral().getEquipment());

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
		super.draw(batch, parentAlpha);
		//if (mousedOver()) drawInfo(batch, parentAlpha);
	}

	@Override
	public void enemyLocationCollision(Location targetLocation) {
		setPaused(true);
		this.setTarget(null);
		targetLocation.hostilePlayerTouched = true;
		getKingdom().getMapScreen().getSidePanel().setActiveLocation(targetLocation);
		getKingdom().getMapScreen().getSidePanel().setHardStay(true);
	}
	
	@Override
	public void friendlyLocationCollision(Location targetLocation) {
		this.garrisonIn(targetLocation);
		targetLocation.playerIn = true;
		this.setTarget(null);

		getSidePanel().setActiveLocation(targetLocation);
		getSidePanel().setHardStay(true);
	}

	private SidePanel getSidePanel() {
	    return getKingdom().getMapScreen().getSidePanel();
    }
	
	@Override
	public boolean detectPointCollision() {
		if (distToCenter(getTarget()) < 1) {
			setStopped(true);
			System.out.println("point collision detected");
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
	public void setStopped(boolean stopped) {
		super.setStopped(stopped);
//		System.out.println("player setting stopped: " + stopped);
	}
	
	@Override
	public void enemyArmyCollision(Army targetArmy) {
		setPaused(true);
		if (!targetArmy.isInBattle() && !this.isInBattle() && targetArmy.getTarget() != this) {
			targetArmy.playerTouched = true;
			getKingdom().getMapScreen().getSidePanel().setActiveArmy(targetArmy);
			getKingdom().getMapScreen().getSidePanel().setHardStay(true); // when an army is reached, force a user decision
		}
		else if (targetArmy.isInBattle() && !this.isInBattle()){
			// join battle?
			if (targetArmy.getBattle().shouldJoinAttackers(this)) {
				targetArmy.getBattle().addToAttackers(this);
				getKingdom().getMapScreen().getSidePanel().setActiveBattle(targetArmy.getBattle());
				this.setBattleActor(targetArmy.getBattleActor());
			} else if (targetArmy.getBattle().shouldJoinDefenders(this)) {
				targetArmy.getBattle().addToDefenders(this);
				this.setBattleActor(targetArmy.getBattleActor());
				getKingdom().getMapScreen().getSidePanel().setActiveBattle(targetArmy.getBattle());
			}
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
					this.joinBattle(targetArmy.getBattleActor());
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
	public void createPlayerBattleWith(Array<Party> allies, Array<Party> enemies, boolean defending, Location siegeOrRaidOf) {
		System.out.println("switching to battle view");
		// first create battle stage with appropriate stuff

		Siege siege = null;
		if (siegeOrRaidOf != null) {
			// If player is raiding, create a new "siege" for the raid.
			if (siegeOrRaidOf.type == Location.LocationType.VILLAGE) {
				siege = new Siege(siegeOrRaidOf, this.getFaction());
			} else {
				siege = siegeOrRaidOf.siege;
			}
			if (siege == null) throw new AssertionError();
		}
		BattleStage bs = new BattleStage(this.getKingdom().getMapScreen(), allies, enemies, defending, siege);
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
		if (target != null)
			System.out.println("setting player target  " + target.getName());
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
		System.out.println("next target player");
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
	
	public String getTitle() {
		return getKingdom().getMapScreen().getCharacter().getTitle();
	}
}
