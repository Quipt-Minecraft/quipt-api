package com.quiptmc.api.components.controllers;

import com.quiptmc.api.QuiptApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {


    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("servers", QuiptApi.utils.serverManager.servers().size());
        model.addAttribute("requestsHandled", QuiptApi.utils.serverManager.requests().size());
        model.addAttribute("uptime", QuiptApi.utils.uptimeManager.getUptime());
        model.addAttribute("hardWorkers", 25);
        return "index";
    }
}
