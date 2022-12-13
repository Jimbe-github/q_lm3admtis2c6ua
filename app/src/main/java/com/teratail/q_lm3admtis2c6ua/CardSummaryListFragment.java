package com.teratail.q_lm3admtis2c6ua;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.*;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import java.util.*;

public class CardSummaryListFragment extends Fragment {
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

    viewModel.getSelectedAiueo().observe(getViewLifecycleOwner(), aiueo -> {
      if(button.getTag() == aiueo) return;
      button.setTag(aiueo);
      button.setText(aiueo == null ? "(未選択)" : "" + aiueo);
      viewModel.requestCardListWithAiueo(aiueo);
    });

    RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
    CardSummaryAdapter adapter = new CardSummaryAdapter(viewModel::setSelectedCardSummary);
    recyclerView.setAdapter(adapter);

    viewModel.getCardListWithAiueo().observe(getViewLifecycleOwner(), cardListWithAiueo -> {
      if(cardListWithAiueo == null) return;
      if(cardListWithAiueo.aiueo == button.getTag()){
        adapter.setList(cardListWithAiueo.list);
        countText.setText("" + cardListWithAiueo.list.size());
      }
    });

    viewModel.getDownload().observe(getViewLifecycleOwner(), workInfoList -> {
      Aiueo aiueo = (Aiueo)button.getTag();
      if(aiueo == null) return;
      if(workInfoList == null || workInfoList.size() == 0) return;
      WorkInfo workInfo = workInfoList.get(0);
      if(workInfo.getState().isFinished()) viewModel.requestCardListWithAiueo(aiueo);
    });
  }

  private static class CardSummaryAdapter extends RecyclerView.Adapter<CardSummaryAdapter.ViewHolder> {
    @FunctionalInterface
    interface RowClickListener {
      void onClick(CardSummary cardSummary);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
      final TextView num, title, subtitle, author;

      public ViewHolder(@NonNull View itemView) {
        super(itemView);
        num = itemView.findViewById(R.id.num);
        title = itemView.findViewById(R.id.title);
        subtitle = itemView.findViewById(R.id.subtitle);
        author = itemView.findViewById(R.id.author);
        if(rowClickListener != null) itemView.setOnClickListener(v -> rowClickListener.onClick((CardSummary)itemView.getTag()));
      }
    }

    private final RowClickListener rowClickListener;
    private List<CardSummary> list = Collections.emptyList();

    public CardSummaryAdapter(RowClickListener rowClickListener) {
      this.rowClickListener = rowClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<CardSummary> list) {
      this.list = new ArrayList<>(list); //防御コピー
      notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
      return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      CardSummary cardSummary = list.get(position);
      holder.itemView.setTag(cardSummary);
      holder.num.setText((position+1) + ".");
      holder.title.setText(cardSummary.title);
      holder.subtitle.setText(cardSummary.subtitle);
      holder.author.setText(cardSummary.author);

      holder.subtitle.setVisibility(cardSummary.subtitle.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
      return list.size();
    }
  }
}
