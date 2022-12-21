package com.teratail.q_lm3admtis2c6ua;

import android.os.Bundle;
import android.view.*;

import androidx.annotation.*;
import androidx.core.view.*;
import androidx.fragment.app.*;
import androidx.lifecycle.Lifecycle;
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

    boolean isAiueoSelectPermanent = view.findViewWithTag(getString(R.string.aiueo_select_fragment_tag)) != null;
    boolean isWebViewPermanent = view.findViewWithTag(getString(R.string.web_view_fragment_tag)) != null;

    FragmentManager fm = getChildFragmentManager();
    NavController navController = NavHostFragment.findNavController(this);

    MenuHost menuHost = requireActivity();
    menuHost.addMenuProvider(new MenuProvider() {
      @Override
      public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main_fragment, menu);
      }
      @Override
      public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId()) {
          case R.id.aiueo_menu:
            navController.navigate(R.id.nav_aiueo_select_dialog);
            return true;
        }
        return false;
      }
    }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

    fm.setFragmentResultListener(CardSummaryListFragment.REQUESTKEY_SELECT_AIUEO, getViewLifecycleOwner(), (requestKey, result) -> {
      if(!isAiueoSelectPermanent) {
        navController.navigate(R.id.nav_aiueo_select_dialog);
      }
    });

    fm.setFragmentResultListener(CardSummaryListFragment.REQUESTKEY_SELECT_URL, getViewLifecycleOwner(), (requestKey, result) -> {
      if(!isWebViewPermanent) {
        navController.navigate(R.id.action_selected_url);
      }
    });
  }
}
