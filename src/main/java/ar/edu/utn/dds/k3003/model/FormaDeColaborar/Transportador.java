package ar.edu.utn.dds.k3003.model.FormaDeColaborar;

import ar.edu.utn.dds.k3003.model.Colaborador;
import ar.edu.utn.dds.k3003.model.DTOs.FormaDeColaborarSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = FormaDeColaborarSerializer.class)
public class Transportador implements FormaDeColaborar {
    private double peso = 2;

    @Override
    public double calcularPuntos(Colaborador colaborador) {
        return peso * colaborador.getViandasRepartidas();
    }

    @Override
    public void setPesoPuntaje(Double peso) {
        this.peso = peso;
    }
}
