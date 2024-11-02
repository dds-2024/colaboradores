package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.model.Colaborador;
import ar.edu.utn.dds.k3003.model.DTOs.ColaboradorDto;
import ar.edu.utn.dds.k3003.model.DTOs.DonacionDto;
import ar.edu.utn.dds.k3003.model.Donacion;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborar;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborarUtil;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.TipoFormaColaborar;

import java.util.List;
import java.util.stream.Collectors;

public class ColaboradorMapper {
  /*public ColaboradorDTO map(Colaborador colaborador){
    ColaboradorDTO colaboradorDTO = new ColaboradorDTO(colaborador.getNombre(),colaborador.getFormas().stream().toList());
    colaboradorDTO.setId(colaborador.getId());
    return colaboradorDTO;
  }*/

  public ColaboradorDto map(Colaborador colaborador) {
    // Mapea las donaciones
    List<DonacionDto> donacionDTOs = colaborador.getDonaciones()
            .stream()
            .map(this::mapDonacionDto)
            .collect(Collectors.toList());

    List<TipoFormaColaborar> tiposFormas = FormaDeColaborarUtil.convertToTipoFormaColaborarList(colaborador.getFormas());


    ColaboradorDto colaboradorDTO = new ColaboradorDto(
            colaborador.getNombre(),
            tiposFormas
    );

    if(!donacionDTOs.isEmpty()){
      colaboradorDTO.agregarDonaciones(donacionDTOs);
    }
    colaboradorDTO.setId(colaborador.getId());
    colaboradorDTO.setHeladerasReparadas(colaborador.getHeladerasReparadas());
    return colaboradorDTO;
  }

  public Donacion mapDonacion(DonacionDto donacionDTO) {
    Donacion donacion = new Donacion();
    donacion.setMonto(donacionDTO.getMonto());
    donacion.setFecha(donacionDTO.getFecha());
    return donacion;
  }

  public DonacionDto mapDonacionDto(Donacion donacion) {
    return new DonacionDto(donacion.getMonto(), donacion.getFecha());
  }

}
