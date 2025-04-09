package es.neesis.demospringbatch.processor;

import es.neesis.demospringbatch.model.Persona;
import es.neesis.demospringbatch.model.UserEntity;
import org.springframework.batch.item.ItemProcessor;

public class PersonaProcessor implements ItemProcessor<UserEntity, Persona> {

    @Override
    public Persona process(UserEntity userEntity) {
        String[] nombreSplit = userEntity.getFullName().split(" ", 2);
        String nombre = nombreSplit.length > 0 ? nombreSplit[0] : "";
        String apellido = nombreSplit.length > 1 ? nombreSplit[1] : "";

        return new Persona(
                userEntity.getId(),
                nombre,
                apellido,
                "00000000A"
        );
    }
}
