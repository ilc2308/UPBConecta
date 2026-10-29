package co.edu.upb.upbconecta.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.upb.upbconecta.dto.HealthResponse;
import co.edu.upb.upbconecta.service.HealthService;

/** GET /api/v1/health: comprueba que el servidor responde. */
@RestController
@RequestMapping("/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public HealthResponse health() {
        return healthService.check();
    }
}
