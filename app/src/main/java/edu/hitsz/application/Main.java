package edu.hitsz.application;

import android.content.Context;

/**
 * Android 运行时共享窗口常量与难度工厂。
 */
public final class Main {

    public static int WINDOW_WIDTH = 512;
    public static int WINDOW_HEIGHT = 768;

    private Main() {
    }

    public static void updateWindowSize(int width, int height) {
        // 由 AbstractGame.surfaceChanged 调用，同步运行时窗口尺寸。
        if (width > 0) {
            WINDOW_WIDTH = width;
        }
        if (height > 0) {
            WINDOW_HEIGHT = height;
        }
    }

    /**
     * 按难度创建游戏实例（Android Context 版本）。
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
     * 兼容旧调用方式的入口。
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
