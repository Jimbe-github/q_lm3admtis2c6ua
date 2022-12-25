package com.teratail.q_lm3admtis2c6ua;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.net.Uri;

import com.teratail.q_lm3admtis2c6ua.AozoraDatabase.*;

import java.util.ArrayList;

public class AozoraContentProvider extends ContentProvider {
  public static final String AUTHORITY = AozoraContentProvider.class.getPackage().getName() + ".provider";

  static final String SUFFIX_NOOP = "_noop";

  private enum Subtype {
    DIR, ITEM
  }
  private enum UriPattern {
    DOWNLOAD(     Download.TABLE,             Subtype.DIR, Download.CONTENT_TYPE, false, true, false), //query,update のみ
    DOWNLOAD_NOOP(Download.TABLE+SUFFIX_NOOP, Subtype.DIR, Download.CONTENT_TYPE),
    OPUS(     Opus.TABLE,      Subtype.DIR,  Opus.CONTENT_TYPE),
    OPUS_ITEM(Opus.TABLE+"/#", Subtype.ITEM, Opus.CONTENT_ITEM_TYPE),
    OPUS_NOOP(Opus.TABLE+SUFFIX_NOOP, Subtype.DIR,  Opus.CONTENT_TYPE),
    PERSON(     Person.TABLE,      Subtype.DIR,  Person.CONTENT_TYPE),
    PERSON_ITEM(Person.TABLE+"/#", Subtype.ITEM, Person.CONTENT_ITEM_TYPE),
    PERSON_NOOP(Person.TABLE+SUFFIX_NOOP, Subtype.DIR,  Person.CONTENT_TYPE),
    OPUS_PERSON_LINK(     OpusPersonLink.TABLE,      Subtype.DIR,  OpusPersonLink.CONTENT_TYPE),
    OPUS_PERSON_LINK_ITEM(OpusPersonLink.TABLE+"/#", Subtype.ITEM, OpusPersonLink.CONTENT_ITEM_TYPE),
    OPUS_PERSON_LINK_NOOP(OpusPersonLink.TABLE+SUFFIX_NOOP, Subtype.DIR,  OpusPersonLink.CONTENT_TYPE),
    FILE(     File.TABLE,      Subtype.DIR,  File.CONTENT_TYPE),
    FILE_ITEM(File.TABLE+"/#", Subtype.ITEM, File.CONTENT_ITEM_TYPE),
    FILE_NOOP(File.TABLE+SUFFIX_NOOP, Subtype.DIR,  File.CONTENT_TYPE),
    ORIGINAL(     Original.TABLE,      Subtype.DIR,  Original.CONTENT_TYPE),
    ORIGINAL_ITEM(Original.TABLE+"/#", Subtype.ITEM, Original.CONTENT_ITEM_TYPE),
    ORIGINAL_NOOP(Original.TABLE+SUFFIX_NOOP, Subtype.DIR,  Original.CONTENT_TYPE),

    CARDSUMMARY(CardSummary.TABLE,
            "(SELECT o."+Opus.TITLE+" as "+CardSummary.TITLE+
                    ", o."+Opus.SORT_TITLE+" as "+CardSummary.SORT_TITLE+
                    ", o."+Opus.SUBTITLE+" as "+CardSummary.SUBTITLE+
                    ", o."+Opus.READING_SUBTITLE+" as "+CardSummary.READING_SUBTITLE+
                    ", o."+Opus.CARD_URL+" as "+CardSummary.CARD_URL+
                    ", p."+Person.FAMILY_NAME+" || ' ' || p."+Person.PERSONAL_NAME+" as "+CardSummary.PERSON+
                    ", p."+Person.SORT_FAMILY_NAME+" as "+CardSummary.SORT_PERSON_FNAME +
                    ", p."+Person.SORT_PERSONAL_NAME+" as "+CardSummary.SORT_PERSON_PNAME +
                    " FROM "+Opus.TABLE+" as o LEFT OUTER JOIN "+Person.TABLE+" as p"+
                        " ON o."+Opus.CARD_PERSON_ID+"=p."+Person._ID+
            ")",
            Subtype.DIR, CardSummary.CONTENT_TYPE, false, false, false); //query のみ

    static UriPattern get(int ordinal) {
      return values()[ordinal];
    }

    final String path, table;
    final Subtype subtype;
    final String mimeType;
    final boolean canInsert, canUpdate, canDelete;
    UriPattern(String table, Subtype subtype, String mimeType) {
      this(table, table, subtype, mimeType, true, true, true);
    }
    UriPattern(String table, Subtype subtype, String mimeType, boolean canInsert, boolean canUpdate, boolean canDelete) {
      this(table, table, subtype, mimeType, canInsert, canUpdate, canDelete);
    }
    UriPattern(String path, String table, Subtype subtype, String mimeType, boolean canInsert, boolean canUpdate, boolean canDelete) {
      this.path = path;
      this.table = table;
      this.subtype = subtype;
      this.mimeType = mimeType;
      this.canInsert = canInsert;
      this.canUpdate = canUpdate;
      this.canDelete = canDelete;
    }
  }

  private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    for(UriPattern p : UriPattern.values()) {
      uriMatcher.addURI(AUTHORITY, p.path, p.ordinal());
    }
  }

  private DatabaseHelper helper;

  @Override
  public boolean onCreate() {
    helper = DatabaseHelper.getInstance(getContext());
    return true;
  }

  @Override
  public String getType(Uri uri) {
    int code = uriMatcher.match(uri);
    if(code == UriMatcher.NO_MATCH) return null; //error?
    UriPattern p = UriPattern.get(code);
    return p.mimeType;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    int code = uriMatcher.match(uri);
    if(code == UriMatcher.NO_MATCH) throw new IllegalArgumentException("uri=" + uri);
    UriPattern p = UriPattern.get(code);

    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    builder.setTables(p.table);
    SQLiteDatabase db = helper.getReadableDatabase();
    Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    cursor.setNotificationUri(getContext().getContentResolver(), uri);
    return cursor;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    int code = uriMatcher.match(uri);
    if(code == UriMatcher.NO_MATCH) throw new IllegalArgumentException("uri=" + uri);
    UriPattern p = UriPattern.get(code);
    if(!p.canInsert || p.subtype == Subtype.ITEM) throw new IllegalArgumentException("This table cannot be inserted。uri=" + uri);

    long id = 0;
    String path = uri.getPath();
    if(path.endsWith(SUFFIX_NOOP)) {
      uri = uri.buildUpon().path(path.substring(0, path.length() - SUFFIX_NOOP.length())).build(); //NOOP を外す
    } else {
      SQLiteDatabase db = helper.getWritableDatabase();
      id = db.replace(p.table, null, values);
      if(id <= 0) throw new IllegalArgumentException("uri = " + uri);
    }
    uri = ContentUris.withAppendedId(uri, id);
    getContext().getContentResolver().notifyChange(uri, null);
    return uri;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int code = uriMatcher.match(uri);
    if(code == UriMatcher.NO_MATCH) throw new IllegalArgumentException("uri=" + uri);
    UriPattern p = UriPattern.get(code);
    if(!p.canDelete) throw new IllegalArgumentException("This table cannot be deleted。uri=" + uri);

    SQLiteDatabase db = helper.getWritableDatabase();
    int count = db.delete(p.table, selection, selectionArgs);
    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    int code = uriMatcher.match(uri);
    if(code == UriMatcher.NO_MATCH) throw new IllegalArgumentException("uri=" + uri);
    UriPattern p = UriPattern.get(code);
    if(!p.canUpdate) throw new IllegalArgumentException("This table cannot be updated。uri=" + uri);

    SQLiteDatabase db = helper.getWritableDatabase();
    int count = db.update(p.table, values, selection, selectionArgs);
    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }

  @Override
  public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
    SQLiteDatabase db = helper.getWritableDatabase();
    try {
      db.beginTransaction();
      ContentProviderResult[] result = super.applyBatch(operations);
      db.setTransactionSuccessful();
      return result;
    } finally {
      db.endTransaction();
    }
  }
}