package edu.hitsz.sound;

/**
 * Legacy player wrapper kept for API compatibility.
 */
public class MusicPlayer extends Thread {

    private final String filePath;
    private final boolean loop;
    private volatile boolean stopFlag = false;

    public MusicPlayer(String filePath, boolean loop) {
        this.filePath = filePath;
        this.loop = loop;
    }

    public void stopMusic() {
        stopFlag = true;
        interrupt();
    }

    @Override
    public void run() {
        do {
            if (stopFlag) {
                break;
            }
            SoundManager.playSoundEffect(filePath);
            if (loop) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    if (stopFlag) {
                        break;
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } while (loop && !stopFlag);
    }
}
