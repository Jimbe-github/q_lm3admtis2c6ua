package com.teratail.q_lm3admtis2c6ua;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.*;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
  private static DatabaseHelper instance;

  /**
   * @param context Context
   * @return object of DatabaseHelper
   */
  static DatabaseHelper getInstance(Context context) {
    if(instance == null) instance = new DatabaseHelper(context.getApplicationContext());
    return instance;
  }

  private DatabaseHelper(@Nullable Context context) {
    super(context, "aozora.db", null, 1);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE download " +
            "( last_modified TEXT NOT NULL" +
            ")");
    db.execSQL("CREATE TABLE card " +
            "( _id INTEGER UNIQUE NOT NULL" +
            ", title TEXT NOT NULL" +
            ", sort_title TEXT NOT NULL" +
            ", subtitle TEXT" +
            ", card_url TEXT NOT NULL" +
            ", author_id INTEGER NOT NULL" +
            ")");
    db.execSQL("CREATE TABLE author " +
            "( _id INTEGER PRIMARY KEY" +
            ", family_name TEXT NOT NULL" +
            ", sort_family_name TEXT NOT NULL" +
            ", personal_name TEXT NOT NULL" +
            ", sort_personal_name TEXT NOT NULL" +
            ")");
    db.execSQL("CREATE TABLE file " +
            "( _id INTEGER PRIMARY KEY" +
            ", card_id INTEGER NOT NULL" +
            ", kind TEXT NOT NULL" +
            ", url TEXT NOT NULL" +
            ", last_update TEXT NOT NULL" +
            ", charset TEXT NOT NULL" +
            ", UNIQUE(card_id, kind)" +
            ")");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }
}
