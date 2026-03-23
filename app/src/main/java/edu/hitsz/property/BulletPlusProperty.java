package edu.hitsz.property;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.strategy.CircleShootStrategy;
import edu.hitsz.sound.SoundManager;

public class BulletPlusProperty extends BaseProperty {

    public BulletPlusProperty(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft hero) {
        System.out.println("FirePlusSupply active!");
        SoundManager.playSoundEffect("src/videos/get_supply.wav");
        hero.setShootStrategy(new CircleShootStrategy(), 3, 2);
    }
}