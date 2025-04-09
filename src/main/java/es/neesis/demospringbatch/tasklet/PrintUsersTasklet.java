package es.neesis.demospringbatch.tasklet;

import es.neesis.demospringbatch.model.UserEntity;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

public class PrintUsersTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<UserEntity> users = (List<UserEntity>) chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("users");

        if (users == null || users.isEmpty()) {
            System.out.println("No hay usuarios para mostrar.");
        } else {
            System.out.println("Mostrando usuarios desde ExecutionContext:");
            users.forEach(System.out::println);
            System.out.println("Fin del listado.");
        }

        return RepeatStatus.FINISHED;
    }
}
