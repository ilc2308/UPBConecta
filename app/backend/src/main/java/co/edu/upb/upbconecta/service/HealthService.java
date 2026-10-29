package co.edu.upb.upbconecta.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import co.edu.upb.upbconecta.dto.HealthResponse;

@Service
public class HealthService {

    public HealthResponse check() {
        return new HealthResponse("UP", "upbconecta-backend", Instant.now());
    }
}
