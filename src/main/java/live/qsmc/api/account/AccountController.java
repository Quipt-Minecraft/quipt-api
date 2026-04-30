package live.qsmc.api.account;

import live.qsmc.api.QuiptApiApplication;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
        if (token == null || email == null || token.isBlank() || email.isBlank()) return Map.of("error", "Token and email are required");
        if (!verificationTokens.containsKey(token)) return Map.of("error", "Invalid token");
        AccountData account = verificationTokens.get(token);
        if (!account.email.equals(email)) return Map.of("error", "Invalid email");

        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        storage.accounts.put(account);
        verificationTokens.remove(token);
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

        String token = UUID.randomUUID().toString().replace("-", "");
        while(storage.token(token) != null){
            token = UUID.randomUUID().toString().replace("-", "");
        }
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

    @RequestMapping("/edit")
    public Map<String, Object> edit(@RequestParam("username") String username, @RequestBody(required = false) String body) {
        if(body == null || body.isBlank()) return Map.of("error", "Body is required in json format");
        JSONObject json;

        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            return Map.of("error", "Invalid JSON format in request body");
        }
        AccountStorage storage = QuiptApiApplication.api().configs().config(AccountStorage.class);
        AccountData account = storage.accounts.get(username);
        if(account == null) return Map.of("error", "Account not found");
        if(json.getString("action").toLowerCase().equals("add_permission")) {
            if (!json.has("permission")) return Map.of("error", "'permission' field is required for add_permission action");
            String permission = json.getString("permission");
            account.add(new AccountPermission(permission, "Admin permission value."));
            storage.save();
        } else {
            return Map.of("error", "Unsupported action: " + json.getString("action"));
        }

        return Map.of("message", "Edit successful");
    }
}
