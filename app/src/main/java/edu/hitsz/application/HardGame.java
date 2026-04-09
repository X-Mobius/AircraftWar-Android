package edu.hitsz.application;

import android.content.Context;
import android.util.Log;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.enemyfactory.BossEnemyFactory;
import edu.hitsz.enemyfactory.EliteEnemyFactory;
import edu.hitsz.enemyfactory.ElitePlusEnemyFactory;
import edu.hitsz.enemyfactory.EnemyFactory;
import edu.hitsz.enemyfactory.MobEnemyFactory;
import edu.hitsz.property.BaseProperty;
import edu.hitsz.property.BloodFactory;
import edu.hitsz.property.BombFactory;
import edu.hitsz.property.BombProperty;
import edu.hitsz.property.BulletFactory;
import edu.hitsz.property.BulletPlusFactory;
import edu.hitsz.property.PropertyFactory;
import edu.hitsz.sound.SoundManager;

public class HardGame extends AbstractGame {

    private int bossHp = 150;
    private int elitePlusEnemyHp = 60;
    private int eliteEnemyHp = 30;
    private int mobEnemyHp = 35;
    private int elitePlusEnemySpeed = 11;
    private int eliteEnemySpeed = 10;
    private int mobEnemySpeed = 9;
    private double elitePlusEnemyProbability = 0.15;
    private double eliteEnemyProbability = 0.35;

    public HardGame() {
        initGameConfig();
    }

    public HardGame(Context context) {
        super(context);
        initGameConfig();
    }

    private void initGameConfig() {
        this.backgroundImage = ImageManager.BACKGROUND_IMAGE_HARD;
        this.enemyMaxNumber = 5;
        this.cycleDuration = 600;
    }

    private int lastDifficultyTime = 0;

    @Override
    protected void difficultyUpdate(int time) {
        // Increase difficulty every 12 seconds.
        if (time - lastDifficultyTime >= 12000) {
            lastDifficultyTime = time;

            if (elitePlusEnemyHp < 120) {
                elitePlusEnemyHp += 10;
                System.out.println("[Difficulty] ElitePlus HP -> " + elitePlusEnemyHp);
            }
            if (eliteEnemyHp < 60) {
                eliteEnemyHp += 10;
                System.out.println("[Difficulty] Elite HP -> " + eliteEnemyHp);
            }
            if (mobEnemyHp < 80) {
                mobEnemyHp += 5;
                System.out.println("[Difficulty] Mob HP -> " + mobEnemyHp);
            }

            if (enemyMaxNumber < 9) {
                enemyMaxNumber += 1;
                System.out.println("[Difficulty] enemyMaxNumber -> " + enemyMaxNumber);
            }

            if (eliteEnemySpeed < 16) {
                eliteEnemySpeed += 1;
                System.out.println("[Difficulty] Elite speed -> " + eliteEnemySpeed);
            }
            if (elitePlusEnemySpeed < 18) {
                elitePlusEnemySpeed += 1;
                System.out.println("[Difficulty] ElitePlus speed -> " + elitePlusEnemySpeed);
            }
            if (mobEnemySpeed < 15) {
                mobEnemySpeed += 1;
                System.out.println("[Difficulty] Mob speed -> " + mobEnemySpeed);
            }

            if (cycleDuration > 300) {
                cycleDuration -= 40;
                System.out.println("[Difficulty] cycleDuration -> " + cycleDuration);
            }

            if (elitePlusEnemyProbability < 0.3) {
                elitePlusEnemyProbability += 0.01;
                System.out.println("[Difficulty] ElitePlus probability -> " + elitePlusEnemyProbability);
            }
            if (eliteEnemyProbability < 0.6) {
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
        Log.i("HardGame", "Game over. mode=hard, score=" + score);
    }

    protected void generateEnemy() {
        if (enemyAircrafts.size() < enemyMaxNumber) {
            EnemyFactory factory;

            boolean bossExists = false;
            for (AbstractAircraft enemy : enemyAircrafts) {
                if (enemy instanceof BossEnemy && !enemy.notValid()) {
                    bossExists = true;
                    break;
                }
            }

            if (!bossExists && score >= lastBossScore + 500) {
                factory = new BossEnemyFactory();
                AbstractAircraft boss = factory.createEnemy(bossHp);
                enemyAircrafts.add(boss);
                SoundManager.playBossBgm();
                lastBossScore += 500;
                bossHp += 30;
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

    // Hard mode custom score/drop tuning.
    @Override
    protected void crashCheckAction() {
        // Enemy bullets hit hero.
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

        // Hero bullets hit enemies.
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    enemyAircraft.decreaseHp(bullet.getPower());
                    SoundManager.playSoundEffect("src/videos/bullet_hit.wav");
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        if (enemyAircraft instanceof BossEnemy) {
                            SoundManager.playBgm();
                            score += 100;
                        } else if (enemyAircraft instanceof ElitePlusEnemy) {
                            score += 75;
                        } else if (enemyAircraft instanceof EliteEnemy) {
                            score += 50;
                        } else {
                            score += 25;
                        }

                        if (enemyAircraft instanceof EliteEnemy
                                || enemyAircraft instanceof ElitePlusEnemy
                                || enemyAircraft instanceof BossEnemy) {

                            double dropChance = Math.random();
                            double threshold;
                            if (enemyAircraft instanceof BossEnemy) {
                                threshold = 1.0;
                            } else if (enemyAircraft instanceof ElitePlusEnemy) {
                                threshold = 0.95;
                            } else {
                                threshold = 0.9;
                            }

                            if (dropChance < threshold) {
                                int dropCount = 1;
                                if (enemyAircraft instanceof BossEnemy) {
                                    dropCount = (int) (Math.random() * 3) + 1;
                                }

                                for (int i = 0; i < dropCount; i++) {
                                    double random = Math.random();
                                    PropertyFactory factory;

                                    if (random < 0.3) {
                                        factory = new BloodFactory();
                                    } else if (random < 0.6) {
                                        factory = new BombFactory();
                                    } else if (random < 0.8) {
                                        factory = new BulletFactory();
                                    } else {
                                        factory = new BulletPlusFactory();
                                    }

                                    int offsetX = (dropCount > 1) ? (i - dropCount / 2) * 20 : 0;

                                    BaseProperty property = factory.createProperty(
                                            enemyAircraft.getLocationX() + offsetX,
                                            enemyAircraft.getLocationY(),
                                            0,
                                            5
                                    );
                                    properties.add(property);
                                }
                            }
                        }
                    }
                }
                // Hero collides with enemy.
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // Hero collects props.
        for (BaseProperty property : properties) {
            if (property.notValid()) {
                continue;
            }
            if (heroAircraft.crash(property)) {
                property.activate(heroAircraft);
                if (property instanceof BombProperty) {
                    ((BombProperty) property).activate(heroAircraft, enemyAircrafts, enemyBullets, this);
                }
                property.vanish();
            }
        }
    }
}
