package com.teratail.q_lm3admtis2c6ua;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

public class DownloadWorker extends Worker {
  private static final String LOG_TAG = DownloadWorker.class.getSimpleName();
  private static final String TARGET_URL = "https://www.aozora.gr.jp/index_pages/list_person_all_extended_utf8.zip";
  private static final String TARGET_FILE = "list_person_all_extended_utf8.csv";

  private URL url;
  private DatabaseHelper helper;

  public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) throws MalformedURLException {
    super(context, workerParams);

    url = new URL(TARGET_URL);
    helper = DatabaseHelper.getInstance(context);
  }

  @NonNull
  @Override
  public Result doWork() {
    Log.d(LOG_TAG, "start");
    try {
      Log.d(LOG_TAG, "url=" + url);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.connect();
      try {
        String lastModified = con.getHeaderField("last-modified");
        Log.d(LOG_TAG, "lastModified=" + lastModified);
        if(!isDownloaded(lastModified)) {
          Log.d(LOG_TAG, "download");
          ZipInputStream zis = new ZipInputStream(new BufferedInputStream(con.getInputStream()));
          try {
            for(ZipEntry entry; (entry = zis.getNextEntry()) != null; ) {
              if(entry != null && entry.getName().equals(TARGET_FILE)) {
                SQLiteDatabase db = helper.getWritableDatabase();
                db.beginTransaction();
                try {
                  parseCsv(db, new BufferedReader(new InputStreamReader(zis)));
                  setDownloaded(db, lastModified);
                  db.setTransactionSuccessful();
                } finally {
                  db.endTransaction();
                }
                break;
              }
              zis.closeEntry();
            }
          } finally {
            zis.close();
          }
        }
      } finally {
        con.disconnect();
      }
    } catch(Exception e) {
      e.printStackTrace();
      return Result.failure();
    }
    Log.d(LOG_TAG, "end");
    return Result.success();
  }

  private void parseCsv(SQLiteDatabase db, Reader r) throws IOException {
    LineFeedFactionReader reader = new LineFeedFactionReader(r);
    CsvParser parser = new CsvParser();

    String header = reader.readLine(); //一行目はヘッダ
    Log.d(LOG_TAG, "header=" + header);
    List<String> columns = new ArrayList<>();
    parser.parse(header, columns::add);

    List<String> tokens = new ArrayList<>(columns.size());
    parser.parse(reader, token -> {
      tokens.add(token);
      if(tokens.size() == columns.size()) {
        try {
          insertDatabase(db, tokens);
        } catch(Exception e) {
          Log.d(LOG_TAG, "tokens=" + tokens.toString());
          throw e;
        }
        tokens.clear();
      }
    });
  }

  private boolean isDownloaded(String lastModified) {
    SQLiteDatabase db = helper.getReadableDatabase();
    try(Cursor cursor = db.rawQuery("SELECT last_modified FROM download WHERE last_modified=? LIMIT 1",
            new String[]{lastModified})) {
      return cursor.moveToNext();
    }
  }
  private void setDownloaded(SQLiteDatabase db, String lastModified) {
    db.execSQL("INSERT OR REPLACE INTO download (last_modified) VALUES (?)", new String[]{lastModified});
  }

  private void insertDatabase(SQLiteDatabase db, List<String> tokens) {
    long cardId = Long.parseLong(tokens.get(0)); //作品ID
    String title = tokens.get(1); //作品名
    String sortTitle = tokens.get(3); //ソート用読み
    String subtitle = tokens.get(4); //副題
    String cardUrl = tokens.get(13); //図書カードURL

    long authorId = Long.parseLong(tokens.get(14)); //人物ID
    String authorFamily = tokens.get(15); //姓
    String authorPersonal = tokens.get(16); //名
    String sortAuthorFamily = tokens.get(19); //ソート用姓読み
    String sortAuthorPersonal = tokens.get(20); //ソート用名読み

    String textUrl = tokens.get(45); //テキストファイルURL
    String textLastUpdate = tokens.get(46); //テキストファイル最終更新日
    String textCharset = tokens.get(47); // テキストファイル符号化方式

    String htmlUrl = tokens.get(50); //(x)htmlファイルURL
    String htmlLastUpdate = tokens.get(51); //(x)htmlファイル最終更新日
    String htmlCharset = tokens.get(52); //(x)htmlファイル符号化方式

    insertAuthor(db, authorId, authorFamily, sortAuthorFamily, authorPersonal, sortAuthorPersonal);
    insertCard(db, cardId, title, sortTitle, subtitle, cardUrl, authorId);
    insertFile(db, cardId, "text", textUrl, textLastUpdate, textCharset);
    insertFile(db, cardId, "html", htmlUrl, htmlLastUpdate, htmlCharset);
  }

  private void insertCard(SQLiteDatabase db, long cardId, String title, String sortTitle, String subtitle, String cardUrl, long authorId) {
    SQLiteStatement stmt = db.compileStatement("INSERT OR REPLACE INTO card (_id, title, sort_title, subtitle, card_url, author_id) VALUES (?,?,?,?,?,?)");
    try {
      stmt.bindLong(1, cardId);
      stmt.bindString(2, title);
      stmt.bindString(3, sortTitle);
      stmt.bindString(4, subtitle);
      stmt.bindString(5, cardUrl);
      stmt.bindLong(6, authorId);
      stmt.executeInsert();
    } finally {
      stmt.close();
    }
  }

  private void insertAuthor(SQLiteDatabase db, long authorId, String familyName, String sortFamilyName, String personalName, String sortPersonalName) {
    SQLiteStatement stmt = db.compileStatement("INSERT OR REPLACE INTO author (_id, family_name, sort_family_name, personal_name, sort_personal_name) VALUES (?,?,?,?,?)");
    try {
      stmt.bindLong(1, authorId);
      stmt.bindString(2, familyName);
      stmt.bindString(3, sortFamilyName);
      stmt.bindString(4, personalName);
      stmt.bindString(5, sortPersonalName);
      stmt.executeInsert();
    } finally {
      stmt.close();
    }
  }

  private void insertFile(SQLiteDatabase db, long cardId, String kind, String url, String last_update, String charset) {
    SQLiteStatement stmt = db.compileStatement("INSERT OR REPLACE INTO file" + " (card_id, kind, url, last_update, charset) VALUES (?,?,?,?,?)");
    try {
      stmt.bindLong(1, cardId);
      stmt.bindString(2, kind);
      stmt.bindString(3, url);
      stmt.bindString(4, last_update);
      stmt.bindString(5, charset);
      stmt.executeInsert();
    } finally {
      stmt.close();
    }
  }
}
