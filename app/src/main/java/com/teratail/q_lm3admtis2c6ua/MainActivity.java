package com.teratail.q_lm3admtis2c6ua;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.*;
import androidx.lifecycle.*;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {
  private static final String LOG_TAG = MainActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    MainModel mainModel = new MainModel();
    getLifecycle().addObserver(mainModel);

    FragmentManager fm = getSupportFragmentManager();
    MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    viewModel.setModel(mainModel);

    ViewPager2 viewPager = findViewById(R.id.viewPager);
    viewPager.setAdapter(new ViewPagerAdapter(this));

    viewModel.getSelectedBookInfo().observe(this, bookInfo -> {
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
      return position == 0 ? new BookListFragment() : new WebViewFragment();
    }

    @Override
    public int getItemCount() {
      return 2;
    }
  }
}

