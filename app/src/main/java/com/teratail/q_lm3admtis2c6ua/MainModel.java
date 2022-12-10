package com.teratail.q_lm3admtis2c6ua;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.regex.*;

class BooklistPage {
  final List<BookInfo> list;
  final Aiueo aiueo;
  final int currentpage, maxPages;

  BooklistPage(List<BookInfo> list, Aiueo aiueo, int currentPage, int maxPages) {
    this.list = Collections.unmodifiableList(list);
    this.aiueo = aiueo;
    this.currentpage = currentPage;
    this.maxPages = maxPages;
  }
}

public class MainModel implements DefaultLifecycleObserver {
  private static final String LOG_TAG = MainModel.class.getSimpleName();

  private final ExecutorService executor = new DownloadExecutor();
  private Future<?> future = null;

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    DefaultLifecycleObserver.super.onStop(owner);
    //Log.d(LOG_TAG, "onStop: executor shutdown now.");
    if(future != null) future.cancel(true);
    executor.shutdownNow();
  }

  private BooklistPage booklistPageCache = null;
  void requestBooklistPage(Aiueo aiueo, int page, Consumer<BooklistPage> callback) {
    //TODO: まともにキャッシュするならサーバ側の更新日付のチェックとか・・・
    if(booklistPageCache != null && booklistPageCache.aiueo == aiueo && booklistPageCache.currentpage == page) {
      callback.accept(booklistPageCache);
    }
    if(future != null) {
      future.cancel(true);
      future = null;
    }
    if(aiueo != null) {
      final int currentPage = page > 1 ? page : 1;
      future = executor.submit(new HttpRequest(createUrl(aiueo, page), html -> {
        Document document = Jsoup.parse(html);
        int maxPage = getPageLinkMax(document);
        if(maxPage == currentPage-1) maxPage = currentPage; //currentPage のリンクは無い為、最終ページの場合リンクの最大は1少ないので補正
        callback.accept(new BooklistPage(parseList(document), aiueo, currentPage, maxPage));
      }));
    }
  }

  private URL createUrl(Aiueo aiueo, int page) {
    try {
      String urltext = "https://www.aozora.gr.jp/index_pages/sakuhin_" + aiueo.tag + page + ".html";
      return new URL(urltext);
    } catch(MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static class DownloadExecutor extends ThreadPoolExecutor {
    interface Cancellable<T> extends Callable<T> {
      void cancel();
      RunnableFuture<T> newTask();
    }
    static abstract class CloseableCancellable<T> implements Cancellable<T> {
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

  private static class HttpRequest extends DownloadExecutor.CloseableCancellable<Void> {
    private static final String LOG_TAG = HttpRequest.class.getSimpleName();
    private final URL url;
    private final Consumer<String> postFunc;

    HttpRequest(URL url, Consumer<String> postFunc) {
      this.url = url;
      this.postFunc = postFunc;
    }

    @Override
    public Void call() {
      try {
        Log.d(LOG_TAG, "this=" + this + ", url="+url);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.connect();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(InputStream is = new BufferedInputStream(con.getInputStream())) {
          setClosable(is);
          byte[] buf = new byte[4096]; //テキトウ
          for(int size; (size = is.read(buf)) >= 0; ) baos.write(buf, 0, size);
        } finally {
          con.disconnect();
        }
        executePost(baos.toString("UTF-8"));
        return null;
      } catch (IOException e) {
        e.printStackTrace();
      }
      executePost("");
      return null;
    }
    private void executePost(String value) {
      postFunc.accept(value);
    }
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
        //Log.d(LOG_TAG, ""+bookInfo);
        list.add(bookInfo);
      } catch(NumberFormatException e) { //番号が数字で無かったら書籍データでは無い
        //continue;
      }
    }
    return list;
  }

  private int getPageLinkMax(Document doc) {
    int count = 1;
    Elements links = doc.select("a[href^=sakuhin_]");
    Pattern p = Pattern.compile("sakuhin_[a-z]{1,2}(\\d+)\\.html");
    for(Element link : links) {
      String href = link.attr("href");
      //Log.d(LOG_TAG,"href="+href);
      Matcher m = p.matcher(href);
      if(m.matches()) {
        try {
          int num = Integer.parseInt(Objects.requireNonNull(m.group(1)));
          count = Math.max(count, num);
        } catch(NumberFormatException ignore) { //念の為
        }
      }
    }
    return count;
  }
}
