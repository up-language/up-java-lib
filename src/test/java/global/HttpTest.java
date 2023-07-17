package global;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

class HttpTest {

	@Test
	void test() throws IOException, InterruptedException {
		// create a client
		var httpClient = HttpClient.newHttpClient();

		// create a request

		HttpRequest request = HttpRequest
				.newBuilder(
						URI.create("https://raw.githubusercontent.com/up-language/up-language/main/om-java/json.js"))
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(response.body()); // 受信したJSON文字列を確認
	}

}
