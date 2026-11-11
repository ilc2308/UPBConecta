package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.dto.OfertaResumenDTO;
import co.edu.upb.upbconecta.repository.OfertaRepository;
import co.edu.upb.upbconecta.repository.PostulacionRepository;
import co.edu.upb.upbconecta.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de busqueda por palabra clave en OfertaService (UPBCONECT-38).
 * El repositorio es simulado con Mockito; no se necesita base de datos.
 */
@ExtendWith(MockitoExtension.class)
class OfertaServiceSearchTest {

    @Mock
    OfertaRepository ofertas;

    @Mock
    UsuarioRepository usuarios;

    @Mock
    PostulacionRepository postulaciones;

    @InjectMocks
    OfertaService ofertaService;

    private Oferta ofertaAbierta(String titulo) {
        return new Oferta(titulo, "Descripcion", "Condiciones", 50000,
                LocalDateTime.now().plusDays(3), false, "Bloque 7",
                "empleador@upb.edu.co", LocalDateTime.now());
    }

    // --- UPBCONECT-38: buscarActivas ---

    @Test
    @DisplayName("buscarActivas con coincidencia devuelve las ofertas que coinciden")
    void buscarActivasConCoincidenciaDevuelvePaginaConResultados() {
        Oferta oferta = ofertaAbierta("Desarrollador Java");
        when(ofertas.buscarActivas(
                eq(EstadoOferta.ABIERTA),
                any(LocalDateTime.class),
                any(String.class),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(oferta)));

        Page<OfertaResumenDTO> resultado = ofertaService.buscarActivas("java", 0, 10);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).titulo()).isEqualTo("Desarrollador Java");
    }

    @Test
    @DisplayName("buscarActivas sin coincidencia devuelve pagina vacia")
    void buscarActivasSinCoincidenciaDevuelvePaginaVacia() {
        when(ofertas.buscarActivas(
                eq(EstadoOferta.ABIERTA),
                any(LocalDateTime.class),
                any(String.class),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<OfertaResumenDTO> resultado = ofertaService.buscarActivas("xyzabc", 0, 10);

        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    @DisplayName("buscarActivas con keyword menor a 2 caracteres llama a listarActivas (fallback)")
    void buscarActivasConKeywordCortaUsaFallbackAListarActivas() {
        Oferta o1 = ofertaAbierta("Tutoria de algebra");
        Oferta o2 = ofertaAbierta("Apoyo en laboratorio");
        when(ofertas.findByEstadoAndFechaLimiteAfter(
                eq(EstadoOferta.ABIERTA),
                any(LocalDateTime.class),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(o1, o2)));

        Page<OfertaResumenDTO> resultado = ofertaService.buscarActivas("a", 0, 10);

        assertThat(resultado.getContent()).hasSize(2);
        // Verifica que se usa el metodo sin patron (listarActivas) y no buscarActivas del repo
        verify(ofertas).findByEstadoAndFechaLimiteAfter(
                eq(EstadoOferta.ABIERTA),
                any(LocalDateTime.class),
                any(Pageable.class));
    }

    @Test
    @DisplayName("buscarActivas escapa correctamente los comodines LIKE en el patron")
    void buscarActivasEscapaComodinesEnElPatron() {
        // "50% descuento" -> sinTildes -> "50% descuento" -> escaparLike -> "50\% descuento" -> patron "%50\% descuento%"
        String patronEsperado = "%50\\% descuento%";
        when(ofertas.buscarActivas(
                eq(EstadoOferta.ABIERTA),
                any(LocalDateTime.class),
                eq(patronEsperado),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<OfertaResumenDTO> resultado = ofertaService.buscarActivas("50% descuento", 0, 10);

        assertThat(resultado).isNotNull();
        verify(ofertas).buscarActivas(
                eq(EstadoOferta.ABIERTA),
                any(LocalDateTime.class),
                eq(patronEsperado),
                any(Pageable.class));
    }

    @Test
    @DisplayName("buscarActivas trunca el keyword a 100 caracteres")
    void buscarActivasTruncaKeywordLargo() {
        String keywordLargo = "a".repeat(200);
        when(ofertas.buscarActivas(
                eq(EstadoOferta.ABIERTA),
                any(LocalDateTime.class),
                any(String.class),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // No debe lanzar excepcion; el patron interno tendra <= 102 chars (% + 100 + %)
        Page<OfertaResumenDTO> resultado = ofertaService.buscarActivas(keywordLargo, 0, 10);

        assertThat(resultado).isNotNull();
        verify(ofertas).buscarActivas(
                eq(EstadoOferta.ABIERTA),
                any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.argThat(patron -> patron.length() <= 102),
                any(Pageable.class));
    }
}
