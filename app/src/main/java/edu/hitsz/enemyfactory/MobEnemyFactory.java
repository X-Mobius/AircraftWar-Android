package edu.hitsz.enemyfactory;

import edu.hitsz.aircraft.*;
import edu.hitsz.application.Main;
import edu.hitsz.application.ImageManager;

public class MobEnemyFactory implements EnemyFactory {

    @Override
    public AbstractAircraft createEnemy() {
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = 0;
        int speedY = 6;
        int hp = 30;

        return new MobEnemy(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public AbstractAircraft createEnemy(int mobEnemyHp, int mobEnemySpeed) {
        int locationX = (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth()));
        int locationY = (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
        int speedX = 0;
        int speedY = mobEnemySpeed;
        int hp = mobEnemyHp;

        return new MobEnemy(locationX, locationY, speedX, speedY, hp);
    }
}
