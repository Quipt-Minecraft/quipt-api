package com.quiptmc.api.config;

import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;

import java.io.File;

@ConfigTemplate(name = "uptime-storage")
public class UptimeStorageConfig extends Config {

    @ConfigValue
    public long startUptimeChecker = 0;

    @ConfigValue
    public long totalDowntimeMillis = 0;

    @ConfigValue
    public long lastCheckTime = 0;

    public UptimeStorageConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }
}
