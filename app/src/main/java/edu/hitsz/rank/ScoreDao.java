package edu.hitsz.rank;

import java.util.List;

public interface ScoreDao {
    void addRecord(ScoreRecord record);
    List<ScoreRecord> getAllRecords();
    void saveAllRecords(List<ScoreRecord> records);
    void deleteRecordById(long id);
}
