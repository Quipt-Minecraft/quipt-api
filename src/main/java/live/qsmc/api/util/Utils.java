package live.qsmc.api.util;

import live.qsmc.api.QuiptApiApplication;

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

        if (!providedSecret.equals(app.config().secret))
            return Map.of("error", "Invalid secret");

        return Map.of();
    }
}
