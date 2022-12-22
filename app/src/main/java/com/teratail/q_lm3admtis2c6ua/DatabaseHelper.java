package com.teratail.q_lm3admtis2c6ua;

import android.content.Context;
import android.database.sqlite.*;

import androidx.annotation.Nullable;

import com.teratail.q_lm3admtis2c6ua.AozoraDatabase.*;

public class DatabaseHelper extends SQLiteOpenHelper {
  private static DatabaseHelper instance;

  public static DatabaseHelper getInstance(Context context) {
    if(instance == null) instance = new DatabaseHelper(context.getApplicationContext());
    return instance;
  }

  private DatabaseHelper(@Nullable Context context) {
    super(context, "aozora.db", null, 3);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE "+ Download.TABLE+" " +
            "( "+Download._ID+" INTEGER PRIMARY KEY" +
            ", "+Download.LAST_MODIFIED+" TEXT NOT NULL" +
            ")");
    db.execSQL("CREATE TABLE "+ Card.TABLE+" " +
            "( "+Card._ID+" INTEGER PRIMARY KEY" +
            ", "+Card.TITLE+" TEXT NOT NULL" +
            ", "+Card.SORT_TITLE+" TEXT NOT NULL" +
            ", "+Card.SUBTITLE+" TEXT" +
            ", "+Card.CARD_URL+" TEXT NOT NULL" +
            ", "+Card.AUTHOR_ID+" INTEGER NOT NULL" +
            ")");
    db.execSQL("CREATE TABLE "+Author.TABLE+" " +
            "( "+Author._ID+" INTEGER PRIMARY KEY" +
            ", "+Author.FAMILY_NAME+" TEXT NOT NULL" +
            ", "+Author.SORT_FAMILY_NAME+" TEXT NOT NULL" +
            ", "+Author.PERSONAL_NAME+" TEXT NOT NULL" +
            ", "+Author.SORT_PERSONAL_NAME+" TEXT NOT NULL" +
            ")");
    db.execSQL("CREATE TABLE file " +
            "( "+File.CARD_ID+" INTEGER NOT NULL" +
            ", "+File.KIND+" TEXT NOT NULL" +
            ", "+File.URL+" TEXT NOT NULL" +
            ", "+File.LAST_UPDATE+" TEXT NOT NULL" +
            ", "+File.CHARSET+" TEXT NOT NULL" +
            ", UNIQUE("+File.CARD_ID+", "+File.KIND+")" +
            ")");

    db.execSQL("INSERT INTO "+Download.TABLE+" ("+Download.LAST_MODIFIED+") VALUES(?)", new String[]{""}); //一行だけ作成
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS download");
    db.execSQL("DROP TABLE IF EXISTS file");
    db.execSQL("DROP TABLE IF EXISTS author");
    db.execSQL("DROP TABLE IF EXISTS card");
    onCreate(db);
  }
}
