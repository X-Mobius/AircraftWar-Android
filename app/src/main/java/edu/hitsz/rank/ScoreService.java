package edu.hitsz.rank;

import java.util.List;

public class ScoreService {
    private final ScoreDao dao = new ScoreDaoImpl();

    public void recordScore(int score) {
        dao.addRecord(new ScoreRecord("Player", score));
    }

    public List<ScoreRecord> getRankList() {
        return dao.getAllRecords();
    }
}
