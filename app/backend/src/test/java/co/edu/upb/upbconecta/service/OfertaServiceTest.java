package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.dto.OfertaCreateDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.repository.OfertaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Reglas de OfertaService (UPBCONECT-39), sin base de datos: el repositorio es simulado. */
@ExtendWith(MockitoExtension.class)
class OfertaServiceTest {

    @Mock
    OfertaRepository ofertas;

    @InjectMocks
    OfertaService ofertaService;

    @Test
    void creaLaOfertaAbiertaANombreDelUsuarioDeLaSesion() {
        when(ofertas.save(any(Oferta.class))).thenAnswer(inv -> inv.getArgument(0));
        OfertaCreateDTO datos = new OfertaCreateDTO("  Imprimir trabajo  ", "Imprimir 20 hojas", "Antes de las 10",
                5000, LocalDateTime.now().plusDays(1), true, "Bloque 11");

        OfertaDTO creada = ofertaService.crear(datos, new SesionService.Usuario("ana@upb.edu.co", "Ana"));

        assertThat(creada.estado()).isEqualTo(EstadoOferta.ABIERTA);
        assertThat(creada.publicadorCorreo()).isEqualTo("ana@upb.edu.co");
        assertThat(creada.titulo()).isEqualTo("Imprimir trabajo");
        assertThat(creada.urgente()).isTrue();
        assertThat(creada.fechaPublicacion()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
