package edu.hitsz.property;

public class BulletFactory extends PropertyFactory {
    @Override
    public BaseProperty createProperty(int locationX, int locationY, int speedX, int speedY) {
        return new BulletProperty(locationX, locationY, speedX, speedY);
    }
}
