package com.alerta.alerta_nacional.controllers;

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

    public static class ImagePublishRequest {
        private String imageUrl;
        private String message;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
