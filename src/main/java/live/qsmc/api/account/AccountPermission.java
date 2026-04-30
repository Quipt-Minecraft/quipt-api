package live.qsmc.api.account;

import live.qsmc.core2.QuiptIntegration;
import live.qsmc.core2.config.objects.ConfigString;
import live.qsmc.core2.data.JsonSerializable;
import org.json.JSONArray;
import org.json.JSONObject;

public class AccountPermission implements JsonSerializable {

    String id;

    public AccountPermission(String id) {
        this.id = id;
    }

}
