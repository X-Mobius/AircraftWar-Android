package edu.hitsz.rank;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ScoreDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "aircraft_war.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_SCORE = "score_records";
    public static final String COL_ID = "id";
    public static final String COL_PLAYER_NAME = "player_name";
    public static final String COL_SCORE = "score";
    public static final String COL_TIME = "time";

    public ScoreDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 排行榜 SQLite 表结构：id 为主键，name/score/time 为展示字段。
        String createSql = "CREATE TABLE IF NOT EXISTS " + TABLE_SCORE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PLAYER_NAME + " TEXT NOT NULL, "
                + COL_SCORE + " INTEGER NOT NULL, "
                + COL_TIME + " TEXT NOT NULL"
                + ")";
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 当前实验阶段采用直接重建表的升级策略。
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORE);
        onCreate(db);
    }
}
