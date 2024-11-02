package ar.edu.utn.dds.k3003.model.FormaDeColaborar;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class FormaDeColaborarConverter implements AttributeConverter<FormaDeColaborar, String> {

    @Override
    public String convertToDatabaseColumn(FormaDeColaborar formaDeColaborar) {
        if (formaDeColaborar == null) {
            return null;
        }
        // Convertir a String usando el tipo de forma de colaborar
        // Suponiendo que tienes un método para obtener el tipo de forma de colaborar
        return formaDeColaborar.getClass().getSimpleName(); // Retorna el nombre de la clase, pero puedes modificar esto según tu necesidad
    }

    @Override
    public FormaDeColaborar convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        // Usar el enum TipoFormaColaborar para crear instancias
        TipoFormaColaborar tipo = TipoFormaColaborar.valueOf(dbData.toUpperCase()); // Convierte a mayúsculas si es necesario

        // Usar la factory para crear instancias de forma de colaborar
        FormaDeColaborarUtil factory = new FormaDeColaborarUtil(); // Instanciar la factory

        // Crear y retornar la instancia correspondiente
        return FormaDeColaborarUtil.createFormaDeColaborar(tipo);
    }
}