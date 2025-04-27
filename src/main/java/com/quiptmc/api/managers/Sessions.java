package com.quiptmc.api.managers;

import com.quiptmc.api.config.SessionStorageConfig;
import com.quiptmc.api.feedback.Feedback;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.ConfigManager;
import org.json.JSONObject;

public class Sessions {

    private final SessionStorageConfig config;

    public Sessions(QuiptIntegration integration) {
        config = ConfigManager.registerConfig(integration, SessionStorageConfig.class);
    }

    public Feedback.Result update(String uuid, JSONObject json) {
        if (!config.sessions.has(uuid)) config.sessions.put(uuid, new JSONObject());
        config.sessions.getJSONObject(uuid).put(System.currentTimeMillis() + "", json);
        return Feedback.Result.SUCCESS;
    }
}
