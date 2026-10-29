package co.edu.upb.upbconecta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class UsuarioEntity {

    @Id
    private UUID id;

    @Column(name = "microsoft_oid", nullable = false, unique = true, length = 100)
    private String microsoftOid;

    @Column(nullable = false, unique = true, length = 254)
    private String correo;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Column(nullable = false, length = 120)
    private String carrera;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private Instant fechaRegistro;

    @Column(nullable = false)
    private boolean activo;

    protected UsuarioEntity() {}

    public UsuarioEntity(String microsoftOid, String correo, String nombre,
                         RolUsuario rol, String carrera) {
        this.microsoftOid = microsoftOid;
        this.correo = correo;
        this.nombre = nombre;
        this.rol = rol;
        this.carrera = carrera;
        this.activo = true;
    }

    @PrePersist
    void alCrear() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (fechaRegistro == null) {
            fechaRegistro = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public String getMicrosoftOid() { return microsoftOid; }
    public String getCorreo() { return correo; }
    public String getNombre() { return nombre; }
    public RolUsuario getRol() { return rol; }
    public String getCarrera() { return carrera; }
    public Instant getFechaRegistro() { return fechaRegistro; }
    public boolean isActivo() { return activo; }
}
