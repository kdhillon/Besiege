package kyle.game.besiege.desktop; /*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class MyPacker {
        public static void main (String[] args) {

        	//TexturePacker2.process("C:/Users/kdhillon3/Documents/java/besiege/besiege-game-android/assets/weapons", "C:/Users/kdhillon3/Documents/java/besiege/besiege-game-android/assets", "weapons");
            System.out.println("packing");
            TexturePacker.Settings settings = new TexturePacker.Settings();

            String envVar = "PWD";
            String outputDir = System.getenv(envVar) + "-android/assets/";

        	TexturePacker.process(settings,System.getenv(envVar) + "-android/assets/textures",outputDir, "atlas1");
            TexturePacker.process(settings,System.getenv(envVar) + "-android/assets/weapons", outputDir, "weapons1");
            TexturePacker.process(settings,System.getenv(envVar) + "-android/assets/units", outputDir, "units1");
            TexturePacker.process(settings,System.getenv(envVar) + "-android/assets/map", outputDir, "map1");
            TexturePacker.process(settings,System.getenv(envVar) + "-android/assets/equipment", outputDir, "equipment1");
            TexturePacker.process(settings,System.getenv(envVar) + "-android/assets/crests", outputDir, "crests1");
            System.out.println("done");
        }
}
