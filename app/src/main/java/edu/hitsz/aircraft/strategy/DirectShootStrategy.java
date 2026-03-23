package edu.hitsz.aircraft.strategy;

import edu.hitsz.bullet.*;
import edu.hitsz.aircraft.*;
import java.util.LinkedList;
import java.util.List;

/**
 * 直射策略 —— 多发子弹平行向前射出
 */
public class DirectShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + aircraft.getDirection() * 2;
        int speedX = 0;
        int speedY = aircraft.getSpeedY() + aircraft.getDirection() * 10;
        int shootNum = aircraft.getShootNum();
        int power = aircraft.getPower();

        for (int i = 0; i < shootNum; i++) {
            BaseBullet bullet;
            // 多发子弹横向平行分布
            if (aircraft instanceof HeroAircraft) {
                bullet = new HeroBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, -12, power);
            } else {
                bullet = new EnemyBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, speedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}
