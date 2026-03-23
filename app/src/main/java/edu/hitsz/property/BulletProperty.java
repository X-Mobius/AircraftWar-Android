package edu.hitsz.property;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.strategy.ScatterShootStrategy;
import edu.hitsz.sound.SoundManager;

public class BulletProperty extends BaseProperty {

    public BulletProperty(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft hero) {
        System.out.println("FireSupply active!");
        SoundManager.playSoundEffect("src/videos/get_supply.wav");
        // 切换英雄机射击策略为散射
        hero.setShootStrategy(new ScatterShootStrategy(), 3, 1);
    }
}