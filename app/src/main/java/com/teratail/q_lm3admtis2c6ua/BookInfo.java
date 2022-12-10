package com.teratail.q_lm3admtis2c6ua;

import androidx.annotation.NonNull;

import java.net.URL;

class BookInfo {
  final int num;
  final String title, subtitle, author;
  final URL url;

  BookInfo(int num, String title, URL url, String subtitle, String author) {
    this.num = num;
    this.title = title;
    this.url = url;
    this.subtitle = subtitle;
    this.author = author;
  }

  @NonNull
  @Override
  public String toString() {
    return "No." + num + " " + title + (subtitle.isEmpty() ? "" : "[" + subtitle + "]") + "(" + url + ") " + author;
  }
}
