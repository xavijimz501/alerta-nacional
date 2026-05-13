package com.alerta.alerta_nacional.batch;

import com.alerta.alerta_nacional.Dto.PersonImageData;
import com.alerta.alerta_nacional.services.FacebookGraphService;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.io.File;

@Component
public class ImageItemWriter implements ItemWriter<PersonImageData> {

    private final FacebookGraphService facebookGraphService;

    public ImageItemWriter(FacebookGraphService facebookGraphService) {
        this.facebookGraphService = facebookGraphService;
    }

    @Override
    public void write(Chunk<? extends PersonImageData> chunk) throws Exception {
        for (PersonImageData data : chunk) {
            File file = new File(data.getFilePath());
            if (file.exists()) {
                String message = String.format("#AlertaNacional de Búsqueda ⚠️\n" + "\n" +
                        "Solicitamos apoyo para localizar a:\n" +
                        "%s\n" + "\n" +
                        "Si tienes información comunícate a los teléfonos:\n" +
                        "Desde México: 800 028 77 83\n" +
                        "WhatsApp: 55 1309 9024\n" +
                        "Desde otro país: 182 52 62 31 09\n" +
                        "#HastaEncontrarles", data.getPersonName());

                // Subir Historia y publicar post usando el nuevo método para File
                facebookGraphService.uploadPostAndStoryFromFile(file, message);

                // Subir unicamente post
                // facebookGraphService.uploadPhotoFromFile(file, message);

                // Subir unicamente historia
                // facebookGraphService.uploadPhotoStoryFromFile(file);

                // Borrar el archivo una vez procesado correctamente
                Files.deleteIfExists(file.toPath());
            }
        }
    }
}
