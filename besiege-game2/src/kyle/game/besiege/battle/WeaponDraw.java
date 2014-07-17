package kyle.game.besiege.battle;

import kyle.game.besiege.Assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class WeaponDraw extends Actor {
	private float DEFAULT_OFFSET = 0;
	private float FIRST_OFFSET = 4;
	private float ATTACK_ROTATION = 30;
	private float ATTACK_OFFSET = 4;
	private float HORSE_OFFSET_X = .2f;
	private float HORSE_OFFSET_Y = -26;
	private float HORSE_SCALE;
	private float scale_x = 1.6f;
	private float scale_y = 2.5f;
	private Unit unit;
	private int offset_x = 12;
	private int offset_y = 4;
	private TextureRegion white;
	private TextureRegion toDraw;
	
	private Animation horseWalk;
//	private Animation hors
	
	public WeaponDraw(Unit unit) {
		this.unit = unit;
		
		String filename = "";
		
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
			filename = "shortbow";
			break;
			
		case CROSSBOW :
			filename = "crossbow";
			break;
		case RECURVE :
			filename = "recurve";
			break;
		case LONGBOW :
			filename = "longbow";
			break;
			
		case ADV_CROSSBOW :
			filename = "crossbow";
			break;
		case ADV_RECURVE :
			filename = "recurve";
			break;
		case ADV_LONGBOW :
			filename = "longbow";
			break;
		}
		
		this.toDraw = Assets.weapons.findRegion(filename);
//		this.toDraw = new TextureRegion(new Texture(Gdx.files.internal("weapons/axe")));
		white = new TextureRegion(new Texture("whitepixel.png"));
		
		if (unit.soldier.horse != null) {
			this.HORSE_SCALE = 5;
			Texture walkSheet2 = new Texture(Gdx.files.internal("equipment/horse_brown_walk.png")); 
			TextureRegion[][] textureArray2 = TextureRegion.split(walkSheet2, walkSheet2.getWidth()/2, walkSheet2.getHeight()/1);
			horseWalk = new Animation((float)Math.random()*.05f+.25f, textureArray2[0]);
			horseWalk.setPlayMode(Animation.LOOP);
		}
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {	
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
		if (horseWalk != null) {
			float time = unit.stateTime;
			if (!unit.moveSmooth) time = .3f;
			TextureRegion region = horseWalk.getKeyFrame(time, false);
			batch.draw(region, HORSE_OFFSET_X*unit.stage.scale, HORSE_OFFSET_Y*unit.stage.scale, 0, 0, toDraw.getRegionWidth(), toDraw.getRegionHeight(), unit.stage.scale*HORSE_SCALE, unit.stage.scale*HORSE_SCALE, 0);		
			batch.setColor(c);
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
		
		batch.draw(toDraw, offset_x*unit.stage.scale, (offset_y+man_offset_y/1.5f)*unit.stage.scale, 0, 0, toDraw.getRegionWidth(), toDraw.getRegionHeight(), unit.stage.scale*scale_x, unit.stage.scale*scale_y, man_rotation);		
	}
}