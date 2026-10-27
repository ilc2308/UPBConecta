package co.edu.upb.upbconecta.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import co.edu.upb.upbconecta.dto.HealthResponse;
import co.edu.upb.upbconecta.service.HealthService;

class HealthControllerTest {

    @Test
    void healthDevuelveUp() {
        HealthController controller = new HealthController(new HealthService());

        HealthResponse response = controller.health();

        assertEquals("UP", response.status());
        assertEquals("upbconecta-backend", response.service());
        assertNotNull(response.timestamp());
    }
}