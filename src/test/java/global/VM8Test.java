package global;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.util.Date;

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
		// vm.load(":/json.js");
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
		// vm.load(":/run.js");
		// vm.load(":/error.js");
		Object dt = vm.load(":/date.js");
		vm.print(dt.getClass().getName(), "dt.getClass().getName()");
		System.out.println(dt.toString());
		var keys = vm.asObject(dt).keySet().toArray();
		//for (int i = 0; i < keys.length; i++) {
		//	System.out.println("[" + keys[i] + "]");
		//}
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        Date date = df.parse(dt.toString());
        System.out.println(date);

		vm.js("console.log(JSON.stringify(new Date()))");
		vm.load(":/class.js");
		// com.oracle.truffle.polyglot.PolyglotMap
		vm.print(vm.newDate(), "vm.newDate()");
		vm.print(vm.newDate("2023-07-17T17:13:12.577Z"), "vm.newDate(\"2023-07-17T17:13:12.577Z\")");
		vm.print(vm.newDate(new Date()), "vm.newDate(new Date()");
		
		for (int i=0; i<(int)vm.js("json.c.length"); i++) {
			vm.print(vm.js("json.c[$0]", i), "enum");
		}
		
		if ((boolean)vm.js("json.hasOwnProperty('a')")) {
			vm.print("has a");
		}
		
		vm.js("""
			  $xyz = 123;
			  console.log($xyz);
			  """);
		
		vm.js("dt = new Date()");
		vm.asDate(vm.js("dt"));
		
		var dtAry = vm.newArray(vm.newDate());
		var dtAryJson = vm.toJson(dtAry);
		System.out.println(dtAryJson);
		var dtAryNative = vm.toNative(dtAryJson);
		vm.print(dtAryNative);

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
