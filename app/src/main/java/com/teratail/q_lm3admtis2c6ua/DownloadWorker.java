package com.teratail.q_lm3admtis2c6ua;

import static com.teratail.q_lm3admtis2c6ua.AozoraDatabase.Author;
import static com.teratail.q_lm3admtis2c6ua.AozoraDatabase.Card;
import static com.teratail.q_lm3admtis2c6ua.AozoraDatabase.Download;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
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

  private static class Storer {
    private SQLiteStatement upsertAuthorStmt, upsertCardStmt, upsertFileStmt, updateDownloadStmt;

    Storer(SQLiteDatabase db) {
      upsertAuthorStmt = db.compileStatement("INSERT OR REPLACE INTO "+Author.TABLE+"(" +
              Author._ID+","+Author.FAMILY_NAME+","+Author.SORT_FAMILY_NAME+","+Author.PERSONAL_NAME+","+Author.SORT_PERSONAL_NAME +
              ") VALUES (?,?,?,?,?)");
      upsertCardStmt = db.compileStatement("INSERT OR REPLACE INTO "+Card.TABLE+"(" +
              Card._ID+","+Card.TITLE+","+Card.SORT_TITLE+","+Card.SUBTITLE+","+Card.CARD_URL+","+Card.AUTHOR_ID +
              ") VALUES (?,?,?,?,?,?)");
      upsertFileStmt = db.compileStatement("INSERT OR REPLACE INTO "+AozoraDatabase.File.TABLE+"(" +
              AozoraDatabase.File.CARD_ID+","+AozoraDatabase.File.KIND+","+AozoraDatabase.File.URL+","+AozoraDatabase.File.LAST_UPDATE+","+AozoraDatabase.File.CHARSET +
              ") VALUES (?,?,?,?,?)");
      updateDownloadStmt = db.compileStatement("UPDATE "+Download.TABLE+" SET "+Download.LAST_MODIFIED+"=?");
    }

    void upsertAuthor(long authorId, String familyName, String personalName, String sortFamilyName, String sortPersonalName) {
      upsertAuthorStmt.bindLong(1, authorId);
      upsertAuthorStmt.bindString(2, familyName); //姓
      upsertAuthorStmt.bindString(3, sortFamilyName); //ソート用姓読み
      upsertAuthorStmt.bindString(4, personalName); //名
      upsertAuthorStmt.bindString(5, sortPersonalName); //ソート用名読み
      upsertAuthorStmt.executeInsert();
    }

    void upsertCard(long cardId, String title, String sortTitle, String subtitle, String cardUrl, long authorId) {
      upsertCardStmt.bindLong(1, cardId);
      upsertCardStmt.bindString(2, title); //作品名
      upsertCardStmt.bindString(3, sortTitle); //ソート用読み
      upsertCardStmt.bindString(4, subtitle); //副題
      upsertCardStmt.bindString(5, cardUrl); //図書カードURL
      upsertCardStmt.bindLong(6, authorId);
      upsertCardStmt.executeInsert();
    }

    void upsertFile(long cardId, String kind, String url, String lastUpdate, String charset) {
      upsertFileStmt.bindLong(1, cardId);
      upsertFileStmt.bindString(2, kind); //種別('text'/'html')
      upsertFileStmt.bindString(3, url); //ファイルURL
      upsertFileStmt.bindString(4, lastUpdate); //ファイル最終更新日
      upsertFileStmt.bindString(5, charset); //ファイル符号化方式
      upsertFileStmt.executeInsert();
    }

    void updateDownload(String lastModified) {
      updateDownloadStmt.bindString(1, lastModified);
      updateDownloadStmt.executeUpdateDelete();
    }

    void close() {
      upsertAuthorStmt.close();
      upsertCardStmt.close();
      upsertFileStmt.close();
      updateDownloadStmt.close();
    }
  }

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
      con.connect();
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
    Storer storer = new Storer(db);
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

    notifyInsert(Author.CONTENT_URI);
    notifyInsert(Card.CONTENT_URI);
    notifyInsert(AozoraDatabase.File.CONTENT_URI);
    notifyInsert(Download.CONTENT_URI);
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
