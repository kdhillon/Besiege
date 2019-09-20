package kyle.game.besiege;

import com.badlogic.gdx.audio.Music;
import kyle.game.besiege.battle.Battle;
import kyle.game.besiege.party.CultureType;
import kyle.game.besiege.voronoi.Center;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static kyle.game.besiege.MapScreen.ZOOM_MAX;
import static kyle.game.besiege.MapScreen.ZOOM_MIN;


// Music inspo:
// https://www.youtube.com/watch?v=mJOTZwiCvDA&index=4&list=PL04AFFAFAADD767C4

// TODO make this a simple "directional" engine for battles.
public class SoundPlayer {
    private static float MASTER_VOLUME = .5f;

    private static float RAIN_FADE_TIME = 10f;
    private static float MUSIC_BPM = 74;
    private static double SECONDS_PER_BEAT = (60 / MUSIC_BPM);

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    private static LoopingSound rain = new LoopingSound(Assets.rain, RAIN_FADE_TIME, 0.3f);

    // Need to test music transition
    // every song has a BPM

    // to test, play music 1 for exactly SECONDS_PER_BEAT * 8, then stop it and immediately start track 2.

    private static LoopingSound music1 = new LoopingSound(Assets.music1, 0, 1);
    private static LoopingSound music2 = new LoopingSound(Assets.music2, 0, 1);
    private static LoopingSound forest = new LoopingSound(Assets.forestMusic, 3, 1);

    private static MusicPlayer musicPlayer = new MusicPlayer();

    private static class MusicPlayer {
        CultureType currentCulture;
        LoopingSound currentlyPlaying;
        HashMap<CultureType, LoopingSound> cultureToSound = new HashMap();

        private List<LoopingSound> currentlyPlayingSounds = new ArrayList<LoopingSound>();

        private float timeSinceStart = 0;
        private boolean isPlaying = false;

        /** Start playing music. Music will repeat when it's done. */
        public void start(CultureType cultureType) {
            isPlaying = true;
//            start(forest);

//            switchSongs();
            System.out.println("starting fade in of music");
//            scheduleNextSwitch(8);
        }

        private void start(LoopingSound sound) {
            sound.fadeIn();
            currentlyPlayingSounds.add(sound);
        }

        /**
         * On player entering the center, play new music if appropriate. Wait until next smooth opportunity before
         * starting the transition, perhaps in case the player keeps switching between two areas frequently.
         *
         * When a player enters a new center, wait until the next mod 16 bar, start another song from the beginning.
         */
        public void playerEnteredCenter(Center center) {

        }

        /** Player has entered battle, start battle music. */
        public void enterBattle(Battle battle) {

        }

        /** Player has left battle, end battle music. */
        public void leaveBattle() {

        }

        /** Set the music volume, between [0, 1]. */
        public void setMusicVolume(float volume) {

        }

        private void scheduleNextSwitch(int beats) {
            scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    switchSongs();
                }
            }, (long) (SECONDS_PER_BEAT * beats * 1000L - 10), TimeUnit.MILLISECONDS);
        }

        private void switchSongs() {
            if (music1.isPlaying) {
                music1.fadeOut();
                music2.fadeIn();
            } else if (music2.isPlaying) {
                music2.fadeOut();
                music1.fadeIn();
            }
            // TODO do this arbitrarily.
            update(0);
            java.util.concurrent.locks.LockSupport.parkNanos((long) (1000000000 * SECONDS_PER_BEAT * 8));
            update(0);
//            switchSongs();
        }

        public void update(float delta) {
            if (isPlaying) {
                timeSinceStart += delta;
                // if we've been playing for more than x seconds, transition
//                if (timeSinceStart > SECONDS_PER_BEAT * 8) {
//                    System.out.println("starting fade out of music: " + timeSinceStart);
//                    switchSongs();
//                    timeSinceStart = 0;
//                }
                for (LoopingSound sound : currentlyPlayingSounds) {
                    sound.update(delta);
                }
            }
        }
    }

    private static class LoopingSound {
        private Music sound;
        private float timeSinceStart;
        private float timeSinceEnd;
        private boolean isPlaying;
        private boolean fadingOut;
        private float fadeTime;
        private float volumeFactor;

        LoopingSound(Music music, float fadeTimeS, float volumeFactor) {
            this.sound = music;
            this.fadeTime = fadeTimeS;
            this.volumeFactor = volumeFactor;
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
                    if (timeSinceEnd >= fadeTime) {
                        timeSinceEnd = 0;
                        isPlaying = false;
                        sound.stop();
                    } else {
                        sound.setVolume(MASTER_VOLUME * volumeFactor * (1 - Math.min(timeSinceEnd / fadeTime, 1)));
                    }
                } else {
                    if (timeSinceStart <= fadeTime) {
                        timeSinceStart += delta;

                        System.out.println("Setting volume to: " + sound.getVolume());
                        sound.setVolume(MASTER_VOLUME * volumeFactor * Math.min(timeSinceStart / fadeTime, 1));
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

    public static void updateSounds(final float delta) {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                rain.update(delta);
                musicPlayer.update(delta);
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public static void startRain() {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("fading in rain");
                rain.fadeIn();
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public static void stopRain() {
        rain.fadeOut();
    }

    // TODO fix this to work with fading out.
    public static void setZoomBattle(float zoomBattle) {
        float zoomDif = (ZOOM_MAX / 2) - ZOOM_MIN;
        float soundFactor = ((zoomBattle - ZOOM_MIN) / zoomDif);
        Assets.rain.setVolume(MASTER_VOLUME * .3f * (1 - soundFactor));
    }

    /**
     * Start playing music at the start of the game.
     */
    public static void startMusic(final CultureType cultureType) {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                musicPlayer.start(cultureType);
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * On player entering the center, play new music if appropriate.
     * Wait until next smooth opportunity before starting the transition, perhaps in case the player keeps switching
     * between two areas frequently.
     */
    public static void playerEnteredCenter(Center center) {
        musicPlayer.playerEnteredCenter(center);
    }

    /**
     * Player has entered battle, start battle music.
     */
    public static void enterBattle(Battle battle) {
        musicPlayer.enterBattle(battle);
    }

    /**
     * Player has left battle, end battle music.
     */
    public static void leaveBattle() {
        musicPlayer.leaveBattle();
    }

    /**
     * Set the music volume, between [0, 1].
     */
    public static void setMusicVolume(float volume) {
        musicPlayer.setMusicVolume(volume);
    }
}
