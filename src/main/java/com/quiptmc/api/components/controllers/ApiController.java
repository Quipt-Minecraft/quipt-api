package com.quiptmc.api.components.controllers;

import com.quiptmc.api.QuiptApi;
import com.quiptmc.api.feedback.Feedback;
import com.quiptmc.core.utils.NetworkUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @PostMapping("/register/{ip}")
    public String registerServer(@PathVariable String ip, @RequestBody String data) {
        try {
            JSONObject json = new JSONObject(data);
            if (!json.has("secret")) return new Feedback(Feedback.Result.FAILURE, "Secret not provided").json();
            return new Feedback(QuiptApi.utils.serverManager.register(ip, json.getString("secret")), "Registering Server...").json();
        } catch (JSONException ex) {
            return new Feedback(Feedback.Result.FAILURE, "Invalid JSON").json();
        }
    }

    @PostMapping("/update/{ip}")
    public String updateServer(@PathVariable String ip, @RequestBody String data) {
        try {
            JSONObject json = new JSONObject(data);
            if (!json.has("secret")) return new Feedback(Feedback.Result.FAILURE, "Secret not provided").json();
            return new Feedback(QuiptApi.utils.serverManager.update(ip, json.getString("secret"), json), "Updating Server...").json();
        } catch (JSONException ex) {
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