package kyle.game.besiege.battle;

import kyle.game.besiege.army.Army;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.location.Village;

public class VictoryManager {
    Location siegeOf;
    Battle battle;

    public VictoryManager(Battle battle, Location siegeOf) {
        this.siegeOf = siegeOf;
        this.battle = battle;
    }

    // change faction of city if siege
    public void handleVictory(boolean didAtkWin) {
        if (siegeOf != null) {
            if (didAtkWin) {
                if (siegeOf.isVillage()) {
                    Army attackingArmy = battle.getAttackingParties().first().army;
                    if (attackingArmy != null) {
                        ((Village) siegeOf).handleRaidVictory(attackingArmy);
                    }
                } else {
                    siegeOf.getSiege().siegeSuccess();
                }
            } else {
                siegeOf.getSiege().siegeFailure();
            }
        }
    }

}
