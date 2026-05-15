package live.qsmc.api.spring.controller;

import live.qsmc.api.util.Utils;
import live.qsmc.quipt.core.utils.net.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
public class TokenController {

    @GetMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> validate(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        ApiResponse<?> response = Utils.validateAuthorizationHeader(authorizationHeader);
        if (response.isFailure()) return response;
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, "Token is valid");
    }
}
