package global;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;

import java.io.File;
//import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class VM {
	public Context context;
	public Global global;

	public VM() {
		this.context = Context.enter();
		// load、importPackage、print を使うために必要
		this.global = new Global(context);
		// スコープを初期化
		ImporterTopLevel.init(context, global, true);
		this.setGlobal("$vm", this);
		this.js("globalThis.print = function(x, title) { $vm.print(x, title===undefined?null:title); }");
		this.js("globalThis.console = { log: globalThis.print }");
		this.js("globalThis.load = function(path) { return $vm.loadFile(path); }");
		this.js("globalThis.js = function() { var args=['<eval>']; for (var i=0; i<arguments.length; i++) args.push(arguments[i]); return $vm.runArray(args); }");
		this.js("globalThis.jsWithPath = function() { var args=[]; for (var i=0; i<arguments.length; i++) args.push(arguments[i]); return $vm.runArray(args); }");
	}

	public Object setGlobal(String name, Object x) {
		ScriptableObject.putProperty(global, name, toNative(x));
		return ScriptableObject.getProperty(global, name);
	}

	private Object run_(String path, String script, Object[] args) {
		Scriptable scope = context.initStandardObjects(global);
		for (int i = 0; i < args.length; i++) {
			ScriptableObject.putProperty(scope, "$" + i, toNative(args[i]));
		}
		return context.evaluateString(scope, script, path, 1, null);
	}

	public Object js(String script, Object... args) {
		return run_("<eval>", script, args);
	}

	public Object jsToJson(String script, Object... args) {
		return toJson(run_("<eval>", script, args));
	}

	public Object jsWithPath(String path, String script, Object... args) {
		return run_(path, script, args);
	}

	public Object jsToJsonWithPath(String path, String script, Object... args) {
		return toJson(run_(path, script, args));
	}

	public Object runArray(Object x) {
		org.mozilla.javascript.NativeArray ary = (org.mozilla.javascript.NativeArray)x;
		String path = (String)ary.get(0);
		String script = (String)ary.get(1);
		Object[] args = new Object[ary.size()-2];
		for (int i=2; i<ary.size(); i++) {
			args[i-2] = ary.get(i);
		}
		return run_(path, script, args);
	}

	public void print(Object x, String title) {
		if (title != null) {
			System.out.print(title);
			System.out.print(": ");
		}
		// run("print(JSON.stringify($0, null, 2))", x);
		if (x instanceof String) {
			System.out.println(x);
		} else {
			String json = (String) js("JSON.stringify($0, null, 2)", x);
			System.out.println(json);
		}
	}

	public void print(Object x) {
		print(x, null);
	}

	public Object load(String x) {
		return this.js("load($0)", x);
	}

	public Object loadToJson(String x) {
		return this.jsToJson("load($0)", x);
	}

	public Object toJson(Object x) {
		if (x == null)
			return null;
		String className = x.getClass().getName();
		switch (className) {
		case "org.mozilla.javascript.NativeArray": {
			org.mozilla.javascript.NativeArray ary = (org.mozilla.javascript.NativeArray) x;
			JSONArray result = new JSONArray();
			for (int i = 0; i < ary.size(); i++) {
				result.put(i, toJson(ary.get(i)));
			}
			return result;
		}
		case "org.mozilla.javascript.NativeObject": {
			org.mozilla.javascript.NativeObject obj = (org.mozilla.javascript.NativeObject) x;
			JSONObject result = new JSONObject();
			Object[] keys = obj.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				result.put((String) keys[i], toJson(obj.get(keys[i])));
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
			NativeArray result = new NativeArray(ary.length());
			for (int i = 0; i < ary.length(); i++) {
				NativeArray.putProperty(result, i, toNative(ary.get(i)));
			}
			return result;
		}
		case "org.json.JSONObject": {
			org.json.JSONObject obj = (org.json.JSONObject) x;
			NativeObject result = new NativeObject();
			Object[] keys = obj.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				NativeObject.putProperty(result, (String) keys[i], toNative(obj.get((String) keys[i])));
			}
			return result;
		}
		default: {
			return x;
		}
		}
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
		return this.run_(path, this.getSourceCode(path), new Object[] {});
	}

}
