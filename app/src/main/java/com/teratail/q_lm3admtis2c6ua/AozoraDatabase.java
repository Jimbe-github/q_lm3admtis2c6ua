package com.teratail.q_lm3admtis2c6ua;

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

    public static final Uri CONTENT_URI = Uri.parse("content://" + AozoraContentProvider.AUTHORITY + "/" + TABLE);
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AozoraContentProvider.AUTHORITY + "." + TABLE;

    private CardSummary() {}
  }
}
