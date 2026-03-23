package edu.hitsz.aircraft;

import edu.hitsz.aircraft.strategy.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;


import java.util.LinkedList;
import java.util.List;

/**
 * 所有种类飞机的抽象父类：
 * 敌机（BOSS, ELITE, MOB），英雄飞机
 *
 * @author hitsz
 */
public abstract class AbstractAircraft extends AbstractFlyingObject {
    /**
     * 生命值
     */
    protected int maxHp;
    protected int hp;
    // 子弹相关
    protected int power;       // 子弹威力
    protected int shootNum;    // 每次发射子弹数量
    protected int direction;   // 子弹飞行方向（英雄机向上为-1，敌机向下为1）

    protected ShootStrategy shootStrategy; // 当前射击策略
    protected long shootStrategyEndTime = 0;
    protected int shootStrategyPriority = 0;
    protected long strategyEndTime = 0;
    protected ShootStrategy pendingShootStrategy = null;
    protected int pendingDurationSeconds = 0;
    protected int pendingPriority = 0;

    // 用于管理当前策略计时线程
    protected Thread strategyTimerThread;
    protected volatile boolean strategyActive = false;


    public AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
    }

    public void decreaseHp(int decrease){
        hp -= decrease;
        if(hp <= 0){
            hp=0;
            vanish();
        }
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(hp, getMaxHp())); // 保证血量在 0 和 maxHp 之间
    }

    public int getPower() {
        return power;
    }

    public int getShootNum() {
        return shootNum;
    }

    public void setShootNum(int shootNum) {
        this.shootNum = shootNum;
    }

    public int getDirection() {
        return direction;
    }

    /**
     * 飞机射击方法，可射击对象必须实现
     * @return
     *  可射击对象需实现，返回子弹
     *  非可射击对象空实现，返回null
     */
    public synchronized void setShootStrategy(ShootStrategy strategy, int durationSeconds, int priority) {
        if (strategy == null) return;

        if (shootStrategy == null || priority > this.shootStrategyPriority) {
            // 高优先级 → 立即激活
            activateStrategyWithTimer(strategy, durationSeconds, priority);
        }
        else if (strategy.getClass().equals(this.shootStrategy.getClass())) {
            // 相同策略 → 刷新计时
            refreshStrategyTimer(durationSeconds);
            System.out.println("Refreshed " + strategy.getClass().getSimpleName() + " duration");
        }
        else if (priority < this.shootStrategyPriority) {
            // 低优先级 → 延迟生效策略
            long currentTime = System.currentTimeMillis();
            long remainingHighPriority = strategyEndTime - currentTime; // 高级策略剩余时间

            this.pendingShootStrategy = strategy;
            this.pendingPriority = priority;

            // 剩余时间 = 道具持续时间 - 高级策略剩余时间
            int remainingSeconds = (int) Math.max(0, durationSeconds - (remainingHighPriority / 1000));
            this.pendingDurationSeconds = remainingSeconds;

            System.out.println("Stored pending strategy: " + strategy.getClass().getSimpleName()
                    + " (will remain " + remainingSeconds + "s after current ends)");
        }
    }



    public void activateStrategy(ShootStrategy strategy, int durationSeconds, int priority) {
        long currentTime = System.currentTimeMillis();

        this.shootStrategy = strategy;
        this.shootStrategyPriority = priority;
        this.shootStrategyEndTime = currentTime + durationSeconds * 1000L;

        if (strategy instanceof DirectShootStrategy) {
            this.setShootNum(1);
        } else if (strategy instanceof ScatterShootStrategy) {
            this.setShootNum(3);
        } else if (strategy instanceof CircleShootStrategy) {
            this.setShootNum(20);
        }

        System.out.println("Activated strategy: " + strategy.getClass().getSimpleName()
                + " for " + durationSeconds + "s");
    }

    private void activateStrategyWithTimer(ShootStrategy strategy, int durationSeconds, int priority) {
        if (strategyTimerThread != null && strategyTimerThread.isAlive()) {
            strategyActive = false;
            strategyTimerThread.interrupt();
        }

        activateStrategy(strategy, durationSeconds, priority);

        long startTime = System.currentTimeMillis();
        strategyEndTime = startTime + durationSeconds * 1000L;

        strategyActive = true;
        strategyTimerThread = new Thread(() -> {
            try {
                Thread.sleep(durationSeconds * 1000L);
                synchronized (AbstractAircraft.this) {
                    if (!strategyActive) return;

                    if (pendingShootStrategy != null) {
                        long currentTime = System.currentTimeMillis();
                        long remaining = pendingDurationSeconds * 1000L
                                - (currentTime - strategyEndTime);

                        // 若还有剩余时间则激活pending
                        if (remaining > 0) {
                            int remainingSeconds = (int) (remaining / 1000);
                            System.out.println("Switching to pending strategy: " +
                                    pendingShootStrategy.getClass().getSimpleName() +
                                    " (remaining " + remainingSeconds + "s)");
                            activateStrategyWithTimer(pendingShootStrategy, remainingSeconds, pendingPriority);
                        } else {
                            System.out.println("Pending strategy expired before activation, ignored.");
                            activateStrategy(new DirectShootStrategy(), Integer.MAX_VALUE, 0);
                        }
                        pendingShootStrategy = null;
                    } else {
                        activateStrategy(new DirectShootStrategy(), Integer.MAX_VALUE, 0);
                        System.out.println("Back to DirectShootStrategy");
                    }
                }
            } catch (InterruptedException ignored) {}
        });
        strategyTimerThread.start();
    }



    private void refreshStrategyTimer(int durationSeconds) {
        // 终止旧线程并重启一个新的倒计时线程
        if (strategyTimerThread != null && strategyTimerThread.isAlive()) {
            strategyActive = false;
            strategyTimerThread.interrupt();
        }

        strategyActive = true;
        strategyTimerThread = new Thread(() -> {
            try {
                Thread.sleep(durationSeconds * 1000L);
                if (strategyActive) {
                    synchronized (AbstractAircraft.this) {
                        if (pendingShootStrategy != null) {
                            activateStrategyWithTimer(pendingShootStrategy, pendingDurationSeconds, pendingPriority);
                            pendingShootStrategy = null;
                        } else {
                            activateStrategy(new DirectShootStrategy(), Integer.MAX_VALUE, 0);
                            System.out.println("Back to DirectShootStrategy");
                        }
                    }
                }
            } catch (InterruptedException ignored) {}
        });
        strategyTimerThread.start();
    }


    public List<BaseBullet> shoot() {
        {
            if (shootStrategy == null) {
                return new LinkedList<>();
            }
            return shootStrategy.shoot(this);
        }
    };

}


