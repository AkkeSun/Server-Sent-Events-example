package com.sweettracker.clinet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientController {

    @GetMapping
    public String index () {
        return "index";
    }
}
