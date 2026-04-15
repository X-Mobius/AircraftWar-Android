package edu.hitsz.rank;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ScoreDaoImpl implements ScoreDao {

    private final ScoreDbHelper dbHelper;

    public ScoreDaoImpl() {
        this(resolveContext());
    }

    public ScoreDaoImpl(Context context) {
        if (context == null) {
            throw new IllegalStateException("Context is required for ScoreDaoImpl.");
        }
        // 持有 Application Context，避免 Activity 泄漏。
        this.dbHelper = new ScoreDbHelper(context.getApplicationContext());
    }

    private static Context resolveContext() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object app = activityThread.getMethod("currentApplication").invoke(null);
            if (app instanceof Context) {
                return (Context) app;
            }
        } catch (Exception ignored) {
            // no-op
        }
        throw new IllegalStateException("Cannot resolve application context.");
    }

    @Override
    public void addRecord(ScoreRecord record) {
        // 结算流程的单条插入路径。
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ScoreDbHelper.COL_PLAYER_NAME, record.getPlayerName());
        values.put(ScoreDbHelper.COL_SCORE, record.getScore());
        values.put(ScoreDbHelper.COL_TIME, record.getTime());
        db.insert(ScoreDbHelper.TABLE_SCORE, null, values);
    }

    @Override
    public List<ScoreRecord> getAllRecords() {
        List<ScoreRecord> list = new ArrayList<>();
        // 昵称存在性校验（忽略大小写），用于结算页去重提示。
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ScoreDbHelper.TABLE_SCORE,
                new String[]{
                        ScoreDbHelper.COL_ID,
                        ScoreDbHelper.COL_PLAYER_NAME,
                        ScoreDbHelper.COL_SCORE,
                        ScoreDbHelper.COL_TIME
                },
                null,
                null,
                null,
                null,
                // 同分时按更早写入顺序保持稳定。
                ScoreDbHelper.COL_SCORE + " DESC, " + ScoreDbHelper.COL_ID + " ASC"
        );
        try {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(ScoreDbHelper.COL_ID));
                String playerName = cursor.getString(cursor.getColumnIndexOrThrow(ScoreDbHelper.COL_PLAYER_NAME));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(ScoreDbHelper.COL_SCORE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(ScoreDbHelper.COL_TIME));
                list.add(new ScoreRecord(id, playerName, score, time));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    @Override
    public void saveAllRecords(List<ScoreRecord> records) {
        // 结算流程的单条插入路径。
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // 全量覆盖操作使用事务，保证数据一致性。
        db.beginTransaction();
        try {
            db.delete(ScoreDbHelper.TABLE_SCORE, null, null);
            for (ScoreRecord record : records) {
                ContentValues values = new ContentValues();
                values.put(ScoreDbHelper.COL_PLAYER_NAME, record.getPlayerName());
                values.put(ScoreDbHelper.COL_SCORE, record.getScore());
                values.put(ScoreDbHelper.COL_TIME, record.getTime());
                db.insert(ScoreDbHelper.TABLE_SCORE, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void deleteRecordById(long id) {
        // 结算流程的单条插入路径。
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(
                ScoreDbHelper.TABLE_SCORE,
                ScoreDbHelper.COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    @Override
    public boolean existsPlayerName(String playerName) {
        if (playerName == null) {
            return false;
        }
        String normalizedName = playerName.trim();
        if (normalizedName.isEmpty()) {
            return false;
        }

        // 昵称存在性校验（忽略大小写），用于结算页去重提示。
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ScoreDbHelper.TABLE_SCORE,
                new String[]{ScoreDbHelper.COL_ID},
                "LOWER(" + ScoreDbHelper.COL_PLAYER_NAME + ") = LOWER(?)",
                new String[]{normalizedName},
                null,
                null,
                null,
                "1"
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }
}
