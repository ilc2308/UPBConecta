package co.edu.upb.upbconecta.controller;

import co.edu.upb.upbconecta.config.CorsConfig;
import co.edu.upb.upbconecta.config.SecurityConfig;
import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.EstadoPostulacion;
import co.edu.upb.upbconecta.dto.ActualizarEstadoOfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaDetalleDTO;
import co.edu.upb.upbconecta.dto.OfertaResumenDTO;
import co.edu.upb.upbconecta.dto.PostulanteDTO;
import co.edu.upb.upbconecta.exception.AccesoDenegadoException;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.exception.ResourceNotFoundException;
import co.edu.upb.upbconecta.service.OfertaService;
import co.edu.upb.upbconecta.service.PostulacionService;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoints de ofertas con la seguridad real.
 * POST /api/v1/ofertas (UPBCONECT-39 y 40): 201 con sesion, 400 datos invalidos, 401 sin sesion, 409 duplicado.
 * GET /api/v1/ofertas/mias (UPBCONECT-40): 200 con las publicaciones del usuario, 401 sin sesion.
 * GET /api/v1/ofertas (UPBCONECT-37): 200 con listado paginado de ofertas activas, 401 sin sesion.
 * PATCH /api/v1/ofertas/{id}/estado (UPBCONECT-44): 200 finalizada, 401 sin sesion, 403 no dueno, 404 no existe, 409 transicion invalida.
 * GET /api/v1/ofertas/asignadas/mias (UPBCONECT-44): 200 con trabajos asignados y finalizados del estudiante.
 */
@WebMvcTest(OfertaController.class)
@Import({SecurityConfig.class, CorsConfig.class, SesionService.class,
        OfertaControllerWebTest.ConSeguridad.class})
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
    JwtDecoder jwtDecoder;

    @MockitoBean
    OfertaService ofertaService;

    @MockitoBean
    PostulacionService postulacionService;

    private final SesionService.Usuario usuario = new SesionService.Usuario("demo@upb.edu.co", "Usuario Demo");

    private Cookie sesion() {
        return new Cookie("UPBC_SESSION", sesiones.crear(usuario));
    }

    // --- UPBCONECT-39 / 40 ---

    @Test
    void publicaConJwtInstitucionalYResponde201() throws Exception {
        SesionService.Usuario profe = new SesionService.Usuario("profe@upb.edu.co", "Profesor");
        when(ofertaService.crear(any(), eq(profe))).thenReturn(new OfertaDTO(2L,
                "Apoyo para feria de emprendimiento", "Organizar asistentes y material", "Puntualidad requerida",
                80000, LocalDateTime.of(2099, 12, 31, 18, 0), false, "Campus UPB - Bloque 7",
                EstadoOferta.ABIERTA, LocalDateTime.now(), "profe@upb.edu.co"));

        org.springframework.security.oauth2.jwt.Jwt jwtMock = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("mock-token")
                .header("alg", "HS256")
                .subject(java.util.UUID.randomUUID().toString())
                .claim("email", "profe@upb.edu.co")
                .claim("nombre", "Profesor")
                .issuedAt(java.time.Instant.now())
                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                .build();
        when(jwtDecoder.decode("mock-token")).thenReturn(jwtMock);

        mockMvc.perform(post("/api/v1/ofertas")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON).content(VALIDA))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(2))
               .andExpect(jsonPath("$.publicadorCorreo").value("profe@upb.edu.co"));
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

    // --- UPBCONECT-41 ---

    private final PostulanteDTO postulante = new PostulanteDTO(10L, "Ana", "ana@upb.edu.co", "Sistemas",
            EstadoPostulacion.PENDIENTE, LocalDateTime.of(2026, 1, 1, 8, 0));

    @Test
    void detalleConSesionResponde200ConDatosDelEmpleador() throws Exception {
        when(ofertaService.obtenerDetalle(eq(1L), any())).thenReturn(new OfertaDetalleDTO(1L, "Titulo", "Actividad",
                "Condiciones", 1000, LocalDateTime.of(2099, 1, 1, 8, 0), false, "Bloque 7", EstadoOferta.ABIERTA, true,
                LocalDateTime.of(2026, 1, 1, 8, 0), "Empleador", "emp@upb.edu.co", "Derecho", true));

        mockMvc.perform(get("/api/v1/ofertas/1").cookie(sesion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicadorNombre").value("Empleador"))
                .andExpect(jsonPath("$.publicadorCarrera").value("Derecho"))
                .andExpect(jsonPath("$.yaPostulado").value(true));
    }

    @Test
    void detalleSinSesionResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/ofertas/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void detalleDeOfertaInexistenteResponde404() throws Exception {
        when(ofertaService.obtenerDetalle(eq(99L), any())).thenThrow(new ResourceNotFoundException("No existe"));

        mockMvc.perform(get("/api/v1/ofertas/99").cookie(sesion())).andExpect(status().isNotFound());
    }

    @Test
    void detalleConIdNoNumericoResponde400() throws Exception {
        mockMvc.perform(get("/api/v1/ofertas/abc").cookie(sesion())).andExpect(status().isBadRequest());
    }

    @Test
    void postularResponde201() throws Exception {
        when(postulacionService.postular(eq(1L), any())).thenReturn(postulante);

        mockMvc.perform(post("/api/v1/ofertas/1/postulaciones").cookie(sesion()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void postularSinSesionResponde401() throws Exception {
        mockMvc.perform(post("/api/v1/ofertas/1/postulaciones")).andExpect(status().isUnauthorized());
        verify(postulacionService, never()).postular(any(), any());
    }

    @Test
    void postularDuplicadoResponde409ConMensaje() throws Exception {
        when(postulacionService.postular(eq(1L), any()))
                .thenThrow(new ConflictoNegocioException("Ya te postulaste a esta oferta"));

        mockMvc.perform(post("/api/v1/ofertas/1/postulaciones").cookie(sesion()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya te postulaste a esta oferta"));
    }

    @Test
    void postularSinRolEstudianteResponde403() throws Exception {
        when(postulacionService.postular(eq(1L), any()))
                .thenThrow(new AccesoDenegadoException("Solo los estudiantes pueden postularse"));

        mockMvc.perform(post("/api/v1/ofertas/1/postulaciones").cookie(sesion()))
                .andExpect(status().isForbidden());
    }

    // --- UPBCONECT-43 ---

    @Test
    void seleccionarResponde200ConLaListaActualizada() throws Exception {
        when(postulacionService.seleccionar(eq(1L), eq(10L), any())).thenReturn(List.of(new PostulanteDTO(
                10L, "Ana", "ana@upb.edu.co", "Sistemas", EstadoPostulacion.SELECCIONADA,
                LocalDateTime.of(2026, 1, 1, 8, 0))));

        mockMvc.perform(patch("/api/v1/ofertas/1/postulaciones/10/seleccionar").cookie(sesion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("SELECCIONADA"));
    }

    @Test
    void seleccionarSegundoPostulanteResponde409() throws Exception {
        when(postulacionService.seleccionar(eq(1L), eq(11L), any()))
                .thenThrow(new ConflictoNegocioException("Esta oferta ya no admite seleccionar postulantes"));

        mockMvc.perform(patch("/api/v1/ofertas/1/postulaciones/11/seleccionar").cookie(sesion()))
                .andExpect(status().isConflict());
    }

    @Test
    void seleccionarSinSesionResponde401() throws Exception {
        mockMvc.perform(patch("/api/v1/ofertas/1/postulaciones/10/seleccionar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tareasPendientesResponde200() throws Exception {
        when(ofertaService.tareasPendientes(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/ofertas/asignadas").cookie(sesion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- UPBCONECT-44 ---

    @Test
    void actualizarEstadoAFinalizadaResponde200() throws Exception {
        OfertaDTO ofertaFinalizada = new OfertaDTO(1L, "Titulo", "Actividad", "Condiciones", 1000,
                LocalDateTime.of(2099, 1, 1, 8, 0), false, "Bloque 7", EstadoOferta.FINALIZADA,
                LocalDateTime.of(2026, 1, 1, 8, 0), "demo@upb.edu.co", "Estudiante",
                "est@upb.edu.co", LocalDateTime.now(), LocalDateTime.now());

        when(ofertaService.actualizarEstado(eq(1L), eq(new ActualizarEstadoOfertaDTO(EstadoOferta.FINALIZADA)), any()))
                .thenReturn(ofertaFinalizada);

        mockMvc.perform(patch("/api/v1/ofertas/1/estado")
                        .cookie(sesion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"FINALIZADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"));
    }

    @Test
    void actualizarEstadoSinSesionResponde401() throws Exception {
        mockMvc.perform(patch("/api/v1/ofertas/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"FINALIZADA\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actualizarEstadoNoDuenoResponde403() throws Exception {
        when(ofertaService.actualizarEstado(eq(1L), any(), any()))
                .thenThrow(new AccesoDenegadoException("Solo quien publicó la oferta puede actualizar su estado"));

        mockMvc.perform(patch("/api/v1/ofertas/1/estado")
                        .cookie(sesion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"FINALIZADA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarEstadoOfertaInexistenteResponde404() throws Exception {
        when(ofertaService.actualizarEstado(eq(99L), any(), any()))
                .thenThrow(new ResourceNotFoundException("No existe"));

        mockMvc.perform(patch("/api/v1/ofertas/99/estado")
                        .cookie(sesion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"FINALIZADA\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarEstadoTransicionInvalidaResponde409() throws Exception {
        when(ofertaService.actualizarEstado(eq(1L), any(), any()))
                .thenThrow(new ConflictoNegocioException("Transición de estado no permitida"));

        mockMvc.perform(patch("/api/v1/ofertas/1/estado")
                        .cookie(sesion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"FINALIZADA\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Transición de estado no permitida"));
    }

    @Test
    void misAsignadasResponde200() throws Exception {
        when(ofertaService.misTrabajosAsignados(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/ofertas/asignadas/mias").cookie(sesion()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void misAsignadasSinSesionResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/ofertas/asignadas/mias"))
                .andExpect(status().isUnauthorized());
    }
}
