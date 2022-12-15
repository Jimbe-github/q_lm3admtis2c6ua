package com.teratail.q_lm3admtis2c6ua;

import static com.teratail.q_lm3admtis2c6ua.AozoraDatabase.Author;
import static com.teratail.q_lm3admtis2c6ua.AozoraDatabase.Card;
import static com.teratail.q_lm3admtis2c6ua.AozoraDatabase.Download;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
                ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                parseCsv(operations, new BufferedReader(new InputStreamReader(zis)));
                setDownloaded(operations, lastModified);
                resolver.applyBatch(AozoraContentProvider.AUTHORITY, operations);
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

  private void parseCsv(ArrayList<ContentProviderOperation> operations, Reader r) throws IOException {
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
          insertDatabase(operations, tokens);
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
  private void setDownloaded(ArrayList<ContentProviderOperation> operations, String lastModified) {
    operations.add(ContentProviderOperation.newUpdate(Download.CONTENT_URI)
            .withValue(Download.LAST_MODIFIED, lastModified)
            .build());
  }

  private void insertDatabase(ArrayList<ContentProviderOperation> operations, List<String> tokens) {
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

    operations.add(ContentProviderOperation.newInsert(Author.CONTENT_URI)
            .withValue(Author._ID, authorId)
            .withValue(Author.FAMILY_NAME, authorFamily)
            .withValue(Author.SORT_FAMILY_NAME, sortAuthorFamily)
            .withValue(Author.PERSONAL_NAME, authorPersonal)
            .withValue(Author.SORT_PERSONAL_NAME, sortAuthorPersonal)
            .build());

    operations.add(ContentProviderOperation.newInsert(Card.CONTENT_URI)
            .withValue(Card._ID, cardId)
            .withValue(Card.TITLE, title)
            .withValue(Card.SORT_TITLE, sortTitle)
            .withValue(Card.SUBTITLE, subtitle)
            .withValue(Card.CARD_URL, cardUrl)
            .withValue(Card.AUTHOR_ID, authorId)
            .build());

    operations.add(ContentProviderOperation.newInsert(AozoraDatabase.File.CONTENT_URI)
            .withValue(AozoraDatabase.File.CARD_ID, cardId)
            .withValue(AozoraDatabase.File.KIND, "text")
            .withValue(AozoraDatabase.File.URL, textUrl)
            .withValue(AozoraDatabase.File.LAST_UPDATE, textLastUpdate)
            .withValue(AozoraDatabase.File.CHARSET, textCharset)
            .build());

    operations.add(ContentProviderOperation.newInsert(AozoraDatabase.File.CONTENT_URI)
            .withValue(AozoraDatabase.File.CARD_ID, cardId)
            .withValue(AozoraDatabase.File.KIND, "html")
            .withValue(AozoraDatabase.File.URL, htmlUrl)
            .withValue(AozoraDatabase.File.LAST_UPDATE, htmlLastUpdate)
            .withValue(AozoraDatabase.File.CHARSET, htmlCharset)
            .build());
  }
}
