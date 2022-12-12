package com.teratail.q_lm3admtis2c6ua;

import java.io.*;

/** 特定の目的のためだけの Reader の為、それ以外はテキトウ */
class LineFeedFactionReader extends Reader {
  private static final int INVALID = -2; //-1 は EOF として reader.read() が返すので使えない

  private Reader reader;
  private int nextChar = INVALID; //1文字バッファ

  LineFeedFactionReader(Reader reader) {
    this.reader = reader;
  }

  String readLine() throws IOException {
    StringBuilder sb = new StringBuilder();
    while(true) {
      int c = read();
      if(c == -1 || c == '\n')
        break;
      sb.append((char)c);
    }
    return sb.toString();
  }

  @Override
  public int read() throws IOException {
    if(nextChar != INVALID) {
      int c = nextChar;
      nextChar = INVALID;
      return c;
    }
    int c = reader.read();
    if(c == '\r') {
      c = reader.read();
      if(c != '\n') {
        nextChar = c;
        c = '\n';
      }
    }
    return c;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
