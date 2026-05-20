package com.alerta.alerta_nacional.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;

@Service
@Slf4j
public class FacebookPublisherService {

    private final FacebookGraphService facebookGraphService;

    public FacebookPublisherService(FacebookGraphService facebookGraphService) {
        this.facebookGraphService = facebookGraphService;
    }

    /**
     * Procesa una imagen asíncronamente extrayendo el nombre del archivo,
     * estructurando el mensaje de Alerta Nacional, publicándolo en Facebook
     * y eliminando el archivo procesado.
     *
     * @param file El archivo de la imagen a publicar.
     */
    @Async
    public void publishImageAsync(File file) {
        try {
            if (file == null || !file.exists()) {
                log.warn("El archivo especificado es nulo o no existe.");
                return;
            }

            String filename = file.getName();
            log.info("Iniciando procesamiento asíncrono para el archivo: {}", filename);

            // Eliminar extensión
            int lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex == -1) {
                log.warn("El archivo no tiene una extensión válida: {}", filename);
                return;
            }
            String nameWithoutExtension = filename.substring(0, lastDotIndex);

            // Reemplazar guiones bajos por espacios y convertir a mayúsculas
            String personName = nameWithoutExtension.replace("_", " ").toUpperCase();

            // Estructurar el mensaje de la Alerta
            String message = String.format("#AlertaNacional de Búsqueda ⚠️\n\n" +
                    "Solicitamos apoyo para localizar a:\n" +
                    "%s\n\n" +
                    "Si tienes información comunícate a los teléfonos:\n" +
                    "Desde México: 800 028 77 83\n" +
                    "WhatsApp: 55 1309 9024\n" +
                    "Desde otro país: 182 52 62 31 09\n" +
                    "#HastaEncontrarles", personName);

            log.info("Publicando en Facebook para: {}", personName);

            // Delegar la petición HTTP a la capa de infraestructura
            // facebookGraphService.uploadPhotoFromFile(file, message);

            // Subir Historia y publicar post usando el nuevo método para File
            facebookGraphService.uploadPostAndStoryFromFile(file, message);

            log.info("Publicación exitosa en Facebook para: {}", personName);

            // Eliminar el archivo procesado para evitar lecturas duplicadas
            boolean deleted = Files.deleteIfExists(file.toPath());
            if (deleted) {
                log.info("Archivo eliminado correctamente: {}", file.getAbsolutePath());
            } else {
                log.warn("No se pudo eliminar el archivo o ya no existía: {}", file.getAbsolutePath());
            }

        } catch (Exception e) {
            log.error("Error al procesar/publicar la imagen {}: {}",
                    file != null ? file.getName() : "nulo", e.getMessage(), e);
        }
    }
}
