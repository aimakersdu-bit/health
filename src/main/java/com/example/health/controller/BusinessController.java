package com.example.health.controller;

import com.example.health.indicator.BusinessHealthIndicator;
import com.example.health.indicator.RegistrationHealthIndicator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/control")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessHealthIndicator businessHealthIndicator;
    private final RegistrationHealthIndicator registrationHealthIndicator;

    @PostMapping("/business/toggle")
    public String toggleBusiness(@RequestParam boolean up) {
        businessHealthIndicator.setBusinessUp(up);
        return "Business health set to " + (up ? "UP" : "DOWN");
    }

    @PostMapping("/registration/toggle")
    public String toggleRegistration(@RequestParam boolean registered) {
        registrationHealthIndicator.setRegistered(registered);
        return "Registration status set to " + (registered ? "REGISTERED" : "NOT_REGISTERED");
    }
}
