package com.quiptmc.api.managers;

import com.quiptmc.api.config.ServerStorageConfig;
import com.quiptmc.api.feedback.Feedback;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.ConfigManager;
import org.json.JSONObject;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ServerManager {

    private final ServerStorageConfig config;

    public ServerManager(QuiptIntegration integration){
        config = ConfigManager.registerConfig(integration, ServerStorageConfig.class);
    }

    public Feedback.Result register(String ip, String secret) {
        if (!config.servers.has(ip)) {
            config.createServer(ip, secret);
            return Feedback.Result.SUCCESS;
        }
        return Feedback.Result.NO_ACTION;
    }

    private Optional<String> getSecret(String ip) {
        return config.secret(ip);
    }


    public Feedback.Result update(String ip, String secret, JSONObject data) {
        if (!config.servers.has(ip)) return Feedback.Result.NO_ACTION;
        Optional<String> token = getSecret(ip);
        assert token.isPresent();
        if (!token.get().equals(secret)) return Feedback.Result.FAILURE;
        return config.updateServer(ip, data);
    }

    public JSONObject data(String ip, @Nullable String secret) {
        return config.data(ip, secret);
    }

    public Collection<String> servers() {
        return config.servers.keySet();
    }

    public Collection<JSONObject> requests() {
        List<JSONObject> requests = new ArrayList<>();
        for(int i = 0; i < config.requests.length(); i++){
            requests.add(config.requests.getJSONObject(i));
        }
        return requests;
    }
}
