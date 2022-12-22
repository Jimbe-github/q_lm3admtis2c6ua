package com.teratail.q_lm3admtis2c6ua;

import static com.teratail.q_lm3admtis2c6ua.AozoraDatabase.CardSummary;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.fragment.app.*;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

public class CardSummaryListFragment extends Fragment {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = CardSummaryListFragment.class.getSimpleName();

  static final String REQUESTKEY_SELECT_URL = CardSummaryListFragment.class.getSimpleName()+".selectUrl";
  static final String REQUESTKEY_SELECT_AIUEO = CardSummaryListFragment.class.getSimpleName()+".selectAiueo";

  private MainViewModel viewModel;

  public CardSummaryListFragment() {
    super(R.layout.fragment_cardsummarylist);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    FragmentManager pfm = getParentFragmentManager();

    ProgressBar circlerProgess = view.findViewById(R.id.circularProgress);
    RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
    CardSummaryAdapter adapter = new CardSummaryAdapter(url -> {
      viewModel.setSelectedCardUrl(url);
      pfm.setFragmentResult(REQUESTKEY_SELECT_URL, new Bundle());
    });
    recyclerView.setAdapter(adapter);

    viewModel.getCardSummaryProcessState().observe(getViewLifecycleOwner(), state -> {
      boolean inProcessing = state == MainViewModel.CardSummaryProcessState.PROCESSING;
      circlerProgess.setVisibility(inProcessing ? View.VISIBLE : View.INVISIBLE);
      recyclerView.setVisibility(inProcessing ? View.INVISIBLE : View.VISIBLE);
    });

    viewModel.getCardSummaryCursor().observe(getViewLifecycleOwner(), adapter::swapCursor); //戻り値の Cursor は close したりしない

    ActionBar actionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();
    viewModel.getTitle().observe(getViewLifecycleOwner(), actionBar::setTitle);
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
        titleIndex = this.cursor.getColumnIndex(CardSummary.TITLE);
        subtitleIndex = this.cursor.getColumnIndex(CardSummary.SUBTITLE);
        urlIndex = this.cursor.getColumnIndex(CardSummary.CARD_URL);
        authorIndex = this.cursor.getColumnIndex(CardSummary.AUTHOR);
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
