package com.teratail.q_lm3admtis2c6ua;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class WebViewFragment extends Fragment {
  public WebViewFragment() {
    super(R.layout.fragment_webview);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    WebView webview = view.findViewById(R.id.webview);

    viewModel.getSelectedCardSummary().observe(getViewLifecycleOwner(), cardSummary -> {
      webview.loadUrl(cardSummary.url);
    });
  }
}
