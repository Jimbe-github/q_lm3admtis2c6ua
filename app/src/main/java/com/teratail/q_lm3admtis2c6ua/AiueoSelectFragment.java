package com.teratail.q_lm3admtis2c6ua;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.EnumMap;

//普通にフラグメントとしての使用の他、ダイアログとしても使用可能
public class AiueoSelectFragment extends DialogFragment {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = AiueoSelectFragment.class.getSimpleName();

  private Dialog dialog;
  private Button latestSelect;
  private int defaultColor = Color.BLACK;

  // xml で 50 個ボタン定義を並べるのは流石に面倒なのでプログラムで生成
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    Context context = container == null ? getContext() : container.getContext();
    assert context != null;

    MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    View.OnClickListener clickListener = v -> {
      Button button = (Button)v;
      changeSelected(button);
      viewModel.setSelectedAiueo((Aiueo)button.getTag());
      if(dialog != null) {
        dialog.dismiss();
        dialog = null; //念の為
      }
    };

    EnumMap<Aiueo,Button> bMap = new EnumMap<>(Aiueo.class);

    GridLayout grid = new GridLayout(context);
    grid.setColumnCount(5);
    for(int i=0, j=0; i<Aiueo.values().length; i++, j++) {
      Aiueo aiueo = Aiueo.values()[i];
      if(aiueo == Aiueo.ゆ || aiueo == Aiueo.よ || aiueo == Aiueo.他) j++; //ボタンの隙間を空ける(enum の並び順依存)
      Button button = createButton(grid, 60, 60, aiueo.toString(), 30, j%5, j/5);
      button.setOnClickListener(clickListener);
      button.setTag(aiueo);
      grid.addView(button);
      bMap.put(aiueo, button);
      defaultColor = button.getTextColors().getDefaultColor(); //どれでもいいのでとにかくデフォルトの色
    }

    viewModel.getSelectedAiueo().observe(getViewLifecycleOwner(), aiueo -> changeSelected(bMap.get(aiueo)));

    HorizontalScrollView hscroll = new HorizontalScrollView(context);
    hscroll.addView(grid);
    ScrollView view = new ScrollView(context);
    view.addView(hscroll);
    return view;
  }

  private Button createButton(ViewGroup parent, int width_dp, int height_dp, String text, int textSize_dp, int column, int row) {
    float density = parent.getContext().getResources().getDisplayMetrics().density; //dp->px
    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
    params.width = (int)(width_dp * density); //[px]
    params.height = (int)(height_dp * density); //[px]
    params.columnSpec = GridLayout.spec(column);
    params.rowSpec = GridLayout.spec(row);

    Button button = new AppCompatButton(parent.getContext());
    button.setLayoutParams(params);
    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize_dp);
    button.setText(text);
    return button;
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

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    dialog = super.onCreateDialog(savedInstanceState);
    return dialog;
  }
}
