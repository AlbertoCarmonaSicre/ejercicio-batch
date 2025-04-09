package es.neesis.demospringbatch.config.batch;

import es.neesis.demospringbatch.listener.UserStepListener;
import es.neesis.demospringbatch.model.Persona;
import es.neesis.demospringbatch.processor.PersonaProcessor;
import es.neesis.demospringbatch.processor.UserEditProcessor;
import es.neesis.demospringbatch.tasklet.PrintUsersTasklet;
import es.neesis.demospringbatch.tasklet.ShowUserInfoTasklet;
import es.neesis.demospringbatch.writer.PersonaCsvWriter;
import es.neesis.demospringbatch.writer.UserUpdaterWriter;
import es.neesis.demospringbatch.writer.UserWriter;
import lombok.RequiredArgsConstructor;
import es.neesis.demospringbatch.dto.User;
import es.neesis.demospringbatch.listener.UserExecutionListener;
import es.neesis.demospringbatch.model.UserEntity;
import es.neesis.demospringbatch.processor.UserProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public ItemReader<User> reader() {
        return new FlatFileItemReaderBuilder<User>()
                .name("userItemReader")
                .resource(new ClassPathResource("sample.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "username", "password", "email", "name", "surname")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<User>() {{
                    setTargetType(User.class);
                }}).build();
    }

    @Bean
    public ItemReader<User> readerBDD(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<User>()
                .name("readerBDD")
                .dataSource(dataSource)
                .sql("SELECT id, username, password, email, fullName, createdAt FROM users")
                .rowMapper((rs, rowNum) -> {
                    String fullName = rs.getString("fullName");
                    String[] nameParts = fullName != null ? fullName.split(" ", 2) : new String[]{"", ""};
                    return User.builder()
                            .id(rs.getString("id"))
                            .username(rs.getString("username"))
                            .password(rs.getString("password"))
                            .email(rs.getString("email"))
                            .name(nameParts[0])
                            .surname(nameParts.length > 1 ? nameParts[1] : "")
                            .build();
                })
                .build();
    }

    @Bean
    public ItemReader<UserEntity> readerFromUsers(DataSource dataSource){
        return new JdbcCursorItemReaderBuilder<UserEntity>()
                .name("readerFromUsers")
                .dataSource(dataSource)
                .sql("SELECT id, username, password, email, fullName, createdAt FROM users")
                .rowMapper(new BeanPropertyRowMapper<>(UserEntity.class))
                .build();
    }

    @Bean
    public UserProcessor processor() {
        return new UserProcessor();
    }

    @Bean
    public UserEditProcessor editProcessor() {
        return new UserEditProcessor();
    }

    @Bean
    public Tasklet printUsersTasklet() {
        return new PrintUsersTasklet();
    }

    @Bean
    public ItemWriter<UserEntity> writer(DataSource dataSource) {
        return new UserWriter(dataSource);
    }

    @Bean
    public ItemWriter<UserEntity> updateWriter(DataSource dataSource) {
        return new UserUpdaterWriter(dataSource);
    }

    @Bean
    public PersonaProcessor personaProcessor() {
        return new PersonaProcessor();
    }

    @Bean
    public ItemWriter<Persona> personaCsvWriter() {
        return new PersonaCsvWriter();
    }

    @Bean
    public Job importUserJob(UserExecutionListener listener, Step step1, Step step2, Step step3, Step step4, Step step5) {
        return jobBuilderFactory.get("importUserJob")
                .listener(listener)
                .start(step1)
                .next(step2)
                .next(step3)
                .next(step4)
                .next(step5)
                .build();
    }

    @Bean
    public Step step1(ItemReader<User> reader, ItemWriter<UserEntity> writer, ItemProcessor<User, UserEntity> processor, UserStepListener listener) {
        return stepBuilderFactory.get("step1")
                .<User, UserEntity>chunk(4) // El processor se ejecutará cada 2 registros de manera secuencial
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(listener)
                .build();
    }

    @Bean
    public Step step2(ItemReader<User> readerBDD, ItemWriter<UserEntity> updateWriter, ItemProcessor<User, UserEntity> editProcessor, UserStepListener listener) {
        return stepBuilderFactory.get("step2")
                .<User, UserEntity>chunk(2) // El processor se ejecutará cada 2 registros de manera secuencial
                .reader(readerBDD)
                .processor(editProcessor)
                .writer(updateWriter)
                .listener(listener)
                .build();
    }

    @Bean
    public Step step3(ShowUserInfoTasklet showUserInfoTasklet, UserStepListener listener) {
        return stepBuilderFactory.get("step3")
                .tasklet(showUserInfoTasklet).listener(listener)
                .build();
    }

    @Bean
    public Step step4(Tasklet printUsersTasklet, UserStepListener listener) {
        return stepBuilderFactory.get("step4")
                .tasklet(printUsersTasklet)
                .listener(listener)
                .build();
    }

    @Bean
    public Step step5(ItemReader<UserEntity> readerFromUsers, ItemProcessor<UserEntity, Persona> personaProcessor, ItemWriter<Persona> personaCsvWriter, UserStepListener listener) {
        return stepBuilderFactory.get("step5")
                .<UserEntity, Persona>chunk(10)
                .reader(readerFromUsers)
                .processor(personaProcessor)
                .writer(personaCsvWriter)
                .listener(listener)
                .build();
    }

}
