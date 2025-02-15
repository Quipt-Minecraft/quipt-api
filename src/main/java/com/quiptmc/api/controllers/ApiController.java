package com.quiptmc.api.controllers;

import com.quiptmc.core.utils.NetworkUtils;
import com.quiptmc.api.QuiptApi;
import com.quiptmc.api.feedback.Feedback;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.quiptmc.api.feedback.Feedback.Result.SUCCESS;


@RestController
@RequestMapping("/api")
public class ApiController {


    @PostMapping("/register/{ip}")
    public String registerServer(@PathVariable String ip, @RequestParam String token) {
        QuiptApi.utils.addRequest();
        QuiptApi.utils.register(ip, token);
        return new Feedback(SUCCESS, "Server registered").json();
    }

    @PostMapping("/update/{ip}")
    public String updateServer(@PathVariable String ip, @RequestBody String data) {
        QuiptApi.utils.addRequest();
        if(QuiptApi.utils.getToken(ip).isEmpty()) return new Feedback(Feedback.Result.FAILURE, "Server not registered").json();
        QuiptApi.utils.update(ip, new JSONObject(data));
        String xml = new Feedback(SUCCESS, "Server data updated").xml();
        return new Feedback(SUCCESS, "Server data updated").json();
    }

    @PostMapping("/server_status/{ip}")
    public String postServerStatus(@PathVariable String ip, @RequestBody String data) {
        JSONObject request = new JSONObject(data);
        System.out.println(request.toString(2));
        return requestServerStatus(ip, request.has("secret") ? request.getString("secret") : null);
    }



    @GetMapping("/server_status/{ip}")
    public String getServerStatus(@PathVariable String ip) {
        return requestServerStatus(ip, null);
    }

    private String requestServerStatus(String ip, String secret) {
        QuiptApi.utils.addRequest();
        Optional<String> storedToken = QuiptApi.utils.getToken(ip);
        JSONObject response = new JSONObject(NetworkUtils.request(QuiptApi.utils.config.fallbackApi.replaceAll("%ip%", ip)));
        if (storedToken.isPresent()) {
            JSONObject quiptData = QuiptApi.utils.getServerData(ip, secret);
            response.put("quipt_data", quiptData);
        }
        return response.toString();
    }


}