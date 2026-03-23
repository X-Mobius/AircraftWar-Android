package edu.hitsz.property;

public class BulletPlusFactory extends PropertyFactory {
    @Override
    public BaseProperty createProperty(int locationX, int locationY, int speedX, int speedY) {
        return new BulletPlusProperty(locationX, locationY, speedX, speedY);
    }
}
