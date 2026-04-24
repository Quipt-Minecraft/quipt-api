package live.qsmc.api.config;

import live.qsmc.core2.QuiptIntegration;
import live.qsmc.core2.config.Config;
import live.qsmc.core2.config.ConfigTemplate;
import live.qsmc.core2.config.ConfigValue;

import java.beans.ConstructorProperties;
import java.io.File;

@ConfigTemplate(name = "config", ext = ConfigTemplate.Extension.JSON)
public class DefaultConfig extends Config {

    @ConfigValue
    public String secret = "abc123";

    public DefaultConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }
}
