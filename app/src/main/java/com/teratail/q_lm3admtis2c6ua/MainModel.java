package com.teratail.q_lm3admtis2c6ua;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import androidx.annotation.NonNull;
import androidx.work.*;

import java.util.*;
import java.util.function.Consumer;

class CardSummary {
  final String title, subtitle, url, author;
  CardSummary(String title, String subtitle, String url, String author) {
    this.title = title;
    this.subtitle = subtitle;
    this.url = url;
    this.author = author;
  }
  @NonNull
  @Override
  public String toString() {
    return new StringBuilder(getClass().getSimpleName()).append('@').append(hashCode())
            .append("[title=").append(title)
            .append(",subtitle=").append(subtitle)
            .append(",url=").append(url)
            .append(",author=").append(author)
            .append(']').toString();
  }
}

class CardListWithAiueo {
  final Aiueo aiueo;
  final List<CardSummary> list;
  CardListWithAiueo(Aiueo aiueo, List<CardSummary> list) {
    this.aiueo = aiueo;
    this.list = Collections.unmodifiableList(list);
  }
}

public class MainModel {
  private static final String LOG_TAG = MainModel.class.getSimpleName();

  private DatabaseHelper helper;
  private WorkManager workManager;

  MainModel(Context context) {
    helper = new DatabaseHelper(context);

    workManager = WorkManager.getInstance(context);

    //最新チェック・ダウンロード
    workManager.enqueue(OneTimeWorkRequest.from(DownloadWorker.class));
  }

  void requestCardListWithAiueo(Aiueo aiueo, Consumer<CardListWithAiueo> callback) {
    List<CardSummary> list = new ArrayList<>();
    SQLiteDatabase db = helper.getReadableDatabase();

    String selection = "c.sort_title like ? || '%'";
    String[] selectionArgs = new String[]{"" + aiueo};
    if(aiueo == Aiueo.他) {
      selection = "(c.sort_title == '' or INSTR(?, SUBSTR(c.sort_title,1,1)) == 0)";
      StringBuilder sb = new StringBuilder();
      for(Aiueo a : Aiueo.values()) if(a != Aiueo.他) sb.append(a.toString());
      selectionArgs = new String[]{sb.toString()};
    }
    try(Cursor cursor = db.rawQuery("SELECT" +
            " c.title, c.subtitle, c.card_url, a.family_name || ' ' || a.personal_name as author" +
            " FROM card as c INNER JOIN author as a ON c.author_id = a._id" +
            " WHERE " + selection +
            " ORDER BY c.sort_title", selectionArgs)) {
      int titleIndex = cursor.getColumnIndex("title");
      int subtitleIndex = cursor.getColumnIndex("subtitle");
      int urlIndex = cursor.getColumnIndex("card_url");
      int authorIndex = cursor.getColumnIndex("author");
      while(cursor.moveToNext()) {
        String title = cursor.getString(titleIndex);
        String subtitle = cursor.getString(subtitleIndex);
        String url = cursor.getString(urlIndex);
        String author = cursor.getString(authorIndex);
        list.add(new CardSummary(title, subtitle, url, author));
      }
    }
    callback.accept(new CardListWithAiueo(aiueo, list));
  }
}