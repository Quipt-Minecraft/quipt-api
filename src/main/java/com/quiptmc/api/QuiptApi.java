package com.quiptmc.api;

import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.api.utils.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableScheduling
public class QuiptApi extends SpringBootServletInitializer {



    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(QuiptApi.class);
    }

    public static final QuiptIntegration integration = new QuiptIntegration() {
        @Override
        public void enable() {
            if (!dataFolder().exists()) dataFolder().mkdirs();
        }

        @Override
        public File dataFolder() {
            return new File("api");
        }

        @Override
        public String name() {
            return "QuiptApi";
        }

        @Override
        public String version() {
            return "v1";
        }
    };
    public static final QuiptApi instance = new QuiptApi();
    public static final Utils utils = new Utils(integration);

    public static void main(String[] args) {
        SpringApplication.run(QuiptApi.class, args);
    }


}
