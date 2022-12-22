package com.teratail.q_lm3admtis2c6ua;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

public class DownloadWorker extends Worker {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = DownloadWorker.class.getSimpleName();

  private static final String TARGET_URL = "https://www.aozora.gr.jp/index_pages/list_person_all_extended_utf8.zip";
  private static final String TARGET_FILE = "list_person_all_extended_utf8.csv";

  private URL url;
  private DatabaseHelper helper;
  private ContentResolver resolver;

  public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) throws MalformedURLException {
    super(context, workerParams);

    url = new URL(TARGET_URL);
    helper = DatabaseHelper.getInstance(context);
    resolver = context.getContentResolver();
  }

  @NonNull
  @Override
  public Result doWork() {
    Log.d(LOG_TAG, "start");
    try {
      Log.d(LOG_TAG, "url=" + url);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      try {
        String lastModified = con.getHeaderField("last-modified");
        Log.d(LOG_TAG, "lastModified=" + lastModified);
        if(!isDownloaded(lastModified)) {
          Log.d(LOG_TAG, "download");
          try(ZipInputStream zis = new ZipInputStream(new BufferedInputStream(con.getInputStream()))) {
            for(ZipEntry entry; (entry = zis.getNextEntry()) != null; ) {
              if(entry != null && entry.getName().equals(TARGET_FILE)) {
                storeCsv(new BufferedReader(new InputStreamReader(zis)), lastModified);
                break;
              }
              zis.closeEntry();
            }
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

  private void storeCsv(Reader r, String lastModified) throws IOException {
    LineFeedFactionReader reader = new LineFeedFactionReader(r);
    CsvParser parser = new CsvParser();

    String header = reader.readLine(); //一行目はヘッダ
    Log.d(LOG_TAG, "header=" + header);
    List<String> columns = new ArrayList<>();
    parser.parse(header, columns::add);

    SQLiteDatabase db = helper.getWritableDatabase();

    db.beginTransaction();
    AozoraDatabase.Storer storer = new AozoraDatabase.Storer(db);
    try {
      List<String> tokens = new ArrayList<>(columns.size());
      parser.parse(reader, token -> {
        tokens.add(token);
        if(tokens.size() < columns.size()) return;
        try {
          long cardId = Long.parseLong(tokens.get(0)); //作品ID
          long authorId = Long.parseLong(tokens.get(14)); //人物ID
          //15=姓, 16=名, 19=ソート用姓読み, 20=ソート用名読み
          storer.upsertAuthor(authorId, tokens.get(15), tokens.get(16), tokens.get(19), tokens.get(20));
          //1=作品名, 3=ソート用読み, 4=副題, 13=図書カードURL
          storer.upsertCard(cardId, tokens.get(1), tokens.get(3), tokens.get(4), tokens.get(13), authorId);
          //45=テキストファイルURL, 46=テキストファイル最終更新日, 47=テキストファイル符号化方式
          storer.upsertFile(cardId, "text", tokens.get(45), tokens.get(46), tokens.get(47));
          //50=(x)htmlファイルURL, 51=(x)htmlファイル最終更新日, 52=(x)htmlファイル符号化方式
          storer.upsertFile(cardId, "html", tokens.get(50), tokens.get(51), tokens.get(52));
        } catch(Exception e) {
          Log.d(LOG_TAG, "tokens=" + tokens.toString());
          throw e;
        }
        tokens.clear();
      });
      storer.updateDownload(lastModified);
      db.setTransactionSuccessful();
    } finally {
      storer.close();
      db.endTransaction();
    }

    notifyInsert(AozoraDatabase.Author.CONTENT_URI);
    notifyInsert(AozoraDatabase.Card.CONTENT_URI);
    notifyInsert(AozoraDatabase.File.CONTENT_URI);
    notifyInsert(AozoraDatabase.Download.CONTENT_URI);
  }

  private boolean isDownloaded(String lastModified) {
    SQLiteDatabase db = helper.getReadableDatabase();
    try(Cursor cursor = db.rawQuery("SELECT last_modified FROM download WHERE last_modified=? LIMIT 1", new String[]{lastModified})) {
      return cursor.moveToNext();
    }
  }

  private void notifyInsert(Uri uri) {
    Uri noopUri = uri.buildUpon().path(uri.getPath() + AozoraContentProvider.SUFFIX_NOOP).build();
    resolver.insert(noopUri, null);
  }
}
