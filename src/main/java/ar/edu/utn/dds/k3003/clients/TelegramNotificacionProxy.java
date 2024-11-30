package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import ar.edu.utn.dds.k3003.model.DTOs.MensajeNotificacionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.HttpStatus;
import lombok.SneakyThrows;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;
import java.util.NoSuchElementException;

public class TelegramNotificacionProxy {
    private final String endpoint;
    private final TelegramNotificacionRetrofitClient service;

    public TelegramNotificacionProxy(ObjectMapper objectMapper) {
        var env = System.getenv();
        this.endpoint = env.getOrDefault("URL_TELEGRAM", "http://localhost:8081/");

        var retrofit =
                new Retrofit.Builder()
                        .baseUrl(this.endpoint)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .build();

        this.service = retrofit.create(TelegramNotificacionRetrofitClient.class);
    }

    @SneakyThrows
    public void enviarMensaje(Long chatId, String mensaje) {
        // Crear el DTO del mensaje
        MensajeNotificacionDTO mensajeDTO = new MensajeNotificacionDTO(mensaje);

        // Realizar la llamada a la API para enviar el mensaje
        Response<Void> response = service.enviarNotificacion(chatId, mensajeDTO).execute();

        if (response.isSuccessful()) {
            System.out.println("Mensaje enviado correctamente a " + chatId);
        } else {
            if (response.code() == HttpStatus.NOT_FOUND.getCode()) {
                throw new NoSuchElementException("No se pudo encontrar el chat con ID " + chatId);
            }
            throw new RuntimeException("Error al conectarse con la API de Telegram, código: " + response.code());
        }
    }
}
