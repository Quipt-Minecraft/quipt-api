package live.qsmc.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
class FileController {

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> upload(
            @RequestParam("secret") String secret,
            @RequestParam("file") MultipartFile file
    ) {
        if (!hasValidSecret(secret))
            return Map.of("error", "Invalid secret");
        if (file.isEmpty())
            return Map.of("error", "File is required");

        try {
            Path target = saveFile(file);
            return Map.of(
                    "message", "File uploaded successfully",
                    "file", target.getFileName().toString(),
                    "path", target.toString()
            );
        } catch (IllegalArgumentException e) {
            return Map.of("error", e.getMessage());
        } catch (IOException e) {
            return Map.of("error", "Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> uploadMultiple(
            @RequestParam("secret") String secret,
            @RequestParam("files") MultipartFile[] files
    ) {
        if (!hasValidSecret(secret))
            return Map.of("error", "Invalid secret");
        if (files == null || files.length == 0)
            return Map.of("error", "At least one file is required");

        List<String> uploaded = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                failed.add("(empty file)");
                continue;
            }

            try {
                uploaded.add(saveFile(file).getFileName().toString());
            } catch (IllegalArgumentException | IOException e) {
                failed.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        return Map.of(
                "message", "Upload completed",
                "uploaded", uploaded,
                "failed", failed
        );
    }

    @RequestMapping("/download")
    public String download() {
        return "download";
    }

    private static boolean hasValidSecret(String secret) {
        QuiptApiApplication app = QuiptApiApplication.api();
        return app != null && secret != null && secret.equals(app.config().secret);
    }

    private static Path saveFile(MultipartFile file) throws IOException {
        String filename = sanitizeFilename(file.getOriginalFilename());
        Path uploadDirectory = resolveUploadDirectory();
        Path target = uploadDirectory.resolve(filename).normalize();
        if (!target.startsWith(uploadDirectory))
            throw new IllegalArgumentException("Invalid file path");

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    static String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank())
            throw new IllegalArgumentException("File name is missing");

        String normalized = originalFilename.replace('\\', '/').trim();
        int lastSlash = normalized.lastIndexOf('/');
        String filename = (lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized).trim();
        filename = filename.replaceAll("[^A-Za-z0-9._-]", "_");
        if (filename.isBlank())
            throw new IllegalArgumentException("File name is invalid");
        return filename;
    }

    private static Path resolveUploadDirectory() throws IOException {
        QuiptApiApplication app = QuiptApiApplication.api();
        Path baseDirectory = app != null ? app.folder().toPath() : Path.of("quipt", "api");
        Path uploadDirectory = baseDirectory.resolve("uploads").normalize();
        Files.createDirectories(uploadDirectory);
        return uploadDirectory;
    }

}
