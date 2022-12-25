package com.teratail.q_lm3admtis2c6ua;

import android.database.sqlite.*;
import android.net.Uri;
import android.provider.BaseColumns;

public class AozoraDatabase {
  public static class Download implements BaseColumns {
    static final String TABLE = "download";
    public static final String NAME = "name";
    public static final String LAST_MODIFIED = "last_modified";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private Download() {}
  }

  public static class Opus implements BaseColumns {
    public static final String READING_SUBTITLE = "reading_subtitle";
    public static final String TITLE = "title";
    public static final String SORT_TITLE = "sort_title";
    public static final String SUBTITLE = "subtitle";
    public static final String CARD_PERSON_ID = "card_person_id";
    static final String TABLE = "opus";
    public static final String CARD_URL = "card_url";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private Opus() {}
  }

  public static class OpusPersonLink implements BaseColumns {
    public static final String OPUS_ID = "opus_id";
    public static final String PERSON_ID = "person_id";
    public static final String KIND = "kind"; //"著者","翻訳者" 等
    static final String TABLE = "opus_person_link";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private OpusPersonLink() {}
  }

  public static class Person implements BaseColumns {
    public static final String TABLE = "person";
    public static final String FAMILY_NAME = "family_name";
    public static final String SORT_FAMILY_NAME = "sort_family_name";
    public static final String PERSONAL_NAME = "personal_name";
    public static final String SORT_PERSONAL_NAME = "sort_personal_name";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private Person() {}
  }

  //底本
  public static class Original implements BaseColumns {
    public static final String OPUS_ID = "opus_id";
    public static final String NUMBER = "number";
    public static final String NAME = "name";
    public static final String PUBLISHER = "publisher";
    public static final String YEAR_OF_FIRST_PUBLICATION = "yearOfFirstPublication";
    public static final String INPUT_PUBLICATION = "inputPublication";
    public static final String PROOFREADING_PUBLICATION = "ProofreadingPublication";
    static final String TABLE = "original";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private Original() {}
  }

  public static class File implements BaseColumns {
    static final String TABLE = "file";
    public static final String OPUS_ID = "opus_id";
    public static final String KIND = "kind";
    public static final String URL = "url";
    public static final String LAST_UPDATE = "last_update";
    public static final String CHARSET = "charset";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private File() {}
  }

  public static class CardSummary implements BaseColumns {
    static final String TABLE = "card_summary";
    public static final String TITLE = Opus.TITLE;
    public static final String SORT_TITLE = Opus.SORT_TITLE;
    public static final String SUBTITLE = Opus.SUBTITLE;
    public static final String READING_SUBTITLE = Opus.READING_SUBTITLE;
    public static final String CARD_URL = Opus.CARD_URL;
    public static final String PERSON = "person";
    public static final String SORT_PERSON_FNAME = "sort_person_family_name";
    public static final String SORT_PERSON_PNAME = "sort_person_personal_name";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private CardSummary() {}
  }

  static class Storer {
    private final SQLiteStatement insertPersonStmt, insertOpusStmt, insertOpusPersonLinkStmt, insertOriginalStmt, insertFileStmt;
    private final SQLiteStatement upsertDownloadStmt;

    Storer(SQLiteDatabase db) {
      insertPersonStmt = db.compileStatement("INSERT OR IGNORE INTO "+ Person.TABLE+" (" +
              Person._ID+","+Person.FAMILY_NAME+","+Person.SORT_FAMILY_NAME+","+Person.PERSONAL_NAME+","+Person.SORT_PERSONAL_NAME +
              ") VALUES (?,?,?,?,?)");
      insertOpusStmt = db.compileStatement("INSERT OR IGNORE INTO "+ Opus.TABLE+" (" +
              Opus._ID+","+Opus.TITLE+","+Opus.SORT_TITLE+","+Opus.SUBTITLE+","+Opus.READING_SUBTITLE+","+Opus.CARD_PERSON_ID+","+Opus.CARD_URL +
              ") VALUES (?,?,?,?,?,?,?)");
      insertOpusPersonLinkStmt = db.compileStatement("INSERT OR IGNORE INTO "+OpusPersonLink.TABLE+" (" +
              OpusPersonLink.OPUS_ID+","+OpusPersonLink.PERSON_ID+","+OpusPersonLink.KIND +
              ") VALUES (?,?,?)");
      insertOriginalStmt = db.compileStatement("INSERT OR IGNORE INTO "+Original.TABLE+" (" +
              Original.OPUS_ID+","+Original.NUMBER+","+Original.NAME+","+Original.PUBLISHER+","+Original.YEAR_OF_FIRST_PUBLICATION+","+Original.INPUT_PUBLICATION+","+Original.PROOFREADING_PUBLICATION +
              ") VALUES (?,?,?,?,?,?,?)");
      insertFileStmt = db.compileStatement("INSERT OR IGNORE INTO "+ File.TABLE+" (" +
              File.OPUS_ID+","+File.KIND+","+File.URL+","+File.LAST_UPDATE+","+File.CHARSET +
              ") VALUES (?,?,?,?,?)");
      upsertDownloadStmt = db.compileStatement("INSERT OR REPLACE INTO "+Download.TABLE+" (" +
              Download.NAME+","+Download.LAST_MODIFIED +
              ") VALUES (?,?)");
    }

    void insertPerson(long personId, String familyName, String personalName, String sortFamilyName, String sortPersonalName) {
      insertPersonStmt.bindLong(1, personId); //人物 ID
      insertPersonStmt.bindString(2, familyName); //姓
      insertPersonStmt.bindString(3, sortFamilyName); //ソート用姓読み
      insertPersonStmt.bindString(4, personalName); //名
      insertPersonStmt.bindString(5, sortPersonalName); //ソート用名読み
      insertPersonStmt.executeInsert();
    }

    void insertOpus(long opusId, String title, String sortTitle, String subtitle, String readingSubtitle, long cardPersonId, String cardUrl) {
      insertOpusStmt.bindLong(1, opusId); //作品 ID
      insertOpusStmt.bindString(2, title); //作品名
      insertOpusStmt.bindString(3, sortTitle); //ソート用読み
      insertOpusStmt.bindString(4, subtitle); //副題
      insertOpusStmt.bindString(5, readingSubtitle); //副題読み
      insertOpusStmt.bindLong(6, cardPersonId); //図書カード URL 内の 人物 ID
      insertOpusStmt.bindString(7, cardUrl); //図書カードURL
      insertOpusStmt.executeInsert();
    }

    void insertOpusPersonLink(long opusId, long personId, String kind) {
      insertOpusPersonLinkStmt.bindLong(1, opusId);
      insertOpusPersonLinkStmt.bindLong(2, personId);
      insertOpusPersonLinkStmt.bindString(3, kind);
      insertOpusPersonLinkStmt.executeInsert();
    }

    void insertOriginal(long opusId, int number, String name, String publisher, String yearOfFirstPublication, String inputPublication, String proofreadingPublication) {
      insertOriginalStmt.bindLong(1, opusId);
      insertOriginalStmt.bindLong(2, number); //1,2,…
      insertOriginalStmt.bindString(3, name); //底本名
      insertOriginalStmt.bindString(4, publisher); //底本出版社名
      insertOriginalStmt.bindString(5, yearOfFirstPublication); //底本初版発行年
      insertOriginalStmt.bindString(6, inputPublication); //入力に使用した版
      insertOriginalStmt.bindString(7, proofreadingPublication); //校正に使用した版
      insertOriginalStmt.executeInsert();
    }

    void insertFile(long opusId, String kind, String url, String lastUpdate, String charset) {
      insertFileStmt.bindLong(1, opusId);
      insertFileStmt.bindString(2, kind); //種別('text'/'html')
      insertFileStmt.bindString(3, url); //ファイルURL
      insertFileStmt.bindString(4, lastUpdate); //ファイル最終更新日
      insertFileStmt.bindString(5, charset); //ファイル符号化方式
      insertFileStmt.executeInsert();
    }

    void updateDownload(String name, String lastModified) {
      upsertDownloadStmt.bindString(1, name);
      upsertDownloadStmt.bindString(2, lastModified);
      upsertDownloadStmt.executeUpdateDelete();
    }

    void close() {
      insertPersonStmt.close();
      insertOpusStmt.close();
      insertOpusPersonLinkStmt.close();
      insertOriginalStmt.close();
      insertFileStmt.close();
      upsertDownloadStmt.close();
    }
  }
}
