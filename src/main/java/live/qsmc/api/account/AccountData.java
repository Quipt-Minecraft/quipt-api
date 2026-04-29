package live.qsmc.api.account;

import live.qsmc.core2.QuiptIntegration;
import live.qsmc.core2.config.objects.ConfigObject;
import live.qsmc.core2.data.JsonSerializable;
import org.json.JSONObject;

public class AccountData extends ConfigObject  {

    public String email;
    public String password;
    public String token;


    public AccountData(QuiptIntegration integration, JSONObject json) {
        super(integration, json);
    }

    public AccountData(QuiptIntegration integration, String username, String email, String password, String token) {
        super(integration);
        super.id = username;
        this.email = email;
        this.password = password;
        this.token = token;
    }

}
