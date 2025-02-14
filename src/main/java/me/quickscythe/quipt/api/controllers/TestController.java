package me.quickscythe.quipt.api.controllers;

import me.quickscythe.quipt.api.feedback.Feedback;
import me.quickscythe.quipt.api.utils.NetworkUtils;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.logging.Logger;


@RestController
@RequestMapping("/test")
public class TestController {

    public static String post(String url, JSONObject data, String... auth) {
        try {
            URL myUrl = new URI(url).toURL();
            HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
            conn.setDoOutput(true);
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestMethod("POST");


            if (auth != null && auth.length >= 2) {
                String userCredentials = auth[0].trim() + ":" + auth[1].trim();
                String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
                conn.setRequestProperty("Authorization", basicAuth);
            }
            conn.getOutputStream().write(data.toString().getBytes());
            String response = NetworkUtils.streamToString(conn.getInputStream());
            System.out.println("Response: " + response);
            return response;
        } catch (Exception ex) {
            Logger.getLogger("Network").info("An error occurred while downloading file");
            ex.printStackTrace();
        }
        return null;
    }
    @PostMapping("/post")
    public String doPost(@RequestBody String data){
        System.out.println("Test: " + data);
        return data;
    }

    @GetMapping("/test")
    public String doTest(){
        JSONObject data = new JSONObject();
        data.put("test", "test");
        String response = NetworkUtils.post("http://localhost:8080/api/update/127.0.0.1", data);
        System.out.println("Response 2: " + response);
        return new Feedback(Feedback.Result.SUCCESS, "Test successful").json();
    }
}
