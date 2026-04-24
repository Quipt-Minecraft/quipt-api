package live.qsmc.api;

import live.qsmc.core2.utils.TaskScheduler;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.MediaType;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/data")
class DataController {

    @GetMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject update() {
        TaskScheduler.scheduleAsyncTask(() -> {
            System.exit(0);
        },2, TimeUnit.SECONDS);
        JSONObject json = new JSONObject();
        json.put("message", "Update in progress...");
        return json;
    }

    @RequestMapping("/download")
    public String download() {
        return "download";
    }

}
