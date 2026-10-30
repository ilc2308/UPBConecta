package co.edu.upb.upbconecta.controller;

import co.edu.upb.upbconecta.config.CorsConfig;
import co.edu.upb.upbconecta.config.SecurityConfig;
import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaResumenDTO;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.service.OfertaService;
import co.edu.upb.upbconecta.service.SesionService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoints de ofertas con la seguridad real.
 * POST /api/v1/ofertas (UPBCONECT-39 y 40): 201 con sesion, 400 datos invalidos, 401 sin sesion, 409 duplicado.
 * GET /api/v1/ofertas/mias (UPBCONECT-40): 200 con las publicaciones del usuario, 401 sin sesion.
 * GET /api/v1/ofertas (UPBCONECT-37): 200 con listado paginado de ofertas activas, 401 sin sesion.
 */
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

    // --- UPBCONECT-39 / 40 ---

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

    @Test
    void envioDuplicadoResponde409() throws Exception {
        when(ofertaService.crear(any(), eq(usuario)))
                .thenThrow(new ConflictoNegocioException("Ya publicaste esta oferta hace un momento"));

        mockMvc.perform(post("/api/v1/ofertas").cookie(sesion())
                        .contentType(MediaType.APPLICATION_JSON).content(VALIDA))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.detail").value("Ya publicaste esta oferta hace un momento"));
    }

    @Test
    void misPublicacionesDevuelveLasDelUsuarioDeLaSesion() throws Exception {
        when(ofertaService.misOfertas(usuario)).thenReturn(List.of(new OfertaDTO(7L, "Tutoria de calculo",
                "Repasar derivadas", "Llevar apuntes", 20000, LocalDateTime.of(2099, 1, 1, 10, 0), false,
                "Bloque 10", EstadoOferta.ABIERTA, LocalDateTime.now(), "demo@upb.edu.co")));

        mockMvc.perform(get("/api/v1/ofertas/mias").cookie(sesion()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].titulo").value("Tutoria de calculo"));
    }

    @Test
    void misPublicacionesSinSesionResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/ofertas/mias"))
               .andExpect(status().isUnauthorized());
    }

    // --- UPBCONECT-37 ---

    @Test
    void listarActivasConSesionResponde200() throws Exception {
        OfertaResumenDTO oferta = new OfertaResumenDTO(1L, "Apoyo en feria", "Organizar el espacio",
                80000, LocalDateTime.of(2099, 12, 31, 18, 0), false, "Bloque 7",
                EstadoOferta.ABIERTA, LocalDateTime.now(), "empleador@upb.edu.co");

        when(ofertaService.listarActivas(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(oferta), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/ofertas").cookie(sesion()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content.length()").value(1))
               .andExpect(jsonPath("$.content[0].titulo").value("Apoyo en feria"))
               .andExpect(jsonPath("$.content[0].estado").value("ABIERTA"))
               .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listarActivasSinSesionResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/ofertas"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void listarActivasConListaVaciaResponde200ConCeroElementos() throws Exception {
        when(ofertaService.listarActivas(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/ofertas").cookie(sesion()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void listarActivasConPaginacionResponde200() throws Exception {
        when(ofertaService.listarActivas(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/ofertas")
                        .param("page", "0")
                        .param("size", "5")
                        .cookie(sesion()))
               .andExpect(status().isOk());
    }
}
