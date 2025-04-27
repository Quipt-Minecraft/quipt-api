package com.quiptmc.api.utils;

import com.quiptmc.api.config.ApiConfig;
import com.quiptmc.api.managers.Servers;
import com.quiptmc.api.managers.Sessions;
import com.quiptmc.api.managers.Uptime;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.ConfigManager;

public class Utils {

    public final ApiConfig config;
    public final Servers servers;
    public final Uptime uptime;
    public final Sessions sessions;

    public Utils(QuiptIntegration integration) {
        config = ConfigManager.registerConfig(integration, ApiConfig.class);
        servers = new Servers(integration);
        uptime = new Uptime(integration);
        sessions = new Sessions(integration);
        integration.enable();
    }


}
