package kyle.game.besiege.battle;

import kyle.game.besiege.Assets;
import kyle.game.besiege.party.RangedWeapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class WeaponDraw extends Actor {
	private float DEFAULT_OFFSET = 0;
	private float FIRST_OFFSET = 2;
	private float ATTACK_ROTATION = 30;
	private float ATTACK_OFFSET = 2;
	private float HORSE_OFFSET_X = -2f;
	private float HORSE_OFFSET_Y = -25;
	private float HORSE_SCALE = 2.5f;
	private float SHIELD_SCALE = 2.0f;
	private float SHIELD_OFFSET_X = -2;
	private float SHIELD_OFFSET_Y = 8;
	private float scale_x = 1.6f;
	private float scale_y = 2.5f;
	private Unit unit;
	private int offset_x = 12;
	private int offset_y = 4;
	
	private float offset_x_ranged = 14;
	private float offset_y_ranged = -.5f;
	private TextureRegion white;
	private TextureRegion weaponMelee;
	private TextureRegion weaponRanged;
	
	public Animation horseWalk;
	public TextureRegion shield;
//	private Animation hors
	
	public WeaponDraw(Unit unit) {
		this.unit = unit;
		
		mapWeaponTextures();
		
//		this.toDraw = new TextureRegion(new Texture(Gdx.files.internal("weapons/axe")));
		white = new TextureRegion(new Texture("whitepixel.png"));
		
		if (unit.horse != null) {
			Texture walkSheet2 = new Texture(Gdx.files.internal("equipment/horse_brown_walk.png")); 
			TextureRegion[][] textureArray2 = TextureRegion.split(walkSheet2, walkSheet2.getWidth()/2, walkSheet2.getHeight()/1);
			horseWalk = new Animation((float)Math.random()*.05f+.25f, textureArray2[0]);
			horseWalk.setPlayMode(Animation.LOOP);
		}
		if (unit.shield != null) {
			shield = new TextureRegion(new Texture(Gdx.files.internal("equipment/iron_shield.png"))); 
		}
	}
	
	private void mapWeaponTextures() {
		String filename = "";
		String rangedFilename = "";
		switch(unit.weapon) {
		case PITCHFORK :
			filename = "pitchfork";
			break;
		case MILITARY_FORK : 
			filename = "militaryfork";
			break;
		case SPEAR :
			filename = "spear";
			break;
		case HATCHET :
			filename = "axe";
			break;
		case CLUB :
			filename = "club";
			break;
		case PIKE :
			filename = "pike";
			break;
		case HALBERD :
			filename = "halberd";
			break;
		case LONGSWORD :
			filename = "longsword";
			break;
		case BATTLE_AXE :
			filename = "battleaxe";
			break;
		case SHORTSWORD :
			filename = "shortsword";
			break;
		case WAR_HAMMER :
			filename = "warhammer";
			break;
		case MACE :
			filename = "mace";
			break;
		case CAVALRY_SPEAR :
			filename = "spear";
			break;
		case CAVALRY_AXE :
			filename = "axe";
			break;
		case CAVALRY_PICK :
			filename = "warhammer";
			break;
			
		case LANCE :
			filename = "lance";
			break;
		case ARMING_SWORD :
			filename = "shortsword";
			break;
		case FLAIL :
			filename = "morningstar";
			break;
		
		case GUISARME :
			filename = "guisarme";
			break;
		case VOULGE :
			filename = "voulge";
			break;
		case GREATSWORD :
			filename = "claymore";
			break;
		case GLAIVE :
			filename = "bardiche";
			break;
		case FALCHION :
			filename = "falchion";
			break;
		case MAUL :
			filename = "maul";
			break;
		case MORNINGSTAR :
			filename = "morningstar";
			break;
			
		// ranged
		case SHORTBOW :
			filename = "dagger";
			rangedFilename = "shortbow";
			break;
			
		case CROSSBOW :
			filename = "dagger";
			rangedFilename = "crossbow";
			break;
		case RECURVE :
			filename = "dagger";
			rangedFilename = "recurve";
			break;
		case LONGBOW :
			filename = "dagger";
			rangedFilename = "longbow";
			break;
			
		case ADV_CROSSBOW :
			filename = "shortsword";
			rangedFilename = "crossbow";
			break;
		case ADV_RECURVE :
			filename = "shortsword";
			rangedFilename = "recurve";
			break;
		case ADV_LONGBOW :
			filename = "shortsword";
			rangedFilename = "longbow";
			break;
		}
		
		if (rangedFilename != "") 
			this.weaponRanged = Assets.weapons.findRegion(rangedFilename);		
		this.weaponMelee = Assets.weapons.findRegion(filename);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {	
		if (!unit.inMap()) return;

		//this.toFront();
		this.toBack();
		Color c = new Color(batch.getColor());
		if (unit.team == 0)
			batch.setColor(1, 0, 0, .2f); 
		else batch.setColor(0, 0, 1, .2f);
		
		// draw white if selected
		if (this.unit == unit.stage.selectedUnit || this.unit.isHit) batch.setColor(1, 1, 1, .5f);
		
		batch.draw(white, 0, 0, unit.stage.scale*unit.stage.unit_width, unit.stage.scale*unit.stage.unit_height);
		batch.setColor(c);
		
		// draw horse
		if (unit.isMounted()) {
			float time = unit.stateTime;
			if (!unit.moveSmooth) time = .3f;
			TextureRegion region = horseWalk.getKeyFrame(time, false);
			batch.draw(region, HORSE_OFFSET_X*unit.stage.scale, HORSE_OFFSET_Y*unit.stage.scale, region.getRegionWidth()*unit.stage.scale*HORSE_SCALE, region.getRegionHeight()* unit.stage.scale*HORSE_SCALE);
		}
		
		// manual offset
		float man_offset_y = 0;
		float man_rotation = 0;
		
		if (unit.moving) {
			if (unit.animationWalk.getKeyFrameIndex(unit.stateTime) == 0)
				man_offset_y = DEFAULT_OFFSET;
			else
				man_offset_y = FIRST_OFFSET;
		}
		else if (unit.attacking != null) {
			if (unit.animationWalk.getKeyFrameIndex(unit.stateTime) == 1) {
				man_offset_y = ATTACK_OFFSET;
				man_rotation = ATTACK_ROTATION;
			}
			else
				man_offset_y = FIRST_OFFSET;
		}
		
		// draw shield
		if (shield != null && !unit.bowOut()) {
			float offset_to_use = FIRST_OFFSET;
			if (unit.animationWalk.getKeyFrameIndex(unit.stateTime) == 1 && !unit.stage.isOver) offset_to_use = DEFAULT_OFFSET;
			batch.draw(shield, SHIELD_OFFSET_X*unit.stage.scale, (SHIELD_OFFSET_Y+offset_to_use)*unit.stage.scale, shield.getRegionWidth()*unit.stage.scale*SHIELD_SCALE, shield.getRegionHeight()*unit.stage.scale*SHIELD_SCALE);		
		}
		
		float offset_x_to_use = offset_x;
		float offset_y_to_use = offset_y;
		TextureRegion toDraw = weaponMelee;
		if (unit.bowOut()) {
			toDraw = weaponRanged;
			if (unit.rangedWeapon != RangedWeapon.ADV_CROSSBOW  && unit.rangedWeapon != RangedWeapon.CROSSBOW) {
				offset_x_to_use = offset_x_ranged;
				offset_y_to_use = offset_y_ranged;
			}
			// don't draw if firing
			if (!unit.moveSmooth) return;
		}
		batch.draw(toDraw, offset_x_to_use*unit.stage.scale, (offset_y_to_use+man_offset_y/1.5f)*unit.stage.scale, 0, 0, weaponMelee.getRegionWidth(), weaponMelee.getRegionHeight(), unit.stage.scale*scale_x, unit.stage.scale*scale_y, man_rotation);		
	}
}