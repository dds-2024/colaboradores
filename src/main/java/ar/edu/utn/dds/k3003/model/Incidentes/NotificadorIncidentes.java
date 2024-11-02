package ar.edu.utn.dds.k3003.model.Incidentes;

import ar.edu.utn.dds.k3003.model.DTOs.ColaboradorDto;

public class NotificadorIncidentes {
    public void notificar(Incidente incidente, ColaboradorDto colaboradorDto) {
        String mensaje = construirMensajeDeAlerta(incidente, colaboradorDto);
        System.out.print(mensaje);
    }

    private String construirMensajeDeAlerta(Incidente incidente, ColaboradorDto colaboradorDTO) {
        return String.format("Se notifica al colaborador %d que la heladera %d sufrió un incidente de tipo %s%n",
                colaboradorDTO.getId(),
                incidente.getHeladeraId(),
                incidente.getTipoAlerta());
    }
}
