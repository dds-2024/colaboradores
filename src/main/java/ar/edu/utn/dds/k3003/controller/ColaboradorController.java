package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
import ar.edu.utn.dds.k3003.model.DTOs.ColaboradorDto;
import ar.edu.utn.dds.k3003.model.DTOs.DonacionDto;
import ar.edu.utn.dds.k3003.model.FormaDeColaborar.TipoFormaColaborar;
import ar.edu.utn.dds.k3003.model.Incidentes.SuscripcionDTO;
import ar.edu.utn.dds.k3003.model.TipoCoeficiente;
import ar.edu.utn.dds.k3003.model.UpdateFormasColaborarRequest;
import ar.edu.utn.dds.k3003.model.UpdatePesosPuntosRequest;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import io.javalin.micrometer.MicrometerPlugin;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

import java.util.List;
import java.util.NoSuchElementException;
public class ColaboradorController {

  private final Fachada fachada;
  private final Counter colaboradoresCounter;
  private final Counter colaboradoresModificadosCounter;
  private PrometheusMeterRegistry registry;

  public ColaboradorController(Fachada fachada, PrometheusMeterRegistry registry) {
    this.fachada = fachada;
    this.registry = registry;

    // Asegúrate de que el registry no sea nulo antes de registrar el contador
    if (this.registry == null) {
      throw new IllegalArgumentException("El registro de métricas no puede ser nulo");
    }

    this.colaboradoresCounter = Counter.builder("colaboradores_agregados")
            .description("Cantidad de colaboradores agregados")
            .register(this.registry);

    this.colaboradoresModificadosCounter = Counter.builder("colaboradores_modificados")
            .description("Cantidad de colaboradores modificados")
            .register(this.registry);
  }

  public void agregar(Context context) {
    try{
      var colaboradorDTO = context.bodyAsClass(ColaboradorDto.class);
      ColaboradorDto colaboradorDTORta = this.fachada.agregar(colaboradorDTO);

      colaboradoresCounter.increment();
      registry.config().commonTags("app", "metrics-colaborador");

      context.json(colaboradorDTORta);
      context.status(HttpStatus.CREATED);
    }
    catch(IllegalArgumentException ex){
      context.result(ex.getMessage());
      context.status(HttpStatus.BAD_REQUEST);
    }
    catch (NoSuchElementException ex){
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.BAD_REQUEST);
    }
  }

  public void modificar(Context context) {
    var id = context.pathParamAsClass("colaboradorId", Long.class).get();

    List<TipoFormaColaborar> formas = context.bodyAsClass(UpdateFormasColaborarRequest.class).getFormas();

    try{
      var colaboradorDTO = this.fachada.modificar(id, formas);

      colaboradoresModificadosCounter.increment();
      registry.config().commonTags("app", "metrics-colaborador");
      
      context.json(colaboradorDTO);
    } catch (NoSuchElementException ex) {
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.NOT_FOUND);
    }
  }

  public void obtener(Context context) {
    var id = context.pathParamAsClass("colaboradorId", Long.class).get();
    try {
      var colaboradorDTO = this.fachada.buscarXId(id);
      context.json(colaboradorDTO);
    } catch (NoSuchElementException ex) {
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.NOT_FOUND);
    }
  }

  public void puntos(Context context) {
    var id = context.pathParamAsClass("colaboradorId", Long.class).get();
    var anio = context.queryParamAsClass("anio", Integer.class).get();
    var mes = context.queryParamAsClass("mes", Integer.class).get();
    try {
      var puntosColaborador = this.fachada.puntos(id,mes,anio);

      context.json(puntosColaborador);
    } catch (NoSuchElementException ex) {
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.NOT_FOUND);
    }
  }

  public void actualizarPesosPuntos(Context context) {
    try {
      var id = context.pathParamAsClass("colaboradorId", Long.class).get();

      // Obtener los parámetros del cuerpo de la solicitud
      double pesosDonados = context.bodyAsClass(UpdatePesosPuntosRequest.class).getPesosDonados();
      double viandasDistribuidas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getViandasDistribuidas();
      double viandasDonadas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getViandasDonadas();
      double heladerasActivas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getHeladerasActivas();

      // Actualizar los coeficientes de puntos
      this.fachada.actualizarPesosPuntos(
          id,
          pesosDonados,
          viandasDistribuidas,
          viandasDonadas,
          heladerasActivas
      );
      context.result("Puntos correctamente actualizados");
      context.status(HttpStatus.OK);

    } catch (Exception e) {
      context.result(e.getLocalizedMessage());
      context.status(HttpStatus.BAD_REQUEST);
    }
  }

  public void gestionarIncidente(Context context){
    var notificacionIncidente = context.bodyAsClass(SuscripcionDTO.class);
    try {
      fachada.notificarIncidente(notificacionIncidente);
      context.status(HttpStatus.OK);
      context.result("Se reportó correctamente el incidente");
    } catch (NoSuchElementException ex) {
      context.result("No se pudo reportar el incidente");
      context.status(HttpStatus.NOT_ACCEPTABLE);
    }
  }

  public void recibirDonacion(Context context) {
    try {
      var id = context.pathParamAsClass("colaboradorId", Long.class).get();
      var donacionDto = context.bodyAsClass(DonacionDto.class);
      this.fachada.agregarDonacion(id,donacionDto);
      context.result("Donación recibida correctamente");
      context.status(HttpStatus.OK);
    } catch (Exception e) {
      context.result(e.getLocalizedMessage());
      context.status(HttpStatus.BAD_REQUEST);
    }
  }

  public void repararHeladera(Context context){
    var colaboradorId = context.pathParamAsClass("colaboradorId", Long.class).get();
    var heladeraId = context.pathParamAsClass("heladeraId", Long.class).get();
    try{
      fachada.repararHeladera(colaboradorId, heladeraId);
      context.status(HttpStatus.OK);
      context.result("Reparación exitosa");
    } catch (NoSuchElementException ex) {
      context.result("Error al reparar la heladera"); //ex.getLocalizedMessage());
      context.status(HttpStatus.NOT_ACCEPTABLE);
    }
  }

  public void suscripcionHeladera(Context context){
    var suscripcionDto = context.bodyAsClass(SuscripcionDTO.class);

    try {
            fachada.suscripcionHeladera(suscripcionDto);
            context.status(HttpStatus.OK);
            context.result("El colaborador " + suscripcionDto.getColaboradorId() + " se ha suscripto correctamente a la heladera " + suscripcionDto.getHeladeraId());
        } catch (NoSuchElementException ex) {
            context.result("No se suscribir a la heladera"); //ex.getLocalizedMessage());
            context.status(HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
