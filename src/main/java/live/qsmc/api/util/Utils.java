package live.qsmc.api.util;

import live.qsmc.api.QuiptApiApplication;
import live.qsmc.api.account.AccountStorage;
import live.qsmc.api.account.Token;
import live.qsmc.quipt.core.utils.HashUtils;
import live.qsmc.quipt.core.utils.net.ApiResponse;

import java.util.UUID;

public class Utils {

    public static ApiResponse<?> validateAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank())
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Authorization header is required");

        String providedSecret = authorizationHeader.trim();
        if (providedSecret.equalsIgnoreCase("Bearer"))
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Authorization header must include a secret");

        if (providedSecret.regionMatches(true, 0, "Bearer ", 0, 7))
            providedSecret = providedSecret.substring(7).trim();

        providedSecret = HashUtils.sha256(providedSecret);

        if (providedSecret.isBlank())
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Authorization header must include a secret");

        QuiptApiApplication app = QuiptApiApplication.api();


        AccountStorage storage = app.configs().config(AccountStorage.class);
        if (storage == null)
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Server configuration is unavailable");

        Token token = storage.token(providedSecret);
        if (token == null)
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Invalid secret");
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, storage.account(token));
    }

    public static String generateToken() {
        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        String token = UUID.randomUUID().toString().replace("-", "");
        while (storage.token(token) != null) {
            token = UUID.randomUUID().toString().replace("-", "");
        }
        return token;
    }
}
