package edu.hitsz.enemyfactory;
import edu.hitsz.aircraft.AbstractAircraft;

public interface EnemyFactory {
    AbstractAircraft createEnemy();
    default AbstractAircraft createEnemy(int hp) {
        return createEnemy(); // 默认调用无参版本
    }
    default AbstractAircraft createEnemy(int hp, int speed) {
        return createEnemy(); // 默认调用无参版本
    }
}
