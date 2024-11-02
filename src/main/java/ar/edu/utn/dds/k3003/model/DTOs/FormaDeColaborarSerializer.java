package ar.edu.utn.dds.k3003.model.DTOs;

import ar.edu.utn.dds.k3003.model.FormaDeColaborar.FormaDeColaborar;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class FormaDeColaborarSerializer extends JsonSerializer<FormaDeColaborar> {

    @Override
    public void serialize(FormaDeColaborar formaDeColaborar, JsonGenerator jsonGenerator, SerializerProvider serializers)
            throws IOException {
        if (formaDeColaborar != null) {
            jsonGenerator.writeString(formaDeColaborar.getClass().getSimpleName()); // Solo el nombre de la clase
        }
    }
}