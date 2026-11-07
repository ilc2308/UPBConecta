package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.EstadoPostulacion;
import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.domain.Postulacion;
import co.edu.upb.upbconecta.dto.PostulanteDTO;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de postulacion (UPBCONECT-41), sin base de datos. */
@ExtendWith(MockitoExtension.class)
class PostulacionServiceTest {

    @Mock OfertaRepository ofertas;
    @Mock PostulacionRepository postulaciones;
    @Mock UsuarioRepository usuarios;
    @InjectMocks PostulacionService servicio;

    private final SesionService.Usuario sesionEstudiante = new SesionService.Usuario("ana@upb.edu.co", "Ana");
    private final SesionService.Usuario sesionDueno = new SesionService.Usuario("emp@upb.edu.co", "Emp");
    private final UsuarioEntity ana = usuario("ana@upb.edu.co", RolUsuario.ESTUDIANTE);

    private static UsuarioEntity usuario(String correo, RolUsuario rol) {
        UsuarioEntity u = new UsuarioEntity("oid-" + correo, correo, correo, rol, "Sistemas");
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
        return u;
    }

    private Oferta oferta(LocalDateTime limite) {
        return new Oferta("Titulo", "Actividad", "Condiciones", 1000, limite, false, "Bloque 7",
                "emp@upb.edu.co", LocalDateTime.now());
    }

    private Oferta abierta() {
        return oferta(LocalDateTime.now().plusDays(1));
    }

    private void conOferta(Oferta oferta) {
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.of(oferta));
    }

    // --- UPBCONECT-41 ---

    @Test
    void creaLaPostulacionPendiente() {
        conOferta(abierta());
        when(usuarios.findByCorreo("ana@upb.edu.co")).thenReturn(Optional.of(ana));
        when(postulaciones.save(any(Postulacion.class))).thenAnswer(inv -> inv.getArgument(0));

        PostulanteDTO creada = servicio.postular(1L, sesionEstudiante);

        assertThat(creada.estado()).isEqualTo(EstadoPostulacion.PENDIENTE);
        assertThat(creada.correo()).isEqualTo("ana@upb.edu.co");
    }

    @Test
    void ofertaInexistenteResponde404() {
        when(ofertas.findConBloqueoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.postular(1L, sesionEstudiante))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void unEmpleadorNoPuedePostularse() {
        conOferta(abierta());
        when(usuarios.findByCorreo("ana@upb.edu.co"))
                .thenReturn(Optional.of(usuario("ana@upb.edu.co", RolUsuario.EMPLEADOR)));

        assertThatThrownBy(() -> servicio.postular(1L, sesionEstudiante))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(postulaciones, never()).save(any());
    }

    @Test
    void unUsuarioSinCuentaRegistradaNoPuedePostularse() {
        conOferta(abierta());
        when(usuarios.findByCorreo("ana@upb.edu.co")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.postular(1L, sesionEstudiante))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void noSePuedePostularAUnaOfertaPropia() {
        conOferta(abierta());
        when(usuarios.findByCorreo("emp@upb.edu.co"))
                .thenReturn(Optional.of(usuario("emp@upb.edu.co", RolUsuario.ESTUDIANTE)));

        assertThatThrownBy(() -> servicio.postular(1L, sesionDueno))
                .isInstanceOf(ConflictoNegocioException.class)
                .hasMessageContaining("propia");
    }

    @Test
    void noSePuedePostularAUnaOfertaCerrada() {
        Oferta cerrada = abierta();
        ReflectionTestUtils.setField(cerrada, "estado", EstadoOferta.ASIGNADA);
        conOferta(cerrada);
        when(usuarios.findByCorreo("ana@upb.edu.co")).thenReturn(Optional.of(ana));

        assertThatThrownBy(() -> servicio.postular(1L, sesionEstudiante))
                .isInstanceOf(ConflictoNegocioException.class)
                .hasMessageContaining("disponible");
    }

    @Test
    void noSePuedePostularAUnaOfertaVencida() {
        conOferta(oferta(LocalDateTime.now().minusMinutes(1)));
        when(usuarios.findByCorreo("ana@upb.edu.co")).thenReturn(Optional.of(ana));

        assertThatThrownBy(() -> servicio.postular(1L, sesionEstudiante))
                .isInstanceOf(ConflictoNegocioException.class)
                .hasMessageContaining("disponible");
    }

    @Test
    void noSePuedePostularDosVeces() {
        conOferta(abierta());
        when(usuarios.findByCorreo("ana@upb.edu.co")).thenReturn(Optional.of(ana));
        when(postulaciones.existsByOfertaIdAndEstudianteId(1L, ana.getId())).thenReturn(true);

        assertThatThrownBy(() -> servicio.postular(1L, sesionEstudiante))
                .isInstanceOf(ConflictoNegocioException.class)
                .hasMessageContaining("Ya te postulaste");
        verify(postulaciones, never()).save(any());
    }
}
