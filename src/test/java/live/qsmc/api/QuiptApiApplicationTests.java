package live.qsmc.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class QuiptApiApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void startupCleanupKeepsOnlyCurrentVersionedJar() throws IOException {
        Path tempDirectory = Files.createTempDirectory("quipt-api-cleanup-test");
        Path currentJar = Files.createFile(tempDirectory.resolve("quipt-api-0.0.1-alpha.3.jar"));
        Path oldJar = Files.createFile(tempDirectory.resolve("quipt-api-0.0.1-alpha.2.jar"));
        Path plainJar = Files.createFile(tempDirectory.resolve("quipt-api-0.0.1-alpha.3-plain.jar"));

        assertFalse(QuiptApiApplication.shouldDeleteJarFile(currentJar, "0.0.1-alpha.3"));
        assertTrue(QuiptApiApplication.shouldDeleteJarFile(oldJar, "0.0.1-alpha.3"));
        assertTrue(QuiptApiApplication.shouldDeleteJarFile(plainJar, "0.0.1-alpha.3"));
    }

}
