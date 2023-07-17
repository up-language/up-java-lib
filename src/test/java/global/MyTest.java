package global;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalcTest {

  // テスト開始前に1回だけ実行される
  @BeforeAll
  static void beforeAll() {
    System.out.println("CalcTest 開始");
  }

  // テスト開始後に1回だけ実行される
  @AfterAll
  static void afterAll() {
    System.out.println("CalcTest 終了");
  }

  // 各テストメソッド開始前に実行される
  @BeforeEach
  void beforeEach() {
    //System.out.println("CalcTest のテストメソッドをひとつ開始");
  }

  // 各テストメソッド開始後に実行される
  @AfterEach
  void afterEach() {
    //System.out.println("CalcTest のテストメソッドをひとつ終了");
  }

  // テストメソッドは private や static メソッドにしてはいけない
  // 値を返してもいけないので戻り値は void にする
  @Test
  void testPlus() {
    System.out.println("testPlus を実行: 11 + 22 = 33");
    String s = """
    		   abc
    		   def""";
    System.out.println("["+s+"]");
    int x = 11 + 22;
    // 第1引数: expected 想定される結果
    // 第2引数: actual 実行結果
    // 第3引数: message 失敗時に出力するメッセージ
    assertEquals(33, x, "testPlus()の検証");
  }

}
