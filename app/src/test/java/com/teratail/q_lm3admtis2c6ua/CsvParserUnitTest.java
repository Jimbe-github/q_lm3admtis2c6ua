package com.teratail.q_lm3admtis2c6ua;

import static org.junit.Assert.assertEquals;

import org.junit.*;

import java.util.*;

public class CsvParserUnitTest {
  private CsvParser parser;
  private List<String> list;
  @Before
  public void befor() {
    list = new ArrayList<>();
    parser = new CsvParser();
  }
  @Test
  public void test0lengthTokens() {
    parser.parse("", list::add);

    assertEquals(1, list.size());
    assertEquals("", list.get(0));
  }
  @Test
  public void test2EmptyTokenlines() {
    parser.parse("\n", list::add);

    assertEquals(2, list.size());
    assertEquals("", list.get(0));
    assertEquals("", list.get(1));
  }
  @Test
  public void test1Tokens() {
    parser.parse("aaa", list::add);

    assertEquals(1, list.size());
    assertEquals("aaa", list.get(0));
  }
  @Test
  public void test3Tokens() {
    parser.parse("aaa,bbb,ccc", list::add);

    assertEquals(3, list.size());
    assertEquals("aaa", list.get(0));
    assertEquals("bbb", list.get(1));
    assertEquals("ccc", list.get(2));
  }
  @Test
  public void testEmptyTokensWithQuot() {
    parser.parse("\"\"", list::add);

    assertEquals(1, list.size());
    assertEquals("", list.get(0));
  }
  @Test
  public void test1TokensWithQuot() {
    parser.parse("\"aaa\"", list::add);

    assertEquals(1, list.size());
    assertEquals("aaa", list.get(0));
  }
  @Test
  public void test3TokensWithQuot() {
    parser.parse("\"aaa\",\"bbb\",\"ccc\"", list::add);

    assertEquals(3, list.size());
    assertEquals("aaa", list.get(0));
    assertEquals("bbb", list.get(1));
    assertEquals("ccc", list.get(2));
  }
  @Test
  public void testEscapeQuots() {
    parser.parse("\"\"\"\"", list::add);

    assertEquals(1, list.size());
    assertEquals("\"", list.get(0));
  }
  @Test
  public void testSpaceTrim() {
    parser.parse(" \t \" \ta \t\" \t , b ", list::add);

    assertEquals(2, list.size());
    assertEquals(" \ta \t", list.get(0));
    assertEquals(" b ", list.get(1));
  }
  @Test
  public void testHeader() {
    parser.parse("作品ID,作品名,作品名読み,ソート用読み,副題,副題読み,原題,初出,分類番号,文字遣い種別,作品著作権フラグ,公開日,最終更新日,図書カードURL,人物ID,姓,名,姓読み,名読み,姓読みソート用,名読みソート用,姓ローマ字,名ローマ字,役割フラグ,生年月日,没年月日,人物著作権フラグ,底本名1,底本出版社名1,底本初版発行年1,入力に使用した版1,校正に使用した版1,底本の親本名1,底本の親本出版社名1,底本の親本初版発行年1,底本名2,底本出版社名2,底本初版発行年2,入力に使用した版2,校正に使用した版2,底本の親本名2,底本の親本出版社名2,底本の親本初版発行年2,入力者,校正者,テキストファイルURL,テキストファイル最終更新日,テキストファイル符号化方式,テキストファイル文字集合,テキストファイル修正回数,XHTML/HTMLファイルURL,XHTML/HTMLファイル最終更新日,XHTML/HTMLファイル符号化方式,XHTML/HTMLファイル文字集合,XHTML/HTMLファイル修正回数", list::add);

    assertEquals(55, list.size());
    assertEquals("作品ID", list.get(0));
    assertEquals("XHTML/HTMLファイル修正回数", list.get(54));
  }
  @Test
  public void testData() {
    parser.parse("\"059898\",\"ウェストミンスター寺院\",\"ウェストミンスターじいん\",\"うえすとみんすたあしいん\",\"\",\"\",\"\",\"\",\"NDC 933\",\"新字新仮名\",\"なし\",2020-04-03,2020-03-28,\"https://www.aozora.gr.jp/cards/001257/card59898.html\",\"001257\",\"アーヴィング\",\"ワシントン\",\"アーヴィング\",\"ワシントン\",\"ああういんく\",\"わしんとん\",\"Irving\",\"Washington\",\"著者\",\"1783-04-03\",\"1859-11-28\",\"なし\",\"スケッチ・ブック\",\"新潮文庫、新潮社\",\"1957（昭和32）年5月20日\",\"2000（平成12）年2月20日33刷改版\",\"2000（平成12）年2月20日33刷改版\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"えにしだ\",\"砂場清隆\",\"https://www.aozora.gr.jp/cards/001257/files/59898_ruby_70679.zip\",\"2020-03-28\",\"ShiftJIS\",\"JIS X 0208\",\"0\",\"https://www.aozora.gr.jp/cards/001257/files/59898_70731.html\",\"2020-03-28\",\"ShiftJIS\",\"JIS X 0208\",\"0\"", list::add);

    assertEquals(55, list.size());
    assertEquals("ウェストミンスター寺院", list.get(1));
    assertEquals("JIS X 0208", list.get(53));
  }
}
