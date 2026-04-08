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

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;

@Service
public class FacebookGraphService {

    private final FacebookProperties facebookProperties;
    private final RestTemplate restTemplate;

    public FacebookGraphService(FacebookProperties facebookProperties) {
        this.facebookProperties = facebookProperties;
        this.restTemplate = new RestTemplate();
    }

    // METODO PARA PUBLICAR LA IMAGEN CON URL
    public Map<String, Object> publishPhoto(String imageUrl, String message) {
        String url = "https://graph.facebook.com/v25.0/" + facebookProperties.getId() + "/photos";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("url", imageUrl);
        if (message != null && !message.isEmpty()) {
            map.add("message", message);
        }
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

    // METODO PARA SUBIR LA IMAGEN EN LOCAL
    public Map<String, Object> uploadPhoto(MultipartFile imageFile, String message) throws java.io.IOException {
        String url = "https://graph.facebook.com/v25.0/" + facebookProperties.getId() + "/photos";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        // Se convierte el MultipartFile a Resource porque WebClient/RestTemplate lo
        // requieren
        ByteArrayResource fileAsResource = new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        };

        map.add("source", fileAsResource);
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

    // METODO PARA PUBLICAR UNA HISTORIA CON URL DE IMAGEN
    public Map<String, Object> publishPhotoStory(String imageUrl) {
        // Paso 1: Subir la foto como no publicada
        String photoId = uploadUnpublishedPhoto(imageUrl);

        // Paso 2: Publicar como historia
        return publishStoryWithPhotoId(photoId);
    }

    // METODO PARA PUBLICAR UNA HISTORIA CON ARCHIVO LOCAL
    public Map<String, Object> uploadPhotoStory(MultipartFile imageFile) throws java.io.IOException {
        // Paso 1: Subir la foto como no publicada
        String photoId = uploadUnpublishedPhotoFromFile(imageFile);

        // Paso 2: Publicar como historia
        return publishStoryWithPhotoId(photoId);
    }

    // METODO PARA PUBLICAR POST + HISTORIA CON URL DE IMAGEN
    public Map<String, Object> publishPostAndStory(String imageUrl, String message) {
        // Publicar el post con mensaje
        Map<String, Object> postResponse = publishPhoto(imageUrl, message);

        // Publicar la historia sin mensaje
        Map<String, Object> storyResponse = publishPhotoStory(imageUrl);

        // Combinar las respuestas
        Map<String, Object> combined = new HashMap<>();
        combined.put("post", postResponse);
        combined.put("story", storyResponse);
        return combined;
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

    // HELPER: Subir foto no publicada con URL y obtener photo_id
    private String uploadUnpublishedPhoto(String imageUrl) {
        String url = "https://graph.facebook.com/v25.0/" + facebookProperties.getId() + "/photos";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("url", imageUrl);
        map.add("published", "false");
        map.add("access_token", facebookProperties.getAccessToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

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

    // HELPER: Subir foto no publicada desde archivo local y obtener photo_id
    private String uploadUnpublishedPhotoFromFile(MultipartFile imageFile) throws java.io.IOException {
        String url = "https://graph.facebook.com/v25.0/" + facebookProperties.getId() + "/photos";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        ByteArrayResource fileAsResource = new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        };

        map.add("source", fileAsResource);
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
