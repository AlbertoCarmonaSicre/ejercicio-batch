package es.neesis.demospringbatch.writer;

import es.neesis.demospringbatch.model.Persona;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;

import java.util.List;
import java.util.stream.Collectors;

public class PersonaCsvWriter implements ItemWriter<Persona> {

    private ExecutionContext executionContext;
    private final FlatFileItemWriter<Persona> delegate;

    public PersonaCsvWriter() {
        // Configuración del writer con tu estilo
        BeanWrapperFieldExtractor<Persona> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"idPersona", "nombre", "apellido", "dni"});

        DelimitedLineAggregator<Persona> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(extractor);

        this.delegate = new FlatFileItemWriterBuilder<Persona>()
                .name("personaCsvWriter")
                .resource(new FileSystemResource("output/personas.csv")) // Carpeta output, crea si no existe
                .lineAggregator(aggregator)
                .headerCallback(writer -> writer.write("idPersona,nombre,apellido,dni"))
                .append(false)
                .build();
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getExecutionContext();
        try {
            this.delegate.open(this.executionContext);
        } catch (Exception e) {
            throw new RuntimeException("Error al abrir el writer de personas", e);
        }
    }

    @Override
    public void write(List<? extends Persona> list) throws Exception {
        List<Persona> personas = list.stream().map(Persona.class::cast).collect(Collectors.toList());
        delegate.write(personas);

        List<Persona> acumuladas = (List<Persona>) executionContext.get("personasExportadas");
        if (acumuladas == null) {
            acumuladas = personas;
        } else {
            acumuladas.addAll(personas);
        }
        executionContext.put("personasExportadas", acumuladas);
    }
}
