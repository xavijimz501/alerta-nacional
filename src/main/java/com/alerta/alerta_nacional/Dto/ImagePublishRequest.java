package com.alerta.alerta_nacional.Dto;

// DTO PARA RECIBIR LA URL DE LA IMAGEN Y EL MENSAJE
public class ImagePublishRequest {
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
