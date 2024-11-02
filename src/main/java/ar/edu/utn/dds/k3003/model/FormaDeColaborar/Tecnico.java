package ar.edu.utn.dds.k3003.model.FormaDeColaborar;

import ar.edu.utn.dds.k3003.model.Colaborador;
import ar.edu.utn.dds.k3003.model.Serializer.FormaDeColaborarSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = FormaDeColaborarSerializer.class)
public class Tecnico implements FormaDeColaborar {
    private double peso = 5;

    @Override
    public double calcularPuntos(Colaborador colaborador) {
        return peso * colaborador.getHeladerasReparadas();
    }

    @Override
    public void setPesoPuntaje(Double peso) {
        this.peso = peso;
    }
}
