package live.qsmc.api.account;


import live.qsmc.quipt.core.QuiptIntegration;
import live.qsmc.quipt.core.config.*;
import live.qsmc.quipt.core.config.objects.ConfigMap;

import java.io.File;

@ConfigTemplate(name = "account_storage", ext = ConfigTemplate.Extension.JSON)
public class AccountStorage extends Config {

    @ConfigValue
    public ConfigMap<AccountData> accounts;

    public AccountStorage(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
        accounts = new ConfigMap<>(integration);
    }

    @Override
    public void save() {
        for(AccountData account : accounts.values())
            account.bake();

        super.save();
    }

    public Token token(String token) {
        for(AccountData account : accounts.values()){
            if(account.tokenStorage.has(token))
                return account.token(token);
        }
        return null;
    }

    public AccountData account(String id) {
        for(AccountData account : accounts.values()){
            if(account.id.equals(id))
                return account;
        }
        return null;
    }

    public AccountData account(Token token) {
        for(AccountData account : accounts.values()){
            if(account.token(token.id) != null)
                return account;
        }
        return null;
    }
}
