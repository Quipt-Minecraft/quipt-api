package live.qsmc.api;

import live.qsmc.api.util.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
    public Map<String, Object> upload(@RequestHeader(value = "Authorization") String authorizationHeader,
                                      @RequestParam(name = "path", required = false) String path,
                                      @RequestParam("file") MultipartFile file) {
        if (path == null) path = "";
        Map<String, Object> passwordValidation = Utils.validateAuthorizationHeader(authorizationHeader);
        if(!passwordValidation.isEmpty()) return passwordValidation;
        if (file.isEmpty()) return Map.of("error", "File is required");

        try {
            Path target = saveFile(path, file);
            return Map.of("message", "File uploaded successfully", "file", target.getFileName().toString(), "path", target.toString());
        } catch (IllegalArgumentException e) {
            return Map.of("error", e.getMessage());
        } catch (IOException e) {
            return Map.of("error", "Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> uploadMultiple(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                              @RequestParam(name = "path", required = false) String path,
                                              @RequestParam("files") MultipartFile[] files) {
        if (path == null) path = "";
        Map<String, Object> passwordValidation = Utils.validateAuthorizationHeader(authorizationHeader);
        if(!passwordValidation.isEmpty()) return passwordValidation;
        if (files == null || files.length == 0) return Map.of("error", "At least one file is required");

        List<String> uploaded = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                failed.add("(empty file)");
                continue;
            }

            try {
                uploaded.add(saveFile(path, file).getFileName().toString());
            } catch (IllegalArgumentException | IOException e) {
                failed.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        return Map.of("message", "Upload completed", "uploaded", uploaded, "failed", failed);
    }

    @GetMapping("/download/**")
    public ResponseEntity<?> download(HttpServletRequest request) {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String relativePath = new AntPathMatcher().extractPathWithinPattern(pattern, fullPath);

        if (relativePath == null || relativePath.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File path is required"));
        }

        try {
            Path uploadDirectory = resolveUploadDirectory();
            Path target = uploadDirectory.resolve(relativePath).normalize();
            if (!target.startsWith(uploadDirectory)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file path"));
            }

            if (!Files.exists(target) || !Files.isRegularFile(target)) {
                return ResponseEntity.status(404).body(Map.of("error", "File not found"));
            }

            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(404).body(Map.of("error", "File not found"));
            }

            String contentType = Files.probeContentType(target);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + target.getFileName().toString().replace("\"", "") + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to read file: " + e.getMessage()));
        }
    }

    private static Path saveFile(String path, MultipartFile file) throws IOException {
        String filename = sanitizeFilename(file.getOriginalFilename());
        File containerFolder = new File(QuiptApiApplication.api().folder(), "uploads/" + path);
        containerFolder.mkdirs();
//        Path uploadDirectory = resolveUploadDirectory();

//        Path target = uploadDirectory.resolve(path + (path.startsWith("/") ? "" : "/") + filename).normalize();
//        if (!target.startsWith(uploadDirectory)) throw new IllegalArgumentException("Invalid file path");
        Path target = containerFolder.toPath().resolve(filename);
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
        if (filename.isBlank()) throw new IllegalArgumentException("File name is invalid");
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
