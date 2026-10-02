package live.qsmc.api.spring.controller;

import live.qsmc.api.util.Utils;
import live.qsmc.quipt.core.utils.TaskScheduler;
import live.qsmc.quipt.core.utils.net.ApiResponse;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/data")
public class DataController {

    private static final long START_TIME_MILLIS = System.currentTimeMillis();

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<JSONObject> status() {
        long jvmUptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        long startTime = System.currentTimeMillis() - jvmUptimeMillis;
        long uptimeSeconds = jvmUptimeMillis / 1000;

        long days = uptimeSeconds / 86400;
        long hours = (uptimeSeconds % 86400) / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;

        String formatted = String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds);

        JSONObject data = new JSONObject();
        data.put("status", "UP");
        data.put("uptime_ms", jvmUptimeMillis);
        data.put("uptime_seconds", uptimeSeconds);
        data.put("start_time_ms", startTime);
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
