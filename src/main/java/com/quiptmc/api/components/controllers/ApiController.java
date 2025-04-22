package com.quiptmc.api.components.controllers;

import com.quiptmc.api.QuiptApi;
import com.quiptmc.api.feedback.Feedback;
import com.quiptmc.core.utils.NetworkUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    @PostMapping("/action/{ip}")
    public String postAction(@PathVariable String ip, @RequestBody String data) {
        try {
            JSONObject json = new JSONObject(data);
            Optional<String> secret = extractSecret(json);
            return secret.map(s -> new Feedback(QuiptApi.utils.serverManager.action(ip, json), "Sending Action...").json()).orElseGet(() -> new Feedback(Feedback.Result.FAILURE, "Secret not provided").json());
        } catch (JSONException ex) {
            return new Feedback(Feedback.Result.FAILURE, "Invalid JSON").json();
        }
    }

    @PostMapping("/register/{ip}")
    public String registerServer(@PathVariable String ip, @RequestBody String data) {
        try {
            JSONObject json = new JSONObject(data);
            Optional<String> secret = extractSecret(json);
            return secret.map(s -> new Feedback(QuiptApi.utils.serverManager.register(ip, s), "Registering Server...").json()).orElseGet(() -> new Feedback(Feedback.Result.FAILURE, "Secret not provided").json());
        } catch (JSONException ex) {
            return new Feedback(Feedback.Result.FAILURE, "Invalid JSON").json();
        }
    }

    private Optional<String> extractSecret(JSONObject json) {
        if(!json.has("secret")) return Optional.empty();
        if(json.get("secret") instanceof JSONObject) return Optional.of(json.getJSONObject("secret").optString("secret", null));
        return Optional.of(json.optString("secret", null));
    }

    @PostMapping("/update/{ip}")
    public String updateServer(@PathVariable String ip, @RequestBody String data) {
        try {
            JSONObject json = new JSONObject(data);
            Optional<String> secret = extractSecret(json);
            return secret.map(s -> new Feedback(QuiptApi.utils.serverManager.update(ip, json), "Updating Server...").json()).orElseGet(() -> new Feedback(Feedback.Result.FAILURE, "Secret not provided").json());
        } catch (JSONException ex) {
            ex.printStackTrace();
            return new Feedback(Feedback.Result.FAILURE, "Invalid JSON").json();
        }
    }

    @PostMapping("/server_status/{ip}")
    public String postServerStatus(@PathVariable String ip, @RequestBody String data) {
        try {
            JSONObject request = new JSONObject(data);
            return requestServerStatus(ip, request.has("secret") ? request.getString("secret") : null);
        } catch (JSONException e) {
            return requestServerStatus(ip, null);
        }
    }

    @GetMapping("/server_status/{ip}")
    public String getServerStatus(@PathVariable String ip) {
        return requestServerStatus(ip, null);
    }

    private String requestServerStatus(String ip, String secret) {
        JSONObject response = new JSONObject(NetworkUtils.request(QuiptApi.utils.config.fallbackApi.replaceAll("%ip%", ip)));
        JSONObject quiptData = QuiptApi.utils.serverManager.data(ip, secret);
        response.put("quipt_data", quiptData);
        return response.toString();
    }
}