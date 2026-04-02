package edu.hitsz.application;

import android.content.Context;
import android.util.Log;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.enemyfactory.EliteEnemyFactory;
import edu.hitsz.enemyfactory.ElitePlusEnemyFactory;
import edu.hitsz.enemyfactory.EnemyFactory;
import edu.hitsz.enemyfactory.MobEnemyFactory;
import edu.hitsz.sound.SoundManager;

public class EasyGame extends AbstractGame {

    public EasyGame() {
        initGameConfig();
    }

    public EasyGame(Context context) {
        super(context);
        initGameConfig();
    }

    private void initGameConfig() {
        this.backgroundImage = ImageManager.BACKGROUND_IMAGE_EASY;
        this.enemyMaxNumber = 3;
        this.cycleDuration = 600;
    }

    @Override
    protected void difficultyUpdate(int time) {
        // Easy mode keeps fixed difficulty.
    }

    @Override
    protected void onGameOver() {
        SoundManager.stopBgm();
        SoundManager.playSoundEffect("src/videos/game_over.wav", -15f);
        Log.i("EasyGame", "Game over. mode=easy, score=" + score);
    }

    protected void generateEnemy() {
        if (enemyAircrafts.size() < enemyMaxNumber) {
            EnemyFactory factory;

            // Spawn mob/elite enemies by probability.
            double random = Math.random();
            if (random < 0.15) {
                factory = new ElitePlusEnemyFactory();
            } else if (random < 0.35) {
                factory = new EliteEnemyFactory();
            } else {
                factory = new MobEnemyFactory();
            }
            AbstractAircraft enemy = factory.createEnemy();
            enemyAircrafts.add(enemy);
        }
    }
}
