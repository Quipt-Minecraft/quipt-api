package live.qsmc.api.account;

import live.qsmc.quipt.core.QuiptIntegration;
import live.qsmc.quipt.core.config.objects.ConfigObject;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccountData extends ConfigObject {

    public String email;
    public String password;

    JSONObject tokenStorage = new JSONObject();
    JSONObject permissionsStorage = new JSONObject();

    private final Map<String, Token> tokensCache = new HashMap<>();
    private final Map<String, Permission> permissionsCache = new HashMap<>();


    public AccountData(QuiptIntegration integration) {
        super(integration);
    }

    public AccountData(QuiptIntegration integration, String username, String email, String password, String verificationToken) {
        super(integration);
        super.id = username;
        this.email = email;
        this.password = password;
        Token token = new Token(verificationToken, "Verification token");
        token.permissionsArray.put("registration");
        add(token);
    }

    public Token token(String token) {
        if (!tokensCache.containsKey(token)) {
            if (tokenStorage.has(token)) {
                JSONObject tokenJson = tokenStorage.getJSONObject(token);
                tokensCache.put(token, new Token(token, tokenJson.getString("description"), tokenJson.getLong("created"), tokenJson.getLong("expires")));
            } else return null;
        }
        return tokensCache.get(token);
    }

    public Permission permission(String permission) {
        if (!permissionsCache.containsKey(permission)) {
            if (permissionsStorage.has(permission))
                permissionsCache.put(permission, new Permission(permission));
            else return null;
        }
        return permissionsCache.get(permission);
    }

    public void bake() {
        for (Token token : tokensCache.values()) {
            tokenStorage.put(token.id, token.json());
        }
        for (Permission permission : permissionsCache.values()) {
            permissionsStorage.put(permission.id, permission.json());
        }
    }

    public void add(Permission accountPermission) {
        permissionsCache.put(accountPermission.id, accountPermission);
    }

    public void add(Token accountToken) {
        tokensCache.put(accountToken.id, accountToken);
    }

    public void remove(Token token) {
        tokensCache.remove(token.id);
        tokenStorage.remove(token.id);
    }

    public void remove(Permission permission) {
        permissionsCache.remove(permission.id);
        permissionsStorage.remove(permission.id);
    }
}
