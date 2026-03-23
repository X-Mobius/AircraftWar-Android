package edu.hitsz.rank;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreDaoImpl implements ScoreDao {
    private final File file = resolveScoreFile();

    private static File resolveScoreFile() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object app = activityThread.getMethod("currentApplication").invoke(null);
            if (app != null) {
                File filesDir = (File) app.getClass().getMethod("getFilesDir").invoke(app);
                if (filesDir != null) {
                    return new File(filesDir, "scoreboard.txt");
                }
            }
        } catch (Exception ignored) {
            // Fallback for non-Android runtime.
        }
        return new File("scoreboard.txt");
    }

    @Override
    public void addRecord(ScoreRecord record) {
        List<ScoreRecord> list = getAllRecords();
        list.add(record);
        Collections.sort(list);
        saveAllRecords(list);
    }

    @Override
    public List<ScoreRecord> getAllRecords() {
        List<ScoreRecord> list = new ArrayList<>();
        if (!file.exists()) {
            return list;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 每行格式: playerName,score,time
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    list.add(new ScoreRecord(parts[0], Integer.parseInt(parts[1]), parts[2]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void saveAllRecords(List<ScoreRecord> records) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (ScoreRecord r : records) {
                bw.write(r.getPlayerName() + "," + r.getScore() + "," + r.getTime());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
