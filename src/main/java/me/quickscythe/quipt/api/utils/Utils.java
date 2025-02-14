package me.quickscythe.quipt.api.utils;

import me.quickscythe.quipt.api.QuiptIntegration;
import me.quickscythe.quipt.api.config.ApiConfig;
import me.quickscythe.quipt.api.config.ConfigManager;
import org.json.JSONObject;
import org.springframework.lang.Nullable;

import java.util.Optional;

public class Utils {

    public final ApiConfig config;


    public Utils(QuiptIntegration integration) {
        config = ConfigManager.registerConfig(integration, ApiConfig.class);
        integration.enable();
    }

    public void register(String key, String token) {
        config.servers.put(key, token);
        config.save();
    }

    public Optional<String> getToken(String ip) {
        return Optional.ofNullable(config.servers.has(ip) ? config.servers.getString(ip) : null);
    }



    public void update(String ip, JSONObject data) {
        config.data.put(ip, data);
        config.save();
    }

    public JSONObject getServerData(String ip, @Nullable String secret) {
        Optional<String> token = getToken(ip);
        System.out.println("Token Present: " + token.isPresent());
        token.ifPresent(s -> System.out.println("  - Token: " + s));
        System.out.println("IP: " + ip);
        System.out.println("Secret: " + secret);
        if(!config.data.has(ip)) return new JSONObject();
        JSONObject response = new JSONObject(config.data.getJSONObject(ip).toString());
        if (secret == null || (token.isPresent() && !token.get().equals(secret))) response.remove("player_stats");
        return response;
    }
}
