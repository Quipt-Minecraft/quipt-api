package com.quiptmc.api.components.controllers;


import com.quiptmc.api.QuiptApi;
import com.quiptmc.api.components.controllers.handler.SecretHandler;
import com.quiptmc.api.feedback.Feedback;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/sessions")
public class SessionController extends SecretHandler {

    @PostMapping("/update/{uuid}")
    public String postAction(@PathVariable String uuid, @RequestBody String data) {
        try {
            JSONObject json = new JSONObject(data);
            Optional<String> secret = extractSecret(json);
            return secret.map(s -> new Feedback(QuiptApi.utils.sessions.update(uuid, json), "Sending Action...").json()).orElseGet(() -> new Feedback(Feedback.Result.FAILURE, "Secret not provided").json());
        } catch (JSONException ex) {
            return new Feedback(Feedback.Result.FAILURE, "Invalid JSON").json();
        }
    }
}
