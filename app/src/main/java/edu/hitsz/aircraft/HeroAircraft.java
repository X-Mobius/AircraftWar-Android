package edu.hitsz.aircraft;

import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.aircraft.strategy.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄飞机，游戏玩家操控
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    private volatile static HeroAircraft heroAircraft;
    private static final int DEFAULT_HP = 1000;
    private static final int DEFAULT_POWER = 30;
    private static final int DEFAULT_SHOOT_NUM = 1;
    private static final int DEFAULT_DIRECTION = -1;

    /**
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param speedY 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param hp    初始生命值
     */
    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        // 初始化射击属性
        this.power = 30;
        this.shootNum = 1;
        this.direction = -1;

        // 默认使用直射策略
        this.shootStrategy = new DirectShootStrategy();
    }

    public static HeroAircraft getHeroAircraft() {
        if (heroAircraft == null) {
            synchronized (HeroAircraft.class) {
                if (heroAircraft == null) {
                    heroAircraft = new HeroAircraft(
                            Main.WINDOW_WIDTH / 2,
                            Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight() ,
                            0, 0, DEFAULT_HP);
                }
            }
        }
        return heroAircraft;
    }

    public synchronized void resetForNewGame(int locationX, int locationY) {
        this.setLocation(locationX, locationY);
        this.isValid = true;
        this.setHp(this.getMaxHp());
        this.power = DEFAULT_POWER;
        this.shootNum = DEFAULT_SHOOT_NUM;
        this.direction = DEFAULT_DIRECTION;
        this.shootStrategy = new DirectShootStrategy();
        this.shootStrategyPriority = 0;
        this.shootStrategyEndTime = 0;
        this.strategyEndTime = 0;
        this.pendingShootStrategy = null;
        this.pendingDurationSeconds = 0;
        this.pendingPriority = 0;
        this.strategyActive = false;
        if (this.strategyTimerThread != null && this.strategyTimerThread.isAlive()) {
            this.strategyTimerThread.interrupt();
        }
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

    @Override
    public List<BaseBullet> shoot() {
        if (shootStrategy == null) {
            return new LinkedList<>();
        }
        return shootStrategy.shoot(this);
    }


}
