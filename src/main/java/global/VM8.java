package global;

import com.oracle.truffle.js.runtime.JSContextOptions;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.AbstractList;
import java.util.AbstractMap;

import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.json.JSONArray;
import org.json.JSONObject;

public class VM8 implements Closeable {

	public GraalJSScriptEngine engine;

	public VM8() {
		this.engine = GraalJSScriptEngine.create(
				Engine.newBuilder().option("engine.WarnInterpreterOnly", "false").build(),
				Context.newBuilder("js").allowAllAccess(true)
						// .allowIO(false)
						.option(JSContextOptions.ECMASCRIPT_VERSION_NAME, "2022"));
		this.setGlobal("$vm", this);
		try {
			this.js("globalThis.print = function(x, title) { $vm.print(x, title===undefined?null:title); }");
			this.js("globalThis.load = function(path) { return $vm.load(path); }");
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

	public void deleteGlobal(String name) throws ScriptException {
		engine.eval("delete " + name);

	}

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

	@SuppressWarnings("unchecked")
	public AbstractList<Object> asArray(Object x) {
		return (AbstractList<Object>)x;
	}

	@SuppressWarnings("unchecked")
	public AbstractMap<String, Object> asObject(Object x) {
		return (AbstractMap<String, Object>)x;
	}

	private Object run(String script, Object[] args) throws ScriptException {
		for (int i = 0; i < args.length; i++) {
			this.setGlobal("$" + i, toNative(args[i]));
		}
		try {
			return engine.eval(script);
		} finally {
			for (int i = 0; i < args.length; i++) {
				this.deleteGlobal("$" + i);
			}
		}
	}

	public Object js(String script, Object... args) throws ScriptException {
		return run(script, args);
	}

	public Object jsToJson(String script, Object... args) throws ScriptException {
		return toJson(run(script, args));
	}

	public Object toJson(Object x) {
		if (x == null)
			return null;
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
			try {
				json = (String) js("JSON.stringify($0, null, 2)", x);
			} catch (ScriptException e) {
			}
			System.out.println(json);
		}
	}

	public void print(Object x) {
		print(x, null);
	}

	public Object load(String path) throws Exception {
		return loadFile(path);
	}

	public String getSourceCode(String path) throws Exception {
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

	public Object loadFile(String path) throws Exception {
		// return this.run_(path, this.getSourceCode(path), new Object[] {});
		return js(getSourceCode(path));
	}

	class Printer {
		public void print(Object x) {
			System.out.println(x);
		}
	}

}