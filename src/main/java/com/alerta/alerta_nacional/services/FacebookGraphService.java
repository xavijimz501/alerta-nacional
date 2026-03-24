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
}
