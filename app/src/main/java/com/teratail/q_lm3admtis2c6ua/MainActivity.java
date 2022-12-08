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
import java.util.regex.*;

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
    BookInfoAdapter adapter = new BookInfoAdapter(rowData -> {
      //行クリック時
    });
    recyclerView.setAdapter(adapter);

    FragmentManager fm = getSupportFragmentManager();
    fm.setFragmentResultListener(REQUESTKEY_AIUEO_SELECT, this, (requestKey, result) -> {
      try {
        if(future != null) future.cancel(true);
        URL url = createUrl(result.getString(AiueoSelectFragment.RESULT_AIUEO_TAG));
        future = executor.submit(new HttpRequest(url, html -> {
          Document document = Jsoup.parse(html);
          adapter.setList(parseList(document));
          int maxPage = parsePageCount(document);
          //Log.d("***", "maxPage="+maxPage);
        }));
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

  private List<BookInfo> createDataset(String html) {
    return parseList(Jsoup.parse(html));
  }

  private List<BookInfo> parseList(Document doc) {
    List<BookInfo> list = new ArrayList<>();

    Elements rows = doc.select("table.list tr:not(:first-child)");
    for(Element row : rows) {
      Elements tds = row.select("td");
      try {
        int num = Integer.parseInt(tds.get(0).text()); //番号
        Element titleElem = tds.get(1); //<a href="～">タイトル</a><br>サブタイトル
        Elements link = titleElem.select("a");
        String title = link.text().trim(); //タイトル
        String href = link.attr("href");
        String[] titles = titleElem.html().split("<br>", 2);
        String subtitle = titles.length > 1 ? titles[1].trim() : ""; //サブタイトル
        String author = tds.get(3).text().trim(); //著者名
        BookInfo bookInfo = new BookInfo(num, title, href, subtitle, author);
        //Log.d("***", ""+bookInfo);
        list.add(bookInfo);
      } catch(NumberFormatException e) { //番号が数字で無かったら書籍データでは無い
        //continue;
      }
    }
    return list;
  }

  private int parsePageCount(Document doc) {
    int count = 1;
    Elements links = doc.select("a[href^=sakuhin_]");
    Pattern p = Pattern.compile("sakuhin_[a-z]{1,2}(\\d+)\\.html");
    for(Element link : links) {
      String href = link.attr("href");
      Log.d("***","href="+href);
      Matcher m = p.matcher(href);
      if(m.matches()) {
        try {
          int num = Integer.parseInt(m.group(1));
          count = Math.max(count, num);
        } catch(NumberFormatException ignore) { //念の為
        }
      }
    }
    return count;
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

class BookInfo {
  final int num;
  final String title, href, subtitle, author;

  BookInfo(int num, String title, String href, String subtitle, String author) {
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

class BookInfoAdapter extends RecyclerView.Adapter<BookInfoAdapter.ViewHolder> {
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
      if(rowClickListener != null)
        itemView.setOnClickListener(v -> rowClickListener.onClick((BookInfo)itemView.getTag()));
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
