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

    private final int elitePlusEnemyHp = 30;
    private final int eliteEnemyHp = 30;
    private final int mobEnemyHp = 30;
    private final int elitePlusEnemySpeed = 7;
    private final int eliteEnemySpeed = 6;
    private final int mobEnemySpeed = 5;

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
            AbstractAircraft enemy;
            if (random < 0.15) {
                factory = new ElitePlusEnemyFactory();
                enemy = factory.createEnemy(elitePlusEnemyHp, elitePlusEnemySpeed);
            } else if (random < 0.35) {
                factory = new EliteEnemyFactory();
                enemy = factory.createEnemy(eliteEnemyHp, eliteEnemySpeed);
            } else {
                factory = new MobEnemyFactory();
                enemy = factory.createEnemy(mobEnemyHp, mobEnemySpeed);
            }
            enemyAircrafts.add(enemy);
        }
    }
}
