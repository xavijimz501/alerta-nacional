package com.alerta.alerta_nacional.controllers;

import com.alerta.alerta_nacional.services.FacebookGraphService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import java.util.Map;

@RestController
@RequestMapping("/api/facebook")
public class FacebookController {

    private final FacebookGraphService facebookGraphService;
    private final JobLauncher jobLauncher;
    private final Job postImagesJob;

    public FacebookController(FacebookGraphService facebookGraphService, JobLauncher jobLauncher, Job postImagesJob) {
        this.facebookGraphService = facebookGraphService;
        this.jobLauncher = jobLauncher;
        this.postImagesJob = postImagesJob;
    }

    // PETICION PARA EJECUTAR EL PROCESAMIENTO POR LOTES (BATCH)
    @PostMapping("/run-batch")
    public ResponseEntity<?> runBatch() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(postImagesJob, params);
            return ResponseEntity.ok("Batch Job ejecutado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error ejecutando Batch: " + e.getMessage());
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
