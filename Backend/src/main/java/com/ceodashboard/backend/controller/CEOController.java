package com.ceodashboard.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ceo")
public class CEOController {

    @GetMapping("/dashboard")
    public String ceoDashboard() {
        return "CEO ACCESS GRANTED";
    }
}