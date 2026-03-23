package edu.hitsz.property;

import edu.hitsz.application.Main;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.aircraft.HeroAircraft;

public abstract class BaseProperty extends AbstractFlyingObject {

    public BaseProperty(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴出界
        if (speedY > 0 && locationY >= Main.WINDOW_HEIGHT ) {
            // 向下飞行出界
            vanish();
        }else if (locationY <= 0){
            // 向上飞行出界
            vanish();
        }
    }


    /**
     * 英雄机获得补给时触发的效果
     * @param hero 英雄机
     */
    public abstract void activate(HeroAircraft hero);
}