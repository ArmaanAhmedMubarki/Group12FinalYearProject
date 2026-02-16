package com.group12.athleticaX;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to AthleticaX - Sports Performance Monitoring System";
    }
}
