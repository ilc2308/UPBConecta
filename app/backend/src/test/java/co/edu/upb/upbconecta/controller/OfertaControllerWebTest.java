package co.edu.upb.upbconecta.controller;

import co.edu.upb.upbconecta.config.CorsConfig;
import co.edu.upb.upbconecta.config.SecurityConfig;
import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.service.OfertaService;
import co.edu.upb.upbconecta.service.SesionService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** POST /api/v1/ofertas (UPBCONECT-39) con la seguridad real: 201 con sesion, 400 con datos invalidos, 401 sin sesion. */
@WebMvcTest(OfertaController.class)
@Import({SecurityConfig.class, CorsConfig.class, SesionService.class, OfertaControllerWebTest.ConSeguridad.class})
class OfertaControllerWebTest {

    /** En Spring Boot 4 el slice @WebMvcTest no activa Spring Security por si solo. */
    @TestConfiguration
    @EnableWebSecurity
    static class ConSeguridad {
    }

    private static final String VALIDA = """
            {"titulo": "Apoyo para feria de emprendimiento",
             "actividad": "Organizar asistentes y material",
             "condiciones": "Puntualidad requerida",
             "valorOfrecido": 80000,
             "fechaLimite": "2099-12-31T18:00:00",
             "urgente": false,
             "ubicacion": "Campus UPB - Bloque 7"}
            """;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    SesionService sesiones;

    @MockitoBean
    OfertaService ofertaService;

    private final SesionService.Usuario usuario = new SesionService.Usuario("demo@upb.edu.co", "Usuario Demo");

    private Cookie sesion() {
        return new Cookie("UPBC_SESSION", sesiones.crear(usuario));
    }

    @Test
    void publicaConDatosValidosYResponde201() throws Exception {
        when(ofertaService.crear(any(), eq(usuario))).thenReturn(new OfertaDTO(1L,
                "Apoyo para feria de emprendimiento", "Organizar asistentes y material", "Puntualidad requerida",
                80000, LocalDateTime.of(2099, 12, 31, 18, 0), false, "Campus UPB - Bloque 7",
                EstadoOferta.ABIERTA, LocalDateTime.now(), "demo@upb.edu.co"));

        mockMvc.perform(post("/api/v1/ofertas").cookie(sesion())
                        .contentType(MediaType.APPLICATION_JSON).content(VALIDA))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.estado").value("ABIERTA"))
               .andExpect(jsonPath("$.publicadorCorreo").value("demo@upb.edu.co"));
    }

    @Test
    void rechazaCamposVaciosValorNoPositivoYFechaPasadaCon400() throws Exception {
        String invalida = """
                {"titulo": "", "actividad": "", "condiciones": "",
                 "valorOfrecido": 0, "fechaLimite": "2000-01-01T10:00:00",
                 "urgente": false, "ubicacion": ""}
                """;

        mockMvc.perform(post("/api/v1/ofertas").cookie(sesion())
                        .contentType(MediaType.APPLICATION_JSON).content(invalida))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errores.length()").value(6));

        verify(ofertaService, never()).crear(any(), any());
    }

    @Test
    void sinSesionResponde401() throws Exception {
        mockMvc.perform(post("/api/v1/ofertas")
                        .contentType(MediaType.APPLICATION_JSON).content(VALIDA))
               .andExpect(status().isUnauthorized());

        verify(ofertaService, never()).crear(any(), any());
    }
}
