package es.neesis.demospringbatch.processor;

import es.neesis.demospringbatch.model.Persona;
import es.neesis.demospringbatch.model.UserEntity;
import org.springframework.batch.item.ItemProcessor;

public class UserEntityToPersonaProcessor implements ItemProcessor<UserEntity, Persona> {

    @Override
    public Persona process(UserEntity user) {
        // Suponemos que el fullName tiene nombre y apellido separados por espacio
        String[] nombreApellido = user.getFullName().split(" ", 2);
        String nombre = nombreApellido.length > 0 ? nombreApellido[0] : "";
        String apellido = nombreApellido.length > 1 ? nombreApellido[1] : "";

        return new Persona(
                user.getId(),
                nombre,
                apellido,
                "00000000X" // Valor dummy para DNI
        );
    }
}
