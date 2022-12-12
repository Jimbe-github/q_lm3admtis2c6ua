package com.teratail.q_lm3admtis2c6ua;

import androidx.lifecycle.*;

public class MainViewModel extends ViewModel {
  private static final String LOG_TAG = MainViewModel.class.getSimpleName();

  private MainModel mainModel;
  void setModel(MainModel mainModel) {
    this.mainModel = mainModel;
  }

  private final MutableLiveData<CardListWithAiueo> cardListWithAiueoLiveData = new MutableLiveData<>(null);
  LiveData<CardListWithAiueo> getCardListWithAiueo() { return cardListWithAiueoLiveData; }
  void requestCardListWithAiueo(Aiueo aiueo) {
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

