package edu.hitsz.application;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.enemyfactory.BossEnemyFactory;
import edu.hitsz.enemyfactory.EliteEnemyFactory;
import edu.hitsz.enemyfactory.ElitePlusEnemyFactory;
import edu.hitsz.enemyfactory.EnemyFactory;
import edu.hitsz.enemyfactory.MobEnemyFactory;
import edu.hitsz.rank.ScoreDao;
import edu.hitsz.rank.ScoreDaoImpl;
import edu.hitsz.rank.ScoreRecord;
import edu.hitsz.sound.SoundManager;

/**
 * Legacy compatibility game class.
 * Kept to preserve old call sites while delegating the game loop/rendering to AbstractGame.
 */
public class Game extends AbstractGame {

    private final String difficulty;

    public Game() {
        this("easy");
    }

    public Game(String difficulty) {
        super();
        this.difficulty = normalizeDifficulty(difficulty);
        initGameConfig();
    }

    public Game(Context context, String difficulty) {
        super(context);
        this.difficulty = normalizeDifficulty(difficulty);
        initGameConfig();
    }

    private static String normalizeDifficulty(String difficulty) {
        return difficulty == null ? "easy" : difficulty.toLowerCase();
    }

    private void initGameConfig() {
        switch (difficulty) {
            case "easy":
                this.backgroundImage = ImageManager.BACKGROUND_IMAGE_EASY;
                this.enemyMaxNumber = 3;
                break;
            case "medium":
                this.backgroundImage = ImageManager.BACKGROUND_IMAGE_MEDIUM;
                this.enemyMaxNumber = 4;
                break;
            case "hard":
                this.backgroundImage = ImageManager.BACKGROUND_IMAGE_HARD;
                this.enemyMaxNumber = 5;
                break;
            default:
                this.backgroundImage = ImageManager.BACKGROUND_IMAGE;
                this.enemyMaxNumber = 4;
                break;
        }
        this.cycleDuration = 600;
    }

    @Override
    protected void difficultyUpdate(int time) {
        // Keep old Game behavior: no dynamic difficulty scaling in this compatibility class.
    }

    @Override
    protected void generateEnemy() {
        if (enemyAircrafts.size() >= enemyMaxNumber) {
            return;
        }

        EnemyFactory factory;

        boolean bossExists = false;
        for (AbstractAircraft enemy : enemyAircrafts) {
            if (enemy instanceof BossEnemy && !enemy.notValid()) {
                bossExists = true;
                break;
            }
        }

        if (!bossExists && score >= lastBossScore + 300) {
            factory = new BossEnemyFactory();
            AbstractAircraft boss = factory.createEnemy();
            enemyAircrafts.add(boss);
            SoundManager.playBossBgm();
            lastBossScore += 300;
            return;
        }

        double random = Math.random();
        if (random < 0.15) {
            factory = new ElitePlusEnemyFactory();
        } else if (random < 0.35) {
            factory = new EliteEnemyFactory();
        } else {
            factory = new MobEnemyFactory();
        }
        enemyAircrafts.add(factory.createEnemy());
    }

    @Override
    protected void onGameOver() {
        SoundManager.stopBgm();
        SoundManager.playSoundEffect("src/videos/game_over.wav", -15f);

        int finalScore = score;
        String playerName = "Player";

        ScoreDao scoreDao = new ScoreDaoImpl();
        scoreDao.addRecord(new ScoreRecord(playerName, finalScore));

        Log.i("Game", "Game over. mode=" + difficulty + ", score=" + finalScore);
        post(() -> Toast.makeText(getContext(), "Game Over, Score: " + finalScore, Toast.LENGTH_SHORT).show());
    }
}
