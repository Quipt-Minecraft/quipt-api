package live.qsmc.api.account;

import live.qsmc.quipt.core.data.JsonSerializable;
import org.json.JSONArray;

import java.util.concurrent.TimeUnit;

public class Token implements JsonSerializable {

    public String id;
    public String description;
    public JSONArray permissionsArray = new JSONArray();
    public long created;
    public long expires;

    public Token(String token, String description) {
        this.id = token;
        this.description = description;
        this.created = System.currentTimeMillis();
        this.expires = created + TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS);
    }

    public Token(String token, String description, long created, long expires) {
        this.id = token;
        this.description = description;
        this.created = created;
        this.expires = expires;
    }


}
