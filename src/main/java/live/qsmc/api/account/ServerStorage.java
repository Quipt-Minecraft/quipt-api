package live.qsmc.api.account;

import live.qsmc.quipt.core.QuiptIntegration;
import live.qsmc.quipt.core.config.Config;
import live.qsmc.quipt.core.config.ConfigTemplate;
import live.qsmc.quipt.core.config.ConfigValue;
import org.json.JSONArray;

import java.io.File;

@ConfigTemplate(name = "server_storage", ext = ConfigTemplate.Extension.JSON)
public class ServerStorage extends Config {

    /** Epoch millis of the very first server boot. Persists across restarts. */
    @ConfigValue
    public long firstStartMs;

    @ConfigValue
    public JSONArray logs = new JSONArray();

    @ConfigValue
    public JSONArray previousVersions = new JSONArray();

    public ServerStorage(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }
}
