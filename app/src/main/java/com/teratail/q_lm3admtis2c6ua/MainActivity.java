package com.teratail.q_lm3admtis2c6ua;

import android.os.Bundle;

import androidx.appcompat.app.*;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.*;
import androidx.navigation.*;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.*;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = MainActivity.class.getSimpleName();

  private NavController navController;
  private DrawerLayout drawerLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);

    drawerLayout = findViewById(R.id.drawer_layout);
    Toolbar toolbar = findViewById(R.id.toolbar);

    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.navigation_view);
    navigationView.setNavigationItemSelectedListener(item -> {
      switch(item.getItemId()) {
        case R.id.menu_opus:
          viewModel.setSelectedTarget(Target.OPUS);
          drawerLayout.closeDrawers();
          return true;
        case R.id.menu_author:
          viewModel.setSelectedTarget(Target.AUTHOR);
          drawerLayout.closeDrawers();
          return true;
      }
      return false;
    });
    navigationView.setCheckedItem(R.id.menu_opus);

    NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment); //FragmentContainerView を使っている場合はこうするしか無さそう
    navController = navHostFragment.getNavController();
    NavigationUI.setupWithNavController(toolbar, navController, drawerLayout);

    setSupportActionBar(toolbar); //フラグメントでアクティビティを MenuHost とする場合に必要
    //これ以降は toolbar は使わないこと.

    viewModel.requestDownload(); //最新チェック・ダウンロード
  }

  //setSupportActionBar しているならコレが必要
  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(navController, drawerLayout) || super.onSupportNavigateUp();
  }
}