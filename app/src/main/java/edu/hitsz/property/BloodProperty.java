package edu.hitsz.property;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.sound.SoundManager;

public class BloodProperty extends BaseProperty {

    private int healPoints = 100;  // 回复血量

    public BloodProperty(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft hero) {
        // 不能超过最大初始血量
        int newHp = Math.min(hero.getHp() + healPoints, hero.getMaxHp());
        hero.setHp(newHp);
        SoundManager.playSoundEffect("src/videos/get_supply.wav");
        System.out.println("BloodSupply active! 英雄回血 +" + healPoints + "，当前血量：" + hero.getHp());
    }
}
