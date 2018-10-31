package kyle.game.besiege.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;

import kyle.game.besiege.Assets;
import kyle.game.besiege.party.UnitType;
import kyle.game.besiege.party.WeaponType;

public class WeaponDraw extends Group { // can have arrows.
    private static final float TINT_ALPHA = 0.2f;
    private static final Color redTint = new Color(1, 0.3f, 0, TINT_ALPHA);
    private static final Color redRootSubpartyTint = new Color(    1, 0.7f, 0, TINT_ALPHA);

    private static final Color redAllyTint = new Color(1, .5f, .5f, TINT_ALPHA);
    private static final Color redGeneralTint = new Color(1, 0.7f, 0, TINT_ALPHA);
    private static final Color blueTint = new Color(    .3f, .3f, 1, TINT_ALPHA);
    private static final Color blueRootSubpartyTint = new Color(    .3f, .7f, 1, TINT_ALPHA);
    private static final Color blueGeneralTint = new Color(0, 0.7f, 1, TINT_ALPHA);

	private static final float DEFAULT_OFFSET = 0;
	private static final float FIRST_OFFSET = 2;
	private static final float ATTACK_ROTATION = 30;
	private static final float ATTACK_OFFSET = 2;
	private static final float HORSE_OFFSET_X = -2f;
	private static final float HORSE_OFFSET_Y = -25;
	private static final float HORSE_SCALE = 2.5f;
	private static final float SHIELD_SCALE = 2.0f;
	private static final float SHIELD_OFFSET_X = -2;
	private static final float SHIELD_OFFSET_Y = 8;
	private static final float scale_x = 1.6f;
	private static final float scale_y = 2f;
	private static final int offset_x = 12;
	private static final int offset_y = 4;

    private Unit unit;

    public float shieldOffset;
	
	private static final float offset_x_ranged = 14;
	private static final float offset_y_ranged = -.1f; // .5 before

    private TextureRegion weaponMelee;
	private TextureRegion weaponRanged;
	
	private Animation horseWalk;
	public TextureRegion shield;
	public Color shieldColor;

	private Color c = new Color();
	
	public static TextureRegion shadowTexture = Assets.units.findRegion("shadow");

	public WeaponDraw(Unit unit) {
		this.unit = unit;
		
		mapWeaponTextures();
		
//		this.toDraw = new TextureRegion(new Texture(Gdx.files.internal("weapons/axe")));
		
		if (unit.horse != null) {
			Texture walkSheet2 = new Texture(Gdx.files.internal("equipment/horse-brown-walk.png"));
			TextureRegion[][] textureArray2 = TextureRegion.split(walkSheet2, walkSheet2.getWidth()/2, walkSheet2.getHeight()/1);
			horseWalk = new Animation((float)Math.random()*.05f+.25f, textureArray2[0]);
			horseWalk.setPlayMode(Animation.LOOP);
		}
		if (unit.shield != null) {
			shield = unit.shield.getTexture();
			if (shield == null) throw new AssertionError(unit.shield.name + " not found");
			shieldColor = unit.shield.color;
		}
	}
	
	public static TextureRegion GetMeleeWeaponTextureForUnittype(UnitType unit) {
		if (unit.melee.type == WeaponType.Type.UNARMED) {
			return new TextureRegion();
		}
		String filename = unit.melee.texture;
		TextureRegion weaponMeleeTexture;
		weaponMeleeTexture = Assets.weapons.findRegion(filename);
		
		// check last word of weapon because yolo
//		if (weaponMeleeTexture == null) {
//
//			String[] split = filename.split(" ");
//			if (split.length > 0) {
//                weaponMeleeTexture = Assets.weapons.findRegion(split[split.length - 1]);
//                if (weaponMeleeTexture == null) {
//                    weaponMeleeTexture = Assets.weapons.findRegion(split[split.length-1].toLowerCase());
//                }
//			}
//		}

		if (weaponMeleeTexture == null) {
			System.out.println("can't find texture for melee weapon: " + filename);
			throw new AssertionError();
//			weaponMeleeTexture = Assets.weapons.findRegion("shortsword");
		}
		return weaponMeleeTexture;
	}
	
	public static TextureRegion GetRangedWeaponTextureForUnittype(UnitType unit) {
		String rangedFilename = unit.ranged.texture;

		TextureRegion weaponRangedTexture = Assets.weapons.findRegion(rangedFilename);
		
		if (weaponRangedTexture == null) {
			System.out.println("can't find texture for ranged weapon: " + rangedFilename);
			throw new AssertionError();
//			weaponRangedTexture = Assets.weapons.findRegion("Longbow");
		}
		return weaponRangedTexture;
	}
	
	private void mapWeaponTextures() {
		if (unit.isRanged()) {
		    System.out.println("Setting ranged weapon: " + unit.soldier.unitType.ranged.name);
			this.weaponRanged = GetRangedWeaponTextureForUnittype(unit.soldier.unitType);
			if (weaponRanged == null) throw new AssertionError();
		}

		this.weaponMelee = GetMeleeWeaponTextureForUnittype(unit.soldier.unitType);
		if (weaponMelee == null) throw new AssertionError();
	}
	
	public void drawShadow(SpriteBatch batch, float x, float y, float width, float height) {
		Color o = batch.getColor();
		width *= 0.8f;
		height *= 0.8f;
		batch.setColor(BattleMap.SHADOW_COLOR);

		batch.draw(shadowTexture, x, y + height * 0.3f, width/2, height * 0.2f, width, height, 1, unit.stage.battlemap.sunStretch, unit.stage.battlemap.sunRotation - this.getParent().getRotation());
		batch.setColor(o);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {	
		super.draw(batch, parentAlpha);
		if (!unit.inMap()) return;
		if (unit.isHidden() && unit.team != 0) return;

		// draw shadow
		drawShadow(batch, 0, 0, unit.getWidth() * unit.getScaleX(), unit.getHeight() * unit.getScaleY());

		//this.toFront();

		float alpha = 0.2f;
		this.toBack();
		
		c.set(batch.getColor());
		if (unit.team == 0) {
			if (unit.party == unit.stage.allies.first()) {
                if (unit.bsp.isRoot()) {
                    batch.setColor(blueRootSubpartyTint);
                } else batch.setColor(blueTint);
			}
			else batch.setColor(blueTint);
		}
		else {
		    if (unit.bsp.isRoot()) {
		        batch.setColor(redRootSubpartyTint);
            } else
		        batch.setColor(redTint);
        }

		if (this.unit.isGeneral()) {
			if (unit.team == 0)
				batch.setColor(blueGeneralTint);
			else
				batch.setColor(redGeneralTint);
		}

		// draw white if selected
		if (this.unit == unit.stage.selectedUnit || this.unit.isHit) {
				batch.setColor(1, 1, 1, alpha);
		}

		boolean drawTeams = true;
		boolean onlyDrawFriendly = false;

		if (drawTeams || (onlyDrawFriendly && unit.team == 0))
			batch.draw(Assets.white, 0, 0, unit.stage.unit_width/2, unit.stage.unit_height/2, unit.stage.unit_width, unit.stage.unit_height, 1, 1, -this.getParent().getRotation());
//		batch.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, kingdomRotation);

		// draw horse
		if (unit.isMounted()) {
			float time = unit.stateTime;
			if (!unit.moveSmooth) time = .3f;
			TextureRegion region = horseWalk.getKeyFrame(time, false);
			batch.draw(region, HORSE_OFFSET_X, HORSE_OFFSET_Y, region.getRegionWidth()*HORSE_SCALE, region.getRegionHeight()* HORSE_SCALE);
		}
		
		// manual offset
		float man_offset_y = 0;
		float man_rotation = 0;
		
		if (unit.moving) {
			if (unit.unitDraw.walkArmor.getKeyFrameIndex(unit.stateTime) == 0)
				man_offset_y = DEFAULT_OFFSET;
			else
				man_offset_y = FIRST_OFFSET;
		}
		else if (unit.attacking != null) {
			if (unit.unitDraw.walkArmor.getKeyFrameIndex(unit.stateTime) == 1) {
				man_offset_y = ATTACK_OFFSET;
				man_rotation = ATTACK_ROTATION;
			}
			else
				man_offset_y = FIRST_OFFSET;
		}
		
		// draw shield
		if (shield != null && !unit.rangedWeaponOut()) {
			shieldOffset = FIRST_OFFSET;
			if (unit.unitDraw.walkArmor.getKeyFrameIndex(unit.stateTime) == 1 && !unit.stage.isOver && (unit.moveSmooth || unit.attacking != null)) shieldOffset = DEFAULT_OFFSET;

			batch.setColor(shieldColor);
			batch.draw(shield, SHIELD_OFFSET_X, (SHIELD_OFFSET_Y+shieldOffset), shield.getRegionWidth()*SHIELD_SCALE, shield.getRegionHeight()*SHIELD_SCALE);
		}

		float offset_x_to_use = offset_x;
		float offset_y_to_use = offset_y;
		TextureRegion toDraw = weaponMelee;

        batch.setColor(c);

        if (unit.rangedWeaponOut()) { //  || unit.isFiring()
			toDraw = weaponRanged;
//			if (unit.rangedWeapon != RangedWeapon.ADV_CROSSBOW  && unit.rangedWeapon != RangedWeapon.CROSSBOW) {
				offset_x_to_use = offset_x_ranged;
				offset_y_to_use = offset_y_ranged;
//			}
            // TODO this needs to be drawn at the same time as Unit (not WeaponDraw)
			// don't draw if firing
			if (unit.isFiring()) {
                // Draw  ammo on top
                if (unit.drawAmmo && unit.unitDraw.isDrawingRangedLoadedAnimation()) {
                    float ammoRotation = getRotation();
                    if (unit.drawAmmoReversed)
                        ammoRotation = ammoRotation + 180;
                    batch.draw(
                            unit.ammoType.getRegion(),
                            getX() + unit.getWidth() * 6 / 8,
                            getY() + unit.getHeight() * 2 / 8,
                            unit.ammoType.getRegion().getRegionWidth() / 2,
                            unit.ammoType.getRegion().getRegionHeight() / 2,
                            unit.ammoType.getRegion().getRegionWidth(),
                            unit.ammoType.getRegion().getRegionHeight(),
                            Projectile.getDefaultSmallScale() * 4,
                            Projectile.getDefaultSmallScale() * 4,
                            ammoRotation);

                }
			    return;
            }
		}
		if (toDraw == weaponMelee && unit.soldier.unitType.melee.type == WeaponType.Type.UNARMED)
			return;

		if (weaponMelee == null) throw new AssertionError();
		batch.draw(toDraw, offset_x_to_use, (offset_y_to_use+man_offset_y/1.5f), 0, 0, weaponMelee.getRegionWidth(), weaponMelee.getRegionHeight(), scale_x, scale_y, man_rotation);		
	}
}