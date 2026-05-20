package com.alerta.alerta_nacional.controllers;

import com.alerta.alerta_nacional.services.FacebookGraphService;
import com.alerta.alerta_nacional.services.FacebookPublisherService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/api/facebook")
public class FacebookController {

    private final FacebookGraphService facebookGraphService;
    private final FacebookPublisherService facebookPublisherService;
    private final String inputDirectory;

    public FacebookController(
            FacebookGraphService facebookGraphService,
            FacebookPublisherService facebookPublisherService,
            @Value("${batch.input.directory:/Users/xaviersalvadorjimenezroldan/Downloads/}") String inputDirectory) {
        this.facebookGraphService = facebookGraphService;
        this.facebookPublisherService = facebookPublisherService;
        this.inputDirectory = inputDirectory;
    }

    // PETICION PARA EJECUTAR EL PROCESAMIENTO (AHORA ASINCRONO POR LOTES EN CARPETA)
    @PostMapping("/run-batch")
    public ResponseEntity<?> runBatch() {
        try {
            File dir = new File(inputDirectory);
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null && files.length > 0) {
                for (File file : files) {
                    facebookPublisherService.publishImageAsync(file);
                }
                return ResponseEntity.ok("Procesamiento asíncrono iniciado para " + files.length + " imágenes en la carpeta.");
            }
            return ResponseEntity.ok("No se encontraron imágenes pendientes (.png) para procesar en: " + inputDirectory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al iniciar el procesamiento: " + e.getMessage());
        }
    }

    // PETICION CON LA IMAGEN EN LOCAL
    @PostMapping(value = "/upload-image", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "message", required = false) String message) {
        try {
            Map<String, Object> response = facebookGraphService.uploadPhoto(file, message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // PETICION PARA SUBIR HISTORIA CON IMAGEN LOCAL
    @PostMapping(value = "/upload-story", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadStory(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> response = facebookGraphService.uploadPhotoStory(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // PETICION PARA SUBIR POST + HISTORIA CON IMAGEN LOCAL (solo el post lleva
    // mensaje)
    @PostMapping(value = "/upload-post-and-story", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadPostAndStory(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "message", required = false) String message) {
        try {
            Map<String, Object> response = facebookGraphService.uploadPostAndStory(file, message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
