package ar.edu.utn.dds.k3003.model.FormaDeColaborar;

import ar.edu.utn.dds.k3003.model.Colaborador;

public interface FormaDeColaborar {
    double calcularPuntos(Colaborador colaborador);
    void setPesoPuntaje(Double peso);
}