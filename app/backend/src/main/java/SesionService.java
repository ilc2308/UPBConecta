package co.edu.upb.upbconecta.controller;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Sesiones en memoria. Suficiente para el modo demo; se pierden al reiniciar. */
@Service
public class SesionService {

    public record Usuario(String correo, String nombre) {}

    private final Map<String, Usuario> sesiones = new ConcurrentHashMap<>();

    public String crear(Usuario usuario) {
        String id = UUID.randomUUID().toString();
        sesiones.put(id, usuario);
        return id;
    }

    public Optional<Usuario> buscar(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(sesiones.get(id));
    }

    public void eliminar(String id) {
        if (id != null) {
            sesiones.remove(id);
        }
    }
}
