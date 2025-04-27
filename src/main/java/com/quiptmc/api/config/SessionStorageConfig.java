package com.quiptmc.api.config;

import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;
import org.json.JSONObject;

import java.io.File;

@ConfigTemplate(name = "sessions")
public class SessionStorageConfig  extends Config {

    @ConfigValue
    public JSONObject sessions = new JSONObject();

    /**
     * Creates a new config file
     *
     * @param file        The file to save to
     * @param name        The name of the config
     * @param extension   The extension of the config
     * @param integration The plugin that owns this config
     */
    public SessionStorageConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }
}
