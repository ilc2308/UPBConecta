package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.dto.OfertaCreateDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaResumenDTO;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.repository.OfertaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de OfertaService (UPBCONECT-39, 40 y 37), sin base de datos: el repositorio es simulado. */
@ExtendWith(MockitoExtension.class)
class OfertaServiceTest {

    @Mock
    OfertaRepository ofertas;

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
}
