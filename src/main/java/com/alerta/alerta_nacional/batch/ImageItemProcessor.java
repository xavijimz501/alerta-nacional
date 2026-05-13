package com.alerta.alerta_nacional.batch;

import com.alerta.alerta_nacional.Dto.PersonImageData;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ImageItemProcessor implements ItemProcessor<Resource, PersonImageData> {

    @Override
    public PersonImageData process(Resource resource) throws Exception {
        String filename = resource.getFilename();
        if (filename == null)
            return null;

        // Eliminar extensión
        String nameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'));

        // Reemplazar guiones bajos por espacios
        String personName = nameWithoutExtension.replace("_", " ").toUpperCase();

        return new PersonImageData(resource.getFile().getAbsolutePath(), personName);
    }
}
