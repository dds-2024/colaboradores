package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaLogistica;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;

import ar.edu.utn.dds.k3003.facades.dtos.TrasladoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import ar.edu.utn.dds.k3003.model.CoeficientesPuntos;
import ar.edu.utn.dds.k3003.model.Colaborador;
import ar.edu.utn.dds.k3003.model.DTOs.ColaboradorDto;
import ar.edu.utn.dds.k3003.model.DTOs.DonacionDto;
import ar.edu.utn.dds.k3003.model.Donacion;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborar;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborarConverter;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborarUtil;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.TipoFormaColaborar;
import ar.edu.utn.dds.k3003.model.TipoCoeficiente;
import ar.edu.utn.dds.k3003.repositories.ColaboradorMapper;
import ar.edu.utn.dds.k3003.repositories.ColaboradorRepository;
import lombok.Getter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

public class Fachada {
  @Getter
  private ColaboradorRepository colaboradorRepository;
  private ColaboradorMapper colaboradorMapper;
  private FachadaViandas fachadaViandas;
  private FachadaLogistica fachadaLogistica;
  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;

  public Fachada() {
    this.entityManagerFactory = Persistence.createEntityManagerFactory("bd_colaboradores_f5t3");
    this.entityManager = entityManagerFactory.createEntityManager();
    this.colaboradorRepository = new ColaboradorRepository();
    this.colaboradorRepository.setEntityManager(entityManager);
    this.colaboradorMapper = new ColaboradorMapper();
  }

  public Fachada(ColaboradorRepository _colaboradorRepository, ColaboradorMapper _colaboradorMapper, EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
    this.colaboradorRepository = _colaboradorRepository;
    colaboradorRepository.setEntityManagerFactory(entityManagerFactory);
    colaboradorRepository.setEntityManager(entityManagerFactory.createEntityManager());
    this.colaboradorMapper = _colaboradorMapper;
  }


  public ColaboradorDto agregar(ColaboradorDto colaboradorDTO) {
    Colaborador colaborador = new Colaborador(colaboradorDTO.getId(),colaboradorDTO.getNombre(),colaboradorDTO.getFormas(),new ArrayList<>(),0L,0L,0L);
    colaborador = this.colaboradorRepository.save(colaborador);
    return colaboradorMapper.map(colaborador);
  }

  public ColaboradorDto buscarXId(Long colaboradorId) throws NoSuchElementException {
    Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
    return colaboradorMapper.map(colaborador);
  }


  public Double puntos(Long colaboradorId,Integer mes, Integer anio) throws NoSuchElementException {

    Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);

    List<ViandaDTO> viandasDTO = fachadaViandas.viandasDeColaborador(colaboradorId,mes,anio);
    Integer viandasDonadas = viandasDTO.size();
    colaborador.setViandasRepartidas(viandasDonadas.longValue());

    List<TrasladoDTO> trasladosDTO = fachadaLogistica.trasladosDeColaborador(colaboradorId,mes,anio);
    Integer traslados = trasladosDTO.size();
    colaborador.setViandasRepartidas(traslados.longValue());

    return colaborador.calcularPuntajeTotal();
  }

  public ColaboradorDto modificar(Long colaboradorId, List<TipoFormaColaborar> formas) throws NoSuchElementException {
    // Buscar el colaborador por ID
    Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
    List<FormaDeColaborar> formaDeColaborar = FormaDeColaborarUtil.convertToFormaColaborarList(formas);
    colaboradorRepository.update(colaborador,formaDeColaborar);
    return colaboradorMapper.map(colaborador);
  }

  public ColaboradorDto agregarDonacion(Long colaboradorId, DonacionDto donacionDto) throws NoSuchElementException {
    Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
    Donacion donacion = colaboradorMapper.mapDonacion(donacionDto);
    colaboradorRepository.agregarDonacion(colaborador,donacion);
    return colaboradorMapper.map(colaborador);
  }

  public void actualizarPesosPuntos(Long colaboradorId,
                                    Double pesosDonados,
                                    Double viandasDistribuidas,
                                    Double viandasDonadas,
                                    Double heladerasReparadas) {

    Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);

    var formas = colaborador.getFormas();

    for (FormaDeColaborar forma : formas) {

      TipoFormaColaborar tipoForma = FormaDeColaborarUtil.convertToTipoFormaColaborar(forma);

      switch (tipoForma) {
        case DONADOR:
          forma.setPesoPuntaje(viandasDonadas);
          break;
        case DONADORPESOS:
          forma.setPesoPuntaje(pesosDonados);
          break;
        case TRANSPORTADOR:
          forma.setPesoPuntaje(viandasDistribuidas);
          break;
        case TECNICO:
          forma.setPesoPuntaje(heladerasReparadas);
          break;
        default:
          throw new IllegalArgumentException("Forma de Colaborar desconocida: " + tipoForma);
      }
    }
    this.colaboradorRepository.save(colaborador);
  }

  public void setLogisticaProxy(FachadaLogistica fachadaLogistica) {
    this.fachadaLogistica = fachadaLogistica;
  }

  public void setViandasProxy(FachadaViandas fachadaViandas) {
    this.fachadaViandas = fachadaViandas;
  }
}
