package ar.edu.utn.dds.k3003.model.Incidentes;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Incidente {
    private Long heladeraId;
    private TipoSuscripcion tipoAlerta;
    private LocalDateTime fecha;

    public Incidente(Long heladeraId, TipoSuscripcion tipoAlerta){
        this.heladeraId = heladeraId;
        this.fecha = LocalDateTime.now();
        this.tipoAlerta = tipoAlerta;
    }
}