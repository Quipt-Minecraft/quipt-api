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
import java.util.Properties;

@SpringBootApplication
public class QuiptApiApplication extends QuiptIntegration {

    private static QuiptIntegration quipt;

    public static void main(String[] args)
    {
        //Enable Quipt API integration
        quipt = new QuiptApiApplication();
        Quipt.INSTANCE.enable(quipt);

        //Check for updates
        Properties properties = new Properties();
        try {
            properties.load(QuiptApiApplication.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            quipt.logger().error("Update Checker", "Failed to load application.properties");
            return;
        }
        String storedVersion = properties.getProperty("version");

        HttpResponse<String> responseRaw = NetworkUtils.get(HttpConfig.DEFAULTS, "https://ci.qsmc.live/job/QuiptApi/lastSuccessfulBuild/api/json?pretty=true&tree=artifacts[*]");
        JSONObject response = new JSONObject(responseRaw.body());
        JSONArray artifacts = response.getJSONArray("artifacts");
        for(Object raw : artifacts) {
            if(raw instanceof JSONObject artifact) {
                String displayPath = artifact.getString("displayPath");
                if(displayPath.endsWith("-plain.jar")) continue;
                String onlineVersion = displayPath.substring("quipt-api-".length(), displayPath.length() - 4);
                if(onlineVersion.equalsIgnoreCase(storedVersion)) quipt.logger().log("Update Checker", "Quipt API is up to date!");
                else quipt.logger().log("Update Checker", "Quipt API is outdated! Current: " + storedVersion + ", Online: " + onlineVersion);
            }
        }

        SpringApplication.run(QuiptApiApplication.class, args);

    }

    public static QuiptIntegration get() {
        return quipt;
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
