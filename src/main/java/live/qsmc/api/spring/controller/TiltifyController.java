package live.qsmc.api.spring.controller;

import jakarta.servlet.http.HttpServletRequest;
import live.qsmc.api.QuiptApiApplication;
import live.qsmc.api.util.Utils;
import live.qsmc.quipt.core.utils.net.ApiResponse;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tiltify")
class TiltifyController {

    @RequestMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> verify() {
        return new ApiResponse<>(ApiResponse.Status.SUCCESS, "Tiltify verification successful");
    }

}

