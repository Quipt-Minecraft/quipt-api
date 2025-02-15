package me.quickscythe.quipt.api.controllers;

import me.quickscythe.quipt.api.QuiptApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {


    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("servers", QuiptApi.utils.config.servers.length());
        model.addAttribute("requestsHandled", QuiptApi.utils.config.requests);
        model.addAttribute("uptime", 99.1);
        model.addAttribute("hardWorkers", 25);
        return "index";
    }
}
