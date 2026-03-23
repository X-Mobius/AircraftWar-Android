package edu.hitsz.aircraft;

import edu.hitsz.aircraft.strategy.*;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.observer.BombSubscriber;


import java.util.LinkedList;
import java.util.List;


/**
 * 精英敌机
 */
public class EliteEnemy extends AbstractAircraft  implements BombSubscriber {

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = 30;
        this.shootNum = 1;
        this.direction = 1;
        this.shootStrategy = new DirectShootStrategy(); // 默认直射
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
        vanish();
    }

    @Override
    public List<BaseBullet> shoot() {
        if (shootStrategy == null) {
            return new java.util.LinkedList<>();
        }
        return shootStrategy.shoot(this); // 调用策略
    }
}
