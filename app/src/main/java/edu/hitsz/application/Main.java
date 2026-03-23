package edu.hitsz.application;

import android.content.Context;

/**
 * Shared game constants and difficulty factory for Android runtime.
 */
public final class Main {

    public static int WINDOW_WIDTH = 512;
    public static int WINDOW_HEIGHT = 768;

    private Main() {
    }

    public static void updateWindowSize(int width, int height) {
        if (width > 0) {
            WINDOW_WIDTH = width;
        }
        if (height > 0) {
            WINDOW_HEIGHT = height;
        }
    }

    /**
     * Build a game instance by difficulty with Android context.
     */
    public static AbstractGame createGame(Context context, String difficulty) {
        String mode = difficulty == null ? "easy" : difficulty.toLowerCase();
        switch (mode) {
            case "easy":
                return new EasyGame(context);
            case "medium":
                return new NormalGame(context);
            case "hard":
                return new HardGame(context);
            default:
                throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
        }
    }

    /**
     * Legacy compatibility for old call sites.
     */
    public static AbstractGame startGame(String difficulty) {
        String mode = difficulty == null ? "easy" : difficulty.toLowerCase();
        switch (mode) {
            case "easy":
                return new EasyGame();
            case "medium":
                return new NormalGame();
            case "hard":
                return new HardGame();
            default:
                throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
        }
    }
}
