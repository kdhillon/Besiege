package kyle.game.besiege.party;

public class AmmoType {
       public enum Type {
            ARROW,
            ARROW_FIRE,
            ARROW_POISON,
            DART,
            THROWN,
            THROWN_AXE,
            SLING,
            CROSSBOW_BOLT,
            BULLET
        }

        public String name;
        public int dmg;

        public Type type;

        public boolean isValidForWeapon(RangedWeaponType.Type rangedWeapon) {
            switch (rangedWeapon) {
                case BOW:
                    if (type == Type.ARROW || type == Type.ARROW_FIRE || type == Type.ARROW_POISON) return true;
                    return false;
            }
            // TODO, for now allow everything.
            return true;
        }

        public void setType(String typeString) {
            if (typeString.equals("arrow"))
                type = Type.ARROW;
            if (typeString.equals("arrow_poison"))
                type = Type.ARROW_POISON;
            if (typeString.equals("arrow_fire"))
                type = Type.ARROW_FIRE;
            if (typeString.equals("sling"))
                type = Type.SLING;
            if (typeString.equals("dart"))
                type = Type.DART;
            if (typeString.equals("crossbow"))
                type = Type.CROSSBOW_BOLT;
            if (typeString.equals("thrown"))
                type = Type.THROWN;
            if (typeString.equals("thrown_axe"))
                type = Type.THROWN_AXE;
            if (typeString.equals("firearm"))
                type = Type.BULLET;
        }
}
