package kyle.game.besiege.battle;

import com.badlogic.gdx.Game;
import kyle.game.besiege.Assets;
import kyle.game.besiege.party.PartyType;
import kyle.game.besiege.party.UnitLoader;

public class QuickBattleGame extends Game {
    @Override
    public void create() {
        Assets.load();

        // TODO make this real.
        BattleOptions options = new BattleOptions();

        options.mapType = BattleMap.MapType.MOUNTAINS;
        BattleOptions.PartyOptions allyOptions = new BattleOptions.PartyOptions();
        allyOptions.partyCount = 1;
        allyOptions.partyType = PartyType.generatePartyType(PartyType.Type.ELITE, UnitLoader.cultureTypes.get("Desert"));
        options.allyOptions = allyOptions;
        options.enemyOptions = allyOptions;
        options.timeOfDay = BattleOptions.TimeOfDay.DAY;

        Simulation simulation = new Simulation(options, null);

        setScreen(simulation.getMapScreen()); // eventually make mainMenu
    }
}
