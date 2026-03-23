package edu.hitsz.aircraft;

import edu.hitsz.aircraft.strategy.*;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.observer.BombSubscriber;


import java.util.LinkedList;
import java.util.List;

/**
 * 超级精英敌机
 */
public class ElitePlusEnemy extends AbstractAircraft implements BombSubscriber {

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        // 设置射击属性
        this.power = 30;
        this.shootNum = 3;
        this.direction = 1;

        // 默认使用散射射击策略
        this.shootStrategy = new ScatterShootStrategy();
    }

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT ) {
            vanish();
        }
    }

    @Override
    public void onBombExplode() {
        decreaseHp(30); // 比如减少 100 点血量
        if (getHp() <= 0) {
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

