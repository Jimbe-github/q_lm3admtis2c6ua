package com.teratail.q_lm3admtis2c6ua;

import android.util.Log;

import androidx.lifecycle.*;

import java.util.*;

public class MainViewModel extends ViewModel {
  private static final String LOG_TAG = MainViewModel.class.getSimpleName();

  private MainModel mainModel;
  void setModel(MainModel mainModel) {
    this.mainModel = mainModel;
  }

  private MutableLiveData<BooklistPage> booklistPageLiveData = new MutableLiveData<>(new BooklistPage(Collections.emptyList(), null, 1, 1));
  LiveData<BooklistPage> getBooklistPage() {
    return booklistPageLiveData;
  }
  void requestBookInfoList(Aiueo aiueo, int page) {
    mainModel.requestBooklistPage(aiueo, page, booklistPage -> {
      booklistPageLiveData.postValue(booklistPage);
      Log.d(LOG_TAG, "maxPage=" + booklistPage.maxPages);
    });
  }

  private MutableLiveData<Aiueo> selectedFromAiueoLiveData = new MutableLiveData<>();
  LiveData<Aiueo> getSelectedFromAiueo() {
    return selectedFromAiueoLiveData;
  }
  void setSelectedFromAiueo(Aiueo aiueo) {
    selectedFromAiueoLiveData.setValue(aiueo);
  }

  private Runnable requestSelectingFromAiueoCallback = null;
  void setRequestSelectingFromAiueoCallback(Runnable callback) {
    requestSelectingFromAiueoCallback = callback;
  }
  void requestSelectingFromAiueo() {
    if(requestSelectingFromAiueoCallback != null) requestSelectingFromAiueoCallback.run();
  }
}
