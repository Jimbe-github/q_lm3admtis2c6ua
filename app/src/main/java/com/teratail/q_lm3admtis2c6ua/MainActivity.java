package com.teratail.q_lm3admtis2c6ua;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.*;
import androidx.lifecycle.*;

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

    View view = null;
    if((view = findViewById(R.id.fragment_container_view)) != null) {
      Log.d(LOG_TAG, "R.id.fragment_container_view: view="+view.getClass().getSimpleName());
      if(view instanceof FragmentContainerView) {
        fm.beginTransaction().replace(R.id.fragment_container_view, new BookListFragment()).commit();
      }
    }
  }
}

