package edu.hitsz.aircraft.strategy;

import edu.hitsz.bullet.*;
import edu.hitsz.aircraft.*;
import java.util.LinkedList;
import java.util.List;

/**
 * 散射策略 —— 三发子弹呈角度散射
 */
public class ScatterShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + aircraft.getDirection() * 2;
        int baseSpeedY = aircraft.getSpeedY() + aircraft.getDirection() * 5;
        int power = aircraft.getPower();
        int shootNum = aircraft.getShootNum();

        // 若shootNum<3可动态调整，但默认取前三个偏移
        int[] offsetX = {-15, 0, 15};
        int[] offsetSpeedX = {-2, 0, 2};

        for (int i = 0; i < shootNum && i < 3; i++) {
            BaseBullet bullet;
            if (aircraft instanceof HeroAircraft) {
                bullet = new HeroBullet(
                        x + offsetX[i],
                        y,
                        offsetSpeedX[i],
                        -8,
                        power
                );
            } else {
                bullet = new EnemyBullet(
                        x + offsetX[i],
                        y,
                        offsetSpeedX[i],
                        baseSpeedY,
                        power
                );
            }

            res.add(bullet);
        }
        return res;
    }
}
