package edu.hitsz.rank;

import java.util.Collections;
import java.util.List;

public class ScoreService {
    private final ScoreDao dao = new ScoreDaoImpl();

    public void recordScore(int score) {
        // 暂时不需要输入玩家名
        ScoreRecord record = new ScoreRecord("Player", score);
        dao.addRecord(record);
    }

    public void printRankList() {
        List<ScoreRecord> list = dao.getAllRecords();
        Collections.sort(list);

        System.out.println("\n===== 得分排行榜 =====");
        System.out.printf("%-3s %-8s %-6s %s\n", "名次", "玩家名", "得分", "记录时间");
        for (int i = 0; i < list.size(); i++) {
            ScoreRecord r = list.get(i);
            System.out.printf("%-4d %-10s %-6d %s\n", i + 1, r.getPlayerName(), r.getScore(), r.getTime());
        }
        System.out.println("=====================");
    }
}
