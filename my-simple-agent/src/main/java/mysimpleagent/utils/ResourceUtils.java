package mysimpleagent.utils;

import mysimpleagent.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceUtils {
    public static String loadResourceAsString(String resourcePath) {
        var url = Config.class.getClassLoader().getResource(resourcePath);
        if (url == null) {
            var msg = String.format("Resource not found: %s", resourcePath);
            throw new AssertionError(msg);
        }

        try {
            URI uri = url.toURI();
            return Files.readString(Path.of(uri), StandardCharsets.UTF_8);
        } catch (URISyntaxException | IOException e) {
            throw new AssertionError(e);
        }
    }
}
