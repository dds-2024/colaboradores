package ar.edu.utn.dds.k3003.model.DTOs;

import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborar;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborarUtil;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.TipoFormaColaborar;
import ar.edu.utn.dds.k3003.model.Serializer.ColaboradorDtoDeserializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = ColaboradorDtoDeserializer.class)
@Getter
@Setter
public class ColaboradorDto {
    private Long id;
    private Long chat_id;
    private String nombre;
    private List<FormaDeColaborar> formas;
    private List<DonacionDto> donaciones;
    private Long heladerasReparadas;

    @JsonCreator
    public ColaboradorDto(@JsonProperty("nombre") String nombre,
                          @JsonProperty("chat_id") Long chat_id,
                          @JsonProperty("formas") List<TipoFormaColaborar> formas) {
        this.nombre = nombre;
        this.chat_id = chat_id;
        this.donaciones = new ArrayList<>();
        this.formas = new ArrayList<>();
        this.heladerasReparadas = 0L;
        FormaDeColaborarUtil factory = new FormaDeColaborarUtil();
        for (TipoFormaColaborar formaColab : formas) {
            this.formas.add(FormaDeColaborarUtil.createFormaDeColaborar(formaColab));
        }
    }

    public void agregarDonacion(DonacionDto donacion) {
        if (donacion != null) {
            this.donaciones.add(donacion);
        }
    }

    public void agregarDonaciones(List<DonacionDto> donaciones) {
        if (donaciones != null) {
            this.donaciones.addAll(donaciones);
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ColaboradorDto)) {
            return false;
        } else {
            ColaboradorDto other = (ColaboradorDto) o;
            if (!equalsHelper(this.getId(), other.getId())) return false;
            if (!equalsHelper(this.getNombre(), other.getNombre())) return false;
            if (!equalsHelper(this.getFormas(), other.getFormas())) return false;
            return equalsHelper(this.getDonaciones(), other.getDonaciones()); // Comparar donaciones
        }
    }

    private boolean equalsHelper(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (getId() == null ? 43 : getId().hashCode());
        result = result * 59 + (getChat_id() == null ? 43 : getChat_id().hashCode());
        result = result * 59 + (getNombre() == null ? 43 : getNombre().hashCode());
        result = result * 59 + (getFormas() == null ? 43 : getFormas().hashCode());
        result = result * 59 + (getDonaciones() == null ? 43 : getDonaciones().hashCode()); // Calcular hash para donaciones
        return result;
    }

    public String toString() {
        return "ColaboradorDTO(id=" + getId() +
                ", chatId=" + getChat_id() +
                ", nombre=" + getNombre() +
                ", formas=" + getFormas() +
                ", donaciones=" + getDonaciones() + ")"; // Incluir donaciones en la representación de cadena
    }

    protected ColaboradorDto() {
    }
}