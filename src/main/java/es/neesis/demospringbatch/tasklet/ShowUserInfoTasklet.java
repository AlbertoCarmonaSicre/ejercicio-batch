package es.neesis.demospringbatch.tasklet;

import es.neesis.demospringbatch.model.UserEntity;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShowUserInfoTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
        System.out.println("Mostrando información de usuarios");
        List<UserEntity> usuarios = (List<UserEntity>) chunkContext.getStepContext().getJobExecutionContext().get("users");

        usuarios.forEach(user -> System.out.println(user.toString()));

        System.out.println("Fin de la información de usuarios");

        System.out.println("Mostrando información de usuarios Actualizados");

        List<UserEntity> usuariosActualizados = (List<UserEntity>) chunkContext.getStepContext().getJobExecutionContext().get("usersUpdated");

        usuariosActualizados.forEach(user -> System.out.println(user.toString()));

        System.out.println("Fin de la información de usuarios Actualizados");

        return RepeatStatus.FINISHED;
    }
}
