package co.edu.upb.upbconecta.domain;

/** Ciclo de vida de una oferta: Publicada (ABIERTA) -> Asignada -> En proceso -> Finalizada. */
public enum EstadoOferta {
    ABIERTA,
    ASIGNADA,
    EN_PROCESO,
    FINALIZADA,
    CANCELADA
}
