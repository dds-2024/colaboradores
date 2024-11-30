package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.clients.HeladerasProxy;
import ar.edu.utn.dds.k3003.clients.LogisticaProxy;
import ar.edu.utn.dds.k3003.clients.TelegramNotificacionProxy;
import ar.edu.utn.dds.k3003.clients.ViandasProxy;
import ar.edu.utn.dds.k3003.controller.ColaboradorController;
import ar.edu.utn.dds.k3003.facades.dtos.Constants;
import ar.edu.utn.dds.k3003.model.Incidentes.NotificadorIncidentes;
import ar.edu.utn.dds.k3003.repositories.ColaboradorMapper;
import ar.edu.utn.dds.k3003.repositories.ColaboradorRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.micrometer.MicrometerPlugin;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class WebApp {
  private static final String TOKEN = "tokenColaboradores";

  public static void main(String[] args) {
///////////metrics////////////

    final var registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    registry.config().commonTags("app", "metrics-colaborador");

    // agregamos a nuestro reigstro de métricas todo lo relacionado a infra/tech
    // de la instancia y JVM
    try (var jvmGcMetrics = new JvmGcMetrics();
         var jvmHeapPressureMetrics = new JvmHeapPressureMetrics()) {
      jvmGcMetrics.bindTo(registry);
      jvmHeapPressureMetrics.bindTo(registry);
    }
    new JvmMemoryMetrics().bindTo(registry);
    new ProcessorMetrics().bindTo(registry);
    new FileDescriptorMetrics().bindTo(registry);

    final var micrometerPlugin =
            new MicrometerPlugin(config -> config.registry = registry);

    ////////////////////////////

    ColaboradorRepository colaboradoresRepository = new ColaboradorRepository();
    EntityManagerFactory entityManagerFactory = startEntityManagerFactory();
    colaboradoresRepository.setEntityManagerFactory(entityManagerFactory);
    ColaboradorMapper colaboradorMapper = new ColaboradorMapper();
    NotificadorIncidentes notificadorIncidentes = new NotificadorIncidentes();

    var fachada = new Fachada(colaboradoresRepository,colaboradorMapper,entityManagerFactory,notificadorIncidentes);

    var objectMapper = createObjectMapper();
    fachada.setViandasProxy(new ViandasProxy(objectMapper));
    fachada.setHeladerasProxy(new HeladerasProxy(objectMapper));
    fachada.setLogisticaProxy(new LogisticaProxy(objectMapper));
    fachada.setTelegramNotificacionProxy(new TelegramNotificacionProxy(objectMapper));

    var port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8082"));

    /*var app = Javalin.create(config -> {
      config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
        configureObjectMapper(mapper);
      }));
    }).start(port);*/

    Javalin app = Javalin.create(config -> {
              config.registerPlugin(micrometerPlugin); })
            .start(port);

    //var colaboradorController = new ColaboradorController(fachada);
    var colaboradorController = new ColaboradorController(fachada,registry);

    app.post("/colaboradores", colaboradorController::agregar);
    app.get("/colaboradores/{colaboradorId}", colaboradorController::obtener);
    app.patch("/colaboradores/{colaboradorId}",colaboradorController::modificar);
    app.get("/colaboradores/{colaboradorId}/puntos",colaboradorController::puntos);
    app.put("/colaboradores/{colaboradorId}/formula",colaboradorController::actualizarPesosPuntos);
    app.put("/formula",colaboradorController::actualizarPesosPuntos);
    app.post("/colaboradores/{colaboradorId}/donar", colaboradorController::recibirDonacion);
    app.put("/colaboradores/{colaboradorId}/repararHeladera/{heladeraId}",colaboradorController::repararHeladera); //ok
    app.post("/colaboradores/gestionarIncidente", colaboradorController::gestionarIncidente);
    app.post("/colaboradores/suscripcionHeladera",colaboradorController::suscripcionHeladera);//ok

    app.get("/metrics",
            ctx -> {
              // chequear el header de authorization y chequear el token bearer
              // configurado
              var auth = ctx.header("Authorization");
              if (auth != null && auth.intern() == "Bearer " + TOKEN) {
                ctx.contentType("text/plain; version=0.0.4").result(registry.scrape());
              } else {
                // si el token no es el apropiado, devolver error,
                // desautorizado
                // este paso es necesario para que Grafana online
                // permita el acceso
                ctx.status(401).json("unauthorized access");
              }
            });
    //app.delete("/colaboradores", colaboradorController::cleanUp);
  }

  public static ObjectMapper createObjectMapper() {
    var objectMapper = new ObjectMapper();
    configureObjectMapper(objectMapper);
    return objectMapper;
  }

  public static void configureObjectMapper(ObjectMapper objectMapper) {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    var sdf = new SimpleDateFormat(Constants.DEFAULT_SERIALIZATION_FORMAT, Locale.getDefault());
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    objectMapper.setDateFormat(sdf);
  }

  public static EntityManagerFactory startEntityManagerFactory() {
    Map<String, String> env = System.getenv();
    Map<String, Object> configOverrides = new HashMap<String, Object>();
    String[] keys = new String[] { "javax.persistence.jdbc.url", "javax.persistence.jdbc.user",
            "javax.persistence.jdbc.password", "javax.persistence.jdbc.driver", "hibernate.hbm2ddl.auto",
            "hibernate.connection.pool_size", "hibernate.show_sql" };
    for (String key : keys) {
      if (env.containsKey(key)) {
        String value = env.get(key);
        configOverrides.put(key, value);
      }
    }
    return Persistence.createEntityManagerFactory("bd_colaboradores_f5t3", configOverrides);
  }
}
