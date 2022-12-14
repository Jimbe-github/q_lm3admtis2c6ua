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
import java.util.regex.*;
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
      loadAozora(new URL(TARGET_URL), TARGET_FILE, this::storeCsv1);

      notifyInsert(AozoraDatabase.Person.CONTENT_URI);
      notifyInsert(AozoraDatabase.Opus.CONTENT_URI);
      notifyInsert(AozoraDatabase.Original.CONTENT_URI);
      notifyInsert(AozoraDatabase.File.CONTENT_URI);
      notifyInsert(AozoraDatabase.Download.CONTENT_URI);
    } catch(Exception e) {
      e.printStackTrace();
      return Result.failure();
    }
    Log.d(LOG_TAG, "end");
    return Result.success();
  }

  private void loadAozora(URL url, String filename, storerFunc storeFunc) throws IOException {
    Log.d(LOG_TAG, "url=" + url);
    HttpURLConnection con = (HttpURLConnection)url.openConnection();
    con.setRequestMethod("GET");
    try {
      String lastModified = con.getHeaderField("last-modified");
      Log.d(LOG_TAG, "lastModified=" + lastModified);
      if(!isDownloaded(filename, lastModified)) {
        Log.d(LOG_TAG, "download");
        try(ZipInputStream zis = new ZipInputStream(new BufferedInputStream(con.getInputStream()))) {
          for(ZipEntry entry; (entry = zis.getNextEntry()) != null; ) {
            if(entry != null && entry.getName().equals(filename)) {
              storeFunc.store(new BufferedReader(new InputStreamReader(zis)), filename, lastModified);
              break;
            }
            zis.closeEntry();
          }
        }
      }
    } finally {
      con.disconnect();
    }
  }

  private void storeCsv1(Reader r, String name, String lastModified) throws IOException {
    CsvReader cr = new CsvReader(r);
    List<String> header = cr.readLine(); //?????????????????????
    Log.d(LOG_TAG, "header=" + header);

    SQLiteDatabase db = helper.getWritableDatabase();

    db.beginTransaction();
    AozoraDatabase.Storer storer = new AozoraDatabase.Storer(db);
    Pattern cardPersonPattern = Pattern.compile("/([0-9]+)/");
    try {
      for(List<String> tokens; (tokens=cr.readLine()) != null && tokens.size() == header.size(); ) {
        try {
          long opusId = Long.parseLong(tokens.get(0)); //??????ID
          long personId = Long.parseLong(tokens.get(14)); //??????ID
          Matcher m = cardPersonPattern.matcher(tokens.get(13));
          if(m.find()) {
            long cardPersonId = Long.parseLong(m.group(1));
            //1=?????????, 3=??????????????????, 4=??????, 5=????????????, 13=???????????????URL
            storer.insertOpus(opusId, tokens.get(1), tokens.get(3), tokens.get(4), tokens.get(5), cardPersonId, tokens.get(13));
          }
          //15=???, 16=???, 19=?????????????????????, 20=?????????????????????
          storer.insertPerson(personId, tokens.get(15), tokens.get(16), tokens.get(19), tokens.get(20));
          //23=???????????????
          storer.insertOpusPersonLink(opusId, personId, tokens.get(23));
          if(!tokens.get(27).trim().isEmpty()) {
            //27=?????????1, 28=??????????????????1, 29=?????????????????????1, 30=????????????????????????1, 31=????????????????????????1
            storer.insertOriginal(opusId, 1, tokens.get(27), tokens.get(28), tokens.get(29), tokens.get(30), tokens.get(31));
          }
          if(!tokens.get(35).trim().isEmpty()) {
            //35=?????????2, 36=??????????????????2, 37=?????????????????????2, 38=????????????????????????2, 39=????????????????????????2
            storer.insertOriginal(opusId, 2, tokens.get(35), tokens.get(36), tokens.get(37), tokens.get(38), tokens.get(39));
          }
          //45=????????????????????????URL, 46=???????????????????????????????????????, 47=???????????????????????????????????????
          storer.insertFile(opusId, "text", tokens.get(45), tokens.get(46), tokens.get(47));
          //50=(x)html????????????URL, 51=(x)html???????????????????????????, 52=(x)html???????????????????????????
          storer.insertFile(opusId, "html", tokens.get(50), tokens.get(51), tokens.get(52));
        } catch(Exception e) {
          Log.d(LOG_TAG, "Exception: "+e.getMessage()+", tokens=" + tokens.toString());
          //throw e;
        }
      }
      storer.updateDownload(name, lastModified);
      db.setTransactionSuccessful();
    } finally {
      storer.close();
      db.endTransaction();
    }
  }

  @FunctionalInterface
  private interface storerFunc {
    void store(Reader r, String name, String lastModified) throws IOException;
  }

  private boolean isDownloaded(String name, String lastModified) {
    SQLiteDatabase db = helper.getReadableDatabase();
    try(Cursor cursor = db.rawQuery("SELECT last_modified FROM download WHERE name=? AND last_modified=?", new String[]{name,lastModified})) {
      return cursor.moveToNext();
    }
  }

  private void notifyInsert(Uri uri) {
    Uri noopUri = uri.buildUpon().path(uri.getPath() + AozoraContentProvider.SUFFIX_NOOP).build();
    resolver.insert(noopUri, null);
  }
}
