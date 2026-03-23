package edu.hitsz.property;

public class BloodFactory extends PropertyFactory {
    @Override
    public BaseProperty createProperty(int locationX, int locationY, int speedX, int speedY) {
        return new BloodProperty(locationX, locationY, speedX, speedY);
    }
}
