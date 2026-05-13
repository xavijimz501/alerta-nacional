package com.alerta.alerta_nacional.batch;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job postImagesJob;

    public BatchScheduler(JobLauncher jobLauncher, Job postImagesJob) {
        this.jobLauncher = jobLauncher;
        this.postImagesJob = postImagesJob;
    }

    // Ejecuta cada 20 minutos de cada hora ("0 0/20 * * * *")
    @Scheduled(cron = "0 0/20 * * * *")
    public void runJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    // .addString("startAt", LocalDateTime.now().toString())
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(postImagesJob, params);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println("Job ejecutado por Scheduler a las: " + LocalDateTime.now().format(formatter));
            // System.out.println("Job ejecutado por Scheduler a las: " +
            // LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Error ejecutando Batch desde Scheduler: " + e.getMessage());
        }
    }
}
