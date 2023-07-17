package global;

import static org.junit.jupiter.api.Assertions.*;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

import com.oracle.truffle.js.runtime.JSContextOptions;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;

class VM8Test {

	@Test
	void test() throws Exception {

		GraalJSScriptEngine engine = GraalJSScriptEngine
				.create(Engine.newBuilder().option("engine.WarnInterpreterOnly", "false").build(),
						Context.newBuilder("js")
								// .allowIO(false)
								.option(JSContextOptions.ECMASCRIPT_VERSION_NAME, "2022"));
		Object x778 = engine.eval("777+1");
		System.out.println(x778);
		engine.eval("console.log(777+1)");
		engine.put("graal", 1221);
		engine.eval("console.log(graal)");

		VM8 vm = new VM8();

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
		//vm.load(":/json.js");
		vm.loadFile(":/json.js");
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
		//vm.load(":/run.js");
		// vm.load(":/error.js");
		Object dt = vm.load(":/date.js");
		System.out.println(dt.getClass().getName());
		vm.js("console.log(JSON.stringify(new Date()))");
		vm.load(":/class.js");
		
		/*
		 * assertEquals("{\"a\":\"abc\",\"b\":123,\"c\":[11,22,33]}",
		 * vm.jsToJson("json").toString()); Object json = vm.js("json");
		 * assertEquals(33, vm.jsToJson("$0.c[2]", json)); assertEquals(33,
		 * vm.jsToJson("$0.c[$1]", json, 2)); vm.jsToJson("$0.c[$1]=$2", json, 2, 777);
		 * vm.js("print(JSON.stringify(json, null, 2))"); assertEquals(777,
		 * vm.js("json.c[2]")); vm.print(json, "json"); JSONArray ary = new JSONArray();
		 * ary.put(111); ary.put(222); ary.put(333); vm.print(ary); Object ref =
		 * vm.setGlobal("ary", ary); vm.print(ref); vm.js("$0[1]=777", ref);
		 * vm.print(vm.js("ary")); vm.js("console.log($0)", "this is $0");
		 * vm.load(":/run.js"); // vm.load(":/error.js"); Object dt =
		 * vm.load(":/date.js"); System.out.println(dt.getClass().getName());
		 * vm.js("console.log(JSON.stringify(new Date()))");
		 */
	}

}
