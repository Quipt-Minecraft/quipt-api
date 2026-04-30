package live.qsmc.api.account;

import live.qsmc.core2.data.JsonSerializable;
import org.json.JSONArray;

public class AccountToken implements JsonSerializable {

    String id;
    JSONArray permissionsArray = new JSONArray();

    public AccountToken(String token) {
        this.id = token;
    }


}
