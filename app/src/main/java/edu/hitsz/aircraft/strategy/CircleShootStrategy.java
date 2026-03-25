package edu.hitsz.aircraft.strategy;

import edu.hitsz.bullet.*;
import edu.hitsz.aircraft.*;
import java.util.LinkedList;
import java.util.List;

/**
 * 改进版环射策略 —— 子弹沿圆周方向均匀射出
 */
public class CircleShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY();
        int power = aircraft.getPower();
        int shootNum = aircraft.getShootNum();

        double bulletSpeed = 6.0;    // 子弹基础速度
        double angleStep = 2 * Math.PI / shootNum;

        for (int i = 0; i < shootNum; i++) {
            double angle = i * angleStep - Math.PI / 2;  // 从上方开始发射
            double vx = bulletSpeed * Math.cos(angle);
            double vy = bulletSpeed * Math.sin(angle);

            BaseBullet bullet;
            if (aircraft instanceof HeroAircraft) {
                bullet = new HeroBullet(x, y, (int)(vx * 2), (int)(vy * 2), power);
            } else {
                bullet = new EnemyBullet(x, y, (int)(vx * 2), (int)(vy * 2), power);
            }

            res.add(bullet);
        }


        return res;
    }
}
