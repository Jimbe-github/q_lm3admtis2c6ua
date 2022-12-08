package com.teratail.q_lm3admtis2c6ua;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

public class AiueoSelectFragment extends DialogFragment {
  private static final String ARG_REQUESTKEY = "requestkey";
  private static final String ARG_SELECTED = "selected";

  public static final String RESULT_AIUEO = "aiueo";
  public static final String RESULT_AIUEO_TAG = "aiueo_tag";

  private static final String AIUEO =
          "あa いi うu えe おo " + "かkaきkiくkuけkeこko" + "さsaしsiすsuせseそso" +
          "たtaちtiつtuてteとto" + "なnaにniぬnuねneのno" + "はhaひhiふhuへheほho" +
          "まmaみmiむmuめmeもmo" + "やya　  ゆyu　  よyo" + "らraりriるruれreろro" +
          "わwaをwoんnn　  他zz";

  static AiueoSelectFragment newInstance(String requestKey, String selected) {
    AiueoSelectFragment fragment = new AiueoSelectFragment();
    Bundle args = new Bundle();
    args.putString(ARG_REQUESTKEY, requestKey);
    args.putString(ARG_SELECTED, selected);
    fragment.setArguments(args);
    return fragment;
  }

  private Button latestSelect;
  private Runnable closeProcess;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    closeProcess = () -> dismiss();
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity())
            .setView(createView())
            .setNegativeButton("キャンセル", null);
    return builder.create();
  }

  private View createView() {
    Context context = getContext();

    String requestKey = getArguments().getString(ARG_REQUESTKEY);
    String selected = getArguments().getString(ARG_SELECTED);

    View.OnClickListener clickListener = v -> {
      Button button = (Button)v;
      changeSelected(button);
      Bundle result = new Bundle();
      result.putString(RESULT_AIUEO, button.getText().toString());
      result.putString(RESULT_AIUEO_TAG, (String)button.getTag());
      getParentFragmentManager().setFragmentResult(requestKey, result);
      if(closeProcess != null) closeProcess.run();
    };

    float density = context.getResources().getDisplayMetrics().density;
    int width = (int)(60 * density);
    int height = (int)(60 * density);

    ScrollView view = new ScrollView(context);
    GridLayout grid = new GridLayout(context);
    grid.setColumnCount(5);
    for(int i=0, j=0; i<AIUEO.length(); i+=3, j++) {
      if(AIUEO.charAt(i) == '　') continue; //全角スペース
      String text = "" + AIUEO.charAt(i);
      Button button = new AppCompatButton(context);
      GridLayout.LayoutParams params = new GridLayout.LayoutParams();
      params.width = width;
      params.height = height;
      params.columnSpec = GridLayout.spec(j % 5);
      params.rowSpec = GridLayout.spec(j / 5);
      button.setLayoutParams(params);
      button.setText(text);
      button.setTextSize(30);
      button.setTag(AIUEO.substring(i+1, i+3).trim());
      button.setOnClickListener(clickListener);
      if(text.equals(selected)) changeSelected(button);
      grid.addView(button);
    }
    view.addView(grid);

    return view;
  }
  private void changeSelected(Button button) {
    if(latestSelect != button) {
      if(latestSelect != null) {
        latestSelect.setTextColor(button.getTextColors().getDefaultColor());
      }
      latestSelect = button;
      latestSelect.setTextColor(Color.RED);
    }
  }
}
