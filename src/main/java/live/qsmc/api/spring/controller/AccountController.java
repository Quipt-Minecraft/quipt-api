package live.qsmc.api.spring.controller;

import live.qsmc.api.account.*;
import live.qsmc.api.QuiptApiApplication;
import live.qsmc.api.spring.service.VerificationEmailService;
import live.qsmc.api.util.Utils;
import live.qsmc.quipt.core.utils.HashUtils;
import live.qsmc.quipt.core.utils.net.ApiResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    public static final String[] allowedTlds = {"com", "net", "org", "live", "io", "dev", "app"};

    private final VerificationEmailService verificationEmailService;
    private final VerificationTokens verificationTokens = new VerificationTokens();


    public AccountController(VerificationEmailService verificationEmailService) {
        this.verificationEmailService = verificationEmailService;
    }

    @GetMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> verify(@RequestParam("token") String tokenId, @RequestParam("email") String email) {
        if (tokenId == null || email == null || tokenId.isBlank() || email.isBlank())
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Token and email are required");
        String tokenHash = HashUtils.sha256(tokenId);
        if (!verificationTokens.has(tokenHash)) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Invalid token");
        Token token = verificationTokens.token(tokenHash);
        if (token == null) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Invalid token");
        AccountData account = verificationTokens.account(tokenHash);
        if (account == null) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Account not found");
        if (!account.email.equals(email)) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Invalid email");

        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        storage.accounts.put(account);
        verificationTokens.remove(token);
        account.remove(token);
        storage.save();
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, "Email verified successfully");
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> register(@RequestBody(required = false) String body) {
        if (body == null || body.isBlank())
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Body is required in json format");
        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (Exception e) {
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Body must be in json format");
        }
        if (!json.has("username")) return new ApiResponse<>(ApiResponse.Status.FAILURE, "'username' field is required");
        if (!json.has("password")) return new ApiResponse<>(ApiResponse.Status.FAILURE, "'password' field is required");
        if (!json.has("email")) return new ApiResponse<>(ApiResponse.Status.FAILURE, "'email' field is required");
        if (!(json.get("username") instanceof String username) || !(json.get("email") instanceof String) || !(json.get("password") instanceof String password))
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "'username', 'email' and 'password' fields must be strings");
        String email = json.getString("email").toLowerCase(Locale.ROOT);
        if (invalidEmail(email)) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Invalid email format");
        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        for (AccountData account : storage.accounts.values()) {
            if (account.id.equals(username)) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Username is already taken");
            if (account.email.equals(email)) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Email is already in use");
        }

        String passwordHash = HashUtils.sha256(password);

        String tokenId = Utils.generateToken();
        String tokenHash = HashUtils.sha256(tokenId);
        AccountData accountData = new AccountData(
            QuiptApiApplication.api(),
            username,
            email,
            passwordHash,
            tokenId
        );
        Token token = new Token(tokenHash, "Registration token");
        verificationTokens.put(token, accountData);

        try {
            verificationEmailService.sendVerificationEmail(accountData.email, tokenHash);
        } catch (MailException e) {
            QuiptApiApplication.api().logger().error("Registration", "Failed to send verification email: " + e.getMessage());
            verificationTokens.remove(token);
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Registration failed: unable to send verification email");
        }
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, "Registration successful. Please check your email to verify your account.");
    }

    private boolean invalidEmail(String email) {
        if (!email.contains("@") || !email.contains(".")) return true;
        for (String tld : allowedTlds) {
            if (email.endsWith("." + tld)) return false;
        }
        return true;
    }

    @RequestMapping(value = "/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> edit(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody(required = false) String body) {
        ApiResponse<?> response = Utils.validateAuthorizationHeader(authorizationHeader);
        if (response.isFailure()) return new ApiResponse<>(ApiResponse.Status.FAILURE, response.data());
        if (!(response.data() instanceof AccountData accountData))
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
                if (!json.has("user"))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'user' field is required for add_permission action");
                String permission = json.getString("permission");
                String user = json.getString("user");
                AccountData targetAccount = storage.account(user);
                if (targetAccount == null)
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "User " + user + " not found for add_permission action");
                if (accountData.permission(permission) == null)
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "You don't have this permission to give.");

                targetAccount.add(new Permission(permission));
                storage.save();
            }
            case "create_token" -> {
                if (!json.has("description"))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'description' field is required for create_token action");
                if (!json.has("permissions"))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'permissions' field is required for create_token action");
                if (!(json.get("permissions") instanceof JSONArray permissionsJson))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'permissions' field must be a string array for create_token action");
                String description = json.getString("description");
                JSONObject responseObject = new JSONObject();
                List<String> permissions = new ArrayList<>();

                for (int i = 0; i < permissionsJson.length(); i++) {
                    if (accountData.permission(permissionsJson.getString(i)) != null) {
                        permissions.add(permissionsJson.getString(i));
                    } else {
                        if (!responseObject.has("invalid_permissions")) responseObject.put("invalid_permissions", new JSONArray());
                        JSONArray invalidPermissions = responseObject.getJSONArray("invalid_permissions");
                        JSONObject invalidPermission = new JSONObject();
                        invalidPermission.put("permission", permissionsJson.getString(i));
                        invalidPermission.put("error", "You don't have this permission to give.");
                        invalidPermissions.put(invalidPermission);
                    }
                }
                if (permissions.isEmpty())
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, responseObject.put("error", "No valid permissions provided."));

                String tokenId = Utils.generateToken();
                JSONArray permissionsApplied = new JSONArray();
                responseObject.put("token", tokenId);
                responseObject.put("description", description);

                Token token = new Token(tokenId, description);
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
        private final Map<Token, AccountData> verificationTokens = new HashMap<>();

        public void put(Token token, AccountData account) {
            verificationTokens.put(token, account);
        }

        public AccountData account(String token) {
            for (Token t : verificationTokens.keySet()) {
                if (t.id.equals(token)) return verificationTokens.get(t);
            }
            return null;
        }

        public Token token(String token) {
            for (Token t : verificationTokens.keySet()) {
                if (t.id.equals(token)) return t;
            }
            return null;
        }

        public boolean has(String token) {
            return token(token) != null;
        }

        public void remove(Token token) {
            verificationTokens.remove(token);
        }

        public void remove(String token) {
            remove(token(token));
        }
    }

}
