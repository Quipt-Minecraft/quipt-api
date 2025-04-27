package com.quiptmc.api.components.controllers.handler;

import org.json.JSONObject;

import java.util.Optional;

public class SecretHandler {

    protected Optional<String> extractSecret(JSONObject json) {
        if(!json.has("secrets") && !json.has("secret")) return Optional.empty();
        if(json.get("secrets") instanceof JSONObject){
            String secret = json.getJSONObject("secrets").optString("secret", null);
            json.remove("secrets");
            return Optional.ofNullable(secret);
        }
        String secret = json.optString("secret", null);
        json.remove("secret");
        return Optional.ofNullable(secret);
    }
}
