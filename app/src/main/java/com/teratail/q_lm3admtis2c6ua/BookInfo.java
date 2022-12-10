package com.teratail.q_lm3admtis2c6ua;

import androidx.annotation.NonNull;

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
    return "No." + num + " " + title + (subtitle.isEmpty() ? "" : "[" + subtitle + "]") + "(" + href + ") " + author;
  }
}
