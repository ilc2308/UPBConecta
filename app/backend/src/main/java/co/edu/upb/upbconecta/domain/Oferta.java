package co.edu.upb.upbconecta.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Trabajo publicado en la plataforma (tabla "oferta").
 * Cualquier usuario autenticado puede publicar; no hay roles.
 * La usan tambien el listado del Dashboard (UPBCONECT-37), la busqueda (38) y la postulacion (41).
 */
@Entity
@Table(name = "oferta", indexes = @Index(name = "idx_oferta_estado_fecha", columnList = "estado, fecha_publicacion"))
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String actividad;

    @Column(nullable = false, length = 1000)
    private String condiciones;

    /** Valor en pesos colombianos. */
    @Column(name = "valor_ofrecido", nullable = false)
    private Integer valorOfrecido;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDateTime fechaLimite;

    @Column(nullable = false)
    private boolean urgente;

    @Column(nullable = false, length = 150)
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOferta estado;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    // TODO(UPBCONECT-5): reemplazar por una FK a usuario.id cuando exista la entidad Usuario.
    @Column(name = "publicador_correo", nullable = false, length = 150)
    private String publicadorCorreo;

    /** Requerido por JPA. */
    protected Oferta() {
    }

    public Oferta(String titulo, String actividad, String condiciones, Integer valorOfrecido,
                  LocalDateTime fechaLimite, boolean urgente, String ubicacion,
                  String publicadorCorreo, LocalDateTime fechaPublicacion) {
        this.titulo = titulo;
        this.actividad = actividad;
        this.condiciones = condiciones;
        this.valorOfrecido = valorOfrecido;
        this.fechaLimite = fechaLimite;
        this.urgente = urgente;
        this.ubicacion = ubicacion;
        this.publicadorCorreo = publicadorCorreo;
        this.fechaPublicacion = fechaPublicacion;
        this.estado = EstadoOferta.ABIERTA;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getActividad() { return actividad; }
    public String getCondiciones() { return condiciones; }
    public Integer getValorOfrecido() { return valorOfrecido; }
    public LocalDateTime getFechaLimite() { return fechaLimite; }
    public boolean isUrgente() { return urgente; }
    public String getUbicacion() { return ubicacion; }
    public EstadoOferta getEstado() { return estado; }
    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public String getPublicadorCorreo() { return publicadorCorreo; }
}
