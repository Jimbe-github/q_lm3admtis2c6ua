package com.teratail.q_lm3admtis2c6ua;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.net.Uri;

import com.teratail.q_lm3admtis2c6ua.AozoraDatabase.*;

import java.util.ArrayList;

public class AozoraContentProvider extends ContentProvider {
  public static final String AUTHORITY = AozoraContentProvider.class.getPackage().getName() + ".provider";

  private enum Subtype {
    DIR, ITEM
  }
  private enum UriPattern {
    DOWNLOAD(   Download.TABLE, Subtype.DIR, Download.CONTENT_TYPE, false, true, false), //query,update のみ
    CARD(       Card.TABLE,      Subtype.DIR,  Card.CONTENT_TYPE),
    CARD_ITEM(  Card.TABLE+"/#", Subtype.ITEM, Card.CONTENT_ITEM_TYPE),
    AUTHOR(     Author.TABLE,      Subtype.DIR,  Author.CONTENT_TYPE),
    AUTHOR_ITEM(Author.TABLE+"/#", Subtype.ITEM, Author.CONTENT_ITEM_TYPE),
    FILE(       File.TABLE,      Subtype.DIR,  File.CONTENT_TYPE),
    FILE_ITEM(  File.TABLE+"/#", Subtype.ITEM, File.CONTENT_ITEM_TYPE),

    CARDSUMMARY(CardSummary.TABLE,
            "(SELECT c."+Card.TITLE+" as "+CardSummary.TITLE+","+
                    " c."+Card.SORT_TITLE+" as "+CardSummary.SORT_TITLE+","+
                    " c."+Card.SUBTITLE+" as "+CardSummary.SUBTITLE +","+
                    " c."+Card.CARD_URL+" as "+CardSummary.CARD_URL+","+
                    " a."+Author.FAMILY_NAME+" || ' ' || a."+Author.PERSONAL_NAME+" as "+CardSummary.AUTHOR +
                    " FROM "+Card.TABLE+" as c INNER JOIN "+Author.TABLE+" as a"+
                    " ON c."+Card.AUTHOR_ID+" = a."+Author._ID+")" ,
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

    SQLiteDatabase db = helper.getWritableDatabase();
    long id = db.replace(p.table, null, values);
    if(id <= 0) throw new IllegalArgumentException("uri = " + uri);

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