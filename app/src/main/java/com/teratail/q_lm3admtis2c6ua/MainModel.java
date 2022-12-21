package com.teratail.q_lm3admtis2c6ua;

import android.content.*;
import android.database.*;
import android.os.*;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.*;

import com.teratail.q_lm3admtis2c6ua.AozoraDatabase.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;


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
  void requestCardSummaryCursor(@NonNull Aiueo aiueo, @NonNull Handler handler, @NonNull BiConsumer<Aiueo,Cursor> callback) {
    requestCardSummaryThread = new CancellationThread(new RequestCardSummaryRunnable(aiueo, resolver, handler, callback)).start();
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

interface CancellationRunnable extends Runnable {
  void cancel(Thread thread);
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
  private final Aiueo aiueo;
  private final ContentResolver resolver;
  private final Handler handler;
  private final BiConsumer<Aiueo,Cursor> callback;
  private final CancellationSignal cancelSignal = new CancellationSignal();

  private static final String[] PROJECTION = new String[]{CardSummary.TITLE, CardSummary.SUBTITLE, CardSummary.CARD_URL, CardSummary.AUTHOR};
  private String[] selectionArgs;
  private String selection;

  RequestCardSummaryRunnable(@NonNull Aiueo aiueo, @NonNull ContentResolver resolver, @NonNull Handler handler, @NonNull BiConsumer<Aiueo,Cursor> callback) {
    this.aiueo = aiueo;
    this.resolver = resolver;
    this.handler = handler;
    this.callback = callback;

    if(aiueo == Aiueo.他) {
      selection = CardSummary.SORT_TITLE + "=? or INSTR(?,SUBSTR(" + CardSummary.SORT_TITLE + ",1,1))=0";
      selectionArgs = new String[]{"", Aiueo.VALID_STRING};
    } else {
      selection = "SUBSTR(" + CardSummary.SORT_TITLE + ",1,1)=?";
      selectionArgs = new String[]{"" + aiueo};
    }
  }

  private boolean isInterrupted() {
    return Thread.currentThread().isInterrupted();
  }

  @Override
  public void run() {
    try {
      if(isInterrupted()) return;
      Cursor cursor = resolver.query(CardSummary.CONTENT_URI, PROJECTION, selection, selectionArgs, CardSummary.SORT_TITLE, cancelSignal);
      if(isInterrupted()) return;
      handler.post(() -> callback.accept(aiueo, cursor));
    } catch(CancellationException ignore) { //cancelSignal による中止
      //return;
    }
  }

  @Override
  public void cancel(Thread thread) {
    thread.interrupt();
    cancelSignal.cancel();
  }
}