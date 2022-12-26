package com.teratail.q_lm3admtis2c6ua;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class CsvReader implements Closeable {
  private Reader reader;
  private boolean eof = false;
  private boolean wasCr = false;

  CsvReader(Reader reader) {
    this.reader = reader;
  }

  public List<String> readLine() throws IOException {
    if(eof) return null;

    List<String> line = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    boolean inDQuot = false, wasDQuot = false;
    boolean skipLeft = false;
    while(true) {
      int c = reader.read();
      //System.out.println("c=" + (c==-1?"-1":""+(char)c));
      if(c == 0xfeff || c == 0xfffe) continue; //BOM
      eof = c == -1;
      //改行コード対応(CR/CRLF/LF)
      if(c == '\n' && wasCr) continue;
      wasCr = false;
      if(c == '\r') {
        c = '\n';
        wasCr = true;
      }
      //ダブルクオット対応
      if(c == '"' && !skipLeft) {
        if(sb.toString().trim().length() == 0 && !inDQuot) {
          inDQuot = true; //'"' で囲まれたトークンらしぃ
          sb.delete(0, sb.length()); //クリア
          continue;
        }
        if(wasDQuot) {
          wasDQuot = false; //二つ目は普通の文字として通す
        } else if(inDQuot) {
          wasDQuot = true; //一つ目は無視(終わりの可能性も)
          continue;
        }
      } else if(inDQuot && wasDQuot) { //トークンが終わっている
        inDQuot = false;
        skipLeft = true; //トークン区切りまで無視するモード
      }
      //区切り
      if(eof || ((c == '\n' || c == ',') && !inDQuot)) {
        String token = sb.toString();
        if(!wasDQuot) token = token.trim();
        line.add(token);
        if(eof || c == '\n') break;
        sb.delete(0, sb.length()); //クリア
        skipLeft = inDQuot = wasDQuot = false; //クリア
        continue;
      }
      if(skipLeft) continue;

      sb.append((char)c);
    }
    return line;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
