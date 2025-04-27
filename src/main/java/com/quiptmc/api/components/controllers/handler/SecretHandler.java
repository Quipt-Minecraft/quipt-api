package com.quiptmc.api.components.controllers.handler;

import org.json.JSONObject;

import java.util.Optional;

public class SecretHandler {

    protected Optional<String> extractSecret(JSONObject json) {
        if(!json.has("secret")) return Optional.empty();
        if(json.get("secret") instanceof JSONObject) return Optional.of(json.getJSONObject("secret").optString("secret", null));
        return Optional.of(json.optString("secret", null));
    }
}
