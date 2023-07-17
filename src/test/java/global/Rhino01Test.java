package global;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Rhino01Test {

  // テスト開始前に1回だけ実行される
  @BeforeAll
  static void beforeAll() {
    //System.out.println("Rhino01Test 開始");
  }

  // テスト開始後に1回だけ実行される
  @AfterAll
  static void afterAll() {
    //System.out.println("Rhino01Test 終了");
  }

  // 各テストメソッド開始前に実行される
  @BeforeEach
  void beforeEach() {
    //System.out.println("Rhino01Test のテストメソッドをひとつ開始");
  }

  // 各テストメソッド開始後に実行される
  @AfterEach
  void afterEach() {
    //System.out.println("Rhino01Test のテストメソッドをひとつ終了");
  }

  @Test
  void testPlus() {
    System.out.println("testPlus を実行: 11 + 22 = 33");
    int x = 11 + 22;
    assertEquals(33, x, "testPlus()の検証");
  }

}
