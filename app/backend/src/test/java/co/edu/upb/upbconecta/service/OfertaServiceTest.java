package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.dto.ActualizarEstadoOfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaCreateDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaDetalleDTO;
import co.edu.upb.upbconecta.dto.OfertaResumenDTO;
import co.edu.upb.upbconecta.exception.AccesoDenegadoException;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.exception.ResourceNotFoundException;
import co.edu.upb.upbconecta.model.RolUsuario;
import co.edu.upb.upbconecta.model.UsuarioEntity;
import co.edu.upb.upbconecta.repository.OfertaRepository;
import co.edu.upb.upbconecta.repository.PostulacionRepository;
import co.edu.upb.upbconecta.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de OfertaService (UPBCONECT-39, 40, 37, 41, 43 y 44), sin base de datos: el repositorio es simulado. */
@ExtendWith(MockitoExtension.class)
class OfertaServiceTest {

    @Mock
    OfertaRepository ofertas;

    @Mock
    UsuarioRepository usuarios;

    @Mock
    PostulacionRepository postulaciones;

    @InjectMocks
    OfertaService ofertaService;

    private final SesionService.Usuario ana = new SesionService.Usuario("ana@upb.edu.co", "Ana");

    private final OfertaCreateDTO datos = new OfertaCreateDTO("  Imprimir trabajo  ", "Imprimir 20 hojas",
            "Antes de las 10", 5000, LocalDateTime.now().plusDays(1), true, "Bloque 11");

    // --- UPBCONECT-39 / 40 ---

    @Test
    void creaLaOfertaAbiertaANombreDelUsuarioDeLaSesion() {
        when(ofertas.save(any(Oferta.class))).thenAnswer(inv -> inv.getArgument(0));

        OfertaDTO creada = ofertaService.crear(datos, ana);

        assertThat(creada.estado()).isEqualTo(EstadoOferta.ABIERTA);
        assertThat(creada.publicadorCorreo()).isEqualTo("ana@upb.edu.co");
        assertThat(creada.titulo()).isEqualTo("Imprimir trabajo");
        assertThat(creada.urgente()).isTrue();
        assertThat(creada.fechaPublicacion()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void rechazaConConflictoLaMismaOfertaEnviadaDeNuevoAntesDeUnMinuto() {
        when(ofertas.existsByPublicadorCorreoAndTituloIgnoreCaseAndFechaPublicacionAfter(
                eq("ana@upb.edu.co"), eq("Imprimir trabajo"), any())).thenReturn(true);

        assertThatThrownBy(() -> ofertaService.crear(datos, ana))
                .isInstanceOf(ConflictoNegocioException.class);
        verify(ofertas, never()).save(any());
    }

    @Test
    void buscaDuplicadosSoloEnElUltimoMinuto() {
        when(ofertas.save(any(Oferta.class))).thenAnswer(inv -> inv.getArgument(0));
        LocalDateTime antes = LocalDateTime.now().minus(OfertaService.VENTANA_DUPLICADO);

        ofertaService.crear(datos, ana);

        verify(ofertas).existsByPublicadorCorreoAndTituloIgnoreCaseAndFechaPublicacionAfter(
                anyString(), anyString(), org.mockito.ArgumentMatchers.argThat(desde -> !desde.isBefore(antes)));
    }

    @Test
    void misOfertasDevuelveLasPublicacionesDelUsuarioDeLaSesion() {
        Oferta propia = new Oferta("Tutoria de calculo", "Repasar derivadas", "Llevar apuntes", 20000,
                LocalDateTime.now().plusDays(2), false, "Bloque 10", "ana@upb.edu.co", LocalDateTime.now());
        when(ofertas.findByPublicadorCorreoOrderByFechaPublicacionDesc("ana@upb.edu.co")).thenReturn(List.of(propia));

        List<OfertaDTO> mias = ofertaService.misOfertas(ana);

        assertThat(mias).extracting(OfertaDTO::titulo).containsExactly("Tutoria de calculo");
    }

    // --- UPBCONECT-37 ---

    private Oferta ofertaAbierta(String titulo) {
        return new Oferta(titulo, "Descripcion", "Condiciones", 50000,
                LocalDateTime.now().plusDays(3), false, "Bloque 7",
                "empleador@upb.edu.co", LocalDateTime.now());
    }

    @Test
    void listarActivasDevuelveSoloOfertasAbiertasVigentes() {
        Oferta abierta = ofertaAbierta("Apoyo en feria de emprendimiento");
        when(ofertas.findByEstadoAndFechaLimiteAfter(
                eq(EstadoOferta.ABIERTA), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(abierta)));

        Page<OfertaResumenDTO> resultado = ofertaService.listarActivas(0, 10);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).titulo()).isEqualTo("Apoyo en feria de emprendimiento");
        assertThat(resultado.getContent().get(0).estado()).isEqualTo(EstadoOferta.ABIERTA);
    }

    @Test
    void listarActivasDevuelvePaginaVaciaCuandoNoHayOfertas() {
        when(ofertas.findByEstadoAndFechaLimiteAfter(
                eq(EstadoOferta.ABIERTA), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<OfertaResumenDTO> resultado = ofertaService.listarActivas(0, 10);

        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    void listarActivasLimitaElTamanoDePageA50() {
        when(ofertas.findByEstadoAndFechaLimiteAfter(
                eq(EstadoOferta.ABIERTA), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // Solicitar 200 elementos debe limitarse a 50 internamente
        ofertaService.listarActivas(0, 200);

        verify(ofertas).findByEstadoAndFechaLimiteAfter(
                eq(EstadoOferta.ABIERTA),
                any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.argThat(p -> p.getPageSize() == 50));
    }

    // --- UPBCONECT-41 ---

    private Oferta ofertaDeEmp(LocalDateTime limite) {
        return new Oferta("Titulo", "Actividad", "Condiciones", 1000, limite, false, "Bloque 7",
                "emp@upb.edu.co", LocalDateTime.now());
    }

    @Test
    void detalleIncluyeDatosDelEmpleadorYSiElUsuarioYaSePostulo() {
        when(ofertas.findById(1L)).thenReturn(Optional.of(ofertaDeEmp(LocalDateTime.now().plusDays(1))));
        when(usuarios.findByCorreo("emp@upb.edu.co")).thenReturn(Optional.of(new UsuarioEntity(
                "oid", "emp@upb.edu.co", "Empleador UPB", RolUsuario.EMPLEADOR, "Derecho")));
        when(postulaciones.existsByOfertaIdAndEstudianteCorreo(1L, "ana@upb.edu.co")).thenReturn(true);

        OfertaDetalleDTO detalle = ofertaService.obtenerDetalle(1L, ana);

        assertThat(detalle.publicadorNombre()).isEqualTo("Empleador UPB");
        assertThat(detalle.publicadorCarrera()).isEqualTo("Derecho");
        assertThat(detalle.condiciones()).isEqualTo("Condiciones");
        assertThat(detalle.disponible()).isTrue();
        assertThat(detalle.yaPostulado()).isTrue();
    }

    @Test
    void detalleDeOfertaVencidaNoEstaDisponible() {
        when(ofertas.findById(1L)).thenReturn(Optional.of(ofertaDeEmp(LocalDateTime.now().minusMinutes(1))));
        when(usuarios.findByCorreo("emp@upb.edu.co")).thenReturn(Optional.empty());

        assertThat(ofertaService.obtenerDetalle(1L, ana).disponible()).isFalse();
    }

    @Test
    void detalleFuncionaAunqueElPublicadorNoEsteRegistrado() {
        when(ofertas.findById(1L)).thenReturn(Optional.of(ofertaDeEmp(LocalDateTime.now().plusDays(1))));
        when(usuarios.findByCorreo("emp@upb.edu.co")).thenReturn(Optional.empty());

        OfertaDetalleDTO detalle = ofertaService.obtenerDetalle(1L, ana);

        assertThat(detalle.publicadorNombre()).isEqualTo("emp@upb.edu.co");
        assertThat(detalle.publicadorCarrera()).isNull();
    }

    @Test
    void detalleDeOfertaInexistenteResponde404() {
        when(ofertas.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ofertaService.obtenerDetalle(1L, ana))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- UPBCONECT-44 ---

    @Test
    void actualizarEstadoAFinalizadaDesdeEnProcesoConExito() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1));
        UsuarioEntity estudiante = new UsuarioEntity("oid-est", "est@upb.edu.co", "Estudiante", RolUsuario.ESTUDIANTE, "Sistemas");
        oferta.asignar(estudiante, LocalDateTime.now());
        oferta.ponerEnProceso();
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));

        SesionService.Usuario emp = new SesionService.Usuario("emp@upb.edu.co", "Empleador");
        ActualizarEstadoOfertaDTO dto = new ActualizarEstadoOfertaDTO(EstadoOferta.FINALIZADA);

        OfertaDTO resultado = ofertaService.actualizarEstado(1L, dto, emp);

        assertThat(resultado.estado()).isEqualTo(EstadoOferta.FINALIZADA);
        assertThat(resultado.fechaCierre()).isNotNull();
    }

    @Test
    void actualizarEstadoAEnProcesoConExito() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1));
        UsuarioEntity estudiante = new UsuarioEntity("oid-est", "est@upb.edu.co", "Estudiante", RolUsuario.ESTUDIANTE, "Sistemas");
        oferta.asignar(estudiante, LocalDateTime.now());
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));

        SesionService.Usuario emp = new SesionService.Usuario("emp@upb.edu.co", "Empleador");
        ActualizarEstadoOfertaDTO dto = new ActualizarEstadoOfertaDTO(EstadoOferta.EN_PROCESO);

        OfertaDTO resultado = ofertaService.actualizarEstado(1L, dto, emp);

        assertThat(resultado.estado()).isEqualTo(EstadoOferta.EN_PROCESO);
    }

    @Test
    void estudianteAsignadoPuedePasarAEnProceso() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1));
        UsuarioEntity estudiante = new UsuarioEntity("oid-est", "est@upb.edu.co", "Estudiante", RolUsuario.ESTUDIANTE, "Sistemas");
        oferta.asignar(estudiante, LocalDateTime.now());
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));

        SesionService.Usuario est = new SesionService.Usuario("est@upb.edu.co", "Estudiante");
        ActualizarEstadoOfertaDTO dto = new ActualizarEstadoOfertaDTO(EstadoOferta.EN_PROCESO);

        OfertaDTO resultado = ofertaService.actualizarEstado(1L, dto, est);

        assertThat(resultado.estado()).isEqualTo(EstadoOferta.EN_PROCESO);
    }

    @Test
    void estudianteNoAsignadoNoPuedePasarAEnProceso() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1));
        UsuarioEntity estudiante = new UsuarioEntity("oid-est", "est@upb.edu.co", "Estudiante", RolUsuario.ESTUDIANTE, "Sistemas");
        oferta.asignar(estudiante, LocalDateTime.now());
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));

        SesionService.Usuario otro = new SesionService.Usuario("otro@upb.edu.co", "Otro");

        assertThatThrownBy(() -> ofertaService.actualizarEstado(1L,
                new ActualizarEstadoOfertaDTO(EstadoOferta.EN_PROCESO), otro))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void estudianteAsignadoPuedeFinalizar() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1));
        UsuarioEntity estudiante = new UsuarioEntity("oid-est", "est@upb.edu.co", "Estudiante", RolUsuario.ESTUDIANTE, "Sistemas");
        oferta.asignar(estudiante, LocalDateTime.now());
        oferta.ponerEnProceso();
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));

        SesionService.Usuario est = new SesionService.Usuario("est@upb.edu.co", "Estudiante");

        OfertaDTO resultado = ofertaService.actualizarEstado(1L,
                new ActualizarEstadoOfertaDTO(EstadoOferta.FINALIZADA), est);

        assertThat(resultado.estado()).isEqualTo(EstadoOferta.FINALIZADA);
        assertThat(resultado.fechaCierre()).isNotNull();
    }

    @Test
    void estudianteNoAsignadoNoPuedeFinalizar() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1));
        UsuarioEntity estudiante = new UsuarioEntity("oid-est", "est@upb.edu.co", "Estudiante", RolUsuario.ESTUDIANTE, "Sistemas");
        oferta.asignar(estudiante, LocalDateTime.now());
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));

        SesionService.Usuario otro = new SesionService.Usuario("otro@upb.edu.co", "Otro");

        assertThatThrownBy(() -> ofertaService.actualizarEstado(1L,
                new ActualizarEstadoOfertaDTO(EstadoOferta.FINALIZADA), otro))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void actualizarEstadoAFinalizadaConExito() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1));
        UsuarioEntity estudiante = new UsuarioEntity("oid-est", "est@upb.edu.co", "Estudiante", RolUsuario.ESTUDIANTE, "Sistemas");
        oferta.asignar(estudiante, LocalDateTime.now());
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));

        SesionService.Usuario emp = new SesionService.Usuario("emp@upb.edu.co", "Empleador");
        ActualizarEstadoOfertaDTO dto = new ActualizarEstadoOfertaDTO(EstadoOferta.FINALIZADA);

        OfertaDTO resultado = ofertaService.actualizarEstado(1L, dto, emp);

        assertThat(resultado.estado()).isEqualTo(EstadoOferta.FINALIZADA);
        assertThat(resultado.fechaCierre()).isNotNull();
        assertThat(resultado.estudianteAsignadoCorreo()).isEqualTo("est@upb.edu.co");
    }

    @Test
    void actualizarEstadoOfertaInexistenteLanza404() {
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ofertaService.actualizarEstado(1L,
                new ActualizarEstadoOfertaDTO(EstadoOferta.FINALIZADA), ana))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void actualizarEstadoNoDuenoLanza403() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1));
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));

        assertThatThrownBy(() -> ofertaService.actualizarEstado(1L,
                new ActualizarEstadoOfertaDTO(EstadoOferta.FINALIZADA), ana))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void actualizarEstadoDesdeAbiertaLanza409() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1)); // estado ABIERTA
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));
        SesionService.Usuario emp = new SesionService.Usuario("emp@upb.edu.co", "Empleador");

        assertThatThrownBy(() -> ofertaService.actualizarEstado(1L,
                new ActualizarEstadoOfertaDTO(EstadoOferta.FINALIZADA), emp))
                .isInstanceOf(ConflictoNegocioException.class)
                .hasMessageContaining("ASIGNADA");
    }

    @Test
    void actualizarEstadoYaFinalizadaLanza409() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1));
        UsuarioEntity estudiante = new UsuarioEntity("oid-est", "est@upb.edu.co", "Estudiante", RolUsuario.ESTUDIANTE, "Sistemas");
        oferta.asignar(estudiante, LocalDateTime.now());
        oferta.finalizar(LocalDateTime.now());
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));
        SesionService.Usuario emp = new SesionService.Usuario("emp@upb.edu.co", "Empleador");

        assertThatThrownBy(() -> ofertaService.actualizarEstado(1L,
                new ActualizarEstadoOfertaDTO(EstadoOferta.FINALIZADA), emp))
                .isInstanceOf(ConflictoNegocioException.class)
                .hasMessageContaining("ya se encuentra finalizada");
    }

    @Test
    void actualizarEstadoTransicionNoPermitidaLanza409() {
        Oferta oferta = ofertaDeEmp(LocalDateTime.now().plusDays(1));
        UsuarioEntity estudiante = new UsuarioEntity("oid-est", "est@upb.edu.co", "Estudiante", RolUsuario.ESTUDIANTE, "Sistemas");
        oferta.asignar(estudiante, LocalDateTime.now());
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));
        SesionService.Usuario emp = new SesionService.Usuario("emp@upb.edu.co", "Empleador");

        assertThatThrownBy(() -> ofertaService.actualizarEstado(1L,
                new ActualizarEstadoOfertaDTO(EstadoOferta.ABIERTA), emp))
                .isInstanceOf(ConflictoNegocioException.class)
                .hasMessageContaining("no permitida");
    }

    @Test
    void misTrabajosAsignadosConsultaEstadosAsignadaEnProcesoYFinalizada() {
        when(ofertas.findByEstudianteAsignadoCorreoAndEstadoInOrderByFechaAsignacionDesc(
                eq("ana@upb.edu.co"), eq(List.of(EstadoOferta.ASIGNADA, EstadoOferta.EN_PROCESO, EstadoOferta.FINALIZADA))))
                .thenReturn(List.of());

        List<OfertaDTO> resultado = ofertaService.misTrabajosAsignados(ana);

        assertThat(resultado).isEmpty();
        verify(ofertas).findByEstudianteAsignadoCorreoAndEstadoInOrderByFechaAsignacionDesc(
                eq("ana@upb.edu.co"), eq(List.of(EstadoOferta.ASIGNADA, EstadoOferta.EN_PROCESO, EstadoOferta.FINALIZADA)));
    }
}
