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
    super(context, "aozora.db", null, 1);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE "+ Download.TABLE+" " +
            "( "+Download.NAME+" TEXT UNIQUE NOT NULL" +
            ", "+Download.LAST_MODIFIED+" TEXT NOT NULL" +
            ")");
    db.execSQL("CREATE TABLE "+ Opus.TABLE+" " +
            "( "+ Opus._ID+" INTEGER UNIQUE NOT NULL" +
            ", "+ Opus.TITLE+" TEXT NOT NULL" +
            ", "+ Opus.SORT_TITLE+" TEXT NOT NULL" +
            ", "+ Opus.SUBTITLE+" TEXT" +
            ", "+ Opus.READING_SUBTITLE+" TEXT" +
            ", "+ Opus.CARD_PERSON_ID+" INTEGER NOT NULL" +
            ", "+ Opus.CARD_URL+" TEXT NOT NULL" +
            ")");
    db.execSQL("CREATE TABLE "+ Person.TABLE+" " +
            "( "+ Person._ID+" INTEGER UNIQUE NOT NULL" +
            ", "+ Person.FAMILY_NAME+" TEXT NOT NULL" +
            ", "+ Person.SORT_FAMILY_NAME+" TEXT NOT NULL" +
            ", "+ Person.PERSONAL_NAME+" TEXT NOT NULL" +
            ", "+ Person.SORT_PERSONAL_NAME+" TEXT NOT NULL" +
            ")");
    db.execSQL("CREATE TABLE "+ OpusPersonLink.TABLE+" " +
            "( "+ OpusPersonLink.OPUS_ID +" INTEGER NOT NULL" +
            ", "+ OpusPersonLink.PERSON_ID +" INTEGER NOT NULL" +
            ", "+ OpusPersonLink.KIND+" TEXT NOT NULL" +
            ", UNIQUE("+ OpusPersonLink.OPUS_ID +", "+ OpusPersonLink.PERSON_ID +")" +
            ")");
    db.execSQL("CREATE TABLE "+Original.TABLE+" " +
            "( "+Original.OPUS_ID +" INTEGER NOT NULL" +
            ", "+Original.NUMBER+" INTEGER NOT NULL" +
            ", "+Original.NAME+" TEXT NOT NULL" +
            ", "+Original.PUBLISHER+" TEXT NOT NULL" +
            ", "+Original.YEAR_OF_FIRST_PUBLICATION+" TEXT NOT NULL" +
            ", "+Original.INPUT_PUBLICATION+" TEXT NOT NULL" +
            ", "+Original.PROOFREADING_PUBLICATION+" TEXT NOT NULL" +
            ", UNIQUE("+Original.OPUS_ID +", "+Original.NUMBER+")" +
            ")");
    db.execSQL("CREATE TABLE file " +
            "( "+File.OPUS_ID +" INTEGER NOT NULL" +
            ", "+File.KIND+" TEXT NOT NULL" +
            ", "+File.URL+" TEXT NOT NULL" +
            ", "+File.LAST_UPDATE+" TEXT NOT NULL" +
            ", "+File.CHARSET+" TEXT NOT NULL" +
            ", UNIQUE("+File.OPUS_ID +", "+File.KIND+")" +
            ")");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS download");
    db.execSQL("DROP TABLE IF EXISTS file");
    db.execSQL("DROP TABLE IF EXISTS person");
    db.execSQL("DROP TABLE IF EXISTS original");
    db.execSQL("DROP TABLE IF EXISTS opus_person_link");
    db.execSQL("DROP TABLE IF EXISTS card");
    onCreate(db);
  }
}
