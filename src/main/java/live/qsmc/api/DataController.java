package live.qsmc.api;

import live.qsmc.api.util.Utils;
import live.qsmc.core2.utils.TaskScheduler;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/data")
class DataController {

    @RequestMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> update(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Map<String, Object> passwordValidation = Utils.validateAuthorizationHeader(authorizationHeader);
        if (!passwordValidation.isEmpty()) return passwordValidation;

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
