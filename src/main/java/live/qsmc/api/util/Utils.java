package live.qsmc.api.util;

import live.qsmc.api.QuiptApiApplication;
import live.qsmc.api.account.AccountData;
import live.qsmc.api.account.AccountStorage;
import live.qsmc.api.account.AccountToken;

import java.util.Map;
import java.util.UUID;

public class Utils {

    public static ApiResponse<Object> validateAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank())
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Authorization header is required");

        String providedSecret = authorizationHeader.trim();
        if (providedSecret.equalsIgnoreCase("Bearer"))
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Authorization header must include a secret");

        if (providedSecret.regionMatches(true, 0, "Bearer ", 0, 7))
            providedSecret = providedSecret.substring(7).trim();

        if (providedSecret.isBlank())
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Authorization header must include a secret");

        QuiptApiApplication app = QuiptApiApplication.api();
        if (app == null || app.config() == null)
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Server configuration is unavailable");

        AccountToken token = app.configs().config(AccountStorage.class).token(providedSecret);
        if (token == null)
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Invalid secret");
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, token);
    }

    public static String generateToken(){
        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        String token = UUID.randomUUID().toString().replace("-", "");
        while (storage.token(token) != null) {
            token = UUID.randomUUID().toString().replace("-", "");
        }
        return token;
    }
}
