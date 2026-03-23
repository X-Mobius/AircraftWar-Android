package edu.hitsz.property;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.*;
import edu.hitsz.application.AbstractGame;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.observer.BombSubscriber;
import edu.hitsz.sound.SoundManager;

import java.util.List;

public class BombProperty extends BaseProperty {

    public BombProperty(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft hero) {
        SoundManager.playSoundEffect("src/videos/bomb_explosion.wav", -15f);
        System.out.println("BombProperty activated!");
    }

    /**
     * 在炸弹生效时，从游戏对象中实时获取当前敌机和子弹，
     * 并立即通知它们执行炸弹响应逻辑
     */
    public void activate(HeroAircraft hero, List<AbstractAircraft> enemyAircrafts, List<BaseBullet> enemyBullets, AbstractGame game) {

        int totalScore = 0;
        // 先遍历敌机列表进行“加分预测”
        for (AbstractAircraft enemy : enemyAircrafts) {
            if (enemy instanceof BossEnemy) {
                continue; // Boss 不受影响
            }

            if (enemy instanceof MobEnemy) {
                totalScore += 10; // 普通敌机固定加10分
            } else if (enemy instanceof EliteEnemy) {
                totalScore += 20; // 精英敌机固定加20分
            } else if (enemy instanceof ElitePlusEnemy) {
                // 超级精英敌机：如果炸弹造成的伤害足以致死，则加分（但不真正扣血）
                ElitePlusEnemy se = (ElitePlusEnemy) enemy;
                int predictedHp = se.getHp() - 30; // 预测伤害值
                if (predictedHp <= 0) {
                    totalScore += 30;
                }
            }
        }

        if (totalScore > 0) {
            game.addScore(totalScore);
            System.out.println("Bomb predicted to destroy worth " + totalScore + " points!");
        }

        // 通知所有敌机
        for (AbstractAircraft enemy : enemyAircrafts) {
            if (enemy instanceof BossEnemy) continue; // Boss 不受影响
            if (enemy instanceof BombSubscriber) {
                ((BombSubscriber) enemy).onBombExplode();
            }
        }

        // 通知所有敌机子弹
        for (BaseBullet bullet : enemyBullets) {
            if (bullet instanceof BombSubscriber) {
                ((BombSubscriber) bullet).onBombExplode();
            }
        }
    }
}
