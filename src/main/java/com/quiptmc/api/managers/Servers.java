package com.quiptmc.api.managers;

import com.quiptmc.api.config.ServerStorageConfig;
import com.quiptmc.api.feedback.Feedback;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.utils.NetworkUtils;
import org.json.JSONObject;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Servers {

    private final ServerStorageConfig config;

    public Servers(QuiptIntegration integration){
        config = ConfigManager.registerConfig(integration, ServerStorageConfig.class);
    }

    public Feedback.Result register(String ip, String secret) {
        if (!config.servers.has(ip)) {
//            config.createServer(ip, secret);
            return Feedback.Result.SUCCESS;
        }
        return Feedback.Result.NO_ACTION;
    }

    private Optional<String> getSecret(String ip) {
        return config.secret(ip);
    }


    public Feedback.Result update(String ip, JSONObject data) {
        if (!config.servers.has(ip)) return Feedback.Result.NO_ACTION;
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

    public void gc() {
        List<String> toRemove = new ArrayList<>();
        for(String key :config.servers.keySet()){
            JSONObject server = config.servers.getJSONObject(key);
            //todo change this time value to 3 months
            if(server.getLong("updated") + TimeUnit.MICROSECONDS.convert(92, TimeUnit.DAYS) < System.currentTimeMillis()){
                toRemove.add(key);
            }
        }
        for(String key : toRemove){
            config.servers.remove(key);
        }
        config.save();
    }

    public Feedback.Result action(String ip, JSONObject json) {
        String callbackUrl = json.getJSONObject("secret").getString("callback_url");
        NetworkUtils.post(callbackUrl + "/action", json);
        return Feedback.Result.SUCCESS;
    }
}
