package com.ceodashboard.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hr")
public class HRController {

    @GetMapping("/panel")
    public String hrPanel() {
        return "HR ACCESS GRANTED";
    }
}