package me.quickscythe.quipt.api.config;

import me.quickscythe.quipt.api.QuiptIntegration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import static me.quickscythe.quipt.api.config.ConfigTemplate.Extension.JSON;

@ConfigTemplate(name = "config", ext = JSON)
public class ApiConfig extends Config {

    @ConfigValue
    public JSONObject servers = new JSONObject();

    @ConfigValue
    public JSONObject data = new JSONObject();

    @ConfigValue
    public String fallbackApi = "https://api.mcsrvstat.us/3/%ip%";

    public ApiConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }
}
