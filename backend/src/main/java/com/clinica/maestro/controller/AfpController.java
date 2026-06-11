package com.clinica.maestro.controller;

import com.clinica.maestro.dto.AfpResponse;
import com.clinica.maestro.service.AfpService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/afps")
public class AfpController {

    private final AfpService afpService;

    public AfpController(AfpService afpService) {
        this.afpService = afpService;
    }

    @GetMapping
    public List<AfpResponse> findAll() {
        return afpService.findAll();
    }
}
