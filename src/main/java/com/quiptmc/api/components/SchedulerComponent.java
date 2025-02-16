package com.quiptmc.api.components;

import com.quiptmc.api.QuiptApi;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerComponent {

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void checkUptime() {
        QuiptApi.utils.uptimeManager.checkUptime();
        QuiptApi.utils.serverManager.gc();
    }
}
