package live.qsmc.api.account;

import live.qsmc.api.util.ApiResponse;
import live.qsmc.api.QuiptApiApplication;
import live.qsmc.api.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final VerificationEmailService verificationEmailService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokens verificationTokens = new VerificationTokens();


    public AccountController(VerificationEmailService verificationEmailService, PasswordEncoder passwordEncoder) {
        this.verificationEmailService = verificationEmailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Object> verify(@RequestParam("token") String token, @RequestParam("email") String email) {
        if (token == null || email == null || token.isBlank() || email.isBlank())
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Token and email are required");
        if (!verificationTokens.has(token)) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Invalid token");
        AccountData account = verificationTokens.account(token);
        if (account == null) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Account not found");
        if (!account.email.equals(email)) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Invalid email");

        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        storage.accounts.put(account);
        verificationTokens.remove(token);
        account.remove(account.token(token));
        storage.save();
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, "Email verified successfully");
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Object> register(@RequestBody(required = false) String body) {
        if (body == null || body.isBlank()) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Body is required in json format");
        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (Exception e) {
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Body must be in json format");
        }
        if (!json.has("username")) new ApiResponse<>(ApiResponse.Status.FAILURE, "'username' field is required");
        if (!json.has("password")) new ApiResponse<>(ApiResponse.Status.FAILURE, "'password' field is required");
        if (!json.has("email")) new ApiResponse<>(ApiResponse.Status.FAILURE, "'email' field is required");
        if(!(json.get("username") instanceof String username) || !(json.get("email") instanceof String email) || !(json.get("password") instanceof String password))
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "'username', 'email' and 'password' fields must be strings");
        email = email.toLowerCase(Locale.ROOT);
        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        for(AccountData account : storage.accounts.values()) {
            if (account.id.equals(username)) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Username is already taken");
            if (account.email.equals(email)) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Email is already in use");
        }

        String passwordHash = passwordEncoder.encode(json.getString("password"));

        String tokenId = Utils.generateToken();
        AccountData accountData = new AccountData(
            QuiptApiApplication.api(),
            username,
            email,
            passwordHash,
            tokenId
        );
        AccountToken token = new AccountToken(tokenId, "Registration token");
        verificationTokens.put(token, accountData);

        try {
            verificationEmailService.sendVerificationEmail(accountData.email, tokenId);
        } catch (MailException e) {
            QuiptApiApplication.api().logger().error("Registration", "Failed to send verification email: " + e.getMessage());
            verificationTokens.remove(token);
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Registration failed: unable to send verification email");
        }
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, "Registration successful. Please check your email to verify your account.");
    }

    @RequestMapping(value = "/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Object> edit(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody(required = false) String body) {
        ApiResponse<Object> response = Utils.validateAuthorizationHeader(authorizationHeader);
        if (!response.isSuccess()) return response;
        if(!(response.data instanceof AccountData accountData))
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Account data is not available");


        if (body == null || body.isBlank())
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Body is required in json format");
        JSONObject json;

        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Invalid JSON format in request body");
        }
        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);

        if (!json.has("action")) return new ApiResponse<>(ApiResponse.Status.FAILURE, "'action' field is required");
        String action = json.getString("action").toLowerCase(Locale.ROOT);
        switch (action) {
            case "add_permission" -> {
                if (!json.has("permission"))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'permission' field is required for add_permission action");
                if(!json.has("user"))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'user' field is required for add_permission action");
                if(!(json.getString("permission") instanceof String permission))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'permission' field must be a string for add_permission action");
                if(!(json.getString("user") instanceof String user))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'user' field must be a string for add_permission action");
                AccountData targetAccount = storage.account(user);
                if(targetAccount == null)
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "User " + user + " not found for add_permission action");
                if(accountData.permission(permission) == null)
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "You don't have this permission to give.");

                targetAccount.add(new AccountPermission(permission));
                storage.save();
            }
            case "create_token" -> {
                if (!json.has("description"))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'description' field is required for create_token action");
                if (!json.has("permissions"))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'permissions' field is required for create_token action");
                if (!(json.getString("description") instanceof String description))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'description' field must be a string for create_token action");
                if (!(json.get("permissions") instanceof JSONArray permissionsJson))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'permissions' field must be a string array for create_token action");
                JSONObject responseObject = new JSONObject();
                List<String> permissions = new ArrayList<>();

                for (int i = 0; i < permissionsJson.length(); i++) {
                    if (accountData.permission(permissionsJson.getString(i)) != null)
                        permissions.add(permissionsJson.getString(i));
                    else {
                        if(!responseObject.has("invalid_permissions")) responseObject.put("invalid_permissions", new JSONArray());
                        JSONArray invalidPermissions = responseObject.getJSONArray("invalid_permissions");
                        JSONObject invalidPermission = new JSONObject();
                        invalidPermission.put("permission", permissionsJson.getString(i));
                        invalidPermission.put("error", "You don't have this permission to give.");
                        invalidPermissions.put(invalidPermission);
                    }
                }
                if(permissions.isEmpty())
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, responseObject.put("error", "No valid permissions provided."));

                String tokenId = Utils.generateToken();
                JSONArray permissionsApplied = new JSONArray();
                responseObject.put("token", tokenId);
                responseObject.put("description", description);

                AccountToken token = new AccountToken(tokenId, json.getString("description"));
                for (String permission : permissions) {
                    token.permissionsArray.put(permission);
                    permissionsApplied.put(permission);
                }
                responseObject.put("permissions_applied", permissionsApplied);

                accountData.add(token);
                storage.save();
                return new ApiResponse<>(ApiResponse.Status.SUCCESS, responseObject.put("raw_token", token.json()));

            }
            default -> {
                return new ApiResponse<>(ApiResponse.Status.FAILURE, "Unsupported action: " + action);
            }
        }
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, "Edit successful");
    }

    private static class VerificationTokens {
        private final Map<AccountToken, AccountData> verificationTokens = new HashMap<>();

        public void put(AccountToken token, AccountData account) {
            verificationTokens.put(token, account);
        }

        public AccountData account(String token) {
            for (AccountToken t : verificationTokens.keySet()) {
                if (t.id.equals(token)) return verificationTokens.get(t);
            }
            return null;
        }

        public AccountToken token(String token){
            for (AccountToken t : verificationTokens.keySet()) {
                if (t.id.equals(token)) return t;
            }
            return null;
        }

        public boolean has(String token) {
            return token(token) != null;
        }

        public void remove(AccountToken token) {
            verificationTokens.remove(token);
        }

        public void remove(String token) {
            remove(token(token));
        }
    }

}
