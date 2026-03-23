package edu.hitsz.enemyfactory;

import edu.hitsz.aircraft.*;
import edu.hitsz.application.Main;
import edu.hitsz.application.ImageManager;

/**
 * Boss 敌机工厂类
 */
public class BossEnemyFactory implements EnemyFactory {

    @Override
    public AbstractAircraft createEnemy() {
        // Boss 出现在屏幕上方中央附近
        int locationX = (int) (Main.WINDOW_WIDTH / 2.0 - ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth() / 2.0);
        int locationY = (int) (Main.WINDOW_HEIGHT * 0.1);

        // 左右移动的水平速度（随机方向）
        int speedX = (Math.random() < 0.5) ? -4 : 4;
        int speedY = 0; // 不向下移动
        int hp = 100;   // Boss 血量较高，可根据难度调整

        return new BossEnemy(locationX, locationY, speedX, speedY, hp);
    }

    public AbstractAircraft createEnemy(int BossHp) {
        // Boss 出现在屏幕上方中央附近
        int locationX = (int) (Main.WINDOW_WIDTH / 2.0 - ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth() / 2.0);
        int locationY = (int) (Main.WINDOW_HEIGHT * 0.1);

        // 左右移动的水平速度（随机方向）
        int speedX = (Math.random() < 0.5) ? -4 : 4;
        int speedY = 0; // 不向下移动
        int hp = BossHp;   // Boss 血量较高，可根据难度调整

        return new BossEnemy(locationX, locationY, speedX, speedY, hp);
    }
}

