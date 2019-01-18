package kyle.game.besiege.party;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import kyle.game.besiege.Assets;
import kyle.game.besiege.StrictArray;
import kyle.game.besiege.battle.Projectile;
import kyle.game.besiege.battle.Unit;

// Class for drawing the body of the Unit
public class UnitDraw extends Actor {
    public Animation walkArmor, walkSkin;
    //	public Animation animationAttack;
    public Animation dieArmor, dieSkin;
    public Animation firingArmor, firingSkin;

    private boolean firingLoop;
    public boolean drawAmmo;
    public boolean drawAmmoReversed;

    public Color armorTint;
    public Color skinTint;

    public Color armorTintDead;
    public Color skinTintDead;

    public Soldier soldier;
    public RangedWeaponType rangedWeapon;

    private Color c = new Color();

    private Unit unit;
    private UnitType type;

    public UnitDraw(Unit unit) {
        this.unit = unit;
        this.soldier = unit.soldier;
        this.type = soldier.unitType;

        this.rangedWeapon = unit.rangedWeapon;

        this.setWidth(unit.stage.unit_width);
        this.setHeight(unit.stage.unit_height);

        assignColor();

        float ani = 0.25f;
        walkArmor	= createAnimation(type.armor.getWalkAnimation(), 2, ani);
        walkSkin 	= createAnimation("walk-skin", 2, ani);

        // later on randomize the dying animation
        dieArmor	= createAnimation(type.armor.getDyingAnimation(), 4, ani);
        dieSkin 	= createAnimation("die1-skin", 4, ani);
        dieArmor.setPlayMode(Animation.PlayMode.NORMAL);
        dieSkin.setPlayMode(Animation.PlayMode.NORMAL);

        if (unit.isRanged()) {
            if (this.rangedWeapon.type == RangedWeaponType.Type.FIREARM) {
                firingArmor	= createAnimation(type.armor.getFirearmAnimation(), 2, 2);
                firingSkin 	= createAnimation("firearm-skin", 2, 2);
                firingArmor.setPlayMode(Animation.PlayMode.NORMAL);
                firingSkin.setPlayMode(Animation.PlayMode.NORMAL);
                firingLoop = true;
                drawAmmo = false;
            }
            else if (this.rangedWeapon.type == RangedWeaponType.Type.ATLATL) {
                firingArmor	=    createAnimation(type.armor.getAtlatlArmor(), 2, 2);
                firingSkin 	= createAnimation("atlatl-skin", 2, 2);
                firingArmor.setPlayMode(Animation.PlayMode.REVERSED);
                firingSkin.setPlayMode(Animation.PlayMode.REVERSED);
                firingLoop = false;
                drawAmmo = true;
            }
            else if (this.rangedWeapon.type == RangedWeaponType.Type.THROWN_AXE) {
                firingArmor	=    createAnimation(type.armor.getThrownAnimation(), 2, 2);
                firingSkin 	= createAnimation("thrown-skin", 2, 2);
                firingArmor.setPlayMode(Animation.PlayMode.REVERSED);
                firingSkin.setPlayMode(Animation.PlayMode.REVERSED);
                firingLoop = false;
                drawAmmo = true;
                drawAmmoReversed = true;
            }
            else if (this.rangedWeapon.type == RangedWeaponType.Type.THROWN || this.rangedWeapon.type == RangedWeaponType.Type.THROWN_FIRE || this.rangedWeapon.type == RangedWeaponType.Type.SLING) {
                firingArmor	=    createAnimation(type.armor.getThrownAnimation(), 2, 2);
                firingSkin 	= createAnimation("thrown-skin", 2, 2);
                firingArmor.setPlayMode(Animation.PlayMode.REVERSED);
                firingSkin.setPlayMode(Animation.PlayMode.REVERSED);
                firingLoop = false;
                drawAmmo = true;
                drawAmmoReversed = false;
            }
            else {
                firingArmor = createAnimation(type.armor.getFiringAnimation(), 2, 2);
                firingSkin = createAnimation("firing-skin", 2, 2);
                firingArmor.setPlayMode(Animation.PlayMode.NORMAL);
                firingSkin.setPlayMode(Animation.PlayMode.NORMAL);
                firingLoop = false;
                drawAmmo = false;
            }
        }
    }

    /** TODO draw faded if hidden */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        c.set(batch.getColor());
        if (unit.isDying) {
            // For now, don't draw equipment.
//             TODO fix headdress drawing
//            System.out.println("drawing dying unit: " + unit.timeSinceDeath + " " + (dieArmor == null) + (dieSkin == null) + " " +  armorTint.toString() + " " + skinTint.toString());
            drawUnit(this, batch, dieArmor, dieSkin, armorTintDead, skinTintDead, unit.timeSinceDeath, false, unit.equipment, unit.alphaIfHidden());
        } else {

            if (unit.isHit)
                batch.setColor(1, 0, 0, 1);
            else if (unit.attacking != null) {
                // maybe remove later
//                unit.face(unit.attacking, );
                drawUnit(this, batch, walkArmor, walkSkin, armorTint, skinTint, unit.stateTime, unit.equipment, unit.alphaIfHidden());
            } else if (unit.moveSmooth) {
                drawUnit(this, batch, walkArmor, walkSkin, armorTint, skinTint, unit.stateTime, unit.equipment, unit.alphaIfHidden());
            } else if (unit.isFiring()) {
                drawUnit(this, batch, firingArmor, firingSkin, armorTint, skinTint, unit.firingStateTime, firingLoop, unit.equipment, unit.alphaIfHidden());
                drawAmmoIfNecessary(batch);
            } else {
                drawUnit(this, batch, walkArmor, walkSkin, armorTint, skinTint, 0, false, unit.equipment, unit.alphaIfHidden());
            }
        }
        if (unit.isHit){
            unit.isHit = false;
        }

        batch.setColor(c);
    }

    // Draws ammo on top of firing animation (for atlatls and tomahawks for example)
    private void drawAmmoIfNecessary(Batch batch) {
        if (unit.rangedWeaponOut()) { //  || unit.isFiring()
            if (unit.isFiring()) {
                // Draw  ammo on top
                if (unit.rangedWeapon.shouldDrawAmmo() && unit.unitDraw.isDrawingRangedLoadedAnimation()) {
                    float ammoRotation = getRotation();
                    float y = getY() + unit.getHeight() * 2 / 8;
                    float x = getX() + unit.getWidth() * 6 / 8;
                    if (unit.rangedWeapon.drawAmmoReversed()) {
                        ammoRotation = ammoRotation + 180;
                        x = getX() + unit.getWidth() * 7 / 8;
                        y = getY();
                    }
                    batch.draw(
                            unit.ammoType.getRegion(),
                            x,
                            y,
                            unit.ammoType.getRegion().getRegionWidth() / 2,
                            unit.ammoType.getRegion().getRegionHeight() / 2,
                            unit.ammoType.getRegion().getRegionWidth(),
                            unit.ammoType.getRegion().getRegionHeight(),
                            Projectile.getDefaultSmallScale() * 4,
                            Projectile.getDefaultSmallScale() * 4,
                            ammoRotation);

                }
            }
        }
    }

    private void assignColor() {
        this.armorTint = soldier.getArmor().getColorTopDown();
        this.skinTint = soldier.getColor();

        this.armorTintDead = new Color(0.8f, 0.8f, 0.8f, 1).mul(armorTint);
        if (armorTint == Color.CLEAR) armorTintDead = Color.CLEAR;
//        this.skinTintDead = skinTint;
        this.skinTintDead = new Color(0.6f, 0.6f, 0.6f, 1).mul(skinTint);
    }

    // create animation with speed .25f assuming one row, loops by default
    public static Animation<TextureRegion> createAnimation(String filename, int columns, float time) {
        TextureRegion walkSheet = Assets.units.findRegion(filename);
        if (walkSheet == null) throw new AssertionError(filename + " not found");
        TextureRegion[][] textureArray = walkSheet.split(walkSheet.getRegionWidth()/columns, walkSheet.getRegionHeight()/1);
        Animation<TextureRegion> animation = new Animation(time, textureArray[0]);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        return animation;
    }

    public boolean isDrawingRangedLoadedAnimation() {
        return !unit.moveSmooth && unit.isFiring() && firingArmor.getKeyFrameIndex(unit.firingStateTime) == 0 && unit.attacking == null && !unit.isDying;
    }

    public static void drawAnimationTint(Batch batch, Animation<TextureRegion> animation, float stateTime, boolean loop, Color tint, Actor actor, float alpha) {
        TextureRegion region = animation.getKeyFrame(stateTime, loop);
        drawItemTint(batch, region, tint, actor, alpha);
    }

    /** TODO this is inefficient*/
    public static void drawItemTint(Batch batch, TextureRegion region, Color tint, Actor actor, float alpha) {
        Color c = batch.getColor();
//        batch.setColor(tint);
        // Should we try multiplying by the current color to darken?
//        System.out.println(c.toString());
        Color blended = blend(c, tint);
        blended.a = alpha;
        batch.setColor(blended);
//        batch.setColor(tint);

//        System.out.println(batch.getColor().toString());
        drawItem(batch, region, actor);
        batch.setColor(c);
    }

    public static void drawUnit(Actor actor, Batch batch, Animation armor, Animation skin, Color armorColor, Color skinColor, float stateTime) {
        drawUnit(actor, batch, armor, skin, armorColor, skinColor,stateTime, true, null, 1);
    }

    // Allows you to specify alpha
    public static void drawUnit(Actor actor, Batch batch, Animation armor, Animation skin, Color armorColor, Color skinColor, float stateTime, StrictArray<Equipment> equipment, float alpha) {
        drawUnit(actor, batch, armor, skin, armorColor, skinColor, stateTime, true, equipment, alpha);
    }

    public static void drawUnit(Actor actor, Batch batch, Animation armor, Animation skin, Color armorColor, Color skinColor, float stateTime, StrictArray<Equipment> equipment) {
        drawUnit(actor, batch, armor, skin, armorColor, skinColor, stateTime, true, equipment, 1);
    }

    public static void drawUnit(Actor actor, Batch batch, Animation armor, Animation skin, Color armorColor, Color skinColor, float stateTime, boolean loop, StrictArray<Equipment> equipment) {
        drawUnit(actor, batch, armor, skin, armorColor, skinColor, stateTime, loop, equipment, 1);
    }

    public static void drawUnit(Actor actor, Batch batch, Animation armor, Animation skin, Color armorColor, Color skinColor, float stateTime, boolean loop, StrictArray<Equipment> equipment, float alpha) {
        if (skinColor == null || armorColor == null) throw new AssertionError();
        drawAnimationTint(batch, skin, stateTime, loop, skinColor, actor, alpha);

        if (armor != null && armorColor != Color.CLEAR) {
            drawAnimationTint(batch, armor, stateTime, loop, armorColor, actor, alpha);
        }

        if (equipment != null) {
            for (Equipment equip : equipment) {
                // TODO draw additional items
                if (equip.getRegion() == null) throw new AssertionError("Can't find equipment: " + equip.textureName);

//                System.out.println("drawing equipment: " + equip.name);

                drawItemTint(batch, equip.getRegion(), new Color(1, 1, 1, armorColor.a), actor, 1);
            }
        }
    }

    public static Color blend(Color c1, Color c2) {
//        c1 = new Color(c1);
//        c2 = new Color(c2);

        // This might only work for one color with alpha...
        // jst changed this to c2;
        c1.a = 1-c2.a;
        c1.premultiplyAlpha();
        c2.premultiplyAlpha();
        Color c3 = c1.add(c2);
//	    c3.a = 1;
        return c3;
    }

    private static void drawItem(Batch batch, TextureRegion textureRegion, Actor actor) {
        batch.draw(textureRegion, actor.getX(), actor.getY(), actor.getOriginX(), actor.getOriginY(), actor.getWidth(), actor.getHeight(), actor.getScaleX(), actor.getScaleY(), actor.getRotation());
    }
}
