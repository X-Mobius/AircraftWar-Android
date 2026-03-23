package edu.hitsz.aircraft.strategy;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.aircraft.AbstractAircraft;
import java.util.List;

/**
 * 射击策略接口
 * 定义不同射击方式的统一行为
 */
public interface ShootStrategy {
    /**
     * 执行射击动作
     * @param aircraft 当前飞机对象（用于获取位置、速度、方向等）
     * @return 生成的子弹列表
     */
    List<BaseBullet> shoot(AbstractAircraft aircraft);
}
