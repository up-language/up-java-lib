package global;

import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.oracle.truffle.js.scriptengine.*;
import org.graalvm.polyglot.*;
import org.json.JSONArray;
import org.json.JSONObject;

import com.eclipsesource.v8.*;

class Printer {
	public void print(Object x) {
		System.out.println(x);
	}
}

class Rhino02Test {

	@Test
	void test01() throws Exception {
		
		//ScriptEngine graalEngine = new ScriptEngineManager().getEngineByName("graal.js");
		//graalEngine.eval("print('Hello Graal!');");
		
		V8 v8 = V8.createV8Runtime();
		v8.registerJavaMethod(new Printer(), "print", "print", new Class<?>[] { Object.class });
		v8.executeVoidScript("print('Hello, World!');");
		v8.executeVoidScript("print(JSON.stringify('Hello, World 2!'));");
		v8.add("v8val", 12345);
		v8.executeVoidScript("print(v8val);");
		v8.add("v8val", 123456);
		v8.executeVoidScript("print(v8val);");
		Object v8obj = v8.executeObjectScript("[11,22]");
		System.out.println(v8obj.getClass().getName());
		v8.release(false);
		
		try (VM8 vm8 = new VM8()) {
			Object vm8obj = vm8.js("[11,22]");
			System.out.println(vm8obj.getClass().getName());
		}

		VM vm = new VM();
		vm.js("console.log('hello-world-0');");
		Object o1 = vm.jsToJson("[11,22]");
		System.out.printf("o1=%s\n", o1);
		assertEquals("[11,22]", o1.toString());
		Object o2 = vm.jsToJson("({x:11,y:22})");
		System.out.printf("o2=%s\n", o2);
		assertEquals("{\"x\":11,\"y\":22}", o2.toString());
		Object o3 = vm.jsToJson("123.45");
		System.out.printf("o3=%s\n", o3);
		assertEquals(123.45, o3);
		vm.setGlobal("count", 3);
		vm.js("print(count)");
		assertEquals(3, vm.js("count"));
		vm.load(":/json.js");
		// vm.loadFile("json.js");
		// vm.loadFile(":/json.js");
		// vm.loadFile("https://raw.githubusercontent.com/up-language/up-language/main/om-java/json.js");
		vm.js("print(JSON.stringify(json, null, 2))");
		assertEquals("{\"a\":\"abc\",\"b\":123,\"c\":[11,22,33]}", vm.jsToJson("json").toString());
		Object json = vm.js("json");
		assertEquals(33, vm.jsToJson("$0.c[2]", json));
		assertEquals(33, vm.jsToJson("$0.c[$1]", json, 2));
		vm.jsToJson("$0.c[$1]=$2", json, 2, 777);
		vm.js("print(JSON.stringify(json, null, 2))");
		assertEquals(777, vm.js("json.c[2]"));
		vm.print(json, "json");
		JSONArray ary = new JSONArray();
		ary.put(111);
		ary.put(222);
		ary.put(333);
		vm.print(ary);
		Object ref = vm.setGlobal("ary", ary);
		vm.print(ref);
		vm.js("$0[1]=777", ref);
		vm.print(vm.js("ary"));
		vm.js("console.log($0)", "this is $0");
		vm.load(":/run.js");
		// vm.load(":/error.js");
		Object dt = vm.load(":/date.js");
		System.out.println(dt.getClass().getName());
		vm.js("console.log(JSON.stringify(new Date()))");

		/*
		 * 
		 * rhino.eval2("print(JSON.stringify(json, null, 2))"); Object json =
		 * rhino.to_json(rhino.eval2("json")); System.out.printf("json=%s\n", json);
		 * Object json2 = rhino.to_json(rhino.from_json(json));
		 * System.out.printf("json2=%s\n", json2);
		 * 
		 * // context.evaluateString(global, "console.log('hello rhino!')", "<eval>", 1,
		 * // null); context.evaluateString(global, "globalThis.xyz = 'xyz'", "<eval>",
		 * 1, null); context.evaluateString(global, "print(xyz)", "<eval>", 1, null);
		 * 
		 * context.evaluateString(global, "globalThis.console = { log: print }",
		 * "<eval>", 1, null); context.evaluateString(global,
		 * "console.log('hello-world');", "<eval>", 1, null); // JSファイルのロード
		 * context.evaluateString(global, "load('index.js')", "<eval>", 1, null);
		 * context.evaluateString(global, "print(add2(22, 33))", "<eval>", 1, null);
		 * 
		 * Object xyz2 = rhino.call2("add2", new Object[] { 1111, 2222 });
		 * System.out.printf("xyz2=%s\n", xyz2);
		 * 
		 * Object xyz3 = rhino.eval2("({x:11,y:22})"); rhino.print(xyz3);
		 * rhino.print(true); rhino.print(false); rhino.print(null);
		 * rhino.print("a string"); rhino.print(rhino.access(xyz3, "x.y"));
		 */
		/*
		 * // JSファイルのロード context.evaluateString(global, "load('JsFunctions.js')",
		 * "<eval>", 1, null);
		 * 
		 * // パッケージのインポート context.evaluateString(global,
		 * "importPackage(Packages.test.js)", "<eval>", 1, null);
		 * 
		 * // スクリプトをコンパイル Script compiledScript =
		 * context.compileString("print(sum(10,50))", "<eval>", 1, null);
		 * 
		 * // グローバル変数を定義 context.evaluateString(global, "var gVal = 0", "eval", 1,
		 * null);
		 * 
		 * JavaClass jClass = new JavaClass("rhino"); for (int i = 0; i < 10; i++) { //
		 * 評価用のスコープを用意（グローバルスコープを引数に渡してグローバルオブジェクトを共有する） Scriptable scope =
		 * context.initStandardObjects(global);
		 * 
		 * // グローバル変数を出力後、カウントアップ context.evaluateString(scope,
		 * "print('グローバル変数 gVal=' + gVal++)", "<eval>", 1, null);
		 * 
		 * // Javaのクラスを使用するfunctionを定義 context.evaluateString(scope,
		 * "function sum() { return JavaFunctions.sum(Array.prototype.slice.call(arguments)); }"
		 * , "<eval>", 1, null);
		 * 
		 * // 内部でJavaのクラスを使用 context.evaluateString(scope, "print(sum(1,5));", "<eval>",
		 * 1, null);
		 * 
		 * // Javaから受け渡されたデータ、オブジェクトを使用 ScriptableObject.putProperty(scope, "count", i *
		 * 10); ScriptableObject.putProperty(scope, "str", "creator");
		 * ScriptableObject.putProperty(scope, "jClass", jClass); //
		 * 受け渡されたデータをJavaのクラスに設定 context.evaluateString(scope, "jClass.setCount(count)",
		 * "<eval>", 1, null); // 1.7.13 ではテンプレートリテラルの変数が展開されない //
		 * context.evaluateString(scope, "jClass.println(`str=${str}`)", "<eval>", 1, //
		 * null); context.evaluateString(scope, "jClass.println('str=' + str)",
		 * "<eval>", 1, null);
		 * 
		 * // ロードしたJSファイルの無名関数を使用 context.evaluateString(scope,
		 * "print(joiner('Doc', 'Creator', 4))", "<eval>", 1, null);
		 * 
		 * // 事前にコンパイルしたスクリプトを実行 compiledScript.exec(context, scope); }
		 * 
		 * // } finally { // Context.exit(); // }
		 * 
		 */
	}

	@Test
	void testPlus() {
	}

}
