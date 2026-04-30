package live.qsmc.api.account;

import live.qsmc.core2.QuiptIntegration;
import live.qsmc.core2.config.objects.ConfigObject;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccountData extends ConfigObject {

    public String email;
    public String password;

    JSONObject tokenStorage = new JSONObject();
    JSONObject permissionsStorage = new JSONObject();

    private final Map<String, AccountToken> tokensCache = new HashMap<>();
    private final Map<String, AccountPermission> permissionsCache = new HashMap<>();


    public AccountData(QuiptIntegration integration) {
        super(integration);
    }

    public AccountData(QuiptIntegration integration, String username, String email, String password, String verificationToken) {
        super(integration);
        super.id = username;
        this.email = email;
        this.password = password;
        AccountToken token = new AccountToken(verificationToken);
        token.permissionsArray.put("registration");
        add(token);
    }

    public AccountToken token(String token) {
        if (!tokensCache.containsKey(token)) {
            if (tokenStorage.has(token))
                tokensCache.put(token, new AccountToken(token));
            else return null;
        }
        return tokensCache.get(token);
    }

    public AccountPermission permission(String permission) {
        if (!permissionsCache.containsKey(permission)) {
            if (permissionsStorage.has(permission))
                permissionsCache.put(permission, new AccountPermission(permission, permission));
            else return null;
        }
        return permissionsCache.get(permission);
    }

    public void bake() {
        tokenStorage = new JSONObject();
        permissionsStorage = new JSONObject();
        for (AccountToken token : tokensCache.values()) {
            tokenStorage.put(token.id, token.json());
        }
        for (AccountPermission permission : permissionsCache.values()) {
            permissionsStorage.put(permission.id, permission.json());
        }
    }

    public void add(AccountPermission accountPermission) {
        permissionsCache.put(accountPermission.id, accountPermission);
    }

    public void add(AccountToken accountToken) {
        tokensCache.put(accountToken.id, accountToken);
    }
}
