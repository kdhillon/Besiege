/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
package kyle.game.besiege;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

import kyle.game.besiege.battle.WeaponDraw;
import kyle.game.besiege.panels.SidePanel;
import kyle.game.besiege.party.Equipment;
import kyle.game.besiege.party.Soldier;

import java.util.ArrayList;

public class MiniMap extends Actor {
//	private final float BORDER = .04f;
	private final float BORDER = .1f;
	private final Color skyBlue = new Color(135f/256,206/256f,250f/256, 1);

	private SidePanel panel;
	
	private TextureRegion unitArmor;
	private TextureRegion unitSkin;

	public MiniMap(SidePanel panel) {
		this.panel = panel;
		unitArmor = Assets.units.findRegion("preview-armor");
		unitSkin = Assets.units.findRegion("skin-preview-2");
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
		//this.setRotation(panel.getRotation());
//		batch.draw(Assets.map.findRegion("smallMap"), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
		if (panel.getActiveCrest() != null) {
            panel.getActiveCrest().setPosition(getX(), getY());
			panel.getActiveCrest().setSize(getWidth(), getHeight());
			panel.getActiveCrest().draw(batch, parentAlpha);
//			batch.draw(panel.getActiveCrest(), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
		}
		if (panel.getSecondCrest() != null)  {
			System.out.println("ignoring second crest");
//			batch.draw(panel.getSecondCrest(), getX()+getWidth()/2, getY(), getOriginX(), getOriginY(), getWidth()/2, getHeight(), 1, 1, getRotation());
		}

		/* draw unit preview */
		if (panel.getActiveCrest() == null && panel.getSoldierInstead() != null) {
			// first draw white background?
			Soldier toPreview = panel.getSoldierInstead();

//			if (toPreview.party.getFaction() != null)
//				batch.setColor(skyBlue);
//			else batch.setColor(Color.WHITE);
            batch.setColor(toPreview.unitType.cultureType.colorLite);
			batch.draw(Assets.white, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
			batch.setColor(Color.WHITE);

            // hacky but should work? Set/unset unit width/height just for this section
            float unit_draw_size = 3f/4;
            this.setWidth(unit_draw_size* this.getWidth());
            this.setHeight(unit_draw_size* this.getHeight());

			float rotation = getRotation();
			// center unit a bit.
			float x_boost = getWidth()* 2f/8;

			float y = getY() + getWidth()*3f/8;

			// TODO speed this up by not looking this stuff up every frame
			TextureRegion weapon = WeaponDraw.GetMeleeWeaponTextureForUnittype(toPreview.unitType);
			boolean drawWeaponInFront = false;
			if (toPreview.unitType.ranged != null) {
				weapon = WeaponDraw.GetRangedWeaponTextureForUnittype(toPreview.unitType);
				rotation = 180;
				y -= getWidth()*8f/8;
//				drawInFront = true;
			}
			else if (toPreview.unitType.melee.isPolearm()) { //&& !toPreview.weapon.troopName.startsWith("Sword")
				y -= getWidth()*3f/8;
//				drawInFront = false;
			}

			if (!drawWeaponInFront)
                drawWeapon(batch, weapon, x_boost, y, rotation);

            Color c = batch.getColor();
			batch.setColor(toPreview.skinColor);
			batch.draw(unitSkin, getX() + x_boost, getY(), getOriginX(), getOriginY(), getWidth(), getWidth(), 1, 1, getRotation());

            if (!toPreview.unitType.armor.isNaked()) {
                batch.setColor(toPreview.unitType.armor.color);
                batch.draw(unitArmor, getX() + x_boost, getY(), getOriginX(), getOriginY(), getWidth(), getWidth(), 1, 1, getRotation());
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
            StrictArray<Equipment> equipmentArrayList = toPreview.getEquipment();
            for (Equipment e : equipmentArrayList) {
			    if (e.type == Equipment.Type.HEAD) {
			        batch.setColor(Color.WHITE);
			        // draw headgear upside down
                    batch.draw(Assets.equipment.findRegion(e.textureName), getX() + x_boost, getY() + getWidth()*3/8f, getWidth() / 2, getWidth() / 2, getWidth(), getWidth(), 1, 1, getRotation() - 180);
                }
            }

			// reset width/height
            this.setWidth(this.getWidth()/unit_draw_size);
            this.setHeight(this.getHeight()/unit_draw_size);
		}
	}
    private void drawWeapon(SpriteBatch batch, TextureRegion weapon, float x_boost, float y, float rotation) {
        batch.draw(weapon, getX() - getWidth() / 8 + x_boost, y, getWidth() * 3f / 16, getWidth() * 12f / 16, getWidth() * 3f / 8, getWidth() * 12f / 8, 1, 1, rotation);
    }
}
