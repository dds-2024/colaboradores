package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.TipoFormaColaborar;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateFormasColaborarRequest {
  private List<TipoFormaColaborar> formas;

  public UpdateFormasColaborarRequest() {
  }

}
