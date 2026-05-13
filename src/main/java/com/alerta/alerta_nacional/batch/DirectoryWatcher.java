package com.alerta.alerta_nacional.batch;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.job.Job;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DirectoryWatcher {
    private final JobLauncher jobLauncher;
    private final Job postImagesJob;

    public DirectoryWatcher(JobLauncher jobLauncher, Job postImagesJob) {
        this.jobLauncher = jobLauncher;
        this.postImagesJob = postImagesJob;
    }

    @PostConstruct
    public void init() {
        startWatcher();
    }

    private void startWatcher() {
        new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get("/Users/xaviersalvadorjimenezroldan/Downloads/");
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                System.out.println("Watcher iniciado en carpeta: " + path);

                while (true) {
                    WatchKey key = watchService.take();

                    List<String> nuevosArchivos = new ArrayList<>();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            String fileName = event.context().toString();
                            if (fileName.toLowerCase().endsWith(".png")) {
                                nuevosArchivos.add(fileName);
                            }
                        }
                    }

                    if (!nuevosArchivos.isEmpty()) {
                        System.out.println("Archivos detectados: " + String.join("\n", nuevosArchivos));

                        JobParameters params = new JobParametersBuilder()
                                .addString("startAt", LocalDateTime.now().toString())
                                .toJobParameters();

                        jobLauncher.run(postImagesJob, params);
                        System.out.println("Job ejecutado por detección de archivos.");
                    }

                    key.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
