package live.qsmc.api.spring.controller;

import live.qsmc.api.util.Utils;
import live.qsmc.core2.utils.TaskScheduler;
import live.qsmc.core2.utils.net.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/data")
class DataController {

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
