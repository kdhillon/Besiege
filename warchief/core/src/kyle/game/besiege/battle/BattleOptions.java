package kyle.game.besiege.battle;

import com.badlogic.gdx.graphics.Color;
import kyle.game.besiege.location.Location;
import kyle.game.besiege.party.PartyType;

public class BattleOptions {
    // Includes all options required to launch a quick battle.

    // Map options:
    BattleMap.MapType mapType;

    Location.LocationType siegeType; // Can be village, city, or ruin. Null if no siege.

    public enum TimeOfDay {
        DAY(new Color(0,0,0,0)),
        EVENING(new Color(0.8f, 0.3f, 0.1f, 0.05f)),
        NIGHT(new Color(0.01f, 0.01f, 0.04f, 0.4f));

        final Color tint;
        TimeOfDay(Color tint) {
            this.tint = tint;
        }
    }

    TimeOfDay timeOfDay = TimeOfDay.DAY;

    public enum WeatherEffect {
        NONE,
        RAINING,
        SNOWING
    }

    WeatherEffect weatherEffect = WeatherEffect.NONE;

    boolean alliesDefending;

    public static class PartyOptions {
        PartyType partyType;
        int partyCount;
    }

    PartyOptions allyOptions;
    PartyOptions enemyOptions;
}
