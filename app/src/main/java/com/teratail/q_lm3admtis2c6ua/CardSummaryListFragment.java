package com.teratail.q_lm3admtis2c6ua;

import android.annotation.SuppressLint;
import android.database.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.*;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import com.google.android.material.progressindicator.CircularProgressIndicator;

public class CardSummaryListFragment extends Fragment {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = CardSummaryListFragment.class.getSimpleName();

  public CardSummaryListFragment() {
    super(R.layout.fragment_cardsummarylist);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    TextView countText = view.findViewById(R.id.countText);
    Button button = view.findViewById(R.id.button);
    //xml 上に ( 直接 AiueoSelectFragment を指定してある FragmentContainerView の )ID があるなら, ダイアログは必要無い = null
    if(view.findViewById(R.id.aiueo_select) == null) {
      button.setOnClickListener(v ->
              AiueoSelectFragment.newInstance(AiueoSelectFragment.Mode.DIALOG).show(getChildFragmentManager(), null));
    }

    ProgressBar circlerProgess = view.findViewById(R.id.circularProgress);
    RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
    CardSummaryAdapter adapter = new CardSummaryAdapter(viewModel::setSelectedCardSummary);
    recyclerView.setAdapter(adapter);

    viewModel.getSelectedAiueo().observe(getViewLifecycleOwner(), aiueo -> {
      if(button.getTag() == aiueo) return;
      button.setTag(aiueo);
      button.setText(aiueo == null ? "(未選択)" : "" + aiueo);
      viewModel.requestCardSummaryCursor(aiueo);
      circlerProgess.setVisibility(View.VISIBLE);
      recyclerView.setVisibility(View.INVISIBLE);
    });

    viewModel.getCardSummaryCursor().observe(getViewLifecycleOwner(), cursor -> {
      adapter.swapCursor(cursor);
      countText.setText("" + (cursor == null ? 0 : cursor.getCount()));
      circlerProgess.setVisibility(View.INVISIBLE);
      recyclerView.setVisibility(View.VISIBLE);
    });
  }

  private static class CardSummaryAdapter extends RecyclerView.Adapter<CardSummaryAdapter.ViewHolder> {
    @FunctionalInterface
    interface RowClickListener {
      void onClick(String cardUrl);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
      final TextView num, title, subtitle, author;

      public ViewHolder(@NonNull View itemView) {
        super(itemView);
        num = itemView.findViewById(R.id.num);
        title = itemView.findViewById(R.id.title);
        subtitle = itemView.findViewById(R.id.subtitle);
        author = itemView.findViewById(R.id.author);
        if(rowClickListener != null) itemView.setOnClickListener(v -> rowClickListener.onClick((String)itemView.getTag()));
      }
    }

    private final RowClickListener rowClickListener;
    private Cursor cursor;
    int titleIndex, subtitleIndex, urlIndex, authorIndex;

    public CardSummaryAdapter(RowClickListener rowClickListener) {
      this.rowClickListener = rowClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public Cursor swapCursor(Cursor cursor) {
      Cursor old = this.cursor;
      this.cursor = cursor;
      if(this.cursor != null) {
        titleIndex = this.cursor.getColumnIndex("title");
        subtitleIndex = this.cursor.getColumnIndex("subtitle");
        urlIndex = this.cursor.getColumnIndex("card_url");
        authorIndex = this.cursor.getColumnIndex("author");
      }
      notifyDataSetChanged();
      return old;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
      return new ViewHolder(inflate);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      cursor.moveToPosition(position);
      String title = cursor.getString(titleIndex);
      String subtitle = cursor.getString(subtitleIndex);
      String url = cursor.getString(urlIndex);
      String author = cursor.getString(authorIndex);

      holder.itemView.setTag(url);

      holder.num.setText(String.format("%d.", position+1));
      holder.title.setText(title);
      holder.subtitle.setText(subtitle);
      holder.author.setText(author);

      holder.subtitle.setVisibility(subtitle.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
      return cursor == null ? 0 : cursor.getCount();
    }
  }
}
