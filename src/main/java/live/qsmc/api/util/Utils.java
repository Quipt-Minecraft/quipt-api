package live.qsmc.api.util;

import live.qsmc.api.QuiptApiApplication;
import live.qsmc.api.account.AccountStorage;
import live.qsmc.api.account.AccountToken;

import java.util.Map;

public class Utils {

    public static Map<String, Object> validateAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank())
            return Map.of("error", "Authorization header is required");

        String providedSecret = authorizationHeader.trim();
        if (providedSecret.equalsIgnoreCase("Bearer"))
            return Map.of("error", "Authorization header must include a secret");

        if (providedSecret.regionMatches(true, 0, "Bearer ", 0, 7))
            providedSecret = providedSecret.substring(7).trim();

        if (providedSecret.isBlank())
            return Map.of("error", "Authorization header must include a secret");

        QuiptApiApplication app = QuiptApiApplication.api();
        if (app == null || app.config() == null)
            return Map.of("error", "Server configuration is unavailable");

        AccountToken token = app.configs().config(AccountStorage.class).token(providedSecret);
        if (token == null)
            return Map.of("error", "Invalid secret");
        return Map.of("success", token);
    }
}
