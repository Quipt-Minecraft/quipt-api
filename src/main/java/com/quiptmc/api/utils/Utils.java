package com.quiptmc.api.utils;

import com.quiptmc.api.config.ApiConfig;
import com.quiptmc.api.managers.ServerManager;
import com.quiptmc.api.managers.UptimeManager;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.ConfigManager;

public class Utils {

    public final ApiConfig config;
    public final ServerManager serverManager;
    public final UptimeManager uptimeManager;

    public Utils(QuiptIntegration integration) {
        config = ConfigManager.registerConfig(integration, ApiConfig.class);
        serverManager = new ServerManager(integration);
        uptimeManager = new UptimeManager(integration);
        integration.enable();
    }


}
