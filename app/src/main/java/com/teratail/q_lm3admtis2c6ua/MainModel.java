package com.teratail.q_lm3admtis2c6ua;

import android.content.*;
import android.database.*;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.*;

import com.teratail.q_lm3admtis2c6ua.AozoraDatabase.*;

import java.util.List;
import java.util.function.Consumer;

public class MainModel {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = MainModel.class.getSimpleName();

  private final WorkManager workManager;
  private final ContentResolver resolver;

  MainModel(Context context) {
    context = context.getApplicationContext();
    workManager = WorkManager.getInstance(context);
    resolver = context.getContentResolver();
  }

  LiveData<List<WorkInfo>> requestDownloadWork() {
    OneTimeWorkRequest workRequest = new OneTimeWorkRequest
            .Builder(DownloadWorker.class)
            .addTag("downloadWork")
            .build();
    workManager.enqueue(workRequest);
    return workManager.getWorkInfosByTagLiveData("downloadWork");
  }

  void requestCardSummaryCursor(@NonNull Aiueo aiueo, @NonNull Handler handler, @NonNull Consumer<Cursor> callback) {
    new Thread(() -> {
      String[] projection = new String[]{CardSummary.TITLE, CardSummary.SUBTITLE, CardSummary.CARD_URL, CardSummary.AUTHOR};
      String selection;
      String[] selectionArgs;
      if(aiueo == Aiueo.他) {
        selection = CardSummary.SORT_TITLE + "=? or INSTR(?,SUBSTR(" + CardSummary.SORT_TITLE + ",1,1))=0";
        selectionArgs = new String[]{"", Aiueo.VALID_STRING};
      } else {
        selection = "SUBSTR(" + CardSummary.SORT_TITLE + ",1,1)=?";
        selectionArgs = new String[]{"" + aiueo};
      }
      Cursor cursor = resolver.query(CardSummary.CONTENT_URI, projection, selection, selectionArgs, CardSummary.SORT_TITLE);
      handler.post(() -> callback.accept(cursor));
    }).start();
  }
}