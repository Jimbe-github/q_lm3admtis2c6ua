package com.teratail.q_lm3admtis2c6ua;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.*;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = MainActivity.class.getSimpleName();

  MainModel mainModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mainModel = new MainModel(this);

    MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    viewModel.setModel(mainModel);

    viewModel.setDownloadLiveData(mainModel.requestDownloadWork()); //ダウンロード開始

    ViewPager2 viewPager = findViewById(R.id.viewPager);
    viewPager.setAdapter(new ViewPagerAdapter(this));

    viewModel.getSelectedCardUrl().observe(this, cardSummary -> {
      viewPager.setCurrentItem(1);
    });
  }

  private static class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
      super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
      return position == 0 ? new CardSummaryListFragment() : new WebViewFragment();
    }

    @Override
    public int getItemCount() {
      return 2;
    }
  }
}

