package ar.edu.utn.dds.k3003.model.FormaDeColaborar;

import java.util.List;
import java.util.stream.Collectors;

public class FormaDeColaborarUtil{

    public static FormaDeColaborar createFormaDeColaborar(TipoFormaColaborar tipo) {
        switch (tipo) {
            case DONADORPESOS:
                return new DonadorPesos();
            case TRANSPORTADOR:
                return new Transportador();
            case TECNICO:
                return new Tecnico();
            case DONADOR:
                return new Donador();
            default:
                throw new IllegalArgumentException("Forma de Colaborar desconocida: " + tipo);
        }
    }

    // Convertir de FormaDeColaborar a TipoFormaColaborar
    public static TipoFormaColaborar convertToTipoFormaColaborar(FormaDeColaborar forma) {
        if (forma instanceof DonadorPesos) {
            return TipoFormaColaborar.DONADORPESOS;
        } else if (forma instanceof Transportador) {
            return TipoFormaColaborar.TRANSPORTADOR;
        } else if (forma instanceof Tecnico) {
            return TipoFormaColaborar.TECNICO;
        } else if (forma instanceof Donador) {
            return TipoFormaColaborar.DONADOR;
        } else {
            throw new IllegalArgumentException("Forma de colaborar desconocida: " + forma.getClass().getSimpleName());
        }
    }

    // Convertir una lista de FormaDeColaborar a una lista de TipoFormaColaborar
    public static List<TipoFormaColaborar> convertToTipoFormaColaborarList(List<FormaDeColaborar> formas) {
        return formas.stream()
                .map(FormaDeColaborarUtil::convertToTipoFormaColaborar)
                .collect(Collectors.toList());
    }

    public static List<FormaDeColaborar> convertToFormaColaborarList(List<TipoFormaColaborar> tiposFormasColaborar) {
        return tiposFormasColaborar.stream()
                .map(FormaDeColaborarUtil::createFormaDeColaborar)
                .collect(Collectors.toList());
    }
}