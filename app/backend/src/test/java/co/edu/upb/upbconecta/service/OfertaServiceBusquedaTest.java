package co.edu.upb.upbconecta.service;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.dto.OfertaResumenDTO;
import co.edu.upb.upbconecta.repository.OfertaRepository;
import co.edu.upb.upbconecta.repository.PostulacionRepository;
import co.edu.upb.upbconecta.repository.UsuarioRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Busqueda por palabra clave (UPBCONECT-38), sin base de datos. */
@ExtendWith(MockitoExtension.class)
class OfertaServiceBusquedaTest {

    @Mock
    private OfertaRepository ofertas;
    @Mock
    private UsuarioRepository usuarios;
    @Mock
    private PostulacionRepository postulaciones;

    private OfertaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new OfertaService(ofertas, usuarios, postulaciones);
    }

    private Oferta oferta() {
        Oferta o = new Oferta("Tutoria de calculo", "Ayuda para estudiar un parcial",
                "Presencial en la biblioteca", 50000, LocalDateTime.now().plusDays(2), false,
                "Biblioteca", "emp@upb.edu.co", LocalDateTime.now());
        ReflectionTestUtils.setField(o, "id", 1L);
        return o;
    }

    @Test
    void conPalabraClaveDevuelveLosResultadosSinImportarMayusculasNiTildes() {
        when(ofertas.buscarActivas(eq(EstadoOferta.ABIERTA), any(LocalDateTime.class),
                eq("%tutoria%"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(oferta())));

        Page<OfertaResumenDTO> resultado = servicio.buscarActivas("TUTORÍA", 0, 10);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
    }

    @Test
    void sinCoincidenciasDevuelveUnaPaginaVacia() {
        when(ofertas.buscarActivas(eq(EstadoOferta.ABIERTA), any(LocalDateTime.class),
                eq("%zzzz%"), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThat(servicio.buscarActivas("zzzz", 0, 10).getTotalElements()).isZero();
    }

    @Test
    void conMenosDeDosCaracteresDevuelveElListadoNormal() {
        when(ofertas.findByEstadoAndFechaLimiteAfter(eq(EstadoOferta.ABIERTA),
                any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(oferta())));

        Page<OfertaResumenDTO> resultado = servicio.buscarActivas("  a ", 0, 10);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        verify(ofertas, never()).buscarActivas(any(), any(), any(), any());
    }

    @Test
    void recortaLaPalabraClaveA100Caracteres() {
        when(ofertas.buscarActivas(eq(EstadoOferta.ABIERTA), any(LocalDateTime.class),
                any(String.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        servicio.buscarActivas("a".repeat(150), 0, 10);

        ArgumentCaptor<String> patron = ArgumentCaptor.forClass(String.class);
        verify(ofertas).buscarActivas(eq(EstadoOferta.ABIERTA), any(LocalDateTime.class),
                patron.capture(), any(Pageable.class));
        assertThat(patron.getValue()).isEqualTo("%" + "a".repeat(100) + "%");
    }

    @Test
    void escapaLosComodinesDeLike() {
        when(ofertas.buscarActivas(eq(EstadoOferta.ABIERTA), any(LocalDateTime.class),
                any(String.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        servicio.buscarActivas("50%_x", 0, 10);

        ArgumentCaptor<String> patron = ArgumentCaptor.forClass(String.class);
        verify(ofertas).buscarActivas(eq(EstadoOferta.ABIERTA), any(LocalDateTime.class),
                patron.capture(), any(Pageable.class));
        assertThat(patron.getValue()).isEqualTo("%50\\%\\_x%");
    }
}