package edu.hitsz.property;

public class BombFactory extends PropertyFactory {
    @Override
    public BaseProperty createProperty(int locationX, int locationY, int speedX, int speedY) {
        return new BombProperty(locationX, locationY, speedX, speedY);
    }
}