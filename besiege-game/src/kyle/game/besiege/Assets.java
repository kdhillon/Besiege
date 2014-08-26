/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
// contains all assets for this game

package kyle.game.besiege;

//import java.util.Scanner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

public class Assets {
	private static int cityCount = 20;
	private static int castleCount = 20;
	private static int villageCount = 40;
	public static Array<String> cityArray;
	public static Array<String> villageArray;
	public static Array<String> castleArray;
	//	public static Scanner cityList;
	//	public static Scanner villageList;
	public static TextureAtlas atlas;
	public static TextureAtlas weapons;
	public static TextureAtlas map;
	public static Texture map1;
	public static Texture map2;
	public static Texture map3;
	public static Texture map4;

	public static BitmapFont pixel12;
	public static BitmapFont pixel13neg;
	public static BitmapFont pixel14;
	public static BitmapFont pixel14neg;
	public static BitmapFont pixel15;
	public static BitmapFont pixel16;
	public static BitmapFont pixel16neg;
	public static BitmapFont pixel17;
	public static BitmapFont pixel18;
	public static BitmapFont pixel20;
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

	public static void load() {
		atlas = new TextureAtlas(Gdx.files.internal("atlas1.atlas"));
		weapons = new TextureAtlas(Gdx.files.internal("weapons.atlas"));
		//		map = new TextureAtlas(Gdx.files.internal("mapAtlas.atlas"));
		//		map1 = new Texture(Gdx.files.internal("map/Map2_1.PNG"));
		//		map2 = new Texture(Gdx.files.internal("map/Map2_2.PNG"));
		//		map3 = new Texture(Gdx.files.internal("map/Map2_3.PNG"));
		//		map4 = new Texture(Gdx.files.internal("map/Map2_4.PNG"));

		//		map2 = new Texture(Gdx.files.internal("map/Map2_1.PNG"));
		//		map3 = new Texture(Gdx.files.internal("map/Map2_1.PNG"));
		//		map4 = new Texture(Gdx.files.internal("map/Map2_1.PNG"));
	
		String[] cities = {
				"Catterick",
				"Colne",
				"Fotheringhay",
				"Hawarden",
				"Gloucester",
				"Furness",
				"Weobley",
				"Billinghame",
				"Portishead",
				"Oakham",
				"Warkworth",
				"Kidwelly",
				"Brading",
				"Hayton",
				"Neville",
				"Portsmouth",
				"Colchester",
				"Newstead",
				"Sheffield",
				"Salisbury",
				"Knaresborough",
				"Norham",
				"Brackley",
				"Elstow",
				"Bloxham",
				"Rhuddlan",
				"Dudley",
				"Scunthorpe",
				"Hexham",
				"Usk",
				"Bebington",
				"Wilton",
				"Minster",
				"Walpole",
				"Lewes",
				"Bamburgh",
				"Buildwas",
				"Pebmarsh",
				"Oxford",
				"Oxted"
		};

		cityArray = new Array<String>();
		for (int i = 0; i < cityCount; i++) 
			cityArray.add(cities[i]);

		String[] castlesVillages = {
				"Lewes",
				"Deal",
				"Bytham",
				"Ely",
				"Northhallterton",
				"Lacock",
				"Leiston",
				"Neath",
				"Ipswich",
				"Bushbury",
				"Holbeach",
				"Bodiam",
				"Runnymede",
				"Pebmarsh",
				"Rothley",
				"Folkingham",
				"Bamburgh",
				"Mold",
				"Milford",
				"Castor",
				"Ebbsfleet",
				"Egremount",
				"Bowes",
				"Radcliffe",
				"Drayton",
				"Brackley",
				"Peterborough",
				"Hay",
				"Axminster",
				"Avon",
				"Ilchester",
				"Tavistock",
				"Deanwy",
				"Stretton",
				"Cheltenham",
				"Dyserth",
				"Arundel",
				"Teignmouth",
				"Bangor",
				"Brecon",
				"Billinghame",
				"Bridport",
				"Barborough",
				"Romsey",
				"Alton",
				"Bridgewater",
				"Neath",
				"Hartlepool",
				"Crediton",
				"Ripley",
				"Spalding",
				"Bath",
				"Uffington",
				"Bardney",
				"Haddon",
				"Otterburn",
				"Huyton",
				"Starford",
				"Belvoir",
				"Walpole",
				"Oxford",
				"Deerhurst",
				"Tynemouth",
				"Newcastle",
				"Haverfordwest",
				"Selby",
				"Cambridge",
				"Swansea",
				"Albans",
				"Walsingham",
				"Baldock",
				"Hutton",
				"Wigmore",
				"Worthing",
				"Carlisle",
				"Elmham",
				"Oswestry",
				"Cricklade",
				"Evesham",

//				"Lewes2",
//				"Deal2",
//				"Bytham2",
//				"Ely2",
//				"Northhallterton2",
//				"Lacock2",
//				"Leiston2",
//				"Neath2",
//				"Ipswich2",
//				"Bushbury2",
//				"Holbeach2",
//				"Bodiam2",
//				"Runnymede2",
//				"Pebmarsh2",
//				"Rothley2",
//				"Folkingham2",
//				"Bamburgh2",
//				"Mold2",
//				"Milford2",
//				"Castor2",
//				"Ebbsfleet2",
//				"Egremount2",
//				"Bowes2",
//				"Radcliffe2",
//				"Drayton2",
//				"Brackley2",
//				"Peterborough2",
//				"Hay2",
//				"Axminster2",
//				"Avon2",
//				"Ilchester2",
//				"Tavistock2",
//				"Deanwy2",
//				"Stretton2",
//				"Cheltenham2",
//				"Dyserth2",
//				"Arundel2",
//				"Teignmouth2",
//				"Bangor2",
//				"Brecon2",
//				"Billinghame2",
//				"Bridport2",
//				"Barborough2",
//				"Romsey2",
//				"Alton2",
//				"Bridgewater2",
//				"Neath2",
//				"Hartlepool2",
//				"Crediton2",
//				"Ripley2",
//				"Spalding2",
//				"Bath2",
//				"Uffington2",
//				"Bardney2",
//				"Haddon2",
//				"Otterburn2",
//				"Huyton2",
//				"Starford2",
//				"Belvoir2",
//				"Walpole2",
//				"Oxford2",
//				"Deerhurst2",
//				"Tynemouth2",
//				"Newcastle2",
//				"Haverfordwest2",
//				"Selby2",
//				"Cambridge2",
//				"Swansea2",
//				"Albans2",
//				"Walsingham2",
//				"Baldock2",
//				"Hutton2",
//				"Wigmore2",
//				"Worthing2",
//				"Carlisle2",
//				"Elmham2",
//				"Oswestry2",
//				"Cricklade2",
//				"Evesham2",
				"Harcourt"
		};
		villageArray = new Array<String>();
		for (int i = 0; i < villageCount; i++) 
			villageArray.add(castlesVillages[i]);
		
		// go backwards in list for castles, starting from bottom
		castleArray = new Array<String>();
		for (int i = villageArray.size-1; i >= villageArray.size-1-castleCount; i--) {
			if (castlesVillages[i].length() < 8) {
				if (Math.random() < .7)
					castleArray.add(castlesVillages[i] + " Castle");
				else castleArray.add(castlesVillages[i] + " Fortress");
			}
		}

		//		cityList = new Scanner(Gdx.files.internal("mapSmall.txt").readString());
		//		cityList = new Scanner(Gdx.files.internal("map40.txt").readString());

		//		villageList = new Scanner(Gdx.files.internal("villages60.txt").readString());

		pixel12 = new BitmapFont(Gdx.files.internal("data/droid12.fnt"), false);
		pixel13neg = new BitmapFont(Gdx.files.internal("data/droid13neg.fnt"),false);
		pixel14 = new BitmapFont(Gdx.files.internal("data/droid14.fnt"), false);
		pixel14neg = new BitmapFont(Gdx.files.internal("data/droid14neg.fnt"), false);
		pixel15 = new BitmapFont(Gdx.files.internal("data/droid15.fnt"), false);
		pixel16 = new BitmapFont(Gdx.files.internal("data/droid16.fnt"), false);
		pixel16neg = new BitmapFont(Gdx.files.internal("data/droid16neg.fnt"), false);
		pixel17 = new BitmapFont(Gdx.files.internal("data/droid17.fnt"), false);
		pixel18 = new BitmapFont(Gdx.files.internal("data/droid18.fnt"), false);
		pixel20 = new BitmapFont(Gdx.files.internal("data/droid20.fnt"), false);
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

		//		Weapon.load();
	}

	public static void dispose() {
		atlas.dispose();
		weapons.dispose();
//		map.dispose();
	}
}
