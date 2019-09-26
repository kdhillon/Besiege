package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import kyle.game.besiege.Assets;

/* contains information about a specific weapon type */
public class ShieldType {
    public enum Type {
        ROUND,
        LARGE,
    }

    public String name;
    public Color color;
    public int hp;
    public int spdMod;
    public Type type;

    public String getStatsSummary() {
        return name + "\n " + type.name() + "\n " + hp + " Shield HP\n " + UnitType.formatStat(spdMod) + " Speed";
    }

    // TODO replace with a getter for the appropriate textureregion
    public void setType(String typeString) {
        if (typeString.equals("round"))
            type = Type.ROUND;
        if (typeString.equals("large"))
            type = Type.LARGE;
        if (type == null)
            throw new AssertionError();
    }

    public TextureRegion getTexture() {
        switch (type) {
            case ROUND: return Assets.equipment.findRegion("woodenshield2");
            case LARGE: return Assets.equipment.findRegion("largeshield");
        }
        return null;
    }

    public TextureRegion getPreviewTexture() {
        switch (type) {
            case ROUND: return Assets.equipment.findRegion("shieldpreview");
            case LARGE: return Assets.equipment.findRegion("largeshieldpreview");
        }
        return null;
    }
}
