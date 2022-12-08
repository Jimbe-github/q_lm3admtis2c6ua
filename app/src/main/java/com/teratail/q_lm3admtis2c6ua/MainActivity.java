package com.teratail.q_lm3admtis2c6ua;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
  private static final String REQUESTKEY_AIUEO_SELECT = "aiueo_select";

  private ExecutorService executor = new DownloadExecutor();
  private Future<?> future = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button button = findViewById(R.id.button);
    button.setOnClickListener(v -> {
      String selected = ((Button)v).getText().toString();
      AiueoSelectFragment.newInstance(REQUESTKEY_AIUEO_SELECT, selected).show(getSupportFragmentManager(), null);
    });

    RecyclerView recyclerView = findViewById(R.id.recyclerView);
    RowDataAdapter adapter = new RowDataAdapter(rowData -> {
      //行クリック時
    });
    recyclerView.setAdapter(adapter);

    FragmentManager fm = getSupportFragmentManager();
    fm.setFragmentResultListener(REQUESTKEY_AIUEO_SELECT, this, (requestKey, result) -> {
      try {
        if(future != null) future.cancel(true);
        URL url = createUrl(result.getString(AiueoSelectFragment.RESULT_AIUEO_TAG));
        future = executor.submit(new HttpRequest(url, html -> adapter.setList(createDataset(html))));
        button.setText(result.getString(AiueoSelectFragment.RESULT_AIUEO));
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    });
  }

  private class HttpRequest extends DownloadExecutor.CloseableCancellable<String> {
    private final URL url;
    private final Consumer<String> postFunc;

    HttpRequest(URL url, Consumer<String> postFunc) {
      this.url = url;
      this.postFunc = postFunc;
    }

    @Override
    public String call() {
      try {
        //Log.d("***", "url="+url);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.connect();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(InputStream is = con.getInputStream()) {
          setClosable(is);
          byte[] buf = new byte[4096]; //テキトウ
          for(int size; (size = is.read(buf)) >= 0; ) baos.write(buf, 0, size);
        } finally {
          con.disconnect();
        }
        executePost(baos.toString("UTF-8"));
        return "";
      } catch (IOException e) {
        e.printStackTrace();
      }
      executePost("");
      return "";
    }
    private void executePost(String value) {
      runOnUiThread(() -> postFunc.accept(value));
    }
  }

  private URL createUrl(String yomi) throws MalformedURLException {
    String urltext = "https://www.aozora.gr.jp/index_pages/sakuhin_" + yomi + "1.html";
    return new URL(urltext);
  }

  private List<RowData> createDataset(String html) {
    return parseDocument(Jsoup.parse(html));
  }

  private List<RowData> parseDocument(Document doc) {
    List<RowData> rowDataList = new ArrayList<>();

    Elements rows = doc.select("tr");
    for(Element row : rows) {
      Elements tds = row.select("td");
      try {
        int num = Integer.parseInt(tds.get(0).text());
        Element titleElem = tds.get(1);
        Elements link = titleElem.select("a");
        String title = link.text().trim();
        String href = link.attr("href");
        link.remove();
        String subtitle = titleElem.text().trim();
        String author = tds.get(3).text().trim(); //著者名
        RowData rowData = new RowData(num, title, href, subtitle, author);
        //Log.d("***", ""+rowData);
        rowDataList.add(rowData);
      } catch(NumberFormatException e) { //番号が数字で無かったら書籍データでは無い
        //continue;
      }
    }
    return rowDataList;
  }
}

class DownloadExecutor extends ThreadPoolExecutor {
  interface Cancellable<T> extends Callable<T> {
    void cancel();
    RunnableFuture<T> newTask();
  }
  public static abstract class CloseableCancellable<T> implements Cancellable<T> {
    protected Closeable closeable;
    protected synchronized void setClosable(Closeable closeable) {
      this.closeable = closeable;
    }
    @Override
    public synchronized void cancel() {
      if(closeable != null) {
        try {
          closeable.close();
          closeable = null;
        } catch(IOException ignore) {
        }
      }
    }
    @Override
    public RunnableFuture<T> newTask() {
      return new FutureTask<T>(this) {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
          CloseableCancellable.this.cancel();
          return super.cancel(mayInterruptIfRunning);
        }
      };
    }
  }

  public DownloadExecutor() {
    super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
  }
  @Override
  protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
    if(callable instanceof Cancellable) {
      return ((Cancellable<T>)callable).newTask();
    }
    return super.newTaskFor(callable);
  }
}

class RowData {
  final int num;
  final String title, href, subtitle, author;

  RowData(int num, String title, String href, String subtitle, String author) {
    this.num = num;
    this.title = title;
    this.href = href;
    this.subtitle = subtitle;
    this.author = author;
  }
  @NonNull
  @Override
  public String toString() {
    return "No."+num+" "+title+(subtitle.isEmpty()?"":"["+subtitle+"]")+"("+href+") "+author;
  }
}

class RowDataAdapter extends RecyclerView.Adapter<RowDataAdapter.ViewHolder> {
  @FunctionalInterface
  interface RowClickListener {
    void onClick(RowData rowData);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    final TextView num, title, subtitle, author;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      num = itemView.findViewById(R.id.num);
      title = itemView.findViewById(R.id.title);
      subtitle = itemView.findViewById(R.id.subtitle);
      author = itemView.findViewById(R.id.author);
      if(rowClickListener != null)
        itemView.setOnClickListener(v -> rowClickListener.onClick((RowData)itemView.getTag()));
    }
  }

  private final RowClickListener rowClickListener;
  private List<RowData> list = Collections.emptyList();

  public RowDataAdapter(RowClickListener rowClickListener) {
    this.rowClickListener = rowClickListener;
  }

  @SuppressLint("NotifyDataSetChanged")
  public void setList(List<RowData> list) {
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
    RowData rowData = list.get(position);
    holder.itemView.setTag(rowData);
    holder.num.setText(rowData.num + ".");
    holder.title.setText(rowData.title);
    holder.subtitle.setText(rowData.subtitle);
    holder.author.setText(rowData.author);

    holder.subtitle.setVisibility(rowData.subtitle.isEmpty() ? View.GONE : View.VISIBLE);
  }

  @Override
  public int getItemCount() {
    return list.size();
  }
}
