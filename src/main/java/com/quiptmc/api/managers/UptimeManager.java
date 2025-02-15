package com.quiptmc.api.managers;

import com.quiptmc.api.config.UptimeStorageConfig;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.ConfigManager;

import java.util.concurrent.TimeUnit;

public class UptimeManager {

    private final UptimeStorageConfig config;

    public UptimeManager(QuiptIntegration integration) {
        this.config = ConfigManager.registerConfig(integration, UptimeStorageConfig.class);
        if(config.startUptimeChecker == 0) {
            config.startUptimeChecker = System.currentTimeMillis();
            config.save();
        }
        if(config.lastCheckTime == 0) {
            config.lastCheckTime = System.currentTimeMillis();
            config.save();
        }
    }

    public void checkUptime() {

        long now = System.currentTimeMillis();
        long timeElapsed = now - config.lastCheckTime;
        System.out.println("Time elapsed: " + timeElapsed);
//        Instant now = Instant.now();
//        Duration timeElapsed = Duration.between(Instant.ofEpochMilli(config.lastCheckTime), now);

        // Check if the time difference is larger than expected (e.g., 60 seconds)
        long delay = TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS);
        if (timeElapsed > delay) {
            long downtime = timeElapsed - delay;
            System.out.println("Downtime: " + downtime);
            config.totalDowntimeMillis += downtime;


        }
        config.lastCheckTime = now;
        config.save();
    }

    public double getUptime() {
        double downtimeMillis = config.totalDowntimeMillis;
        double now = System.currentTimeMillis();
        double start = config.startUptimeChecker;
        double percent = (1-(downtimeMillis / (now - start)))*100;
        System.out.println("Double percent: " + (1-(downtimeMillis / (now - start)))*100);
        System.out.println("Total downtime: " + config.totalDowntimeMillis);
        System.out.println("Current uptime: " + (System.currentTimeMillis() - config.startUptimeChecker));
        System.out.println("Uptime percentage: " + (1-(config.totalDowntimeMillis / (System.currentTimeMillis() - config.startUptimeChecker))));
        System.out.println("Down time percentage: " + (config.totalDowntimeMillis / (System.currentTimeMillis() - config.startUptimeChecker)));
        return (1-(downtimeMillis / (now - start))) * 100;
    }
}
