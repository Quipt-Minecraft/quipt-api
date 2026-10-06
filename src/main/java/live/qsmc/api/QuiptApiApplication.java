package live.qsmc.api;

import live.qsmc.api.account.AccountData;
import live.qsmc.api.account.AccountStorage;
import live.qsmc.api.account.ServerStorage;
import live.qsmc.api.account.Token;
import live.qsmc.api.util.Utils;
import live.qsmc.quipt.core.Quipt;
import live.qsmc.quipt.core.QuiptIntegration;
import live.qsmc.quipt.core.config.factories.GenericFactory;
import live.qsmc.quipt.core.utils.HashUtils;
import live.qsmc.quipt.core.utils.net.HttpConfig;
import live.qsmc.quipt.core.utils.net.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileNotFoundException;
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

    private static QuiptApiApplication api;

    public static void main(String[] args) throws FileNotFoundException {
        //Enable Quipt API integration
        api = new QuiptApiApplication();
        Quipt.INSTANCE.enable(api);

        boolean skipUpdateCheck = false;
        for (String arg : args) {
            switch (arg) {
                case "--help", "-h", "/h", "/help" -> {
                    System.out.println("Quipt API Usage:");
                    System.out.println("  --skip-update    Skip the update check on startup");
                    System.exit(0);
                }
                case "--skip-update" -> skipUpdateCheck = true;
            }
        }

        if (!skipUpdateCheck) {
            //Check for updates
            Properties properties = new Properties();
            try (var resourceStream = QuiptApiApplication.class.getResourceAsStream("/application.properties")) {
                if (resourceStream == null) {
                    api.logger().error("Update Checker", "Failed to load application.properties");
                    return;
                }
                properties.load(resourceStream);
            } catch (IOException e) {
                api.logger().error("Update Checker", "Failed to load application.properties");
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
            for (Object raw : artifacts) {
                if (raw instanceof JSONObject artifact) {
                    String displayPath = artifact.optString("displayPath", "");
                    if (!displayPath.startsWith(ARTIFACT_PREFIX) || !displayPath.endsWith(ARTIFACT_SUFFIX) || displayPath.endsWith("-plain.jar"))
                        continue;
                    onlineVersion = displayPath.substring(ARTIFACT_PREFIX.length(), displayPath.length() - ARTIFACT_SUFFIX.length());
                    versionedArtifactFound = true;
                    break;

                }
            }
            if (!versionedArtifactFound)
                api.logger().log("Update Checker", "No versioned boot artifact found in Jenkins response; continuing with current version " + storedVersion);
            if (onlineVersion.equalsIgnoreCase(storedVersion))
                api.logger().log("Update Checker", "Quipt API is up to date!");
            else {
                api.logger().log("Update Checker", "Quipt API is outdated! Current: " + storedVersion + ", Online: " + onlineVersion);
                Path target = new File(ARTIFACT_PREFIX + onlineVersion + ARTIFACT_SUFFIX).toPath();
                NetworkUtils.get(HttpConfig.DEFAULTS, "https://ci.qsmc.live/job/QuiptApi/lastSuccessfulBuild/artifact/build/libs/" + ARTIFACT_PREFIX + onlineVersion + ARTIFACT_SUFFIX, HttpResponse.BodyHandlers.ofFile(target));
                syncVersionFile(onlineVersion);
                System.exit(0);
            }
        } else api.logger().log("Update Checker", "Skipping update check");
        api.configs().factory(new GenericFactory<>(AccountData.class));
        AccountStorage accountStorage = api.configs().register(AccountStorage.class);
        ServerStorage serverStorage = api.configs().register(ServerStorage.class);
        if (serverStorage.firstStartMs == 0) {
            serverStorage.firstStartMs = System.currentTimeMillis();
            serverStorage.save();
            api.logger().log("ServerStorage", "First boot recorded: " + serverStorage.firstStartMs);
        }

        if(accountStorage.account("admin") == null){
            String passwordHash = HashUtils.sha256("password");

            String tokenId = Utils.generateToken();
            AccountData accountData = new AccountData(
                QuiptApiApplication.api(),
                "admin",
                "admin@localhost",
                passwordHash,
                tokenId
            );
            Token token = new Token(tokenId, "Default Admin Token");
            token.permissionsArray.put("read:all");
            token.permissionsArray.put("write:all");
            token.permissionsArray.put("admin");
            token.expires = 0;
            accountData.add(token);
            accountStorage.accounts.put(accountData);
            File file = new File("defaultAdminAccount");
            try {
                api.logger().log("AccountStorage", "Created default admin account file... " + (file.createNewFile() ? "(Success)" : "(Failed)"));
                Files.writeString(file.toPath(), "Username: admin\nPassword: password\nAccess Token: " + tokenId + "\nPlease change the password immediately.");
            } catch (IOException e) {
                api.logger().error("AccountStorage", "Failed to create default admin account file: " + e.getMessage());
            }
            accountStorage.save();
        }

        SpringApplication.run(QuiptApiApplication.class, args);

    }


    public static QuiptApiApplication api() {
        return api;
    }

    private static void syncVersionFile(String version) {
        try {
            Files.writeString(Path.of(VERSION_FILE_NAME), version);
        } catch (IOException e) {
            api.logger().log("Update Checker", "Failed to write version file: " + e.getMessage());
        }
    }

    private static void cleanupHomeJars(String version) {
        if (version == null || version.isBlank()) {
            api.logger().log("Startup Cleanup", "Skipping jar cleanup because the current version is missing.");
            return;
        }

        Path homeDirectory = Path.of(System.getProperty("user.home"));
        try (DirectoryStream<Path> jarFiles = Files.newDirectoryStream(homeDirectory, "*.jar")) {
            for (Path jarFile : jarFiles) {
                if (shouldDeleteJarFile(jarFile, version)) {
                    Files.deleteIfExists(jarFile);
                    api.logger().log("Startup Cleanup", "Deleted old jar: " + jarFile.getFileName());
                }
            }
        } catch (IOException e) {
            api.logger().log("Startup Cleanup", "Failed to clean home directory jars: " + e.getMessage());
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
