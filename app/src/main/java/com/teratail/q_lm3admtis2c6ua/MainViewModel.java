package com.teratail.q_lm3admtis2c6ua;

import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import androidx.work.WorkInfo;

import java.util.List;

public class MainViewModel extends ViewModel {
  private static final String LOG_TAG = MainViewModel.class.getSimpleName();

  private MainModel mainModel;
  void setModel(MainModel mainModel) {
    this.mainModel = mainModel;
  }

  private LiveData<List<WorkInfo>> downloadLiveData;
  void setDownloadLiveData(LiveData<List<WorkInfo>> workInfoListLiveData) { downloadLiveData = workInfoListLiveData; }
  LiveData<List<WorkInfo>> getDownload() { return downloadLiveData; }

  private final MutableLiveData<CardListWithAiueo> cardListWithAiueoLiveData = new MutableLiveData<>(null);
  LiveData<CardListWithAiueo> getCardListWithAiueo() { return cardListWithAiueoLiveData; }
  void requestCardListWithAiueo(@NonNull Aiueo aiueo) {
    mainModel.requestCardListWithAiueo(aiueo, cardListWithAiueo -> {
      cardListWithAiueoLiveData.postValue(cardListWithAiueo);
    });
  }

  private final MutableLiveData<Aiueo> selectedAiueoLiveData = new MutableLiveData<>();
  LiveData<Aiueo> getSelectedAiueo() {
    return selectedAiueoLiveData;
  }
  void setSelectedAiueo(Aiueo aiueo) {
    selectedAiueoLiveData.setValue(aiueo);
  }

  private final MutableLiveData<CardSummary> selectedCardSummaryLiveData = new MutableLiveData<>();
  LiveData<CardSummary> getSelectedCardSummary() {
    return selectedCardSummaryLiveData;
  }
  void setSelectedCardSummary(CardSummary cardSummary) {
    selectedCardSummaryLiveData.setValue(cardSummary);
  }
}

