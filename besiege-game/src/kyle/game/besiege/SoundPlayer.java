package kyle.game.besiege;

import static kyle.game.besiege.MapScreen.ZOOM_MAX;
import static kyle.game.besiege.MapScreen.ZOOM_MIN;


// Music inspo:
// https://www.youtube.com/watch?v=mJOTZwiCvDA&index=4&list=PL04AFFAFAADD767C4

// TODO make this a simple "directional" engine for battles.
public class SoundPlayer {
    public static float MASTER_VOLUME = .5f;

    public static void playThunder() {
        if (Math.random() < .5) {
            Assets.thunder1.play(MASTER_VOLUME * (float) (.2 + Math.random() * 0.5));
        } else Assets.thunder2.play(MASTER_VOLUME * (float) (.2 + Math.random() * 0.5));
    }

    public static void startRain() {
        Assets.rain.setLooping(true);
        Assets.rain.setVolume(MASTER_VOLUME * .3f);
        Assets.rain.play();
    }

    public static void stopRain() {
        Assets.rain.pause();
    }

    public static void setZoomBattle(float zoomBattle) {
        float zoomDif = (ZOOM_MAX / 2) - ZOOM_MIN;
        float soundFactor = ((zoomBattle - ZOOM_MIN) / zoomDif);
        Assets.rain.setVolume(MASTER_VOLUME * .3f * (1-soundFactor));
    }
}
