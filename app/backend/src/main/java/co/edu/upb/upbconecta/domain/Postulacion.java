package co.edu.upb.upbconecta.domain;

import co.edu.upb.upbconecta.model.UsuarioEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/** Postulacion de un estudiante a una oferta (UPBCONECT-41). Un estudiante solo puede postularse una vez. */
@Entity
@Table(name = "postulacion", uniqueConstraints = @UniqueConstraint(
        name = "uk_postulacion_oferta_estudiante", columnNames = {"oferta_id", "estudiante_id"}))
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false)
    private Oferta oferta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private UsuarioEntity estudiante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPostulacion estado;

    @Column(name = "fecha_postulacion", nullable = false)
    private LocalDateTime fechaPostulacion;

    /** Requerido por JPA. */
    protected Postulacion() {
    }

    public Postulacion(Oferta oferta, UsuarioEntity estudiante, LocalDateTime fechaPostulacion) {
        this.oferta = oferta;
        this.estudiante = estudiante;
        this.fechaPostulacion = fechaPostulacion;
        this.estado = EstadoPostulacion.PENDIENTE;
    }

    public Long getId() { return id; }
    public Oferta getOferta() { return oferta; }
    public UsuarioEntity getEstudiante() { return estudiante; }
    public EstadoPostulacion getEstado() { return estado; }
    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }
}
