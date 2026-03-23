package edu.hitsz.sound;

import android.util.Log;

/**
 * Android compatibility sound facade.
 * Keeps the old API shape so game logic can stay unchanged during migration.
 */
public final class SoundManager {

    private static final String TAG = "SoundManager";
    private static boolean soundOn = true;

    private SoundManager() {
    }

    public static void setSoundOn(boolean on) {
        soundOn = on;
    }

    public static boolean isSoundOn() {
        return soundOn;
    }

    public static void playBgm() {
        if (!soundOn) {
            return;
        }
        Log.d(TAG, "playBgm()");
    }

    public static void playBossBgm() {
        if (!soundOn) {
            return;
        }
        Log.d(TAG, "playBossBgm()");
    }

    public static void stopBgm() {
        Log.d(TAG, "stopBgm()");
    }

    public static void playSoundEffect(String filePath) {
        if (!soundOn) {
            return;
        }
        Log.d(TAG, "playSoundEffect(" + filePath + ")");
    }

    public static void playSoundEffect(String path, float volume) {
        if (!soundOn) {
            return;
        }
        Log.d(TAG, "playSoundEffect(" + path + ", volume=" + volume + ")");
    }
}
