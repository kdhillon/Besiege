package kyle.game.besiege.battle;

import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.PartyType;

public class BattleOptions {
    // Includes all options required to launch a quick battle.

    // Map options:
    BattleMap.MapType mapType;

    Location.LocationType siegeType; // Can be village, city, or ruin. Null if no siege.

    // TODO make this an enum called "WeatherEffect"
    public enum WeatherEffect {
        NONE,
        RAINING,
        SNOWING
    }

    WeatherEffect weatherEffect = WeatherEffect.NONE;

//    boolean nightTime;

    boolean alliesDefending;

    public static class PartyOptions {
        PartyType partyType;
        int partyCount;
    }

    PartyOptions allyOptions;
    PartyOptions enemyOptions;
}
