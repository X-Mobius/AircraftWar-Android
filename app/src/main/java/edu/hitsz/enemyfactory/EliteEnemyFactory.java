package edu.hitsz.enemyfactory;

import edu.hitsz.aircraft.*;
import edu.hitsz.application.Main;
import edu.hitsz.application.ImageManager;

public class EliteEnemyFactory implements EnemyFactory {

    @Override
    public AbstractAircraft createEnemy() {
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = 0;
        int speedY = 7;
        int hp = 30;

        return new EliteEnemy(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public AbstractAircraft createEnemy(int EliteEnemyHp) {
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = 0;
        int speedY = 7;
        int hp = EliteEnemyHp;

        return new EliteEnemy(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public AbstractAircraft createEnemy(int EliteEnemyHp, int EliteEnemySpeed) {
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = 0;
        int speedY = EliteEnemySpeed;
        int hp = EliteEnemyHp;

        return new EliteEnemy(locationX, locationY, speedX, speedY, hp);
    }
}