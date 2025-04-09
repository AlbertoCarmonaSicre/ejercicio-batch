package es.neesis.demospringbatch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserStepListener extends StepExecutionListenerSupport {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Iniciando el Step: {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Finalizado el Step: {}", stepExecution.getStepName());
        log.info("Exit Status: {}", stepExecution.getExitStatus());
        log.info("Read Count: {}", stepExecution.getReadCount());
        log.info("Write Count: {}", stepExecution.getWriteCount());
        log.info("Skip Count: {}", stepExecution.getSkipCount());

        return stepExecution.getExitStatus();
    }
}
