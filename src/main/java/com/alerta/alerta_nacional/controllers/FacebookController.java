package com.alerta.alerta_nacional.controllers;

import com.alerta.alerta_nacional.Dto.ImagePublishRequest;
import com.alerta.alerta_nacional.services.FacebookGraphService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/facebook")
public class FacebookController {

    private final FacebookGraphService facebookGraphService;

    public FacebookController(FacebookGraphService facebookGraphService) {
        this.facebookGraphService = facebookGraphService;
    }

    // PETICION CON URL DE LA IMAGEN
    @PostMapping("/publish-image")
    public ResponseEntity<?> publishImage(@RequestBody ImagePublishRequest request) {
        try {
            Map<String, Object> response = facebookGraphService.publishPhoto(request.getImageUrl(),
                    request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
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

    // PETICION PARA SUBIR HISTORIA CON URL DE LA IMAGEN
    @PostMapping("/publish-story")
    public ResponseEntity<?> publishStory(@RequestBody ImagePublishRequest request) {
        try {
            Map<String, Object> response = facebookGraphService.publishPhotoStory(request.getImageUrl());
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

    // PETICION PARA SUBIR POST + HISTORIA CON URL DE LA IMAGEN (solo el post lleva
    // mensaje)
    @PostMapping("/publish-post-and-story")
    public ResponseEntity<?> publishPostAndStory(@RequestBody ImagePublishRequest request) {
        try {
            Map<String, Object> response = facebookGraphService.publishPostAndStory(
                    request.getImageUrl(), request.getMessage());
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
