package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.clients.HeladerasProxy;
import ar.edu.utn.dds.k3003.clients.TelegramNotificacionProxy;
import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
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
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.*;
import ar.edu.utn.dds.k3003.model.Incidentes.Incidente;
import ar.edu.utn.dds.k3003.model.Incidentes.NotificadorIncidentes;
import ar.edu.utn.dds.k3003.model.Incidentes.SuscripcionDTO;
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
  private HeladerasProxy fachadaHeladeras;
  private TelegramNotificacionProxy fachadaTelegram;
  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;
  public NotificadorIncidentes notificador;

  public Fachada() {
    this.entityManagerFactory = Persistence.createEntityManagerFactory("bd_colaboradores_f5t3");
    this.entityManager = entityManagerFactory.createEntityManager();
    this.colaboradorRepository = new ColaboradorRepository();
    this.colaboradorRepository.setEntityManager(entityManager);
    this.colaboradorMapper = new ColaboradorMapper();
    this.notificador = new NotificadorIncidentes();
  }

  public Fachada(ColaboradorRepository _colaboradorRepository, ColaboradorMapper _colaboradorMapper, EntityManagerFactory entityManagerFactory, NotificadorIncidentes _notificador) {
    this.entityManagerFactory = entityManagerFactory;
    this.colaboradorRepository = _colaboradorRepository;
    colaboradorRepository.setEntityManagerFactory(entityManagerFactory);
    colaboradorRepository.setEntityManager(entityManagerFactory.createEntityManager());
    this.colaboradorMapper = _colaboradorMapper;
    this.notificador = _notificador;
  }


  public ColaboradorDto agregar(ColaboradorDto colaboradorDTO) {
    Colaborador colaborador = new Colaborador(colaboradorDTO.getId(),colaboradorDTO.getChat_id(),colaboradorDTO.getNombre(),colaboradorDTO.getFormas(),new ArrayList<>(),0L,0L,0L);
    colaborador = this.colaboradorRepository.save(colaborador);
    return colaboradorMapper.map(colaborador);
  }

  public ColaboradorDto buscarXId(Long colaboradorId) throws NoSuchElementException {
    Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);
    return colaboradorMapper.map(colaborador);
  }

  public void notificarIncidente(SuscripcionDTO notificacionIncidente){
    Incidente incidente = new Incidente(Long.valueOf(notificacionIncidente.getHeladeraId()),notificacionIncidente.getTipoSuscripcion());
    Colaborador colaborador = this.colaboradorRepository.findById(notificacionIncidente.getColaboradorId().longValue());
    fachadaTelegram.enviarMensaje(colaborador.getChat_id(),construirMensajeDeAlerta(incidente,colaboradorMapper.map(colaborador)));
    //notificador.notificar(incidente,colaboradorMapper.map(colaborador));
  }

  private String construirMensajeDeAlerta(Incidente incidente, ColaboradorDto colaboradorDTO) {
    return String.format("Se notifica al colaborador %d que la heladera %d sufrió un incidente de tipo %s%n",
            colaboradorDTO.getId(),
            incidente.getHeladeraId(),
            incidente.getTipoAlerta());
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

  public void repararHeladera(Long colaboradorId, Long heladeraId) {
    Colaborador colaborador = this.colaboradorRepository.findById(colaboradorId);

    // Verificar si el colaborador tiene la forma de colaborar de tipo Tecnico
    boolean esTecnico = colaborador.getFormas().stream()
            .anyMatch(forma -> forma instanceof Tecnico);

    if (esTecnico) {
      // Informar a heladeras
      // Persistir en BD
      colaboradorRepository.añadirReparoHeladera(colaborador, colaborador.getHeladerasReparadas() + 1);
    } else {
      throw new NoSuchElementException("El colaborador debe tener Tecnico en sus formas de colaborar ");
    }
  }

   public void suscripcionHeladera(SuscripcionDTO suscripcion){
        fachadaHeladeras.suscribir(suscripcion);
  }


  public void setLogisticaProxy(FachadaLogistica fachadaLogistica) {
    this.fachadaLogistica = fachadaLogistica;
  }

  public void setViandasProxy(FachadaViandas fachadaViandas) {
    this.fachadaViandas = fachadaViandas;
  }

  public void setHeladerasProxy(HeladerasProxy fachadaHeladeras) {
    this.fachadaHeladeras = fachadaHeladeras;
  }

  public void setTelegramNotificacionProxy(TelegramNotificacionProxy telegramNotificacionProxy) {
    this.fachadaTelegram = telegramNotificacionProxy;
  }

}
