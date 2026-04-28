package com.ceodashboard.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PublicController {

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
            "status", "ok",
            "message", "Backend is running"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}