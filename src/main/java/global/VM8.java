package global;

import com.oracle.truffle.js.runtime.JSContextOptions;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Date;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.json.JSONArray;
import org.json.JSONObject;

public class VM8 implements Closeable {

	public GraalJSScriptEngine engine;

	public VM8() {
		this.engine = GraalJSScriptEngine.create(
				Engine.newBuilder().option("engine.WarnInterpreterOnly", "false").build(),
				Context.newBuilder("js").allowHostAccess(HostAccess.ALL).allowHostClassLookup(className -> true)
						.allowAllAccess(true).option(JSContextOptions.ECMASCRIPT_VERSION_NAME, "2022"));
		this.setGlobal("__vm__", this);
		try {
			this.js("""
					globalThis.print = function(x, title) {
					  __vm__.print(x, title===undefined?null:title);
					}
					""");
			this.js("""
					globalThis.load = function(path) {
					  return __vm__.load(path);
					}
					""");
			this.js("""
					globalThis.readAsText = function(path) {
					  return __vm__.readAsText(path);
					}
					""");
			this.js("""
					globalThis.readAsJson = function(path) {
					  return __vm__.readAsJson(path);
					}
					""");
			this.js("""
					globalThis.verify = function(x) {
					  if (!x) throw Error("Verification failed.");
					}
					""");
			this.js("""
					globalThis.__typeof__ = function(x) {
					  if (x === null) return "null";
					  if (x instanceof Array) return "array";
					  if (x instanceof Date) return "date";
					  return (typeof x);
					}
					""");
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		engine.close();
	}

	public Object setGlobal(String name, Object x) {
		Object n = toNative(x);
		engine.put(name, n);
		return n;
	}

	// public void deleteGlobal(String name) throws ScriptException {
	// engine.eval("delete " + name);
	// }

	@SuppressWarnings("unchecked")
	public AbstractList<Object> newArray(Object... args) {
		try {
			AbstractList<Object> result = (AbstractList<Object>) this.js("[]");
			for (int i = 0; i < args.length; i++) {
				result.add(toNative(args[i]));
			}
			return result;
		} catch (ScriptException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public AbstractMap<String, Object> newObject(Object... args) {
		try {
			AbstractMap<String, Object> result = (AbstractMap<String, Object>) this.js("({})");
			for (int i = 0; i < args.length; i += 2) {
				result.put((String) args[i], toNative(args[i + 1]));
			}
			return result;
		} catch (ScriptException e) {
			return null;
		}
	}

	public String typeof(Object x) {
		return (String) __js__("__typeof__($0)", x);
	}

	public boolean typeis(Object x, String type) {
		return typeof(x).equals(type);
	}

	public Object newDate() {
		try {
			return js("new Date()");
		} catch (ScriptException e) {
			return null;
		}
	}

	public Object newDate(String x) {
		try {
			var result = newDate();
			var ts = js("Date.parse($0)", x);
			js("$0.setTime($1)", result, ts);
			return result;
		} catch (ScriptException e) {
			return null;
		}
	}

	public Object newDate(Date x) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		return newDate(sdf.format(x));
	}

	@SuppressWarnings("unchecked")
	public AbstractList<Object> asArray(Object x) {
		return (AbstractList<Object>) x;
	}

	@SuppressWarnings("unchecked")
	public AbstractMap<String, Object> asObject(Object x) {
		return (AbstractMap<String, Object>) x;
	}

	public Date asDate(Object x) throws ParseException {
		verify("$0 instanceof Date", x);
		verify("(typeof $0) === 'object'", x);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		Date date = df.parse(x.toString());
		return date;
	}

	private Object run(String script, Object[] args) throws ScriptException {
		for (int i = 0; i < args.length; i++) {
			this.setGlobal("$" + i, toNative(args[i]));
		}
		try {
			return engine.eval(script);
		} finally {
			engine.eval("""
					for (let x in globalThis) {
					  //console.log("<"+x+">");
					  if (x.startsWith("$")) {
					    //console.log("deleting <"+x+">");
					    delete globalThis[x];
					  }
					}
					""");
		}
	}

	public Object js(String script, Object... args) throws ScriptException {
		return run(script, args);
	}

	public Object __js__(String script, Object... args) {
		try {
			return run(script, args);
		} catch (ScriptException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object jsToJson(String script, Object... args) throws ScriptException {
		return toJson(run(script, args));
	}

	public Object toJson(Object x) {
		if (x == null)
			return null;
		if (typeis(x, "undefined"))
			return null;
		if (typeis(x, "date"))
			try {
				return asDate(x);
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		String className = x.getClass().getName();
		switch (className) {
		case "com.oracle.truffle.polyglot.PolyglotList": {
			@SuppressWarnings("unchecked")
			AbstractList<Object> ary = (AbstractList<Object>) x;
			JSONArray result = new JSONArray();
			for (int i = 0; i < ary.size(); i++) {
				result.put(i, toJson(ary.get(i)));
			}
			return result;
		}
		case "com.oracle.truffle.polyglot.PolyglotMap": {
			@SuppressWarnings("unchecked")
			AbstractMap<String, Object> obj = (AbstractMap<String, Object>) x;
			JSONObject result = new JSONObject();
			Object[] keys = obj.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				result.put((String) keys[i], toJson(obj.get((String) keys[i])));
			}
			return result;
		}
		default: {
			return x;
		}
		}
	}

	public Object toNative(Object x) {
		if (x == null)
			return null;
		if (x instanceof java.util.Date)
			return newDate((java.util.Date) x);
		String className = x.getClass().getName();
		switch (className) {

		case "org.json.JSONArray": {
			org.json.JSONArray ary = (org.json.JSONArray) x;
			AbstractList<Object> result = newArray();
			for (int i = 0; i < ary.length(); i++) {
				result.add(toNative(ary.get(i)));
			}
			return result;
		}
		case "org.json.JSONObject": {
			org.json.JSONObject obj = (org.json.JSONObject) x;
			AbstractMap<String, Object> result = newObject();
			Object[] keys = obj.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				result.put((String) keys[i], toNative(obj.get((String) keys[i])));
			}
			return result;
		}
		default: {
			return x;
		}
		}
	}

	public void print(Object x, String title) {
		if (title != null) {
			System.out.print(title);
			System.out.print(": ");
		}
		if (x instanceof String) {
			System.out.println(x);
		} else {
			String json = null;
			json = stringify(x, 2); // (String) js("JSON.stringify($0, null, 2)", x);
			System.out.println(json);
		}
	}

	public void print(Object x) {
		print(x, null);
	}

	public Object load(String path) throws Exception {
		return js(readAsText(path));
	}

	public String readAsText(String path) throws Exception {
		if (path.startsWith(":/")) {
			return ResourceUtil.GetString(path.substring(2));
		} else if (path.startsWith("http:") || path.startsWith("https:")) {
			try (InputStream in = new URL(path).openStream()) {
				return IOUtils.toString(in);
			}
		} else {
			return FileUtils.readFileToString(new File(path));
		}
	}

	public Object readAsJson(String path) throws ScriptException, Exception {
		return parse(readAsText(path));
	}

	public String stringify(Object x, int indent) {
		return (String) __js__("JSON.stringify($0, null, $1)", x, indent);
	}

	public String stringify(Object x) {
		return (String) __js__("JSON.stringify($0)", x);
	}

	public Object parse(String json) throws ScriptException {
		return js("JSON.parse($0)", json);
	}

	public void verify(String script, Object... args) {
		Object result = null;
		try {
			result = run(script, args);
		} catch (ScriptException e) {
			e.printStackTrace();
			org.junit.jupiter.api.Assertions.fail();
		}
		if (result == null)
			org.junit.jupiter.api.Assertions.fail();
		if (!(result instanceof java.lang.Boolean))
			org.junit.jupiter.api.Assertions.fail();
		org.junit.jupiter.api.Assertions.assertTrue((boolean) result);
	}

	class Printer {
		public void print(Object x) {
			System.out.println(x);
		}
	}

}
