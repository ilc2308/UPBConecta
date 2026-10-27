package co.edu.upb.upbconecta.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.upb.upbconecta.service.HealthService;
import co.edu.upb.upbconecta.dto.HealthResponse;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

/**
 * Prueba de integracion del endpoint GET /api/v1/health.
 * Verifica que el endpoint este publicado, sea anonimo y devuelva 200.
 */
@WebMvcTest(HealthController.class)
class HealthControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthService healthService;

    @Test
    void healthEndpointDevuelve200YEsAnonimo() throws Exception {
        when(healthService.getHealth())
                .thenReturn(new HealthResponse("UP", "upbconecta-backend", "1.0.0", Instant.now()));

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("upbconecta-backend"));
    }
}