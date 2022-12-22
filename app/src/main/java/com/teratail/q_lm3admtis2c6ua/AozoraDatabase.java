package com.teratail.q_lm3admtis2c6ua;

import android.database.sqlite.*;
import android.net.Uri;
import android.provider.BaseColumns;

public class AozoraDatabase {
  public static class Download implements BaseColumns {
    static final String TABLE = "download";
    public static final String LAST_MODIFIED = "last_modified";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private Download() {}
  }

  public static class Card implements BaseColumns {
    static final String TABLE = "card";
    public static final String TITLE = "title";
    public static final String SORT_TITLE = "sort_title";
    public static final String SUBTITLE = "subtitle";
    public static final String CARD_URL = "card_url";
    public static final String AUTHOR_ID = "author_id";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private Card() {}
  }

  public static class Author implements BaseColumns {
    public static final String TABLE = "author";
    public static final String FAMILY_NAME = "family_name";
    public static final String SORT_FAMILY_NAME = "sort_family_name";
    public static final String PERSONAL_NAME = "personal_name";
    public static final String SORT_PERSONAL_NAME = "sort_personal_name";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private Author() {}
  }

  public static class File implements BaseColumns {
    static final String TABLE = "file";
    public static final String CARD_ID = "card_id";
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
    public static final String TITLE = Card.TITLE;
    public static final String SORT_TITLE = Card.SORT_TITLE;
    public static final String SUBTITLE = Card.SUBTITLE;
    public static final String CARD_URL = Card.CARD_URL;
    public static final String AUTHOR = "author";
    public static final String SORT_AUTHOR_FNAME = "sort_author_family_name";
    public static final String SORT_AUTHOR_PNAME = "sort_author_personal_name";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private CardSummary() {}
  }

  static class Storer {
    private final SQLiteStatement upsertAuthorStmt, upsertCardStmt, upsertFileStmt, updateDownloadStmt;

    Storer(SQLiteDatabase db) {
      upsertAuthorStmt = db.compileStatement("INSERT OR REPLACE INTO "+Author.TABLE+"(" +
              Author._ID+","+Author.FAMILY_NAME+","+Author.SORT_FAMILY_NAME+","+Author.PERSONAL_NAME+","+Author.SORT_PERSONAL_NAME +
              ") VALUES (?,?,?,?,?)");
      upsertCardStmt = db.compileStatement("INSERT OR REPLACE INTO "+Card.TABLE+"(" +
              Card._ID+","+Card.TITLE+","+Card.SORT_TITLE+","+Card.SUBTITLE+","+Card.CARD_URL+","+Card.AUTHOR_ID +
              ") VALUES (?,?,?,?,?,?)");
      upsertFileStmt = db.compileStatement("INSERT OR REPLACE INTO "+ File.TABLE+"(" +
              File.CARD_ID+","+ File.KIND+","+ File.URL+","+ File.LAST_UPDATE+","+ File.CHARSET +
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
}
