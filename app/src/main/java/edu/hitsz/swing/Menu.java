package edu.hitsz.swing;

import android.util.Log;

import edu.hitsz.application.Main;
import edu.hitsz.sound.SoundManager;

/**
 * Legacy menu compatibility class.
 * Desktop Swing UI has been removed in Android migration.
 */
public class Menu {

    private static final String TAG = "Menu";

    public Menu() {
    }

    public void startEasy() {
        Log.i(TAG, "start easy mode");
        Main.startGame("easy");
    }

    public void startMedium() {
        Log.i(TAG, "start medium mode");
        Main.startGame("medium");
    }

    public void startHard() {
        Log.i(TAG, "start hard mode");
        Main.startGame("hard");
    }

    public void setSoundEnabled(boolean enabled) {
        SoundManager.setSoundOn(enabled);
        Log.i(TAG, "sound=" + (enabled ? "on" : "off"));
    }

    public Object getMainPanel() {
        return null;
    }

    public static void main(String[] args) {
        Log.i(TAG, "Menu.main() is not used on Android runtime.");
    }
}
