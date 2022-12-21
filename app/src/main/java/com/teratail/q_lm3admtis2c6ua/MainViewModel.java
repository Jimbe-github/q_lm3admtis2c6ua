package com.teratail.q_lm3admtis2c6ua;

import android.app.Application;
import android.database.*;
import android.os.*;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import androidx.work.WorkInfo;

import java.util.List;
import java.util.function.*;

public class MainViewModel extends AndroidViewModel {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = MainViewModel.class.getSimpleName();

  private MainModel mainModel;
  private Handler handler;

  private LiveData<List<WorkInfo>> downloadLiveData;

  public MainViewModel(@NonNull Application application) {
    super(application);
    mainModel = new MainModel(application);
    handler = new Handler(Looper.getMainLooper());
  }

  void requestDownload() {
    setDownloadLiveData(mainModel.requestDownloadWork());
  }

  void setDownloadLiveData(LiveData<List<WorkInfo>> workInfoListLiveData) { downloadLiveData = workInfoListLiveData; }
  LiveData<List<WorkInfo>> getDownloadWorkInfo() { return downloadLiveData; }

  private class RequestCardSummaryCursorCallback extends ContentObserver implements BiConsumer<Aiueo,Cursor> {
    private Aiueo aiueo;
    RequestCardSummaryCursorCallback() {
      super(null);
    }
    @Override
    public void accept(Aiueo aiueo, Cursor cursor) {
      Cursor old = cardSummaryCursorLiveData.getValue();
      if(old == cursor) return;
      if(old != null) {
        old.unregisterContentObserver(this);
        old.close();
      }
      this.aiueo = aiueo;
      if(cursor != null) cursor.registerContentObserver(this);
      cardSummaryCursorLiveData.setValue(cursor);
    }
    //accept で受けたカーソルの利用中にその元テーブルが修正された場合に呼ばれる.
    @Override
    public void onChange(boolean selfChange) {
      requestCardSummaryCursor(aiueo); //再取得
    }
  }

  private final RequestCardSummaryCursorCallback requestCardSummaryCursorCallback = new RequestCardSummaryCursorCallback();

  private final MutableLiveData<Cursor> cardSummaryCursorLiveData = new MutableLiveData<>(null);
  LiveData<Cursor> getCardSummaryCursor() { return cardSummaryCursorLiveData; }
  void requestCardSummaryCursor(Aiueo aiueo) {
    mainModel.requestCardSummaryCursor(aiueo, handler, requestCardSummaryCursorCallback);
  }

  private final MutableLiveData<Aiueo> selectedAiueoLiveData = new MutableLiveData<>();
  LiveData<Aiueo> getSelectedAiueo() {
    return selectedAiueoLiveData;
  }
  void setSelectedAiueo(Aiueo aiueo) {
    selectedAiueoLiveData.setValue(aiueo);
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

