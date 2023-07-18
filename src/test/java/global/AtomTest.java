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

class AtomTest {

	@Test
	void test() throws Exception {

		VM8 vm = new VM8();

		var build_cmd = vm.readAsText("https://raw.githubusercontent.com/atom/atom/master/script/build.cmd");
		vm.print(build_cmd, "build_cmd");
		build_cmd = (String)vm.js("readAsText('https://raw.githubusercontent.com/atom/atom/master/script/build.cmd')");
		vm.print(build_cmd, "build_cmd");

		var package_json = vm.readAsJson("https://raw.githubusercontent.com/atom/atom/master/package.json");
		vm.print(package_json, "package_json");
		package_json = vm.js("readAsJson('https://raw.githubusercontent.com/atom/atom/master/package.json')");
		vm.print(package_json, "package_json");
	}

}
