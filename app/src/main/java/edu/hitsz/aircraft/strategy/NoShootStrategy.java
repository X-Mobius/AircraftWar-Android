package edu.hitsz.aircraft.strategy;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.aircraft.AbstractAircraft;
import java.util.LinkedList;
import java.util.List;

/**
 * 不射击策略 —— 飞机不会发射子弹
 */
public class NoShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        return new LinkedList<>(); // 返回空列表
    }
}
