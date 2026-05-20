package com.alerta.alerta_nacional.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@Service
public class FacebookGraphService {

    private final FacebookProperties facebookProperties;
    private final RestTemplate restTemplate;

    public FacebookGraphService(FacebookProperties facebookProperties) {
        this.facebookProperties = facebookProperties;
        this.restTemplate = new RestTemplate();
    }

    // METODO PARA SUBIR LA IMAGEN EN LOCAL
    public Map<String, Object> uploadPhoto(MultipartFile imageFile, String message) throws java.io.IOException {
        return uploadPhotoFromResource(new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        }, message);
    }

    // METODO PARA SUBIR IMAGEN DESDE UN ARCHIVO LOCAL (usado por el Watcher)
    public Map<String, Object> uploadPhotoFromFile(File file, String message) throws java.io.IOException {
        return uploadPhotoFromResource(new FileSystemResource(file), message);
    }

    // HELPER PARA EVITAR DUPLICACION DE LOGICA DE SUBIDA
    private Map<String, Object> uploadPhotoFromResource(Resource resource, String message) {
        String url = "https://graph.facebook.com/v25.0/" + facebookProperties.getId() + "/photos";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("source", resource);
        if (message != null && !message.isEmpty()) {
            map.add("message", message);
        }
        map.add("access_token", facebookProperties.getAccessToken());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        return response.getBody();
    }

    // METODO PARA PUBLICAR UNA HISTORIA CON ARCHIVO LOCAL
    public Map<String, Object> uploadPhotoStory(MultipartFile imageFile) throws java.io.IOException {
        // Paso 1: Subir la foto como no publicada
        String photoId = uploadUnpublishedPhotoFromFile(imageFile);

        // Paso 2: Publicar como historia
        return publishStoryWithPhotoId(photoId);
    }

    // METODO PARA PUBLICAR POST + HISTORIA CON ARCHIVO LOCAL
    public Map<String, Object> uploadPostAndStory(MultipartFile imageFile, String message)
            throws java.io.IOException {
        // Publicar el post con mensaje
        Map<String, Object> postResponse = uploadPhoto(imageFile, message);

        // Publicar la historia sin mensaje
        Map<String, Object> storyResponse = uploadPhotoStory(imageFile);

        // Combinar las respuestas
        Map<String, Object> combined = new HashMap<>();
        combined.put("post", postResponse);
        combined.put("story", storyResponse);
        return combined;
    }

    // METODO PARA PUBLICAR UNA HISTORIA CON File
    public Map<String, Object> uploadPhotoStoryFromFile(File file) {
        String photoId = uploadUnpublishedPhotoFromResource(new FileSystemResource(file));
        return publishStoryWithPhotoId(photoId);
    }

    // METODO PARA PUBLICAR POST + HISTORIA CON File
    public Map<String, Object> uploadPostAndStoryFromFile(File file, String message) throws java.io.IOException {
        // Publicar el post con mensaje
        Map<String, Object> postResponse = uploadPhotoFromFile(file, message);

        // Publicar la historia sin mensaje
        Map<String, Object> storyResponse = uploadPhotoStoryFromFile(file);

        // Combinar las respuestas
        Map<String, Object> combined = new HashMap<>();
        combined.put("post", postResponse);
        combined.put("story", storyResponse);
        return combined;
    }

    // HELPER: Subir foto no publicada desde archivo local y obtener photo_id
    private String uploadUnpublishedPhotoFromFile(MultipartFile imageFile) throws java.io.IOException {
        ByteArrayResource fileAsResource = new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        };
        return uploadUnpublishedPhotoFromResource(fileAsResource);
    }

    // HELPER: Subir foto no publicada desde recurso y obtener photo_id
    private String uploadUnpublishedPhotoFromResource(Resource resource) {
        String url = "https://graph.facebook.com/v25.0/" + facebookProperties.getId() + "/photos";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        map.add("source", resource);
        map.add("published", "false");
        map.add("access_token", facebookProperties.getAccessToken());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("id")) {
            throw new RuntimeException("No se pudo obtener el photo_id de la foto subida");
        }
        return body.get("id").toString();
    }

    // HELPER: Publicar historia con el photo_id
    private Map<String, Object> publishStoryWithPhotoId(String photoId) {
        String url = "https://graph.facebook.com/v25.0/" + facebookProperties.getId() + "/photo_stories";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("photo_id", photoId);
        map.add("access_token", facebookProperties.getAccessToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        return response.getBody();
    }
}
