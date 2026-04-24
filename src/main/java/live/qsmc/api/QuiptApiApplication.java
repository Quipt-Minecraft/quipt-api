package live.qsmc.api;

import live.qsmc.core2.Quipt;
import live.qsmc.core2.QuiptIntegration;
import live.qsmc.core2.utils.net.HttpConfig;
import live.qsmc.core2.utils.net.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@SpringBootApplication
public class QuiptApiApplication extends QuiptIntegration {

    private static final String VERSION_FILE_NAME = "version.txt";
    private static final String ARTIFACT_PREFIX = "quipt-api-";
    private static final String ARTIFACT_SUFFIX = ".jar";

    private static QuiptIntegration quipt;

    public static void main(String[] args)
    {
        //Enable Quipt API integration
        quipt = new QuiptApiApplication();
        Quipt.INSTANCE.enable(quipt);

        //Check for updates
        Properties properties = new Properties();
        try (var resourceStream = QuiptApiApplication.class.getResourceAsStream("/application.properties")) {
            if (resourceStream == null) {
                quipt.logger().error("Update Checker", "Failed to load application.properties");
                return;
            }
            properties.load(resourceStream);
        } catch (IOException e) {
            quipt.logger().error("Update Checker", "Failed to load application.properties");
            return;
        }
        String storedVersion = properties.getProperty("version");
        String onlineVersion = storedVersion;
        syncVersionFile(storedVersion);
        cleanupHomeJars(storedVersion);


        HttpResponse<String> responseRaw = NetworkUtils.get(HttpConfig.DEFAULTS, "https://ci.qsmc.live/job/QuiptApi/lastSuccessfulBuild/api/json?pretty=true&tree=artifacts[*]");
        JSONObject response = new JSONObject(responseRaw.body());
        JSONArray artifacts = response.getJSONArray("artifacts");
        boolean versionedArtifactFound = false;
        for(Object raw : artifacts) {
            if(raw instanceof JSONObject artifact) {
                String displayPath = artifact.optString("displayPath", "");
                if(!displayPath.startsWith(ARTIFACT_PREFIX) || !displayPath.endsWith(ARTIFACT_SUFFIX) || displayPath.endsWith("-plain.jar")) continue;
                onlineVersion = displayPath.substring(ARTIFACT_PREFIX.length(), displayPath.length() - ARTIFACT_SUFFIX.length());
                versionedArtifactFound = true;
                break;

            }
        }
        if(!versionedArtifactFound) quipt.logger().log("Update Checker", "No versioned boot artifact found in Jenkins response; continuing with current version " + storedVersion);
        if(onlineVersion.equalsIgnoreCase(storedVersion)) quipt.logger().log("Update Checker", "Quipt API is up to date!");
        else{
            quipt.logger().log("Update Checker", "Quipt API is outdated! Current: " + storedVersion + ", Online: " + onlineVersion);
            Path target = new File(ARTIFACT_PREFIX + onlineVersion + ARTIFACT_SUFFIX).toPath();
            NetworkUtils.get(HttpConfig.DEFAULTS, "https://ci.qsmc.live/job/QuiptApi/lastSuccessfulBuild/artifact/build/libs/" + ARTIFACT_PREFIX + onlineVersion + ARTIFACT_SUFFIX, HttpResponse.BodyHandlers.ofFile(target));
            syncVersionFile(onlineVersion);
            System.exit(0);
        }




        SpringApplication.run(QuiptApiApplication.class, args);

    }

    public static QuiptIntegration get() {
        return quipt;
    }

    private static void syncVersionFile(String version) {
        try {
            Files.writeString(Path.of(VERSION_FILE_NAME), version);
        } catch (IOException e) {
            quipt.logger().log("Update Checker", "Failed to write version file: " + e.getMessage());
        }
    }

    private static void cleanupHomeJars(String version) {
        if (version == null || version.isBlank()) {
            quipt.logger().log("Startup Cleanup", "Skipping jar cleanup because the current version is missing.");
            return;
        }

        Path homeDirectory = Path.of(System.getProperty("user.home"));
        try (DirectoryStream<Path> jarFiles = Files.newDirectoryStream(homeDirectory, "*.jar")) {
            for (Path jarFile : jarFiles) {
                if (shouldDeleteJarFile(jarFile, version)) {
                    Files.deleteIfExists(jarFile);
                    quipt.logger().log("Startup Cleanup", "Deleted old jar: " + jarFile.getFileName());
                }
            }
        } catch (IOException e) {
            quipt.logger().log("Startup Cleanup", "Failed to clean home directory jars: " + e.getMessage());
        }
    }

    static boolean shouldDeleteJarFile(Path jarFile, String version) {
        return Files.isRegularFile(jarFile) && !jarFile.getFileName().toString().equals(versionedArtifactName(version));
    }

    static String versionedArtifactName(String version) {
        return ARTIFACT_PREFIX + version + ARTIFACT_SUFFIX;
    }

    @Override
    public String name() {
        return "API";
    }

    @Override
    public String version() {
        return "1";
    }

    @Override
    public File folder() {
        return new File("quipt/api");
    }

    @Override
    public void enable() {
    }
}
