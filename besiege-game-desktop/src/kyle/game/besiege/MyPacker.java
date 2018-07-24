package kyle.game.besiege; /*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.tools.imagepacker.TexturePacker;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;

import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;

public class MyPacker {
        public static void main (String[] args) {

        	//TexturePacker2.process("C:/Users/kdhillon3/Documents/java/besiege/besiege-game-android/assets/weapons", "C:/Users/kdhillon3/Documents/java/besiege/besiege-game-android/assets", "weapons");
            System.out.println("packing");
            TexturePacker.Settings settings = new TexturePacker.Settings();
        	TexturePacker.process(settings,"C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets/textures","C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets", "atlas1");
            TexturePacker.process(settings,"C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets/weapons", "C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets", "weapons1");
            TexturePacker.process(settings,"C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets/units", "C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets", "units1");
            TexturePacker.process(settings,"C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets/map", "C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets", "map1");
            TexturePacker.process(settings,"C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets/equipment", "C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets", "equipment1");
            TexturePacker.process(settings,"C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets/crests", "C:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets", "crests1");
            System.out.println("done");
        }
}
