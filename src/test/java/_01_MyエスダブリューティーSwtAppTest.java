import static org.junit.jupiter.api.Assertions.*;

import swt.test.MySwtApp;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class _01_MyエスダブリューティーSwtAppTest {

	@SuppressWarnings("static-access")
	@Test
	void test() throws Exception {
		new ディレクトリ取得().main(new String[] {});
		new MySwtApp().main(new String[] {});
	}

	class ディレクトリ取得 {
		public static void main(String[] args) throws IOException {
			Path dirpath = Paths.get("C:/ProgramData/.repo");
			try (Stream<Path> stream = Files.list(dirpath)) {
				stream.forEach(p -> {
					if (Files.isDirectory(p))
						System.out.println(p.toString());
				});
			}
		}
	}
}
