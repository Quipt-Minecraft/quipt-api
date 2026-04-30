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
    private final Map<String, AccountData> verificationTokens = new HashMap<>();

    public AccountController(VerificationEmailService verificationEmailService, PasswordEncoder passwordEncoder) {
        this.verificationEmailService = verificationEmailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/verify")
    public Map<String, Object> verify(@RequestParam("token") String token, @RequestParam("email") String email) {
        if (token == null || email == null || token.isBlank() || email.isBlank())
            return Map.of("error", "Token and email are required");
        if (!verificationTokens.containsKey(token)) return Map.of("error", "Invalid token");
        AccountData account = verificationTokens.get(token);
        if (!account.email.equals(email)) return Map.of("error", "Invalid email");

        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        storage.accounts.put(account);
        verificationTokens.remove(token);
        account.remove(account.token(token));
        storage.save();
        return Map.of("message", "Email verified successfully");
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody(required = false) String body) {
        if (body == null || body.isBlank()) return Map.of("error", "Body is required in json format");
        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (Exception e) {
            return Map.of("error", "Body must be in json format");
        }
        if (!json.has("username")) return Map.of("error", "'username' field is required");
        if (!json.has("password")) return Map.of("error", "'password' field is required");
        if (!json.has("email")) return Map.of("error", "'email' field is required");

        String username = json.getString("username");
        String email = json.getString("email").toLowerCase(Locale.ROOT);
        String passwordHash = passwordEncoder.encode(json.getString("password"));

        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        if (storage.accounts.contains(username)) return Map.of("error", "Username is already taken");
        for (AccountData account : storage.accounts.values()) {
            if (account.email.equals(email)) return Map.of("error", "Email is already in use");
        }

        String token = Utils.generateToken();
        AccountData accountData = new AccountData(
            QuiptApiApplication.api(),
            username,
            email,
            passwordHash,
            token
        );
        verificationTokens.put(token, accountData);

        try {
            verificationEmailService.sendVerificationEmail(accountData.email, token);
        } catch (MailException e) {
            QuiptApiApplication.api().logger().error("Registration", "Failed to send verification email: " + e.getMessage());
            verificationTokens.remove(token);
            return Map.of("error", "Registration failed: unable to send verification email");
        }
//        String verifyUrl = "https://api.qsmc.live/account/verify?token=" + token + "&email=" + accountData.email;
        return Map.of("message", "Registration successful. Please check your email to verify your account.");
    }

    @RequestMapping(value = "/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Object> edit(@RequestParam("username") String username, @RequestBody(required = false) String body) {
        if (body == null || body.isBlank())
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Body is required in json format");
        JSONObject json;

        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Invalid JSON format in request body");
        }
        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        AccountData account = storage.accounts.get(username);

        if (account == null) return new ApiResponse<>(ApiResponse.Status.FAILURE, "Account not found");
        if (!json.has("action")) return new ApiResponse<>(ApiResponse.Status.FAILURE, "'action' field is required");
        String action = json.getString("action").toLowerCase(Locale.ROOT);
        switch (action) {
            case "add_permission" -> {
                if (!json.has("permission"))
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, "'permission' field is required for add_permission action");
                String permission = json.getString("permission");
                account.add(new AccountPermission(permission));
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
                JSONObject response = new JSONObject();
                List<String> permissions = new ArrayList<>();

                for (int i = 0; i < permissionsJson.length(); i++) {
                    if (account.permission(permissionsJson.getString(i)) != null)
                        permissions.add(permissionsJson.getString(i));
                    else {
                        if(!response.has("invalid_permissions")) response.put("invalid_permissions", new JSONArray());
                        JSONArray invalidPermissions = response.getJSONArray("invalid_permissions");
                        JSONObject invalidPermission = new JSONObject();
                        invalidPermission.put("permission", permissionsJson.getString(i));
                        invalidPermission.put("error", "You don't have this permission to give.");
                        invalidPermissions.put(invalidPermission);
                    }
                }
                if(permissions.isEmpty())
                    return new ApiResponse<>(ApiResponse.Status.FAILURE, response.put("error", "No valid permissions provided."));

                String tokenId = Utils.generateToken();
                JSONArray permissionsApplied = new JSONArray();
                response.put("token", tokenId);
                response.put("description", description);

                AccountToken token = new AccountToken(tokenId, json.getString("description"));
                for (String permission : permissions) {
                    token.permissionsArray.put(permission);
                    permissionsApplied.put(permission);
                }
                response.put("permissions_applied", permissionsApplied);

                account.add(token);
                storage.save();
                return new ApiResponse<>(ApiResponse.Status.SUCCESS, response);

            }
            default -> {
                return new ApiResponse<>(ApiResponse.Status.FAILURE, "Unsupported action: " + action);
            }
        }
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, "Edit successful");
    }


}
