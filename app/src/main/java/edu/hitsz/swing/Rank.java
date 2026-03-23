package edu.hitsz.swing;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.rank.ScoreDao;
import edu.hitsz.rank.ScoreDaoImpl;
import edu.hitsz.rank.ScoreRecord;
import edu.hitsz.rank.ScoreService;

/**
 * Legacy ranking compatibility class.
 * Swing table UI is removed; data access capability is kept for migration continuity.
 */
public class Rank {

    private final ScoreService scoreService = new ScoreService();
    private final ScoreDao scoreDao = new ScoreDaoImpl();
    private final String difficulty;

    public Rank(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public List<ScoreRecord> getRecords() {
        List<ScoreRecord> records = new ArrayList<>(scoreDao.getAllRecords());
        records.sort(null);
        return records;
    }

    public void deleteRecord(int index) {
        List<ScoreRecord> records = new ArrayList<>(scoreDao.getAllRecords());
        if (index < 0 || index >= records.size()) {
            return;
        }
        records.remove(index);
        scoreDao.saveAllRecords(records);
    }

    public ScoreService getScoreService() {
        return scoreService;
    }

    public Object getMainPanel() {
        return null;
    }
}
