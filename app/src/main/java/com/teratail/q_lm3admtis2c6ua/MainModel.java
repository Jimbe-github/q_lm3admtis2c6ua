package com.teratail.q_lm3admtis2c6ua;

import android.content.*;
import android.database.Cursor;
import android.os.*;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.*;

import com.teratail.q_lm3admtis2c6ua.AozoraDatabase.CardSummary;

import java.util.List;
import java.util.concurrent.CancellationException;

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
    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DownloadWorker.class).addTag("downloadWork").build();
    workManager.enqueue(workRequest);
    return workManager.getWorkInfosByTagLiveData("downloadWork");
  }

  private CancellationThread requestCardSummaryThread;
  void requestCardSummaryCursor(@NonNull Target listTarget, @NonNull Aiueo aiueo, @NonNull Handler handler, @NonNull RequestCardSummaryRunnable.CallBack callback) {
    requestCardSummaryThread = new CancellationThread(new RequestCardSummaryRunnable(listTarget, aiueo, resolver, handler, callback)).start();
  }
  void cancelRequestCardSummaryCursor() {
    if(requestCardSummaryThread != null) {
      requestCardSummaryThread.cancel().join();
      requestCardSummaryThread = null;
    }
  }

  void clear() {
    cancelRequestCardSummaryCursor();
  }
}

/** キャンセルメソッド付き Runnable */
interface CancellationRunnable extends Runnable {
  default boolean isInterrupted() {
    return Thread.currentThread().isInterrupted();
  }
  default void cancel(Thread thread) {
    thread.interrupt();
  }
}

class CancellationThread {
  private Thread thread;
  private CancellationRunnable cancellationRunnable;
  CancellationThread(CancellationRunnable cr) {
    this.cancellationRunnable = cr;
    thread = new Thread(cr);
  }
  CancellationThread start() {
    thread.start();
    return this;
  }
  CancellationThread cancel() {
    cancellationRunnable.cancel(thread);
    return this;
  }
  void join() {
    try {
      thread.join();
    } catch(InterruptedException e) {
      e.printStackTrace();
    }
  }
}

class RequestCardSummaryRunnable implements CancellationRunnable {
  interface CallBack {
    void start(Target listTarget, Aiueo aiueo);
    void complete(Target listTarget, Aiueo aiueo, Cursor cursor);
    void cancel(Target listTarget, Aiueo aiueo);
  }

  private final Target listTarget;
  private final Aiueo aiueo;
  private final ContentResolver resolver;
  private final Handler handler;
  private final CallBack callback;

  private final CancellationSignal cancelSignal = new CancellationSignal();

  private static final String[] PROJECTION = new String[]{CardSummary.TITLE, CardSummary.SUBTITLE, CardSummary.CARD_URL, CardSummary.AUTHOR};
  private String[] selectionArgs;
  private String selection, sortOrder;

  RequestCardSummaryRunnable(@NonNull Target listTarget, @NonNull Aiueo aiueo, @NonNull ContentResolver resolver, @NonNull Handler handler, @NonNull CallBack callback) {
    this.listTarget = listTarget;
    this.aiueo = aiueo;
    this.resolver = resolver;
    this.handler = handler;
    this.callback = callback;

    String targetColumn;
    if(listTarget == Target.OPUS) {
      targetColumn = CardSummary.SORT_TITLE;
      sortOrder = CardSummary.SORT_TITLE+", "+CardSummary.SORT_AUTHOR_FNAME+", "+CardSummary.SORT_AUTHOR_FNAME;
    } else {
      targetColumn = CardSummary.SORT_AUTHOR_FNAME;
      sortOrder = CardSummary.SORT_AUTHOR_FNAME+", "+CardSummary.SORT_AUTHOR_FNAME+", "+CardSummary.SORT_TITLE;
    }
    if(aiueo == Aiueo.他) {
      selection = targetColumn+"=? OR INSTR(?,SUBSTR(" + targetColumn + ",1,1))=0"; //SUBSTR は空文字列からは空文字列を返すため先に判定が必要(nullだったらIFNULL使えたのに…)
      selectionArgs = new String[]{"", Aiueo.VALID_STRING};
    } else {
      selection = "SUBSTR(" + targetColumn + ",1,1)=?";
      selectionArgs = new String[]{aiueo.toString()};
    }
  }

  @Override
  public void run() {
    try {
      handler.post(() -> callback.start(listTarget, aiueo));
      if(isInterrupted()) throw new CancellationException();
      Cursor cursor = resolver.query(CardSummary.CONTENT_URI, PROJECTION, selection, selectionArgs, sortOrder, cancelSignal);
      if(isInterrupted()) throw new CancellationException();
      handler.post(() -> callback.complete(listTarget, aiueo, cursor));
    } catch(Exception ignore) { //cancelSignal による中止等
      handler.post(() -> callback.cancel(listTarget, aiueo));
    }
  }

  @Override
  public void cancel(Thread thread) {
    CancellationRunnable.super.cancel(thread);
    cancelSignal.cancel();
  }
}