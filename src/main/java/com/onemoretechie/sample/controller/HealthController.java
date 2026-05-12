package com.onemoretechie.sample.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
                "service", "sample-web-app",
                "status", "ok",
                "docs", "/actuator");
    }
}
