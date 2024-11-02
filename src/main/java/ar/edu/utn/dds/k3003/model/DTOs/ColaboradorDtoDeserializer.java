package ar.edu.utn.dds.k3003.model.DTOs;


import ar.edu.utn.dds.k3003.model.FormaDeColaborar.TipoFormaColaborar;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ColaboradorDtoDeserializer extends JsonDeserializer<ColaboradorDto> {

    @Override
    public ColaboradorDto deserialize(JsonParser jsonParser, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        // Leer el objeto JSON
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String nombre = node.get("nombre").asText();

        // Procesar las formas
        List<TipoFormaColaborar> formas = new ArrayList<>();
        if (node.has("formas")) {
            for (JsonNode formaNode : node.get("formas")) {
                // Aquí asumimos que el JSON tiene el campo "tipo" para identificar el tipo
                TipoFormaColaborar tipoForma = TipoFormaColaborar.valueOf(formaNode.asText().toUpperCase());
                formas.add(tipoForma);
            }
        }

        // Crear y devolver el DTO
        return new ColaboradorDto(nombre, formas);
    }
}