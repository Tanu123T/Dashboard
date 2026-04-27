package com.ceodashboard.backend.controller;

import com.ceodashboard.backend.service.OpenProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/sync")

public class OpenProjectController {

    @Autowired
    private OpenProjectService service;

    @GetMapping
    public String sync() {
        service.syncProjects();
        return "Data Synced!";
    }

    @GetMapping("/raw")
    public ResponseEntity<Map<String, Object>> fetchRaw(@RequestParam String path) {
        return ResponseEntity.ok(service.fetchResource(path));
    }
}