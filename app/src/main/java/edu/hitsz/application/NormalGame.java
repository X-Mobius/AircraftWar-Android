package edu.hitsz.application;

import android.content.Context;
import android.util.Log;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.enemyfactory.BossEnemyFactory;
import edu.hitsz.enemyfactory.EliteEnemyFactory;
import edu.hitsz.enemyfactory.ElitePlusEnemyFactory;
import edu.hitsz.enemyfactory.EnemyFactory;
import edu.hitsz.enemyfactory.MobEnemyFactory;
import edu.hitsz.sound.SoundManager;

public class NormalGame extends AbstractGame {

    private int elitePlusEnemyHp = 30;
    private int eliteEnemyHp = 30;
    private int mobEnemyHp = 30;
    private int elitePlusEnemySpeed = 9;
    private int eliteEnemySpeed = 8;
    private int mobEnemySpeed = 7;
    private double elitePlusEnemyProbability = 0.15;
    private double eliteEnemyProbability = 0.35;

    public NormalGame() {
        initGameConfig();
    }

    public NormalGame(Context context) {
        super(context);
        initGameConfig();
    }

    private void initGameConfig() {
        this.backgroundImage = ImageManager.BACKGROUND_IMAGE_MEDIUM;
        this.enemyMaxNumber = 4;
        this.cycleDuration = 600;
    }

    private int lastDifficultyTime = 0;

    @Override
    protected void difficultyUpdate(int time) {
        // Increase difficulty every 15 seconds.
        if (time - lastDifficultyTime >= 15000) {
            lastDifficultyTime = time;

            if (elitePlusEnemyHp < 60) {
                elitePlusEnemyHp += 10;
                if (elitePlusEnemyHp > 60) {
                    elitePlusEnemyHp = 60;
                }
                System.out.println("[Difficulty] ElitePlus HP -> " + elitePlusEnemyHp);
            }
            if (eliteEnemyHp < 55) {
                eliteEnemyHp += 5;
                if (eliteEnemyHp > 55) {
                    eliteEnemyHp = 55;
                }
                System.out.println("[Difficulty] Elite HP -> " + eliteEnemyHp);
            }
            if (mobEnemyHp < 60) {
                mobEnemyHp += 5;
                if (mobEnemyHp > 60) {
                    mobEnemyHp = 60;
                }
                System.out.println("[Difficulty] Mob HP -> " + mobEnemyHp);
            }

            if (enemyMaxNumber < 7) {
                enemyMaxNumber += 1;
                System.out.println("[Difficulty] enemyMaxNumber -> " + enemyMaxNumber);
            }

            if (eliteEnemySpeed < 13) {
                eliteEnemySpeed += 1;
                System.out.println("[Difficulty] Elite speed -> " + eliteEnemySpeed);
            }
            if (elitePlusEnemySpeed < 14) {
                elitePlusEnemySpeed += 1;
                System.out.println("[Difficulty] ElitePlus speed -> " + elitePlusEnemySpeed);
            }
            if (mobEnemySpeed < 13) {
                mobEnemySpeed += 1;
                System.out.println("[Difficulty] Mob speed -> " + mobEnemySpeed);
            }

            if (cycleDuration > 400) {
                cycleDuration -= 40;
                System.out.println("[Difficulty] cycleDuration -> " + cycleDuration);
            }

            if (elitePlusEnemyProbability < 0.2) {
                elitePlusEnemyProbability += 0.01;
                System.out.println("[Difficulty] ElitePlus probability -> " + elitePlusEnemyProbability);
            }
            if (eliteEnemyProbability < 0.45) {
                eliteEnemyProbability += 0.02;
                System.out.println("[Difficulty] Elite probability -> "
                        + (eliteEnemyProbability - elitePlusEnemyProbability));
            }
        }
    }

    @Override
    protected void onGameOver() {
        SoundManager.stopBgm();
        SoundManager.playSoundEffect("src/videos/game_over.wav", -15f);
        Log.i("NormalGame", "Game over. mode=normal, score=" + score);
    }

    protected void generateEnemy() {
        if (enemyAircrafts.size() < enemyMaxNumber) {
            EnemyFactory factory;

            // Check whether there is a living boss.
            boolean bossExists = false;
            for (AbstractAircraft enemy : enemyAircrafts) {
                if (enemy instanceof BossEnemy && !enemy.notValid()) {
                    bossExists = true;
                    break;
                }
            }

            if (!bossExists && score >= lastBossScore + 300) {
                factory = new BossEnemyFactory();
                AbstractAircraft boss = factory.createEnemy();
                enemyAircrafts.add(boss);
                SoundManager.playBossBgm();
                lastBossScore += 300;
            } else {
                double random = Math.random();
                AbstractAircraft enemy;
                if (random < elitePlusEnemyProbability) {
                    factory = new ElitePlusEnemyFactory();
                    enemy = factory.createEnemy(elitePlusEnemyHp, elitePlusEnemySpeed);
                } else if (random < eliteEnemyProbability) {
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
}
