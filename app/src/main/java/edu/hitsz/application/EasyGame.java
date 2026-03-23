package edu.hitsz.application;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.enemyfactory.EliteEnemyFactory;
import edu.hitsz.enemyfactory.ElitePlusEnemyFactory;
import edu.hitsz.enemyfactory.EnemyFactory;
import edu.hitsz.enemyfactory.MobEnemyFactory;
import edu.hitsz.rank.ScoreDao;
import edu.hitsz.rank.ScoreDaoImpl;
import edu.hitsz.rank.ScoreRecord;
import edu.hitsz.sound.SoundManager;

public class EasyGame extends AbstractGame {

    public EasyGame() {
        initGameConfig();
    }

    public EasyGame(Context context) {
        super(context);
        initGameConfig();
    }

    private void initGameConfig() {
        this.backgroundImage = ImageManager.BACKGROUND_IMAGE_EASY;
        this.enemyMaxNumber = 3;
        this.cycleDuration = 600;
    }

    @Override
    protected void difficultyUpdate(int time) {
        // Easy mode keeps fixed difficulty.
    }

    @Override
    protected void onGameOver() {
        SoundManager.stopBgm();
        SoundManager.playSoundEffect("src/videos/game_over.wav", -15f);

        int finalScore = score;
        String playerName = "Player";

        ScoreRecord record = new ScoreRecord(playerName, finalScore);
        ScoreDao scoreDao = new ScoreDaoImpl();
        scoreDao.addRecord(record);

        Log.i("EasyGame", "Game over. mode=easy, score=" + finalScore);
        post(() -> Toast.makeText(getContext(), "Game Over, Score: " + finalScore, Toast.LENGTH_SHORT).show());
    }

    protected void generateEnemy() {
        if (enemyAircrafts.size() < enemyMaxNumber) {
            EnemyFactory factory;

            // Spawn mob/elite enemies by probability.
            double random = Math.random();
            if (random < 0.15) {
                factory = new ElitePlusEnemyFactory();
            } else if (random < 0.35) {
                factory = new EliteEnemyFactory();
            } else {
                factory = new MobEnemyFactory();
            }
            AbstractAircraft enemy = factory.createEnemy();
            enemyAircrafts.add(enemy);
        }
    }
}
