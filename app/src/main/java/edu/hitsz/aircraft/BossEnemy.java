package edu.hitsz.aircraft;

import edu.hitsz.aircraft.strategy.*;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * Boss敌机类
 */
public class BossEnemy extends AbstractAircraft {

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        // 设置射击属性
        this.shootNum = 20;
        this.power = 10;
        this.direction = 1;

        // 默认使用环形射击策略
        this.shootStrategy = new CircleShootStrategy();
    }

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

    @Override
    public List<BaseBullet> shoot() {
        if (shootStrategy == null) {
            return new LinkedList<>();
        }
        return shootStrategy.shoot(this);
    }
}