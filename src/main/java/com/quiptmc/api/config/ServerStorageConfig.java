package com.quiptmc.api.config;

import com.quiptmc.api.feedback.Feedback;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Optional;

@ConfigTemplate(name = "server-storage")
public class ServerStorageConfig extends Config {

    @ConfigValue
    public JSONObject servers = new JSONObject();

    @ConfigValue
    public JSONArray requests = new JSONArray();


    public ServerStorageConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }

//    public MinecraftServer createServer(String ip, String secret) {
//        JSONObject server = new JSONObject();
//        server.put("ip", ip);
//        server.put("secret", secret);
//        server.put("updated", System.currentTimeMillis());
//        //todo add some information about how to reach the server with webhook requests
//        servers.put(ip, server);
//        reportRequest(ip, RequestType.REGISTER, Feedback.Result.SUCCESS);
//        save();
//        //todo remove all api data from MinecraftServer
//        return new MinecraftServer(ip, server);
//    }

    private void reportRequest(String ip, RequestType type, Feedback.Result result) {
        JSONObject request = new JSONObject();
        request.put("ip", ip);
        request.put("time", System.currentTimeMillis());
        request.put("type", type.name());
        request.put("result", result.name());
        requests.put(request);
    }

    public Feedback.Result updateServer(String ip, JSONObject data) {
        if (!servers.has(ip)) {
            reportRequest(ip, RequestType.UPDATE, Feedback.Result.FAILURE);
            throw new IllegalArgumentException("Server does not exist");
        }
        JSONObject server = servers.getJSONObject(ip);
        server.put("updated", System.currentTimeMillis());
        data.keySet().forEach(key -> server.put(key, data.get(key)));
        reportRequest(ip, RequestType.UPDATE, Feedback.Result.SUCCESS);
        save();
        return Feedback.Result.SUCCESS;
    }

    public JSONObject data(String ip, String secret) {
        reportRequest(ip, RequestType.REQUEST, Feedback.Result.SUCCESS);
        save();
        Optional<String> token = secret(ip);
        if (!servers.has(ip)) return new JSONObject();
        JSONObject response = new JSONObject(servers.getJSONObject(ip).toString());
        if (secret == null || (token.isPresent() && !token.get().equals(secret))) response.remove("player_stats");
        return response;
    }

    public Optional<String> secret(String ip) {
        if (servers.has(ip)) {
            JSONObject server = servers.getJSONObject(ip);
            return Optional.ofNullable(server.optString("secret", null));
        }
        return Optional.empty();
    }

    public enum RequestType {
        REGISTER,
        UPDATE,
        REQUEST;
    }
}
