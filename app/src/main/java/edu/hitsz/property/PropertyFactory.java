package edu.hitsz.property;

public abstract class PropertyFactory {
    public abstract BaseProperty createProperty(int locationX, int locationY, int speedX, int speedY);
}
