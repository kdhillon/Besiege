/*******************************************************************************
 * Besiege
 * by Kyle Dhillon
 * Source Code available under a read-only license. Do not copy, modify, or distribute.
 ******************************************************************************/
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
public class MyPacker {
        public static void main (String[] args) throws Exception {

        	//TexturePacker2.process("C:/Users/kdhillon3/Documents/java/besiege/besiege-game-android/assets/weapons", "C:/Users/kdhillon3/Documents/java/besiege/besiege-game-android/assets", "weapons");
 
        	TexturePacker2.process("A:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets/textures","A:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets", "atlas1");
            TexturePacker2.process("A:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets/weapons", "A:/Users/Kyle/Dropbox/LibGDX/repo/besiege/besiege-game-android/assets", "weapons");
        }
}
