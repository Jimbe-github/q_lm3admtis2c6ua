package com.teratail.q_lm3admtis2c6ua;

import android.app.Application;
import android.database.*;
import android.os.*;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import androidx.work.WorkInfo;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = MainViewModel.class.getSimpleName();

  private final MainModel mainModel;
  private final Handler handler;

  private LiveData<List<WorkInfo>> downloadLiveData;

  public MainViewModel(@NonNull Application application) {
    super(application);
    mainModel = new MainModel(application);
    handler = new Handler(Looper.getMainLooper());
    setTitle();
  }

  void requestDownload() {
    setDownloadLiveData(mainModel.requestDownloadWork());
  }

  void setDownloadLiveData(LiveData<List<WorkInfo>> workInfoListLiveData) { downloadLiveData = workInfoListLiveData; }
  @SuppressWarnings("unused")
  LiveData<List<WorkInfo>> getDownloadWorkInfo() { return downloadLiveData; }

  private final MutableLiveData<Target> selectedTargetLiveData = new MutableLiveData<>(Target.OPUS);
  LiveData<Target> getSelectedTarget() {
    return selectedTargetLiveData;
  }
  void setSelectedTarget(@NonNull Target listTarget) {
    if(selectedTargetLiveData.getValue() == listTarget) return;
    selectedTargetLiveData.setValue(listTarget);
    setTitle();
    requestCardSummaryCursor();
  }

  private class RequestCardSummaryCursorCallback extends ContentObserver implements RequestCardSummaryRunnable.CallBack {
    RequestCardSummaryCursorCallback() {
      super(null);
    }
    @Override
    public void start(Target listTarget, Aiueo aiueo) {
      cardSummaryProcessingStateLiveData.setValue(CardSummaryProcessingState.START);
    }
    @Override
    public void complete(Target listTarget, Aiueo aiueo, Cursor cursor) {
      cardSummaryProcessingStateLiveData.setValue(CardSummaryProcessingState.STOP);
      Cursor old = cardSummaryCursorLiveData.getValue();
      if(old == cursor) return;
      if(old != null) {
        old.unregisterContentObserver(this);
        old.close();
      }
      if(cursor != null) cursor.registerContentObserver(this);
      cardSummaryCursorLiveData.setValue(cursor);
      setTitle();
    }
    @Override
    public void cancel(Target listTarget, Aiueo aiueo) {
      cardSummaryProcessingStateLiveData.setValue(CardSummaryProcessingState.STOP);
    }
    //accept で受けたカーソルの利用中にその元テーブルが修正された場合に呼ばれる.
    @Override
    public void onChange(boolean selfChange) {
      requestCardSummaryCursor(); //再取得
    }
  }
  private final RequestCardSummaryCursorCallback requestCardSummaryCursorCallback = new RequestCardSummaryCursorCallback();

  enum CardSummaryProcessingState { START, STOP }
  private final MutableLiveData<CardSummaryProcessingState> cardSummaryProcessingStateLiveData = new MutableLiveData<>(CardSummaryProcessingState.STOP);
  LiveData<CardSummaryProcessingState> getCardSummaryProcessingState() { return cardSummaryProcessingStateLiveData; }

  private final MutableLiveData<Cursor> cardSummaryCursorLiveData = new MutableLiveData<>(null);
  LiveData<Cursor> getCardSummaryCursor() { return cardSummaryCursorLiveData; }

  private void requestCardSummaryCursor() {
    if(selectedAiueoLiveData.getValue() == null) return;
    mainModel.requestCardSummaryCursor(selectedTargetLiveData.getValue(), selectedAiueoLiveData.getValue(), handler, requestCardSummaryCursorCallback);
  }

  private final MutableLiveData<Aiueo> selectedAiueoLiveData = new MutableLiveData<>();
  LiveData<Aiueo> getSelectedAiueo() {
    return selectedAiueoLiveData;
  }
  void setSelectedAiueo(Aiueo aiueo) {
    if(selectedAiueoLiveData.getValue() == aiueo) return;
    selectedAiueoLiveData.setValue(aiueo);
    setTitle();
    requestCardSummaryCursor();
  }

  private final MutableLiveData<String> titleLiveData = new MutableLiveData<>("");
  LiveData<String> getTitle() {
    return titleLiveData;
  }
  private void setTitle() {
    StringBuilder sb = new StringBuilder(selectedTargetLiveData.getValue().text).append("一覧");
    if(selectedAiueoLiveData.getValue() != null) {
      sb.append("(").append(selectedAiueoLiveData.getValue()).append(":");
      Cursor cursor = cardSummaryCursorLiveData.getValue();
      int rows = cursor == null ? 0 : cursor.getCount();
      sb.append(rows).append("件)");
    }
    titleLiveData.setValue(sb.toString());
  }

  private final MutableLiveData<String> selectedCardUrlLiveData = new MutableLiveData<>();
  LiveData<String> getSelectedCardUrl() {
    return selectedCardUrlLiveData;
  }
  void setSelectedCardUrl(String url) {
    selectedCardUrlLiveData.setValue(url);
  }

  @Override
  protected void onCleared() {
    Log.d(LOG_TAG, "onCleared");
    super.onCleared();

    //検索中等を止める
    mainModel.clear();

    //カーソル解放
    Cursor cursor = cardSummaryCursorLiveData.getValue();
    if(cursor != null) {
      cardSummaryCursorLiveData.setValue(null);
      cursor.close();
    }
  }
}