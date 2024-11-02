package ar.edu.utn.dds.k3003.model.Incidentes;

import lombok.Getter;

@Getter
public class SuscripcionDTO {
    private Integer colaboradorId;
    private Integer heladeraId;
    private Integer cantidadViandas;
    private TipoSuscripcion tipoSuscripcion;
}
