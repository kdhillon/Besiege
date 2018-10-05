package kyle.game.besiege;

import kyle.game.besiege.utils.TextGenerator;

import java.io.FileNotFoundException;

public class NameGenerator {
//	private static final String citiesFile = "data/namegen/cities15000.txt";
//	private static final String castlesFile = "data/namegen/cities15000.txt";
//	private static final String villagesFile = "data/namegen/cities15000.txt";
//
////	private static final String firstMaleFile = "data/namegen/firstMale.txt";
//	private static final String firstMaleFile = "data/namegen/inuit.txt";
//	private static final String firstFemaleFile = "data/namegen/inuit.txt";
//	private static final String lastFile = "data/namegen/inuit.txt";

//	private static TextGenerator tgCities;
//	private static TextGenerator tgCastles;
//	private static TextGenerator tgVillages;
//
//	private static TextGenerator tgFirstMale;
//	private static TextGenerator tgFirstFemale;
//	private static TextGenerator tgLast;

//    private String PATH = "data/namegen/";
    private String PATH = "";
//    private String ABSOLUTE = "/Users/kdhillon/Documents/repo/besiege/besiege-game-android/assets/data/namegen/";
    private String relativePath = "data/namegen/";

    private TextGenerator cityGen;
    private TextGenerator castleGen;
    private TextGenerator villageGen;

    private TextGenerator firstMaleGen;
    private TextGenerator firstFemaleGen;
    private TextGenerator lastGen;

	public NameGenerator(String fileName) {
        this(fileName, fileName, fileName, fileName);
	}

	public NameGenerator(String firstMaleFileName, String firstFemaleFileName, String lastFileName, String cityFileName) {
        try {
            System.out.println("Creating namegen for: " + firstMaleFileName);

            firstMaleGen = new TextGenerator(3, relativePath,PATH + firstMaleFileName);
            firstFemaleGen = new TextGenerator(3, relativePath, PATH + firstFemaleFileName);
            lastGen = new TextGenerator(3, relativePath, PATH + lastFileName);

            cityGen = new TextGenerator(4, relativePath, PATH + cityFileName);
            castleGen = new TextGenerator(4, relativePath, PATH + cityFileName);
            villageGen = new TextGenerator(4, relativePath,PATH + cityFileName);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
	
//	public static void init() {
//		tgCities =		new TextGenerator(4, citiesFile);
//		tgCastles = 	new TextGenerator(4, castlesFile);
//		tgVillages = 	new TextGenerator(4, villagesFile);
//
//		tgFirstMale =	new TextGenerator(3, firstMaleFile);
//		tgFirstFemale = new TextGenerator(3, firstFemaleFile);
//		tgLast = 		new TextGenerator(3, lastFile);
//	}
	
	public static String lowercase(String input) {
		String[] split = input.split(" ");
		for (int i = 0; i < split.length; i++) {
			String suffix = split[i].substring(1, split[i].length());
			split[i] = split[i].charAt(0) + suffix.toLowerCase();
		}
		String combined = "";
		for (String s : split) {
			combined += s;
			combined += " ";
		}
		combined = combined.substring(0, combined.length()-1);

		return combined.trim();
	}

    public String generateCity() {
        return lowercase(cityGen.gen(6, 12, 4));
    }

    public String generateCastle() {
        String base = lowercase(castleGen.gen(6, 8, 4));
        if (Math.random() < .7)
            base += " Citadel";
        else if (Math.random() < 0.7)
            base += " Fortress";
        else
            base += " Stronghold";
        return base;
    }

    public String generateRuins() {
        String base = lowercase(castleGen.gen(6, 8, 4));
        return base + " Ruins";
    }

    public String generateVillage() {
        return lowercase(villageGen.gen(6, 8, 4));
    }

    public String generateFirstNameMale() {
	    // prev 5 10 5
        return  lowercase(firstMaleGen.gen(4, 8, 4));
    }

    public String generateFirstNameFemale() {
	    // prev 5 10 5
        return  lowercase(firstFemaleGen.gen(4, 8, 4));
    }

    public String generateLastName() {
        return  lowercase(lastGen.gen(4, 8, 4));
    }

    public String generateFactionName() {
        return  lowercase(lastGen.gen(5, 8, 5));
    }
	
//	public static String generateCity() {
//		return lowercase(tgCities.gen(6, 12, 4));
//	}
//
//	public static String generateCastle() {
//		String base = lowercase(tgCastles.gen(6, 8, 4));
//		if (Math.random() < .7)
//			base += " Castle";
//		else if (Math.random() < 0.7)
//			base += " Fortress";
//		else
//			base += " Stronghold";
//		return base;
//	}
//
//	public static String generateRuins() {
//		String base = lowercase(tgCastles.gen(6, 8, 4));
//		return base + " Ruins";
//	}
//
//	public static String generateVillage() {
//		return lowercase(tgVillages.gen(6, 12, 4));
//	}
//
//
//	public static String generateFirstNameMale() {
//		return  lowercase(tgFirstMale.gen(5, 10, 5));
//	}
//
//	public static String generateFirstNameFemale() {
//		return  lowercase(tgFirstFemale.gen(5, 10, 5));
//	}
//
//	public static String generateLastName() {
//		return  lowercase(tgLast.gen(5, 10, 5));
//	}
//
//	public static String generateFactionName() {
//		return  lowercase(tgLast.gen(5, 8, 5));
//	}
	
	public static void main(String[] args) {
//		init();
//		System.out.println(generateFirstNameMale());
//		System.out.println(generateFirstNameFemale());
//		System.out.println(generateLastName());
	}
}
