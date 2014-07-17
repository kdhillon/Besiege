//package kyle.game.besiege.battle;
//
//import kyle.game.besiege.BesiegeMain;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.graphics.g2d.TextureRegion;
//import com.badlogic.gdx.scenes.scene2d.Group;
//
//public class BattleMap extends Group {
//	TextureRegion map;
//	int size;
//	
//	public BattleMap(BattleStage bs) {
//		this.size = (int) (bs.size_x*bs.scale*bs.unit_width);
//		this.map = new TextureRegion(new Texture(Gdx.files.internal("gras.jpg")));
//	}
//	
//	public void draw(SpriteBatch batch, float parentAlpha) {
//		batch.draw(map, 0, 0, size, size);
//	}
//
//}
