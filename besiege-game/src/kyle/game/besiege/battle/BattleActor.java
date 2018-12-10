package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import kyle.game.besiege.*;
import kyle.game.besiege.Destination.DestType;
import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;
import kyle.game.besiege.party.Party;

// Represents a battle on the world map. 
public class BattleActor extends Actor implements Destination {
	private Battle battle;
	private final String REGION = "battle";
	transient private TextureRegion region;
//	transient public TextureRegion halfCrest;
	private String name;
	private Kingdom kingdom;
	private Siege siege; // only if a siegeOrRaid

	private InputListener getNewInputListener() {
		return new InputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y,
								int pointer, int button) {
				boolean touchdown=true;
				//do your stuff
				//it will work when finger is released..
				System.out.println("Touched up " + getName());
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y,
									 int pointer, int button) {
				boolean touchdown=false;
				//do your stuff it will work when u touched your actor
				return true;
			}

			@Override
			public void enter(InputEvent event,  float x, float y, int pointer, Actor fromActor) {
				BattleActor battle = (BattleActor) event.getListenerActor();
				if (!kingdom.getPlayer().hasVisibilityOf(battle)) return;

				System.out.println("Mousing over " + getName());
				System.out.println("Setting panel! " + getName());
				kingdom.setPanelTo(battle);
				kingdom.setMousedOver(battle);
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				BattleActor battle = (BattleActor) event.getListenerActor();
				if (!kingdom.getPlayer().hasVisibilityOf(battle)) return;

				System.out.println("returning to previous (exit)");
				getKingdom().mouseOverCurrentPoint();
			}
		};
	}

	// For Kryo
	public BattleActor() {}
	
	public BattleActor(Kingdom kingdom, Party initAttackerParty, Party initDefenderParty, Siege siege) {
		Army initAttacker = initAttackerParty.army;
		this.kingdom = kingdom;
		this.name = initAttackerParty.getName() + " vs " + initDefenderParty.getName();
		this.siege = siege;
		this.battle = new BattleSim(this, initAttackerParty, initDefenderParty);
		this.setScale(10);
		
		region = Assets.atlas.findRegion(REGION);
		this.setPosition(initAttacker.getCenterX(), initAttacker.getCenterY());
		this.setWidth(region.getRegionWidth()*getScaleX());
		this.setHeight(region.getRegionHeight()*getScaleY());
		this.setOrigin(region.getRegionWidth()*getScaleX()/2, region.getRegionWidth()*getScaleY()/2);
		this.addListener(getNewInputListener());
	}
	
	@Override
	public void act(float delta) {
		battle.simulate(delta);
		super.act(delta);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		
		// draw crests too?
		if (!kingdom.getPlayer().hasVisibilityOf(this)) return;
		
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), 1, 1, getRotation());
		
//		if (aArmies.first() != null)
//			batch.draw(this.aArmies.first().getFaction().crest, getX()+getWidth()/2, getY() + getHeight(), getOriginX(), getOriginY(),
//				getWidth(), getHeight(), 1, 1, getKingdomRotation());
//		if (dArmies.first() != null)
//			batch.draw(this.halfCrest, getX(), getY() + getHeight(), getOriginX(), getOriginY(),
//				getWidth()/2, getHeight(), 1, 1, getKingdomRotation());
		super.draw(batch, parentAlpha);
	}
	
	// change faction of city if siegeOrRaid
//	public void handleVictory(boolean didAtkWin) {
//		if (siege != null) {
//			if (didAtkWin) {
//				if (siege.location.isVillage()) {
//					Army attackingArmy = battle.getAttackingParties().first().army;
//					if (attackingArmy != null) {
//						((Village) siege.location).handleRaidVictory(attackingArmy);
//					}
//				} else {
//                    siege.siegeSuccess();
//				}
//			} else {
//                siege.siegeFailure();
//			}
//		}
//	}

	@Override
	public Faction getFaction() {
		return null;
	}

	@Override
	public DestType getType() {
		return Destination.DestType.BATTLE;
	}
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public float getCenterX() {
		return this.getX() + this.getOriginX();
	}
	
	@Override
	public float getCenterY() {
		return this.getY() + this.getOriginY();
	}

	public void destroy() {
		System.out.println("Destroying battle victory(army, army) " + this.getName());

        if (this.siege != null) {
//				this.siegeOf.siegeOrRaid.destroy();
            this.siege.battleActor = null;
        }

		this.kingdom.removeBattle(this);
		this.remove();
	}
	
	public Kingdom getKingdom() {
		return kingdom;
	}
	
	public Battle getBattle() {
		return battle;
	}

	public Siege getSiege() {
		return siege;
	}
}
