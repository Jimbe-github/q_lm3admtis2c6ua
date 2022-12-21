package com.teratail.q_lm3admtis2c6ua;

import android.os.Bundle;

import androidx.appcompat.app.*;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.*;
import androidx.navigation.*;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.*;

public class MainActivity extends AppCompatActivity {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = MainActivity.class.getSimpleName();

  private NavController navController;
  private AppBarConfiguration appBarConfiguration;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar); //Navigation によりトップフラグメントでタイトルを設定している場合は、 AndroidManifest.xml で Application の android:title は消しておくこと

    NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment); //FragmentContainerView を使っている場合はこうするしか無さそう
    navController = navHostFragment.getNavController();
    appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    viewModel.requestDownload();
  }

  //Toolbar を ActionBar として用いる場合はコレが必要
  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
  }
}