package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import kyle.game.besiege.Assets;

public class AmmoType {
    public enum Type {
        ARROW,
        ARROW_FIRE,
        ARROW_POISON,
        DART,
        THROWN_AXE,
        THROWN_FIRE,
        SPEAR,
        SPEAR_FIRE,
        ROCK,
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
        if (typeString.equals("arrow_fire")) {
            type = Type.ARROW_FIRE;
        }
        if (typeString.equals("rock"))
            type = Type.ROCK;
        if (typeString.equals("dart"))
            type = Type.DART;
        if (typeString.equals("crossbow"))
            type = Type.CROSSBOW_BOLT;
        if (typeString.equals("spear"))
            type = Type.SPEAR;
        if (typeString.equals("spear_fire"))
            type = Type.SPEAR_FIRE;
        if (typeString.equals("thrown_axe"))
            type = Type.THROWN_AXE;
        if (typeString.equals("thrown_fire"))
            type = Type.THROWN_FIRE;
        if (typeString.equals("firearm"))
            type = Type.BULLET;

        if (type == null) throw new AssertionError("type not found " + typeString);
    }

    public boolean isOnFire() {
        switch(type) {
            case ARROW_FIRE:
            case THROWN_FIRE:
            case SPEAR_FIRE:
                return true;
        }
        return false;
    }

    public TextureRegion getRegion() {
        switch (type) {
            case ARROW:
            case ARROW_FIRE:
            case ARROW_POISON:
                return Assets.map.findRegion("arrow");
            case DART:
                return Assets.map.findRegion("dart");
            case SPEAR:
            case SPEAR_FIRE:
                return Assets.map.findRegion("dart");
            case THROWN_AXE:
                return Assets.map.findRegion("axe");
            case THROWN_FIRE:
            case ROCK:
                return Assets.map.findRegion("slingstone");
        }
        return Assets.map.findRegion("catapult");
    }

    public TextureRegion getBrokenRegion() {
        switch (type) {
            case ARROW:
            case ARROW_FIRE:
            case ARROW_POISON:
                return Assets.map.findRegion("half arrow");
            case DART:
            case SPEAR:
            case SPEAR_FIRE:
                return Assets.map.findRegion("half dart");
            case THROWN_AXE:
                double rand = Math.random();
                if (rand < 0.4)
                    return Assets.map.findRegion("half axe");
                if (rand < 0.66)
                    return Assets.map.findRegion("half axe2");
                return Assets.map.findRegion("half axe3");
            case THROWN_FIRE:
            case ROCK:
                return Assets.map.findRegion("slingstone");
        }
        return Assets.map.findRegion("catapult");
    }

    public boolean shouldSpin() {
        switch (type) {
            case ARROW:
            case ARROW_FIRE:
            case ARROW_POISON:
            case DART:
            case SPEAR:
            case SPEAR_FIRE:
            case CROSSBOW_BOLT:
                return false;
            case THROWN_AXE:
            case THROWN_FIRE:
            case ROCK:
            case BULLET:
                return true;
        }
        return false;
    }

}
