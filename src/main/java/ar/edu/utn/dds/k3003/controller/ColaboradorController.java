package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.ColaboradorDTO;
import ar.edu.utn.dds.k3003.facades.dtos.FormaDeColaborarEnum;
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
  }

  public void agregar(Context context) {
    try{
      var colaboradorDTO = context.bodyAsClass(ColaboradorDTO.class);
      var colaboradorDTORta = this.fachada.agregar(colaboradorDTO);

      colaboradoresCounter.increment();
      registry.config().commonTags("app", "metrics-colaborador");

      context.json(colaboradorDTORta);
      context.status(HttpStatus.CREATED);
    }
    catch (NoSuchElementException ex){
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.BAD_REQUEST);
    }
  }

  public void modificar(Context context) {
    var id = context.pathParamAsClass("colaboradorId", Long.class).get();

    List<FormaDeColaborarEnum> formas = context.bodyAsClass(UpdateFormasColaborarRequest.class).getFormas();

    try{
      var colaboradorDTO = this.fachada.modificar(id, formas);
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
    try {
      var puntosColaborador = this.fachada.puntos(id);
      context.json(puntosColaborador);
    } catch (NoSuchElementException ex) {
      context.result(ex.getLocalizedMessage());
      context.status(HttpStatus.NOT_FOUND);
    }
  }

  public void actualizarPesosPuntos(Context context) {
    try {
      // Obtener los parámetros del cuerpo de la solicitud
      double pesosDonados = context.bodyAsClass(UpdatePesosPuntosRequest.class).getPesosDonados();
      double viandasDistribuidas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getViandasDistribuidas();
      double viandasDonadas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getViandasDonadas();
      double tarjetasRepartidas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getTarjetasRepartidas();
      double heladerasActivas = context.bodyAsClass(UpdatePesosPuntosRequest.class).getHeladerasActivas();

      // Actualizar los coeficientes de puntos
      this.fachada.actualizarPesosPuntos(
          pesosDonados,
          viandasDistribuidas,
          viandasDonadas,
          tarjetasRepartidas,
          heladerasActivas
      );
      context.result("Puntos correctamente actualizados");
      context.status(HttpStatus.OK);

    } catch (Exception e) {
      context.result(e.getLocalizedMessage());
      context.status(HttpStatus.BAD_REQUEST);
    }
  }
}
