package com.quiptmc.api.config;

import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;
import org.json.JSONObject;

import java.io.File;

import static com.quiptmc.core.config.ConfigTemplate.Extension.JSON;

@ConfigTemplate(name = "config", ext = JSON)
public class ApiConfig extends Config {



    @ConfigValue
    public String fallbackApi = "https://api.mcsrvstat.us/3/%ip%";



    public ApiConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }
}
