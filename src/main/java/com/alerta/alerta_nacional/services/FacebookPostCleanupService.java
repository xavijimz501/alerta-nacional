package com.alerta.alerta_nacional.services;

import com.alerta.alerta_nacional.dto.FacebookGraphDtos.DeleteResponse;
import com.alerta.alerta_nacional.dto.PostCleanupReportDto;
import com.alerta.alerta_nacional.dto.PostDeleteResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FacebookPostCleanupService {

    private static final String GRAPH_API_BASE = "https://graph.facebook.com/v25.0";

    private final FacebookProperties facebookProperties;
    private final RestTemplate restTemplate;

    public FacebookPostCleanupService(FacebookProperties facebookProperties) {
        this.facebookProperties = facebookProperties;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Procesa un archivo .txt con un ID de publicación por línea y ejecuta
     * la eliminación (DELETE) en la Graph API de Facebook de manera sucesiva.
     *
     * @param file Archivo MultipartFile (.txt) con IDs de publicaciones
     * @return Reporte detallado del procesamiento de eliminación
     */
    public PostCleanupReportDto processCleanupFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        log.info("Iniciando procesamiento de archivo para eliminación de publicaciones: {}", originalFilename);

        List<String> postIds = extractIdsFromFile(file);
        log.info("Se extrajeron {} IDs de publicación para eliminar desde el archivo.", postIds.size());

        PostCleanupReportDto report = PostCleanupReportDto.builder()
                .fileName(originalFilename)
                .totalIdsProcessed(postIds.size())
                .executionTimestamp(LocalDateTime.now())
                .build();

        int totalDeleted = 0;
        int totalErrors = 0;

        for (int i = 0; i < postIds.size(); i++) {
            String postId = postIds.get(i);
            log.info("[{}/{}] Procesando eliminación para Post ID: '{}'", (i + 1), postIds.size(), postId);

            PostDeleteResultDto result = deletePost(postId);
            report.getDetails().add(result);

            if ("SUCCESS".equalsIgnoreCase(result.getStatus())) {
                totalDeleted++;
            } else {
                totalErrors++;
            }
        }

        report.setTotalPostsDeleted(totalDeleted);
        report.setTotalErrors(totalErrors);

        log.info("Proceso de eliminación finalizado. Total IDs: {}, Eliminados con éxito: {}, Errores/No encontrados: {}",
                postIds.size(), totalDeleted, totalErrors);

        return report;
    }

    /**
     * Ejecuta la petición DELETE a la Graph API de Facebook para un ID de publicación específico.
     *
     * @param postId Identificador de la publicación en Facebook
     * @return Resultado de la operación para este ID
     */
    private PostDeleteResultDto deletePost(String postId) {
        String deleteUrl = UriComponentsBuilder
                .fromUriString(GRAPH_API_BASE + "/" + postId)
                .queryParam("access_token", facebookProperties.getAccessToken())
                .toUriString();

        try {
            log.info("Enviando petición DELETE a Graph API para Post ID: {}", postId);
            ResponseEntity<DeleteResponse> response = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    null,
                    DeleteResponse.class
            );

            DeleteResponse body = response.getBody();
            boolean success = body != null && body.isSuccess();

            if (success) {
                log.info("Publicación {} eliminada exitosamente de Facebook.", postId);
                return PostDeleteResultDto.builder()
                        .postId(postId)
                        .status("SUCCESS")
                        .message("Publicación eliminada exitosamente.")
                        .build();
            } else {
                log.warn("Facebook respondió sin confirmación de éxito para Post ID: {}", postId);
                return PostDeleteResultDto.builder()
                        .postId(postId)
                        .status("FAILED")
                        .message("Facebook no confirmó la eliminación de la publicación.")
                        .build();
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Publicación con ID {} no encontrada (404) en Facebook: {}", postId, e.getMessage());
            return PostDeleteResultDto.builder()
                    .postId(postId)
                    .status("NOT_FOUND")
                    .message("La publicación no existe o ya fue eliminada previamente.")
                    .build();
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("Límite de cuota alcanzado (Rate Limit) al eliminar Post ID {}: {}", postId, e.getMessage());
            return PostDeleteResultDto.builder()
                    .postId(postId)
                    .status("FAILED")
                    .message("Límite de cuota (Rate Limit) de la API de Facebook alcanzado.")
                    .build();
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Error del cliente HTTP {} al eliminar Post ID {}: {}", e.getStatusCode(), postId, errorBody);

            if (errorBody.contains("does not exist") || errorBody.contains("GraphMethodException")) {
                return PostDeleteResultDto.builder()
                        .postId(postId)
                        .status("NOT_FOUND")
                        .message("La publicación no existe o ya fue eliminada.")
                        .build();
            }

            return PostDeleteResultDto.builder()
                    .postId(postId)
                    .status("FAILED")
                    .message("Error Graph API: " + e.getStatusCode() + " - " + errorBody)
                    .build();
        } catch (ResourceAccessException e) {
            log.error("Error de conectividad/timeout con Facebook Graph API para Post ID {}: {}", postId, e.getMessage());
            return PostDeleteResultDto.builder()
                    .postId(postId)
                    .status("FAILED")
                    .message("Error de conexión con Facebook: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error inesperado al eliminar publicación con ID {}: {}", postId, e.getMessage(), e);
            return PostDeleteResultDto.builder()
                    .postId(postId)
                    .status("FAILED")
                    .message("Error interno: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Extrae y limpia los IDs del archivo .txt.
     */
    private List<String> extractIdsFromFile(MultipartFile file) throws IOException {
        List<String> ids = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Ignorar líneas vacías o comentarios que inicien con #
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    ids.add(trimmed);
                }
            }
        }
        return ids;
    }
}
