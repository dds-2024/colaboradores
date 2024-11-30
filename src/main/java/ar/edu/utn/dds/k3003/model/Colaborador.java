package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.model.DTOs.DonacionDto;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborar;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborarConverter;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborarUtil;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.TipoFormaColaborar;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "colaboradores")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Colaborador {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "chat_id")
  private Long chat_id;

  @Column(name = "nombre")
  private String nombre;

  @Convert(converter = FormaDeColaborarConverter.class) // Usar el convertidor
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "colaborador_formas", joinColumns = @JoinColumn(name = "colaborador_id"))
  private List<FormaDeColaborar> formas = new ArrayList<>();

  @ElementCollection(targetClass = Donacion.class, fetch = FetchType.EAGER)
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY) // Agregar cascade
  @JoinColumn(name = "colaborador_id")
  private List<Donacion> donaciones = new ArrayList<>();

  @Column
  private Long heladerasReparadas;

  @Transient
  private  Long viandasRepartidas;

  @Transient
  private  Long viandasTransportadas;


  @PreRemove
  private void preRemove() {
    this.formas.clear();
  }

  public double totalDonaciones() {
    double total = 0;
    if (donaciones != null) {
      for (Donacion donacion : donaciones) {
        total += donacion.getMonto();
      }
    }
    return total;
  }


  public double calcularPuntajeTotal() {
    return this.getFormas().stream()
            .mapToDouble(forma -> forma.calcularPuntos(this))
            .sum();
  }
}