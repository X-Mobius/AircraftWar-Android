package edu.hitsz.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Android 音频管理器。
 * MediaPlayer 负责长时循环的背景音乐，SoundPool 负责短促高频的音效。
 */
public final class SoundManager {

    // 迁移关键点：保留 Windows 版本的调用风格（如 src/videos/xxx.wav），
    // 在 Android 侧统一映射到 raw 资源 id。

    private static final String TAG = "SoundManager";
    private static final int DEFAULT_MAX_STREAMS = 8;
    private static final String RAW_TYPE = "raw";

    private static volatile boolean soundOn = true;
    private static Context appContext;

    private static MediaPlayer bgmPlayer;
    private static int currentBgmResId = 0;

    private static SoundPool soundPool;
    private static final Map<Integer, Integer> effectResToSoundId = new HashMap<>();

    private static final String EFFECT_BULLET_HIT = "bullet_hit";
    private static final String EFFECT_BOMB_EXPLOSION = "bomb_explosion";
    private static final String EFFECT_GET_SUPPLY = "get_supply";
    private static final String EFFECT_GAME_OVER = "game_over";
    private static final String BGM_NORMAL = "bgm";
    private static final String BGM_BOSS = "bgm_boss";

    private SoundManager() {
    }

    public static synchronized void init(Context context) {
        if (context == null) {
            return;
        }
        appContext = context.getApplicationContext();
        if (soundPool == null) {
            // SoundPool 只初始化一次，并预加载常用音效降低首次延迟。
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(DEFAULT_MAX_STREAMS)
                    .build();
            preloadEffect(EFFECT_BULLET_HIT);
            preloadEffect(EFFECT_BOMB_EXPLOSION);
            preloadEffect(EFFECT_GET_SUPPLY);
            preloadEffect(EFFECT_GAME_OVER);
        }
    }

    public static synchronized void release() {
        stopBgm();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        effectResToSoundId.clear();
    }

    public static synchronized void setSoundOn(boolean on) {
        soundOn = on;
        if (!soundOn) {
            // 关闭音效时立即停止 BGM，保证设置实时生效。
            stopBgm();
        }
    }

    public static boolean isSoundOn() {
        return soundOn;
    }

    public static synchronized void playBgm() {
        if (!soundOn) {
            // 关闭音效时立即停止 BGM，保证设置实时生效。
            return;
        }
        playLoopingBgm(BGM_NORMAL);
    }

    public static synchronized void playBossBgm() {
        if (!soundOn) {
            // 关闭音效时立即停止 BGM，保证设置实时生效。
            return;
        }
        playLoopingBgm(BGM_BOSS);
    }

    public static synchronized void stopBgm() {
        if (bgmPlayer != null) {
            try {
                bgmPlayer.stop();
            } catch (IllegalStateException ignored) {
            }
            try {
                bgmPlayer.release();
            } catch (Exception ignored) {
            }
            bgmPlayer = null;
            currentBgmResId = 0;
        }
    }

    public static synchronized void playSoundEffect(String filePath) {
        if (!soundOn) {
            // 关闭音效时立即停止 BGM，保证设置实时生效。
            return;
        }
        playEffectByPath(filePath, 1.0f);
    }

    public static synchronized void playSoundEffect(String path, float volume) {
        if (!soundOn) {
            // 关闭音效时立即停止 BGM，保证设置实时生效。
            return;
        }
        playEffectByPath(path, dbToLinear(volume));
    }

    private static void playLoopingBgm(String nameOrPath) {
        int resId = resolveRawResId(nameOrPath);
        if (resId == 0) {
            Log.w(TAG, "BGM resource not found: " + nameOrPath);
            return;
        }
        if (bgmPlayer != null && currentBgmResId == resId) {
            // 相同 BGM 已存在时直接继续播放，避免重复创建播放器。
            if (!bgmPlayer.isPlaying()) {
                bgmPlayer.start();
            }
            return;
        }

        stopBgm();
        if (appContext == null) {
            Log.w(TAG, "SoundManager not initialized, call init(context) first.");
            return;
        }

        bgmPlayer = MediaPlayer.create(appContext, resId);
        if (bgmPlayer == null) {
            Log.w(TAG, "Failed to create MediaPlayer for resId=" + resId);
            return;
        }
        bgmPlayer.setLooping(true);
        bgmPlayer.start();
        currentBgmResId = resId;
    }

    private static void playEffectByPath(String nameOrPath, float volume) {
        if (appContext == null) {
            Log.w(TAG, "SoundManager not initialized, call init(context) first.");
            return;
        }
        if (soundPool == null) {
            // SoundPool 只初始化一次，并预加载常用音效降低首次延迟。
            init(appContext);
        }

        int resId = resolveRawResId(nameOrPath);
        if (resId == 0 || soundPool == null) {
            Log.w(TAG, "Effect resource not found: " + nameOrPath);
            return;
        }

        Integer soundId = effectResToSoundId.get(resId);
        if (soundId == null) {
            // 对非常用音效懒加载，兼顾性能和兼容性。
            soundId = soundPool.load(appContext, resId, 1);
            effectResToSoundId.put(resId, soundId);
        }
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
    }

    private static void preloadEffect(String rawName) {
        int resId = resolveRawResId(rawName);
        if (resId == 0 || appContext == null || soundPool == null) {
            return;
        }
        int soundId = soundPool.load(appContext, resId, 1);
        effectResToSoundId.put(resId, soundId);
    }

    private static int resolveRawResId(String nameOrPath) {
        if (appContext == null || nameOrPath == null || nameOrPath.trim().isEmpty()) {
            return 0;
        }
        // 同时兼容资源名（bgm_boss）和旧路径（src/videos/bgm_boss.wav）。
        String normalized = nameOrPath.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String base = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        base = base.toLowerCase(Locale.ROOT);
        return appContext.getResources().getIdentifier(base, RAW_TYPE, appContext.getPackageName());
    }

    private static float dbToLinear(float db) {
        float linear = (float) Math.pow(10.0, db / 20.0);
        if (Float.isNaN(linear) || Float.isInfinite(linear)) {
            return 1.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, linear));
    }
}
