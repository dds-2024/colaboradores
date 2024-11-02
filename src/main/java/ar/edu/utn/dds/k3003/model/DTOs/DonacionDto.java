package ar.edu.utn.dds.k3003.model.DTOs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class DonacionDto {
    private double monto; // Monto donado
    private LocalDate fecha; // Fecha de la donación
    //private Long colaboradorId; // ID del colaborador que realizó la donación

    @JsonCreator // Indica que este constructor se usará para deserializar
    public DonacionDto(
            @JsonProperty("monto") double monto,
            @JsonProperty("fecha") LocalDate fecha) {
        this.monto = monto;
        this.fecha = fecha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DonacionDto)) return false;
        DonacionDto that = (DonacionDto) o;
        return Double.compare(that.monto, monto) == 0 &&
                fecha.equals(that.fecha);
    }


    @Override
    public int hashCode() {
        int result = Double.hashCode(monto);
        result = 31 * result + fecha.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DonacionDTO{" +
                "monto=" + monto +
                ", fecha=" + fecha +
                '}';
    }
}