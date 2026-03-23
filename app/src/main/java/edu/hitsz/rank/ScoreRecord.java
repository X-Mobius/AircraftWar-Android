package edu.hitsz.rank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScoreRecord implements Comparable<ScoreRecord> {
    private String playerName;
    private int score;
    private String time;

    public ScoreRecord(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
        // 自动生成当前时间字符串
        this.time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public ScoreRecord(String playerName, int score, String time) {
        this.playerName = playerName;
        this.score = score;
        this.time = time;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public String getTime() {
        return time;
    }

    @Override
    public int compareTo(ScoreRecord o) {
        // 按分数降序排列
        return Integer.compare(o.score, this.score);
    }

    @Override
    public String toString() {
        return String.format("%-10s %-6d %s", playerName, score, time);
    }
}
