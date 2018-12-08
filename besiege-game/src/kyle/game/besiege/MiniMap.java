/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

import kyle.game.besiege.battle.Unit;
import kyle.game.besiege.battle.WeaponDraw;
import kyle.game.besiege.panels.PanelUnit;
import kyle.game.besiege.panels.SidePanel;
import kyle.game.besiege.party.*;

public class MiniMap extends Actor {
//	private final float BORDER = .04f;
	private final float BORDER = .1f;
	private final Color skyBlue = new Color(135f/256,206/256f,250f/256, 1);

	private SidePanel panel;
	
	private TextureRegion unitArmor;
	private TextureRegion unitSkin;

	String currentArmorString = null;

	public MiniMap(SidePanel panel) {
		this.panel = panel;
		unitSkin = Assets.units.findRegion("skin-preview-3");
		if (unitSkin == null) throw new AssertionError();
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
//		this.setWidth(panel.getWidth()*(1-BORDER));
		this.setHeight(panel.getWidth()*(1-BORDER));
//		this.setWidth(getHeight()*2/3);
        this.setWidth(getHeight());
//		this.setPosition(panel.getWidth()*(BORDER/2) + getWidth()/4, (panel.getHeight()-this.getHeight())-panel.getWidth()*(BORDER/2));
        this.setPosition(panel.getWidth()*(BORDER/2), (panel.getHeight()-this.getHeight())-panel.getWidth()*(BORDER/2));
//		this.setOrigin(this.getWidth()/2, this.getWidth()/2);
//		this.setOrigin(0, 0);

//		this.setOrigin(panel.getOriginX() - getX(), panel.getOriginY() - getY()); // Buggy
		//this.setKingdomRotation(panel.getKingdomRotation());
//		batch.draw(Assets.map.findRegion("smallMap"), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getKingdomRotation());
		if (panel.getActiveCrest() != null) {
		    // TODO use "crestDraw"
            panel.getActiveCrest().defaultCrestDraw.setPosition(getX(), getY());
			panel.getActiveCrest().defaultCrestDraw.setSize(getWidth(), getHeight());
			panel.getActiveCrest().defaultCrestDraw.draw(batch, parentAlpha);

//			batch.draw(panel.getActiveCrest().defaultCrestDraw, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getKingdomRotation());
		}
		if (panel.getSecondCrest() != null)  {
			System.out.println("ignoring second crest");
//			batch.draw(panel.getSecondCrest(), getX()+getWidth()/2, getY(), getOriginX(), getOriginY(), getWidth()/2, getHeight(), 1, 1, getKingdomRotation());
		}

		/* draw unit preview */
		if (panel.getActiveCrest() == null && panel.getSoldierInstead() != null) {
			// first draw white background?
			Soldier toPreview = panel.getSoldierInstead();
			Unit unit = null; // Unit, in case the unit is actively in battle.
			if (panel.getActivePanel().getClass() == PanelUnit.class) {
                unit = ((PanelUnit) panel.getActivePanel()).getUnit();
            }

//			if (toPreview.party.getFaction() != null)
//				batch.setColor(skyBlue);
//			else batch.setColor(Color.WHITE);
            batch.setColor(toPreview.unitType.cultureType.colorLite);
			batch.draw(Assets.white, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
			batch.setColor(Color.WHITE);

			if (toPreview.getArmor().getPreviewTexture() != currentArmorString) {
                currentArmorString = toPreview.getArmor().getPreviewTexture();
                unitArmor = Assets.units.findRegion(currentArmorString);
                if (unitArmor == null) throw new AssertionError("Can't find : " + toPreview.getArmor().name);
                System.out.println("updating unit preview armor");
            }

            // hacky but should work? Set/unset unit width/height just for this section
            float unit_draw_size = 7/8f;
            float x_boost = getWidth() * 2f/16;

            // If the unit has headdress, make unit a bit smaller to fit
            StrictArray<Equipment> equipmentArrayList = toPreview.getEquipment();
            for (Equipment e : equipmentArrayList) {
                if (e.type == Equipment.Type.HEAD) {
                    unit_draw_size = 3f/4;
                    x_boost = getWidth() * 3f / 16;
                }
            }

            this.setWidth(unit_draw_size* this.getWidth());
            this.setHeight(unit_draw_size* this.getHeight());

			float rotation = getRotation();
			// center unit a bit, more if smaller

			float y = getY() + getWidth()*3f/8;

			// TODO speed this up by not looking this stuff up every frame
			TextureRegion weapon = WeaponDraw.GetMeleeWeaponTextureForUnittype(toPreview.unitType);
			boolean drawWeaponInFront = false;

			boolean drawingRangedWeapon = false;
			if (toPreview.unitType.ranged != null) {
				if (unit != null) {
					drawingRangedWeapon = unit.isFiring() || toPreview.unitType.melee.type == WeaponType.Type.UNARMED || toPreview.unitType.ranged.quiver > 2;
				} else {
					if (toPreview.unitType.ranged.quiver > 2)
						drawingRangedWeapon = true;
				}
			}
//			if (toPreview.unitType.ranged == null || (unit != null && !unit.rangedWeaponOut()) || toPreview.unitType.ranged.type == RangedWeaponType.Type.THROWN || toPreview.unitType.ranged.type == RangedWeaponType.Type.THROWN_AXE) {
//                drawingRangedWeapon = false;
//            }

            if (drawingRangedWeapon) {
				weapon = WeaponDraw.GetRangedWeaponTextureForUnittype(toPreview.unitType);

				if (toPreview.unitType.ranged.type == RangedWeaponType.Type.BOW) {
                    rotation = 180;
                    y -= getWidth() * 7f / 8;
                } else if (toPreview.unitType.ranged.type == RangedWeaponType.Type.SLING) {
				    y -= getWidth() * 1f / 8;
                } else {
                    y -= getWidth() * 2f / 8;
                }

//				drawInFront = true;
			}
			else if (toPreview.unitType.melee.isPolearm()) { //&& !toPreview.weapon.troopName.startsWith("Sword")
				y -= getWidth()*3f/8;
//				drawInFront = false;
			}

			if (weapon.getTexture() == null) {
				if (drawingRangedWeapon)
					throw new AssertionError(toPreview.unitType.ranged.name + " not found");
				else
					throw new AssertionError(toPreview.unitType.melee.name + " not found");
			}

			if (!drawWeaponInFront)
                drawWeapon(batch, weapon, x_boost, y, rotation);

            Color c = batch.getColor();
			batch.setColor(toPreview.skinColor);
			batch.draw(unitSkin, getX() + x_boost, getY(), getOriginX(), getOriginY(), getWidth(), getWidth(), 1, 1, getRotation());

			// TODO account for extra high armors (hoods)
            if (!toPreview.unitType.armor.isNaked()) {
                batch.setColor(toPreview.unitType.armor.getColorPreview());
                float height = getHeight();
                if (toPreview.unitType.armor.type == ArmorType.Type.HOODED)
                	height *= 9f/8;
                batch.draw(unitArmor, getX() + x_boost, getY(), getOriginX(), getOriginY(), getWidth(), height, 1, 1, getRotation());
            }

			batch.setColor(c);
			if (drawWeaponInFront) {
                // weapons are 3x12
                drawWeapon(batch, weapon, x_boost, y, rotation);
			}

			if (toPreview.unitType.shieldType != null) {
			    batch.setColor(toPreview.unitType.shieldType.color);
                batch.draw(toPreview.unitType.shieldType.getPreviewTexture(), getX() + x_boost, getY(), getOriginX(), getOriginY(), getWidth(), getWidth(), 1, 1, getRotation());
//                batch.draw(unitShield, getX() + getWidth()*10f/16 - getWidth()/8f, getY() + getWidth()*0f/8 + getWidth() * 4f/16, 0, 0, getWidth() * 4f/8, getWidth()*4f/8, 1, 1, 0);
			}

			// Draw headgear if present :)
            for (Equipment e : equipmentArrayList) {
			    if (e.type == Equipment.Type.HEAD) {
			        batch.setColor(Color.WHITE);
			        // draw headgear upside down
					if (Assets.equipment.findRegion(e.textureName) == null) throw new AssertionError("cant find equipment: " + e.textureName);
                    batch.draw(Assets.equipment.findRegion(e.textureName), getX() + x_boost, getY() + getWidth()*3/8f, getWidth() / 2, getWidth() / 2, getWidth(), getWidth(), 1, 1, getRotation() - 180);
                }
            }

			// reset width/height
            this.setWidth(this.getWidth()/unit_draw_size);
            this.setHeight(this.getHeight()/unit_draw_size);
		}
	}
    private void drawWeapon(SpriteBatch batch, TextureRegion weapon, float x_boost, float y, float rotation) {
		if (weapon == null) throw new AssertionError();
        batch.draw(weapon, getX() - getWidth() / 8 + x_boost, y, getWidth() * 3f / 16, getWidth() * 12f / 16, getWidth() * 3f / 8, getWidth() * 12f / 8, 1, 1, rotation);
    }
}
