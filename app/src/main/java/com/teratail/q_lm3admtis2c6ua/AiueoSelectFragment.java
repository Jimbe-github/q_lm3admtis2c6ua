package com.teratail.q_lm3admtis2c6ua;

import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.*;

public class AiueoSelectFragment extends DialogFragment {
  private static final String LOG_TAG = AiueoSelectFragment.class.getSimpleName();

  enum Mode {
    NORMAL(),
    DIALOG() {
      @Override
      void dismiss(DialogFragment f) { f.dismiss(); }
    };

    void dismiss(DialogFragment f) { /*default: nothing*/ }
  }

  static AiueoSelectFragment newInstance(Mode mode) {
    AiueoSelectFragment fragment = new AiueoSelectFragment();
    Bundle args = new Bundle();
    args.putSerializable("mode", mode);
    fragment.setArguments(args);
    return fragment;
  }

  private Mode mode = Mode.NORMAL;
  private Button latestSelect;
  private int defaultColor = Color.BLACK;

  // xml で 50 個ボタン定義を並べるのは流石に面倒なのでプログラムで生成
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    Context context = container == null ? getContext() : container.getContext();
    assert context != null;

    MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    if(getArguments() != null) {
      mode = (Mode)getArguments().getSerializable("mode");
    }
    Log.d(LOG_TAG, "mode=" + mode);

    View.OnClickListener clickListener = v -> {
      Button button = (Button)v;
      changeSelected(button);
      Log.d(LOG_TAG, "this=" + AiueoSelectFragment.this + ", viewModel.setSelectedFromAiueo(" + button.getTag() + ")");
      viewModel.setSelectedFromAiueo((Aiueo)button.getTag());
      mode.dismiss(this);
    };

    float density = context.getResources().getDisplayMetrics().density; //px->dp
    int width = (int)(60 * density);
    int height = (int)(60 * density);
    int textSize = (int)(10 * density);

    ScrollView view = new ScrollView(context);
    HorizontalScrollView hscroll = new HorizontalScrollView(context);
    GridLayout grid = new GridLayout(context);
    grid.setColumnCount(5);
    EnumMap<Aiueo,Button> bMap = new EnumMap<>(Aiueo.class);
    for(int i=0, j=0; i<Aiueo.values().length; i++, j++) {
      Aiueo aiueo = Aiueo.values()[i];
      if(aiueo == Aiueo.ゆ || aiueo == Aiueo.よ || aiueo == Aiueo.他) j++; //ボタンの隙間を空ける(enum の並び順依存)
      Button button = new AppCompatButton(context);
      GridLayout.LayoutParams params = new GridLayout.LayoutParams();
      params.width = width;
      params.height = height;
      params.columnSpec = GridLayout.spec(j % 5);
      params.rowSpec = GridLayout.spec(j / 5);
      button.setLayoutParams(params);
      button.setText(aiueo.toString());
      button.setTextSize(textSize);
      button.setTag(aiueo);
      button.setOnClickListener(clickListener);
      grid.addView(button);

      bMap.put(aiueo, button);
      defaultColor = button.getTextColors().getDefaultColor(); //どれでもいいのでとにかくデフォルトの色
    }
    hscroll.addView(grid);
    view.addView(hscroll);

    viewModel.getSelectedFromAiueo().observe(getViewLifecycleOwner(), aiueo -> changeSelected(bMap.get(aiueo)));

    return view;
  }

  private void changeSelected(Button button) {
    if(latestSelect != button) {
      if(latestSelect != null) {
        latestSelect.setTextColor(defaultColor);
      }
      latestSelect = button;
      if(latestSelect != null) {
        latestSelect.setTextColor(Color.RED);
      }
    }
  }
}
