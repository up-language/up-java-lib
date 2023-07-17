package global;

import java.net.URL;
import java.nio.charset.Charset;

import com.google.common.io.Resources;

public class ResourceUtil {

	public static String GetString(URL url, Charset cs) throws Exception {
//		InputStream in = url.openStream();
//		try {
//			return IOUtils.toString(in, "UTF-8");
//		} finally {
//			IOUtils.closeQuietly(in);
//		}
		return Resources.toString(url, cs);
	}

	public static String GetString(URL url) throws Exception {
		return GetString(url, Charset.forName("UTF-8"));
	}

	public static String GetString(String name, Charset cs) throws Exception {
//		URL url = Resources.getResource(name);
//		return GetString(url);
		return Resources.toString(Resources.getResource(name), cs);
	}

	public static String GetString(String name) throws Exception {
		return GetString(name, Charset.forName("UTF-8"));
	}

	public static byte[] GetBinary(URL url) throws Exception {
//		InputStream in = url.openStream();
//		try {
//			return IOUtils.toByteArray(in);
//		} finally {
//			IOUtils.closeQuietly(in);
//		}
		return Resources.toByteArray(url);
	}

	public static byte[] GetBinary(String name) throws Exception {
//		URL url = Resources.getResource(name);
//		return GetBinary(url);
		return Resources.toByteArray(Resources.getResource(name));

	}

}
