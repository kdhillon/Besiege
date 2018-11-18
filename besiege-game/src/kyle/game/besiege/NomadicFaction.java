package kyle.game.besiege;

import com.badlogic.gdx.graphics.Color;
import kyle.game.besiege.army.Bandit;
import kyle.game.besiege.voronoi.Biomes;
import kyle.game.besiege.voronoi.Center;

/**
 * A faction that doesn't just sit its ass on locations.
 * As opposed to a stationary faction
 *
 * However,a nomadic faction should have 1-2 centers that serve as its reference and return point.
 */
public class NomadicFaction extends Faction {
    public static final int TOTAL_BANDITS = 5; // bandits per nomadic faction

    // Differences:
    // (for now)
    //   No nobles
    //   No villages or cities
    //
    //  Mostly just for holding a crest and representing a
    //  When sacking a city its

    public int banditCount;

    public Center baseCenter;

    // TODO make it so not all nomadic factions are bandit factions

    public NomadicFaction(Kingdom kingdom, String name, Color color, Center baseCenter) {
	    super(kingdom, name, color);
	    nobles.clear();
	    cultureType = null;
	    this.baseCenter = baseCenter;
	    this.cultureType = baseCenter.cultureType;
	    if (cultureType == null) throw new AssertionError();
        generateCrest(true);
    }

    // Normally, a location manages all its armies (patrols, villagers)
    // In nomadic faction, they are managed all at once (like bandits)

    @Override
    public boolean isNomadic() {
        return true;
    }

    @Override
    public void act(float delta) {
         manageBandits();
    }

    public void manageBandits() {
        if (banditCount <= TOTAL_BANDITS) {
//			if (Math.random() < 1.0/BANDIT_FREQ) {
//				City originCity = cities.random();
            //					if (originCity.getVillages().size == 0)
            createBandit(baseCenter);
            banditCount++;
        }
    }

    public void createBandit(Center center) {
        //get a good bandit location, out of player's LOS, away from other armies, etc.
        Bandit bandit = new Bandit(kingdom, "Bandit", 0, 0, this);
        float posX, posY;
        //		float posX = 2048;
        //		float posY = 2048;
        Point p = new Point(0,0);
        int count = 0;
        do {
            count++;
            posX = center.loc.x + Random.getRandomInRange(-bandit.getLineOfSight(), bandit.getLineOfSight());
            posY = Map.HEIGHT - center.loc.y + Random.getRandomInRange(-bandit.getLineOfSight(), bandit.getLineOfSight());
            p.setPos(posX, posY);
            //			System.out.println("creating bandit spot");
        }
        while(kingdom.map.isInWater(p));

        // TODO bring this back if we want to ensure armies don't spawn close to player.
//        while ((kingdom.map.isInWater(p) || Kingdom.distBetween(p, player) <= player.getLineOfSight()) && count < 10); // makes sure bandit is out of sight of player!

        if (count == 10) return;

        bandit.setPosition(posX, posY);
        //		System.out.println("new bandit created at " + origin.getName() + posX + "  " + posY);
        kingdom.addArmy(bandit);
//        banditCount++;
    }
}
