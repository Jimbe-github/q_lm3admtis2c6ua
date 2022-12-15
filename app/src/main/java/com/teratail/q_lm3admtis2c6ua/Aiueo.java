package com.teratail.q_lm3admtis2c6ua;

public enum Aiueo {
  あ, い, う, え, お,
  か, き, く, け, こ,
  さ, し, す, せ, そ,
  た, ち, つ, て, と,
  な, に, ぬ, ね, の,
  は, ひ, ふ, へ, ほ,
  ま, み, む, め, も,
  や, ゆ, よ,
  ら, り, る, れ, ろ,
  わ, を, ん, 他;

  /** Aiueo.他 以外の要素を区切り無く文字列として並べたもの */
  public static final String VALID_STRING;
  static {
    StringBuilder sb = new StringBuilder();
    for(Aiueo aiueo : values()) if(aiueo != Aiueo.他) sb.append(aiueo.toString());
    VALID_STRING = sb.toString();
  }
}
