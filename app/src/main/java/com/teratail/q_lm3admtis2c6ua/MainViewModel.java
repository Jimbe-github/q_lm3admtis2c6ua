package com.teratail.q_lm3admtis2c6ua;

import android.database.*;

import androidx.lifecycle.*;
import androidx.work.WorkInfo;

import java.util.List;

public class MainViewModel extends ViewModel {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = MainViewModel.class.getSimpleName();

  private MainModel mainModel;
  void setModel(MainModel mainModel) {
    this.mainModel = mainModel;
  }

  private LiveData<List<WorkInfo>> downloadLiveData;
  void setDownloadLiveData(LiveData<List<WorkInfo>> workInfoListLiveData) { downloadLiveData = workInfoListLiveData; }
  LiveData<List<WorkInfo>> getDownloadWorkInfo() { return downloadLiveData; }

  private class CursorContentObserver extends ContentObserver {
    private final Aiueo aiueo;

    CursorContentObserver(Aiueo aiueo) {
      super(null);
      this.aiueo = aiueo;
    }
    @Override
    public void onChange(boolean selfChange) {
      if(aiueo == null) return;
      requestCardSummaryCursor(aiueo);
    }
  }
  private CursorContentObserver cursorContentObserver = null;

  private final MutableLiveData<Cursor> cardSummaryCursorLiveData = new MutableLiveData<>(null);
  LiveData<Cursor> getCardSummaryCursor() { return cardSummaryCursorLiveData; }
  void requestCardSummaryCursor(Aiueo aiueo) {
    mainModel.requestCardSummaryCursor(aiueo, cursor -> {
      Cursor old = cardSummaryCursorLiveData.getValue();
      if(old == cursor) return;
      if(old != null) {
        old.unregisterContentObserver(cursorContentObserver);
        old.close();
      }
      if(cursor != null) {
        cursorContentObserver = new CursorContentObserver(aiueo);
        cursor.registerContentObserver(cursorContentObserver);
      }
      cardSummaryCursorLiveData.postValue(cursor);
    });
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
  void setSelectedCardSummary(String url) {
    selectedCardUrlLiveData.setValue(url);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    Cursor cursor = cardSummaryCursorLiveData.getValue();
    if(cursor != null) {
      cardSummaryCursorLiveData.setValue(null);
      cursor.unregisterContentObserver(cursorContentObserver);
      cursor.close();
    }
  }
}

