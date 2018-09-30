package kyle.game.besiege;

import com.badlogic.gdx.audio.Music;

import static kyle.game.besiege.MapScreen.ZOOM_MAX;
import static kyle.game.besiege.MapScreen.ZOOM_MIN;


// Music inspo:
// https://www.youtube.com/watch?v=mJOTZwiCvDA&index=4&list=PL04AFFAFAADD767C4

// TODO make this a simple "directional" engine for battles.
public class SoundPlayer {
    public static float MASTER_VOLUME = .5f;

    public static float RAIN_FADE_TIME = 10f;
    private static LoopingSound rain = new LoopingSound(Assets.rain, RAIN_FADE_TIME);

    private static class LoopingSound {
        private Music sound;
        private float timeSinceStart;
        private float timeSinceEnd;
        private boolean isPlaying;
        private boolean fadingOut;
        private float fadeTime;

        LoopingSound(Music music, float fadeTime) {
            this.sound = music;
            this.fadeTime = fadeTime;
        }

        void fadeIn() {
            timeSinceStart = 0;
            isPlaying = true;
            sound.setLooping(true);
            // fade in
            sound.setVolume(0);
            sound.play();
            fadingOut = false;
        }

        void fadeOut() {
            fadingOut = true;
        }

        public void update(float delta) {
            if (isPlaying) {
                if (fadingOut) {
                    timeSinceEnd += delta;
                    if (timeSinceEnd > fadeTime) {
                        timeSinceEnd = 0;
                        isPlaying = false;
                        sound.pause();
                    } else {
//                        System.out.println("Setting volume to: " + (MASTER_VOLUME * .3f * (1 - timeSinceEnd / fadeTime)));

                        sound.setVolume(MASTER_VOLUME * .3f * (1 - timeSinceEnd / fadeTime));
                    }
                } else {
                    if (timeSinceStart < fadeTime) {
                        timeSinceStart += delta;
//                        System.out.println("Setting volume to: " + (MASTER_VOLUME * .3f * timeSinceStart / fadeTime));
                        sound.setVolume(MASTER_VOLUME * .3f * timeSinceStart
                                / fadeTime);
                    }
                }
            }
        }
    }

    public static void playThunder() {
        if (Math.random() < .5) {
            Assets.thunder1.play(MASTER_VOLUME * (float) (.2 + Math.random() * 0.5));
        } else Assets.thunder2.play(MASTER_VOLUME * (float) (.2 + Math.random() * 0.5));
    }

    static void updateSounds(float delta) {
        rain.update(delta);
    }

    public static void startRain() {
        rain.fadeIn();
    }

    public static void stopRain() {
        rain.fadeOut();
    }

    // TODO fix this to work with fading out.
    public static void setZoomBattle(float zoomBattle) {
        float zoomDif = (ZOOM_MAX / 2) - ZOOM_MIN;
        float soundFactor = ((zoomBattle - ZOOM_MIN) / zoomDif);
        Assets.rain.setVolume(MASTER_VOLUME * .3f * (1-soundFactor));
    }
}
