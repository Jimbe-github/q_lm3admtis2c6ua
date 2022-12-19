package com.teratail.q_lm3admtis2c6ua;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.*;
import androidx.navigation.*;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.*;

public class MainActivity extends AppCompatActivity {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = MainActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    //setSupportActionBar(toolbar); //Navi によるタイトルの表示に支障が出る

    NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
    NavController navController = navHostFragment.getNavController();
    AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
    NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

    MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    viewModel.requestDownload();
  }
}