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
import kyle.game.besiege.party.Soldier;

public class MiniMap extends Actor {
//	private final float BORDER = .04f;
	private final float BORDER = .1f;

	private SidePanel panel;
	
	private TextureRegion unitArmor;
	private TextureRegion unitSkin;
	
	private TextureRegion unitShield;
	
	public MiniMap(SidePanel panel) {
		this.panel = panel;
		unitArmor = Assets.units.findRegion("preview_armor");
		unitSkin = Assets.units.findRegion("preview_skin");
		unitShield = new TextureRegion(new Texture(Gdx.files.internal("equipment/shield_side_wd.png"))); 
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
//		this.setWidth(panel.getWidth()*(1-BORDER));
		this.setHeight(panel.getWidth()*(1-BORDER));
		this.setWidth(getHeight()*2/3);
		this.setPosition(panel.getWidth()*(BORDER/2) + getWidth()/4, (panel.getHeight()-this.getHeight())-panel.getWidth()*(BORDER/2));
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

			if (toPreview.party.getFaction() != null)
				batch.setColor(toPreview.party.getFaction().color);
			else batch.setColor(Color.WHITE);
			batch.draw(Assets.white, getX() - getWidth() / 8, getY(), getOriginX(), getOriginY(), getWidth() + getWidth() / 8, getWidth() +  getWidth() * 3f/ 8, 1, 1, getRotation());
			batch.setColor(Color.WHITE);

			
			float rotation = getRotation();
			float y = getY() + getWidth()*3f/8;
			
			// TODO speed this up
			TextureRegion weapon = WeaponDraw.GetMeleeWeaponTextureForUnittype(toPreview.unitType);
			boolean drawInFront = false;
			if (toPreview.unitType.ranged != null) {
				weapon = WeaponDraw.GetRangedWeaponTextureForUnittype(toPreview.unitType);
				rotation = 180;
				y -= getWidth()*8f/8;
//				drawInFront = true;
			}
			else if (toPreview.unitType.melee.polearm) { //&& !toPreview.weapon.troopName.startsWith("Sword")
				y -= getWidth()*3f/8;
//				drawInFront = false;
			}
			
			// weapons are 3x12			
			Color c = batch.getColor();
			batch.setColor(toPreview.unitType.armor.color);
			batch.draw(unitArmor, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getWidth(), 1, 1, getRotation());

			batch.setColor(c);
			if (!drawInFront) 
				batch.draw(weapon, getX() - getWidth()/8f, y, getWidth() * 3f/16, getWidth()*12f/16, getWidth() * 3f/8, getWidth()*12f/8, 1, 1, rotation);	
			
			batch.setColor(toPreview.skinColor);
			batch.draw(unitSkin, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getWidth(), 1, 1, getRotation());	
	
			batch.setColor(c);
			if (drawInFront) {
				batch.draw(weapon, getX() - getWidth()/8f, y, getWidth() * 3f/16, getWidth()*12f/16, getWidth() * 3f/8, getWidth()*12f/8, 1, 1, rotation);		
			}
			
			if (toPreview.unitType.shield) {
				batch.draw(unitShield, getX() + getWidth()*10f/16 - getWidth()/8f, getY() + getWidth()*0f/8 + getWidth() * 4f/16, 0, 0, getWidth() * 4f/8, getWidth()*4f/8, 1, 1, 0);		
			}
		}
	}
}
