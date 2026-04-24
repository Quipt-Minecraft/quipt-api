package live.qsmc.api;

import live.qsmc.api.util.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void sanitizeFilenameRemovesPathAndUnsafeCharacters() {
        String sanitized = FileController.sanitizeFilename("../my report?.txt");
        assertEquals("my_report_.txt", sanitized);
    }

    @Test
    void sanitizeFilenameRejectsBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> FileController.sanitizeFilename("   "));
    }

    @Test
    void validateAuthorizationHeaderRejectsMissingHeader() {
        assertEquals(Map.of("error", "Authorization header is required"), Utils.validateAuthorizationHeader(null));
    }

    @Test
    void validateAuthorizationHeaderRejectsBlankBearerToken() {
        assertEquals(Map.of("error", "Authorization header must include a secret"), Utils.validateAuthorizationHeader("Bearer   "));
    }

    @Test
    void updateRejectsMissingAuthorizationHeader() {
        assertEquals(Map.of("error", "Authorization header is required"), new DataController().update(null));
    }

    @Test
    void uploadRejectsMissingAuthorizationHeaderBeforeProcessingFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());

        assertEquals(Map.of("error", "Authorization header is required"), new FileController().upload(null, file));
    }

}
