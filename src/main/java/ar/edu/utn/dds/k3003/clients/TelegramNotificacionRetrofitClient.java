package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.model.DTOs.MensajeNotificacionDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TelegramNotificacionRetrofitClient {
    @POST("/notificarA/{chatID}")
    Call<Void> enviarNotificacion(@Path("chatID") Long chatID, @Body MensajeNotificacionDTO mensaje);
}
