package edu.hitsz.enemyfactory;

import edu.hitsz.aircraft.*;
import edu.hitsz.application.Main;
import edu.hitsz.application.ImageManager;

public class ElitePlusEnemyFactory implements EnemyFactory {

    @Override
    public AbstractAircraft createEnemy() {
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = (Math.random() < 0.5) ? -3 : 3;
        int speedY = 8;
        int hp = 30;

        return new ElitePlusEnemy(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public AbstractAircraft createEnemy(int ElitePlusEnemyHp) {
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = (Math.random() < 0.5) ? -3 : 3;
        int speedY = 8;
        int hp = ElitePlusEnemyHp;

        return new ElitePlusEnemy(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public AbstractAircraft createEnemy(int ElitePlusEnemyHp,int ElitePlusEnemySpeed) {
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = (Math.random() < 0.5) ? -3 : 3;
        int speedY = ElitePlusEnemySpeed;
        int hp = ElitePlusEnemyHp;

        return new ElitePlusEnemy(locationX, locationY, speedX, speedY, hp);
    }
}
