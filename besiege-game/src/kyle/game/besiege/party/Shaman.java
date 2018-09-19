package kyle.game.besiege.party;

import kyle.game.besiege.MultiValue;

// Represents a Shaman or priest unit.
// Shamans are a special unit type that don't fight in battle.
// There is a limited number of Shamans for each
public class Shaman extends Soldier {
    private MultiValue power; // represents a Shaman or priest's spiritual power. in reality, it's an AOE.
    private Type type;

    // These types of shamans give different powers to their subparties during battle.
    // They also increase the healing rate of all units in the subparty.
    // They march into battle behind their BSPs, like their general
    enum Type {
        // Forest
        WOLF("Wolf"),
        // Tundra
        BEAR("Bear"),
        // Plains
        HAWK("Hawk"),
        // Desert
        DEATH("Death");
        public String name;
        public UnitType type;
        Type(String name) {
            this.name = name;
            generateUnitType();
        }

        private void generateUnitType() {
            this.type = new UnitType();
            this.type.cultureType = null; // TODO
        }
    }

    // Instead of attack, Shamans have a "
    // Generates a random type for this party.
    public Shaman(Party party) {
        this.party = party;
        this.atk = null;
        this.power = new MultiValue();
        this.type = getRandomType();

    }

    @Override
    public boolean isShaman() {
        return true;
    }

    public Type getRandomType() {
        return null; // TODO
    }

    @Override
    public String getTypeName() {
        if (this.unitType.cultureType.name == "Desert")
            return type.name + " Priest";
        else return type.name + " Shaman";
    }

    @Override
    public float getAtk() {
        throw new AssertionError();
    }
}
