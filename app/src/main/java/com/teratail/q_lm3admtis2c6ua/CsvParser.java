package com.teratail.q_lm3admtis2c6ua;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.*;
import java.util.function.Consumer;

class CsvParser {
  private static class TokenBuilfer {
    private StringBuilder sb = new StringBuilder();
    private int countSpace = 0;

    void append(char c) {
      sb.append(c);
      countSpace = isSpace(c) ? countSpace + 1 : 0;
    }

    private boolean isSpace(char c) { return c == ' ' || c == '\t' || isCRLF(c); }

    void clear() {
      sb.delete(0, sb.length());
      countSpace = 0;
    }

    boolean allSpace() { return sb.length() == countSpace; }

    @Override
    public String toString() { return sb.toString(); }
  }

  private enum State { CAPTURE, END; } //'"' で囲まれたトークンで、終わりの '"' から区切り文字 (',' 等 ) までの間を飛ばす判定の為
  private boolean isTokenEnd(int c) { return c < 0 || c == ',' || isCRLF(c); }
  private static boolean isCRLF(int c) { return c == '\r' || c == '\n'; }

  private TokenBuilfer tb = new TokenBuilfer();
  private boolean useQuot = false;
  private int countQuot = 0;
  private State state = State.CAPTURE;

  void parse(String line, @NonNull Consumer<String> consumer) {
    try {
      parse(new StringReader(line), consumer);
    } catch(IOException ignore) {
    }
  }

  //サロゲート対応が必要かどうか…
  void parse(Reader reader, @NonNull Consumer<String> consumer) throws IOException {
    for(int c=0; c >= 0; ) {
      c = reader.read();
      put(c, consumer);
    }
  }

  private void put(int c, @NonNull Consumer<String> consumer) {
    if(isTokenEnd(c) && useQuot == (countQuot == 1)) {
      String token = tb.toString();
      tb.clear();
      useQuot = false;
      countQuot = 0;
      state = State.CAPTURE;
      consumer.accept(token);
      return;
    }

    if(state == State.END) return;

    if(c == '"') {
      if(useQuot) { //'"' で囲まれたトークン内
        if(++countQuot == 2) { //エスケープの 2 文字目なら
          tb.append((char)c);
          countQuot = 0;
        }
        return;
      }
      if(tb.allSpace()) { //( スペース後の )トークンの始まりの '"'
        useQuot = true;
        tb.clear();
        countQuot = 0;
        return;
      }
      //'"' で囲まれていないトークン内に '"' が出たら… → 無視(削除) か例外案件か
      return;
    }
    if(useQuot && countQuot == 1) {
      state = State.END;
      return;
    }
    tb.append((char)c);
  }
}
