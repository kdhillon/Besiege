/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
// contains all assets for this game

package kyle.game.besiege;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import kyle.game.besiege.party.UnitLoader;

public class Assets {
	public static TextureAtlas atlas;
	public static TextureAtlas weapons;
	public static TextureAtlas map;
	public static TextureAtlas units;
	public static TextureAtlas equipment;
    public static TextureAtlas crests;

    public static Texture black;

	public static FreeTypeFontGenerator droidGen;
	
	public static BitmapFont pixel12;
	public static BitmapFont pixel13neg;
	public static BitmapFont pixel14;
	public static BitmapFont pixel14neg;
	public static BitmapFont pixel15;
	public static BitmapFont pixel16;
	public static BitmapFont pixel16neg;
	public static BitmapFont pixel17;
	public static BitmapFont pixel18;
	public static BitmapFont pixel20forCities;
	public static BitmapFont pixel22;
	public static BitmapFont pixel24;
	public static BitmapFont pixel30;
	public static BitmapFont pixel40;
	public static BitmapFont pixel50;
	public static BitmapFont pixel64;
	public static BitmapFont pixel80;
	public static BitmapFont pixel100;
	public static BitmapFont pixel128;
	public static BitmapFont pixel150;
	public static BitmapFont pixel200;
	public static BitmapFont pixel256;
	
	public static Music rain;
	public static Music music1;
	public static Music music2;
	public static Music forestMusic;

	public static Sound thunder1;
	public static Sound thunder2;
	public static Sound thunder3;
	public static Sound thunder4;
	
	public static TextureRegion white;

	private static final int r = 3;
	private static final String tablePatch = "grey-d9";
	public static Drawable ninepatchBackgroundDarkGray;
	public static Drawable ninepatchBackgroundGray;
	public static Drawable ninepatchBackgroundLightGray;
	public static Drawable ninepatchBackgroundGrayTrans;

	// TODO move everything into a subfolder for cleanliness
	public static final String subfolderName = "data/";

	public static void load() {
		atlas = new TextureAtlas(Gdx.files.internal("atlas1.atlas"));
		weapons = new TextureAtlas(Gdx.files.internal("weapons1.atlas"));


		units = new TextureAtlas(Gdx.files.internal("units1.atlas"));
		map = new TextureAtlas(Gdx.files.internal("map1.atlas"));
		equipment = new TextureAtlas(Gdx.files.internal("equipment1.atlas"));
        crests = new TextureAtlas(Gdx.files.internal("crests1.atlas"));

        white = new TextureRegion(new Texture("whitepixel.png"));
		black = new Texture("black.png");

		ninepatchBackgroundDarkGray = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion("grey-d9"), r,r,r,r));
		ninepatchBackgroundGray = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion("grey-dm9"), r,r,r,r));
		ninepatchBackgroundLightGray = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion("grey-lm9"), r,r,r,r));
		ninepatchBackgroundGrayTrans = new NinePatchDrawable(new NinePatch(Assets.atlas.findRegion("grey-med-trans"), 1,1,1,1));

		// load units
		UnitLoader.load("chieftain");

		rain = Gdx.audio.newMusic(Gdx.files.internal("sound/rain1.mp3"));
		music1 = Gdx.audio.newMusic(Gdx.files.internal("sound/music1.wav"));
		music2 = Gdx.audio.newMusic(Gdx.files.internal("sound/music2.wav"));
		forestMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/forest.wav"));

		thunder1 = Gdx.audio.newSound(Gdx.files.internal("sound/thunder1.wav"));
		thunder2 = Gdx.audio.newSound(Gdx.files.internal("sound/thunder2.wav"));

		pixel12 = new BitmapFont(Gdx.files.internal("data/droid12.fnt"), false);
		pixel13neg = new BitmapFont(Gdx.files.internal("data/droid13neg.fnt"),false);
		pixel14 = new BitmapFont(Gdx.files.internal("data/droid14.fnt"), false);
		pixel14neg = new BitmapFont(Gdx.files.internal("data/droid14neg.fnt"), false);
		pixel15 = new BitmapFont(Gdx.files.internal("data/droid15.fnt"), false);
		pixel16 = new BitmapFont(Gdx.files.internal("data/droid16.fnt"), false);
		pixel16neg = new BitmapFont(Gdx.files.internal("data/droid16neg.fnt"), false);
		pixel17 = new BitmapFont(Gdx.files.internal("data/droid17.fnt"), false);
		pixel18 = new BitmapFont(Gdx.files.internal("data/droid18.fnt"), false);
		pixel20forCities = new BitmapFont(Gdx.files.internal("data/droid20.fnt"), false);
		pixel22 = new BitmapFont(Gdx.files.internal("data/droid22.fnt"), false);
		pixel24 = new BitmapFont(Gdx.files.internal("data/droid24neg.fnt"), false);
		pixel30 = new BitmapFont(Gdx.files.internal("data/droid30.fnt"), false);
		pixel40 = new BitmapFont(Gdx.files.internal("data/droid40.fnt"), false);
		pixel50 = new BitmapFont(Gdx.files.internal("data/droid50.fnt"), false);
		pixel64 = new BitmapFont(Gdx.files.internal("data/droid64.fnt"), false);
		pixel80 = new BitmapFont(Gdx.files.internal("data/droid80.fnt"), false);
		pixel100 = new BitmapFont(Gdx.files.internal("data/droid100.fnt"), false);
		pixel128 = new BitmapFont(Gdx.files.internal("data/droid128.fnt"), false);
		pixel150 = new BitmapFont(Gdx.files.internal("data/droid150.fnt"), false);
		pixel200 = new BitmapFont(Gdx.files.internal("data/droid200.fnt"), false);
		pixel256 = new BitmapFont(Gdx.files.internal("data/droid256.fnt"), false);

		// can't use this until all of gdx is updated probably... todo later
//		droidGen = new FreeTypeFontGenerator(Gdx.files.internal("data/droid100.fnt"));
//		FreeTypeFontParameter p = new FreeTypeFontParameter();
//		p.size = 128;
//		p.borderColor = Color.BLACK;
//		p.color = Color.RED;
//		pixel128 = droidGen.generateFont(p);
		
		//		Weapon.load();
	}

	public static void dispose() {
		atlas.dispose();
		weapons.dispose();
		rain.dispose();
		thunder1.dispose();
		thunder2.dispose();
//		thunder3.dispose();
//		thunder4.dispose();
	}
}
