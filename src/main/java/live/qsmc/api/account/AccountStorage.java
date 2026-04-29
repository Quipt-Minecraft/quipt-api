package live.qsmc.api.account;

import live.qsmc.core2.QuiptIntegration;
import live.qsmc.core2.config.Config;
import live.qsmc.core2.config.ConfigTemplate;
import live.qsmc.core2.config.ConfigValue;
import live.qsmc.core2.config.objects.ConfigMap;

import java.io.File;

@ConfigTemplate(name = "account_storage", ext = ConfigTemplate.Extension.JSON)
public class AccountStorage extends Config {

    @ConfigValue
    public ConfigMap<AccountData> accounts;

    public AccountStorage(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
        accounts = new ConfigMap<>(integration);
    }
}
