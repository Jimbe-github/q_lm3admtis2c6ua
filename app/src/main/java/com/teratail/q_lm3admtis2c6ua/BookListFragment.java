package com.teratail.q_lm3admtis2c6ua;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.*;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class BookListFragment extends Fragment {
  private static final String LOG_TAG = BookListFragment.class.getSimpleName();

  public BookListFragment() {
    super(R.layout.fragment_booklist);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    Button button = view.findViewById(R.id.button);
    button.setOnClickListener(v -> viewModel.requestSelectingFromAiueo());

    viewModel.getSelectedFromAiueo().observe(getViewLifecycleOwner(), aiueo -> {
      if(button.getTag() == aiueo) return;
      button.setTag(aiueo);
      button.setText(aiueo == null ? "(未選択)" : "" + aiueo);
      Log.d(LOG_TAG, "this=" + BookListFragment.this + " viewModel.requestBookInfoList(" + aiueo + ")");
      viewModel.requestBookInfoList(aiueo, 1);
    });

    RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
    BookInfoAdapter adapter = new BookInfoAdapter(rowData -> {
      //行クリック時
    });
    recyclerView.setAdapter(adapter);
    viewModel.getBooklistPage().observe(getViewLifecycleOwner(), booklistPage -> {
      adapter.setList(booklistPage.list);
    });
  }

  private static class BookInfoAdapter extends RecyclerView.Adapter<BookInfoAdapter.ViewHolder> {
    @FunctionalInterface
    interface RowClickListener {
      void onClick(BookInfo bookInfo);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
      final TextView num, title, subtitle, author;

      public ViewHolder(@NonNull View itemView) {
        super(itemView);
        num = itemView.findViewById(R.id.num);
        title = itemView.findViewById(R.id.title);
        subtitle = itemView.findViewById(R.id.subtitle);
        author = itemView.findViewById(R.id.author);
        if(rowClickListener != null) itemView.setOnClickListener(v -> rowClickListener.onClick((BookInfo)itemView.getTag()));
      }
    }

    private final RowClickListener rowClickListener;
    private List<BookInfo> list = Collections.emptyList();

    public BookInfoAdapter(RowClickListener rowClickListener) {
      this.rowClickListener = rowClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<BookInfo> list) {
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
      BookInfo bookInfo = list.get(position);
      holder.itemView.setTag(bookInfo);
      holder.num.setText(bookInfo.num + ".");
      holder.title.setText(bookInfo.title);
      holder.subtitle.setText(bookInfo.subtitle);
      holder.author.setText(bookInfo.author);

      holder.subtitle.setVisibility(bookInfo.subtitle.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
      return list.size();
    }
  }
}
