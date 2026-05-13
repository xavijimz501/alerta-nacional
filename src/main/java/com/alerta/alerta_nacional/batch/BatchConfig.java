package com.alerta.alerta_nacional.batch;

import java.io.File;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.core.io.FileSystemResource;
import java.util.ArrayList;
import java.util.List;
import com.alerta.alerta_nacional.Dto.PersonImageData;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.file.MultiResourceItemReader;
import org.springframework.batch.infrastructure.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.infrastructure.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.scheduling.annotation.EnableScheduling; // Descomentar para habilitar el scheduler
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
// @EnableScheduling // Descomentar para habilitar el scheduler
public class BatchConfig {

    // DIRECTORIO DENTRO DEL PROYECTO DONDE SE ENCUENTRAN LAS IMAGENES
    @Value("${batch.input.directory:/Users/xaviersalvadorjimenezroldan/Downloads/}")
    // @Value("${batch.input.directory:input/fichas/}")
    private String inputDirectory;

    @Bean
    @StepScope
    public MultiResourceItemReader<Resource> multiResourceItemReader() throws java.io.IOException {
        File dir = new File(inputDirectory);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));

        List<Resource> resourceList = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                resourceList.add(new FileSystemResource(f));
            }
        }

        return new MultiResourceItemReaderBuilder<Resource>()
                .name("imageReader")
                .resources(resourceList.toArray(new Resource[0]))
                .delegate(new ResourceAwareItemReaderItemStream<Resource>() {
                    private Resource resource;

                    @Override
                    public void setResource(Resource resource) {
                        this.resource = resource;
                    }

                    @Override
                    public Resource read() {
                        Resource res = this.resource;
                        this.resource = null;
                        return res;
                    }

                    @Override
                    public void open(ExecutionContext executionContext) {
                    }

                    @Override
                    public void update(ExecutionContext executionContext) {
                    }

                    @Override
                    public void close() {
                    }
                })
                .build();
    }

    @Bean
    public Step postImagesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            MultiResourceItemReader<Resource> reader,
            ImageItemProcessor processor,
            ImageItemWriter writer) {
        return new StepBuilder("postImagesStep", jobRepository)
                .<Resource, PersonImageData>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Job postImagesJob(JobRepository jobRepository, Step postImagesStep) {
        return new JobBuilder("postImagesJob", jobRepository)
                .start(postImagesStep)
                .build();
    }
}
