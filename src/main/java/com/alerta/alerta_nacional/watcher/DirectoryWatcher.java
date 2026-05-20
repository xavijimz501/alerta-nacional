package com.alerta.alerta_nacional.watcher;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.alerta.alerta_nacional.services.FacebookPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Slf4j
public class DirectoryWatcher {

    private final FacebookPublisherService facebookPublisherService;
    private final String inputDirectory;

    public DirectoryWatcher(
            FacebookPublisherService facebookPublisherService,
            @Value("${app.watcher.input-directory:/Users/xaviersalvadorjimenezroldan/Downloads/}") String inputDirectory) {
        this.facebookPublisherService = facebookPublisherService;
        this.inputDirectory = inputDirectory;
    }

    @PostConstruct
    public void init() {
        startWatcher();
    }

    private void startWatcher() {
        new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get(inputDirectory);

                // Asegurar que el directorio existe
                File dir = path.toFile();
                if (!dir.exists()) {
                    log.info("Creando directorio de entrada: {}", inputDirectory);
                    dir.mkdirs();
                }

                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                log.info("Watcher iniciado exitosamente en la carpeta: {}", path);

                while (true) {
                    WatchKey key = watchService.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            String fileName = event.context().toString();
                            if (fileName.toLowerCase().endsWith(".png")) {
                                Path filePath = path.resolve(fileName);
                                File file = filePath.toFile();
                                log.info("Nuevo archivo PNG detectado: {}. Enviando a publicación asíncrona.",
                                        fileName);

                                // Invocar de forma no bloqueante al servicio asíncrono
                                facebookPublisherService.publishImageAsync(file);
                            }
                        }
                    }

                    key.reset();
                }
            } catch (Exception e) {
                log.error("Error crítico en el hilo del DirectoryWatcher: {}", e.getMessage(), e);
            }
        }, "DirectoryWatcher-Thread").start();
    }
}
