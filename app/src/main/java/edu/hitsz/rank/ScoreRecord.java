package edu.hitsz.rank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScoreRecord implements Comparable<ScoreRecord> {
    private final long id;
    private final String playerName;
    private final int score;
    private final String time;

    public ScoreRecord(String playerName, int score) {
        this(-1L, playerName, score,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public ScoreRecord(String playerName, int score, String time) {
        this(-1L, playerName, score, time);
    }

    public ScoreRecord(long id, String playerName, int score, String time) {
        this.id = id;
        this.playerName = playerName;
        this.score = score;
        this.time = time;
    }

    public long getId() {
        return id;
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
    public int compareTo(ScoreRecord other) {
        return Integer.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return String.format("%-10s %-6d %s", playerName, score, time);
    }
}
