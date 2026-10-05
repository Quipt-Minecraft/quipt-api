package live.qsmc.api.spring.controller;

import live.qsmc.api.QuiptApiApplication;
import live.qsmc.api.account.AccountData;
import live.qsmc.api.account.AccountStorage;
import live.qsmc.api.account.ServerStorage;
import live.qsmc.api.account.Token;
import live.qsmc.api.util.Utils;
import live.qsmc.quipt.core.config.ConfigManager;
import live.qsmc.quipt.core.utils.HashUtils;
import live.qsmc.quipt.core.utils.TaskScheduler;
import live.qsmc.quipt.core.utils.net.ApiResponse;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/data")
public class DataController {

    @PostMapping(value = "/log", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> register(@RequestBody(required = false) String body) {
        if (body == null || body.isBlank())
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Body is required in json format");
        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (Exception e) {
            return new ApiResponse<>(ApiResponse.Status.FAILURE, "Body must be in json format");
        }
        ServerStorage config = QuiptApiApplication.api().configs().config(ServerStorage.class);
        for(int i = 0; i != config.logs.length(); i++){
            JSONObject log = (JSONObject) config.logs.get(i);
            if(log.equals(json)){
                return new ApiResponse<>(ApiResponse.Status.FAILURE, "Duplicate log entry");
            }

        }
        config.logs.put(json);
        config.save();
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, "Registration successful. Please check your email to verify your account.");
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<JSONObject> status() {
        ServerStorage serverStorage = QuiptApiApplication.api().configs().config(ServerStorage.class);
        long firstStartMs = serverStorage.firstStartMs;
        long uptimeMs = System.currentTimeMillis() - firstStartMs;
        long uptimeSeconds = uptimeMs / 1000;

        long days = uptimeSeconds / 86400;
        long hours = (uptimeSeconds % 86400) / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;

        String formatted = String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds);

        JSONObject data = new JSONObject();
        data.put("status", "UP");
        data.put("uptime_ms", uptimeMs);
        data.put("uptime_seconds", uptimeSeconds);
        data.put("start_time_ms", firstStartMs);
        data.put("formatted_uptime", formatted);

        return new ApiResponse<>(ApiResponse.Status.SUCCESS, data);
    }

    @RequestMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> update(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        ApiResponse<?> response = Utils.validateAuthorizationHeader(authorizationHeader);
        if (response.isFailure()) return response;

        TaskScheduler.scheduleAsyncTask(() -> {
            System.exit(0);
        },2, TimeUnit.SECONDS);
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, "Update in progress...");
    }

    @RequestMapping("/download")
    public String download() {
        return "download";
    }

}
