package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.model.Incidentes.SuscripcionDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HeladerasRetrofitClient {
    @POST("/heladeras/suscripciones")
    Call<Void> suscribir(@Body SuscripcionDTO suscripcionDTO);
}