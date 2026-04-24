package live.qsmc.api;

import live.qsmc.api.config.DefaultConfig;
import live.qsmc.core2.utils.TaskScheduler;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/data")
class DataController {

    @RequestMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> update(@RequestBody(required = false) String body) {
        if(body == null || body.isBlank())
            return Map.of("error", "Request body is required in json format");
        if(!body.startsWith("{") || !body.endsWith("}"))
            return Map.of("error", "Request body must be in json format");
        JSONObject json = new JSONObject(body);
        if(!json.has("secret"))
            return Map.of("error", "Secret is required");
        if(!json.getString("secret").equals(QuiptApiApplication.api().config().secret))
            return Map.of("error", "Invalid secret");
        TaskScheduler.scheduleAsyncTask(() -> {
            System.exit(0);
        },2, TimeUnit.SECONDS);
        return Map.of("message", "Update in progress...");
    }

    @RequestMapping("/download")
    public String download() {
        return "download";
    }

}
