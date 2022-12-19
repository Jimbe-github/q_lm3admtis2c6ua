package com.teratail.q_lm3admtis2c6ua;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.*;
import androidx.fragment.app.*;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainFragment extends Fragment {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = MainFragment.class.getSimpleName();

  public MainFragment() {
    super(R.layout.fragment_main);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    FragmentManager fm = getChildFragmentManager();
    NavController navController = NavHostFragment.findNavController(this);

    fm.setFragmentResultListener(CardSummaryListFragment.REQUESTKEY_SELECT_AIUEO, getViewLifecycleOwner(), (requestKey, result) -> {
      Fragment fragment = fm.findFragmentByTag(getString(R.string.aiueo_select_fragment_tag));
      if(fragment == null || !fragment.isVisible()) { //回転してレイアウトが替わってもフラグメントは残っている感じ
        navController.navigate(R.id.nav_aiueo_select_dialog);
      }
    });

    fm.setFragmentResultListener(CardSummaryListFragment.REQUESTKEY_SELECT_URL, getViewLifecycleOwner(), (requestKey, result) -> {
      if(fm.findFragmentByTag(getString(R.string.web_view_fragment_tag)) == null) {
        navController.navigate(R.id.action_selected_url);
      }
    });
  }
}
