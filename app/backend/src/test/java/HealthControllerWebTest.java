package co.edu.upb.upbconecta.controller;

import co.edu.upb.upbconecta.service.HealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica que el endpoint real queda publicado en /api/v1/health, responde 200 y no exige autenticacion. */
@WebMvcTest(HealthController.class)
@Import(HealthService.class)
class HealthControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void healthEstaPublicadoYResponde200SinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
               .andExpect(status().isOk());
    }
}
